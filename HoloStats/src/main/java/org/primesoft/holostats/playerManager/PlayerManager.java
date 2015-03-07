package org.primesoft.holostats.playerManager;

import java.util.Collection;
import java.util.HashMap;
import java.util.UUID;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.primesoft.holostats.HoloStatsMain;
import org.primesoft.holostats.hologram.HologramManager;

/**
 *
 * @author SBPrime
 */
public class PlayerManager implements Listener {

    private final HoloStatsMain m_parent;

    /**
     * List of know players
     */
    private final HashMap<UUID, PlayerEntry> m_playersUids;

    public PlayerManager(HoloStatsMain pluginMain) {
        m_parent = pluginMain;
        m_playersUids = new HashMap<UUID, PlayerEntry>();
    }

    /**
     * Initialize the player manager
     */
    public void initalize() {
        Collection<? extends Player> players = m_parent.getServer().getOnlinePlayers();
        for (Player player : players) {
            addPlayer(player);
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        removePlayer(event.getPlayer());
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        final Player player = event.getPlayer();
        final PlayerEntry entry = addPlayer(player);
        
        if (entry == null) {
            return;
        }
    
        m_parent.getHologramManager().updatePlayer(entry);
    }

    /**
     * Wrap new player
     *
     * @param player
     * @return
     */
    public PlayerEntry addPlayer(Player player) {
        if (player == null) {
            return null;
        }

        UUID uuid = player.getUniqueId();
        String pName = player.getName();
        synchronized (m_playersUids) {
            PlayerEntry wrapper = m_playersUids.get(uuid);

            if (wrapper != null) {
                wrapper.update(player);
                return wrapper;
            }

            wrapper = new PlayerEntry(player, pName);
            m_playersUids.put(uuid, wrapper);
            return wrapper;
        }
    }

    /**
     * Get all known players
     * @return 
     */
    public PlayerEntry[] getAll() {
        synchronized (m_playersUids) {
            return m_playersUids.values().toArray(new PlayerEntry[0]);
        }
    }

    /**
     * Remove player
     *
     * @param player
     */
    public void removePlayer(Player player) {
        if (player == null) {
            return;
        }

        UUID uuid = player.getUniqueId();
        PlayerEntry entry;
        synchronized (m_playersUids) {
            entry = m_playersUids.remove(uuid);
        }
    }

    /**
     * Get the player wrapper based on bukkit player class (null = console)
     *
     * @param player
     * @return
     */
    public PlayerEntry getPlayer(Player player) {
        return player != null ? getPlayer(player.getUniqueId()) : null;
    }

    /**
     * Get the player wrapper based on UUID
     *
     * @param playerUuid
     * @return NEver returns null
     */
    public PlayerEntry getPlayer(UUID playerUuid) {
        if (playerUuid == null) {
            return null;
        }

        PlayerEntry result;

        synchronized (m_playersUids) {
            result = m_playersUids.get(playerUuid);
            if (result != null) {
                return result;
            }
        }

        /**
         * Unknown player try to find it
         */
        return null;
    }

    /**
     * Gets player wrapper from player name
     *
     * @param playerName
     * @return never returns null
     */
    public PlayerEntry getPlayer(String playerName) {
        if (playerName == null || playerName.length() == 0) {
            return null;
        }

        synchronized (m_playersUids) {
            for (PlayerEntry p : m_playersUids.values()) {
                if (p.getName().equalsIgnoreCase(playerName)) {
                    return p;
                }
            }
        }

        /**
         * Player name not found try using it as GUID
         */
        try {
            return getPlayer(UUID.fromString(playerName));
        } catch (IllegalArgumentException ex) {
            //This was not 
        }

        return null;
    }
}
