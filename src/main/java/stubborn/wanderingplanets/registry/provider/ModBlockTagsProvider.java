package stubborn.wanderingplanets.registry.provider;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;
import stubborn.wanderingplanets.registry.ModTags;

import java.util.concurrent.CompletableFuture;

import static stubborn.wanderingplanets.CreateWanderingPlanets.MODID;

public class ModBlockTagsProvider extends BlockTagsProvider {

    public ModBlockTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, @Nullable ExistingFileHelper existingFileHelper) {
        // 参数3传入你模组的 MODID
        super(output, lookupProvider, MODID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider lookupProvider) {
        // 🌟 场景 1：把方块塞进你自己的标签 (比如之前定义的 wanderingplanets:immune_to_etchant)
        // 或者是你改成了 minecraft:immune_to_etchant，只要把 TagKey 传进来即可
        //tag(ModTags.BLOCK.IMMUNE_TO_ETCHANT)
        //        .add(Blocks.OBSIDIAN, Blocks.CRYING_OBSIDIAN, Blocks.BEDROCK) // 添加原版方块
                //.addOptional(ResourceLocation.fromNamespaceAndPath("create", "clutch")) // 可选添加其他模组的方块
        //;

        var blockLookup = lookupProvider.lookupOrThrow(net.minecraft.core.registries.Registries.BLOCK);

        // 2. 获取强酸免疫标签构建器
        var immuneTagBuilder = tag(ModTags.BLOCK.IMMUNE_TO_ETCHANT);

        // 先手动添加指定的绝对免疫的原版基础方块
        //immuneTagBuilder.add(Blocks.OBSIDIAN, Blocks.CRYING_OBSIDIAN, Blocks.BEDROCK); //

        // 3. 安全遍历所有 Holder
        blockLookup.listElements().forEach(blockHolder -> {
            // 获取方块的注册名 ResourceLocation (例如 wanderingplanets:acid_block 或 create:yellow_postbox)
            var registryName = blockHolder.key().location();
            Block block = blockHolder.value(); //

            // 过滤基础空气方块
            if (block == Blocks.AIR || block == Blocks.CAVE_AIR || block == Blocks.VOID_AIR) {
                return; //
            }

            if (block instanceof net.minecraft.world.level.block.LiquidBlock ||
                    block instanceof net.minecraft.world.level.block.LayeredCauldronBlock) {
                return;
            }

            if (!block.defaultBlockState().getFluidState().isEmpty()) {
                return;
            }

            try {
                // 尝试安全读取硬度和爆抗
                float destroySpeed = block.defaultBlockState().getDestroySpeed(null, null); //
                float explosionResistance = block.defaultBlockState().getExplosionResistance(null, null, null); //

                // 判定条件：硬度 >= 45 或为 -1，或爆抗 >= 1000
                if ((destroySpeed >= 45.0F || destroySpeed == -1.0F) || explosionResistance >= 1000.0F) { //
                    immuneTagBuilder.add(blockHolder.key()); // 使用 key (ResourceKey) 添加，比直接传 Block 更符合新版 DataGen 标准
                }
            } catch (Exception e) {
                // 捕获所有类似于 yellow_postbox 抛出的未初始化异常，打印一条警告并优雅跳过
                System.out.println("[" + stubborn.wanderingplanets.CreateWanderingPlanets.MODID + " DataGen] 跳过了无法读取属性的外部方块: " + registryName);
            }
        });

        tag(ModTags.BLOCK.SMELT_BY_ETCHANT)
                .add(Blocks.ANCIENT_DEBRIS); // 添加原版方块
    }
}