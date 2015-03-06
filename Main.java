package com.IntenseArmadillo.armadilloholohead;

import java.util.Collection;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;

public class Main extends JavaPlugin implements Listener{
	public static Main instance;
	
	@Override
	public void onEnable() {

	    if (!Bukkit.getPluginManager().isPluginEnabled("HolographicDisplays")) {
	        getLogger().severe("*** HolographicDisplays is not installed or not enabled. ***");
	        getLogger().severe("*** This plugin will be disabled. ***");
	        this.setEnabled(false);
	        return;
	    }
	    
	    instance = this;
	    
		saveDefaultConfig();
		Bukkit.getPluginManager().registerEvents(this, this);
	}
	
	public void onDisable(){
		System.out.print(":(");
	}
	
	public static final Main getPlugin() {
		return instance;
	}

	@EventHandler
	public void onJoin(PlayerJoinEvent e) {
		HoloHandler.handleMotd(e.getPlayer());
	}
	
	@EventHandler
	public void onQuit(PlayerQuitEvent e) {
		System.out.print(HologramsAPI.getHolograms(getPlugin()).toString());
		for(Hologram s : HologramsAPI.getHolograms(getPlugin())){
			System.out.print(s.toString());
			if(s.getLine(8).equals("IntenseArmadillo")){
				HologramsAPI.getHolograms(getPlugin()).remove(this);
				System.out.print("Yes " + s.getLine(8).toString());
			}
			System.out.print("No " + s.getLine(8).toString());
		}
			
	}
	
    public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
    	if(sender instanceof Player){
    		Player player = (Player) sender;
			if(command.getLabel().equalsIgnoreCase("holohead")){
				System.out.print("Command recieved");
				HoloHandler.handleMotd(player);
				return true;
			}
		}
		return false;
    }
}
