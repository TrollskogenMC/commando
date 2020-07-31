package se.hornta.commando.completers;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;

import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

public class WorldCompleter implements IArgumentHandler {
  @Override
  public Set<String> getItems(CommandSender sender, String argument, String[] prevArgs) {
    return Bukkit
      .getWorlds()
      .stream()
      .map(World::getName)
      .filter((String name) -> name.toLowerCase(Locale.ENGLISH).startsWith(argument.toLowerCase(Locale.ENGLISH)))
      .collect(Collectors.toCollection(LinkedHashSet::new));
  }

  @Override
  public boolean test(Set<String> items, String argument) {
    return items.contains(argument);
  }
}
