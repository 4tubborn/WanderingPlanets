package stubborn.wanderingplanets.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

import static stubborn.wanderingplanets.CreateWanderingPlanets.MODID;

public class ModTags {

    public static final class BLOCK {
        public static final TagKey<Block> IMMUNE_TO_ETCHANT = TagKey.create(
                // The registry key. The type of the registry must match the generic type of the tag.
                Registries.BLOCK,
                // The location of the tag. This example will put our tag at data/examplemod/tags/blocks/example_tag.json.
                ResourceLocation.fromNamespaceAndPath(MODID, "immune_to_etchant")
        );

        public static final TagKey<Block> SMELT_BY_ETCHANT = TagKey.create(
                // The registry key. The type of the registry must match the generic type of the tag.
                Registries.BLOCK,
                // The location of the tag. This example will put our tag at data/examplemod/tags/blocks/example_tag.json.
                ResourceLocation.fromNamespaceAndPath(MODID, "smelt_by_etchant")
        );
    }

    public static final class ITEM {
        public static final TagKey<Item> SMELT_BY_ETCHANT = TagKey.create(
                // The registry key. The type of the registry must match the generic type of the tag.
                Registries.ITEM,
                // The location of the tag. This example will put our tag at data/examplemod/tags/blocks/example_tag.json.
                ResourceLocation.fromNamespaceAndPath(MODID, "smelt_by_etchant")
        );

        public static final TagKey<Item> IMMUNE_TO_ETCHANT = TagKey.create(
                // The registry key. The type of the registry must match the generic type of the tag.
                Registries.ITEM,
                // The location of the tag. This example will put our tag at data/examplemod/tags/blocks/example_tag.json.
                ResourceLocation.fromNamespaceAndPath(MODID, "immune_to_etchant")
        );
    }
}
