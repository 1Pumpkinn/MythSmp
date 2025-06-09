package net.tyrone.abilities;

import net.tyrone.MythAbilities;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class LightningMythAbilities implements Listener {

    private final MythAbilities plugin;
    private final Map<UUID, BukkitTask> lightningStormTasks = new HashMap<>();
    private final Map<UUID, BukkitTask> lightningStrikeTasks = new HashMap<>();

    public LightningMythAbilities(MythAbilities plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public void lightningStorm(Player player) {
        // Check cooldown
        if (plugin.getCooldownManager().isOnCooldown(player, "lightning_storm")) {
            long remaining = plugin.getCooldownManager().getRemainingCooldown(player, "lightning_storm");
            player.sendMessage("§cLightning Storm is on cooldown for " + remaining + " seconds!");
            return;
        }

        // Set cooldown (3 minutes)
        plugin.getCooldownManager().setCooldown(player, "lightning_storm", 180);

        // Cancel existing storm if any
        if (lightningStormTasks.containsKey(player.getUniqueId())) {
            lightningStormTasks.get(player.getUniqueId()).cancel();
        }

        // Start lightning storm for 20 seconds
        BukkitTask stormTask = new BukkitRunnable() {
            int ticks = 0;

            @Override
            public void run() {
                if (ticks >= 400) { // 20 seconds
                    this.cancel();
                    lightningStormTasks.remove(player.getUniqueId());
                    return;
                }

                // Strike lightning every 2 seconds
                if (ticks % 40 == 0) {
                    Location loc = player.getLocation();
                    Collection<Entity> nearbyEntities = player.getWorld().getNearbyEntities(loc, 8, 8, 8);

                    for (Entity entity : nearbyEntities) {
                        if (entity instanceof Player && !entity.equals(player)) {
                            Player target = (Player) entity;
                            if (!plugin.getTrustManager().isTrusted(player, target)) {
                                // Strike lightning at target
                                target.getWorld().strikeLightning(target.getLocation());
                                target.damage(6.0, player); // 3 hearts
                                break; // Only hit one player per strike
                            }
                        }
                    }
                }

                ticks++;
            }
        }.runTaskTimer(plugin, 0, 1);

        lightningStormTasks.put(player.getUniqueId(), stormTask);
    }

    public void lightningStrikeChance(Player player) {
        // Check cooldown
        if (plugin.getCooldownManager().isOnCooldown(player, "lightning_chance")) {
            long remaining = plugin.getCooldownManager().getRemainingCooldown(player, "lightning_chance");
            player.sendMessage("§cLightning Strike Chance is on cooldown for " + remaining + " seconds!");
            return;
        }

        // Set cooldown (5 minutes)
        plugin.getCooldownManager().setCooldown(player, "lightning_chance", 300);

        // Cancel existing effect if any
        if (lightningStrikeTasks.containsKey(player.getUniqueId())) {
            lightningStrikeTasks.get(player.getUniqueId()).cancel();
        }

        // Start lightning strike chance for 45 seconds
        BukkitTask strikeTask = new BukkitRunnable() {
            int ticks = 0;

            @Override
            public void run() {
                if (ticks >= 900) { // 45 seconds
                    this.cancel();
                    lightningStrikeTasks.remove(player.getUniqueId());
                    return;
                }

                ticks++;
            }
        }.runTaskTimer(plugin, 0, 1);

        lightningStrikeTasks.put(player.getUniqueId(), strikeTask);
        player.sendMessage("§eYour attacks now have a 50% chance to strike lightning!");
    }

    @EventHandler
    public void onPlayerDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player)) return;
        if (!(event.getEntity() instanceof Player)) return;

        Player attacker = (Player) event.getDamager();
        Player victim = (Player) event.getEntity();

        // Check if attacker has lightning strike chance active
        if (lightningStrikeTasks.containsKey(attacker.getUniqueId())) {
            if (!plugin.getTrustManager().isTrusted(attacker, victim)) {
                // 50% chance to strike lightning
                if (Math.random() < 0.5) {
                    victim.getWorld().strikeLightning(victim.getLocation());
                    victim.damage(4.0, attacker); // 2 hearts additional damage
                }
            }
        }
    }
}