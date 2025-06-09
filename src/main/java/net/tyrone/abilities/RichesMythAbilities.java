package net.tyrone.abilities;

import net.tyrone.MythAbilities;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.IronGolem;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.VillagerAcquireTradeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MerchantRecipe;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class RichesMythAbilities implements Listener {

    private final MythAbilities plugin;
    private final Map<UUID, IronGolem> summonedGolems = new HashMap<>();
    private final Map<UUID, BukkitTask> golemTasks = new HashMap<>();
    private final Map<UUID, BukkitTask> cheapTradeTasks = new HashMap<>();
    private final Map<UUID, Map<Villager, List<MerchantRecipe>>> originalTrades = new HashMap<>();

    public RichesMythAbilities(MythAbilities plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public void summonIronGolem(Player player) {
        // Check cooldown
        if (plugin.getCooldownManager().isOnCooldown(player, "iron_golem")) {
            long remaining = plugin.getCooldownManager().getRemainingCooldown(player, "iron_golem");
            player.sendMessage("§cIron Golem summon is on cooldown for " + remaining + " seconds!");
            return;
        }

        // Set cooldown (3 minutes)
        plugin.getCooldownManager().setCooldown(player, "iron_golem", 180);

        // Remove existing golem if any
        if (summonedGolems.containsKey(player.getUniqueId())) {
            IronGolem oldGolem = summonedGolems.get(player.getUniqueId());
            if (!oldGolem.isDead()) {
                oldGolem.remove();
            }
            if (golemTasks.containsKey(player.getUniqueId())) {
                golemTasks.get(player.getUniqueId()).cancel();
            }
        }

        // Spawn iron golem
        Location spawnLoc = player.getLocation().add(2, 0, 0);
        IronGolem golem = player.getWorld().spawn(spawnLoc, IronGolem.class);
        golem.setCustomName("§6" + player.getName() + "'s Guardian");
        golem.setCustomNameVisible(true);

        // Make the golem more aggressive and stronger
        golem.setPlayerCreated(false); // This makes it hostile to non-trusted players
        golem.setMaxHealth(200.0); // Increase health
        golem.setHealth(200.0);

        summonedGolems.put(player.getUniqueId(), golem);

        // Make golem target non-trusted players
        BukkitTask golemTask = new BukkitRunnable() {
            int ticks = 0;

            @Override
            public void run() {
                if (ticks >= 1200 || golem.isDead()) { // 1 minute or golem is dead
                    if (!golem.isDead()) {
                        golem.remove();
                        player.sendMessage("§eYour iron golem disappears");
                    }
                    summonedGolems.remove(player.getUniqueId());
                    golemTasks.remove(player.getUniqueId());
                    this.cancel();
                    return;
                }

                // Find nearest non-trusted player every 2 seconds
                if (ticks % 40 == 0) {
                    Player nearestEnemy = null;
                    double nearestDistance = 15.0; // Increased range to 15 blocks

                    Collection<Entity> nearbyEntities = golem.getNearbyEntities(15, 15, 15);
                    for (Entity entity : nearbyEntities) {
                        if (entity instanceof Player && !entity.equals(player)) {
                            Player target = (Player) entity;
                            if (!plugin.getTrustManager().isTrusted(player, target)) {
                                double distance = golem.getLocation().distance(target.getLocation());
                                if (distance < nearestDistance) {
                                    nearestDistance = distance;
                                    nearestEnemy = target;
                                }
                            }
                        }
                    }

                    if (nearestEnemy != null) {
                        golem.setTarget(nearestEnemy);
                        // Add aggressive behavior
                        golem.setAggressive(true);
                    }
                }

                ticks++;
            }
        }.runTaskTimer(plugin, 0, 1);

        golemTasks.put(player.getUniqueId(), golemTask);

        // Effects
        spawnLoc.getWorld().spawnParticle(Particle.CLOUD, spawnLoc, 30, 2, 2, 2, 0.1);
        spawnLoc.getWorld().playSound(spawnLoc, Sound.BLOCK_ANVIL_PLACE, 1.0f, 0.8f);

    }

    public void cheapTrades(Player player) {
        // Check cooldown
        if (plugin.getCooldownManager().isOnCooldown(player, "cheap_trades")) {
            long remaining = plugin.getCooldownManager().getRemainingCooldown(player, "cheap_trades");
            player.sendMessage("§cCheap Trades is on cooldown for " + remaining + " seconds!");
            return;
        }

        // Set cooldown (5 minutes)
        plugin.getCooldownManager().setCooldown(player, "cheap_trades", 300);

        // Cancel existing effect if any
        if (cheapTradeTasks.containsKey(player.getUniqueId())) {
            restoreOriginalTrades(player);
            cheapTradeTasks.get(player.getUniqueId()).cancel();
        }

        // Modify all nearby villager trades
        Collection<Entity> nearbyEntities = player.getWorld().getNearbyEntities(player.getLocation(), 50, 50, 50);
        Map<Villager, List<MerchantRecipe>> playerOriginalTrades = new HashMap<>();

        for (Entity entity : nearbyEntities) {
            if (entity instanceof Villager) {
                Villager villager = (Villager) entity;
                applyCheapTradesToVillager(villager, playerOriginalTrades);
            }
        }

        originalTrades.put(player.getUniqueId(), playerOriginalTrades);

        // Restore trades after 1 minute
        BukkitTask tradeTask = new BukkitRunnable() {
            @Override
            public void run() {
                restoreOriginalTrades(player);
                cheapTradeTasks.remove(player.getUniqueId());
                player.sendMessage("§eVillager trades return to normal");
            }
        }.runTaskLater(plugin, 1200); // 1 minute

        cheapTradeTasks.put(player.getUniqueId(), tradeTask);

        player.sendMessage("§6All villager trades now cost only 1 emerald!");
    }

    private void applyCheapTradesToVillager(Villager villager, Map<Villager, List<MerchantRecipe>> playerOriginalTrades) {
        // Store original trades
        List<MerchantRecipe> originalRecipes = new ArrayList<>(villager.getRecipes());
        playerOriginalTrades.put(villager, originalRecipes);

        // Create new cheap trades
        List<MerchantRecipe> cheapRecipes = new ArrayList<>();
        for (MerchantRecipe original : originalRecipes) {
            MerchantRecipe cheapRecipe = new MerchantRecipe(original.getResult(), original.getMaxUses());
            cheapRecipe.addIngredient(new ItemStack(Material.EMERALD, 1)); // Everything costs 1 emerald
            cheapRecipes.add(cheapRecipe);
        }

        villager.setRecipes(cheapRecipes);
    }

    private void restoreOriginalTrades(Player player) {
        Map<Villager, List<MerchantRecipe>> playerTrades = originalTrades.get(player.getUniqueId());
        if (playerTrades != null) {
            for (Map.Entry<Villager, List<MerchantRecipe>> entry : playerTrades.entrySet()) {
                Villager villager = entry.getKey();
                List<MerchantRecipe> originalRecipes = entry.getValue();

                if (!villager.isDead()) {
                    villager.setRecipes(originalRecipes);
                }
            }
            originalTrades.remove(player.getUniqueId());
        }
    }

    private Player getPlayerWithCheapTradesActive(Villager villager) {
        // Check which player has cheap trades active and this villager is affected
        for (Map.Entry<UUID, Map<Villager, List<MerchantRecipe>>> entry : originalTrades.entrySet()) {
            if (entry.getValue().containsKey(villager)) {
                return plugin.getServer().getPlayer(entry.getKey());
            }
        }
        return null;
    }

    @EventHandler
    public void onVillagerAcquireTrade(VillagerAcquireTradeEvent event) {
        Villager villager = (Villager) event.getEntity();

        // Check if this villager is affected by any player's cheap trades ability
        Player affectedPlayer = getPlayerWithCheapTradesActive(villager);

        if (affectedPlayer != null && cheapTradeTasks.containsKey(affectedPlayer.getUniqueId())) {
            // Get the player's original trades map
            Map<Villager, List<MerchantRecipe>> playerOriginalTrades = originalTrades.get(affectedPlayer.getUniqueId());

            // Update the original trades with the new recipe
            if (playerOriginalTrades.containsKey(villager)) {
                playerOriginalTrades.get(villager).add(event.getRecipe());
            } else {
                List<MerchantRecipe> newOriginalList = new ArrayList<>(villager.getRecipes());
                newOriginalList.add(event.getRecipe());
                playerOriginalTrades.put(villager, newOriginalList);
            }

            // Apply cheap version of the new trade
            new BukkitRunnable() {
                @Override
                public void run() {
                    // Create cheap version of the new recipe
                    MerchantRecipe cheapRecipe = new MerchantRecipe(event.getRecipe().getResult(), event.getRecipe().getMaxUses());
                    cheapRecipe.addIngredient(new ItemStack(Material.EMERALD, 1));

                    // Add the cheap recipe to the villager's current recipes
                    List<MerchantRecipe> currentRecipes = new ArrayList<>(villager.getRecipes());
                    // Replace the last recipe (the new one) with the cheap version
                    if (!currentRecipes.isEmpty()) {
                        currentRecipes.set(currentRecipes.size() - 1, cheapRecipe);
                        villager.setRecipes(currentRecipes);
                    }
                }
            }.runTaskLater(plugin, 1); // Run next tick to ensure the recipe is properly added first
        }
    }

    @EventHandler
    public void onGolemTarget(EntityTargetEvent event) {
        if (!(event.getEntity() instanceof IronGolem)) return;
        if (!(event.getTarget() instanceof Player)) return;

        IronGolem golem = (IronGolem) event.getEntity();
        Player target = (Player) event.getTarget();

        // Find the owner of this golem
        Player owner = null;
        for (Map.Entry<UUID, IronGolem> entry : summonedGolems.entrySet()) {
            if (entry.getValue().equals(golem)) {
                owner = plugin.getServer().getPlayer(entry.getKey());
                break;
            }
        }

        if (owner != null) {
            // Cancel targeting if the target is trusted or is the owner
            if (plugin.getTrustManager().isTrusted(owner, target) || target.equals(owner)) {
                event.setCancelled(true);
            }
            // If not trusted, make sure the golem can attack
            else {
                golem.setAggressive(true);
            }
        }
    }
}