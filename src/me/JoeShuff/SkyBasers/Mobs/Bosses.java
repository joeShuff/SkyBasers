/*
 * Bukkit Plugin - SkyBasers
 * For use on The Justice Network
 * 
 * @author - Joseph Shufflebotham
 * 
 * © Copyright 2016 
 */
package me.JoeShuff.SkyBasers.Mobs;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import me.JoeShuff.SkyBasers.SkyBasers;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.entity.Blaze;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Ghast;
import org.bukkit.entity.Player;
import org.bukkit.entity.Wither;

public class Bosses {

	private List<EnderDragon> dragons = new ArrayList<EnderDragon>();
	private List<Wither> withers = new ArrayList<Wither>();
	
	private List<Blaze> blazes = new ArrayList<Blaze>();
	private List<Ghast> ghasts = new ArrayList<Ghast>();
	
	private SkyBasers plugin;
	
	private World world;
	
	private Random rnd;
	
	public Bosses(SkyBasers plugin)
	{
		this.plugin = plugin;
		
		world = Bukkit.getWorld(SkyBasers.worldName);
		
		rnd = new Random();
	}
	
	private List<Player> getValidPlayers()
	{
		List<Player> players = new ArrayList<Player>();
		
		for (Player p : Bukkit.getOnlinePlayers())
		{
			if (plugin.getPlayerScoreboards(false).hasAlivePlayer(p))
			{
				players.add(p);
			}
		}
		
		return players;
	}
	
	public void addDragon()
	{
		EnderDragon dragon = (EnderDragon) world.spawnEntity(plugin.getCentreMap(true, 250), EntityType.ENDER_DRAGON);
		dragons.add(dragon);
		
		Bukkit.broadcastMessage(ChatColor.GREEN + "+1 EnderDragon");
	}
	
	public void addWither()
	{
		Wither wither = (Wither) world.spawnEntity(plugin.getCentreMap(true, 250).add(new Random().nextInt(70) - 35, 0, new Random().nextInt(70) - 35), EntityType.WITHER);
		withers.add(wither);
		
		List<Player> players = getValidPlayers();
		
		wither.setTarget(players.get(rnd.nextInt(players.size())));
		
		Bukkit.broadcastMessage(ChatColor.GREEN + "+1 Wither Boss");
	}
	
	public void addBoss()
	{
		int choice = rnd.nextInt(100) + 1;
		
		if (choice < 50)
		{
			addWither();
		}
		else
		{
			addDragon();
		}
	}
	
	public void addBlaze()
	{
		Blaze blaze = (Blaze) world.spawnEntity(plugin.getCentreMap(true, 250).add(new Random().nextInt(70) - 35, 0, new Random().nextInt(70) - 35), EntityType.BLAZE);
		blazes.add(blaze);
		
		blaze.setRemoveWhenFarAway(false);
		
		List<Player> players = getValidPlayers();
		
		blaze.setTarget(players.get(rnd.nextInt(players.size())));
		
		Bukkit.broadcastMessage(ChatColor.GREEN + "+1 Blaze");
	}
	
	public void addGhast()
	{
		Ghast ghast = (Ghast) world.spawnEntity(plugin.getCentreMap(true, 250).add(new Random().nextInt(70) - 35, 0, new Random().nextInt(70) - 35), EntityType.GHAST);
		ghasts.add(ghast);
		
		ghast.setRemoveWhenFarAway(false);
		
		Bukkit.broadcastMessage(ChatColor.GREEN + "+1 Ghast");
	}
	
	public void addInstanceGhast(Ghast g)
	{
		ghasts.add(g);
	}
	
	public void addInstanceBlaze(Blaze b)
	{
		blazes.add(b);
	}
	
	public void addNormalBoss()
	{
		int choice = rnd.nextInt(100) + 1;
		
		if (choice < 50)
		{
			addGhast();
		}
		else
		{
			addBlaze();
		}
	}
	
	public void killAll()
	{
		for (EnderDragon dragon : dragons)
		{
			dragon.remove();
		}
		
		for (Wither wither : withers)
		{
			wither.remove();
		}
		
		for (Blaze blaze : blazes)
		{
			blaze.remove();
		}
		
		for (Ghast ghast : ghasts)
		{
			ghast.remove();
		}
	}
}
