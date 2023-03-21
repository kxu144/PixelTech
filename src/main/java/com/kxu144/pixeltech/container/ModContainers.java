package com.kxu144.pixeltech.container;

import com.kxu144.pixeltech.PixelTech;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.extensions.IForgeContainerType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class ModContainers {

    public static DeferredRegister<ContainerType<?>> CONTAINERS =
            DeferredRegister.create(ForgeRegistries.CONTAINERS, PixelTech.MOD_ID);

    public static final RegistryObject<ContainerType<EmitterContainer>> EMITTER_CONTAINER = CONTAINERS.register("emitter_container", () -> IForgeContainerType.create(((windowId, inv, data) -> {
        BlockPos pos = data.readBlockPos();
        World world = inv.player.getCommandSenderWorld();
        return new EmitterContainer(windowId, world, pos, inv, inv.player);
    })));
    public static final RegistryObject<ContainerType<CapturerContainer>> CAPTURER_CONTAINER = CONTAINERS.register("capturer_container", () -> IForgeContainerType.create(((windowId, inv, data) -> {
        BlockPos pos = data.readBlockPos();
        World world = inv.player.getCommandSenderWorld();
        return new CapturerContainer(windowId, world, pos, inv, inv.player);
    })));


    public static void register(IEventBus eventBus) {
        CONTAINERS.register(eventBus);
    }

}
