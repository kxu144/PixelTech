package com.kxu144.pixeltech.block;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.kxu144.pixeltech.tileentity.ModTileEntities;
import com.pixelmonmod.pixelmon.blocks.machines.PCBlock;
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
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PokePipeBlock extends Block implements PokeConnectable {

    public static final BooleanProperty NORTH = BooleanProperty.create("north");
    public static final BooleanProperty SOUTH = BooleanProperty.create("south");
    public static final BooleanProperty WEST = BooleanProperty.create("west");
    public static final BooleanProperty EAST = BooleanProperty.create("east");
    public static final BooleanProperty UP = BooleanProperty.create("up");
    public static final BooleanProperty DOWN = BooleanProperty.create("down");
    public static final Map<Direction, BooleanProperty> PROPERTY_BY_DIRECTION = ImmutableMap.<Direction, BooleanProperty>builder()
            .put(Direction.NORTH, NORTH)
            .put(Direction.EAST, EAST)
            .put(Direction.SOUTH, SOUTH)
            .put(Direction.WEST, WEST)
            .put(Direction.UP, UP)
            .put(Direction.DOWN, DOWN)
            .build();

    public static final List<Class<?>> EXTRA_CONNECTED_BLOCKS = new ArrayList<Class<?>>() {
        {
            add(PCBlock.class);
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
                .setValue(DOWN, false));
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

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        return getConnectionState(context.getLevel(), this.defaultBlockState(), context.getClickedPos());
    }

    private BlockState getConnectionState(World world, BlockState state, BlockPos pos) {
        for (Direction dir : PROPERTY_BY_DIRECTION.keySet()) {
            if (canConnectTo(world, pos.relative(dir))) {
                state = state.setValue(PROPERTY_BY_DIRECTION.get(dir), true);
            }
        }
        return state;
    }

    protected boolean canConnectTo(World world, BlockPos pos) {
        Block block = world.getBlockState(pos).getBlock();
        if (block instanceof PokeConnectable) {
            return true;
        }
        for (Class<?> c : EXTRA_CONNECTED_BLOCKS) {
            if (c.isInstance(block)) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder.add(NORTH).add(EAST).add(SOUTH).add(WEST).add(UP).add(DOWN));
    }

    @Override
    public ActionResultType use(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult hit) {
        if (!world.isClientSide()) {
            for (Direction dir : PROPERTY_BY_DIRECTION.keySet()) {
                System.out.println(dir.toString() + state.getValue(PROPERTY_BY_DIRECTION.get(dir)));
            }
        }
        return ActionResultType.SUCCESS;
    }
}
