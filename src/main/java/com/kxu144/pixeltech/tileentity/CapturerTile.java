package com.kxu144.pixeltech.tileentity;

import com.google.common.collect.Lists;
import com.kxu144.pixeltech.PixelTech;
import com.kxu144.pixeltech.block.PokeConnectable;
import com.kxu144.pixeltech.block.PokePipeBlock;
import com.kxu144.pixeltech.entity.PokePipePixelmonEntity;
import com.pixelmonmod.pixelmon.api.events.CaptureEvent;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.api.pokemon.species.aggression.Aggression;
import com.pixelmonmod.pixelmon.blocks.tileentity.PixelmonSpawnerTileEntity;
import com.pixelmonmod.pixelmon.entities.pixelmon.AbstractBattleEntity;
import com.pixelmonmod.pixelmon.entities.pixelmon.PixelmonEntity;
import com.pixelmonmod.pixelmon.entities.pokeballs.PokeBallEntity;
import com.pixelmonmod.pixelmon.items.LureItem;
import com.pixelmonmod.pixelmon.items.PokeBallItem;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;


public class CapturerTile extends TileEntity implements ITickableTileEntity, PokeConnectable {

    private final ItemStackHandler itemHandler = createHandler();
    private final LazyOptional<IItemHandler> handler = LazyOptional.of(() -> itemHandler);
    private final LazyOptional<IItemHandler> inputHandler = LazyOptional.of(this::createHandler);

    public int radius;
    public int maxSize;
    public int coolDown;
    public AxisAlignedBB aoe;
    public ListNBT capturedPokemon;
    public int tick;
    public boolean canCaptureLegend = false;
    public boolean canCaptureMythic = false;
    public boolean canCaptureUltra = false;


    public CapturerTile(TileEntityType<?> tileEntityType) {
        super(tileEntityType);
        this.radius = 5;
        this.maxSize = 5;
        this.coolDown = 20;
        this.capturedPokemon = new ListNBT();
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
        this.capturedPokemon = nbt.getList("capturedPokemon", Constants.NBT.TAG_COMPOUND);
        super.load(state, nbt);
    }

    @Override
    public CompoundNBT save(CompoundNBT nbt) {
        nbt.putShort("radius", (short)this.radius);
        nbt.putShort("maxSize", (short)this.maxSize);
        nbt.putInt("coolDown", this.coolDown);
        nbt.put("capturedPokemon", this.capturedPokemon);
        return super.save(nbt);
    }



    private ItemStackHandler createHandler() {
        return new ItemStackHandler(1) {
            @Override
            protected void onContentsChanged(int slot) {
                setChanged();
            }

            @Override
            public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
                return stack.getItem() instanceof PokeBallItem;
            }

            @Override
            public int getSlotLimit(int slot) {
                return 64;
            }

            @Nonnull
            @Override
            public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
                if (!isItemValid(slot, stack)) {
                    return stack;
                }

                return super.insertItem(slot, stack, simulate);
            }
        };
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if (cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            if (side == null) {
                return handler.cast();
            } else {
                return inputHandler.cast();
            }
        }

        return super.getCapability(cap);
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
                    ItemStack ball = this.itemHandler.getStackInSlot(0);
                    if (!ball.isEmpty()) {
                        ball.shrink(1);
                        capturePokemon(entity);
                        this.tick = this.coolDown;
                    }
                }
            }
            if (this.tick <= 5) {
                pushPoke();
            }
            if (this.tick > 0) {
                --this.tick;
            }
        }
    }

    private void capturePokemon(PixelmonEntity entity) {
        entity.setFlyHeight(PixelTech.FLY_HEIGHt);
        this.capturedPokemon.add(entity.serializeNBT());
        entity.remove();
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

    public boolean isValidPixelmon(PixelmonEntity entity) {
        if (entity.hasOwner()) {
            return false;
        }
        Pokemon p = entity.getPokemon();
        boolean checkLegend = !this.canCaptureLegend && p.isLegendary();
        boolean checkMythic = !this.canCaptureMythic && p.isMythical();
        boolean checkUltra = !this.canCaptureUltra && p.isUltraBeast();
        if (checkLegend || checkMythic || checkUltra) {
            return false;
        }
        return !(entity instanceof PokePipePixelmonEntity);
    }

    @Override
    public boolean isFull() {
        return this.capturedPokemon.size() == this.maxSize;
    }

    @Override
    public void pushPoke() {
        if (this.capturedPokemon.size() > 0) {
            for (Direction dir : Direction.values()) {
                BlockPos pos = this.getBlockPos().relative(dir);
                TileEntity entity = this.level.getBlockEntity(pos);
                PokePipeTile pipeTile = (entity instanceof PokePipeTile ? (PokePipeTile)entity:null);
                if (pipeTile == null || pipeTile.isFull()
                        || !pipeTile.getBlockState().getValue(PokePipeBlock.PROPERTY_BY_DIRECTION.get(dir.getOpposite()))) {
                    continue;
                }
                pipeTile.pullPoke((CompoundNBT) this.capturedPokemon.remove(0), dir.getOpposite());
                break;
            }
        }
    }

    @Override
    public boolean canPull() {
        return false;
    }

    @Override
    public void pullPoke(CompoundNBT nbt, Direction dir) {
        throw new IllegalStateException("Cannot pull into Capturer.");
    }
}
