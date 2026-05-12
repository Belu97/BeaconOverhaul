package dev.sapphic.beacons.client.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Environment(EnvType.CLIENT)
@Mixin(LightTexture.class)
abstract class LightTextureMixin {
  @Shadow
  @Final
  private Minecraft minecraft;

  @ModifyExpressionValue(
      method = "updateLightTexture(F)V",
      at = @At(value = "INVOKE", target = "Ljava/lang/Double;floatValue()F", ordinal = 1),
      require = 1)
  private float fullBrightNightVision(final float gamma) {
    final @Nullable LocalPlayer player = this.minecraft.player;

    if (player == null) {
      return gamma;
    }

    final @Nullable MobEffectInstance nightVision = player.getEffect(MobEffects.NIGHT_VISION);
    return ((nightVision != null) && (nightVision.getAmplifier() > 0)) ? 15.0F : gamma;
  }
}
