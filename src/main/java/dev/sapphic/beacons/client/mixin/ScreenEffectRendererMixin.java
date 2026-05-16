package dev.sapphic.beacons.client.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.ScreenEffectRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.effect.MobEffects;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ScreenEffectRenderer.class)
abstract class ScreenEffectRendererMixin {
  @Inject(
      method = "renderFire(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;Lnet/minecraft/client/renderer/texture/TextureAtlasSprite;)V",
      at = @At("HEAD"), require = 1, allow = 1, cancellable = true)
  private static void omitFireOverlayIfResistant(final PoseStack stack, final MultiBufferSource bufferSource, final TextureAtlasSprite sprite, final CallbackInfo ci) {
    final Minecraft mc = Minecraft.getInstance();
    if ((mc.player != null) && mc.player.hasEffect(MobEffects.FIRE_RESISTANCE)) {
      ci.cancel();
    }
  }
}
