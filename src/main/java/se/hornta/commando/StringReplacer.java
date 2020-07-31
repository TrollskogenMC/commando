package se.hornta.commando;

import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class StringReplacer {
  private StringReplacer() { }

  static String replace(String input, Pattern regex, Function<Matcher, String> callback) {
    StringBuffer resultString = new StringBuffer();
    Matcher regexMatcher = regex.matcher(input);
    while (regexMatcher.find()) {
      regexMatcher.appendReplacement(resultString, Matcher.quoteReplacement(callback.apply(regexMatcher)));
    }
    regexMatcher.appendTail(resultString);

    return resultString.toString();
  }
}
