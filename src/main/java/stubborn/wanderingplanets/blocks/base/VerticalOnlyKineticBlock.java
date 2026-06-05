package stubborn.wanderingplanets.blocks.base;

import com.simibubi.create.content.kinetics.base.IRotate;
import com.simibubi.create.content.kinetics.base.KineticBlock;
import net.createmod.catnip.data.Iterate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import org.jetbrains.annotations.NotNull;

public class VerticalOnlyKineticBlock extends KineticBlock {

    // 1. 注册一个专门表示“朝上还是朝下”的布尔属性（比注册完整 FACING 更干净）
    public static final BooleanProperty IS_UP = BlockStateProperties.UP;

    public VerticalOnlyKineticBlock(Properties properties) {
        super(properties);
        // 默认状态为朝上
        this.registerDefaultState(this.defaultBlockState().setValue(IS_UP, true));
    }

    // 2. 将 IS_UP 属性加入到方块的状态定义中
    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(IS_UP);
        super.createBlockStateDefinition(builder);
    }

    // 3. 仿照 Create 官方实现的智能吸附算法：检测贴着它的上/下方块是否也是 Y 轴传动
    public static Direction.Axis getPreferredAxis(BlockPlaceContext context) {
        for (Direction side : Iterate.directions) {
            // 我们只关心竖直方向的邻居（UP 和 DOWN）
            if (side.getAxis() != Axis.Y)
                continue;

            BlockPos neighborPos = context.getClickedPos().relative(side);
            BlockState blockState = context.getLevel().getBlockState(neighborPos);

            if (blockState.getBlock() instanceof IRotate rotateBlock) {
                // 如果上/下方的方块能在朝向当前方块的面（side.getOpposite）提供 Y 轴的传动轴连接
                if (rotateBlock.hasShaftTowards(context.getLevel(), neighborPos, blockState, side.getOpposite())) {
                    return Axis.Y; // 找到了匹配的竖直动力源
                }
            }
        }
        return null;
    }

    // 4. 计算放置时的状态
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Axis preferredAxis = getPreferredAxis(context);
        Direction clickedFace = context.getClickedFace();

        // 默认逻辑：如果玩家潜行(Shift)，或者没有检测到竖直轴向的吸附
        boolean isUp = context.getNearestLookingDirection() == Direction.UP;

        // 智能吸附：如果点击的是方块的上面，默认方块应该朝上；点击下面，默认朝下
        if (clickedFace.getAxis() == Axis.Y) {
            isUp = clickedFace == Direction.UP;
        }

        // 如果检测到了现成的竖直动力源（preferredAxis != null），且玩家没按住 Shift 强行覆盖
        if (preferredAxis == Axis.Y && (context.getPlayer() == null || !context.getPlayer().isShiftKeyDown())) {
            // 自动吸附到对接的轴向（如果点击面是 DOWN 说明上方有轴，方块状态应当朝上对接）
            if (clickedFace.getAxis() == Axis.Y) {
                isUp = clickedFace == Direction.UP;
            }
        }

        return this.defaultBlockState().setValue(IS_UP, isUp);
    }

    // 5. 核心：强制该方块在机械动力系统中的传动轴线永远是竖直轴（Y 轴）
    @Override
    public Axis getRotationAxis(BlockState state) {
        return Axis.Y;
    }

    // 6. 核心：告诉 Create 的动力网，该方块只有【最顶上面】和【最底下面】可以输入和输出轴动力
    @Override
    public boolean hasShaftTowards(LevelReader world, BlockPos pos, BlockState state, Direction face) {
        return face.getAxis() == Axis.Y;
    }

    // 7. 安全锁：即便玩家在水平方向上旋转此方块（如使用机械动力扳手或活塞结构组合），也绝不改变其上下状态
    @Override
    public @NotNull BlockState rotate(BlockState state, Rotation rot) {
        return state;
    }
}