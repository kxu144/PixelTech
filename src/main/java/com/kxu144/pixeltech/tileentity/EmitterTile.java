package com.kxu144.pixeltech.tileentity;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.pixelmonmod.api.pokemon.PokemonSpecification;
import com.pixelmonmod.pixelmon.api.config.PixelmonConfigProxy;
import com.pixelmonmod.pixelmon.api.events.spawning.PixelmonSpawnerEvent;
import com.pixelmonmod.pixelmon.api.pokemon.Element;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.api.pokemon.PokemonBuilder;
import com.pixelmonmod.pixelmon.api.pokemon.PokemonFactory;
import com.pixelmonmod.pixelmon.api.pokemon.species.Species;
import com.pixelmonmod.pixelmon.api.registries.PixelmonSpecies;
import com.pixelmonmod.pixelmon.api.util.helpers.RandomHelper;
import com.pixelmonmod.pixelmon.blocks.machines.PokemonRarity;
import com.pixelmonmod.pixelmon.blocks.tileentity.PixelmonSpawnerTileEntity;
import com.pixelmonmod.pixelmon.entities.SpawnLocationType;
import com.pixelmonmod.pixelmon.entities.pixelmon.PixelmonEntity;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.IItemHandler;
import com.pixelmonmod.pixelmon.items.LureItem;
import net.minecraftforge.items.wrapper.SidedInvWrapper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class EmitterTile extends TileEntity implements ITickableTileEntity {

    public static final Set<Material> VALID_LAND_FLOOR_MATERIALS;
    public static final Set<Material> VALID_LAND_AIR_MATERIALS;
    private static final Set<Material> VALID_WATER_FLOOR_MATERIALS;
    private static final Set<Material> VALID_WATER_AIR_MATERIALS;

    private final ItemStackHandler itemHandler = createHandler();
    private final LazyOptional<IItemHandler> handler = LazyOptional.of(() -> itemHandler);
    private final LazyOptional<IItemHandler> inputHandler = LazyOptional.of(this::createHandler);

    public int spawnTick = 200;
    public int spawnRadius = 8;
    public int maxSpawns = 5;
    public int levelMin = 1;
    public int levelMax = 1;
    public boolean spawnLegend = false;
    public boolean spawnMythic = false;
    public boolean spawnUltra = false;
    public float shinyRatio = PixelmonConfigProxy.getSpawning().getShinyRate();
    public SpawnLocationType spawnLocation;
    public List<PixelmonEntity> spawnedPokemon;

    private int tick;

    public EmitterTile(TileEntityType<?> tileEntityTypeIn) {
        super(tileEntityTypeIn);
        this.spawnLocation = SpawnLocationType.LAND;
        this.spawnedPokemon = Lists.newArrayList();
        this.tick = -1;
    }

    public EmitterTile() {
        this(ModTileEntities.EMITTER_TILE.get());
    }

    @Override
    public void load(BlockState state, CompoundNBT nbt) {
        itemHandler.deserializeNBT(nbt.getCompound("inv"));
        this.spawnTick = nbt.getInt("spawnTick");
        this.spawnRadius = nbt.getShort("spawnRadius");
        this.maxSpawns = nbt.getShort("maxSpawns");
        this.levelMin = nbt.getShort("levelMin");
        this.levelMax = nbt.getShort("levelMax");
        this.spawnLegend = nbt.getBoolean("spawnLegend");
        this.spawnMythic = nbt.getBoolean("spawnMythic");
        this.spawnUltra = nbt.getBoolean("spawnUltra");
        this.spawnLocation = SpawnLocationType.getFromIndex(nbt.getShort("spawnLocation"));
        this.shinyRatio = nbt.getFloat("shinyRatio");
        super.load(state, nbt);
    }

    @Override
    public CompoundNBT save(CompoundNBT nbt) {
        nbt.put("inv", itemHandler.serializeNBT());
        nbt.putInt("spawnTick", this.spawnTick);
        nbt.putShort("spawnRadius", (short)this.spawnRadius);
        nbt.putShort("maxSpawns", (short)this.maxSpawns);
        nbt.putShort("levelMin", (short)this.levelMin);
        nbt.putShort("levelMax", (short)this.levelMax);
        nbt.putBoolean("spawnLegend", this.spawnLegend);
        nbt.putBoolean("spawnMythic", this.spawnMythic);
        nbt.putBoolean("spawnUltra", this.spawnUltra);
        nbt.putShort("spawnLocation", (short)this.spawnLocation.ordinal());
        nbt.putFloat("shinyRatio", this.shinyRatio);
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
                return stack.getItem() instanceof LureItem;
            }

            @Override
            public int getSlotLimit(int slot) {
                return 1;
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
        //System.out.println(this.tick);
        ItemStack itemStack = this.itemHandler.getStackInSlot(0);
        if (!this.level.isClientSide) {
            this.doSpawning(false);
            this.updateStuff(itemStack);
        } else if (!itemStack.isEmpty() && this.tick < 20) {
            this.doEffects();
        }
    }

    private void doSpawning(boolean override) {
        if (this.tick == 0 || override) {
            Pokemon p = this.selectPokemonForSpawn();
            if (p == null) {
                this.doFailedEffects();
                return;
            }
            //System.out.println("Found pokemon: " + p.toString());
            boolean checkLegend = !this.spawnLegend && p.isLegendary();
            boolean checkMythic = !this.spawnMythic && p.isMythical();
            boolean checkUltra = !this.spawnUltra && p.isUltraBeast();
            if (checkLegend || checkMythic || checkUltra) {
                this.doFailedEffects();
                return;
            }

            this.spawnPixelmon(p);
            //System.out.println("Spawned " + p.toString());
            this.resetSpawnTick();
            if (override) {
                return;
            }
        }

        if (this.tick == -1) {
            this.resetSpawnTick();
        }
    }

    private void doEffects() {
        if (this.isNearPlayer()) {
            World world = this.getLevel();
            BlockPos blockpos = this.getBlockPos();
            if (!(world instanceof ServerWorld)) {
                double d3 = (double)blockpos.getX() + world.random.nextDouble();
                double d4 = (double)blockpos.getY() + world.random.nextDouble();
                double d5 = (double)blockpos.getZ() + world.random.nextDouble();
                world.addParticle(ParticleTypes.FLAME, d3, d4, d5, 0.0D, 0.0D, 0.0D);
            }
        }
    }

    private void doFailedEffects() {
        if (this.isNearPlayer()) {
            World world = this.getLevel();
            BlockPos blockpos = this.getBlockPos();
            if (!(world instanceof ServerWorld)) {
                double d3 = (double)blockpos.getX() + world.random.nextDouble();
                double d4 = (double)blockpos.getY() + world.random.nextDouble();
                double d5 = (double)blockpos.getZ() + world.random.nextDouble();
                world.addParticle(ParticleTypes.CAMPFIRE_COSY_SMOKE, d3, d4, d5, 0.0D, 0.0D, 0.0D);
            }
        }
    }

    private boolean isNearPlayer() {
        BlockPos blockpos = this.getBlockPos();
        return this.getLevel().hasNearbyAlivePlayer((double)blockpos.getX() + 0.5D, (double)blockpos.getY() + 0.5D, (double)blockpos.getZ() + 0.5D, 16.0D);
    }

    private Pokemon selectPokemonForSpawn() {
        ItemStack itemStack = this.itemHandler.getStackInSlot(0);
        if (!isValidItem()) {
            //System.out.println("Not valid item");
            return null;
        }
        Optional<Species> optional = PixelmonSpecies.getRandomFromType(((LureItem) itemStack.getItem()).type.type);
        return optional.map(PokemonFactory::create).orElse(null);
    }

    private boolean isValidItem() {
        ItemStack itemStack = this.itemHandler.getStackInSlot(0);
        if (itemStack.isEmpty() || !(itemStack.getItem() instanceof LureItem)) {
            return false;
        }
        return true;
    }

    private void updateStuff(ItemStack itemStack) {
        if (!isValidItem()) {
            return;
        }
        --this.tick;
        if (this.tick % 10 == 0) {
            itemStack.setDamageValue(itemStack.getDamageValue() + 1);
        }
        if (itemStack.getDamageValue() >= itemStack.getMaxDamage()) {
            itemStack.shrink(1);
            resetSpawnTick();
        }
    }

    private void spawnPixelmon(Pokemon p) {
        this.checkForDead();
        if (this.spawnedPokemon.size() < this.maxSpawns) {
            if (this.spawnLocation != null) {
                int x = this.worldPosition.getX() + this.level.random.nextInt(this.spawnRadius * 2 + 1) - this.spawnRadius;
                int y = this.worldPosition.getY() - 1;
                int z = this.worldPosition.getZ() + this.level.random.nextInt(this.spawnRadius * 2 + 1) - this.spawnRadius;
                boolean valid = false;
                if (this.spawnLocation == SpawnLocationType.LAND) {
                    y = this.getTopSolidBlock(x, y, z);
                    valid = isBlockValidForPixelmonSpawning(this.level, new BlockPos(x, y, z), PixelmonSpawnerTileEntity.AreaType.LAND);
                } else if (this.spawnLocation == SpawnLocationType.AIR) {
                    y = this.getTopSolidBlock(x, y, z);
                    valid = isBlockValidForPixelmonSpawning(this.level, new BlockPos(x, y, z), PixelmonSpawnerTileEntity.AreaType.LAND);
                } else {
                    Integer ytmp;
                    if (this.spawnLocation == SpawnLocationType.AIR_PERSISTENT) {
                        ytmp = this.getFirstAirBlock(x, y, z);
                        if (ytmp != null) {
                            y = ytmp;
                            valid = true;
                        }
                    } else if (this.spawnLocation == SpawnLocationType.WATER) {
                        ytmp = this.getFirstWaterBlock(x, y, z);
                        if (ytmp != null) {
                            y = ytmp;
                            valid = isBlockValidForPixelmonSpawning(this.level, new BlockPos(x, y + 1, z), PixelmonSpawnerTileEntity.AreaType.WATER);
                        }
                    } else if (this.spawnLocation == SpawnLocationType.UNDERGROUND) {
                        y = this.getTopSolidBlock(x, y, z);
                        valid = isBlockValidForPixelmonSpawning(this.level, new BlockPos(x, y, z), PixelmonSpawnerTileEntity.AreaType.UNDERGROUND);
                    }
                }

                if (valid) {
                    p.setLevel(RandomHelper.getRandomNumberBetween(this.levelMin, this.levelMax));
                    if (RandomHelper.getRandomChance(1.0F / this.shinyRatio)) {
                        p.setShiny();
                    }

                    PixelmonEntity entity = p.getOrCreatePixelmon(this.level, x + 0.5D, y, z + 0.5D);
                    entity.setSpawnLocation(this.spawnLocation);
                    this.level.addFreshEntity(entity);
                    this.spawnedPokemon.add(entity);
                }
            }
        }
    }

    protected static boolean isMostlyEnclosedSpace(World world, BlockPos pos, int radius) {
        Direction[] var3 = Direction.values();
        int var4 = var3.length;

        for(int var5 = 0; var5 < var4; ++var5) {
            Direction dir = var3[var5];
            boolean ok = false;

            for(int i = 0; i < radius; ++i) {
                BlockPos pos2 = new BlockPos(pos.getX() + dir.getStepX() * i, pos.getY() + dir.getStepY() * i, pos.getZ() + dir.getStepZ() * i);
                BlockState state = world.getBlockState(pos2);
                if (state.isRedstoneConductor(world, pos2)) {
                    ok = true;
                    break;
                }
            }

            if (!ok) {
                return false;
            }
        }

        return true;
    }

    public static boolean isBlockValidForPixelmonSpawning(World world, BlockPos pos, PixelmonSpawnerTileEntity.AreaType type) {
        BlockState groundBlock = world.getBlockState(pos.below());
        Material spawnFloorGroundMaterial = groundBlock.getMaterial();
        Material[] spawnAirMaterial = new Material[]{world.getBlockState(pos).getMaterial(), world.getBlockState(pos.above()).getMaterial()};
        if (type == PixelmonSpawnerTileEntity.AreaType.LAND) {
            return VALID_LAND_FLOOR_MATERIALS.contains(spawnFloorGroundMaterial) && VALID_LAND_AIR_MATERIALS.contains(spawnAirMaterial[0]) && VALID_LAND_AIR_MATERIALS.contains(spawnAirMaterial[1]);
        } else if (type == PixelmonSpawnerTileEntity.AreaType.WATER) {
            return VALID_WATER_FLOOR_MATERIALS.contains(spawnFloorGroundMaterial) && VALID_WATER_AIR_MATERIALS.contains(spawnAirMaterial[0]) && VALID_WATER_AIR_MATERIALS.contains(spawnAirMaterial[1]);
        } else {
            return isMostlyEnclosedSpace(world, pos, 5) && VALID_LAND_FLOOR_MATERIALS.contains(spawnFloorGroundMaterial) && VALID_LAND_AIR_MATERIALS.contains(spawnAirMaterial[0]) && VALID_LAND_AIR_MATERIALS.contains(spawnAirMaterial[1]);
        }
    }

    private void checkForDead() {
        for(int i = 0; i < this.spawnedPokemon.size(); ++i) {
            PixelmonEntity p = (PixelmonEntity)this.spawnedPokemon.get(i);
            if (!p.isLoaded(false) || !p.isAlive()) {
                this.spawnedPokemon.remove(i);
                --i;
            }
        }
    }

    private void resetSpawnTick() {
        this.tick = (int)((double)this.spawnTick * (1.0D + (this.level.random.nextDouble() - 0.5D) * 0.2D));
        //System.out.println("Tick reset to " + this.tick);
    }

    private int getTopSolidBlock(int x, int y, int z) {
        boolean valid = false;

        int i;
        BlockPos pos;
        Material blockMaterial;
        for(i = 1; i <= this.spawnRadius / 2; ++i) {
            pos = new BlockPos(x, y + i, z);
            blockMaterial = this.level.getBlockState(pos).getMaterial();
            if (VALID_LAND_AIR_MATERIALS.contains(blockMaterial) && this.isSolidSurface(this.level, pos)) {
                y += i;
                valid = true;
                break;
            }
        }

        if (!valid) {
            for(i = 1; i <= this.spawnRadius / 2; ++i) {
                pos = new BlockPos(x, y - i, z);
                blockMaterial = this.level.getBlockState(pos).getMaterial();
                if (VALID_LAND_AIR_MATERIALS.contains(blockMaterial) && this.isSolidSurface(this.level, pos)) {
                    y -= i;
                    break;
                }
            }
        }

        return y;
    }

    private boolean isSolidSurface(World worldIn, BlockPos pos) {
        return worldIn.getBlockState(pos.below()).isFaceSturdy(worldIn, pos, Direction.UP) && !worldIn.getBlockState(pos).getMaterial().isSolid() && !worldIn.getBlockState(pos.above()).getMaterial().isSolid();
    }

    private Integer getFirstAirBlock(int x, int y, int z) {
        int i;
        for(i = 0; this.level.getBlockState(new BlockPos(x, y + i, z)).getMaterial() != Material.AIR; ++i) {
            if (i > this.spawnRadius / 2) {
                return null;
            }
        }

        return y + i;
    }

    private Integer getFirstWaterBlock(int x, int y, int z) {
        int i;
        for(i = 0; this.level.getBlockState(new BlockPos(x, y + i, z)).getMaterial() != Material.WATER; ++i) {
            if (this.level.getBlockState(new BlockPos(x, y + i, z)).getMaterial() == Material.AIR) {
                return null;
            }
        }

        return y + i;
    }

    static {
        VALID_LAND_FLOOR_MATERIALS = Sets.newHashSet(new Material[]{Material.GRASS, Material.CLOTH_DECORATION, Material.WOOL, Material.GLASS, Material.WOOD, Material.DIRT, Material.STONE, Material.SAND, Material.ICE, Material.TOP_SNOW, Material.SNOW, Material.ICE_SOLID});
        VALID_LAND_AIR_MATERIALS = Sets.newHashSet(new Material[]{Material.AIR, Material.TOP_SNOW, Material.PLANT, Material.REPLACEABLE_PLANT});
        VALID_WATER_FLOOR_MATERIALS = Sets.newHashSet(new Material[]{Material.WATER});
        VALID_WATER_AIR_MATERIALS = Sets.newHashSet(new Material[]{Material.AIR, Material.WATER});
    }

    public static enum AreaType {
        LAND,
        WATER,
        UNDERGROUND;

        private AreaType() {
        }
    }
}
