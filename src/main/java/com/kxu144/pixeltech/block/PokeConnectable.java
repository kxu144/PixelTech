package com.kxu144.pixeltech.block;

import com.pixelmonmod.pixelmon.entities.pixelmon.PixelmonEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;

public interface PokeConnectable {

    public boolean isFull();

    public void pushPoke();

    public boolean canPull();

    public void pullPoke(CompoundNBT nbt, Direction dir);
}
