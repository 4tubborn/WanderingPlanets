package stubborn.wanderingplanets.registry.provider;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;
import stubborn.wanderingplanets.registry.ModTags;

import java.util.concurrent.CompletableFuture;

import static stubborn.wanderingplanets.CreateWanderingPlanets.MODID;

public class ModItemTagsProvider extends ItemTagsProvider {

    public ModItemTagsProvider(PackOutput output,
                               CompletableFuture<HolderLookup.Provider> lookupProvider,
                               CompletableFuture<TagLookup<net.minecraft.world.level.block.Block>> blockTags,
                               @Nullable ExistingFileHelper existingFileHelper) {
        // 🌟 调用父类的标准构造函数
        super(output, lookupProvider, blockTags, stubborn.wanderingplanets.CreateWanderingPlanets.MODID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider lookupProvider) {
        // 🌟 场景 1：把方块塞进你自己的标签 (比如之前定义的 wanderingplanets:immune_to_etchant)
        this.copy(ModTags.BLOCK.SMELT_BY_ETCHANT, ModTags.ITEM.SMELT_BY_ETCHANT);
        //this.copy(ModTags.BLOCK.IMMUNE_TO_ETCHANT, ModTags.ITEM.IMMUNE_TO_ETCHANT);
        var itemLookup = lookupProvider.lookupOrThrow(net.minecraft.core.registries.Registries.ITEM);
        var immuneItemBuilder = tag(ModTags.ITEM.IMMUNE_TO_ETCHANT);

        // 先把原版铁打的这三个基础防火/酸物品手动加进去
        //immuneItemBuilder.add(net.minecraft.world.item.Items.OBSIDIAN, net.minecraft.world.item.Items.CRYING_OBSIDIAN, net.minecraft.world.item.Items.BEDROCK);

        itemLookup.listElements().forEach(itemHolder -> {
            var item = itemHolder.value();
            var registryName = itemHolder.key().location();

            if (item == net.minecraft.world.item.Items.AIR) return;

            // 🌟 反向获取：通过物品直接找它对应的方块
            Block block = Block.byItem(item);

            // 如果这个物品没有对应的方块（比如剑、苹果、皮革），直接跳过（它们会走 fire_resistant 组件判定，不需要进方块免疫标签）
            if (block == net.minecraft.world.level.block.Blocks.AIR) {
                return;
            }

            // 过滤掉流体
            if (block instanceof net.minecraft.world.level.block.LiquidBlock) return;

            // 🌟 核心属性判定：逻辑和方块那边完全一样
            try {
                float destroySpeed = block.defaultBlockState().getDestroySpeed(null, null);
                float explosionResistance = block.defaultBlockState().getExplosionResistance(null,null,null);

                // 只要这个物品对应的方块满足硬度条件，就把它对应的物品加入到物品免疫标签里
                if ((destroySpeed >= 45.0F || destroySpeed == -1.0F) || explosionResistance >= 1000.0F) {
                    immuneItemBuilder.add(itemHolder.key());
                }
            } catch (Exception e) {
                // 彻底对外部模组的异常免疫
                System.out.println("[" + stubborn.wanderingplanets.CreateWanderingPlanets.MODID + " ItemDataGen] 跳过了读取属性异常的外部物品: " + registryName);
            }
        });
    }
}