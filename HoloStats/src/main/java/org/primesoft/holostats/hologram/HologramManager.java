package org.primesoft.holostats.hologram;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import org.bukkit.scheduler.BukkitScheduler;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.ParseException;
import org.primesoft.holostats.HoloStatsMain;
import org.primesoft.holostats.RestAPI;
import org.primesoft.holostats.configuration.ConfigProvider;
import org.primesoft.holostats.playerManager.PlayerEntry;
import org.primesoft.holostats.utils.ExceptionHelper;

/**
 *
 * @author SBPrime
 */
public class HologramManager {

    private final HoloStatsMain m_pluginMain;
    private boolean m_isInitialized;
    private final Object m_mutex;
    private final BukkitScheduler m_scheduler;
    private UUID m_token;

    private static void log(String msg) {
        HoloStatsMain.log(msg);
    }
    private int m_playerHolosCnt;

    private HologramWrapper[] m_globalHolos;

    private HologramWrapper[] m_configHolos;

    public HologramManager(HoloStatsMain pluginMain) {
        m_pluginMain = pluginMain;
        m_mutex = new Object();
        m_isInitialized = false;
        m_scheduler = m_pluginMain.getScheduler();
    }

    public boolean initialize() {
        synchronized (m_mutex) {
            if (m_isInitialized) {
                cleanup();
            }

            if (!m_pluginMain.getPluginManager().isPluginEnabled("HolographicDisplays")) {
                log("HolographicDisplays is not enabled.");
                return false;
            }

            final UUID token = UUID.randomUUID();
            final HologramManager _this = this;
            m_token = token;

            m_scheduler.runTaskAsynchronously(m_pluginMain, new Runnable() {
                public void run() {
                    _this.downloadData(token);
                }
            });

            m_isInitialized = true;
            return true;
        }
    }

    public void cleanup() {
        synchronized (m_mutex) {
            if (!m_isInitialized) {
                return;
            }

            m_isInitialized = false;
            m_token = UUID.randomUUID();
        }
    }

    public void updatePlayer(final PlayerEntry player) {
        final UUID token = m_token;

        m_scheduler.runTaskAsynchronously(m_pluginMain, new Runnable() {
            public void run() {
                int playerHolosCnt = m_playerHolosCnt;
                List<HologramWrapper> pHolos = new ArrayList<HologramWrapper>();
                HologramWrapper hologram;
                for (int i = 0; i < playerHolosCnt; i++) {
                    hologram = RestAPI.getPlayerHologram(i + 1, player.getName());
                    if (hologram != null) {
                        pHolos.add(hologram);
                    }
                }

                synchronized (m_mutex) {
                    if (!token.equals(m_token)) {
                        return;
                    }

                    player.update(pHolos.toArray(new HologramWrapper[0]),
                            m_globalHolos, m_configHolos);
                }
            }
        });
    }

    private void downloadData(UUID token) {
        final String format = "[" + token + "] %s";
        log(String.format(format, "Downloading initial data..."));

        int globalHolosCnt = RestAPI.getGlobalCount();
        int playerHolosCnt = RestAPI.getPlayerCount();

        log(String.format(format, "Found " + globalHolosCnt + " global holograms and " + playerHolosCnt + " player holograms."));

        List<HologramWrapper> globalHolos = new ArrayList<HologramWrapper>();

        HologramWrapper hologram;
        log(String.format(format, "Downloading global holograms."));
        for (int i = 0; i < globalHolosCnt; i++) {
            hologram = RestAPI.getGlobalHologram(i + 1);
            if (hologram != null) {
                globalHolos.add(hologram);
            }
        }

        PlayerEntry[] allPlayers = m_pluginMain.getPlayerManager().getAll();
        log(String.format(format, "Downloading player (" + allPlayers.length + ") holograms."));
        HashMap<PlayerEntry, List<HologramWrapper>> playerHolos = new HashMap<PlayerEntry, List<HologramWrapper>>();
        for (PlayerEntry player : allPlayers) {
            List<HologramWrapper> pHolos = new ArrayList<HologramWrapper>();
            for (int i = 0; i < playerHolosCnt; i++) {
                hologram = RestAPI.getPlayerHologram(i + 1, player.getName());
                if (hologram != null) {
                    pHolos.add(hologram);
                }
            }

            playerHolos.put(player, pHolos);
        }

        List<HologramWrapper> cHolos = new ArrayList<HologramWrapper>();
        for (String s : ConfigProvider.getConfigHolos()) {
            try {
                JSONObject o = (JSONObject) JSONValue.parseWithException(s);
                hologram = HologramWrapper.parse(o);
                if (hologram != null) {
                    cHolos.add(hologram);
                }
            } catch (ParseException ex) {
                ExceptionHelper.printException(ex, "Unable to parse configuration hologram " + s);
            }
        }

        synchronized (m_mutex) {
            if (!token.equals(m_token)) {
                log(String.format(format, "Token changed skipping."));
                return;
            }

            m_playerHolosCnt = playerHolosCnt;
            m_globalHolos = globalHolos.toArray(new HologramWrapper[0]);
            m_configHolos = cHolos.toArray(new HologramWrapper[0]);

            log(String.format(format, m_globalHolos.length + " global holograms loaded."));
            log(String.format(format, m_configHolos.length + " configuration holograms loaded."));
            for (PlayerEntry player : playerHolos.keySet()) {
                HologramWrapper[] holos = playerHolos.get(player).toArray(new HologramWrapper[0]);

                log(String.format(format, holos.length + " holograms gor player " + player.getName() + " loaded."));
                player.update(holos,
                        m_globalHolos, m_configHolos);
                log(String.format(format, m_globalHolos.length + " configuration holograms loaded."));
            }
        }
    }
}
