package stubborn.wanderingplanets.config;

import net.minecraft.Util;
import net.minecraft.util.Unit;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;
import stubborn.wanderingplanets.config.server.WPServerConfig;
import stubborn.wanderingplanets.config.server.WPStress;

public class WPConfig {

    static final WPServerConfig SERVER_CONFIG = new WPServerConfig();

    private static ModConfigSpec COMMON_SPEC;
    private static ModConfigSpec CLIENT_SPEC;
    private static ModConfigSpec SERVER_SPEC;

    public WPConfig (ModContainer container) {
        /*COMMON_SPEC = Util.make(new ModConfigSpec.Builder().configure(builder -> {
            COMMON_CONFIG.registerAll(builder);
            return Unit.INSTANCE;
        }).getValue(), spec -> container.registerConfig(Type.COMMON, spec));
        CLIENT_SPEC = Util.make(new ModConfigSpec.Builder().configure(builder -> {
            CLIENT_CONFIG.registerAll(builder);
            return Unit.INSTANCE;
        }).getValue(), spec -> container.registerConfig(Type.CLIENT, spec));*/
        SERVER_SPEC = Util.make(new ModConfigSpec.Builder().configure(builder -> {
            SERVER_CONFIG.registerAll(builder);
            return Unit.INSTANCE;
        }).getValue(), spec -> container.registerConfig(ModConfig.Type.SERVER, spec));
    }


    static WPServerConfig server() {
        return SERVER_CONFIG;
    }

    /*public static WPStress stress() {
        return SERVER_CONFIG.kinetics.stressValues;
    }*/

    @SubscribeEvent
    public void onLoad(ModConfigEvent.Loading event) {
        var spec = event.getConfig().getSpec();
        if (spec == SERVER_SPEC)
            SERVER_CONFIG.onLoad();
    }

    @SubscribeEvent
    public void onReload(ModConfigEvent.Reloading event) {
        var spec = event.getConfig().getSpec();
        if (spec == SERVER_SPEC)
            SERVER_CONFIG.onReload();
    }

}