package net.tyrone;


import net.tyrone.commands.MythCommand;
import net.tyrone.listeners.AbilityItemListener;
import net.tyrone.listeners.PlayerListener;
import net.tyrone.managers.MythManager;
import net.tyrone.utils.CooldownManager;
import net.tyrone.utils.TrustManager;
import org.bukkit.plugin.java.JavaPlugin;

public class MythAbilities extends JavaPlugin {

    private static MythAbilities instance;
    private CooldownManager cooldownManager;
    private TrustManager trustManager;
    private MythManager mythManager;

    @Override
    public void onEnable() {
        instance = this;

        // Initialize managers
        cooldownManager = new CooldownManager();
        trustManager = new TrustManager();
        mythManager = new MythManager(this);

        // Register commands
        MythCommand mythCommand = new MythCommand(this);
        getCommand("myth").setExecutor(mythCommand);

        // Register listeners
        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
        getServer().getPluginManager().registerEvents(new AbilityItemListener(this, mythCommand), this);

        getLogger().info("MythAbilities plugin has been enabled!");
    }

    @Override
    public void onDisable() {
        getLogger().info("MythAbilities plugin has been disabled!");
    }

    public static MythAbilities getInstance() {
        return instance;
    }

    public CooldownManager getCooldownManager() {
        return cooldownManager;
    }

    public TrustManager getTrustManager() {
        return trustManager;
    }

    public MythManager getMythManager() {
        return mythManager;
    }
}