package dev.sapphic.beacons.client.mixin;

import dev.sapphic.beacons.BeaconMobEffects;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Environment(EnvType.CLIENT)
@Mixin(Gui.class)
abstract class GuiMixin {
  @Shadow
  @Final
  private Minecraft minecraft;

  @Shadow
  private Player getCameraPlayer() {
    throw new AssertionError();
  }

  @ModifyVariable(
      method = "renderFood(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/world/entity/player/Player;II)V",
      at = @At(value = "STORE", ordinal = 1), // Targeting the second store to slot 8 (after the shake logic)
      index = 8, require = 1)
  private int noNutritionHungerShake(final int shakenY, final GuiGraphics graphics, final Player player, final int x, final int y) {
    final Player cameraPlayer = this.getCameraPlayer();

    if ((cameraPlayer != null) && !cameraPlayer.getFoodData().needsFood()) {
      if (cameraPlayer.hasEffect(BeaconMobEffects.NUTRITION)) {
        return y; // Return the original Y coordinate (parameter 'y') instead of the shaken one
      }
    }

    return shakenY;
  }
}
