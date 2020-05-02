package com.github.hornta.commando;

import com.github.hornta.commando.completers.*;

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
