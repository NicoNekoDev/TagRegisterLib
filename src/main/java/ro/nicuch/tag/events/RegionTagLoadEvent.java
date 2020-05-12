package ro.nicuch.tag.events;

import ro.nicuch.tag.nbt.reg.RegionCompoundTag;
import ro.nicuch.tag.register.RegionRegister;

public class RegionTagLoadEvent extends TagLoadEvent<RegionCompoundTag> {
    private final RegionRegister regionRegister;

    public RegionTagLoadEvent(RegionRegister regionRegister, RegionCompoundTag tag) {
        super(tag);
        this.regionRegister = regionRegister;
    }

    public RegionRegister getRegionRegister() {
        return this.regionRegister;
    }
}
