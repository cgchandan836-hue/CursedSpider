package me.cursedspider.cursedcape;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.profile.PlayerProfile;

public class CursedCape extends JavaPlugin {

    private NamespacedKey capeKey;

    @Override
    public void onEnable() {
        capeKey = new NamespacedKey(this, "capes/mycape");

        getCommand("cape").setExecutor((sender, command, label, args) -> {

            if (!(sender instanceof Player player)) {
                sender.sendMessage("Only players can use this command.");
                return true;
            }

            if (!player.hasPermission("cursedcape.use")) {
                player.sendMessage("§cYou don't have permission.");
                return true;
            }

            if (args.length == 0) {
                player.sendMessage("§e/cape set");
                player.sendMessage("§e/cape remove");
                return true;
            }

            if (args[0].equalsIgnoreCase("set")) {
                applyCape(player);
                player.sendMessage("§aCursedSpider Cape equipped! §7🕷");
                return true;
            }

            if (args[0].equalsIgnoreCase("remove")) {
                removeCape(player);
                player.sendMessage("§aCursedSpider Cape removed.");
                return true;
            }

            player.sendMessage("§e/cape set");
            player.sendMessage("§e/cape remove");
            return true;
        });

        getLogger().info("CursedCape enabled!");
    }

    private void applyCape(Player player) {
        PlayerProfile profile = player.getPlayerProfile();

        var patch = profile.getSkinPatch();
        patch.setCapeTexturePatch(capeKey);
        profile.setSkinPatch(patch);

        player.setPlayerProfile(profile);
    }

    private void removeCape(Player player) {
        PlayerProfile profile = player.getPlayerProfile();

        var patch = profile.getSkinPatch();
        patch.setCapeTexturePatch(null);
        profile.setSkinPatch(patch);

        player.setPlayerProfile(profile);
    }

    @Override
    public void onDisable() {
        getLogger().info("CursedCape disabled.");
    }
}
