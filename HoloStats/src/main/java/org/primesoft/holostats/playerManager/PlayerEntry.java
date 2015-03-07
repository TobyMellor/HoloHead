package org.primesoft.holostats.playerManager;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import java.util.ArrayList;
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
    private HologramWrapper[] m_holograms;
    private Hologram m_hologram;
    private final Object m_mutex = new Object();

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
        m_holograms = new HologramWrapper[0];
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

            m_holograms = holograms.toArray(new HologramWrapper[0]);

            System.out.println(m_name);
            for (HologramWrapper h : m_holograms) {
                System.out.println("--------------------------------");
                for (String s : h.getLines(m_name)) {
                    System.out.println("\"" + s + "\"");
                }
                System.out.println("--------------------------------");
            }
        }
    }

    
    /**
     * Set current display hologram
     * @param hologram 
     */
    public void setHologram(Hologram hologram) {
        synchronized(m_mutex){
            if (m_hologram != null) {
                m_hologram.delete();                
            }
            
            m_hologram = hologram;
        }
    }
}
