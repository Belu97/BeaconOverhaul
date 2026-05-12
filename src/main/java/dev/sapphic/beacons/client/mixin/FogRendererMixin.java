package dev.sapphic.beacons.client.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.fog.FogRenderer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Environment(EnvType.CLIENT)
@Mixin(FogRenderer.class)
abstract class FogRendererMixin {
  // In 1.21.10, setupColor is replaced by computeFogColor which returns a Vector4f
  // and static color fields are gone. We'll need a more complex injection to port this correctly.
  // For now, we'll keep the method signature updated but disable the logic.

  @Inject(
      method = "computeFogColor(Lnet/minecraft/client/Camera;FLnet/minecraft/client/multiplayer/ClientLevel;IFZ)Lorg/joml/Vector4f;",
      at = @At(
          target = "Lnet/minecraft/client/renderer/GameRenderer;"
              + "getNightVisionScale(Lnet/minecraft/world/entity/LivingEntity;F)F",
          value = "INVOKE"),
      cancellable = true, require = 0) // require = 0 until we verify the target in mapped environment
  private void skipNightVisionColorShift(
      final Camera camera, final float f, final ClientLevel level, final int i, final float g, final boolean bl,
      final CallbackInfoReturnable<org.joml.Vector4f> cir
  ) {
    final LivingEntity living = (LivingEntity) camera.getEntity();
    final MobEffectInstance nightVision = living.getEffect(MobEffects.NIGHT_VISION);

    if ((nightVision != null) && (nightVision.getAmplifier() > 0)) {
      // Logic to return unshifted color would go here
    }
  }
}
