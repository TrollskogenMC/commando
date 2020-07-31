package se.hornta.commando;

import se.hornta.commando.completers.*;
import se.hornta.commando.completers.ArtCompleter;
import se.hornta.commando.completers.BiomeCompleter;
import se.hornta.commando.completers.BooleanEffectCompleter;
import se.hornta.commando.completers.GameModeCompleter;
import se.hornta.commando.completers.MaterialCompleter;
import se.hornta.commando.completers.OnlinePlayerCompleter;
import se.hornta.commando.completers.PotionEffectCompleter;
import se.hornta.commando.completers.WorldCompleter;
import se.hornta.commando.completers.WorldNormalCompleter;

public enum CarbonArgumentType {
  STRING(true),
  INTEGER(true),
  NUMBER(true),
  BOOLEAN(BooleanEffectCompleter.class),
  ONLINE_PLAYER(OnlinePlayerCompleter.class),
  WORLD(WorldCompleter.class),
  WORLD_NORMAL(WorldNormalCompleter.class),
  POTION_EFFECT(PotionEffectCompleter.class),
  GAME_MODE(GameModeCompleter.class),
  BIOME(BiomeCompleter.class),
  ART(ArtCompleter.class),
  MATERIAL(MaterialCompleter.class),
  DURATION(true),
  OTHER(false);

  private final Class<?> completer;
  private final boolean isPrimitive;

  CarbonArgumentType(boolean isPrimitive) {
    completer = null;
    this.isPrimitive = isPrimitive;
  }

  CarbonArgumentType(Class<?> completer) {
    this.completer = completer;
    isPrimitive = false;
  }

  public boolean isCompletable() {
    return completer != null;
  }

  public Class<?> getCompleter() {
    return completer;
  }

  public boolean isPrimitive() {
    return isPrimitive;
  }
}
