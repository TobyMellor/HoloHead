package org.primesoft.holostats.configuration;

import com.avaje.ebeaninternal.server.core.Message;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;

/**
 *
 * @author SBPrime
 */
public class ConfigProvider {

    /**
     * The API url base
     */
    private static String s_apiUrl;

    /**
     * The API key
     */
    private static String s_apiKey;

    /**
     * The base hologram name
     */
    private static String s_holoName;

    /**
     * List of configuration holograms
     */
    private static String[] s_configHolos;

    public static String getApiKey() {
        return s_apiKey;
    }

    public static String getApiUrl() {
        return s_apiUrl;
    }

    public static String getHoloName() {
        return s_holoName;
    }

    public static String[] getConfigHolos() {
        return s_configHolos;
    }

    /**
     * Load configuration
     *
     * @param plugin parent plugin
     * @return true if config loaded
     */
    public static boolean load(JavaPlugin plugin) {
        if (plugin == null) {
            return false;
        }

        plugin.saveDefaultConfig();

        Configuration config = plugin.getConfig();

        ConfigurationSection mainSection = config.getConfigurationSection("HoloStats");

        if (mainSection == null) {
            return false;
        }

        s_apiKey = mainSection.getString("apiKey");
        s_apiUrl = mainSection.getString("apiUrl");
        s_holoName = mainSection.getString("holoName");
        s_configHolos = mainSection.getStringList("broadcast").toArray(new String[0]);

        return true;
    }
}
