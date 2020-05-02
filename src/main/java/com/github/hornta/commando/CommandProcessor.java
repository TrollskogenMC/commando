package com.github.hornta.commando;

import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

public class CommandProcessor {
  private final Commando commando;
  private final CarbonCommand command;
  private final CommandSender sender;
  private final List<String> inputArgs;

  CommandProcessor(Commando commando, CarbonCommand command, CommandSender sender, List<String> inputArgs) {
    this.commando = commando;
    this.command = command;
    this.sender = sender;
    this.inputArgs = inputArgs;

    this.process();
  }

  private void process() {
    if (!command.allowsSender(sender)) {
      return;
    }

    boolean hasPermission = command.checkPermissions(sender, inputArgs);
    if (!hasPermission) {
      BiConsumer<CommandSender, CarbonCommand> noPermissionHandler = commando.getNoPermissionHandler();
      if(noPermissionHandler != null) {
        noPermissionHandler.accept(sender, command);
      }
      return;
    }

    int argIndex = 0;
    for (ICarbonArgument argument : command.getArguments()) {
      if (argument.isCatchRemaining() && !inputArgs.isEmpty()) {
        String arg = String.join(" ", inputArgs.subList(argIndex, inputArgs.size()));
        if(arg.isEmpty()) {
          break;
        }
        inputArgs.subList(argIndex, inputArgs.size()).clear();
        inputArgs.add(arg);
      }
      argIndex += 1;
    }

    int typedArgs = 0;

    List<String> commandArgs = new ArrayList<>();
    argIndex = 0;
    for (ICarbonArgument argument : command.getArguments()) {

      if(argument.getPermission() != null && !sender.hasPermission(argument.getPermission())) {
        break;
      }

      if(argIndex < inputArgs.size()) {
        typedArgs += 1;
      }

      DefaultArgument defaultArgument = argument.getDefaultValue(sender.getClass());
      String[] prevArgs = getArgumentDependencies(argument);

      if (argIndex >= inputArgs.size() && defaultArgument != null) {
        Object defaultValue = argument.getDefaultValue(sender.getClass()).getValue(sender, prevArgs);

        String stringValue;
        if(defaultValue == null) {
          stringValue = "";
        } else if(defaultValue instanceof Integer) {
          stringValue = String.valueOf((int)defaultValue);
        } else if(defaultValue instanceof Double) {
          stringValue = String.valueOf((double)defaultValue);
        } else if(defaultValue instanceof Boolean) {
          stringValue = String.valueOf((boolean)defaultValue);
        } else {
          stringValue = (String) defaultValue;
        }

        inputArgs.add(argIndex, stringValue);
      }

      if (argIndex > inputArgs.size() - 1) {
        BiConsumer<CommandSender, CarbonCommand> handler = commando.getMissingArgumentHandler();
        if(handler != null) {
          handler.accept(sender, command);
        }
        return;
      }

      String input = inputArgs.get(argIndex);
      ValidationStatus status = getValidationStatus(argument, input, prevArgs);
      if (status != null) {
        ValidationResult validationResult = new ValidationResult(status, command, argument, sender, input, prevArgs);

        if (argument.getHandler() != null) {
          argument.getHandler().whenInvalid(validationResult);
        }

        if (commando.getValidationHandler() != null) {
          commando.getValidationHandler().accept(validationResult);
        }
        return;
      }

      commandArgs.add(inputArgs.get(argIndex));
      argIndex += 1;
    }

    command.getHandler().handle(sender, commandArgs.toArray(new String[0]), typedArgs);
  }

  private ValidationStatus getValidationStatus(ICarbonArgument argument, String input, String[] deps) {
    ValidationStatus status = null;

    if (argument.getHandler() != null) {
      boolean handlerResult = argument.getHandler().test(
        argument.getHandler().getItems(sender, input, deps),
        input
      );

      if (!handlerResult) {
        status = ValidationStatus.ERR_OTHER;
      }
    } else {
      switch (argument.getType()) {

        case DURATION:
          try {
            DateUtil.parseDuration(input, true);
          } catch (IllegalArgumentException e) {
            status = ValidationStatus.ERR_INCORRECT_TYPE;
          }
          break;

        case STRING:
          if (input.length() < argument.getMinLength()) {
            status = ValidationStatus.ERR_MIN_LENGTH;
          } else if (input.length() > argument.getMaxLength()) {
            status = ValidationStatus.ERR_MAX_LENGTH;
          }
          break;

        case NUMBER:
          try {
            double doubleValue = Double.parseDouble(input);
            if (doubleValue < argument.getMin()) {
              status = ValidationStatus.ERR_MIN_LIMIT;
            } else if (doubleValue > argument.getMax()) {
              status = ValidationStatus.ERR_MAX_LIMIT;
            }
          } catch (NumberFormatException e) {
            status = ValidationStatus.ERR_INCORRECT_TYPE;
          }
          break;

        case INTEGER:
          try {
            int integerValue = Integer.parseInt(input);
            if (integerValue < argument.getMin()) {
              status = ValidationStatus.ERR_MIN_LIMIT;
            } else if (integerValue > argument.getMax()) {
              status = ValidationStatus.ERR_MAX_LIMIT;
            }
          } catch (NumberFormatException e) {
            status = ValidationStatus.ERR_INCORRECT_TYPE;
          }
          break;
      }
    }

    return status;
  }

  private String[] getArgumentDependencies(ICarbonArgument argument) {
    String[] deps = new String[argument.getDependencies().size()];

    int i = 0;
    for (ICarbonArgument dependency : argument.getDependencies()) {
      if (!command.getArguments().contains(dependency)) {
        throw new Error("The specified dependency couldn't be found in command arguments");
      }

      deps[i] = inputArgs.get(command.getArguments().indexOf(dependency));
      i += 1;
    }

    return deps;
  }
}
