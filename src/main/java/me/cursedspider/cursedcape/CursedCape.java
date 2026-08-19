package me.cursedspider.cursedcape;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Transformation;
import org.joml.AxisAngle4f;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CursedCape extends JavaPlugin implements Listener {

    private final Map<UUID, ItemDisplay> capes = new HashMap<>();

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);

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
                player.sendMessage("§eUsage: /cape <set|remove|reload>");
                return true;
            }

            switch (args[0].toLowerCase()) {

                case "set" -> {
                    createCape(player);
                    player.sendMessage("§aYour CursedCape has been equipped! §5🕷");
                }

                case "remove" -> {
                    removeCape(player);
                    player.sendMessage("§cYour cape has been removed.");
                }

                case "reload" -> {
                    reloadConfig();
                    player.sendMessage("§aCursedCape reloaded.");
                }

                default -> player.sendMessage(
                        "§eUsage: /cape <set|remove|reload>"
                );
            }

            return true;
        });

        // Update cape positions
        Bukkit.getScheduler().runTaskTimer(this, () -> {

            for (Player player : Bukkit.getOnlinePlayers()) {

                ItemDisplay cape = capes.get(player.getUniqueId());

                if (cape != null && !cape.isDead()) {
                    updateCapePosition(player, cape);
                }
            }

        }, 1L, 1L);
    }

    private void createCape(Player player) {

        removeCape(player);

        ItemDisplay cape = player.getWorld().spawn(
                player.getLocation(),
                ItemDisplay.class
        );

        /*
         * Temporary item.
         *
         * Later we will connect this CustomModelData
         * to your cape PNG through the resource pack.
         */
        ItemStack item = new ItemStack(Material.PAPER);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            meta.setCustomModelData(123456);
            item.setItemMeta(meta);
        }

        cape.setItemStack(item);

        cape.setBillboard(ItemDisplay.Billboard.FIXED);

        cape.setInterpolationDuration(1);
        cape.setTeleportDuration(1);

        cape.setTransformation(
                new Transformation(
                        new Vector3f(0, 0, 0),
                        new AxisAngle4f(0, 0, 0, 1),
                        new Vector3f(1, 1, 1),
                        new AxisAngle4f(0, 0, 0, 1)
                )
        );

        cape.setPersistent(false);

        capes.put(player.getUniqueId(), cape);

        updateCapePosition(player, cape);
    }

    private void updateCapePosition(Player player, ItemDisplay cape) {

        if (cape.isDead()) {
            return;
        }

        var location = player.getLocation().clone();

        float yaw = location.getYaw();

        double radians = Math.toRadians(yaw);

        /*
         * Position behind the player.
         */
        double x = -Math.sin(radians) * 0.35;
        double z = Math.cos(radians) * 0.35;

        location.add(x, 1.35, z);

        cape.teleport(location);
    }

    private void removeCape(Player player) {

        ItemDisplay cape = capes.remove(player.getUniqueId());

        if (cape != null && !cape.isDead()) {
            cape.remove();
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {

        Player player = event.getPlayer();

        // No automatic cape for now.
        // Use /cape set
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        removeCape(event.getPlayer());
    }

    @Override
    public void onDisable() {

        for (Entity entity : capes.values()) {

            if (entity != null && !entity.isDead()) {
                entity.remove();
            }
        }

        capes.clear();
    }
          }
