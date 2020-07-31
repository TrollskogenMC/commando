package se.hornta.commando;

import org.bukkit.command.CommandSender;

@FunctionalInterface
public interface ICommandHandler {
  void handle(CommandSender sender, String[] args, int typedArgs);
}
