package net.tyrone.commands;

import net.tyrone.MythAbilities;
import net.tyrone.abilities.*;
import net.tyrone.enums.MythType;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

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
            case "ability1":
            case "a1":
                return handleAbility1(player);
            case "ability2":
            case "a2":
                return handleAbility2(player);
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
                player.sendMessage("§bAbility 1: Air Dash - Dash 10 blocks in the air (30s cooldown)");
                player.sendMessage("§bAbility 2: Ground Smash - Dash up and smash down dealing 4 hearts (3m cooldown)");
                break;
            case LIGHTNING:
                player.sendMessage("§eLightning Storm: Strike nearby enemies with lightning for 20s (3m cooldown)");
                player.sendMessage("§eAbility 2: Lightning Strike Chance - 50% chance to strike enemies for 45s (5m cooldown)");
                break;
            case FIRE:
                player.sendMessage("§cAbility 1: Fire Shot - Shoot fire dealing 2 hearts and ignite (3m cooldown)");
                player.sendMessage("§cAbility 2: Fire Rain - Rain fire in 10 block radius for 30s (4m cooldown)");
                break;
            case EARTH:
                player.sendMessage("§6Ability 1: Rock Throw - Throw a rock dealing 2 hearts (1m 30s cooldown)");
                player.sendMessage("§6Ability 2: Poison Aura - Poison enemies in 5 block radius for 30s (3m cooldown)");
                break;
            case RICHES:
                player.sendMessage("§dAbility 1: Iron Golem - Summon a protective golem for 1m (3m cooldown)");
                player.sendMessage("§dAbility 2: Cheap Trades - All trades cost 1 emerald for 1m (5m cooldown)");
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

    private boolean handleAbility1(Player player) {
        if (!plugin.getMythManager().hasMyth(player)) {
            player.sendMessage("§cYou don't have any myth abilities!");
            return true;
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
        return true;
    }

    private boolean handleAbility2(Player player) {
        if (!plugin.getMythManager().hasMyth(player)) {
            player.sendMessage("§cYou don't have any myth abilities!");
            return true;
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
        return true;
    }

    private void sendHelpMessage(Player player) {
        player.sendMessage("§6=== Myth Abilities Commands ===");
        player.sendMessage("§b/myth set <type> §7- Set your myth type");
        player.sendMessage("§b/myth remove §7- Remove your myth abilities");
        player.sendMessage("§b/myth info §7- View your myth abilities info");
        player.sendMessage("§b/myth trust <player> §7- Trust a player (abilities won't affect them)");
        player.sendMessage("§b/myth untrust <player> §7- Untrust a player");
        player.sendMessage("§b/myth ability1 (or a1) §7- Use first ability");
        player.sendMessage("§b/myth ability2 (or a2) §7- Use second ability");
        player.sendMessage("§7Available myth types: wind, lightning, fire, earth, riches");
    }
}