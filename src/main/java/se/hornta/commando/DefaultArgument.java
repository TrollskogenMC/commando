package se.hornta.commando;

import org.bukkit.command.CommandSender;

import java.util.function.BiFunction;

class DefaultArgument {
  private BiFunction<CommandSender, String[], Object> func;
  private Object value;

  DefaultArgument(Object value) {
    if (value instanceof BiFunction) {
      func = (BiFunction<CommandSender, String[], Object>) value;
    } else {
      this.value = value;
    }
  }

  public <T> T getValue(CommandSender commandSender, String[] dependencies) {
    if(func != null) {
      return (T)func.apply(commandSender, dependencies);
    }

    return (T)value;
  }
}
