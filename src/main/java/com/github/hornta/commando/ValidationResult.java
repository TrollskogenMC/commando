package com.github.hornta.commando;

import org.bukkit.command.CommandSender;

public class ValidationResult {
  private final ValidationStatus status;
  private final CarbonCommand command;
  private final ICarbonArgument argument;
  private final CommandSender commandSender;
  private final String value;
  private final String[] prevArgs;

  ValidationResult(ValidationStatus status, CarbonCommand command, ICarbonArgument argument, CommandSender commandSender, String value, String[] prevArgs) {
    this.status = status;
    this.command = command;
    this.argument = argument;
    this.commandSender = commandSender;
    this.value = value;
    this.prevArgs = prevArgs;
  }

  public String[] getPrevArgs() {
    return prevArgs;
  }

  public CommandSender getCommandSender() {
    return commandSender;
  }

  public ValidationStatus getStatus() {
    return status;
  }

  public ICarbonArgument getArgument() {
    return argument;
  }

  public CarbonCommand getCommand() {
    return command;
  }

  public String getValue() {
    return value;
  }
}
