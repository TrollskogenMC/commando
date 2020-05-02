## commando [![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
A Bukkit library for creating commands in a declarative way and encourages writing encapsulated command executioners.

## Motivation
What I like with Bukkits API for creating commands is that it makes no assumption about the developers intentions. That is good and Commando is built on top of that mindset, trying not to make assumptions about the developers way of doing things.
Making commands that is fool proof and with validation, error handling and tab completion is a challenging and mostly repetitive task.

---

- [How to use](#how-to-use)
  - [Setup](#setup)
  - [Creating our first command](#creating-our-first-command)
  - [Arguments](#arguments)
    - [Custom arguments](#custom-arguments)
    - [Invalid arguments](#invalid-arguments)
    - [Missing arguments](#missing-arguments)
    - [Validating based on previous arguments](#validating-based-on-previous-arguments)
    - [Tab completion](#tab-completion)
      - [Tab completion based on previous arguments](#tab-completion-based-on-previous-arguments)
    - [Default arguments](#default-arguments)
      - [Static arguments](#static-arguments)
      - [Dynamic arguments](#dynamic-arguments)
  - [Permissions](#permissions)
    - [Static permissions](#static-permissions)
    - [Dynamic permissions](#dynamic-permissions)
  - [Sub commands](#sub-commands)
    - [Handle non existing sub commands](#handle-non-existing-sub-commands)
  - [~~Loose arguments~~](#loose-arguments)
    - [~~Fixed and loose arguments~~](#fixed-and-loose-arguments)
- [Configuration helper](#configuration-helper)
- [Translation helper](#translation-helper)
## How to use

### Setup
Commando is not yet publiced on any registry so local install is the only way to use it right now.

### Creating our first command
Commando allows you to create a command as a chained expression.

```java
public final class MyPlugin extends JavaPlugin {
  private Commando commando;

  @Override
  public void onEnable() {
    // initialize Commando
    commando = new Commando();

    // create command
    commando.addCommand("ping").withHandler((CommandSender commandSender, String[] args) -> {
      commandSender.sendMessage("pong");
    });
  }

  // forward commands to commando
  @Override
  public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    return commando.handleCommand(sender, command, args);
  }
}
```

Our first created command through Commando. Easy right?

Let's give the the command an argument. We can use the `withArgument` method on CarbonCommand for that.

```java
commando.addCommand("hello").withArgument(new CarbonArgument.Builder("name").create()).withHandler((CommandSender commandSender, String[] args) -> {
  commandSender.sendMessage("hello " + args[0]);
});
```

Expressing the command on the same line makes it unreadable, let's split it up for readability.

```java
commando
  .addCommand("hello")
  .withArgument(new CarbonArgument.Builder("name").create())
  .withHandler((CommandSender commandSender, String[] args) -> {
    commandSender.sendMessage("hello " + args[0]);
  });
```

Great! Our command can take one argument. Let's give it a few more.

```java
commando
  .addCommand("teleport")
  .withArgument(new CarbonArgument.Builder("x").setType(CarbonArgumentType.INTEGER).create())
  .withArgument(new CarbonArgument.Builder("y").setType(CarbonArgumentType.INTEGER).create())
  .withArgument(new CarbonArgument.Builder("z").setType(CarbonArgumentType.INTEGER).create())
  .withHandler((CommandSender commandSender, String[] args) -> {
    Player player = (Player) commandSender;
    int x = Integer.parseInt(args[0]);
    int y = Integer.parseInt(args[1]);
    int z = Integer.parseInt(args[2]);
    Location location = player.getWorld().getBlockAt(x, y, z).getLocation();
    player.teleport(location);
  });
```

Cool! Now we made ourselves a teleport to location command. But we need think about separation of concerns. We'll want to separate the command execution from the command setup. Let's do that now.

```java
import ICommandHandler;

public class CommandTeleport implements ICommandHandler {
  @Override
  public void handle(CommandSender commandSender, String[] args) {
    Player player = (Player) commandSender;
    int x = Integer.parseInt(args[0]);
    int y = Integer.parseInt(args[1]);
    int z = Integer.parseInt(args[2]);
    Location location = player.getWorld().getBlockAt(x, y, z).getLocation();
    player.teleport(location);
  }
}
```

Awesome! Now let's instantiate CommandTeleport.

```java
commando
  .addCommand("teleport")
  .withArgument(new CarbonArgument.Builder("x").setType(CarbonArgumentType.INTEGER).create())
  .withArgument(new CarbonArgument.Builder("y").setType(CarbonArgumentType.INTEGER).create())
  .withArgument(new CarbonArgument.Builder("z").setType(CarbonArgumentType.INTEGER).create())
  .withHandler(new CommandTeleport());
```

But wait! What if the server operator typed `teleport 24 74 85` in the console? We've designed this command to only be run by players from within the game. The command will raise an exception if we were to try and cast CommandSender to Player. We can prevent it by adding just one line; no more if statements bloating your command.
*If we wanted to do the opposite, preventing players from typing a command, we can add `.preventPlayerCommandSender()`*

```java
commando
  .addCommand("teleport")
  .withArgument(new CarbonArgument.Builder("x").setType(CarbonArgumentType.INTEGER).create())
  .withArgument(new CarbonArgument.Builder("y").setType(CarbonArgumentType.INTEGER).create())
  .withArgument(new CarbonArgument.Builder("z").setType(CarbonArgumentType.INTEGER).create())
  .withHandler(new CommandTeleport())
  .preventConsoleCommandSender(); // typing "teleport 24 74 85" in the console won't do a thing now
```

### Arguments
Most commands will need some arguments to it. Expressing arguments in Commando is both declaritative and easy to write.

```java
CarbonArgument nameArgument = new CarbonArgument.Builder("myArg").create();
```

We can now use this argument like so:

```java
commando
  .addCommand("hello")
  .withArgument(nameArgument)
  .withHandler((CommandSender sender, String[] args) -> {
    sender.sendMessage("Hello " + args[0])
  });
```

Commando will set the default value of all arguments to a string unless you specify other.
If we want an integer we can do it like so:

```java
CarbonArgument ageArgument = new CarbonArgument.Builder("age").setType(CarbonArgumentType.INTEGER).create();
```

Now Commando will make sure that the command logic is never called if the argument can't be converted to an integer.
Commando allows us to handle the case when our argument isn't of the wanted type:

```java
commando.handleValidation((ValidationResult result) -> {
  switch (result.getStatus()) {
    case ERR_INCORRECT_TYPE:
      String helpTexts = result.getCommand().getHelpTexts().stream().collect(Collectors.joining("\n")));
      String argument = result.getArgument().getName();
      String received = result.getValue();
      String message = "§cExpected argument `" + argument + "` to be an integer but got `" + received + "`.\n\nUsage:\n" + helpTexts;
      result.getCommandSender().sendMessage(message);
      break;
  }
});
```

There are in fact a bunch of possible types of arguments you can have, some of them are listed below:
STRING,
INTEGER,
NUMBER,
BOOLEAN,
ONLINE_PLAYER,
MATERIAL,
WORLD

#### Custom arguments
...
#### Invalid arguments
How do we react when an argument is invalid. Often we want to tell the command sender what is wrong. We can do that by overriding the method `whenInvalid` in the argument validator class.

```java
import com.github.hornta.ValidationHandler;

public class IsIntegerValidator implements ValidationHandler {
  @Override
  public boolean test(CommandSender sender, String[] args) {
    try {
      for (String A : d) {
        Integer.parseInt(args[0]);
      }
      return true;
    } catch(NumberFormatException e) {
      return false;
    }
  }

  @Override
  public void whenInvalid(CommandSender sender, String[] args) {
    sender.sendMessage("Expected argument of type integer but received " + args[0]);
  }
}
```

#### Missing arguments
Suppose you have a command that takes 3 or more arguments. It's easy for command senders to send the wrong number of arguments. In those cases we would want to give them a helping hand. We can give Commando a handler that will be executed whenever a missing argument occurs.

```java
commando.setMissingArgumentHandler((CommandSender sender, CarbonCommand command) -> {
  sender.sendMessage("You've entered an incorrect number of arguments.\n\nUsage:\n" + command.getHelpText());
});
```

#### Validating based on previous arguments
..

#### Tab completion
A good plugin uses tab completion wherever possible and Commando makes it very easy for you. In most cases you won't have to do a thing, but it depends on what argument type you use.

Suppose we are making a command to change gamemode so when we tab complete we want to show the command sender all the possible values.
The first thing we need to do is to forward the tab completion requests to Commando. We can do it like this.
```java
public final class MyPlugin extends JavaPlugin {
  private Commando commando;

  @Override
  public void onEnable() {
    commando = new Commando();
  }

  @Override
  public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
    return commando.getCommandManager().handleAutoComplete(sender, command, args);
  }
}
```

So now that Commando is ready to handle tab completions we can go ahead and start writing our command logic.
```java
import ICommandHandler;

public class CommandGameMode implements ICommandHandler {
  @Override
  public void handle(CommandSender commandSender, String[] args) {
    Player player = (Player) commandSender;
    GameMode gameMode = GameMode.valueOf(args[0].toUpperCase());
    player.setGameMode(gameMode);
    player.sendMessage("Your game mode has been changed.");
  }
}
```

And let's handle the case when the input doesn't convert to a GameMode
```java
commando.handleValidation((ValidationResult result) -> {
  switch (result.getStatus()) {
    case ERR_OTHER:
      if(result.getCommand().getType() != CarbonArgumentType.GAME_MODE) {
          result.getCommandSender().sendMessage("You've entered an invalid game mode. Please try again.");
      }
      break;
  }
});
```

And finally we can setup the command

```java
commando
  .addCommand("gamemode")
  .withArgument(new CarbonArgument.Builder("mode").setType(CarbonArgumentType.GAME_MODE).create())
  .withHandler(new CommandGameMode())
  .preventConsoleCommandSender();
```

Tab completion is automatically added for all possible game modes.

##### Tab completion based on previous arguments
There are rare cases of when tab completion results are based on one or several previous arguments. An example of this could be a command that let's you check up a players home, `/lookuphome hornta mine`. Suppose we wan't to tab complete the second argument of the `/lookuphome` command. In order for us to do that we would need to know the player to get the homes for, thus we would need access to a previous argument; `player`.

Let's create our tab completer. We expect the player name to be the first item in `arguments`.
```java
public class PlayerHomesArgument implements IArgumentHandler {
  @Override
  public Set<String> getItems(CommandSender sender, String arguments, String[] prevArgs) {
    Player player;
    for (Player lookupPlayer : Bukkit.getOnlinePlayers()) {
      if (lookupPlayer.getName() == prevArgs[0]) {
        player = lookupPlayer;
        break;
      }
    }

    return getPlayerHomes(player).stream().filter((Home home) -> {
      return home.getName().startsWith(argument);
    }).collect(Collectors.toCollection(LinkedHashSet::new));
  }
}
```

So our command will need two arguments, the player and the player home. Let's define those arguments and attach them to the command.
```java
CarbonArgument playerArgument = new CarbonArgument.Builder("player").setType(CarbonArgumentType.ONLINE_PLAYER).create();
CarbonArgument playerHomeArgument =
  new CarbonArgument.Builder("home")
    .setHandler(new PlayerHomesArgument)
    .dependsOn(playerArgument) // this is what makes our argument handler depend on the player argument
    .create();

commando
  .addCommand("lookuphome")
  .withArgument(playerArgument)
  .withArgument(playerHomeArgument)
  .withHandler(new LookupHomeCommand());
```

#### Default arguments
##### Static arguments
There are times when you don't want to force the player to enter an argument to a command. A perfect example of this is the faumous `/home` command accompanied by `/sethome`. When typing `/home` it will teleport you to your default home which you set by typing `/sethome`.
You can also create named homes by typing `/sethome mine` and then teleport to there with `/home mine`. In this example `mine` is the argument to the home and sethome command.

Let's see how can set these up.

```java
commando
  .addCommand("sethome")
  .withArgument(new CarbonArgument.Builder("name").setDefaultValue("tellus").create())
  .withHandler(new CommandSetHome())
  .preventConsoleCommandSender();
```

Notice how we used the `setDefaultValue` method and gave it a default value of "tellus".
Whenever the player types `/sethome`, it will actually be `/sethome tellus`. Super cool stuff!

##### Dynamic arguments
The `/home` command is a little bit tricker. We know that the /home command will teleport the player to their first created home, if they have one. We can no longer default to a fixed name of "tellus" like before. Imagine the player creating a bunch of named homes
* tellus
* mine
* nether

And suddenly the player decides to delete their default home by typing `/delhome tellus`. That may not be what we, the developers are expecting the player to do, but it could be possible. What should happen if the player now types `/home`? One could argue that we should tell the player
"Sorry, you do not have a default home, please create one with /sethome".
But we can be better than that. Let's default to the players first home (if they have one).

We can give `withDefaultArgument` a function making it possible to return a value depending on which player executes the command.

```java
commando
  .addCommand("home")
  .withArgument(
    new CarbonArgument.Builder("name")
      .setDefaultValue((CommandSender sender) -> getPlayerFirstHome(sender))
      .create()
  )
  .withHandler(new CommandHome())
  .preventConsoleCommandSender();
```

### Permissions
#### Static permissions
You'll obviously want to control the required permission of a command and Commando makes this easy for you. Commando will even warn you when a command doesn't have a permission (this can be turned of, but you should always hide a command under a permission).

```java
commando
  .addCommand("spawn")
  .withHandler(new CommandSpawn())
  .requiresPermission("myplugin.spawn")
  .preventConsoleCommandSender();
```

Reacting to unauthorized command requests is easy. You'll almost always want to tell the command sender the same thing; "You don't have permission".

```java
commando.setNoPermissionHandler((CommandSender sender, CarbonCommand command) -> {
  // log sender attempting to access command
  sender.sendMessage("You don't have permission to execute this command.");
});

commando.addCommand(...)
```
#### Dynamic permissions
Dynamic permission nodes is also fully supported by Commando. Suppose you have a special warp and you want players to have a certain permission to use that warp. Suppose the permission node looks like this `myplugin.warps.mysecretwarp`. Players may only be using `/warp mysecretwarp` when they have that permission node. We could implement the permission logic in the command handler but then why would we when we can let Commando do it for us.

By specifying the permission with a square bracket, Commando will replace it with whatever that argument is. `[0]` will be replaced with the first argument. `[2]` will be replaced with the third argument and so on.

```java
commando
  .addCommand("warp")
  .withArgument(new CarbonArgument.Builder("name").create())
  .withHandler(new CommandWarp())
  .requiresPermission("myplugin.warps.[0]")
  .preventConsoleCommandSender();
```

### Sub commands
We can create sub commands just as easy as regular commands.
```java
commando
  .addCommand("foo bar")
  .requiresPermission("foo.bar")
  .withHandler(new CommandFooBar());

commando
  .addCommand("foo baz")
  .requiresPermission("foo.baz")
  .withHandler(new CommandFooBaz());
```

What's powerful about this is that a player will get tab completion for `bar` and `baz` when typing `/effect`.
**The tab completion results will only show commands that the player has permission to execute, provided you have specified a permission for the command with `.requiresPermission()`**

#### Handle non existing sub commands
Commando gives you the possibility to act on typing non existing sub commands.

Suppose your plugin has created this command.
```java
commando
  .addCommand("foo bar")
  .requiresPermission("foo.bar")
  .withHandler(new CommandFooBar());
```
What would happen if a command sender types `/foo` or `/foo baz`? If you choose not to act on this, then nothing will happen.
However, you can tell Commando that you want to act on this by `.setMissingCommandHandler`.
Commando will call this handler with the sender and a list of command suggestions.
```java
commando.setMissingCommandHandler((CommandSender sender, List<CarbonCommand> suggestions) -> {
  String suggestions = suggestions.stream()
    .map(CarbonCommand::getHelpText)
    .collect(Collectors.joining("\n"));
  sender.sendMessage("Command wasn't found.\nSuggestions:\n" + suggestions);
});
```

### ~~Loose arguments~~
**Support for loose arguments will come back later in a new fashion**

~~Somtimes you'll create a command that can take any amount of arguments wether it is zero, two or 30 arguments. Simply omit the `.setNumberOfArguments` method from the command setup and Commando will make sure your command handler is executed regardless on how many arguments is passed.~~

~~You can compare it to Javas [*Varargs*](https://www.baeldung.com/java-varargs)~~

~~Let's create a command that broadcasts a message to the entire server~~
```java
commando
  .addCommand("broadcast")
  .setHelpText("/broadcast <message>")
  .withHandler(new CommandBroadcast());
```

~~And create `CommandBroadcast`~~
```java
public void handle(CommandSender commandSender, String[] args) {
  String message = String.join(" ", d);
  Bukkit.broadcastMessage(message);
}
```

#### ~~Fixed and loose arguments~~
~~What if we could modify the command above so that we can broadcast a message to players with a certain permission. The arguments to the command could be `permission` and `message`.
Here I am telling Commando that `CommandBroadcastPermissionCommand` needs at least one argument (the permission node; a fixed argument) and the rest of the arguments will be the actual message (a loose argument).~~
```java
commando
  .addCommand("broadcast")
  .setMinNumberOfArguments(1)
  .setHelpText("/broadcast <permission> <message>")
  .withHandler(new CommandBroadcastPermissionCommand());
```
~~CommandBroadcastPermissionCommand could look like this~~
```java
public class CommandBroadcastPermissionCommand implements ICommandHandler {
  @Override
  public void handle(CommandSender commandSender, String[] args) {
    String permission = args[0];
    String message = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
    for (Player player : Bukkit.getOnlinePlayers()) {
      if (player.hasPermission(permission)) {
        player.sendMessage(message);
      }
    }
  }
}
```

## Configuration helper
Dealing with a configuration file is really simple without any extra things. What Commando adds to this is a consice way of declaring your config.yml programmatically. Normally you are creating a config.yml file in your resources folder, but this Commando you don't do that. Instead you setup the config values in code. This gives you a lot of flexibility when you want to fetch those config option in code. It also deals with things like unused config values and new config values.

Below is an example of a plugin of mine called Wild. I have a bunch of configuration options with different types and default values. When `.build()` is called on the ConfigurationBuilder object, then it attempts to create a config.yml file in the plugin data folder populated with the default config values.
If a config.yml already exists, then it will see if that config.yml contains values not found in this setup and delete them. It won't overwrite current values. It will add missing values not found in the config.yml.
```java
// ConfigKey.java
public enum ConfigKey {
  LANGUAGE,
  COOLDOWN,
  TRIES,
  NO_BORDER_SIZE
}

// MyPlugin.java
try {
  configuration = new ConfigurationBuilder(this)
    .add(ConfigKey.LANGUAGE, "language", ConfigType.STRING.STRING, "english")
    .add(ConfigKey.COOLDOWN, "cooldown", ConfigType.INTEGER, 60)
    .add(ConfigKey.TRIES, "tries", ConfigType.INTEGER, 10)
    .add(ConfigKey.NO_BORDER_SIZE, "no_border_size", ConfigType.INTEGER, 5000)
    .add(ConfigKey.USE_VANILLA_WORLD_BORDER, "use_vanilla_world_border", ConfigType.BOOLEAN, false)
    .build();
} catch (Exception e) {
  setEnabled(false);
  getLogger().log(Level.SEVERE, e.getMessage(), e);
}
```

To reload the configuration you call the `.reload()` method on the configuration object.
```java
try {
  configuration.reload();
} catch (Exception e) {
  getLogger().log(Level.WARNING, e.getMessage(), e);
  commandSender.sendMessage("Failed to reload the configuration.");
}
```

Accessing configuration options in code looks like this:
```
int cooldown = Configuration.get(ConfigKey.COOLDOWN);
```

Most of the times the type can be implicitly cast but sometimes you will need to manually cast it to the expected type.

## Translation helper
...