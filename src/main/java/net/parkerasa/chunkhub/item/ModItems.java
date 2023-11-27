package net.parkerasa.chunkhub.item;

import net.minecraftforge.registries.RegistryObject;
import net.parkerasa.chunkhub.chunkhub;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;



public class ModItems {

    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, chunkhub.MOD_ID);



    public static void register(IEventBus eventBus)
    {
        ITEMS.register(eventBus);
    }
}
