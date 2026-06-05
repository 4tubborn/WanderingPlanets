package stubborn.wanderingplanets.config.server;

import net.createmod.catnip.config.ConfigBase;

public class WPKinetics extends ConfigBase {

    public final WPStress stressValues = this.nested(1, WPStress::new, Comments.stress);

    @Override
    public String getName() {
        return "kinetics";
    }

    private static class Comments {
        static String stress = "Fine tune the kinetic stats of individual components";
    }
}