package se.hornta.commando.completers;

import org.bukkit.WorldType;
import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

public class WorldTypeCompleter implements IArgumentHandler {
  @Override
  public Set<String> getItems(CommandSender sender, String argument, String[] prevArgs) {
    return Arrays.stream(WorldType.values())
      .map(Enum::name)
      .filter((String name) -> name.toLowerCase(Locale.ENGLISH).startsWith(argument.toLowerCase(Locale.ENGLISH)))
      .collect(Collectors.toCollection(LinkedHashSet::new));
  }

  @Override
  public boolean test(Set<String> items, String argument) {
    return items.contains(argument);
  }
}
