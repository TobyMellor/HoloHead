package org.primesoft.holostats.skins;


import org.bukkit.ChatColor;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;

/**
 * User: bobacadodl Date: 1/25/14 Time: 10:28 PM
 */
public class ImageMessage {
	private final static char TRANSPARENT_CHAR = ' ';

	private final Color[] colors = {new Color(0, 0, 0), new Color(0, 0, 170), new Color(0, 170, 0),
			new Color(0, 170, 170), new Color(170, 0, 0), new Color(170, 0, 170), new Color(255, 170, 0),
			new Color(170, 170, 170), new Color(85, 85, 85), new Color(85, 85, 255), new Color(85, 255, 85),
			new Color(85, 255, 255), new Color(255, 85, 85), new Color(255, 85, 255), new Color(255, 255, 85),
			new Color(255, 255, 255),};

	private String[] lines;

	public ImageMessage(BufferedImage image, char imgChar) {
		ChatColor[][] chatColors = toChatColorArray(image);
		lines = toImgMessage(chatColors, imgChar);
	}

        private ChatColor[][] toChatColorArray(BufferedImage image) {
		ChatColor[][] chatImg = new ChatColor[image.getWidth()][image.getHeight()];
		for (int x = 0; x < image.getWidth(); x++) {
			for (int y = 0; y < image.getHeight(); y++) {
				int rgb = image.getRGB(x, y);
				ChatColor closest = getClosestChatColor(new Color(rgb, true));
				chatImg[x][y] = closest;
			}
		}
		return chatImg;
	}

	private String[] toImgMessage(ChatColor[][] colors, char imgchar) {
		String[] lines = new String[colors[0].length];
		for (int y = 0; y < colors[0].length; y++) {
			String line = "";
			for (int x = 0; x < colors.length; x++) {
				ChatColor color = colors[x][y];
				line += (color != null) ? colors[x][y].toString() + imgchar : TRANSPARENT_CHAR;
			}
			lines[y] = line + ChatColor.RESET;
		}
		return lines;
	}


	private double getDistance(Color c1, Color c2) {
		// double rmean = (c1.getRed() + c2.getRed()) / 2.0;
		// double r = c1.getRed() - c2.getRed();
		// double g = c1.getGreen() - c2.getGreen();
		// int b = c1.getBlue() - c2.getBlue();
		// double weightR = 2 + rmean / 256.0;
		// double weightG = 4.0;
		// double weightB = 2 + (255 - rmean) / 256.0;
		// return weightR * r * r + weightG * g * g + weightB * b * b;
		return Math.abs(c1.getRed() - c2.getRed()) + Math.abs(c1.getGreen() - c2.getGreen())
				+ Math.abs(c1.getBlue() - c2.getBlue());
	}

	private boolean areIdentical(Color c1, Color c2) {
		return Math.abs(c1.getRed() - c2.getRed()) <= 20 && Math.abs(c1.getGreen() - c2.getGreen()) <= 20
				&& Math.abs(c1.getBlue() - c2.getBlue()) <= 20;

	}

	private ChatColor getClosestChatColor(Color color) {
		if (color.getAlpha() < 128)
			return null;

		int index = 0;
		double best = -1;

		for (int i = 0; i < colors.length; i++) {
			if (areIdentical(colors[i], color)) {
				return ChatColor.values()[i];
			}
		}

		for (int i = 0; i < colors.length; i++) {
			double distance = getDistance(color, colors[i]);
			if (distance < best || best == -1) {
				best = distance;
				index = i;
			}
		}

		// Minecraft has 15 colors
		return ChatColor.values()[index];
	}

	public String[] getLines() {
		return lines;
	}
}
