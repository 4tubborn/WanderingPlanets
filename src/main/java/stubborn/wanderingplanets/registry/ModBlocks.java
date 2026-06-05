package stubborn.wanderingplanets.registry;

import com.simibubi.create.AllTags;
import com.simibubi.create.content.kinetics.drill.DrillMovementBehaviour;
import com.simibubi.create.foundation.data.BlockStateGen;
import com.simibubi.create.foundation.data.CreateRegistrate;
import com.simibubi.create.foundation.data.SharedProperties;
import com.tterrag.registrate.util.entry.BlockEntry;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;

import stubborn.wanderingplanets.CreateWanderingPlanets;
import stubborn.wanderingplanets.blocks.BedrockBitingPeg;
import stubborn.wanderingplanets.blocks.EtchedBedrockBlock;
import stubborn.wanderingplanets.config.server.WPStress;

import static com.simibubi.create.api.behaviour.movement.MovementBehaviour.movementBehaviour;
import static com.simibubi.create.foundation.data.ModelGen.customItemModel;
import static com.simibubi.create.foundation.data.TagGen.axeOrPickaxe;

public class ModBlocks {

    public static void init() {
    }

    private static final CreateRegistrate REGISTRATE = CreateWanderingPlanets.getRegistrate();

    /*public static final BlockEntry<Block> PLANETARY_ENGINE = REGISTRATE.block("planetary_engine", Block::new)
            .initialProperties(SharedProperties::stone)
            .properties(p -> p.mapColor(MapColor.PODZOL))
            .transform(axeOrPickaxe())
            .blockstate(BlockStateGen.directionalBlockProvider(true))
            .transform(WPStress.setImpact(10000.0))
            .onRegister(movementBehaviour(new DrillMovementBehaviour()))
            .item()
            .tag(AllTags.AllItemTags.CONTRAPTION_CONTROLLED.tag)
            .transform(customItemModel())
            .register();*/

    public static final BlockEntry<EtchedBedrockBlock> ETCHED_BEDROCK = REGISTRATE.block("etched_bedrock", EtchedBedrockBlock::new)
            .initialProperties(SharedProperties::stone)
            .properties(p -> p.mapColor(MapColor.STONE).instrument(NoteBlockInstrument.BASEDRUM).strength(-1.0F, 3600000.0F).noLootTable().isValidSpawn(Blocks::never).lightLevel(state -> 8))
            .blockstate((context, provider) -> {
                // 通过 property() 针对你的布尔状态，分别映射不同的模型文件或者旋转
                provider.getVariantBuilder(context.get())
                        .partialState().with(EtchedBedrockBlock.ENGAGED, false)
                        .modelForState().modelFile(provider.cubeAll(context.get())).addModel() // false 状态用普通立方体

                        .partialState().with(EtchedBedrockBlock.ENGAGED, true)
                        // 如果你设计了 engaged=true 时的独立模型（比如叫 etched_bedrock_engaged），可以指向它
                        // 如果两状态模型完全一样，就继续放 provider.cubeAll(context.get())
                        .modelForState().modelFile(provider.cubeAll(context.get())).addModel();
            })
            .item()
            .build()
            .register()
            ;

    public static final BlockEntry<BedrockBitingPeg> BEDROCK_BITING_PEG = REGISTRATE.block("bedrock_biting_peg", BedrockBitingPeg::new)
            .initialProperties(SharedProperties::stone)
            .properties(p -> p.mapColor(MapColor.STONE).instrument(NoteBlockInstrument.BASEDRUM).strength(-1.0F, 3600000.0F).noLootTable().isValidSpawn(Blocks::never).lightLevel(state -> 8))
            .blockstate((context, provider) -> {
                // 通过 property() 针对你的布尔状态，分别映射不同的模型文件或者旋转
                provider.getVariantBuilder(context.get());
            })
            .item()
            .build()
            .register()
            ;

    public static final BlockEntry<Block> MAGNET_BLOCK = REGISTRATE.block("magnet_block", Block::new)
            .initialProperties(SharedProperties::stone)
            .properties(p -> p.mapColor(MapColor.STONE).instrument(NoteBlockInstrument.BASEDRUM).strength(1.0F, 1200.0F))
            .item()
            .build()
            .register()
            ;
}