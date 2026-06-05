package stubborn.wanderingplanets.registry;

import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.kinetics.base.OrientedRotatingVisual;
import com.simibubi.create.foundation.data.CreateRegistrate;
import com.tterrag.registrate.util.entry.BlockEntityEntry;
import stubborn.wanderingplanets.CreateWanderingPlanets;

public class ModBlockEntities {

    public static void init() {
    }

    private static final CreateRegistrate REGISTRATE = CreateWanderingPlanets.getRegistrate();

    /*public static final BlockEntityEntry<PlanetaryEngineBlockEntity> PLANETARY_ENGINE = REGISTRATE
            .blockEntity("planetary_engine", PlanetaryEngineBlockEntity::new)
            .visual(() -> OrientedRotatingVisual.of(AllPartialModels.DRILL_HEAD), false)
            .validBlocks(ModBlocks.PLANETARY_ENGINE)
            .renderer(() -> PlanetaryEngineRenderer::new)
            .register();*/
}
