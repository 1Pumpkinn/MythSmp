package net.tyrone.utils;

import org.bukkit.entity.Player;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CooldownManager {

    private final Map<UUID, Map<String, Long>> cooldowns = new HashMap<>();

    public boolean isOnCooldown(Player player, String ability) {
        UUID uuid = player.getUniqueId();
        if (!cooldowns.containsKey(uuid)) {
            return false;
        }

        Map<String, Long> playerCooldowns = cooldowns.get(uuid);
        if (!playerCooldowns.containsKey(ability)) {
            return false;
        }

        return System.currentTimeMillis() < playerCooldowns.get(ability);
    }

    public void setCooldown(Player player, String ability, long cooldownTimeInSeconds) {
        UUID uuid = player.getUniqueId();
        long endTime = System.currentTimeMillis() + (cooldownTimeInSeconds * 1000);

        cooldowns.computeIfAbsent(uuid, k -> new HashMap<>()).put(ability, endTime);
    }

    public long getRemainingCooldown(Player player, String ability) {
        UUID uuid = player.getUniqueId();
        if (!cooldowns.containsKey(uuid) || !cooldowns.get(uuid).containsKey(ability)) {
            return 0;
        }

        long endTime = cooldowns.get(uuid).get(ability);
        long remaining = endTime - System.currentTimeMillis();
        return Math.max(0, remaining / 1000);
    }

    public void removeCooldown(Player player, String ability) {
        UUID uuid = player.getUniqueId();
        if (cooldowns.containsKey(uuid)) {
            cooldowns.get(uuid).remove(ability);
        }
    }

    public void clearAllCooldowns(Player player) {
        cooldowns.remove(player.getUniqueId());
    }
}