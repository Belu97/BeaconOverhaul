package dev.sapphic.beacons.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
abstract class LivingEntityMixin extends Entity {
  @Unique
  private @MonotonicNonNull Double baseUpStep;

  @Unique
  private boolean stepIncreased;

  LivingEntityMixin(final EntityType<?> type, final Level level) {
    super(type, level);
  }

  @Shadow
  public abstract boolean hasEffect(final Holder<MobEffect> effect);

  @Shadow
  public abstract AttributeInstance getAttribute(final Holder<net.minecraft.world.entity.ai.attributes.Attribute> attribute);


  @Inject(method = "baseTick()V", at = @At("HEAD"), require = 1)
  private void setBaseUpStep(final CallbackInfo ci) {
    if (this.baseUpStep == null) {
      AttributeInstance stepAttr = this.getAttribute(Attributes.STEP_HEIGHT);
      if (stepAttr != null) {
        this.baseUpStep = stepAttr.getBaseValue();
      }
    }
  }

  @Inject(method = "tickEffects()V", at = @At("HEAD"), require = 1)
  private void updateJumpBoostStepAssist(final CallbackInfo ci) {
    if (this.hasEffect(MobEffects.JUMP_BOOST) && !this.isCrouching()) {
      if (!this.stepIncreased) {
        AttributeInstance stepAttr = this.getAttribute(Attributes.STEP_HEIGHT);
        if (stepAttr != null) stepAttr.setBaseValue(1.0);
        this.stepIncreased = true;
      }
    } else if (this.stepIncreased) {
      AttributeInstance stepAttr = this.getAttribute(Attributes.STEP_HEIGHT);
      if (stepAttr != null && this.baseUpStep != null) stepAttr.setBaseValue(this.baseUpStep);
      this.stepIncreased = false;
    }
  }

  @ModifyExpressionValue(
      method = "getEffectiveGravity()D",
      at = @At(value = "CONSTANT", args = "doubleValue=0.01"),
      require = 1)
  private double dropIfCrouching(final double slowFallingGravity) {
    return this.isCrouching() ? this.getGravity() : slowFallingGravity;
  }

  @Inject(
      method = "travel(Lnet/minecraft/world/phys/Vec3;)V",
      at = @At(
          target = "Lnet/minecraft/world/entity/LivingEntity;travelInAir(Lnet/minecraft/world/phys/Vec3;)V",
          value = "INVOKE"),
      require = 1)
  private void dropIfCrouching(final net.minecraft.world.phys.Vec3 vec3, final CallbackInfo ci) {
    if (this.isCrouching()) {
      // In 1.21.10, we don't need to manually modify deltaMovement here if we use getEffectiveGravity
      // But the original mod might have done more.
    }
  }
}
