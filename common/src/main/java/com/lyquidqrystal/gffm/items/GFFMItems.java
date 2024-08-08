package com.lyquidqrystal.gffm.items;

import com.lyquidqrystal.gffm.GainFriendshipFromMelodies;
import dev.architectury.registry.registries.DeferredRegister;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;

public class GFFMItems {
    public static DeferredRegister<Item> ITEMS = DeferredRegister.create(GainFriendshipFromMelodies.MOD_ID, Registries.ITEM);

    /*
    I tried so hard, and got so far, but in the end, it doesn't even matter. T_T
    No items are added currently,I tried to add a new flute but I failed. I don't think copying the file into my mod is a good idea so I didn't try that. I just tried to get the Instrument using accessor and failed.
    public static final RegistrySupplier<Item> POKEFLUTE = register(GainFriendshipFromMelodies.MOD_ID, "pokeflute", "flute");

    public static RegistrySupplier<Item> register(String namespace, String name, String baseInstrumentName) {
        ResourceLocation resourceLocation = new ResourceLocation(namespace, name);
        //Sounds.Instrument instrument=new Sounds.Instrument(namespace,name)
        InstrumentItem instrumentItem= (InstrumentItem) Items .FLUTE .get();
        Sounds.Instrument instrument=((InstrumentItemAccessor)instrumentItem).getSound();
        long sustain =((InstrumentItemAccessor) instrumentItem).getSustain();
        Vector3f offset=((InstrumentItemAccessor) instrumentItem).getOffset();
        Supplier<Item> itemSupplier = () -> new InstrumentItem(Items.baseProps(), instrument, sustain, offset);

        //Items.items.add(supplier);//I don't think we really need this line
        return ITEMS.register(resourceLocation,itemSupplier);
    }
     */

    public static void bootstrap() {
        GainFriendshipFromMelodies.LOGGER.info("GFFM Registering Items");
        ITEMS.register();
    }
    protected class IMItemData{

    }
}
