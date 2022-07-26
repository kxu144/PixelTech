package com.kxu144.pixeltech.block;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.kxu144.pixeltech.tileentity.ModTileEntities;
import com.kxu144.pixeltech.tileentity.PokePipeTile;
import com.pixelmonmod.pixelmon.blocks.machines.PCBlock;
import com.pixelmonmod.pixelmon.blocks.tileentity.PCTileEntity;
import com.pixelmonmod.pixelmon.entities.pixelmon.PixelmonEntity;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.RedstoneWireBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.RedstoneSide;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PokePipeBlock extends Block {

    public static final BooleanProperty NORTH = BooleanProperty.create("north");
    public static final BooleanProperty SOUTH = BooleanProperty.create("south");
    public static final BooleanProperty WEST = BooleanProperty.create("west");
    public static final BooleanProperty EAST = BooleanProperty.create("east");
    public static final BooleanProperty UP = BooleanProperty.create("up");
    public static final BooleanProperty DOWN = BooleanProperty.create("down");
    public static final BooleanProperty POKE = BooleanProperty.create("poke");
    public static final Map<Direction, BooleanProperty> PROPERTY_BY_DIRECTION = ImmutableMap.<Direction, BooleanProperty>builder()
            .put(Direction.NORTH, NORTH)
            .put(Direction.EAST, EAST)
            .put(Direction.SOUTH, SOUTH)
            .put(Direction.WEST, WEST)
            .put(Direction.UP, UP)
            .put(Direction.DOWN, DOWN)
            .build();
    private static final Map<Direction, VoxelShape> SHAPES_FLOOR = ImmutableMap.<Direction, VoxelShape>builder()
            .put(Direction.NORTH, Block.box(2, 2, 0, 14, 14, 14))
            .put(Direction.SOUTH, Block.box(2, 2, 2, 14, 14, 16))
            .put(Direction.EAST, Block.box(2, 2, 2, 16, 14, 14))
            .put(Direction.WEST, Block.box(0, 2, 2, 14, 14, 14))
            .put(Direction.UP, Block.box(2, 2, 2, 14, 16, 14))
            .put(Direction.DOWN, Block.box(2, 0, 2, 14, 14, 14))
            .build();
    private final Map<BlockState, VoxelShape> SHAPES_CACHE = Maps.newHashMap();



    public static final List<Class<?>> EXTRA_CONNECTED_BLOCKS = new ArrayList<Class<?>>() {
        {
            add(PCTileEntity.class);
        }
    };


    public PokePipeBlock(AbstractBlock.Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(NORTH, false)
                .setValue(EAST, false)
                .setValue(SOUTH, false)
                .setValue(WEST, false)
                .setValue(UP, false)
                .setValue(DOWN, false)
                .setValue(POKE, false));

        for (BlockState state : this.getStateDefinition().getPossibleStates()) {
            if (!state.getValue(POKE)) {
                this.SHAPES_CACHE.put(state, this.calculateShape(state));
            }
        }
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return ModTileEntities.POKEPIPE_TILE.get().create();
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader world, BlockPos pos, ISelectionContext context) {
        return this.SHAPES_CACHE.get(state.setValue(POKE, false));
    }

    private VoxelShape calculateShape(BlockState state) {
        VoxelShape voxelShape = Block.box(2, 2, 2, 14, 14, 14);
        for (Direction dir : PROPERTY_BY_DIRECTION.keySet()) {
            if (state.getValue(PROPERTY_BY_DIRECTION.get(dir))) {
                voxelShape = VoxelShapes.or(voxelShape, SHAPES_FLOOR.get(dir));
            }
        }
        return voxelShape;
    }

    @Override
    public BlockState updateShape(BlockState state, Direction dir, BlockState otherState, IWorld world, BlockPos pos, BlockPos otherPos) {
        return getConnectionState((World) world, state, pos);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        return getConnectionState(context.getLevel(), this.defaultBlockState(), context.getClickedPos());
    }

    private BlockState getConnectionState(World world, BlockState state, BlockPos pos) {
        for (Direction dir : Direction.values()) {
            if (canConnectTo(world, pos.relative(dir))) {
                state = state.setValue(PROPERTY_BY_DIRECTION.get(dir), true);
            }  else {
                state = state.setValue(PROPERTY_BY_DIRECTION.get(dir), false);
            }
        }
        return state;
    }

    protected boolean canConnectTo(World world, BlockPos pos) {
        TileEntity entity = world.getBlockEntity(pos);
        if (entity instanceof PokeConnectable) {
            return true;
        }
        for (Class<?> c : EXTRA_CONNECTED_BLOCKS) {
            if (c.isInstance(entity)) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder.add(NORTH).add(EAST).add(SOUTH).add(WEST).add(UP).add(DOWN).add(POKE));
    }

    @Override
    public ActionResultType use(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult hit) {
        if (!world.isClientSide()) {
            for (Direction dir : Direction.values()) {
                System.out.println(dir.toString() + state.getValue(PROPERTY_BY_DIRECTION.get(dir)));
            }
            System.out.println("POKE:" + state.getValue(POKE));
            try {
                System.out.println(((PokePipeTile) world.getBlockEntity(pos)).poke != null);
                System.out.println("contains " + new PixelmonEntity(world, ((PokePipeTile) world.getBlockEntity(pos)).poke).getPokemonName());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return ActionResultType.SUCCESS;
    }

    @Override
    public void onRemove(BlockState state, World world, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!isMoving && !state.is(newState.getBlock())) {
            super.onRemove(state, world, pos, newState, false);
            if (!world.isClientSide) {
                for (Direction dir : Direction.values()) {
                    world.updateNeighborsAt(pos.relative(dir), this);
                }
            }
        }
    }

    @Override
    public void neighborChanged(BlockState state, World world, BlockPos pos, Block block, BlockPos neighbor, boolean isMoving) {
        if (!world.isClientSide) {
            world.setBlock(pos, getConnectionState(world, state, pos), Constants.BlockFlags.DEFAULT);
        }
    }
}
