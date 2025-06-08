package net.tyrone.listeners;

import net.tyrone.MythAbilities;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerListener implements Listener {

    private final MythAbilities plugin;

    public PlayerListener(MythAbilities plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        // Optional: Welcome message or myth status check
        // You can add custom logic here if needed
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        // Clean up any ongoing tasks or effects when player leaves
        plugin.getCooldownManager().clearAllCooldowns(event.getPlayer());
        plugin.getTrustManager().clearTrustedPlayers(event.getPlayer());
    }
}