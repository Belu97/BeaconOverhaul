package dev.sapphic.beacons;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

public enum PotencyTier {
  NONE,
  LOW,
  HIGH;

  public static final TagKey<Block> LOW_POTENCY_BLOCKS = createBlockTag("low_potency");
  public static final TagKey<Block> HIGH_POTENCY_BLOCKS = createBlockTag("high_potency");

  private static TagKey<Block> createBlockTag(final String name) {
    return TagKey.create(BuiltInRegistries.BLOCK.key(), Identifier.fromNamespaceAndPath(BeaconMobEffects.NAMESPACE, name));
  }
}
