package me.cursedspider.cursedcape;

import io.papermc.paper.datacomponent.item.ResolvableProfile;
import net.kyori.adventure.key.Key;
import org.bukkit.plugin.java.JavaPlugin;

public class CapeProfileTest extends JavaPlugin {

    @Override
    public void onEnable() {

        Key capeKey = Key.key(
                "cursedcape",
                "capes/mycape"
        );

        ResolvableProfile.SkinPatch patch =
                ResolvableProfile.SkinPatch.skinPatch()
                        .cape(capeKey)
                        .build();

        getLogger().info(
                "Cape key: " + patch.cape()
        );

        getLogger().info(
                "CursedCape profile API test passed!"
        );
    }
}
