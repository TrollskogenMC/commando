package carbon;

import com.github.hornta.commando.*;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.internal.verification.VerificationModeFactory;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.BiConsumer;

@RunWith(PowerMockRunner.class)
@PrepareForTest(Bukkit.class)
public class CarbonTest {
  private Commando commando;
  private CommandSender commandSender;
  private static final String[] NO_ARGS = new String[0];

  @Before
  public void beforeEach() {
    commando = new Commando();
    commandSender = Mockito.mock(CommandSender.class);
  }

  @Test
  public void testMissingArgumentWithCatchRemainingZeroArgs() {
    ICarbonArgument playerArg = new CarbonArgument.Builder("player").create();
    ICarbonArgument reasonArg = new CarbonArgument.Builder("reason").setType(CarbonArgumentType.STRING).catchRemaining().create();
    ICommandHandler commandHandler = Mockito.mock(ICommandHandler.class);

    CarbonCommand carbonCommand = commando
      .addCommand("ban")
      .withHandler(commandHandler)
      .withArgument(playerArg)
      .withArgument(reasonArg);

    BiConsumer<CommandSender, CarbonCommand> handler = Mockito.spy(
      new BiConsumer<CommandSender, CarbonCommand>() {
        @Override
        public void accept(CommandSender commandSender, CarbonCommand carbonCommand) {

        }
      }
    );
    commando.setMissingArgumentHandler(handler);

    Command command = Mockito.mock(Command.class);
    Mockito.when(command.getName()).thenReturn("ban");

    commando.handleCommand(commandSender, command, new String[0]);
    Mockito.verify(handler, VerificationModeFactory.times(1)).accept(commandSender, carbonCommand);
  }

  @Test
  public void testMissingArgumentWithCatchRemainingOneArg() {
    ICarbonArgument playerArg = new CarbonArgument.Builder("player").create();
    ICarbonArgument reasonArg = new CarbonArgument.Builder("reason").setType(CarbonArgumentType.STRING).catchRemaining().create();
    ICommandHandler commandHandler = Mockito.mock(ICommandHandler.class);

    CarbonCommand carbonCommand = commando
      .addCommand("ban")
      .withHandler(commandHandler)
      .withArgument(playerArg)
      .withArgument(reasonArg);

    BiConsumer<CommandSender, CarbonCommand> handler = Mockito.spy(
      new BiConsumer<CommandSender, CarbonCommand>() {
        @Override
        public void accept(CommandSender commandSender, CarbonCommand carbonCommand) {

        }
      }
    );
    commando.setMissingArgumentHandler(handler);

    Command command = Mockito.mock(Command.class);
    Mockito.when(command.getName()).thenReturn("ban");

    commando.handleCommand(commandSender, command, new String[]{ "hornta" });
    Mockito.verify(handler, VerificationModeFactory.times(1)).accept(commandSender, carbonCommand);
  }

  @Test
  public void shouldAutoCompleteWithHandler() {
    String commandName = "test";

    commando
      .addCommand(commandName)
      .withArgument(
        new CarbonArgument.Builder("foo")
          .setHandler(new FooBarHandler())
          .create()
      );

    Command command = Mockito.mock(Command.class);
    Mockito.when(command.getName()).thenReturn(commandName);

    List<String> expected = Arrays.asList("bar", "foo");
    List<String> result = commando.handleAutoComplete(commandSender, command, new String[]{ "" });

    Assert.assertEquals(expected, result);
  }

  @Test
  public void shouldIgnoreArgumentsWithoutPermission() {
    String commandName = "test";

    ICarbonArgument arg = new CarbonArgument.Builder("arg").requiresPermission("some.perm").create();
    ICommandHandler handler = Mockito.mock(ICommandHandler.class);
    Command command = Mockito.mock(Command.class);
    Mockito.when(command.getName()).thenReturn(commandName);

    commando
      .addCommand(commandName)
      .withArgument(arg)
      .withHandler(handler);

    commando.handleCommand(commandSender, command, new String[0]);

    Mockito.verify(handler, VerificationModeFactory.times(1)).handle(commandSender, new String[0], 0);
  }

  @Test
  public void shouldAutoCompleteCommand() {
    String commandName = "test";
    commando.addCommand(commandName);

    Command command = Mockito.mock(Command.class);
    Mockito.when(command.getName()).thenReturn("te");

    List<String> expected = Collections.singletonList(commandName);
    List<String> result = commando.handleAutoComplete(commandSender, command, NO_ARGS);

    Assert.assertEquals(expected, result);

    Mockito.when(command.getName()).thenReturn(commandName);
    result = commando.handleAutoComplete(commandSender, command, NO_ARGS);

    Assert.assertEquals(expected, result);
  }

  @Test
  public void shouldNotAutoCompletePermission() {
    commando
      .addCommand("test")
      .requiresPermission("some.permission");

    Command command = Mockito.mock(Command.class);
    Mockito.when(command.getName()).thenReturn("te");

    List<String> expected = Collections.emptyList();
    List<String> result = commando.handleAutoComplete(commandSender, command, NO_ARGS);

    Assert.assertEquals(expected, result);
  }

  @Test
  public void shouldAutoCompletePermission() {
    String permission = "some.permission";
    String commandName = "test";

    commando
      .addCommand(commandName)
      .requiresPermission(permission);

    Mockito.when(commandSender.hasPermission(permission)).thenReturn(true);

    Command command = Mockito.mock(Command.class);
    Mockito.when(command.getName()).thenReturn("te");

    List<String> expected = Collections.singletonList(commandName);
    List<String> result = commando.handleAutoComplete(commandSender, command, NO_ARGS);

    Assert.assertEquals(expected, result);
  }

  @Test
  public void shouldCallMissingCommandHandler() {
    BiConsumer<CommandSender, List<CarbonCommand>> handler = Mockito.spy(
      new BiConsumer<CommandSender, List<CarbonCommand>>() {
        @Override
        public void accept(CommandSender commandSender, List<CarbonCommand> carbonCommands) {

        }
      }
    );

    commando.setMissingCommandHandler(handler);
    Command command = Mockito.mock(Command.class);
    Mockito.when(command.getName()).thenReturn("");
    commando.handleCommand(commandSender, command, new String[] {});

    Mockito.verify(handler, VerificationModeFactory.times(1)).accept(commandSender, Collections.emptyList());
  }

  @Test
  public void permissionHandler() {
    String commandName = "test";
    String permission = "a.permission";

    Mockito.when(commandSender.hasPermission(permission)).thenReturn(false);

    CarbonCommand command = commando
      .addCommand(commandName)
      .withHandler(new NoopCommandHandler())
      .requiresPermission(permission);

    BiConsumer<CommandSender, CarbonCommand> spyHandler = Mockito.spy(new BiConsumer<CommandSender, CarbonCommand>() {
      @Override
      public void accept(CommandSender commandSender, CarbonCommand command) { }
    });

    Command mockCommand = Mockito.mock(Command.class);
    Mockito.when(mockCommand.getName()).thenReturn(commandName);

    // test without permission handler
    commando.handleCommand(commandSender, mockCommand, NO_ARGS);

    // test with permission handler
    commando.setNoPermissionHandler(spyHandler);
    commando.handleCommand(commandSender, mockCommand, NO_ARGS);
    Mockito.verify(spyHandler, VerificationModeFactory.times(1)).accept(commandSender, command);
  }

  @Test
  public void completeTypePotionEffects() {
    String commandName = "test";
    commando
      .addCommand(commandName)
      .withArgument(
        new CarbonArgument.Builder("effect")
          .setType(CarbonArgumentType.POTION_EFFECT)
          .create()
      );

    Command command = Mockito.mock(Command.class);
    Mockito.when(command.getName()).thenReturn(commandName);

    List<String> expected = Collections.singletonList("ABSORPTION");
    List<String> result = commando.handleAutoComplete(commandSender, command, new String[] { "a" });

    Assert.assertEquals(expected, result);
  }

  @Test
  public void completeTypeWorlds() {
    String commandName = "test";

    commando
      .addCommand(commandName)
      .withArgument(
        new CarbonArgument.Builder("world")
          .setType(CarbonArgumentType.WORLD)
          .create()
      );

    Command command = Mockito.mock(Command.class);
    Mockito.when(command.getName()).thenReturn(commandName);

    PowerMockito.mockStatic(Bukkit.class);

    World fooWorld = Mockito.mock(World.class);
    World barWorld = Mockito.mock(World.class);

    Mockito.when(fooWorld.getName()).thenReturn("foo");
    Mockito.when(barWorld.getName()).thenReturn("bar");

    Mockito.when(Bukkit.getWorlds()).thenReturn(Arrays.asList(
      fooWorld,
      barWorld
    ));

    List<String> expected = Collections.singletonList("bar");
    List<String> result = commando.handleAutoComplete(commandSender, command, new String[] { "b" });

    Assert.assertEquals(expected, result);

    expected = Arrays.asList("bar", "foo");
    result = commando.handleAutoComplete(commandSender, command, new String[] { "" });

    Assert.assertEquals(expected, result);
  }

  @Test
  public void completeBiome() {
    String commandName = "test";

    commando
      .addCommand(commandName)
      .withArgument(
        new CarbonArgument.Builder("biome")
          .setType(CarbonArgumentType.BIOME)
          .create()
      );

    Command command = Mockito.mock(Command.class);
    Mockito.when(command.getName()).thenReturn(commandName);

    List<String> expected = Arrays.asList("TAIGA", "TAIGA_HILLS", "TAIGA_MOUNTAINS");
    List<String> result = commando.handleAutoComplete(commandSender, command, new String[] { "tai" });

    Assert.assertEquals(expected, result);
  }

  @Test
  public void completeGameMode() {
    String commandName = "test";

    commando
      .addCommand(commandName)
      .withArgument(
        new CarbonArgument.Builder("gamemode")
          .setType(CarbonArgumentType.GAME_MODE)
          .create()
      );

    Command command = Mockito.mock(Command.class);
    Mockito.when(command.getName()).thenReturn(commandName);

    List<String> expected = Collections.singletonList("CREATIVE");
    List<String> result = commando.handleAutoComplete(commandSender, command, new String[] { "cr" });

    Assert.assertEquals(expected, result);
  }

  @Test
  public void completeArt() {
    String commandName = "test";

    commando
      .addCommand(commandName)
      .withArgument(
        new CarbonArgument.Builder("art")
          .setType(CarbonArgumentType.ART)
          .create()
      );

    Command command = Mockito.mock(Command.class);
    Mockito.when(command.getName()).thenReturn(commandName);

    List<String> expected = Collections.singletonList("SKULL_AND_ROSES");
    List<String> result = commando.handleAutoComplete(commandSender, command, new String[] { "skull" });

    Assert.assertEquals(expected, result);
  }

  @Test
  public void completeTypeOnlinePlayers() {
    String commandName = "test";

    commando
      .addCommand(commandName)
      .withArgument(
        new CarbonArgument.Builder("player")
          .setType(CarbonArgumentType.ONLINE_PLAYER)
          .create()
      );

    Command command = Mockito.mock(Command.class);
    Mockito.when(command.getName()).thenReturn(commandName);

    PowerMockito.mockStatic(Bukkit.class);

    Player fooPlayer = Mockito.mock(Player.class);
    Player barPlayer = Mockito.mock(Player.class);

    Mockito.when(fooPlayer.getName()).thenReturn("foo");
    Mockito.when(barPlayer.getName()).thenReturn("bar");

    Mockito.when(Bukkit.getOnlinePlayers()).thenReturn((Collection) Arrays.asList(
      fooPlayer,
      barPlayer
    ));

    List<String> expected = Collections.singletonList("bar");
    List<String> result = commando.handleAutoComplete(commandSender, command, new String[] { "b" });

    Assert.assertEquals(expected, result);

    expected = Arrays.asList("bar", "foo");
    result = commando.handleAutoComplete(commandSender, command, new String[] { "" });

    Assert.assertEquals(expected, result);
  }
}