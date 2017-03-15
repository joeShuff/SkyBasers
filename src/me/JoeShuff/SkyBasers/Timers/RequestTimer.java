/*
 * Bukkit Plugin - SkyBasers
 * For use on The Justice Network
 * 
 * @author - Joseph Shufflebotham
 * 
 * © Copyright 2016 
 */
package me.JoeShuff.SkyBasers.Timers;

import me.JoeShuff.SkyBasers.SkyBasers;
import me.JoeShuff.SkyBasers.VisualEffects;
import me.JoeShuff.SkyBasers.SkyBasers.GameState;
import me.JoeShuff.SkyBasers.Items.HubItem;
import me.JoeShuff.SkyBasers.MySQL.SQLManager;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class RequestTimer extends BukkitRunnable {

	private SkyBasers plugin;
	
	private int maxPlayers;
	
	private int currentPlayers;
	
	public RequestTimer(SkyBasers plugin) 
	{
		this.plugin = plugin;
		servername = Bukkit.getServerName();
		
		lastRequest = 0;
		
		this.runTaskTimer(plugin, 40, 40);
	}
	
	private String servername;
	
	private int lastRequest;
	
	private String lastType = "";
	
	@Override
	public void run() {
		
		if (plugin.getState() == GameState.PREGAME)
		{
			if (plugin.kitsEnabled())
			{
				for (Player p : Bukkit.getOnlinePlayers())
				{
					if (!HubItem.countdown.containsKey(p.getName()))
					{
						String chosenClass = SQLManager.getClass(p);
						VisualEffects.sendActionBar(p, "" + ChatColor.YELLOW + ChatColor.BOLD + "You have selected kit " + ChatColor.GOLD + ChatColor.BOLD + chosenClass);
					}
				}
			}
			
			if (!plugin.canRequest())
			{
				return;
			}
			
			currentPlayers = Bukkit.getOnlinePlayers().size();
			maxPlayers = plugin.getConfig().getInt("max-players");
			
			String type = plugin.getType().toString();
			
			if (plugin.isTeams())
			{
				type = type + "_TEAM";
			}
			else
			{
				type = type + "_SOLO";
			}
			
			int playersToRequest = maxPlayers - currentPlayers;
			
			
			
			String force = "false";
			
			if (playersToRequest != lastRequest || SQLManager.hasBungeeRestarted())
			{
				force = "true";
			}
			
			lastRequest = playersToRequest;
			
			if (!lastType.equals(type))
			{
				force = "true";
			}
			
			lastType = type;
			
			if (playersToRequest == 0) return;
			
			SQLManager.sendServerRequest(servername, playersToRequest, type, force,false);
		}
		else
		{
			this.cancel();
		}
		
	}
	
}
