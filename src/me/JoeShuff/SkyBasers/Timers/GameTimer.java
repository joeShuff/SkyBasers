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
import me.JoeShuff.SkyBasers.SkyBasers.GameStage;
import me.JoeShuff.SkyBasers.SkyBasers.GameState;
import me.JoeShuff.SkyBasers.SkyBasers.GameType;
import me.JoeShuff.SkyBasers.VisualEffects;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.scheduler.BukkitRunnable;

public class GameTimer extends BukkitRunnable {

	private SkyBasers plugin;
	
	public GameTimer(SkyBasers plugin)
	{
		this.plugin = plugin;
		plugin.getGameListener().testGameOver();
		
		this.runTaskTimer(plugin, 20, 20);
		this.mins = plugin.getConfig().getInt("game-time");
	}
	
	public static int timePassed = 0;
	
	int mins = 10;
	int seconds = 00;
	
	@Override
	public void run() {
		seconds --;
		timePassed ++;
		
		//plugin.getSupplyDropper().attemptSupplyDrop();
		
		if (plugin.getState() == GameState.FINISHED)
		{
			this.cancel();
		}
		
		if (seconds == 0 && mins == 0)
		{
			//START RISING LAVA
			
			for (Player player : Bukkit.getOnlinePlayers())
			{
				VisualEffects.sendTitle(player, ChatColor.RED + "LAVA IS RISING",ChatColor.YELLOW + "PVP Activated!");
			}
			
			Bukkit.getWorld(SkyBasers.worldName).setPVP(true);
			plugin.setStage(GameStage.LAVA);
			this.cancel();
			
			if (plugin.getType() == GameType.NORMAL)
			{
				new LavaTimer((int) plugin.getCentreMap(false, 0).getY(), plugin, (int) plugin.getCentreMap(false, 0).getX(), (int) plugin.getCentreMap(false, 0).getZ(),230,215);
			}
			else
			{
				new LavaTimer((int) plugin.getCentreMap(false, 0).getY(), plugin, (int) plugin.getCentreMap(false, 0).getX(), (int) plugin.getCentreMap(false, 0).getZ(),245,230);
			}
			
			for (String name : plugin.getPlayerScoreboards(false).getAlivePlayers())
			{
				if (Bukkit.getPlayer(name) == null)
				{
					plugin.getGameListener().playerDeath(Bukkit.getOfflinePlayer(name), DamageCause.CUSTOM);
				}
			}
			
			return;
		}
		
		if (seconds == 0 && mins == 5)
		{
			for (Player player : Bukkit.getOnlinePlayers())
			{
				VisualEffects.sendTitle(player, ChatColor.GOLD + "LAVA RISING IN " + ChatColor.RED + "5" + ChatColor.GOLD + " MINUTES","");
			}
		}
		else if (seconds == 0 && mins == 2)
		{
			for (Player player : Bukkit.getOnlinePlayers())
			{
				VisualEffects.sendTitle(player, ChatColor.GOLD + "LAVA RISING IN " + ChatColor.RED + "2" + ChatColor.GOLD + " MINUTES","");
			}
		}
		else if (seconds == 0 && mins == 1)
		{
			for (Player player : Bukkit.getOnlinePlayers())
			{
				VisualEffects.sendTitle(player, ChatColor.GOLD + "LAVA RISING IN " + ChatColor.RED + "1" + ChatColor.GOLD + " MINUTE","");
			}
		}
		
		if (mins == 0 && seconds <= 10)
		{
			String message = "";
			if (seconds == 1)
			{
				message = ChatColor.GOLD + "LAVA RISING IN " + ChatColor.RED + seconds + ChatColor.GOLD + " SECOND";
			}
			else
			{
				message = ChatColor.GOLD + "LAVA RISING IN " + ChatColor.RED + seconds + ChatColor.GOLD + " SECONDS";
			}
			
			for (Player player : Bukkit.getOnlinePlayers())
			{
				VisualEffects.sendTitle(player, message,ChatColor.RED + "Prepare to fight!");
			}
		}
		
		if (seconds < 0)
		{
			mins --;
			seconds = 59;
		}	
		
		String timeLeft;
		
		if (mins > 0)
		{
			timeLeft = String.format("%02d", mins) + ":" + String.format("%02d", seconds);
			plugin.getPlayerScoreboards(false).setTime(timeLeft);
		}
		else
		{
			timeLeft = "00:" + String.format("%02d", seconds);
			plugin.getPlayerScoreboards(false).setTime(timeLeft);
		}
	}

}
