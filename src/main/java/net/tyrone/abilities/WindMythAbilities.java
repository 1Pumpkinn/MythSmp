package net.tyrone.abilities;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import java.util.Collection;

public class WindMythAbilities {

    private final net.tyrone.MythAbilities plugin;

    public WindMythAbilities(net.tyrone.MythAbilities plugin) {
        this.plugin = plugin;
    }

    public void airDash(Player player) {
        // Check cooldown
        if (plugin.getCooldownManager().isOnCooldown(player, "wind_dash")) {
            long remaining = plugin.getCooldownManager().getRemainingCooldown(player, "wind_dash");
            player.sendMessage("§cAir Dash is on cooldown for " + remaining + " seconds!");
            return;
        }

        // Set cooldown (30 seconds)
        plugin.getCooldownManager().setCooldown(player, "wind_dash", 30);

        // Get direction player is looking
        Vector direction = player.getLocation().getDirection();
        direction.setY(0.8); // Add upward momentum
        direction.normalize();
        direction.multiply(10); // 10 blocks distance

        // Apply velocity
        player.setVelocity(direction);

        // No fall damage for 3 seconds
        new BukkitRunnable() {
            @Override
            public void run() {
                player.setFallDistance(0);
            }
        }.runTaskTimer(plugin, 0, 1);

        new BukkitRunnable() {
            @Override
            public void run() {
                this.cancel();
            }
        }.runTaskLater(plugin, 60); // 3 seconds

        // Effects
        player.getWorld().spawnParticle(Particle.CLOUD, player.getLocation(), 30, 1, 1, 1, 0.1);

    }

    public void groundSmash(Player player) {
        // Check cooldown
        if (plugin.getCooldownManager().isOnCooldown(player, "wind_smash")) {
            long remaining = plugin.getCooldownManager().getRemainingCooldown(player, "wind_smash");
            player.sendMessage("§cGround Smash is on cooldown for " + remaining + " seconds!");
            return;
        }

        // Set cooldown (3 minutes)
        plugin.getCooldownManager().setCooldown(player, "wind_smash", 180);

        // Dash up first
        Vector upward = new Vector(0, 5, 0);
        player.setVelocity(upward);

        // After 1.5 seconds, smash down
        new BukkitRunnable() {
            @Override
            public void run() {
                Vector downward = new Vector(0, -10, 0);
                player.setVelocity(downward);

                // Check for ground impact
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        if (player.isOnGround()) {
                            performGroundSmash(player);
                            this.cancel();
                        }
                    }
                }.runTaskTimer(plugin, 0, 1);
            }
        }.runTaskLater(plugin, 30); // 1.5 seconds

    }

    private void performGroundSmash(Player player) {
        Location loc = player.getLocation();

        // Effects
        player.getWorld().playSound(loc, Sound.ENTITY_GENERIC_EXPLODE, 2.0f, 0.5f);
        player.getWorld().spawnParticle(Particle.EXPLOSION, loc, 5, 2, 1, 2, 0);
        player.getWorld().spawnParticle(Particle.CLOUD, loc, 50, 3, 1, 3, 0.2);

        // Damage and knockback nearby players
        Collection<Entity> nearbyEntities = player.getWorld().getNearbyEntities(loc, 5, 5, 5);
        for (Entity entity : nearbyEntities) {
            if (entity instanceof Player && !entity.equals(player)) {
                Player target = (Player) entity;
                if (!plugin.getTrustManager().isTrusted(player, target)) {
                    // Deal 4 hearts (8 damage)
                    target.damage(8.0, player);

                    // Knockback
                    Vector knockback = target.getLocation().toVector().subtract(loc.toVector());
                    knockback.setY(0.5);
                    knockback.normalize();
                    knockback.multiply(2);
                    target.setVelocity(knockback);

                }
            }
        }

    }
}