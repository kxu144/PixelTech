package com.kxu144.pixeltech.block;

import com.kxu144.pixeltech.tileentity.ModTileEntities;
import com.pixelmonmod.pixelmon.blocks.machines.PCBlock;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class PokePipeBlock extends Block implements PokeConnectable {

    public static final BooleanProperty NORTH = BooleanProperty.create("north");
    public static final BooleanProperty SOUTH = BooleanProperty.create("south");
    public static final BooleanProperty WEST = BooleanProperty.create("west");
    public static final BooleanProperty EAST = BooleanProperty.create("east");
    public static final BooleanProperty UP = BooleanProperty.create("up");
    public static final BooleanProperty DOWN = BooleanProperty.create("down");

    public static final List<Class<?>> EXTRA_CONNECTED_BLOCKS = new ArrayList<Class<?>>() {
        {
            add(PCBlock.class);
        }
    };



    public PokePipeBlock(AbstractBlock.Properties properties) {
        super(properties);
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
}
