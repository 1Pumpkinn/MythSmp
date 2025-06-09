package net.tyrone.abilities;

import net.tyrone.MythAbilities;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class EarthMythAbilities implements Listener {

    private final MythAbilities plugin;
    private final Map<UUID, BukkitTask> poisonAuraTasks = new HashMap<>();
    private final Map<UUID, Item> thrownRocks = new HashMap<>();

    public EarthMythAbilities(MythAbilities plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public void throwRock(Player player) {
        // Check cooldown
        if (plugin.getCooldownManager().isOnCooldown(player, "rock_throw")) {
            long remaining = plugin.getCooldownManager().getRemainingCooldown(player, "rock_throw");
            player.sendMessage("§cRock Throw is on cooldown for " + remaining + " seconds!");
            return;
        }

        // Set cooldown (1 minute 30 seconds)
        plugin.getCooldownManager().setCooldown(player, "rock_throw", 90);

        // Create and throw rock
        ItemStack rock = new ItemStack(Material.COBBLESTONE);
        Item thrownRock = player.getWorld().dropItem(player.getEyeLocation(), rock);

        // Set velocity
        Vector direction = player.getLocation().getDirection();
        direction.multiply(2);
        thrownRock.setVelocity(direction);

        // Track the rock
        thrownRocks.put(player.getUniqueId(), thrownRock);

        // Check for hits every tick
        new BukkitRunnable() {
            int ticks = 0;

            @Override
            public void run() {
                if (ticks >= 100 || thrownRock.isDead()) { // 5 seconds max or rock is gone
                    thrownRocks.remove(player.getUniqueId());
                    if (!thrownRock.isDead()) {
                        thrownRock.remove();
                    }
                    this.cancel();
                    return;
                }

                // Check for nearby players
                Collection<Entity> nearby = thrownRock.getNearbyEntities(1, 1, 1);
                for (Entity entity : nearby) {
                    if (entity instanceof Player && !entity.equals(player)) {
                        Player target = (Player) entity;
                        if (!plugin.getTrustManager().isTrusted(player, target)) {
                            // Deal 2 hearts (4 damage)
                            target.damage(4.0, player);

                            // Effects
                            target.getWorld().spawnParticle(Particle.BLOCK, target.getLocation(), 20,
                                    1, 1, 1, 0.1, Material.STONE.createBlockData());
                            target.getWorld().playSound(target.getLocation(), Sound.BLOCK_STONE_BREAK, 1.0f, 0.8f);

                            // Remove rock
                            thrownRock.remove();
                            thrownRocks.remove(player.getUniqueId());
                            this.cancel();
                            return;
                        }
                    }
                }

                ticks++;
            }
        }.runTaskTimer(plugin, 0, 1);

    }

    public void poisonAura(Player player) {
        // Check cooldown
        if (plugin.getCooldownManager().isOnCooldown(player, "poison_aura")) {
            long remaining = plugin.getCooldownManager().getRemainingCooldown(player, "poison_aura");
            player.sendMessage("§cPoison Aura is on cooldown for " + remaining + " seconds!");
            return;
        }

        // Set cooldown (3 minutes)
        plugin.getCooldownManager().setCooldown(player, "poison_aura", 180);

        // Cancel existing aura if any
        if (poisonAuraTasks.containsKey(player.getUniqueId())) {
            poisonAuraTasks.get(player.getUniqueId()).cancel();
        }

        // Start poison aura for 30 seconds
        BukkitTask auraTask = new BukkitRunnable() {
            int ticks = 0;

            @Override
            public void run() {
                if (ticks >= 600) { // 30 seconds
                    this.cancel();
                    poisonAuraTasks.remove(player.getUniqueId());
                    return;
                }

                // Apply poison every 2 seconds
                if (ticks % 40 == 0) {
                    Location loc = player.getLocation();
                    Collection<Entity> nearbyEntities = player.getWorld().getNearbyEntities(loc, 5, 5, 5);

                    // Create poison particles
                    loc.getWorld().spawnParticle(Particle.ITEM, loc, 10, 2, 1, 2, 0.1,
                            new ItemStack(Material.SPIDER_EYE));

                    for (Entity entity : nearbyEntities) {
                        if (entity instanceof Player && !entity.equals(player)) {
                            Player target = (Player) entity;
                            if (!plugin.getTrustManager().isTrusted(player, target)) {
                                // Apply poison effect (level 2 for 3 seconds)
                                target.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 60, 1));
                            }
                        }
                    }
                }

                ticks++;
            }
        }.runTaskTimer(plugin, 0, 1);

        poisonAuraTasks.put(player.getUniqueId(), auraTask);
    }
}