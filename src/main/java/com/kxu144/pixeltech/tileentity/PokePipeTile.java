package com.kxu144.pixeltech.tileentity;

import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;

public class PokePipeTile extends TileEntity implements ITickableTileEntity {

    public PokePipeTile(TileEntityType<?> tileEntityType) {
        super(tileEntityType);
    }

    public PokePipeTile() {
        this(ModTileEntities.POKEPIPE_TILE.get());
    }

    @Override
    public void tick() {

    }
}
