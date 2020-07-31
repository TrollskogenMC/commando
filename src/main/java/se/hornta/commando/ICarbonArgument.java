package se.hornta.commando;

import se.hornta.commando.completers.IArgumentHandler;

import java.util.Set;
import java.util.regex.Pattern;

public interface ICarbonArgument {
  double getMax();
  double getMin();
  int getMinLength();
  int getMaxLength();
  boolean isOptional();
  boolean isTabCompletionActive();
  IArgumentHandler getHandler();
  CarbonArgumentType getType();
  String getName();
  DefaultArgument getDefaultValue(Class<?> senderType);
  Pattern getPattern();
  Set<ICarbonArgument> getDependencies();
  boolean isCatchRemaining();
  String getPermission();
}
