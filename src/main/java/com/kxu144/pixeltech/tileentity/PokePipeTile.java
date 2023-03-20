package com.kxu144.pixeltech.tileentity;

import com.kxu144.pixeltech.block.PokeConnectable;
import com.kxu144.pixeltech.block.PokePipeBlock;
import com.pixelmonmod.pixelmon.entities.pixelmon.PixelmonEntity;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.Constants;

public class PokePipeTile extends TileEntity implements ITickableTileEntity, PokeConnectable {

    public int tickSpeed;
    public Direction prevDir;
    public CompoundNBT poke;

    public int tick;

    public PokePipeTile(TileEntityType<?> tileEntityType) {
        super(tileEntityType);
        this.tickSpeed = 10;
        this.tick = 0;
    }

    public PokePipeTile() {
        this(ModTileEntities.POKEPIPE_TILE.get());
    }

    @Override
    public void load(BlockState state, CompoundNBT nbt) {
        this.tickSpeed = nbt.getInt("tickSpeed");
        loadPokemon(nbt);
        super.load(state, nbt);
    }

    private void loadPokemon(CompoundNBT nbt) {
        if (nbt.size() > 0) {
            String dir = nbt.getString("dir");
            if (!dir.equals("")) {
                this.prevDir = Direction.byName(dir);
            } else {
                this.prevDir = null;
            }
            CompoundNBT poke = nbt.getCompound("poke");
            if (!poke.isEmpty()) {
                this.poke = poke;
            } else {
                this.poke = null;
            }
        }
    }

    @Override
    public CompoundNBT save(CompoundNBT nbt) {
        nbt.putInt("tickSpeed", this.tickSpeed);
        savePokemon(nbt);
        return super.save(nbt);
    }

    private void savePokemon(CompoundNBT nbt) {
        if (this.prevDir != null) {
            nbt.putString("dir", this.prevDir.getName());
        }
        if (this.poke != null) {
            nbt.put("poke", this.poke);
        }
    }

    @Override
    public void tick() {
        if (!this.level.isClientSide) {
            if (this.tick >= this.tickSpeed) {
                this.tick = 0;
                pushPoke();
            }
            ++this.tick;
        }
    }

    @Override
    public boolean isFull() {
        return this.poke != null;
    }

    @Override
    public void pushPoke() {
        if (isFull()) {
            for (Direction dir : Direction.values()) {
                if ((this.prevDir != null && dir == this.prevDir)
                        || !this.getBlockState().getValue(PokePipeBlock.PROPERTY_BY_DIRECTION.get(dir))) {
                    continue;
                }
                BlockPos pos = this.getBlockPos().relative(dir);
                TileEntity entity = this.level.getBlockEntity(pos);
                PokeConnectable pc = (entity instanceof PokeConnectable ? (PokeConnectable)entity:null);
                if (pc == null || pc.isFull() || !pc.canPull()) {
                    continue;
                }
                pc.pullPoke(this.poke, dir.getOpposite());
                resetPoke();
                break;
            }
        }
    }

    private void resetPoke() {
        this.poke = null;
        this.prevDir = null;
        this.level.setBlock(this.getBlockPos(),
                this.getBlockState().setValue(PokePipeBlock.POKE, false),
                Constants.BlockFlags.DEFAULT);
    }

    @Override
    public boolean canPull() {
        return !isFull();
    }

    @Override
    public void pullPoke(CompoundNBT nbt, Direction dir) {
        if (this.poke != null) {
            throw new IllegalStateException("Pipe is full.");
        }
        this.poke = nbt;
        this.prevDir = dir;
        this.level.setBlock(this.getBlockPos(),
                this.getBlockState().setValue(PokePipeBlock.POKE, true),
                Constants.BlockFlags.DEFAULT);
    }
}
