package se.hornta.commando;

import se.hornta.commando.completers.IArgumentHandler;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CarbonCommand {
  private final ArrayList<String> parts;
  private ICommandHandler handler;
  private List<String> permissions = Collections.emptyList();
  private Boolean preventConsoleCommandSender = false;
  private Boolean preventPlayerCommandSender = false;
  private final List<ICarbonArgument> arguments = new ArrayList<>();
  private String helpText;
  private final EnumMap<CarbonArgumentType, IArgumentHandler> typeCompleters = new EnumMap<>(CarbonArgumentType.class);

  private static final Pattern permissionArgumentPattern = Pattern.compile("\\[[0-9]+]", Pattern.CASE_INSENSITIVE);

  CarbonCommand() {
    this.parts = new ArrayList<>();
  }

  public int getNumRequiredArgs(CommandSender sender) {
    int num = 0;
    for (ICarbonArgument argument : arguments) {
      String permission = argument.getPermission();
      DefaultArgument defaultArgument = argument.getDefaultValue(sender.getClass());
      if (defaultArgument != null && (permission == null || sender.hasPermission(permission))) {
        num += 1;
      }
    }
    return num;
  }

  public List<String> getPermissions() {
    return permissions;
  }

  void setParts(String[] parts) {
    if (parts.length == 0) {
      throw new IllegalArgumentException("At least one sub command is required.");
    }
    this.parts.addAll(Arrays.asList(parts));

    StringBuilder stringBuilder = new StringBuilder();
    for(String part : parts) {
      stringBuilder.append(part);
      stringBuilder.append(" ");
    }

    helpText = stringBuilder.insert(0, "/").toString();
  }

  String getPart(Integer index) {
    if (parts.size() - 1 < index) {
      return null;
    }

    return parts.get(index);
  }

  public ICommandHandler getHandler() {
    return handler;
  }

  public List<ICarbonArgument> getArguments() {
    return arguments;
  }

  public Boolean isLastStep(Integer index) {
    return parts.size() - 1 == index;
  }

  public CarbonCommand withHandler(ICommandHandler handler) {
    this.handler = handler;

    return this;
  }

  public CarbonCommand withArgument(ICarbonArgument argument) {
    for(ICarbonArgument carbonArgument : arguments) {
      if(carbonArgument.getName().equals(argument.getName())) {
        throw new Error("Command already contains an argument with name " + argument.getName());
      }

      if (carbonArgument.isCatchRemaining()) {
        throw new Error("Command already contains an argument with catch remaining set to true");
      }
    }

    if(!arguments.isEmpty() && arguments.get(arguments.size() - 1).isOptional() && !argument.isOptional()) {
      throw new Error("A non optional argument shouldn't come after an optional argument");
    }

    arguments.add(argument);

    StringBuilder stringBuilder = new StringBuilder();
    for (String part : parts) {
      stringBuilder.append(part);
      stringBuilder.append(" ");
    }

    for (ICarbonArgument argument1 : arguments) {
      if(argument1.isOptional()) {
        stringBuilder.append("[");
      } else {
        stringBuilder.append("<");
      }
      stringBuilder.append(argument1.getName());
      if(argument1.isOptional()) {
        stringBuilder.append("]");
      } else {
        stringBuilder.append(">");
      }

      stringBuilder.append(" ");
    }

    helpText = stringBuilder.insert(0, "/").toString().trim();
    return this;
  }

  public CarbonCommand requiresPermission(String permission) {
    if(permissions.isEmpty()) {
      permissions = new ArrayList<>();
    }
    permissions.add(permission);
    return this;
  }

  public CarbonCommand preventConsoleCommandSender() {
    preventConsoleCommandSender = true;
    return this;
  }

  public CarbonCommand preventPlayerCommandSender() {
    preventPlayerCommandSender = true;
    return this;
  }

  public boolean checkPermissions(CommandSender sender) {
    return checkPermissions(sender, Collections.emptyList());
  }

  public boolean checkPermissions(CommandSender sender, List<String> args) {
    if(permissions.isEmpty()) {
      return true;
    }

    for (String permission : permissions) {
      Map<String, String> values = new HashMap<>();

      for (int i = 0; i < args.size(); i++) {
        values.put(String.valueOf(i), args.get(i));
      }

      String permissionNode = transformPattern(permission, permissionArgumentPattern, values);
      boolean hasPermission = sender.hasPermission(permissionNode);
      if (hasPermission) {
        return true;
      }
    }

    return false;
  }

  public boolean allowsSender(CommandSender sender) {
    if (preventConsoleCommandSender && sender instanceof ConsoleCommandSender) {
      return false;
    }

    return !preventPlayerCommandSender || !(sender instanceof Player);
  }

  Boolean hasHandler() {
    return handler != null;
  }

  public String getHelpText() {
    return helpText;
  }

  Set<String> autoComplete(CommandSender sender, String[] args) throws
    InstantiationException,
    IllegalAccessException,
    NoSuchMethodException,
    InvocationTargetException
  {
    boolean hasPermission = checkPermissions(sender, Arrays.asList(args));
    if(!hasPermission) {
      return Collections.emptySet();
    }

    int argumentIndex = args.length - 1;

    if(
      argumentIndex < 0 ||
      argumentIndex >= arguments.size()
    ) {
      return Collections.emptySet();
    }

    ICarbonArgument argument = arguments.get(argumentIndex);
    IArgumentHandler argumentHandler = argument.getHandler();

    if (argument.getPermission() != null && !sender.hasPermission(argument.getPermission())) {
      return Collections.emptySet();
    }

    if (argumentHandler == null && !argument.getType().isCompletable() || !argument.isTabCompletionActive()) {
      return Collections.emptySet();
    }

    Set<String> suggestions = new HashSet<>();
    Set<String> prevArgs = new HashSet<>();
    for(ICarbonArgument dependency : argument.getDependencies()) {
      String dependencyValue = args[arguments.indexOf(dependency)];
      if(dependency.getHandler() != null && !dependency.getHandler().test(
        dependency.getHandler().getItems(sender, dependencyValue, prevArgs.toArray(new String[0])),
        dependencyValue
      )) {
        return suggestions;
      }

      if(!arguments.contains(dependency)) {
        throw new Error("The specified dependency couldn't be found in command arguments");
      }

      prevArgs.add(dependencyValue);
    }

    if(argumentHandler == null) {
      if(!typeCompleters.containsKey(argument.getType())) {
        typeCompleters.put(argument.getType(), (IArgumentHandler) argument.getType().getCompleter().getConstructor().newInstance());
      }
      argumentHandler = typeCompleters.get(argument.getType());
    }

    suggestions.addAll(
      argumentHandler.getItems(
        sender,
        args[argumentIndex],
        prevArgs.toArray(new String[0]))
    );

    return suggestions;
  }

  public Boolean mayHaveArguments() {
    return arguments.size() > 0;
  }

  static String transformPattern(String input, Pattern pattern, Map<String, String> values) {
    return StringReplacer.replace(input, pattern, (Matcher m) -> {
      String placeholder = m.group().substring(1, m.group().length() - 1).toLowerCase(Locale.ENGLISH);
      if (values.containsKey(placeholder)) {
        return values.get(placeholder);
      } else {
        return m.group();
      }
    });
  }
}
