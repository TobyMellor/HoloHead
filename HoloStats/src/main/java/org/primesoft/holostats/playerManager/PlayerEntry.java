package org.primesoft.holostats.playerManager;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitScheduler;
import org.primesoft.holostats.HoloStatsMain;
import org.primesoft.holostats.hologram.HologramWrapper;

/**
 *
 * @author SBPrime
 */
public class PlayerEntry {

    private Player m_player;
    private String m_name;
    private UUID m_token;
    private final UUID m_uuid;
    private HologramWrapper[] m_holograms;
    private Hologram m_hologram;
    private final Object m_mutex = new Object();
    private final PlayerManager m_playerManager;

    private HologramWrapper m_currentPage;
    private long m_nextPage = 0;

    public PlayerEntry(PlayerManager playerManager,
            Player player, String name) {
        this(playerManager, player, name, player.getUniqueId());
    }

    public PlayerEntry(PlayerManager playerManager,
            String name, UUID uuid) {
        this(playerManager, null, name, uuid);
    }

    private PlayerEntry(PlayerManager playerManager,
            Player player, String name, UUID uuid) {
        m_player = player;
        m_uuid = uuid;
        m_name = name;
        m_holograms = new HologramWrapper[0];
        m_currentPage = null;
        m_playerManager = playerManager;
        m_token = UUID.randomUUID();
    }

    public Player getPlayer() {
        return m_player;
    }

    public UUID getUUID() {
        return m_uuid;
    }

    public String getName() {
        return m_name;
    }

    public boolean isInGame() {
        return m_player.isOnline();
    }

    @Override
    public int hashCode() {
        return m_uuid.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final PlayerEntry other = (PlayerEntry) obj;

        return !(this.m_uuid != other.m_uuid && (this.m_uuid == null || !this.m_uuid.equals(other.m_uuid)));
    }

    /**
     * Update the player after relogin
     *
     * @param player
     */
    public void update(Player player) {
        m_player = player;
    }

    public void update(HologramWrapper[] pHolograms, HologramWrapper[] gHolograms, HologramWrapper[] cHolograms) {
        synchronized (m_mutex) {
            List<HologramWrapper> holograms = new ArrayList<HologramWrapper>();
            if (pHolograms != null) {
                for (HologramWrapper h : pHolograms) {
                    holograms.add(h);
                }
            }
            if (gHolograms != null) {
                for (HologramWrapper h : gHolograms) {
                    holograms.add(h);
                }
            }
            if (cHolograms != null) {
                for (HologramWrapper h : cHolograms) {
                    holograms.add(h);
                }
            }

            Collections.sort(holograms, new Comparator<HologramWrapper>() {
                public int compare(HologramWrapper o1, HologramWrapper o2) {
                    return (int) Math.signum(o1.getSortId() - o2.getSortId());
                }

            });

            m_currentPage = null;
            m_holograms = holograms.toArray(new HologramWrapper[0]);
            nextPage(System.currentTimeMillis());
        }
    }

    /**
     * Set current display hologram
     *
     * @param hologram
     */
    public void setHologram(Hologram hologram) {
        synchronized (m_mutex) {
            if (m_hologram != null) {
                m_hologram.delete();
            }

            m_hologram = hologram;
        }
    }

    /**
     * Move to next hologram page
     *
     * @param now
     * @return
     */
    public long nextPage(long now) {
        synchronized (m_mutex) {
            if (m_nextPage < now || m_currentPage == null) {
                if (m_currentPage == null) {
                    m_currentPage = m_holograms != null && m_holograms.length > 0 ? m_holograms[0] : null;
                } else {
                    int idx = Arrays.binarySearch(m_holograms, m_currentPage);

                    idx = (idx + 1) % m_holograms.length;

                    m_currentPage = m_holograms[idx];
                }

                m_token = UUID.randomUUID();
                m_nextPage = now + (m_currentPage != null ? m_currentPage.stayTime() : 0) * 1000;

                setPage(m_token, m_currentPage);
            }

            return m_nextPage - now;
        }
    }

    /**
     * Set hologram page
     *
     * @param m_token
     * @param m_currentPage
     */
    private void setPage(final UUID token, final HologramWrapper currentPage) {
        final Hologram hologram;
        synchronized (m_mutex) {
            hologram = m_hologram;

            if (hologram == null || hologram.isDeleted()
                    || currentPage == null) {
                return;
            }
        }

        HoloStatsMain plugin = m_playerManager.getParent();
        BukkitScheduler scheduler = plugin.getScheduler();
        scheduler.runTask(plugin, new Runnable() {
            public void run() {
                String[] lines;

                synchronized (m_mutex) {
                    if (!m_token.equals(token) || hologram.isDeleted()) {
                        return;
                    }

                    lines = currentPage.getLines(m_name);
                }

                hologram.clearLines();
                for (String s : lines) {
                    hologram.appendTextLine(s);
                }
            }
        });
    }
}
