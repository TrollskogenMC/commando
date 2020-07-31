package se.hornta.commando;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class Commando {
  private final CommandSet rootCommandSet;
  private final List<CarbonCommand> allCommands = new ArrayList<>();
  private BiConsumer<CommandSender, CarbonCommand> noPermissionHandler;
  private BiConsumer<CommandSender, CarbonCommand> missingArgumentHandler;
  private BiConsumer<CommandSender, List<CarbonCommand>> missingCommandHandler;
  private Consumer<ValidationResult> validationHandler;

  public Commando() {
    rootCommandSet = new CommandSet(this);
  }

  public CarbonCommand addCommand(String parts) {
    CarbonCommand command = new CarbonCommand();
    command.setParts(parts.split(" "));

    allCommands.add(command);
    CommandSet commandSet = getCommandSet(command, rootCommandSet);
    commandSet.setCommand(command);

    return command;
  }

  public void setNoPermissionHandler(BiConsumer<CommandSender, CarbonCommand> handler) {
    noPermissionHandler = handler;
  }

  public void setMissingArgumentHandler(BiConsumer<CommandSender, CarbonCommand> handler) {
    missingArgumentHandler = handler;
  }

  public void setMissingCommandHandler(BiConsumer<CommandSender, List<CarbonCommand>> handler) {
    missingCommandHandler = handler;
  }

  public void handleValidation(Consumer<ValidationResult> validationHandler) {
    if(this.validationHandler != null) {
      throw new Error("Validation handler already set");
    }

    this.validationHandler = validationHandler;
  }

  public Consumer<ValidationResult> getValidationHandler() {
    return validationHandler;
  }

  public BiConsumer<CommandSender, CarbonCommand> getNoPermissionHandler() {
    return noPermissionHandler;
  }

  public BiConsumer<CommandSender, CarbonCommand> getMissingArgumentHandler() {
    return missingArgumentHandler;
  }

  public BiConsumer<CommandSender, List<CarbonCommand>> getMissingCommandHandler() {
    return missingCommandHandler;
  }

  private static CommandSet getCommandSet(CarbonCommand command, CommandSet set) {
    return getCommandSet(command, set, 0);
  }

  private static CommandSet getCommandSet(CarbonCommand command, CommandSet set, Integer step) {
    String part = command.getPart(step);

    if (part == null) {
      return set;
    }

    if (set.hasChildCommand(part)) {
      if (command.isLastStep(step)) {
        set.getCommandSet(part);
        return set.getCommandSet(part);
      }
      return getCommandSet(command, set.getCommandSet(part), step + 1);
    } else {
      return getCommandSet(command, set.createChildCommand(part), step + 1);
    }
  }

  private void dispatch(CommandSender sender, List<String> args) {
    rootCommandSet.dispatch(sender, args);
  }

  public Boolean handleCommand(CommandSender sender, Command command, String[] args) {
    ArrayList<String> arguments = new ArrayList<>(Arrays.asList(args));
    arguments.add(0, command.getName());
    dispatch(sender, arguments);
    return true;
  }

  public List<String> handleAutoComplete(CommandSender sender, Command command, String[] args) {
    List<String> arguments = new ArrayList<>(Arrays.asList(args));
    arguments.add(0, command.getName());

    return rootCommandSet.autoComplete(sender, arguments).stream().sorted(String::compareTo).collect(Collectors.toList());
  }

  public List<String> getHelpTexts() {
    return getHelpTexts(null);
  }

  public List<String> getHelpTexts(Player player) {
    return allCommands.stream()
      .filter((CarbonCommand command) -> {
        if(player == null) {
          return true;
        }

        for(String permission : command.getPermissions()) {
          if(!player.hasPermission(permission)) {
            return false;
          }
        }
        return true;
      })
      .map(CarbonCommand::getHelpText)
      .collect(Collectors.toList());
  }
}
