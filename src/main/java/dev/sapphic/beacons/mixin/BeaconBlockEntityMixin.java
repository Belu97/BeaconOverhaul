package dev.sapphic.beacons.mixin;

import dev.sapphic.beacons.MutableTieredBeacon;
import dev.sapphic.beacons.PotencyTier;
import dev.sapphic.beacons.TieredBeacon;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BeaconBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Objects;

@Mixin(BeaconBlockEntity.class)
abstract class BeaconBlockEntityMixin extends BlockEntity implements MenuProvider, MutableTieredBeacon {
  @Shadow
  int levels;

  @Unique
  private PotencyTier tier = PotencyTier.NONE;

  BeaconBlockEntityMixin(final BlockEntityType<?> type, final BlockPos pos, final BlockState state) {
    super(type, pos, state);
  }

  @Unique
  @Override
  public final PotencyTier getTier() {
    return this.tier;
  }

  @Unique
  @Override
  public final void setTier(final PotencyTier tier) {
    this.tier = Objects.requireNonNull(tier);
  }

  @Inject(
      method =
          "tick(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;"
              + "Lnet/minecraft/world/level/block/state/BlockState;"
              + "Lnet/minecraft/world/level/block/entity/BeaconBlockEntity;)V",
      at = @At(
          target = "Lnet/minecraft/world/level/block/entity/BeaconBlockEntity;"
              + "updateBase(Lnet/minecraft/world/level/Level;III)I",
          shift = At.Shift.BY, by = 2, value = "INVOKE", opcode = Opcodes.INVOKESTATIC),
      locals = LocalCapture.CAPTURE_FAILHARD, require = 1, allow = 1)
  private static void updateTier(
      final Level level, final BlockPos pos, final BlockState state, final BeaconBlockEntity beacon,
      final CallbackInfo ci, final int x, final int y, final int z) {
    var tier = PotencyTier.HIGH;
    var layerOffset = 1;

    layerCheck:
    while (layerOffset <= 4) {
      final var yOffset = y - layerOffset;

      if (yOffset < level.getMinY()) {
        tier = PotencyTier.NONE;
        break;
      }

      for (var xOffset = x - layerOffset; xOffset <= (x + layerOffset); ++xOffset) {
        for (var zOffset = z - layerOffset; zOffset <= (z + layerOffset); ++zOffset) {
          final var stateAt = level.getBlockState(new BlockPos(xOffset, yOffset, zOffset));

          if (!stateAt.is(BlockTags.BEACON_BASE_BLOCKS)) {
            if (layerOffset == 1) {
              tier = PotencyTier.NONE;
            }

            break layerCheck;
          }

          final PotencyTier tierAt;

          if (stateAt.is(PotencyTier.HIGH_POTENCY_BLOCKS)) {
            tierAt = PotencyTier.HIGH;
          } else if (stateAt.is(PotencyTier.LOW_POTENCY_BLOCKS)) {
            tierAt = PotencyTier.LOW;
          } else {
            tierAt = PotencyTier.NONE;
          }

          if (tierAt.ordinal() < tier.ordinal()) {
            tier = tierAt;
          }
        }
      }

      ++layerOffset;
    }

    ((MutableTieredBeacon) beacon).setTier(tier);
  }

  @ModifyVariable(
      method =
          "applyEffects(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;I"
              + "Lnet/minecraft/core/Holder;Lnet/minecraft/core/Holder;)V",
      at = @At(value = "STORE", opcode = Opcodes.DSTORE, ordinal = 0),
      index = 5, require = 1, allow = 1)
  private static double modifyEffectRadius(final double radius, final Level level, final BlockPos pos) {
    if (level.getBlockEntity(pos) instanceof final TieredBeacon beacon) {
      return radius + (10.0 * beacon.getTier().ordinal());
    }

    return radius; // (levels * 10) + 10
  }

  @ModifyVariable(
      method =
          "applyEffects(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;I"
              + "Lnet/minecraft/core/Holder;Lnet/minecraft/core/Holder;)V",
      at = @At(value = "STORE", opcode = Opcodes.ISTORE, ordinal = 0),
      index = 7, require = 1, allow = 1)
  private static int modifyPrimaryAmplifier(
      final int primaryAmplifier, final Level level, final BlockPos pos, final int levels,
      final @Nullable net.minecraft.core.Holder<MobEffect> primaryEffect) {
    if (primaryEffect != MobEffects.NIGHT_VISION) {
      if (level.getBlockEntity(pos) instanceof final TieredBeacon beacon) {
        return beacon.getTier().ordinal();
      }
    }

    return primaryAmplifier; // 0
  }

  @ModifyVariable(
      method =
          "applyEffects(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;I"
              + "Lnet/minecraft/core/Holder;Lnet/minecraft/core/Holder;)V",
      at = @At(value = "STORE", opcode = Opcodes.ISTORE, ordinal = 1),
      index = 7, require = 1, allow = 1)
  private static int modifyPotentPrimaryAmplifier(
      final int primaryAmplifier, final Level level, final BlockPos pos, final int levels,
      final @Nullable net.minecraft.core.Holder<MobEffect> primaryEffect, final @Nullable net.minecraft.core.Holder<MobEffect> secondaryEffect) {
    if ((primaryEffect != MobEffects.NIGHT_VISION)
        && (secondaryEffect != MobEffects.SLOW_FALLING)
        && (secondaryEffect != MobEffects.FIRE_RESISTANCE)) {
      if (level.getBlockEntity(pos) instanceof final TieredBeacon beacon) {
        return primaryAmplifier + beacon.getTier().ordinal();
      }
    }

    return primaryAmplifier; // 1
  }

  @ModifyVariable(
      method =
          "applyEffects(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;I"
              + "Lnet/minecraft/core/Holder;Lnet/minecraft/core/Holder;)V",
      at = @At(value = "STORE", opcode = Opcodes.ISTORE, ordinal = 0),
      index = 8, require = 1, allow = 1)
  private static int modifyDuration(final int duration, final Level level, final BlockPos pos, final int levels) {
    if (level.getBlockEntity(pos) instanceof final TieredBeacon beacon) {
      return ((9 * (beacon.getTier().ordinal() + 1)) + (levels * 2)) * 20;
    }

    return duration; // (9 + levels * 2) * 20
  }

  // Cannot use ModifyArg here as we need to capture the target method parameters
  @ModifyConstant(
      method =
          "applyEffects(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;I"
              + "Lnet/minecraft/core/Holder;Lnet/minecraft/core/Holder;)V",
      constant = @Constant(/*intValue = 0,*/ ordinal = 1),
      require = 1, allow = 1)
  private static int modifySecondaryAmplifier(
      final int secondaryAmplifier, final Level level, final BlockPos pos, final int levels,
      final @Nullable net.minecraft.core.Holder<MobEffect> primaryEffect, final @Nullable net.minecraft.core.Holder<MobEffect> secondaryEffect) {
    if ((secondaryEffect != MobEffects.SLOW_FALLING)
        && (secondaryEffect != MobEffects.FIRE_RESISTANCE)) {
      if (level.getBlockEntity(pos) instanceof final TieredBeacon beacon) {
        return beacon.getTier().ordinal();
      }
    }

    return secondaryAmplifier; // 0
  }

  @ModifyArg(
      method = "createMenu(ILnet/minecraft/world/entity/player/Inventory;Lnet/minecraft/world/entity/player/Player;)Lnet/minecraft/world/inventory/AbstractContainerMenu;",
      at = @At(
          value = "INVOKE",
          target = "Lnet/minecraft/world/inventory/BeaconMenu;<init>(ILnet/minecraft/world/Container;Lnet/minecraft/world/inventory/ContainerData;Lnet/minecraft/world/inventory/ContainerLevelAccess;)V"
      ),
      index = 2, require = 1, allow = 1
  )
  private ContainerData wrapTieredData(final ContainerData data) {
    return new dev.sapphic.beacons.TieredBeaconData((BeaconBlockEntity) (Object) this, data);
  }
}
