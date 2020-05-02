package com.github.hornta.commando.completers;

import org.bukkit.command.CommandSender;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

public class BooleanEffectCompleter implements IArgumentHandler {
  private static final Set<String> items = new HashSet<>();
  static {
    items.add("true");
    items.add("false");
  }

  @Override
  public Set<String> getItems(CommandSender sender, String argument, String[] prevArgs) {
    return items
      .stream()
      .filter((String name) -> name.toLowerCase(Locale.ENGLISH).startsWith(argument.toLowerCase(Locale.ENGLISH)))
      .collect(Collectors.toCollection(LinkedHashSet::new));
  }

  @Override
  public boolean test(Set<String> items, String argument) {
    return argument.contains(argument);
  }
}
