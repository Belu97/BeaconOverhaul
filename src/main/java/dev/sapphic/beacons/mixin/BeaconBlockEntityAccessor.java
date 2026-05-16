package dev.sapphic.beacons.mixin;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.level.block.entity.BeaconBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Set;

@Mixin(BeaconBlockEntity.class)
public interface BeaconBlockEntityAccessor {
  @Accessor("VALID_EFFECTS")
  @Mutable
  static void setValidEffects(final Set<net.minecraft.core.Holder<MobEffect>> value) {
    throw new AssertionError();
  }

  @Accessor("BEACON_EFFECTS")
  @Mutable
  static void setBeaconEffects(final java.util.List<java.util.List<net.minecraft.core.Holder<MobEffect>>> value) {
    throw new AssertionError();
  }
}
