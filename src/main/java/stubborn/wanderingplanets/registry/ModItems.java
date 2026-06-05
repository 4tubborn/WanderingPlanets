package stubborn.wanderingplanets.registry;

import com.simibubi.create.AllCreativeModeTabs;
import com.simibubi.create.AllTags;
import com.simibubi.create.Create;
import com.simibubi.create.content.processing.sequenced.SequencedAssemblyItem;
import com.simibubi.create.foundation.data.CreateRegistrate;
import com.tterrag.registrate.util.entry.ItemEntry;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.common.Tags;
import stubborn.wanderingplanets.CreateWanderingPlanets;

import static com.simibubi.create.AllTags.AllItemTags.PLATES;

public class ModItems {

    public static void init() {
    }

    private static final CreateRegistrate REGISTRATE = CreateWanderingPlanets.getRegistrate();

    static {
        REGISTRATE.setCreativeTab(AllCreativeModeTabs.BASE_CREATIVE_TAB);
    }

    public static final ItemEntry<Item> STONE_DUST = REGISTRATE.item("stone_dust", Item::new)
            .register();
}
