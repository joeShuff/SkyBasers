/*
 * Bukkit Plugin - SkyBasers
 * For use on The Justice Network
 * 
 * @author - Joseph Shufflebotham
 * 
 * © Copyright 2016 
 */
package me.JoeShuff.SkyBasers.Timers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import me.JoeShuff.SkyBasers.SkyBasers;
import me.JoeShuff.SkyBasers.SkyBasers.GameState;
import me.JoeShuff.SkyBasers.Mobs.MobControl;
import me.JoeShuff.SkyBasers.MySQL.SQLManager;
import me.JoeShuff.SkyBasers.WorldGen.Generator;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class PreGameTimer extends BukkitRunnable
{
	SkyBasers plugin;
	
	public PreGameTimer(SkyBasers plugin)
	{
		this.plugin = plugin;
		
		this.seconds = plugin.getConfig().getInt("start-time");
		
		this.runTaskTimer(plugin, 20, 20);
	}
	
	public PreGameTimer(SkyBasers plugin, boolean forced, int forcedPlayers)
	{
		this.plugin = plugin;
		
		this.forced = forced;
		
		this.forcedPlayers = forcedPlayers;
		
		this.seconds = plugin.getConfig().getInt("start-time");
		
		this.runTaskTimer(plugin, 20, 20);
	}
	
	int seconds = 60;
	
	int forcedPlayers = 0;
	boolean forced = false;
	
	List<String> teleported = new ArrayList<String>();
	
	@Override
	public void run() {
		
		int minplayers = plugin.getConfig().getInt("min-players");
		
		if (!forced)
		{
			if (Bukkit.getOnlinePlayers().size() < minplayers)
			{
				plugin.getScoreBoardManager().setStartTime("Waiting for players...", 0);
				plugin.getScoreBoardManager().updateScores(0);
				plugin.setStarting(false);
				plugin.setCanRequest(true);
				this.cancel();
				return;
			}
		}
		else
		{
			if (Bukkit.getOnlinePlayers().size() < forcedPlayers)
			{
				plugin.getScoreBoardManager().setStartTime("Waiting for players...", 0);
				plugin.getScoreBoardManager().updateScores(0);
				plugin.setStarting(false);
				plugin.setCanRequest(true);
				this.cancel();
				return;
			}
		}
		
		
		seconds --;
		
		plugin.getScoreBoardManager().setStartTime("" + seconds, 0);
		
		if (seconds == 3)
		{
			SQLManager.sendServerRequest(Bukkit.getServerName(), plugin.getConfig().getInt("max-players"), "null", "false", true);
			plugin.setCanRequest(false);
		}
		
		if (seconds <= 0)
		{
			
			if (plugin.isTeams())
			{
				this.plugin.getTeamManager().createRemaindingTeams();
				
				if (this.plugin.getTeamManager().amountOfTeams() <= 1)
				{
					Bukkit.broadcastMessage(ChatColor.RED + "Not enough teams to start, resetting timer!");
					plugin.getScoreBoardManager().setStartTime("Waiting for players...", 0);
					plugin.getScoreBoardManager().updateScores(0);
					plugin.setStarting(false);
					plugin.setCanRequest(true);
					this.cancel();
					return;
				}
			}
			
			List<Player> players = new ArrayList<Player>();
			players.addAll(Bukkit.getOnlinePlayers());
			Collections.shuffle(players);
			
			for (Player player : players)
			{
				player.setLevel(0);
				player.setExp(0);
				player.getInventory().clear();

				player.getInventory().setHelmet(null);
				player.getInventory().setChestplate(null);
				player.getInventory().setLeggings(null);
				player.getInventory().setBoots(null);
				
				if (!teleported.contains(player.getName()))
				{
					Location l = getNewLoc(player,plugin.getConfig().getInt("mapCentreX"), plugin.getConfig().getInt("mapCentreZ"), 32);
					
					player.teleport(l);
					teleported.add(player.getName());
					
					if (plugin.isTeams())
					{
						String playername = plugin.getTeamManager().getTeamMate(player);
						
						try {
							Bukkit.getPlayer(playername).teleport(l);
							teleported.add(playername);
						} catch (Exception e) {}
					}
				}
				
				player.setGameMode(GameMode.ADVENTURE);
			}	
		
			plugin.setState(GameState.LOADING);
			
			this.cancel();
			new Generator(plugin);
			new MobControl(plugin);
		}
	}
	
	private Location getNewLoc(Player player, int centreX, int centreZ, int spread)
	{
		Random rnd = new Random();
		
		int changeX = rnd.nextInt(spread * 2) - spread;
		int changeZ = rnd.nextInt(spread * 2) - spread;
		
		int yaw = 0;
		int pitch = 90;
		
		Location newLoc = new Location(Bukkit.getWorld(SkyBasers.worldName), centreX + changeX, 151, centreZ + changeZ);
		newLoc.setPitch(pitch);
		newLoc.setYaw(yaw);
		
		double distance = 2;
		
		if (plugin.isTeams())
		{
			distance += 2;
		}
		
		boolean validSpawn = true;
		
		for (Entity e : newLoc.getWorld().getNearbyEntities(newLoc, distance, distance, distance))
		{
			if (e instanceof Player)
			{
				validSpawn = false;
				break;
			}
		}
		
		if (!validSpawn)
		{
			return getNewLoc(player, centreX, centreZ, spread); 
		}
		
		Block block = Bukkit.getWorld(SkyBasers.worldName).getBlockAt(centreX + changeX, 150, centreZ + changeZ);
		
		if (!SQLManager.buildPod(player, new Location(Bukkit.getWorld(SkyBasers.worldName), centreX + changeX, 150, centreZ + changeZ)))
		{
			block.setType(Material.DIRT);
		}
		
		newLoc = newLoc.add(0.5,0,0.5);
		
		return newLoc;
	}

}
