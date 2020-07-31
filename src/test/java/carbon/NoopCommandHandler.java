package carbon;

import se.hornta.commando.ICommandHandler;
import org.bukkit.command.CommandSender;

public class NoopCommandHandler implements ICommandHandler {
  @Override
  public void handle(CommandSender sender, String[] args, int typedArgs) {

  }
}
