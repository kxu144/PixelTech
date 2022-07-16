package com.kxu144.pixeltech.item;

import com.kxu144.pixeltech.PixelTech;
import net.minecraft.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.function.Supplier;

public class ModItems {

    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, PixelTech.MOD_ID);

    //public static final RegistryObject<Item> SPAWNER = ITEMS.register("spawner",
    //        () -> new Item(new Item.Properties().tab(ItemGroup.TAB_MISC)));


    public static <T extends Item> void registerItem(String name, Supplier<T> item) {
        ITEMS.register(name, item);
    }

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }




}
