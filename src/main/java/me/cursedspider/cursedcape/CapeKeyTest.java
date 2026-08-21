package me.cursedspider.cursedcape;

import net.kyori.adventure.key.Key;
import org.bukkit.plugin.java.JavaPlugin;

public class CapeKeyTest extends JavaPlugin {

    @Override
    public void onEnable() {

        Key capeKey = Key.key(
                "cursedcape",
                "capes/mycape"
        );

        getLogger().info("Cape namespace: " + capeKey.namespace());
        getLogger().info("Cape path: " + capeKey.value());
        getLogger().info("Cape key: " + capeKey.asString());
        getLogger().info("CursedCape texture key test passed!");
    }
}
