package com.kxu144.pixeltech.block;

import com.kxu144.pixeltech.PixelTech;
import com.kxu144.pixeltech.item.ModItems;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.function.Supplier;

public class ModBlocks {

    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, PixelTech.MOD_ID);

    public static final RegistryObject<Block> SPAWNER = registerBlock("spawner", () -> new Block(AbstractBlock.Properties.of(Material.STONE).strength(5.0F).sound(SoundType.METAL)));
    public static final RegistryObject<Block> EMITTER = registerBlock("emitter", () -> new EmitterBlock(AbstractBlock.Properties.of(Material.STONE).strength(5.0F).sound(SoundType.METAL)));

    public static <T extends Block>RegistryObject<T> registerBlock(String name, Supplier<T> block) {
        RegistryObject<T> obj = BLOCKS.register(name, block);
        registerBlockItem(name, obj);
        return obj;
    }

    private static <T extends Block> void registerBlockItem(String name, RegistryObject<T> block) {
        ModItems.ITEMS.register(name, () -> new BlockItem(block.get(),
                new Item.Properties()));
    }

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }

}
