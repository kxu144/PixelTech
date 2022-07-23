package com.kxu144.pixeltech.tileentity;

import com.google.common.collect.Lists;
import com.pixelmonmod.pixelmon.blocks.tileentity.PixelmonSpawnerTileEntity;
import com.pixelmonmod.pixelmon.entities.pixelmon.PixelmonEntity;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;

import java.util.List;


public class CapturerTile extends TileEntity implements ITickableTileEntity {

    private final ItemStackHandler itemHandler = createHandler();
    private final LazyOptional<IItemHandler> handler = LazyOptional.of(() -> itemHandler);

    public int radius;
    public int maxSize;
    public int coolDown;
    public AxisAlignedBB aoe;
    public List<PixelmonEntity> capturedPokemon;

    public int tick;

    public CapturerTile(TileEntityType<?> tileEntityType) {
        super(tileEntityType);
        this.radius = 5;
        this.maxSize = 5;
        this.coolDown = 20;
        this.capturedPokemon = Lists.newArrayList();
        this.tick = 0;
    }

    public CapturerTile() {
        this(ModTileEntities.CAPTURER_TILE.get());
    }

    @Override
    public void load(BlockState state, CompoundNBT nbt) {
        this.radius = nbt.getShort("radius");
        this.maxSize = nbt.getShort("maxSize");
        this.coolDown = nbt.getInt("coolDown");
        loadPokemon(nbt);
        super.load(state, nbt);
    }

    private void loadPokemon(CompoundNBT nbt) {
        this.capturedPokemon = Lists.newArrayList();
        ListNBT listNBT = nbt.getList("capturedPokemon", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < listNBT.size(); i++) {
            this.capturedPokemon.add(new PixelmonEntity(this.level, listNBT.getCompound(i)));
        }
    }

    @Override
    public CompoundNBT save(CompoundNBT nbt) {
        nbt.putShort("radius", (short)this.radius);
        nbt.putShort("maxSize", (short)this.maxSize);
        nbt.putInt("coolDown", this.coolDown);
        savePokemon(nbt);
        return super.save(nbt);
    }

    private void savePokemon(CompoundNBT nbt) {
        ListNBT listNBT = new ListNBT();
        for (PixelmonEntity pe : this.capturedPokemon) {
            try {
                listNBT.add(pe.serializeNBT());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        nbt.put("capturedPokemon", listNBT);
    }



    private ItemStackHandler createHandler() {
        return new ItemStackHandler();
    }



    @Override
    public void tick() {
        if (!level.isClientSide) {
            if (this.aoe == null) {
                updateAOE();
            }
            if (this.tick <= 0 && this.capturedPokemon.size() < this.maxSize) {
                PixelmonEntity entity = findTarget();
                if (entity != null) {
                    //System.out.println("Disposed of " + entity.getPokemonName());
                    this.capturedPokemon.add(entity);
                    entity.remove();

                    this.tick = this.coolDown;
                }
            }
            if (this.tick > 0) {
                --this.tick;
            }
        }
    }

    private void updateAOE() {
        BlockPos pos = this.getBlockPos();
        BlockPos pos1 = pos.offset(-this.radius, -this.radius, -this.radius);
        BlockPos pos2 = pos.offset(this.radius, this.radius, this.radius);
        this.aoe = new AxisAlignedBB(pos1, pos2);
    }

    private PixelmonEntity findTarget() {
        List<PixelmonEntity> entities = level.getEntitiesOfClass(PixelmonEntity.class, this.aoe);
        while (!entities.isEmpty()) {
            PixelmonEntity entity = entities.remove(level.random.nextInt(entities.size()));
            //System.out.println("Found " + entity.getPokemonName());
            if (isValidPixelmon(entity)) {
                //System.out.println(entity.getPokemonName() + " is valid!");
                return entity;
            }
        }
        return null;
    }

    private boolean isValidPixelmon(PixelmonEntity entity) {
        return !entity.hasOwner();
    }


}
