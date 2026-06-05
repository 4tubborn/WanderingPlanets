package stubborn.wanderingplanets.config.server;

import net.createmod.catnip.config.ConfigBase;

public class WPServerConfig extends ConfigBase {

    //public final OffroadBlockConfigs blocks = this.nested(0, OffroadBlockConfigs::new, Comments.blockConfig);
    public final WPKinetics kinetics = this.nested(0, WPKinetics::new, Comments.kinetics);

    @Override
    public String getName() {
        return "server";
    }

    private static class Comments {
        static String kinetics = "Parameters and abilities of Offroad's kinetic mechanisms";
        static String blockConfig = "Parameters and abilities of Offroad Blocks";
    }
}