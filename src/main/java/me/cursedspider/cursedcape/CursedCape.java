package me.cursedspider.cursedcape;

import io.papermc.paper.datacomponent.item.ResolvableProfile;
import net.kyori.adventure.key.Key;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class CapeProfileTest extends JavaPlugin {

    public void applyTestCape(Player player) {

        Key capeKey = Key.key(
                "cursedcape",
                "capes/mycape"
        );

        ResolvableProfile.SkinPatch skinPatch =
                ResolvableProfile.SkinPatch.skinPatch()
                        .cape(capeKey)
                        .build();

        getLogger().info(
                "Cape key created: " + skinPatch.cape()
        );

        getLogger().info(
                "Player profile cape test prepared successfully."
        );
    }
}
