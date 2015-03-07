package org.primesoft.holostats.hologram;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.ChatColor;
import org.json.simple.JSONObject;
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
    public static HologramWrapper parse(JSONObject o) {
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

        return new HologramWrapper(id.getValue(), title, lines, login,
                stayTime, updateIn > 0 ? (now + updateIn * 1000) : -1, sortId);
    }

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
    private int m_sortId;

    /**
     * The hologram ID
     */
    private int m_id;

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
    public boolean update(long now) {
        return m_nextUpdate != -1 && now > m_nextUpdate;
    }

    /**
     * NUmber of secconds the hologram should stay on
     *
     * @return
     */
    public int stayTime() {
        return m_stayTime;
    }

    private HologramWrapper(int id, String title, String lines, String login, int stayTime, long nextUpdate, int sortId) {
        m_id = id;
        InitializeHologram(title, lines, login, stayTime, nextUpdate, sortId);
    }

    public void InitializeHologram(String title, String message,
            String login,
            int stayTime, long nextUpdate, int sortId) {
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

        int pos = 0;
        for (; pos < skinLines.length; pos++) {
            StringBuilder sb = new StringBuilder();
            sb.append(skinLines[pos]);

            if (pos < lines.length) {
                sb.append(" ");
                sb.append(lines[pos]);
            }

            holoLines.add(sb.toString());
        }

        for (; pos < lines.length; pos++) {
            holoLines.add(lines[pos]);
        }

        synchronized (m_mutex) {
            m_lines = holoLines.toArray(new String[0]);
            m_stayTime = Math.max(stayTime, 1);
            m_nextUpdate = nextUpdate;
            m_sortId = sortId;
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

            int maxLen = 0;
            for (int i = 0; i < m_lines.length; i++) {
                String s = m_lines[i];
                maxLen = Math.max(maxLen, ChatColor.stripColor(s).length());

                s = s.replaceAll("%player", login);

                result[i] = s;
            }

            for (int i = 0; i < result.length; i++) {
                StringBuilder sb = new StringBuilder(result[i]);
                while (sb.length() < maxLen) {
                    sb.append(" ");
                }
                result[i] = sb.toString();
            }

            return result;
        }
    }
}
