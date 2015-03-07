package org.primesoft.holostats.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.primesoft.holostats.HoloStatsMain;
import org.primesoft.holostats.configuration.ConfigProvider;

/**
 *
 * @author SBPrime
 */
public class ReloadCommand implements CommandExecutor 
{
    private final HoloStatsMain m_pluginMain;

    public static void log(String msg) {
        HoloStatsMain.log(msg);
    }
    
    public ReloadCommand(HoloStatsMain plugin) {
        m_pluginMain = plugin;
    }
    
    
    public boolean onCommand(CommandSender cs, Command cmnd, String string, String[] strings) {
        return ReloadConfig();        
    }

    public boolean ReloadConfig() {
        if (!ConfigProvider.load(m_pluginMain)) {
            log("Error loading config");
            return false;
        }
        
        m_pluginMain.getHologramManager().initialize();
        
        log("Config loaded");
        return true;
    }

}
