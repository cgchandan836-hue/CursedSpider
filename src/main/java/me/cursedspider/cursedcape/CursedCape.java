package me.cursedspider.cursedcape;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
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
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CursedCape extends JavaPlugin implements Listener {

    private final Map<UUID, ItemDisplay> capes = new HashMap<>();

    @Override
    public void onEnable() {

        getServer().getPluginManager().registerEvents(this, this);

        if (getCommand("cape") != null) {

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
                    player.sendMessage("§e/cape set <direct PNG URL>");
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

                                createCape(player, image);

                                player.sendMessage(
                                        "§aCustom cape equipped! §7🔥"
                                );
                            });

                        } catch (Exception e) {

                            getLogger().warning(
                                    "Failed to download cape: " + e.getMessage()
                            );

                            Bukkit.getScheduler().runTask(this, () ->
                                    player.sendMessage(
                                            "§cFailed to load the cape."
                                    )
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

                player.sendMessage("§e/cape set <direct PNG URL>");
                player.sendMessage("§e/cape remove");

                return true;
            });
        }

        /*
         * Update cape position every tick.
         */
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

    /*
     * Download PNG from URL.
     */
    private BufferedImage downloadImage(String url) throws Exception {

        URI uri = URI.create(url);

        HttpURLConnection connection =
                (HttpURLConnection) uri.toURL().openConnection();

        connection.setRequestMethod("GET");
        connection.setConnectTimeout(10000);
        connection.setReadTimeout(10000);
        connection.setRequestProperty(
                "User-Agent",
                "CursedCape/1.0"
        );

        int response = connection.getResponseCode();

        if (response != 200) {
            connection.disconnect();
            return null;
        }

        try (InputStream input = connection.getInputStream()) {

            BufferedImage image = ImageIO.read(input);

            connection.disconnect();

            return image;
        }
    }

    /*
     * Create the cape entity.
     */
    private void createCape(Player player, BufferedImage image) {

        removeCape(player);

        World world = player.getWorld();

        ItemDisplay cape = world.spawn(
                player.getLocation(),
                ItemDisplay.class
        );

        /*
         * Minecraft maps can display 128x128 pixels.
         * Resize the cape image to fit the map.
         */
        BufferedImage resized = resizeImage(image, 128, 128);

        MapView mapView = Bukkit.createMap(world);

        /*
         * Remove default map renderers.
         */
        for (MapRenderer renderer : mapView.getRenderers()) {
            mapView.removeRenderer(renderer);
        }

        /*
         * Add our custom image renderer.
         */
        mapView.addRenderer(new CapeRenderer(resized));

        ItemStack mapItem = new ItemStack(
                org.bukkit.Material.FILLED_MAP
        );

        MapMeta mapMeta = (MapMeta) mapItem.getItemMeta();

        if (mapMeta != null) {

            mapMeta.setMapView(mapView);

            mapItem.setItemMeta(mapMeta);
        }

        cape.setItemStack(mapItem);

        cape.setBillboard(ItemDisplay.Billboard.FIXED);

        cape.setInterpolationDuration(1);
        cape.setTeleportDuration(1);

        /*
         * Make the map behave like a flat cape.
         */
        cape.setTransformation(
                new Transformation(
                        new Vector3f(0f, 0f, 0f),
                        new AxisAngle4f(
                                (float) Math.toRadians(90),
                                1f,
                                0f,
                                0f
                        ),
                        new Vector3f(1.5f, 1.5f, 1.5f),
                        new AxisAngle4f(
                                0f,
                                0f,
                                0f,
                                1f
                        )
                )
        );

        cape.setPersistent(false);

        capes.put(
                player.getUniqueId(),
                cape
        );

        updateCapePosition(player, cape);
    }

    /*
     * Resize image while keeping it simple for the map renderer.
     */
    private BufferedImage resizeImage(
            BufferedImage original,
            int width,
            int height
    ) {

        BufferedImage resized = new BufferedImage(
                width,
                height,
                BufferedImage.TYPE_INT_ARGB
        );

        Graphics2D graphics = resized.createGraphics();

        graphics.drawImage(
                original.getScaledInstance(
                        width,
                        height,
                        Image.SCALE_SMOOTH
                ),
                0,
                0,
                null
        );

        graphics.dispose();

        return resized;
    }

    /*
     * Keep cape behind the player.
     */
    private void updateCapePosition(
            Player player,
            ItemDisplay cape
    ) {

        if (cape.isDead()) {
            return;
        }

        var location = player.getLocation().clone();

        float yaw = location.getYaw();

        double radians = Math.toRadians(yaw);

        double x = -Math.sin(radians) * 0.45;
        double z = Math.cos(radians) * 0.45;

        location.add(
                x,
                1.25,
                z
        );

        cape.teleport(location);
    }

    /*
     * Remove player's cape.
     */
    private void removeCape(Player player) {

        ItemDisplay cape =
                capes.remove(player.getUniqueId());

        if (cape != null && !cape.isDead()) {
            cape.remove();
        }
    }

    /*
     * Remove cape when player leaves.
     */
    @EventHandler
    public void onQuit(PlayerQuitEvent event) {

        removeCape(event.getPlayer());
    }

    /*
     * Custom map renderer.
     */
    private static class CapeRenderer extends MapRenderer {

        private final BufferedImage image;

        private CapeRenderer(BufferedImage image) {

            super(false);

            this.image = image;
        }

        @Override
        public void render(
                MapView map,
                MapCanvas canvas,
                Player player
        ) {

            canvas.drawImage(
                    0,
                    0,
                    image
            );
        }
    }

    @Override
    public void onDisable() {

        for (ItemDisplay cape : capes.values()) {

            if (cape != null && !cape.isDead()) {
                cape.remove();
            }
        }

        capes.clear();

        getLogger().info("CursedCape disabled.");
    }
            }
