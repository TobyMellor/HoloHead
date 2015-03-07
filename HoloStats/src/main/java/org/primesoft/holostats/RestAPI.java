package org.primesoft.holostats;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.ParseException;
import org.primesoft.holostats.configuration.ConfigProvider;
import org.primesoft.holostats.hologram.HologramWrapper;
import org.primesoft.holostats.utils.ExceptionHelper;
import org.primesoft.holostats.utils.InOutParam;
import org.primesoft.holostats.utils.JSONUtils;

/**
 *
 * @author SBPrime
 */
public class RestAPI {

    private static void log(String msg) {
        HoloStatsMain.log(msg);
    }

    private static String buildUrl(String command, String[] params) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%s?hash=%s&gamemode=%s",
                ConfigProvider.getApiUrl(), ConfigProvider.getApiKey(),
                command));

        if (params != null) {
            for (String s : params) {
                if (s != null && !s.isEmpty()) {
                    sb.append("&");
                    sb.append(s);
                }
            }
        }

        return sb.toString();
    }

    /**
     * Download version page from the www
     *
     * @param url Version file http page
     * @return Version page content
     */
    private static String downloadPage(String url) {
        try {
            InputStreamReader is = new InputStreamReader(new URL(url).openStream());
            BufferedReader br = new BufferedReader(is);
            StringBuilder sb = new StringBuilder();
            String line;

            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            br.close();

            return sb.toString();
        } catch (Exception e) {
            ExceptionHelper.printException(e, "Error downloading file " + url);
            return null;
        }
    }

    private static JSONObject get(String url) {
        String result = downloadPage(url);

        if (result == null) {
            return null;
        }

        try {
            return (JSONObject) JSONValue.parseWithException(result);
        } catch (ParseException ex) {
            ExceptionHelper.printException(ex, "Unable to parse server response for " + url);
            return null;
        }
    }

    private static int parseSizeResponse(JSONObject response, String method) {
        InOutParam<Integer> size = InOutParam.Out();
        InOutParam<Integer> error = InOutParam.Out();
        if (JSONUtils.getInt(response, "error", error) && error.getValue() > 0) {
            log("Request for \"" + method + "\" returned an error: " + error.getValue());
            return 0;
        }

        if (!JSONUtils.getInt(response, "size", size)) {
            log("Request for \"" + method + "\" did not return size.");
            return 0;
        }

        return size.getValue();
    }

    private static HologramWrapper parseHologramResponse(JSONObject response, String method) {
        InOutParam<Integer> error = InOutParam.Out();
        if (JSONUtils.getInt(response, "error", error) && error.getValue() > 0) {
            log("Request for \"" + method + "\" returned an error: " + error.getValue());
            return null;
        }

        HologramWrapper result = HologramWrapper.parse(response);

        if (result == null) {
            log("Unable to parse request \"" + method + "\" result.");
            return null;
        }

        return result;
    }

    public static int getGlobalCount() {
        JSONObject response = get(buildUrl("globalHoloCount", null));
        if (response == null) {
            return 0;
        }

        return parseSizeResponse(response, "globalHoloCount");
    }

    public static int getPlayerCount() {
        JSONObject response = get(buildUrl("playerHoloCount", null));
        if (response == null) {
            return 0;
        }

        return parseSizeResponse(response, "playerHoloCount");
    }

    public static HologramWrapper getGlobalHologram(int id) {
        JSONObject response = get(buildUrl("globalHolo", new String[]{
            "holoId=" + id
        }));

        if (response == null) {
            return null;
        }

        return parseHologramResponse(response, "globalHolo");
    }

    public static HologramWrapper getPlayerHologram(int id, String playerName) {
        JSONObject response = get(buildUrl("playerHolo", new String[]{
            "holoId=" + id,
            "player=" + playerName
        }));

        if (response == null) {
            return null;
        }

        return parseHologramResponse(response, "playerHolo");
    }
}
