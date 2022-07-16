package com.mrmakui.pixeltech.tileentity;

import com.mrmakui.pixeltech.PixelTech;
import com.mrmakui.pixeltech.block.ModBlocks;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class ModTileEntities {

    public static DeferredRegister<TileEntityType<?>> TILE_ENTITIES =
            DeferredRegister.create(ForgeRegistries.TILE_ENTITIES, PixelTech.MOD_ID);

    public static RegistryObject<TileEntityType<EmitterTile>> EMITTER_TILE = TILE_ENTITIES.register("emitter_tile", () -> TileEntityType.Builder.of(EmitterTile::new, ModBlocks.EMITTER.get()).build(null));

    public static void register(IEventBus eventBus) {
        TILE_ENTITIES.register(eventBus);
    }



}
