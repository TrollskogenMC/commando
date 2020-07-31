package se.hornta.commando;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class CommandSet {
  private Commando commando;
  private CarbonCommand root;
  private Map<String, CommandSet> childCommands = new HashMap<>();

  CommandSet(Commando commando) {
    this.commando = commando;
  }

  public void setCommand(CarbonCommand command) {
    if (root != null) {
      Bukkit.getLogger().severe("A root command already exist!");
      return;
    }
    root = command;
  }

  Boolean hasChildCommand(String name) {
    return childCommands.containsKey(name.toLowerCase(Locale.ENGLISH));
  }

  private Boolean hasAnyChildCommands() {
    return !childCommands.isEmpty();
  }

  CommandSet getCommandSet(String name) {
    String lowerName = name.toLowerCase(Locale.ENGLISH);
    if (!childCommands.containsKey(lowerName)) {
      return null;
    }

    return childCommands.get(lowerName);
  }

  public CarbonCommand getCommand() {
    return root;
  }

  CommandSet createChildCommand(String name) {
    CommandSet set = new CommandSet(commando);
    childCommands.put(name.toLowerCase(Locale.ENGLISH), set);

    return set;
  }

  void dispatch(CommandSender sender, List<String> args) {
    boolean hasHandler = root != null && root.hasHandler();
    if (args.isEmpty()) {
      if(hasHandler) {
        new CommandProcessor(commando, root, sender, args);
        //root.process(sender, new String[0]);
      } else {
        handleMissingArgument(sender, "");
      }
    } else {
      String childCommand = getChildCommand(args.get(0));
      if (childCommand != null) {
        childCommands.get(childCommand).dispatch(sender, args.subList(1, args.size()));
      } else if (hasHandler) {
        new CommandProcessor(commando, root, sender, args);
        //root.process(sender, args.toArray(new String[0]));
      } else {
        handleMissingArgument(sender, args.get(0));
      }
    }
  }

  private void handleMissingArgument(CommandSender sender, String argument) {
    List<CarbonCommand> commands = new ArrayList<>();
    collectAllCommands(childCommands, commands, argument);
    // example
    // "/foo bar" command exist
    // /foo doesn't exist
    // types "/foo" -> missing command
    // types "/foo baz" -> missing command
    BiConsumer<CommandSender, List<CarbonCommand>> handler = commando.getMissingCommandHandler();
    if(handler != null) {
      handler.accept(sender, commands);
    }
  }

  private String getChildCommand(String arg) {
    arg = arg.toLowerCase(Locale.ENGLISH);
    if (childCommands.containsKey(arg)) {
      return arg;
    }
    return null;
  }

  Set<String> autoComplete(CommandSender sender, List<String> args) {
    if (args.isEmpty()) {
      return Collections.emptySet();
    }

    String childCommand = getChildCommand(args.get(0));
    if (childCommand == null) {
      if (root != null && root.mayHaveArguments()) {
        try {
          return root.autoComplete(sender, args.toArray(new String[0]));
        } catch (Exception e) {
          Bukkit.getLogger().log(Level.SEVERE, e.getMessage(), e);
          return Collections.emptySet();
        }
      }

      if(args.size() > 1) {
        return Collections.emptySet();
      }
    }

    Set<String> suggestions = new HashSet<>();

    if(childCommand == null || args.get(args.size() - 1).equalsIgnoreCase(childCommand)) {
      suggestions.addAll(childCommands
        .entrySet()
        .stream()
        .filter(e -> {
          if(e.getValue().root == null) {
            return true;
          }

          return e.getValue().root.checkPermissions(sender);
        })
        .filter(e -> e
          .getKey()
          .toLowerCase(Locale.ENGLISH)
          .startsWith(args.get(0).toLowerCase(Locale.ENGLISH))
        )
        .map(Map.Entry::getKey)
        .collect(Collectors.toList())
      );
    }

    if(childCommand != null) {
      suggestions.addAll(childCommands.get(childCommand).autoComplete(sender, args.subList(1, args.size())));
    }

    return suggestions;
  }

  private static void collectAllCommands(Map<String, CommandSet> commands, List<CarbonCommand> collector) {
    collectAllCommands(commands, collector, "");
  }

  private static void collectAllCommands(Map<String, CommandSet> commands, List<CarbonCommand> collector, String startsWith) {
    for (Map.Entry<String, CommandSet> commandSet : commands.entrySet()) {
      if (!startsWith.isEmpty() && !commandSet.getKey().startsWith(startsWith)) {
        continue;
      }

      if (commandSet.getValue().root != null) {
        collector.add(commandSet.getValue().root);
        if (commandSet.getValue().hasAnyChildCommands()) {
          collectAllCommands(commandSet.getValue().childCommands, collector);
        }
      }
    }
  }
}
