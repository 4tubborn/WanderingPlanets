package stubborn.wanderingplanets.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityStruckByLightningEvent;
import net.neoforged.neoforge.event.level.ExplosionEvent;
import stubborn.wanderingplanets.registry.ModBlocks;

import static stubborn.wanderingplanets.CreateWanderingPlanets.MODID;

@EventBusSubscriber(modid = MODID)
public class IronBlockEvent {
    //被雷电击中
    @SubscribeEvent
    public static void onLightningStrike(EntityStruckByLightningEvent event) {
        Level level = event.getEntity().level();
        if (level.isClientSide) return;

        BlockPos pos = event.getEntity().blockPosition();
        // 检查击中点（或下方）是否有铁块
        if (level.getBlockState(pos).is(Blocks.IRON_BLOCK) || level.getBlockState(pos.below()).is(Blocks.IRON_BLOCK)) {
            applyMagnetTransformation(level, pos.below());
        }
    }


    // 2. 核心转化逻辑：从指定位置开始，向周围随机游走并替换方块
    public static void applyMagnetTransformation(Level level, BlockPos pos) {
        // 先替换被直接击中的那个方块
        level.setBlockAndUpdate(pos, ModBlocks.MAGNET_BLOCK.get().defaultBlockState());

        // 模仿原版随机游走，寻找周围的铁块进行链式转化
        BlockPos.MutableBlockPos mutable = pos.mutable();
        int steps = level.random.nextInt(3) + 3; // 3-5 次传播
        for (int i = 0; i < steps; i++) {
            int spread = level.random.nextInt(8) + 1; // 每次游走的步数
            randomWalkMagnetize(level, pos, mutable, spread);
        }
    }

    private static void randomWalkMagnetize(Level level, BlockPos pos, BlockPos.MutableBlockPos mutable, int steps) {
        mutable.set(pos);
        for (int i = 0; i < steps; i++) {
            // 在 10x10x10 的立方体范围内寻找铁块
            for (BlockPos target : BlockPos.randomInCube(level.random, 10, mutable, 1)) {
                if ((level.getBlockState(target)).is(Blocks.IRON_BLOCK)) {
                    level.setBlockAndUpdate(target, ModBlocks.MAGNET_BLOCK.get().defaultBlockState());
                    mutable.set(target); // 移动到新的位置继续传播
                    level.levelEvent(3002, target, -1); // 播放一个特效，3002 是原版铜块除锈的特效 ID
                    break;
                }
            }
        }
    }
    //苦力怕爆炸
    @SubscribeEvent
    public static void onExplosionDetonate(ExplosionEvent.Detonate event) {
        if (event.getExplosion().getDirectSourceEntity() instanceof Creeper creeper) {
            if(!creeper.isPowered())return;
            // 遍历所有即将被炸毁的方块
            event.getAffectedBlocks().removeIf(pos -> {
                if (event.getLevel().getBlockState(pos).is(Blocks.IRON_BLOCK)) {
                    // 将铁块替换为你的磁铁块
                    event.getLevel().setBlock(pos, ModBlocks.MAGNET_BLOCK.get().defaultBlockState(), 3);
                    return true; // 返回 true 表示从爆炸摧毁列表中移除（防止铁块被炸消失）
                }
                return false;
            });
        }
    }
}