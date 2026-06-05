package stubborn.wanderingplanets.registry.provider;

import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.LanguageProvider;
import stubborn.wanderingplanets.CreateWanderingPlanets;

public class ModLangProvider extends LanguageProvider {

    public ModLangProvider(PackOutput output, String locale) {
        // 参数3传入具体的语言代码，比如 "zh_cn"
        super(output, CreateWanderingPlanets.MODID, locale);
    }

    @Override
    protected void addTranslations() {
        // 🌟 在这里集中写你的中文翻译！

        // 1. 为方块/物品添加中文 (传入你的方块对象，和中文名字)
        // 2. 为流体方块添加中文
        // add(ModFluids.GEOTHERMAL_ETCHANT.get(), "地热蚀刻液");

        add("itemGroup.wanderingplanets", "Wandering Planet");
    }
}