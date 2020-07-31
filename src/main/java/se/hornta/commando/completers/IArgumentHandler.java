package se.hornta.commando.completers;

import se.hornta.commando.ValidationResult;
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.Set;

public interface IArgumentHandler {
  default Set<String> getItems(CommandSender sender, String argument, String[] prevArgs) { return Collections.emptySet(); }
  default void whenInvalid(ValidationResult validationResult) {}
  default boolean test(Set<String> items, String argument) {
    return true;
  }
}
