package stubborn.wanderingplanets;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.data.DatapackBuiltinEntriesProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import stubborn.wanderingplanets.registry.provider.ModBlockTagsProvider;
import stubborn.wanderingplanets.registry.provider.ModDamageTypesProvider;
import stubborn.wanderingplanets.registry.provider.ModItemTagsProvider;
import stubborn.wanderingplanets.registry.provider.ModLangProvider;

import java.util.Set;
import java.util.concurrent.CompletableFuture;

import static stubborn.wanderingplanets.CreateWanderingPlanets.MODID;

@EventBusSubscriber(modid = MODID)
public class ModDataGenHandler {
    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        PackOutput output = generator.getPackOutput();
        CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();
        ExistingFileHelper existingFileHelper = event.getExistingFileHelper();


        // 🌟 注册方块标签生成器
        // event.includeServer() 代表这些数据是服务端/数据包（Datapack）层面的，Tag 属于这一类
        ModBlockTagsProvider blockTagsProvider = new ModBlockTagsProvider(output, lookupProvider, existingFileHelper);
        generator.addProvider(event.includeServer(), blockTagsProvider);

        // 2. 🌟 关键：在实例化物品标签时，将方块标签的 contentsGetter() 作为第三个参数传进去！
        generator.addProvider(event.includeServer(), new ModItemTagsProvider(
                output,
                lookupProvider,
                blockTagsProvider.contentsGetter(), // 就是这一步，把方块标签查找器传给物品标签
                existingFileHelper
        ));

        generator.addProvider(
                event.includeServer(),
                new ModLangProvider(output,"en_us")
        );

        RegistrySetBuilder builder = new RegistrySetBuilder()
                .add(Registries.DAMAGE_TYPE, ModDamageTypesProvider::bootstrap);

        event.getGenerator().addProvider(event.includeServer(),
                new DatapackBuiltinEntriesProvider(output, lookupProvider, builder, Set.of(MODID)));
        // 💡 以后如果你还要生成 Item Tags、Recipe（配方）、LootTable（掉落物），也是用同样的方式加在这里：
        // generator.addProvider(event.includeServer(), new ModRecipeProvider(output, lookupProvider));
    }
}
