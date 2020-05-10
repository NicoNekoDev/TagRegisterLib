package ro.nicuch.tag.events;

import ro.nicuch.tag.nbt.CompoundTag;
import ro.nicuch.tag.register.RegionRegister;

public class RegionTagLoadEvent extends TagLoadEvent {
    private final RegionRegister regionRegister;

    public RegionTagLoadEvent(RegionRegister regionRegister, CompoundTag tag) {
        super(tag);
        this.regionRegister = regionRegister;
    }

    public RegionRegister getRegionRegister() {
        return this.regionRegister;
    }
}
