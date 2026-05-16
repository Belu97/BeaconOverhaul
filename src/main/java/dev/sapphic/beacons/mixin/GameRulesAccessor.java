package dev.sapphic.beacons.mixin;
 
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.gamerules.GameRule;
import net.minecraft.world.level.gamerules.GameRuleCategory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;
 
@Mixin(GameRules.class)
public interface GameRulesAccessor {
  @Invoker("registerInteger")
  static GameRule<Integer> callRegisterInteger(
      final String name, final GameRuleCategory category, final int defaultValue, final int min, final int max) {
    throw new AssertionError();
  }
}
