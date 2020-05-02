package carbon;

import com.github.hornta.commando.DateUtil;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.Calendar;
import java.util.GregorianCalendar;

@RunWith(PowerMockRunner.class)
@PrepareForTest(DateUtil.class)
public class DateUtilTest {

  @Test
  public void testFormat() {
    Calendar from = new GregorianCalendar();
    Calendar to = new GregorianCalendar();
    to.add(Calendar.HOUR, 1);
    String result = DateUtil.formatDateDiff(from, to);
    Assert.assertEquals("1 <hour>", result);
  }

  @Test
  public void testParser() throws Exception {
    long result;
    GregorianCalendar from = new GregorianCalendar(2019, Calendar.AUGUST, 8);
    long fromSeconds = from.getTimeInMillis() / 1000;

    result = DateUtil.parseDuration(from, "10s", true);
    Assert.assertEquals(fromSeconds + 10, result);

    result = DateUtil.parseDuration(from, "10m", true);
    Assert.assertEquals(fromSeconds + 600, result);

    result = DateUtil.parseDuration(from, "1h", true);
    Assert.assertEquals(fromSeconds + 3600, result);

    result = DateUtil.parseDuration(from, "2h", true);
    Assert.assertEquals(fromSeconds + 3600 * 2, result);

    result = DateUtil.parseDuration(from, "7h", true);
    Assert.assertEquals(fromSeconds + 3600 * 7, result);

    result = DateUtil.parseDuration(from, "24h", true);
    Assert.assertEquals(fromSeconds + 3600 * 24, result);

    result = DateUtil.parseDuration(from, "1d", true);
    Assert.assertEquals(fromSeconds + 3600 * 24, result);

    result = DateUtil.parseDuration(from, "5d", true);
    Assert.assertEquals(fromSeconds + 3600 * 24 * 5, result);

    result = DateUtil.parseDuration(from, "30d", true);
    Assert.assertEquals(fromSeconds + 3600 * 24 * 30, result);

    result = DateUtil.parseDuration(from, "31d", true);
    Assert.assertEquals(fromSeconds + 3600 * 24 * 31, result);

    result = DateUtil.parseDuration(from, "32d", true);
    Assert.assertEquals(fromSeconds + 3600 * 24 * 32, result);

    result = DateUtil.parseDuration(from, "33d", true);
    Assert.assertEquals(fromSeconds + 3600 * 24 * 33, result);

    result = DateUtil.parseDuration(from, "40d", true);
    Assert.assertEquals(fromSeconds + 3600 * 24 * 40, result);

    result = DateUtil.parseDuration(from, "45d", true);
    Assert.assertEquals(fromSeconds + 3600 * 24 * 45, result);

    result = DateUtil.parseDuration(from, "50d", true);
    Assert.assertEquals(fromSeconds + 3600 * 24 * 50, result);

    result = DateUtil.parseDuration(from, "100d", true);
    Assert.assertEquals(fromSeconds + 3600 * 24 * 100 + 3600, result); // account for DST

    result = DateUtil.parseDuration(from, "1h,30m", true);
    Assert.assertEquals(fromSeconds + 3600 + 60 * 30, result);
  }
}
