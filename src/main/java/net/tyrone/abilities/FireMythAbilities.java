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
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class FireMythAbilities implements Listener {

    private final MythAbilities plugin;
    private final Map<UUID, BukkitTask> fireRainTasks = new HashMap<>();
    private final Map<Fireball, Player> fireballOwners = new HashMap<>();

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

        // Launch fireball
        Fireball fireball = player.launchProjectile(Fireball.class);
        fireball.setYield(0); // No block damage
        fireballOwners.put(fireball, player);

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
                    player.sendMessage("§eYour fire rain has stopped");
                    return;
                }

                // Rain fire every second in 10 block radius
                if (ticks % 20 == 0) {
                    Location center = player.getLocation();

                    // Create multiple fire spots
                    for (int i = 0; i < 5; i++) {
                        double x = center.getX() + (Math.random() - 0.5) * 20; // 10 block radius
                        double z = center.getZ() + (Math.random() - 0.5) * 20;
                        double y = center.getY() + 15; // Start high

                        Location fireLocation = new Location(center.getWorld(), x, y, z);

                        // Find ground level
                        while (fireLocation.getY() > 0 && fireLocation.getBlock().getType() == Material.AIR) {
                            fireLocation.subtract(0, 1, 0);
                        }
                        fireLocation.add(0, 1, 0); // One block above ground


                        // Damage nearby players
                        Collection<Entity> nearby = fireLocation.getWorld().getNearbyEntities(fireLocation, 2, 2, 2);
                        for (Entity entity : nearby) {
                            if (entity instanceof Player && !entity.equals(player)) {
                                Player target = (Player) entity;
                                if (!plugin.getTrustManager().isTrusted(player, target)) {
                                    target.damage(3.0, player); // 1.5 hearts
                                    target.setFireTicks(60); // 3 seconds of fire
                                }
                            }
                        }
                    }
                }

                ticks++;
            }
        }.runTaskTimer(plugin, 0, 1);

        fireRainTasks.put(player.getUniqueId(), rainTask);
    }

    @EventHandler
    public void onFireballHit(ProjectileHitEvent event) {
        if (!(event.getEntity() instanceof Fireball)) return;

        Fireball fireball = (Fireball) event.getEntity();
        Player owner = fireballOwners.get(fireball);

        if (owner == null) return;

        Location hitLocation = fireball.getLocation();

        // Damage nearby players
        Collection<Entity> nearby = hitLocation.getWorld().getNearbyEntities(hitLocation, 3, 3, 3);
        for (Entity entity : nearby) {
            if (entity instanceof Player && !entity.equals(owner)) {
                Player target = (Player) entity;
                if (!plugin.getTrustManager().isTrusted(owner, target)) {
                    target.damage(4.0, owner); // 2 hearts
                    target.setFireTicks(100); // 5 seconds of fire
                }
            }
        }

        // Effects
        hitLocation.getWorld().spawnParticle(Particle.EXPLOSION, hitLocation, 10, 2, 2, 2, 0.1);

        fireballOwners.remove(fireball);
    }
}