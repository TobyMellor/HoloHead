package org.primesoft.holostats.skins;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import javax.imageio.ImageIO;
import javax.imageio.ImageTypeSpecifier;
import org.primesoft.holostats.HoloStatsMain;
import org.primesoft.holostats.utils.ExceptionHelper;

/**
 *
 * @author SBPrime
 */
public class SkinProvider {

    private final static String SKIN_URL = "http://skins.minecraft.net/MinecraftSkins/%s.png";

    private static void log(String msg) {
        HoloStatsMain.log(msg);
    }

    /**
     * Try to download player skin
     *
     * @param playerLogin
     * @return
     */
    public static BufferedImage downloadSkin(String playerLogin) {
        String fileUrl = String.format(SKIN_URL, playerLogin);
        try {
            URL url = new URL(fileUrl);

            BufferedImage img = (BufferedImage) ImageIO.read(url);
            if (img == null) {
                log("Unable to download player \"" + playerLogin + "\" skin");
                return null;
            }

            BufferedImage result = new BufferedImage(8, 8, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = result.createGraphics();
            g.drawImage(img, 0, 0, 8, 8, 8, 8, 16, 16, null);
            g.dispose();

            return result;
        } catch (Exception e) {
            ExceptionHelper.printException(e, "Unable to download player \"" + playerLogin + "\" skin");
            return null;
        }
    }

    public static String[] getSkinLines(String playerLogin) {
        BufferedImage skin = downloadSkin(playerLogin);

        if (skin == null) {
            return null;
        }

        ImageMessage img = new ImageMessage(skin, ImageChar.BLOCK.getChar());
        return img.getLines();
    }
}
