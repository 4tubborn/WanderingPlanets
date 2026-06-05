package stubborn.wanderingplanets.registry;

import java.util.function.Supplier;
import org.jetbrains.annotations.NotNull;
import com.simibubi.create.foundation.data.CreateRegistrate;
import com.simibubi.create.infrastructure.config.AllConfigs;
import com.tterrag.registrate.builders.FluidBuilder.FluidTypeFactory;
import com.tterrag.registrate.util.entry.FluidEntry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.DispensibleContainerItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;
import net.neoforged.neoforge.fluids.FluidType;
import stubborn.wanderingplanets.CreateWanderingPlanets;
import stubborn.wanderingplanets.fluids.GeothermalEtchant;

public class ModFluids {
    private static final CreateRegistrate REGISTRATE = CreateWanderingPlanets.getRegistrate();

    public static void init() {
        CreateWanderingPlanets.LOGGER.info("Registering fluids for Wandering Planets!");
    }

    public static final FluidEntry<BaseFlowingFluid.Flowing> GEOTHERMAL_ETCHANT =
            REGISTRATE.standardFluid("geothermal_etchant",
                            ModFluids.SolidRenderedPlaceableFluidType.create(0x622020,
                                    () -> 1f / 32f * AllConfigs.client().chocolateTransparencyMultiplier.getF()))
                    .lang("Geothermal Etchant")
                    .properties(b -> b.viscosity(1500).density(1400))
                    .fluidProperties(p -> p.levelDecreasePerBlock(2)
                            .tickRate(25)
                            .slopeFindDistance(3)
                            .explosionResistance(100f))
                    .source(BaseFlowingFluid.Source::new)
                    .block((fluid, properties) -> new GeothermalEtchant(() -> fluid, properties))
                    .properties(p -> p.mapColor(MapColor.TERRACOTTA_BROWN).lightLevel(state -> 15))
                    .build()
                    .bucket()
                    .onRegister(ModFluids::registerFluidDispenseBehavior)
                    .tag(Tags.Items.BUCKETS)
                    .build()
                    .register();

    private static final DispenseItemBehavior DEFAULT = new DefaultDispenseItemBehavior();
    private static final DispenseItemBehavior DISPENSE_FLUID = new DefaultDispenseItemBehavior(){
        @Override
        protected @NotNull ItemStack execute(BlockSource pSource, ItemStack pStack) {
            DispensibleContainerItem dispensibleContainerItem = (DispensibleContainerItem) pStack.getItem();
            BlockPos pos = pSource.pos().relative(pSource.state().getValue(DispenserBlock.FACING));
            Level level = pSource.level();
            if (dispensibleContainerItem.emptyContents(null, level, pos, null, pStack)) {
                return new ItemStack(Items.BUCKET);
            }
            return DEFAULT.dispense(pSource, pStack);
        }
    };

    private static void registerFluidDispenseBehavior(BucketItem bucket) {
        DispenserBlock.registerBehavior(bucket, DISPENSE_FLUID);
    }

    /**
     * 1.21.1 纯服务端/通用属性的基类（不包含任何 Client 代码）
     */
    public static abstract class TintedFluidType extends FluidType {
        private final ResourceLocation stillTexture;
        private final ResourceLocation flowingTexture;

        public TintedFluidType(Properties properties, ResourceLocation stillTexture, ResourceLocation flowingTexture) {
            super(properties);
            this.stillTexture = stillTexture;
            this.flowingTexture = flowingTexture;
        }

        public ResourceLocation getStillTexture() { return stillTexture; }
        public ResourceLocation getFlowingTexture() { return flowingTexture; }

        public abstract int getTintColor();
        public abstract int getBlockTintColor();
        public abstract int getFogColor();
        public abstract float getFogDistanceModifier();
    }

    public static class SolidRenderedPlaceableFluidType extends TintedFluidType {
        private int fogColor;
        private Supplier<Float> fogDistance;

        public static FluidTypeFactory create(int fogColor, Supplier<Float> fogDistance) {
            return (p, s, f) -> {
                SolidRenderedPlaceableFluidType fluidType = new SolidRenderedPlaceableFluidType(p, s, f);
                fluidType.fogColor = fogColor;
                fluidType.fogDistance = fogDistance;
                return fluidType;
            };
        }

        private SolidRenderedPlaceableFluidType(Properties properties, ResourceLocation stillTexture, ResourceLocation flowingTexture) {
            super(properties, stillTexture, flowingTexture);
        }

        @Override
        public int getTintColor() { return 0xffffffff; }

        @Override
        public int getBlockTintColor() { return 0x00ffffff; } // 去除 Alpha 防止光影和群系污染

        @Override
        public int getFogColor() { return this.fogColor; }

        @Override
        public float getFogDistanceModifier() { return this.fogDistance.get(); }
    }
}