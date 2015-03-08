package org.primesoft.holostats.hologram;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.bukkit.ChatColor;
import org.json.simple.JSONObject;
import org.primesoft.holostats.RestAPI;
import org.primesoft.holostats.skins.SkinProvider;
import org.primesoft.holostats.utils.InOutParam;
import org.primesoft.holostats.utils.JSONUtils;

/**
 *
 * @author SBPrime
 */
public final class HologramWrapper {

    /**
     * Parse JSON object into HologramWrapper
     *
     * @param o
     * @return
     */
    public static HologramWrapper parse(JSONObject o, HologramType type, String player) {
        if (o == null) {
            return null;
        }

        long now = System.currentTimeMillis();

        String title = JSONUtils.getString(o, "title", null);
        String lines = JSONUtils.getString(o, "lines", null);
        int stayTime = JSONUtils.getInt(o, "stayTime", 5);
        int updateIn = JSONUtils.getInt(o, "updateIn", -1);
        int sortId = JSONUtils.getInt(o, "sortId", -1);
        String login = JSONUtils.getString(o, "player", null);

        InOutParam<Integer> id = InOutParam.Out();
        if (!JSONUtils.getInt(o, "holoId", id)) {
            return null;
        }

        return new HologramWrapper(id.getValue(), type, title, lines, login,
                stayTime, updateIn > 0 ? (now + updateIn * 1000) : -1, sortId,
                player);
    }

    /**
     * The current player login
     */
    private final String m_player;
    
    /**
     * Mta mutex
     */
    private final Object m_mutex = new Object();

    /**
     * The hologram lines
     */
    private String[] m_lines;

    /**
     * How long should the hologram be displayed
     */
    private int m_stayTime;

    /**
     * Next hologram update
     */
    private long m_nextUpdate;

    /**
     * Hologram sort ID
     */
    private final int m_sortId;

    /**
     * The hologram ID
     */
    private final int m_id;

    private final HologramType m_type;

    public HologramType getType() {
        return m_type;
    }

    public int getId() {
        return m_id;
    }

    /**
     * Get the sort ID
     *
     * @return
     */
    public int getSortId() {
        return m_sortId;
    }

    /**
     * Should the hologram be updated
     *
     * @param now
     * @return
     */
    public long update(long now) {
        synchronized (m_mutex) {
            if (m_nextUpdate == -1) {
                return -1;
            }

            if (m_nextUpdate <= now) {
                JSONObject o = null;

                switch (m_type) {
                    default:
                        break;
                    case Player:
                        o = RestAPI.getPlayerObject(m_id, m_player);
                        break;
                    case Global:
                        o = RestAPI.getGlobalObject(m_id);
                        break;
                }

                if (o != null) {
                    String title = JSONUtils.getString(o, "title", null);
                    String lines = JSONUtils.getString(o, "lines", null);
                    int stayTime = JSONUtils.getInt(o, "stayTime", 5);
                    int updateIn = JSONUtils.getInt(o, "updateIn", -1);
                    String login = JSONUtils.getString(o, "player", null);

                    initializeHologram(title, lines, login, stayTime, updateIn > 0 ? (now + updateIn * 1000) : -1);
                }
            }

            return m_nextUpdate - now;
        }
    }

    /**
     * NUmber of secconds the hologram should stay on
     *
     * @return
     */
    public int stayTime() {
        return m_stayTime;
    }

    private HologramWrapper(int id, HologramType type, String title, String lines, String login, int stayTime, long nextUpdate, int sortId,
            String player) {
        m_id = id;
        m_type = type;
        m_sortId = sortId;
        m_player = player;

        initializeHologram(title, lines, login, stayTime, nextUpdate);
    }

    private void initializeHologram(String title, String message, String login,
            int stayTime, long nextUpdate) {
        List<String> holoLines = new ArrayList<String>();
        if (title != null && !title.isEmpty()) {
            holoLines.add(title);
        }

        final String[] lines = message.replaceAll("(\r\n)|(\n\r)", "\n").split("\n");
        final String[] skinLines;

        if (login != null && !login.isEmpty()) {
            skinLines = SkinProvider.getSkinLines(login);
        } else {
            skinLines = new String[0];
        }

        holoLines.addAll(Arrays.asList(skinLines));
        holoLines.addAll(Arrays.asList(lines));

        synchronized (m_mutex) {
            m_lines = holoLines.toArray(new String[0]);
            m_stayTime = Math.max(stayTime, 1);
            m_nextUpdate = nextUpdate;
        }
    }

    /**
     * Get the hologram lines for login
     *
     * @param login
     * @return
     */
    public String[] getLines(String login) {
        synchronized (m_mutex) {
            String[] result = new String[m_lines.length];

            for (int i = 0; i < m_lines.length; i++) {
                String s = m_lines[i];

                s = s.replaceAll("%player", login);

                result[i] = s;
            }

            return result;
        }
    }
}
