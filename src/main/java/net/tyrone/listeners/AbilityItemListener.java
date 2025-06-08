package net.tyrone.listeners;

import net.tyrone.MythAbilities;
import net.tyrone.commands.MythCommand;
import net.tyrone.enums.MythType;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class AbilityItemListener implements Listener {

    private final MythAbilities plugin;
    private final MythCommand mythCommand;

    public AbilityItemListener(MythAbilities plugin, MythCommand mythCommand) {
        this.plugin = plugin;
        this.mythCommand = mythCommand;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();

        if (item == null || item.getType() == Material.AIR) {
            return;
        }

        if (!plugin.getMythManager().hasMyth(player)) {
            return;
        }

        MythType playerMyth = plugin.getMythManager().getPlayerMyth(player);

        // Check if the item is a myth ability item
        if (!isAbilityItem(item, playerMyth)) {
            return;
        }

        event.setCancelled(true); // Prevent placing blocks or other interactions

        Action action = event.getAction();

        if (action == Action.LEFT_CLICK_AIR || action == Action.LEFT_CLICK_BLOCK) {
            // Left click - Ability 1
            mythCommand.executeAbility1(player);
        } else if (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK) {
            // Right click - Ability 2
            mythCommand.executeAbility2(player);
        }
    }

    private boolean isAbilityItem(ItemStack item, MythType mythType) {
        if (!item.hasItemMeta() || !item.getItemMeta().hasDisplayName()) {
            return false;
        }

        ItemMeta meta = item.getItemMeta();
        String displayName = meta.getDisplayName();

        switch (mythType) {
            case WIND:
                return item.getType() == Material.FEATHER &&
                        displayName.equals("§bWind Powers");
            case LIGHTNING:
                return item.getType() == Material.BLAZE_ROD &&
                        displayName.equals("§eLightning Staff");
            case FIRE:
                return item.getType() == Material.BLAZE_POWDER &&
                        displayName.equals("§cFire Essence");
            case EARTH:
                return item.getType() == Material.COBBLESTONE &&
                        displayName.equals("§6Earth Stone");
            case RICHES:
                return item.getType() == Material.GOLD_INGOT &&
                        displayName.equals("§dGolden Scepter");
            default:
                return false;
        }
    }
}