package stubborn.wanderingplanets;

import com.mojang.blaze3d.shaders.FogShape;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.logging.LogUtils;
import com.simibubi.create.foundation.data.CreateRegistrate;
import com.simibubi.create.foundation.item.ItemDescription;
import com.simibubi.create.foundation.item.KineticStats;
import com.simibubi.create.foundation.item.TooltipHelper;
import com.simibubi.create.foundation.item.TooltipModifier;
import net.createmod.catnip.lang.FontHelper;
import net.createmod.catnip.theme.Color;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.material.FluidState;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;
import org.slf4j.Logger;
import stubborn.wanderingplanets.registry.ModBlockEntities;
import stubborn.wanderingplanets.registry.ModBlocks;
import stubborn.wanderingplanets.registry.ModFluids;
import stubborn.wanderingplanets.registry.ModItems;

import java.util.concurrent.CompletableFuture;


@Mod(CreateWanderingPlanets.MODID)
public class CreateWanderingPlanets {
    public static final String MODID = "wanderingplanets";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static final FontHelper.Palette PALETTE = new FontHelper.Palette(
            TooltipHelper.styleFromColor(0x80AFD2),
            TooltipHelper.styleFromColor(0x4D98FA)
    );

    private static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> WP_TAB =
            CREATIVE_MODE_TABS.register("wandering_tab", () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.wanderingplanets"))
                    .icon(ModItems.STONE_DUST::asStack)
                    .build());

    /**
     * 🔒 JVM 线程绝对安全的类加载单例
     */
    private static final class RegistrateHolder {
        private static final CreateRegistrate INSTANCE = CreateRegistrate.create(CreateWanderingPlanets.MODID)
                .defaultCreativeTab(CreateWanderingPlanets.WP_TAB.getKey())
                .setTooltipModifierFactory(item -> new ItemDescription.Modifier(item, CreateWanderingPlanets.PALETTE)
                        .andThen(TooltipModifier.mapNull(KineticStats.create(item))));
    }

    public static CreateRegistrate getRegistrate() {
        return RegistrateHolder.INSTANCE;
    }

    public CreateWanderingPlanets(IEventBus modEventBus, ModContainer modContainer) {
        // 挂载创造页延迟注册器
        CREATIVE_MODE_TABS.register(modEventBus);

        // 向并发模组总线安全发布 Registrate 的内部监听项
        getRegistrate().registerEventListeners(modEventBus);

        // 强行在单线程构造期拉起静态代码块，防止多线程竞争
        ModBlocks.init();
        ModBlockEntities.init();
        ModItems.init();
        ModFluids.init();

        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    /**
     * =========================================================================
     * 🛠️ 模组生命周期事件监听总线 (Mod Event Bus)
     * =========================================================================
     */
    /*@EventBusSubscriber(modid = MODID)
    public static class ModEventListeners {

        @SubscribeEvent
        public static void commonSetup(FMLCommonSetupEvent event) {
            // 对照文档规范：由于此事件属于 ParallelDispatchEvent (并行触发)，
            // 任何涉及主线程同步的杂项设置，建议包裹在 enqueueWork 中以确保线程安全
            event.enqueueWork(() -> {
                LOGGER.info("Wandering Planets: Thread-safe common setup executed successfully.");
            });
        }
    }

    /**
     * =========================================================================
     * 🌍 游戏全局动态事件监听总线 (Game Event Bus / 旧 Forge 总线)
     * =========================================================================

    @EventBusSubscriber(modid = MODID)
    public static class GameEventListeners {

        @SubscribeEvent
        public static void onServerStarting(ServerStartingEvent event) {
            LOGGER.info("Wandering Planets: Server instance detected and loaded successfully.");
        }
    }*/
    @EventBusSubscriber(modid = MODID)
    public static class ModEventListeners {

        // 🌟 1.21.1 正统流体渲染绑定方式：完全不破坏 FluidType，干净又安全
        @SubscribeEvent
        public static void registerClientExtensions(RegisterClientExtensionsEvent event) {
            // 获取你注册的地热流体的 FluidType
            ModFluids.TintedFluidType fluidType = (ModFluids.TintedFluidType) ModFluids.GEOTHERMAL_ETCHANT.getType();

            event.registerFluidType(new IClientFluidTypeExtensions() {
                @Override
                public ResourceLocation getStillTexture() {
                    return fluidType.getStillTexture();
                }

                @Override
                public ResourceLocation getFlowingTexture() {
                    return fluidType.getFlowingTexture();
                }

                @Override
                public int getTintColor(FluidStack stack) {
                    return fluidType.getTintColor();
                }

                @Override
                public int getTintColor(FluidState state, BlockAndTintGetter getter, BlockPos pos) {
                    return fluidType.getBlockTintColor();
                }

                @Override
                public @NotNull Vector3f modifyFogColor(Camera camera, float partialTick, ClientLevel level,
                                                        int renderDistance, float darkenWorldAmount, Vector3f fluidFogColor) {
                    return new Color(fluidType.getFogColor(), false).asVectorF();
                }

                @Override
                public void modifyFogRender(Camera camera, FogRenderer.FogMode mode, float renderDistance, float partialTick,
                                            float nearDistance, float farDistance, FogShape shape) {
                    float modifier = fluidType.getFogDistanceModifier();
                    float baseWaterFog = 96.0f;
                    if (modifier != 1f) {
                        RenderSystem.setShaderFogShape(FogShape.CYLINDER);
                        RenderSystem.setShaderFogStart(-8);
                        RenderSystem.setShaderFogEnd(baseWaterFog * modifier);
                    }
                }
            }, fluidType);
        }
    }
}