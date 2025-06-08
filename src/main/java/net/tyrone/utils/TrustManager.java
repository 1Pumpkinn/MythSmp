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

    public void untrustPlayer(Player trustor, Player untrusted) {
        UUID trustorUUID = trustor.getUniqueId();
        UUID untrustedUUID = untrusted.getUniqueId();

        if (trustedPlayers.containsKey(trustorUUID)) {
            trustedPlayers.get(trustorUUID).remove(untrustedUUID);
        }
    }

    public boolean isTrusted(Player trustor, Player target) {
        UUID trustorUUID = trustor.getUniqueId();
        UUID targetUUID = target.getUniqueId();

        // Players trust themselves
        if (trustorUUID.equals(targetUUID)) {
            return true;
        }

        return trustedPlayers.containsKey(trustorUUID) &&
                trustedPlayers.get(trustorUUID).contains(targetUUID);
    }

    public Set<UUID> getTrustedPlayers(Player player) {
        return trustedPlayers.getOrDefault(player.getUniqueId(), new HashSet<>());
    }

    public void clearTrustedPlayers(Player player) {
        trustedPlayers.remove(player.getUniqueId());
    }
}