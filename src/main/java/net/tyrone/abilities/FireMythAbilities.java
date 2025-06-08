package net.tyrone.abilities;

import net.tyrone.MythAbilities;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public class FireMythAbilities implements Listener {

    private final net.tyrone.MythAbilities plugin;
    private final Map<UUID, BukkitTask> fireRainTasks = new HashMap<>();
    private final Map<UUID, Fireball> mythFireballs = new HashMap<>();
    private final Random random = new Random();

    public FireMythAbilities(MythAbilities plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public void fireShot(Player player) {
        // Check cooldown
        if (plugin.getCooldownManager().isOnCooldown(player, "fire_shot")) {
            long remaining = plugin.getCooldownManager().getRemainingCooldown(player, "fire_shot");
            player.sendMessage("§cFire Shot is on cooldown for " + remaining + " seconds!");
            return;
        }

        // Set cooldown (3 minutes)
        plugin.getCooldownManager().setCooldown(player, "fire_shot", 180);

        // Shoot fireball
        Fireball fireball = player.launchProjectile(Fireball.class);
        fireball.setIsIncendiary(false); // Don't set blocks on fire
        fireball.setYield(0); // No explosion damage to blocks
        mythFireballs.put(player.getUniqueId(), fireball);

        // Remove after 5 seconds if it hasn't hit anything
        new BukkitRunnable() {
            @Override
            public void run() {
                if (mythFireballs.containsValue(fireball)) {
                    fireball.remove();
                    mythFireballs.remove(player.getUniqueId());
                }
            }
        }.runTaskLater(plugin, 100); // 5 seconds

        player.sendMessage("§cYou shoot a concentrated fire blast!");
    }

    public void fireRain(Player player) {
        // Check cooldown
        if (plugin.getCooldownManager().isOnCooldown(player, "fire_rain")) {
            long remaining = plugin.getCooldownManager().getRemainingCooldown(player, "fire_rain");
            player.sendMessage("§cFire Rain is on cooldown for " + remaining + " seconds!");
            return;
        }

        // Set cooldown (4 minutes)
        plugin.getCooldownManager().setCooldown(player, "fire_rain", 240);

        // Cancel existing fire rain if any
        if (fireRainTasks.containsKey(player.getUniqueId())) {
            fireRainTasks.get(player.getUniqueId()).cancel();
        }

        // Start fire rain for 30 seconds
        BukkitTask rainTask = new BukkitRunnable() {
            int ticks = 0;

            @Override
            public void run() {
                if (ticks >= 600) { // 30 seconds
                    this.cancel();
                    fireRainTasks.remove(player.getUniqueId());
                    player.sendMessage("§eThe fire rain stops falling...");
                    return;
                }

                // Rain fire every second
                if (ticks % 20 == 0) {
                    Location center = player.getLocation();

                    // Create multiple fire drops in 10 block radius
                    for (int i = 0; i < 5; i++) {
                        double x = center.getX() + (random.nextDouble() - 0.5) * 20;
                        double z = center.getZ() + (random.nextDouble() - 0.5) * 20;
                        double y = center.getY() + 15;

                        Location dropLocation = new Location(center.getWorld(), x, y, z);

                        // Create fire effect
                        dropLocation.getWorld().spawnParticle(Particle.FLAME, dropLocation, 10, 0.5, 2, 0.5, 0.1);
                        dropLocation.getWorld().playSound(dropLocation, Sound.BLOCK_FIRE_AMBIENT, 0.5f, 1.0f);

                        // Check for players in the area
                        Collection<Entity> nearbyEntities = dropLocation.getWorld().getNearbyEntities(dropLocation, 2, 20, 2);
                        for (Entity entity : nearbyEntities) {
                            if (entity instanceof Player && !entity.equals(player)) {
                                Player target = (Player) entity;
                                if (!plugin.getTrustManager().isTrusted(player, target)) {
                                    // Deal 3 hearts (6 damage) and set on fire
                                    target.damage(6.0, player);
                                    target.setFireTicks(60); // 3 seconds on fire
                                    target.sendMessage("§c" + player.getName() + "'s fire rain burns you!");
                                }
                            }
                        }
                    }
                }

                ticks++;
            }
        }.runTaskTimer(plugin, 0, 1);

        fireRainTasks.put(player.getUniqueId(), rainTask);
        player.sendMessage("§cYou summon a devastating fire rain!");
    }

    @EventHandler
    public void onFireballHit(ProjectileHitEvent event) {
        if (!(event.getEntity() instanceof Fireball)) return;
        if (!(event.getEntity().getShooter() instanceof Player)) return;

        Fireball fireball = (Fireball) event.getEntity();
        Player shooter = (Player) fireball.getShooter();

        // Check if this is a myth fireball
        if (!mythFireballs.containsValue(fireball)) return;

        // Remove from tracking
        mythFireballs.entrySet().removeIf(entry -> entry.getValue().equals(fireball));

        Location hitLocation = fireball.getLocation();

        // Check if it hit a player
        if (event.getHitEntity() instanceof Player) {
            Player target = (Player) event.getHitEntity();
            if (!plugin.getTrustManager().isTrusted(shooter, target)) {
                // Deal 2 hearts (4 damage) and set on fire
                target.damage(4.0, shooter);
                target.setFireTicks(100); // 5 seconds on fire
                target.sendMessage("§c" + shooter.getName() + "'s fire blast burns you!");
                shooter.sendMessage("§cYour fire blast hits " + target.getName() + "!");
            }
        }

        // Create explosion effect
        hitLocation.getWorld().spawnParticle(Particle.FLAME, hitLocation, 20, 2, 2, 2, 0.2);
        hitLocation.getWorld().playSound(hitLocation, Sound.ENTITY_GENERIC_EXPLODE, 1.0f, 1.5f);
    }
}