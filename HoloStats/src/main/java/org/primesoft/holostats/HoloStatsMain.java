package org.primesoft.holostats;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.Server;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.primesoft.holostats.commands.ReloadCommand;
import org.primesoft.holostats.hologram.HologramManager;
import org.primesoft.holostats.playerManager.PlayerManager;

/**
 *
 * @author SBPrime
 */
public class HoloStatsMain extends JavaPlugin {

    private static final Logger s_log = Logger.getLogger("Minecraft.MidiPlayer");
    private static String s_prefix = null;
    private static final String s_logFormat = "%s %s";

    /**
     * The instance of the class
     */
    private static HoloStatsMain s_instance;

    /**
     * Send message to the log
     *
     * @param msg
     */
    public static void log(String msg) {
        if (s_log == null || msg == null || s_prefix == null) {
            return;
        }

        s_log.log(Level.INFO, String.format(s_logFormat, s_prefix, msg));
    }

    /**
     * The instance of the class
     *
     * @return
     */
    public static HoloStatsMain getInstance() {
        return s_instance;
    }
        
    
    /**
     * The plugin version
     */
    private String m_version;
    
    /**
     * The reload command handler
     */
    private ReloadCommand m_reloadCommandHandler;
    
    
    /**
     * The plugin manager
     */
    private PluginManager m_pluginManager;
    
    /**
     * The player manager
     */
    private PlayerManager m_playerManager;
    
    private Server m_server;
    
    private HologramManager m_hologramManager;
    
    public String getVersion() {
        return m_version;
    }
    
    
    public PluginManager getPluginManager() {
        return m_pluginManager;
    }

    public BukkitScheduler getScheduler() {
        return m_server.getScheduler();
    }

    public PlayerManager getPlayerManager() {
        return m_playerManager;
    }
    
    public HologramManager getHologramManager() {
        return m_hologramManager;
    }

    @Override
    public void onEnable() {
        m_server = getServer();
        PluginDescriptionFile desc = getDescription();
        s_prefix = String.format("[%s]", desc.getName());
        s_instance = this;
        
        m_version = desc.getVersion();
                
        m_playerManager = new PlayerManager(this);
        m_hologramManager = new HologramManager(this);
        
        m_pluginManager = getServer().getPluginManager();
        m_pluginManager.registerEvents(m_playerManager, this);
        
        m_reloadCommandHandler = new ReloadCommand(this);
        
        PluginCommand commandReload = getCommand("hsreload");
        commandReload.setExecutor(m_reloadCommandHandler);
        
        m_playerManager.initalize();
        if (!m_reloadCommandHandler.ReloadConfig()) {
            log("Error loading config");
            return;
        }                        
        
        super.onEnable();
        
        log("Enabled");
    }
    
    @Override
    public void onDisable() {
        m_hologramManager.cleanup();
        
        super.onDisable();
        
        log("Disabled");
    }
}