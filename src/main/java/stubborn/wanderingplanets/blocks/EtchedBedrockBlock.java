package stubborn.wanderingplanets.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BubbleColumnBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.neoforged.neoforge.common.enums.BubbleColumnDirection;
import stubborn.wanderingplanets.registry.ModBlocks;
import stubborn.wanderingplanets.registry.ModTags;

public class EtchedBedrockBlock extends Block {

    public static final BooleanProperty ENGAGED = BooleanProperty.create("engaged");


    public EtchedBedrockBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any()
                .setValue(ENGAGED, false)
        );
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(ENGAGED);
    }

    @Override
    public BubbleColumnDirection getBubbleColumnDirection(BlockState state) {
        return BubbleColumnDirection.DOWNWARD;
    }

    @Override
    public void stepOn(Level level, BlockPos pos, BlockState state, Entity entity) {
        if (!entity.isSteppingCarefully() && entity instanceof LivingEntity) {
            entity.hurt(level.damageSources().hotFloor(), 1.0F);
        }

        super.stepOn(level, pos, state, entity);
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        BubbleColumnBlock.updateColumn(level, pos.above(), state);
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction facing, BlockState facingState, LevelAccessor level, BlockPos currentPos, BlockPos facingPos) {
        if (facing == Direction.UP && facingState.is(Blocks.WATER)) {
            level.scheduleTick(currentPos, this, 20);
        }

        return super.updateShape(state, facing, facingState, level, currentPos, facingPos);
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        level.scheduleTick(pos, this, 20);
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
        super.neighborChanged(state, level, pos, block, fromPos, isMoving);
        if (!level.isClientSide) {
            if(state.getValue(ENGAGED))return;
            BlockState stateAbove = level.getBlockState(pos.above());
            if(stateAbove.is(ModBlocks.BEDROCK_BITING_PEG)) {
                level.setBlock(pos, state.setValue(ENGAGED, true), 3);
            }
        }
    }
}
