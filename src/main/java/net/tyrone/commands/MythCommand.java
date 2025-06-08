package net.tyrone.commands;

import net.tyrone.MythAbilities;
import net.tyrone.abilities.*;
import net.tyrone.enums.MythType;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

public class MythCommand implements CommandExecutor {

    private final MythAbilities plugin;
    private final WindMythAbilities windAbilities;
    private final net.tyrone.abilities.LightningMythAbilities lightningAbilities;
    private final FireMythAbilities fireAbilities;
    private final EarthMythAbilities earthAbilities;
    private final RichesMythAbilities richesAbilities;

    public MythCommand(MythAbilities plugin) {
        this.plugin = plugin;
        this.windAbilities = new WindMythAbilities(plugin);
        this.lightningAbilities = new net.tyrone.abilities.LightningMythAbilities(plugin);
        this.fireAbilities = new FireMythAbilities(plugin);
        this.earthAbilities = new EarthMythAbilities(plugin);
        this.richesAbilities = new RichesMythAbilities(plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cOnly players can use myth abilities!");
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            sendHelpMessage(player);
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "set":
                return handleSetCommand(player, args);
            case "remove":
                return handleRemoveCommand(player);
            case "info":
                return handleInfoCommand(player);
            case "trust":
                return handleTrustCommand(player, args);
            case "untrust":
                return handleUntrustCommand(player, args);
            case "give":
                return handleGiveCommand(player);
            case "help":
                sendHelpMessage(player);
                return true;
            default:
                sendHelpMessage(player);
                return true;
        }
    }

    private boolean handleSetCommand(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage("§cUsage: /myth set <mythtype>");
            player.sendMessage("§cAvailable myths: wind, lightning, fire, earth, riches");
            return true;
        }

        String mythName = args[1].toLowerCase();
        MythType mythType = null;

        switch (mythName) {
            case "wind":
                mythType = MythType.WIND;
                break;
            case "lightning":
                mythType = MythType.LIGHTNING;
                break;
            case "fire":
                mythType = MythType.FIRE;
                break;
            case "earth":
                mythType = MythType.EARTH;
                break;
            case "riches":
                mythType = MythType.RICHES;
                break;
            default:
                player.sendMessage("§cInvalid myth type! Available: wind, lightning, fire, earth, riches");
                return true;
        }

        plugin.getMythManager().setPlayerMyth(player, mythType);
        player.sendMessage("§aYou have been granted the power of " + mythType.getDisplayName() + "!");
        player.sendMessage("§eUse /myth give to receive your ability items!");
        return true;
    }

    private boolean handleRemoveCommand(Player player) {
        if (!plugin.getMythManager().hasMyth(player)) {
            player.sendMessage("§cYou don't have any myth abilities!");
            return true;
        }

        plugin.getMythManager().removePlayerMyth(player);
        player.sendMessage("§eYour myth abilities have been removed.");
        return true;
    }

    private boolean handleInfoCommand(Player player) {
        if (!plugin.getMythManager().hasMyth(player)) {
            player.sendMessage("§cYou don't have any myth abilities!");
            return true;
        }

        MythType mythType = plugin.getMythManager().getPlayerMyth(player);
        player.sendMessage("§6=== " + mythType.getDisplayName() + " ===");

        switch (mythType) {
            case WIND:
                player.sendMessage("§bLeft Click: Air Dash - Dash 10 blocks in the air (30s cooldown)");
                player.sendMessage("§bRight Click: Ground Smash - Dash up and smash down dealing 4 hearts (3m cooldown)");
                break;
            case LIGHTNING:
                player.sendMessage("§eLeft Click: Lightning Storm - Strike nearby enemies with lightning for 20s (3m cooldown)");
                player.sendMessage("§eRight Click: Lightning Strike Chance - 50% chance to strike enemies for 45s (5m cooldown)");
                break;
            case FIRE:
                player.sendMessage("§cLeft Click: Fire Shot - Shoot fire dealing 2 hearts and ignite (3m cooldown)");
                player.sendMessage("§cRight Click: Fire Rain - Rain fire in 10 block radius for 30s (4m cooldown)");
                break;
            case EARTH:
                player.sendMessage("§6Left Click: Rock Throw - Throw a rock dealing 2 hearts (1m 30s cooldown)");
                player.sendMessage("§6Right Click: Poison Aura - Poison enemies in 5 block radius for 30s (3m cooldown)");
                break;
            case RICHES:
                player.sendMessage("§dLeft Click: Iron Golem - Summon a protective golem for 1m (3m cooldown)");
                player.sendMessage("§dRight Click: Cheap Trades - All trades cost 1 emerald for 1m (5m cooldown)");
                break;
        }
        return true;
    }

    private boolean handleTrustCommand(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage("§cUsage: /myth trust <player>");
            return true;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            player.sendMessage("§cPlayer not found!");
            return true;
        }

        if (target.equals(player)) {
            player.sendMessage("§cYou cannot trust yourself!");
            return true;
        }

        plugin.getTrustManager().trustPlayer(player, target);
        player.sendMessage("§aYou now trust " + target.getName() + ". Your abilities won't affect them.");
        target.sendMessage("§a" + player.getName() + " now trusts you and their abilities won't affect you.");
        return true;
    }

    private boolean handleUntrustCommand(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage("§cUsage: /myth untrust <player>");
            return true;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            player.sendMessage("§cPlayer not found!");
            return true;
        }

        plugin.getTrustManager().untrustPlayer(player, target);
        player.sendMessage("§cYou no longer trust " + target.getName() + ". Your abilities will affect them.");
        target.sendMessage("§c" + player.getName() + " no longer trusts you. Their abilities will affect you.");
        return true;
    }

    private boolean handleGiveCommand(Player player) {
        if (!plugin.getMythManager().hasMyth(player)) {
            player.sendMessage("§cYou don't have any myth abilities!");
            return true;
        }

        MythType mythType = plugin.getMythManager().getPlayerMyth(player);
        ItemStack abilityItem = createAbilityItem(mythType);

        player.getInventory().addItem(abilityItem);
        player.sendMessage("§aYou have received your " + mythType.getDisplayName() + " ability item!");
        player.sendMessage("§eLeft click to use ability 1, right click to use ability 2!");

        return true;
    }

    private ItemStack createAbilityItem(MythType mythType) {
        ItemStack item = null;
        String displayName = "";
        String[] lore = null;

        switch (mythType) {
            case WIND:
                item = new ItemStack(Material.FEATHER);
                displayName = "§bWind Powers";
                lore = new String[]{
                        "§7Left Click: §bAir Dash",
                        "§7Right Click: §bGround Smash",
                        "",
                        "§eClick to use your wind abilities!"
                };
                break;
            case LIGHTNING:
                item = new ItemStack(Material.BLAZE_ROD);
                displayName = "§eLightning Staff";
                lore = new String[]{
                        "§7Left Click: §eLightning Storm",
                        "§7Right Click: §eLightning Strike Chance",
                        "",
                        "§eClick to use your lightning abilities!"
                };
                break;
            case FIRE:
                item = new ItemStack(Material.BLAZE_POWDER);
                displayName = "§cFire Essence";
                lore = new String[]{
                        "§7Left Click: §cFire Shot",
                        "§7Right Click: §cFire Rain",
                        "",
                        "§eClick to use your fire abilities!"
                };
                break;
            case EARTH:
                item = new ItemStack(Material.COBBLESTONE);
                displayName = "§6Earth Stone";
                lore = new String[]{
                        "§7Left Click: §6Rock Throw",
                        "§7Right Click: §6Poison Aura",
                        "",
                        "§eClick to use your earth abilities!"
                };
                break;
            case RICHES:
                item = new ItemStack(Material.GOLD_INGOT);
                displayName = "§dGolden Scepter";
                lore = new String[]{
                        "§7Left Click: §dSummon Iron Golem",
                        "§7Right Click: §dCheap Trades",
                        "",
                        "§eClick to use your riches abilities!"
                };
                break;
        }

        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(displayName);
        meta.setLore(Arrays.asList(lore));
        item.setItemMeta(meta);

        return item;
    }

    // Ability execution methods for use by the item listener
    public void executeAbility1(Player player) {
        if (!plugin.getMythManager().hasMyth(player)) {
            return;
        }

        MythType mythType = plugin.getMythManager().getPlayerMyth(player);

        switch (mythType) {
            case WIND:
                windAbilities.airDash(player);
                break;
            case LIGHTNING:
                lightningAbilities.lightningStorm(player);
                break;
            case FIRE:
                fireAbilities.fireShot(player);
                break;
            case EARTH:
                earthAbilities.throwRock(player);
                break;
            case RICHES:
                richesAbilities.summonIronGolem(player);
                break;
        }
    }

    public void executeAbility2(Player player) {
        if (!plugin.getMythManager().hasMyth(player)) {
            return;
        }

        MythType mythType = plugin.getMythManager().getPlayerMyth(player);

        switch (mythType) {
            case WIND:
                windAbilities.groundSmash(player);
                break;
            case LIGHTNING:
                lightningAbilities.lightningStrikeChance(player);
                break;
            case FIRE:
                fireAbilities.fireRain(player);
                break;
            case EARTH:
                earthAbilities.poisonAura(player);
                break;
            case RICHES:
                richesAbilities.cheapTrades(player);
                break;
        }
    }

    private void sendHelpMessage(Player player) {
        player.sendMessage("§6=== Myth Abilities Commands ===");
        player.sendMessage("§b/myth set <type> §7- Set your myth type");
        player.sendMessage("§b/myth remove §7- Remove your myth abilities");
        player.sendMessage("§b/myth info §7- View your myth abilities info");
        player.sendMessage("§b/myth give §7- Get your ability items");
        player.sendMessage("§b/myth trust <player> §7- Trust a player (abilities won't affect them)");
        player.sendMessage("§b/myth untrust <player> §7- Untrust a player");
        player.sendMessage("§7Available myth types: wind, lightning, fire, earth, riches");
        player.sendMessage("§7Use your ability items: Left click for ability 1, right click for ability 2");
    }
}