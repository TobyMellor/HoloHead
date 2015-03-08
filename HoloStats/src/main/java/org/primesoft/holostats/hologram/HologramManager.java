package org.primesoft.holostats.hologram;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import com.gmail.filoghost.holographicdisplays.api.VisibilityManager;
import com.gmail.filoghost.holographicdisplays.object.NamedHologramManager;
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
import org.primesoft.holostats.playerManager.PlayerManager;
import org.primesoft.holostats.utils.ExceptionHelper;

/**
 *
 * @author SBPrime
 */
public class HologramManager {

    /**
     * Page swap and update loop max wait time (miliseconds)
     */
    private static final long MAX_WAIT = 60000;

    /**
     * Number of ticks per 1000ms
     */
    private static final long TICKS_PER_SECOND = 20;

    /**
     * Holo stats plugin main
     */
    private final HoloStatsMain m_pluginMain;

    /**
     * MTA mutex
     */
    private final Object m_mutex;

    /**
     * The bukkit scheduler
     */
    private final BukkitScheduler m_scheduler;

    /**
     * The player manager
     */
    private final PlayerManager m_playerManager;

    /**
     * Is the hologram manager initialized
     */
    private boolean m_isInitialized;

    /**
     * The target hologram
     */
    private Hologram m_targetHologram;

    /**
     * Number of player holograms
     */
    private int m_playerHolosCnt;

    /**
     * List of global holograms
     */
    private HologramWrapper[] m_globalHolos;

    /**
     * List of configuration holograms
     */
    private HologramWrapper[] m_configHolos;

    /**
     * List of player holograms
     */
    private final HashMap<PlayerEntry, List<HologramWrapper>> m_playerHolograms;

    /**
     * Data update token
     */
    private UUID m_token;

    /**
     * The page changer token
     */
    private UUID m_tokenPageChanger;

    /**
     * The hologram update token
     */
    private UUID m_tokenUpdater;

    private static void log(String msg) {
        HoloStatsMain.log(msg);
    }

    public HologramManager(HoloStatsMain pluginMain) {
        m_pluginMain = pluginMain;
        m_mutex = new Object();
        m_isInitialized = false;
        m_scheduler = m_pluginMain.getScheduler();
        m_playerManager = m_pluginMain.getPlayerManager();
        m_playerHolograms = new HashMap<PlayerEntry, List<HologramWrapper>>();
    }

    public boolean initialize() {
        synchronized (m_mutex) {
            if (!m_pluginMain.getPluginManager().isPluginEnabled("HolographicDisplays")) {
                log("HolographicDisplays is not enabled.");
                return false;
            }

            final UUID token = UUID.randomUUID();
            final HologramManager _this = this;
            m_token = token;

            boolean result = findTargetHologram(token);
            if (!result) {
                initializePlayerHolograms();
                return false;
            }
            m_scheduler.runTaskAsynchronously(m_pluginMain, new Runnable() {
                public void run() {
                    _this.downloadData(token);
                }
            });

            m_isInitialized = true;
            initializePlayerHolograms();
            return true;
        }
    }

    public void cleanup() {
        synchronized (m_mutex) {
            if (m_targetHologram != null) {
                VisibilityManager vm = m_targetHologram.getVisibilityManager();
                vm.resetVisibilityAll();
                vm.setVisibleByDefault(true);

                m_targetHologram = null;
            }

            m_playerHolograms.clear();
            m_token = UUID.randomUUID();
            
            initializePlayerHolograms();
        }
    }

    /**
     * Create hologram for player
     *
     * @param player
     * @return
     */
    public Hologram createHologram(PlayerEntry player) {
        if (player == null) {
            return null;
        }

        Hologram result = null;
        synchronized (m_mutex) {
            if (!m_isInitialized || m_targetHologram == null) {
                return null;
            }

            log("Creating hologram for player \"" + player.getName() + "\"...");
            result = HologramsAPI.createHologram(m_pluginMain, m_targetHologram.getLocation());
            result.appendTextLine("Loading...");

            if (result == null) {
                log("Unable to create hologram.");
                return null;
            }

            VisibilityManager vm = result.getVisibilityManager();
            if (vm == null) {
                log("Unable to get the VisibilityManager for hologram.");

                result.delete();
                return null;
            }

            vm.showTo(player.getPlayer());
            vm.setVisibleByDefault(false);
        }

        return result;
    }

    /**
     * Find the target hologram
     *
     * @param token
     */
    private boolean findTargetHologram(final UUID token) {
        final String format = "[" + token + "] %s";
        final String targetName = ConfigProvider.getHoloName();

        log(String.format(format, "Searching for hologram \"" + targetName + "\"."));
        Hologram target = NamedHologramManager.getHologram(targetName);

        if (target == null) {
            log(String.format(format, "Target hologram \"" + targetName + "\" not found. Plugin not initialized."));

            synchronized (m_mutex) {
                if (m_targetHologram != null) {
                    VisibilityManager vm = m_targetHologram.getVisibilityManager();
                    vm.resetVisibilityAll();
                    vm.setVisibleByDefault(true);

                    m_targetHologram = null;
                }
            }

            return false;
        }

        synchronized (m_mutex) {
            m_targetHologram = target;
            VisibilityManager vm = m_targetHologram.getVisibilityManager();
            vm.setVisibleByDefault(false);
        }

        return true;
    }

    
    /**
     * Handle player removal
     *
     * @param entry
     */
    public void removePlayer(PlayerEntry entry) {
        synchronized (m_mutex) {
            if (entry == null) {
                return;
            }
            
            if (m_playerHolograms.containsKey(entry)) {
                m_playerHolograms.remove(entry);
            }
        }
    }
    
    
    /**
     * Update player holograms
     *
     * @param player
     */
    public void updatePlayer(final PlayerEntry player) {
        final UUID token = m_token;
        final String format = "[" + token + "] %s";

        initializePlayerHolograms(player);

        log(String.format(format, "Downloading player " + player.getName() + " holograms."));
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
                        log(String.format(format, "Token changed skipping."));
                        return;
                    }

                    HologramWrapper[] holos = pHolos.toArray(new HologramWrapper[0]);
                    if (m_playerHolograms.containsKey(player)) {
                        m_playerHolograms.remove(player);
                    }
                    m_playerHolograms.put(player, pHolos);

                    log(String.format(format, holos.length + " holograms for player " + player.getName() + " loaded."));
                    player.update(holos, m_globalHolos, m_configHolos);
                }

                pageChangerStart();
                hologramUpdaterStart();
            }
        });
    }

    /**
     * Download all holograms data
     *
     * @param token
     */
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

        PlayerEntry[] allPlayers = m_playerManager.getAll();
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
                hologram = HologramWrapper.parse(o, HologramType.Other, null);
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

            m_playerHolograms.clear();
            m_playerHolosCnt = playerHolosCnt;
            m_globalHolos = globalHolos.toArray(new HologramWrapper[0]);
            m_configHolos = cHolos.toArray(new HologramWrapper[0]);

            log(String.format(format, m_globalHolos.length + " global holograms loaded."));
            log(String.format(format, m_configHolos.length + " configuration holograms loaded."));
            for (PlayerEntry player : playerHolos.keySet()) {
                List<HologramWrapper> tmp = playerHolos.get(player);
                HologramWrapper[] holos = tmp.toArray(new HologramWrapper[0]);
                m_playerHolograms.put(player, tmp);

                log(String.format(format, holos.length + " holograms for player " + player.getName() + " loaded."));
                player.update(holos, m_globalHolos, m_configHolos);
            }
        }

        pageChangerStart();
        hologramUpdaterStart();
    }

    /**
     * Initialize the player holograms
     */
    private void initializePlayerHolograms() {
        for (PlayerEntry p : m_playerManager.getAll()) {
            initializePlayerHolograms(p);
        }
    }

    /**
     * Initialize the player hologram
     *
     * @param player
     */
    private void initializePlayerHolograms(PlayerEntry player) {
        if (player == null) {
            return;
        }

        log("Initializing player \"" + player.getName() + "\" hologram.");
        player.setHologram(createHologram(player));
    }

    /**
     * Start page changer
     */
    private void pageChangerStart() {
        final UUID token = UUID.randomUUID();

        synchronized (m_mutex) {
            m_tokenPageChanger = token;
            log("[" + token.toString() + "] Starting new page changer");
        }

        m_scheduler.runTask(m_pluginMain, new Runnable() {
            public void run() {
                pageChangerLoop(token);
            }
        });
    }

    /**
     * The hologram changer loop
     *
     * @param token
     */
    private void pageChangerLoop(final UUID token) {
        final String format = "[" + token + "] %s";

        long now = System.currentTimeMillis();
        long nextPageChangeIn = Long.MAX_VALUE;

        synchronized (m_mutex) {
            do {
                if (!token.equals(m_tokenPageChanger)) {
                    log(String.format(format, "Token changed."));
                    return;
                }

                log(String.format(format, "Calculating next interewal."));
                final PlayerEntry[] allPlayers = m_playerManager.getAll();
                for (PlayerEntry p : allPlayers) {
                    nextPageChangeIn = Math.min(nextPageChangeIn, p.nextPage(now));
                }
            } while (nextPageChangeIn < 1);

            nextPageChangeIn = Math.min(nextPageChangeIn, MAX_WAIT);
        }

        long minWait = Math.max(1, nextPageChangeIn * TICKS_PER_SECOND / 1000);

        log(String.format(format, "Next page change in " + nextPageChangeIn + "ms (" + minWait + " ticks)"));
        m_scheduler.runTaskLater(m_pluginMain, new Runnable() {
            public void run() {
                pageChangerLoop(token);
            }
        }, minWait);
    }

    /**
     * Start hologram updater
     */
    private void hologramUpdaterStart() {
        final UUID token = UUID.randomUUID();

        synchronized (m_mutex) {
            m_tokenUpdater = token;
            log("[" + token.toString() + "] Starting new hologram updater");
        }

        m_scheduler.runTaskAsynchronously(m_pluginMain, new Runnable() {
            public void run() {
                hologramUpdaterLoop(token);
            }
        });
    }

    /**
     * The hologram updater loop
     *
     * @param token
     */
    private void hologramUpdaterLoop(final UUID token) {
        final String format = "[" + token + "] %s";

        long now = System.currentTimeMillis();
        long nextUpdateIn = Long.MAX_VALUE;

        synchronized (m_mutex) {
            do {
                if (!token.equals(m_tokenUpdater)) {
                    log(String.format(format, "Token changed."));
                    return;
                }

                log(String.format(format, "Calculating next update time."));
                long update;

                for (HologramWrapper h : m_globalHolos) {
                    update = h.update(now);
                    if (update > 0) {
                        nextUpdateIn = Math.min(nextUpdateIn, update);
                    }
                }
                for (List<HologramWrapper> entry : m_playerHolograms.values()) {
                    for (HologramWrapper h : entry) {
                        update = h.update(now);
                        if (update > 0) {
                            nextUpdateIn = Math.min(nextUpdateIn, update);
                        }
                    }
                }
            } while (nextUpdateIn < 1);

            nextUpdateIn = Math.min(nextUpdateIn, MAX_WAIT);
        }

        final long minWait = nextUpdateIn;

        log(String.format(format, "Next update in " + minWait + "ms."));

        m_scheduler.runTaskAsynchronously(m_pluginMain, new Runnable() {
            public void run() {
                try {
                    Thread.sleep(minWait);
                } catch (InterruptedException ex) {
                }
                hologramUpdaterLoop(token);
            }
        });
    }
}
