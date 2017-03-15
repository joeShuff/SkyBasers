/*
 * Bukkit Plugin - SkyBasers
 * For use on The Justice Network
 * 
 * @author - Joseph Shufflebotham
 * 
 * © Copyright 2016 
 */
package me.JoeShuff.SkyBasers.Timers;

import java.util.Random;

import me.JoeShuff.SkyBasers.SkyBasers;
import me.JoeShuff.SkyBasers.SkyBasers.GameStage;
import me.JoeShuff.SkyBasers.SkyBasers.GameState;
import me.JoeShuff.SkyBasers.SkyBasers.GameType;
import me.JoeShuff.SkyBasers.VisualEffects;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

public class LavaTimer extends BukkitRunnable {

	private SkyBasers plugin;
	
	public LavaTimer(int startheight, SkyBasers plugin, int x, int z, int max, int min)
	{
		this.plugin = plugin;
		
		this.height = startheight;
		this.maxHeight = max;
		this.minStop = min;
		
		this.xCentre = x;
		this.zCentre = z;
		
		if (plugin.getType() == GameType.NORMAL)
		{
			this.runTaskTimer(plugin, 50, 50);
		}
		else
		{
			this.runTaskTimer(plugin, 20, 20);
		}
	}
	
	private Random rnd = new Random();
	
	private int minStop;
	private int maxHeight;
	public static int height;
	private int xCentre;
	private int zCentre;
	
	private boolean cover = false;
	
	@Override
	public void run() {
		
		if (plugin.getState() == GameState.FINISHED)
		{
			this.cancel();
		}
		
		if (height > minStop)
		{
			int chance = rnd.nextInt(100) + 1;
			
			if (!(chance < 50 && height < maxHeight))
			{
				for (Player p : Bukkit.getOnlinePlayers())
				{
					VisualEffects.sendTitle(p, ChatColor.RED + "Lava has STOPPED", ChatColor.DARK_RED + "Fight to the Death!");
					
					if (plugin.getPlayerScoreboards(false).hasAlivePlayer(p))
					{
						p.getInventory().addItem(new ItemStack(Material.COBBLESTONE, 64));
					}
				}
				
				int timeLeftMin = plugin.getConfig().getInt("game-over-time");
				
				plugin.getPlayerScoreboards(false).setTime(String.format("%02d", timeLeftMin) + ":00");
				
				plugin.setStage(GameStage.AFTER);
				
				this.cancel();
			}
		}
		
		for (int i = -35 ; i <= 35 ; i ++)
		{
			for (int j = -35 ; j <= 35 ; j ++)
			{
				if (Bukkit.getWorld(SkyBasers.worldName).getBlockAt(xCentre + i, height, zCentre + j).getType() == Material.AIR)
				{
					Bukkit.getWorld(SkyBasers.worldName).getBlockAt(xCentre + i,  height, zCentre + j).setType(Material.LAVA);
				}
			}
		}
		
		cover = true;
		
		for (Player p : Bukkit.getOnlinePlayers())
		{
			VisualEffects.sendActionBar(p, "" + ChatColor.BLUE + ChatColor.BOLD + "The Lava is at Y" + ChatColor.AQUA + ChatColor.BOLD + height);
			
			if (p.getLocation().getY() < height)
			{
				if (plugin.getPlayerScoreboards(false).hasAlivePlayer(p))
				{
					p.getLocation().getBlock().setType(Material.LAVA);
				}
			}
		}
		
		plugin.getPlayerScoreboards(false).update();
		height ++;
	}

}
