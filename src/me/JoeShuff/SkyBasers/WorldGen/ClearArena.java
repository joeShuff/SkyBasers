/*
 * Bukkit Plugin - SkyBasers
 * For use on The Justice Network
 * 
 * @author - Joseph Shufflebotham
 * 
 * © Copyright 2016 
 */
package me.JoeShuff.SkyBasers.WorldGen;

import me.JoeShuff.SkyBasers.SkyBasers;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Difficulty;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.scheduler.BukkitRunnable;

public class ClearArena extends BukkitRunnable implements Listener {

	public ClearArena(SkyBasers plugin)
	{
		Bukkit.getServer().getPluginManager().registerEvents(this, plugin);
		
		Bukkit.getWorld(SkyBasers.worldName).setDifficulty(Difficulty.PEACEFUL);
		
		startLevel = 0;
		
		centreX = plugin.getConfig().getInt("mapCentreX");
		centreY = plugin.getConfig().getInt("mapCentreY");
		centreZ = plugin.getConfig().getInt("mapCentreZ");
		
		emptyChests();
		
		this.runTaskTimer(plugin, 10,10);
	}
	
	private void emptyChests()
	{
		for(Chunk c : Bukkit.getWorld(SkyBasers.worldName).getLoadedChunks()){
			for(BlockState b : c.getTileEntities()){
				if(b instanceof Chest){
					((Chest) b).getBlockInventory().clear();;
				}
			}
		}
	}
	
	private int startLevel;
	
	private int centreX;
	private int centreY;
	private int centreZ;
	
	@Override
	public void run() {
	
		startLevel ++;
		
		if (centreY + startLevel == 254)
		{
			Bukkit.getServer().broadcastMessage(ChatColor.GREEN + "Clearing Complete!");
			HandlerList.unregisterAll(this);
			this.cancel();
			Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "reload");
		}
		
		for (int p = -35 ; p <= 35 ; p ++)
		{
			for (int q = -35 ; q <= 35 ; q ++)
			{
				Bukkit.getWorld(SkyBasers.worldName).getBlockAt(centreX + p, centreY + 165 - startLevel, centreZ + q).setType(Material.AIR);
			}
		}
	}
	
	@EventHandler
	public void chunkUnload(ChunkUnloadEvent event)
	{
		event.setCancelled(true);
	}
	
}
