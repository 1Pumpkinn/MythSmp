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
import java.util.Random;
import java.util.UUID;

public class LightningMythAbilities implements Listener {

    private final MythAbilities plugin;
    private final Map<UUID, BukkitTask> lightningStormTasks = new HashMap<>();
    private final Map<UUID, BukkitTask> lightningStrikeTasks = new HashMap<>();
    private final Random random = new Random();

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
                    player.sendMessage("§eYour lightning storm subsides...");
                    return;
                }

                // Strike lightning every 2 seconds
                if (ticks % 40 == 0) {
                    Location loc = player.getLocation();
                    Collection<Entity> nearbyEntities = player.getWorld().getNearbyEntities(loc, 5, 5, 5);

                    for (Entity entity : nearbyEntities) {
                        if (entity instanceof Player && !entity.equals(player)) {
                            Player target = (Player) entity;
                            if (!plugin.getTrustManager().isTrusted(player, target)) {
                                target.getWorld().strikeLightning(target.getLocation());
                                target.sendMessage("§6You are struck by " + player.getName() + "'s lightning storm!");
                            }
                        }
                    }
                }

                ticks++;
            }
        }.runTaskTimer(plugin, 0, 1);

        lightningStormTasks.put(player.getUniqueId(), stormTask);
        player.sendMessage("§6You summon a devastating lightning storm!");
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

        // Active for 45 seconds
        BukkitTask strikeTask = new BukkitRunnable() {
            @Override
            public void run() {
                lightningStrikeTasks.remove(player.getUniqueId());
                player.sendMessage("§eYour lightning strikes no longer crackle with power...");
            }
        }.runTaskLater(plugin, 900); // 45 seconds

        lightningStrikeTasks.put(player.getUniqueId(), strikeTask);
        player.sendMessage("§6Your attacks now have a chance to strike with lightning!");
    }

    @EventHandler
    public void onPlayerDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player)) return;
        if (!(event.getEntity() instanceof Player)) return;

        Player attacker = (Player) event.getDamager();
        Player victim = (Player) event.getEntity();

        // Check if attacker has lightning strike chance active
        if (!lightningStrikeTasks.containsKey(attacker.getUniqueId())) return;

        // Check if victim is trusted
        if (plugin.getTrustManager().isTrusted(attacker, victim)) return;

        // 50% chance to strike lightning
        if (random.nextBoolean()) {
            victim.getWorld().strikeLightning(victim.getLocation());
            victim.sendMessage("§6" + attacker.getName() + "'s strike calls down lightning!");
            attacker.sendMessage("§6Your strike calls down lightning upon " + victim.getName() + "!");
        }
    }
}