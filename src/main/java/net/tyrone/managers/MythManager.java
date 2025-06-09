package net.tyrone.managers;

import net.tyrone.MythAbilities;
import net.tyrone.enums.MythType;
import org.bukkit.entity.Player;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MythManager {

    private final MythAbilities plugin;
    private final Map<UUID, MythType> playerMyths = new HashMap<>();

    public MythManager(MythAbilities plugin) {
        this.plugin = plugin;
    }

    public void setPlayerMyth(Player player, MythType mythType) {
        playerMyths.put(player.getUniqueId(), mythType);
    }

    public MythType getPlayerMyth(Player player) {
        return playerMyths.get(player.getUniqueId());
    }

    public boolean hasMyth(Player player) {
        return playerMyths.containsKey(player.getUniqueId());
    }

    public void removePlayerMyth(Player player) {
        playerMyths.remove(player.getUniqueId());
    }

    public void clearAllMyths() {
        playerMyths.clear();
    }
}