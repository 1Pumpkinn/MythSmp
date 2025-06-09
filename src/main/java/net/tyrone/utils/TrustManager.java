package net.tyrone.utils;

import org.bukkit.entity.Player;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class TrustManager {

    private final Map<UUID, Set<UUID>> trustedPlayers = new HashMap<>();

    public void trustPlayer(Player trustor, Player trusted) {
        UUID trustorUUID = trustor.getUniqueId();
        UUID trustedUUID = trusted.getUniqueId();

        trustedPlayers.computeIfAbsent(trustorUUID, k -> new HashSet<>()).add(trustedUUID);
    }

    public void untrustPlayer(Player trustor, Player trusted) {
        UUID trustorUUID = trustor.getUniqueId();
        UUID trustedUUID = trusted.getUniqueId();

        if (trustedPlayers.containsKey(trustorUUID)) {
            trustedPlayers.get(trustorUUID).remove(trustedUUID);
        }
    }

    public boolean isTrusted(Player trustor, Player target) {
        UUID trustorUUID = trustor.getUniqueId();
        UUID targetUUID = target.getUniqueId();

        if (!trustedPlayers.containsKey(trustorUUID)) {
            return false;
        }

        return trustedPlayers.get(trustorUUID).contains(targetUUID);
    }

    public void clearTrustedPlayers(Player player) {
        trustedPlayers.remove(player.getUniqueId());
    }

    public Set<UUID> getTrustedPlayers(Player player) {
        return trustedPlayers.getOrDefault(player.getUniqueId(), new HashSet<>());
    }
}