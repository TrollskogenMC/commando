package carbon;

import se.hornta.commando.completers.IArgumentHandler;
import org.bukkit.command.CommandSender;

import java.util.LinkedHashSet;
import java.util.Set;

public class FooBarHandler implements IArgumentHandler {
  @Override
  public Set<String> getItems(CommandSender sender, String argument, String[] prevArgs) {
    Set<String> items = new LinkedHashSet<>();
    items.add("foo");
    items.add("bar");
    return items;
  }
}
