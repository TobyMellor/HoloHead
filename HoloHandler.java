package com.IntenseArmadillo.armadilloholohead;


import com.IntenseArmadillo.armadilloholohead.imgmessage.ImageChar;
import com.IntenseArmadillo.armadilloholohead.imgmessage.ImageMessage;
import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import com.gmail.filoghost.holographicdisplays.api.VisibilityManager;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import javax.imageio.ImageIO;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class HoloHandler {

	public static void handleMotd(final Player p) {
		final Hologram hologram = HologramsAPI.createHologram(Main.instance, p.getLocation());
		VisibilityManager visibilityManager = hologram.getVisibilityManager();
		visibilityManager.showTo(p);
		visibilityManager.setVisibleByDefault(false);
		new Thread(new Runnable() {

			@Override
			public void run() {
				final List<String> face = new ArrayList<String>();
				try {
					face.addAll(Arrays.asList(new ImageMessage(ImageIO.read(new URL("https://minotar.net/avatar/" + p.getName() + "/8.png")), 8, ImageChar.BLOCK
							.getChar()).getLines()));
				} catch (MalformedURLException e) {
				} catch (IOException e) {
				}
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
				}
				Bukkit.getScheduler().scheduleSyncDelayedTask(Main.instance, new Runnable() {
					@Override
					public void run() {
						if (p.isOnline()) {
							if (face.size() == 0) {
								p.sendMessage(getLine("before", p));
								String s = "";
								s = s + getLine("1", p).trim();
								s = s + getLine("2", p).trim();
								;
								s = s + getLine("3", p).trim();
								;
								s = s + getLine("4", p).trim();
								;
								s = s + getLine("5", p).trim();
								;
								p.sendMessage(s);
								p.sendMessage(getLine("after", p));
							} else {
								for (String s : face) {
									hologram.appendTextLine(s);
								}
								hologram.appendTextLine(p.getName());
							}


						}
					}
				});
			}
		}).start();

	}

	public static String getLine(String n, Player p) {
		String line = Main.instance.getConfig().getString("line-" + n);
		line = line.replace("%name", p.getName());
		line = line.replace("%display", p.getDisplayName());
		line = line.replace("%total", p.getServer().getOnlinePlayers().size() + "");
		line = ChatColor.translateAlternateColorCodes('&', line);
		return line;
	}

}
