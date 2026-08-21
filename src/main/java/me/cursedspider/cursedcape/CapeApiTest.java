package me.cursedspider.cursedcape;

import io.papermc.paper.datacomponent.item.ResolvableProfile;
import net.kyori.adventure.key.Key;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class CapeApiTest extends JavaPlugin {

    public void testCapeApi(Player player) {

        Key capeKey = Key.key(
                "cursedcape",
                "capes/mycape"
        );

        ResolvableProfile.SkinPatch patch =
                ResolvableProfile.SkinPatch.skinPatch()
                        .cape(capeKey)
                        .build();

        getLogger().info(
                "Cape API test compiled successfully: "
                        + patch.cape()
        );
    }
}
