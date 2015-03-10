package org.primesoft.holostats.playerManager;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import org.bukkit.entity.Player;
import org.primesoft.holostats.hologram.HologramWrapper;

/**
 *
 * @author SBPrime
 */
public class PlayerEntry {

    private Player m_player;
    private String m_name;
    private final UUID m_uuid;
    private List<HologramWrapper> m_holograms;
    private Hologram m_hologram;
    private final Object m_mutex = new Object();

    private HologramWrapper m_currentPage;
    private long m_nextPage = 0;

    public PlayerEntry(Player player, String name) {
        this(player, name, player.getUniqueId());
    }

    public PlayerEntry(String name, UUID uuid) {
        this(null, name, uuid);
    }

    private PlayerEntry(Player player, String name, UUID uuid) {
        m_player = player;
        m_uuid = uuid;
        m_name = name;
        m_holograms = new ArrayList<HologramWrapper>();
        m_currentPage = null;
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
                holograms.addAll(Arrays.asList(pHolograms));
            }
            if (gHolograms != null) {
                holograms.addAll(Arrays.asList(gHolograms));
            }
            if (cHolograms != null) {
                holograms.addAll(Arrays.asList(cHolograms));
            }

            Collections.sort(holograms, new Comparator<HologramWrapper>() {
                public int compare(HologramWrapper o1, HologramWrapper o2) {
                    return (int) Math.signum(o1.getSortId() - o2.getSortId());
                }

            });

            m_currentPage = null;
            m_holograms.clear();
            m_holograms.addAll(holograms);
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
            if (m_nextPage <= now || m_currentPage == null) {
                if (m_currentPage == null) {
                    m_currentPage = !m_holograms.isEmpty() ? m_holograms.get(0) : null;
                } else {
                    int idx = m_holograms.indexOf(m_currentPage);

                    idx = (idx + 1) % m_holograms.size();

                    m_currentPage = m_holograms.get(idx);
                }

                long stayTime = m_currentPage != null ? m_currentPage.stayTime() : 0;
                if (stayTime <= 0) {
                    m_nextPage = -1;
                } else {
                    m_nextPage = now + stayTime * 1000;
                }

                setPage(m_currentPage);
            }
            return m_nextPage < 0 ? -1 : (m_nextPage - now);
        }
    }

    /**
     * Set hologram page
     *
     * @param m_token
     * @param m_currentPage
     */
    private void setPage(final HologramWrapper currentPage) {
        final Hologram hologram;

        synchronized (m_mutex) {
            hologram = m_hologram;

            if (hologram == null || hologram.isDeleted()) {
                return;
            }
        }

        final String[] lines = currentPage != null ? currentPage.getLines(m_name) : new String[0];

        hologram.clearLines();
        for (String s : lines) {
            hologram.appendTextLine(s);
        }
    }
}
