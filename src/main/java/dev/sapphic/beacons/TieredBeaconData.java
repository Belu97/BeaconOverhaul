package dev.sapphic.beacons;

import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.level.block.entity.BeaconBlockEntity;

public final class TieredBeaconData implements ContainerData {
  private final BeaconBlockEntity beacon;
  private final ContainerData delegate;

  public TieredBeaconData(final BeaconBlockEntity beacon, final ContainerData delegate) {
    this.beacon = beacon;
    this.delegate = delegate;
  }

  @Override
  public int get(final int index) {
    if (index == 3) {
      return ((TieredBeacon) this.beacon).getTier().ordinal();
    }
    return this.delegate.get(index);
  }

  @Override
  public void set(final int index, final int value) {
    if (index == 3) {
      ((MutableTieredBeacon) this.beacon).setTier(PotencyTier.values()[value]);
      return;
    }
    this.delegate.set(index, value);
  }

  @Override
  public int getCount() {
    return 4;
  }
}
