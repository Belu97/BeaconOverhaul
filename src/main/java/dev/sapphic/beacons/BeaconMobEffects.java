package dev.sapphic.beacons;

import dev.sapphic.beacons.mixin.BeaconBlockEntityAccessor;
import dev.sapphic.beacons.mixin.GameRulesAccessor;
import net.fabricmc.api.ModInitializer;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.gamerules.GameRule;
import net.minecraft.world.level.gamerules.GameRuleCategory;
import net.minecraft.world.level.block.entity.BeaconBlockEntity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public final class BeaconMobEffects implements ModInitializer {
  static final String NAMESPACE = "beaconoverhaul";

  public static final GameRule<Integer> LONG_REACH_INCREMENT =
      GameRulesAccessor.callRegisterInteger(
          "long_reach_increment", GameRuleCategory.PLAYER, 2, 0, 1024);

  public static final Holder<MobEffect> LONG_REACH =
      Registry.registerForHolder(BuiltInRegistries.MOB_EFFECT, Identifier.fromNamespaceAndPath(NAMESPACE, "long_reach"),
          new MobEffect(MobEffectCategory.BENEFICIAL, 0xDEF58F) {
          }.addAttributeModifier(Attributes.BLOCK_INTERACTION_RANGE,
              Identifier.fromNamespaceAndPath(NAMESPACE, "long_reach_block"), 1.0, AttributeModifier.Operation.ADD_VALUE
          ).addAttributeModifier(Attributes.ENTITY_INTERACTION_RANGE,
              Identifier.fromNamespaceAndPath(NAMESPACE, "long_reach_entity"), 1.0, AttributeModifier.Operation.ADD_VALUE
          ));

  public static final Holder<MobEffect> NUTRITION =
      Registry.registerForHolder(BuiltInRegistries.MOB_EFFECT, Identifier.fromNamespaceAndPath(NAMESPACE, "nutrition"),
          new MobEffect(MobEffectCategory.BENEFICIAL, 0x8A1A22) {
            @Override
            public boolean applyEffectTick(net.minecraft.server.level.ServerLevel level, LivingEntity entity, int amplifier) {
              if (entity instanceof Player player) {
                if (player.getFoodData().needsFood()) {
                  player.getFoodData().eat(1, 0.0F);
                }
              }
              return true;
            }

            @Override
            public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
              return (duration % Math.max(1, (100 >> amplifier))) == 0;
            }
          });

  private static void addMobEffectsToBeacon() {
    final List<List<Holder<MobEffect>>> effects = new ArrayList<>();
    for (List<Holder<MobEffect>> tier : BeaconBlockEntity.BEACON_EFFECTS) {
      effects.add(new ArrayList<>(tier));
    }

    effects.get(0).add(MobEffects.NIGHT_VISION);
    effects.get(1).add(LONG_REACH);
    effects.get(2).add(NUTRITION);
    effects.get(3).add(MobEffects.FIRE_RESISTANCE);
    effects.get(3).add(MobEffects.SLOW_FALLING);

    // Update BEACON_EFFECTS
    List<List<Holder<MobEffect>>> immutableEffects = effects.stream()
        .map(List::copyOf)
        .collect(Collectors.toList());
    BeaconBlockEntityAccessor.setBeaconEffects(List.copyOf(immutableEffects));

    // Update VALID_EFFECTS
    BeaconBlockEntityAccessor.setValidEffects(
        effects.stream().flatMap(Collection::stream).collect(Collectors.toSet())
    );
  }

  @Override
  public void onInitialize() {
    addMobEffectsToBeacon();
  }
}
