package stubborn.wanderingplanets.fluids;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.functions.SmeltItemFunction;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.neoforged.neoforge.common.Tags;
import stubborn.wanderingplanets.registry.ModBlocks;
import stubborn.wanderingplanets.registry.ModTags;
import stubborn.wanderingplanets.registry.provider.ModDamageTypesProvider;

import java.util.Optional;
import java.util.function.Supplier;

public class GeothermalEtchant extends LiquidBlock {

    public GeothermalEtchant(Supplier<? extends net.minecraft.world.level.material.FlowingFluid> fluid, Properties properties) {
        super(fluid.get(), properties);
    }

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        super.entityInside(state, level, pos, entity);

        // 仅在服务端处理伤害，并且确保对象是活着的生物（玩家、怪物、动物等）
        if (!level.isClientSide) {

            var holder = level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).
                    getHolderOrThrow(ModDamageTypesProvider.GEOTHERMAL_ETCHANT);
            DamageSource damageSource = new DamageSource(holder);

            if(entity instanceof LivingEntity livingEntity) {

                livingEntity.hurt(damageSource, 10.0F);

                // 让生物身上着火（持续 20 秒）
                livingEntity.setRemainingFireTicks(400);
            } else if (entity instanceof ItemEntity itemEntity) {
                //itemEntity.setUnderLavaMovement();
                ItemStack itemStack = itemEntity.getItem();

                if (itemStack.is(ModTags.ITEM.IMMUNE_TO_ETCHANT)) {
                    return;
                }

                if (itemStack.is(ModTags.ITEM.SMELT_BY_ETCHANT)) {
                    ItemStack resultStack = this.spawnSmeltResultItem(itemStack, (ServerLevel) level);
                    if( resultStack != null && !resultStack.isEmpty()) {
                        Block.popResource(level, pos, resultStack);
                        level.playSound(null, pos, SoundEvents.LAVA_EXTINGUISH, SoundSource.BLOCKS, 0.5F, 2.6F);
                        itemEntity.discard();
                        return;
                    }
                }

                // ② 🌟 关键核心：显式判断物品是否拥有原版的 fire_resistant 属性组件（如：下界合金装备/工具/方块）
                if (itemStack.has(DataComponents.FIRE_RESISTANT)) {
                    return; // 绝不破坏防火的宝贝，直接安全返回
                }

                // 3. 如果扔进去的是普通垃圾掉落物（比如木棍、泥土），直接被强酸腐蚀物理蒸发（效仿原版岩浆）
                // 如果你想让下界合金等高级装备免受酸蚀，可以在这里加判断：if (!itemStack.isFireResistant())
                //level.playSound(null, pos, net.minecraft.sounds.SoundEvents.FIRE_EXTINGUISH, net.minecraft.sounds.SoundSource.BLOCKS, 0.2F, 2.0F);
                itemEntity.hurt(damageSource, 10.0F);
            }
        }
    }

    // 🌟 1. 当流体被放置或者蔓延到一个新坐标时触发
    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        super.onPlace(state, level, pos, oldState, isMoving);
        if (!level.isClientSide) {
            this.corrodeSurroundingBlocks(level, pos);
        }
    }

    // 🌟 2. 当周围方块发生状态更新（例如流体流动、方块改变）时触发
    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
        super.neighborChanged(state, level, pos, block, fromPos, isMoving);
        if (!level.isClientSide) {
            this.corrodeSurroundingBlocks(level, pos);
        }
    }

    /**
     * 核心腐蚀逻辑：清除上下左右前后毗邻的方块
     */
    private void corrodeSurroundingBlocks(Level level, BlockPos pos) {

        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        BlockPos.MutableBlockPos neighborPos = new BlockPos.MutableBlockPos();

        for (Direction direction : Direction.values()) {
            // 原地改变指针，不 new 新对象
            neighborPos.set(pos).move(direction);

            BlockState neighborState = level.getBlockState(neighborPos);

            if (neighborState.is(Blocks.BEDROCK) && direction == Direction.DOWN) {
                level.setBlock(neighborPos, ModBlocks.ETCHED_BEDROCK.getDefaultState(), 3);
                continue;
            }

            if ((neighborState.isAir() || neighborState.is(ModTags.BLOCK.IMMUNE_TO_ETCHANT) || neighborState.getBlock() == this ) && !neighborState.is(ModTags.BLOCK.SMELT_BY_ETCHANT)) {
                continue;
            }
            if (!neighborState.getFluidState().isEmpty() && neighborState.getBlock() instanceof LiquidBlock) {
                continue;
            }
            if (neighborState.is(ModTags.BLOCK.SMELT_BY_ETCHANT)) {
                ItemStack resultStack = this.spawnSmeltResultItem(new ItemStack(neighborState.getBlock().asItem()), serverLevel);
                if( resultStack != null && !resultStack.isEmpty()) {
                    Block.popResource(level, neighborPos, resultStack);
                    //level.setBlock(neighborPos, Blocks.AIR.defaultBlockState(), 3);
                    //continue;
                }
            }

            // 破坏方块
            level.destroyBlock(neighborPos, false, null);
        }
    }

    private ItemStack spawnSmeltResultItem(ItemStack inputStack, ServerLevel level) {
        if (inputStack.isEmpty()) {
            return null;
        }

        // 1:1 完美复刻原版 SmeltItemFunction 的核心逻辑，直接向 RecipeManager 索要熔炼结果
        Optional<RecipeHolder<SmeltingRecipe>> recipe = level.getRecipeManager()
                .getRecipeFor(RecipeType.SMELTING, new SingleRecipeInput(inputStack), level);

        if (recipe.isPresent()) {
            // 获取熔炼产物
            ItemStack resultItem = recipe.get().value().getResultItem(level.registryAccess());
            if (!resultItem.isEmpty()) {
                // 继承输入堆叠的数量（如果是单方块则是 1）
                return resultItem.copyWithCount(inputStack.getCount() * resultItem.getCount());
            }
        }

        return null;
    }
}