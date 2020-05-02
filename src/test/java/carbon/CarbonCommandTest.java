package carbon;

import com.github.hornta.commando.CarbonArgument;
import com.github.hornta.commando.Commando;
import com.github.hornta.commando.ICarbonArgument;
import com.github.hornta.commando.ICommandHandler;
import com.github.hornta.commando.completers.IArgumentHandler;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Set;

public class CarbonCommandTest {
  private Commando commando;
  private CommandSender sender;

  @Before
  public void beforeEach() {
    commando = new Commando();
    sender = Mockito.mock(CommandSender.class);
  }

  @Test
  public void testAutocompleteDependency() {
    ICarbonArgument arg = new CarbonArgument.Builder("foo").create();
    IArgumentHandler handler = Mockito.mock(IArgumentHandler.class);
    ICarbonArgument arg2 = new CarbonArgument.Builder("bar").dependsOn(arg).setHandler(handler).create();

    commando
      .addCommand("foobar")
      .withArgument(arg)
      .withArgument(arg2);

    Command command = Mockito.mock(Command.class);
    Mockito.when(command.getName()).thenReturn("foobar");

    commando.handleAutoComplete(sender, command, new String[] { "pig", "" });
    Mockito.verify(handler).getItems(sender, "", new String[] { "pig" });
  }

  @Test
  public void testCatchRemaining() {
    ICommandHandler handler = Mockito.mock(ICommandHandler.class);

    commando
      .addCommand("foobar")
      .withArgument(
        new CarbonArgument.Builder("foo").create()
      )
      .withArgument(
        new CarbonArgument.Builder("bar").catchRemaining().create()
      )
      .withHandler(handler);

    Command command = Mockito.mock(Command.class);
    Mockito.when(command.getName()).thenReturn("foobar");

    commando.handleCommand(sender, command, new String[] { "!", "c", "a", "r", "b", "o", "n" });
    Mockito.verify(handler).handle(sender, new String[] { "!", "c a r b o n" }, 2);
  }

  @Test(expected = Error.class)
  public void testMultipleCatchRemaining() {
    ICommandHandler handler = Mockito.mock(ICommandHandler.class);

    ICarbonArgument catchRemainingArg = new CarbonArgument.Builder("foo").catchRemaining().create();

    commando
      .addCommand("foobar")
      .withArgument(catchRemainingArg)
      .withArgument(catchRemainingArg)
      .withHandler(handler);
  }

  @Test
  public void testDefaultValue() {
    ICommandHandler handler = Mockito.mock(ICommandHandler.class);
    ICarbonArgument defaultArg = new CarbonArgument.Builder("arg").setDefaultValue(sender.getClass(), "default").create();

    commando
      .addCommand("foobar")
      .withArgument(defaultArg)
      .withHandler(handler);

    Command command = Mockito.mock(Command.class);
    Mockito.when(command.getName()).thenReturn("foobar");

    commando.handleCommand(sender, command, new String[0]);
    Mockito.verify(handler).handle(sender, new String[] { "default" }, 0);
  }

  @Test
  public void testDefaultValueFunc() {
    ICommandHandler handler = Mockito.mock(ICommandHandler.class);
    ICarbonArgument defaultArg = new CarbonArgument.Builder("arg")
      .setDefaultValue(sender.getClass(), (CommandSender sender, String[] prevArgs) -> {
        return "default";
      })
      .create();

    commando
      .addCommand("foobar")
      .withArgument(defaultArg)
      .withHandler(handler);

    Command command = Mockito.mock(Command.class);
    Mockito.when(command.getName()).thenReturn("foobar");

    commando.handleCommand(sender, command, new String[0]);
    Mockito.verify(handler).handle(sender, new String[] { "default" }, 0);
  }

  @Test
  public void test() {
    ICarbonArgument homeArgument = new CarbonArgument.Builder("home")
      .setHandler(new IArgumentHandler() {
        @Override
        public Set<String> getItems(CommandSender sender, String argument, String[] prevArgs) {
          return null;
        }
      })
      .setDefaultValue(sender.getClass(), (CommandSender sender, String[] prevArgs) -> "home")
      .create();

    commando
      .addCommand("home")
      .withArgument(homeArgument)
      .withHandler(new ICommandHandler() {
        @Override
        public void handle(CommandSender sender, String[] args, int typedArgs) {

        }
      })
      .requiresPermission("ts.home")
      .preventConsoleCommandSender();


    Command command = Mockito.mock(Command.class);
    Mockito.when(command.getName()).thenReturn("home");

    commando.handleAutoComplete(sender, command, new String[] { "home" });
  }
}
