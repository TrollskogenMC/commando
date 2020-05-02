package com.github.hornta.commando.completers;

import org.bukkit.command.CommandSender;
import org.bukkit.potion.PotionEffectType;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

public class PotionEffectCompleter implements IArgumentHandler {
  private Set<String> items;

  public PotionEffectCompleter() {
    items = Arrays.stream(PotionEffectType.class.getDeclaredFields())
      .map(Field::getName)
      .filter((String name) -> {
        for(int i = 0; i < name.length(); ++i) {
          if(Character.isLowerCase(name.charAt(i))) {
            return false;
          }
        }
        return true;
      })
      .collect(Collectors.toCollection(LinkedHashSet::new));
  }

  @Override
  public Set<String> getItems(CommandSender sender, String argument, String[] prevArgs) {
    return items
      .stream()
      .filter((String name) -> name.toLowerCase(Locale.ENGLISH).startsWith(argument.toLowerCase(Locale.ENGLISH)))
      .collect(Collectors.toSet());
  }

  @Override
  public boolean test(Set<String> items, String argument) {
    return items.contains(argument);
  }
}
