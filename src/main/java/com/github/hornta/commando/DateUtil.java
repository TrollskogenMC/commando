package com.github.hornta.commando;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class DateUtil {
  private static final int maxYears = 1000;

  private DateUtil() {}

  private static final Pattern TIME_PATTERN = Pattern.compile(
    Stream
      .of("y", "mo", "w", "d", "h", "m", "s")
      .map(i -> "(?:([0-9]+)" + i + "[a-z]*[,]?)?")
      .collect(Collectors.joining()), Pattern.CASE_INSENSITIVE);
  private static final int MAX_YEARS = 100000;

  public static long parseDuration(String time, boolean future) {
    return parseDuration(new GregorianCalendar(), time, future);
  }

  public static long parseDuration(Calendar from, String time, boolean future) throws IllegalArgumentException {
    Matcher matcher = TIME_PATTERN.matcher(time);
    int years = 0;
    int months = 0;
    int weeks = 0;
    int days = 0;
    int hours = 0;
    int minutes = 0;
    int seconds = 0;

    boolean found = false;
    while (matcher.find()) {
      if (matcher.group() == null || matcher.group().isEmpty()) {
        continue;
      }
      for (int i = 0; i < matcher.groupCount(); i++) {
        if (matcher.group(i) != null && !matcher.group(i).isEmpty()) {
          found = true;
          break;
        }
      }
      if (found) {
        if (matcher.group(1) != null && !matcher.group(1).isEmpty()) {
          years = Integer.parseInt(matcher.group(1));
        }
        if (matcher.group(2) != null && !matcher.group(2).isEmpty()) {
          months = Integer.parseInt(matcher.group(2));
        }
        if (matcher.group(3) != null && !matcher.group(3).isEmpty()) {
          weeks = Integer.parseInt(matcher.group(3));
        }
        if (matcher.group(4) != null && !matcher.group(4).isEmpty()) {
          days = Integer.parseInt(matcher.group(4));
        }
        if (matcher.group(5) != null && !matcher.group(5).isEmpty()) {
          hours = Integer.parseInt(matcher.group(5));
        }
        if (matcher.group(6) != null && !matcher.group(6).isEmpty()) {
          minutes = Integer.parseInt(matcher.group(6));
        }
        if (matcher.group(7) != null && !matcher.group(7).isEmpty()) {
          seconds = Integer.parseInt(matcher.group(7));
        }
        break;
      }
    }

    if (!found) {
      throw new IllegalArgumentException();
    }

    Calendar c = (Calendar) from.clone();
    if (years > 0) {
      if (years > MAX_YEARS) {
        years = MAX_YEARS;
      }
      c.add(Calendar.YEAR, years * (future ? 1 : -1));
    }
    if (months > 0) {
      c.add(Calendar.MONTH, months * (future ? 1 : -1));
    }
    if (weeks > 0) {
      c.add(Calendar.WEEK_OF_YEAR, weeks * (future ? 1 : -1));
    }
    if (days > 0) {
      c.add(Calendar.DAY_OF_MONTH, days * (future ? 1 : -1));
    }
    if (hours > 0) {
      c.add(Calendar.HOUR_OF_DAY, hours * (future ? 1 : -1));
    }
    if (minutes > 0) {
      c.add(Calendar.MINUTE, minutes * (future ? 1 : -1));
    }
    if (seconds > 0) {
      c.add(Calendar.SECOND, seconds * (future ? 1 : -1));
    }

    Calendar max = (Calendar) from.clone();
    max.add(Calendar.YEAR, maxYears);

    if (c.after(max)) {
      c = max;
    }
    return c.getTimeInMillis() / 1000;
  }

  static int dateDiff(int type, Calendar fromDate, Calendar toDate, boolean future) {
    int year = Calendar.YEAR;

    int fromYear = fromDate.get(year);
    int toYear = toDate.get(year);
    if (Math.abs(fromYear - toYear) > maxYears) {
      toDate.set(year, fromYear + (future ? maxYears : -maxYears));
    }

    int diff = 0;
    long savedDate = fromDate.getTimeInMillis();
    while ((future && !fromDate.after(toDate)) || (!future && !fromDate.before(toDate))) {
      savedDate = fromDate.getTimeInMillis();
      fromDate.add(type, future ? 1 : -1);
      diff++;
    }
    diff--;
    fromDate.setTimeInMillis(savedDate);
    return diff;
  }

  public static String formatDateDiff(long ts) {
    Calendar c = new GregorianCalendar();
    c.setTimeInMillis(ts);
    Calendar now = new GregorianCalendar();
    return formatDateDiff(now, c);
  }

  public static String formatDateDiff(Calendar fromDate, Calendar toDate) {
    boolean future = false;
    if (toDate.equals(fromDate)) {
      return "<now>";
    }
    if (toDate.after(fromDate)) {
      future = true;
    }
    StringBuilder sb = new StringBuilder();

    int[] types = new int[] {
      Calendar.YEAR,
      Calendar.MONTH,
      Calendar.DAY_OF_MONTH,
      Calendar.HOUR_OF_DAY,
      Calendar.MINUTE,
      Calendar.SECOND
    };

    String[] names = new String[] {
      "<year>",
      "<years>",
      "<month>",
      "<months>",
      "<day>",
      "<days>",
      "<hour>",
      "<hours>",
      "<minute>",
      "<minutes>",
      "<second>",
      "<seconds>"
    };

    int accuracy = 0;
    for (int i = 0; i < types.length; i++) {
      if (accuracy == 2) {
        break;
      }
      int diff = dateDiff(types[i], fromDate, toDate, future);
      if (diff > 0) {
        accuracy++;
        sb.append(" ").append(diff).append(" ").append(names[i * 2 + (diff > 1 ? 1 : 0)]);
      }
    }
    if (sb.length() == 0) {
      return "<now>";
    }
    return sb.toString().trim();
  }
}
