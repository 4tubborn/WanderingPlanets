package stubborn.wanderingplanets.registry.provider;

import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.damagesource.DamageScaling;
import net.minecraft.world.damagesource.DamageEffects;

import static stubborn.wanderingplanets.CreateWanderingPlanets.MODID;

public class ModDamageTypesProvider {
    // 🌟 定义伤害类型的 ResourceKey
    public static final ResourceKey<DamageType> GEOTHERMAL_ETCHANT = ResourceKey.create(
            Registries.DAMAGE_TYPE,
            ResourceLocation.fromNamespaceAndPath(MODID, "geothermal_etchant")
    );

    // 🌟 在数据包启动时注入属性
    public static void bootstrap(BootstrapContext<DamageType> context) {
        context.register(GEOTHERMAL_ETCHANT, new DamageType(
                "wanderingplanets.geothermal_etchant", // 语言文件中的本地化键 (Translation Key)
                DamageScaling.NEVER,                 // 伤害缩放（困难难度下是否加伤）
                0.1F,                                 // 伤害击退抗性
                DamageEffects.BURNING                 // 伤害视觉音效反馈（例如：BURNING 带有烧灼感, FREEZING 带有冰冻感）
        ));
    }
}