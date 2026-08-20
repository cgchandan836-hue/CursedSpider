package me.cursedspider.cursedcape;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Transformation;
import org.joml.AxisAngle4f;
import org.joml.Vector3f;

import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CursedCape extends JavaPlugin implements Listener {

    private final Map<UUID, ItemDisplay> capes = new HashMap<>();
    private final Map<UUID, String> capeUrls = new HashMap<>();

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
                player.sendMessage("§e/cape set <URL>");
                player.sendMessage("§e/cape remove");
                return true;
            }

            if (args[0].equalsIgnoreCase("set")) {

                if (args.length < 2) {
                    player.sendMessage("§cUsage: /cape set <direct PNG URL>");
                    return true;
                }

                String url = args[1];

                if (!url.startsWith("https://")) {
                    player.sendMessage("§cThe cape URL must use HTTPS.");
                    return true;
                }

                player.sendMessage("§eDownloading cape...");

                Bukkit.getScheduler().runTaskAsynchronously(this, () -> {

                    try {

                        BufferedImage image = downloadImage(url);

                        if (image == null) {
                            Bukkit.getScheduler().runTask(this, () ->
                                    player.sendMessage("§cCouldn't download that image.")
                            );
                            return;
                        }

                        Bukkit.getScheduler().runTask(this, () -> {

                            capeUrls.put(player.getUniqueId(), url);

                            createCape(player, image);

                            player.sendMessage("§aYour custom cape has been equipped! §7🔥");

                        });

                    } catch (Exception e) {

                        getLogger().warning("Failed to load cape: " + e.getMessage());

                        Bukkit.getScheduler().runTask(this, () ->
                                player.sendMessage("§cFailed to load the cape.")
                        );
                    }
                });

                return true;
            }

            if (args[0].equalsIgnoreCase("remove")) {

                removeCape(player);

                player.sendMessage("§aYour cape has been removed.");

                return true;
            }

            player.sendMessage("§e/cape set <URL>");
            player.sendMessage("§e/cape remove");

            return true;
        });

        // Update cape position
        Bukkit.getScheduler().runTaskTimer(this, () -> {

            for (Player player : Bukkit.getOnlinePlayers()) {

                ItemDisplay cape = capes.get(player.getUniqueId());

                if (cape != null && !cape.isDead()) {
                    updateCapePosition(player, cape);
                }
            }

        }, 1L, 1L);

        getLogger().info("CursedCape enabled!");
    }

    private BufferedImage downloadImage(String url) throws Exception {

        URI uri = URI.create(url);

        HttpURLConnection connection =
                (HttpURLConnection) uri.toURL().openConnection();

        connection.setRequestMethod("GET");
        connection.setConnectTimeout(10000);
        connection.setReadTimeout(10000);
        connection.setRequestProperty("User-Agent", "CursedCape/1.0");

        if (connection.getResponseCode() != 200) {
            connection.disconnect();
            return null;
        }

        try (InputStream input = connection.getInputStream()) {

            BufferedImage image = ImageIO.read(input);

            connection.disconnect();

            return image;
        }
    }

    private void createCape(Player player, BufferedImage image) {

        removeCape(player);

        World world = player.getWorld();

        ItemDisplay cape = world.spawn(
                player.getLocation(),
                ItemDisplay.class
        );

        // Convert image to Minecraft map
        MapView mapView = Bukkit.createMap(world);

        mapView.getRenderers().forEach(mapView::removeRenderer);

        mapView.addRenderer(new CapeRenderer(image));

        ItemStack mapItem = new ItemStack(Material.FILLED_MAP);

        MapMeta mapMeta = (MapMeta) mapItem.getItemMeta();

        if (mapMeta != null) {
            mapMeta.setMapView(mapView);
            mapItem.setItemMeta(mapMeta);
        }

        cape.setItemStack(mapItem);

        cape.setBillboard(ItemDisplay.Billboard.FIXED);

        cape.setInterpolationDuration(1);
        cape.setTeleportDuration(1);

        cape.setTransformation(
                new Transformation(
                        new Vector3f(0f, 0f, 0f),
                        new AxisAngle4f(0f, 0f, 0f, 1f),
                        new Vector3f(1.5f, 1.5f, 1.5f),
                        new AxisAngle4f(0f, 0f, 0f, 1f)
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

        // Position behind the player
        double x = -Math.sin(radians) * 0.35;
        double z = Math.cos(radians) * 0.35;

        location.add(x, 1.35, z);

        cape.teleport(location);
    }

    private void removeCape(Player player) {

        ItemDisplay cape = capes.remove(player.getUniqueId());

        if (cape != null
