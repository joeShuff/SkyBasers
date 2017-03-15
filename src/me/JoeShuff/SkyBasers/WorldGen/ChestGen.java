/*
 * Bukkit Plugin - SkyBasers
 * For use on The Justice Network
 * 
 * @author - Joseph Shufflebotham
 * 
 * © Copyright 2016 
 */
package me.JoeShuff.SkyBasers.WorldGen;

import java.util.Random;

import me.JoeShuff.SkyBasers.SkyBasers;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;

public class ChestGen {
	
	SkyBasers plugin;
	
	WeightedItemGen itemGenerator;
	
	public ChestGen(SkyBasers plugin)
	{
		this.plugin = plugin;
		
		xCentre = plugin.getConfig().getInt("mapCentreX");
		yCentre = plugin.getConfig().getInt("mapCentreY");
		zCentre = plugin.getConfig().getInt("mapCentreZ");
	
		itemGenerator = new WeightedItemGen(plugin);
	}
	
	//Some code courtesy of xTrollxDudex on Bukkit.org
	
	private void fillChests()
	{
		for(Chunk c : Bukkit.getWorld(SkyBasers.worldName).getLoadedChunks()){
			for(BlockState b : c.getTileEntities()){
				if(b instanceof Chest){
					Inventory inv = ((Chest) b).getBlockInventory();
					fill(inv);
					Bukkit.getWorld(SkyBasers.worldName).playSound(b.getLocation(), Sound.CHEST_OPEN, 1, 1);
				}
			}
		}
	}
	
	private void fill(Inventory inv){
		inv.clear();
		 
		for(int i = 0; i < 26; i++){
			Random r = new Random();
		 
			int gen = r.nextInt(26);
			if((i >= gen - 3) && (i <= gen + 3))
			{
				inv.setItem(i, getItem());
			}
		}
	}
	
	private ItemStack getItem()
	{
		return itemGenerator.getItem();
	}
	
	private int xCentre = 0;
	private int yCentre = 0;
	private int zCentre = 0;
	
	public void generateChests()
	{
		Random rnd = new Random();
		
		int chestAttempts = plugin.getspawnRatesData().getInt("chest-attempts");
		int chestChance = plugin.getspawnRatesData().getInt("chest-chance");
		
		for (int i = 0 ; i < chestAttempts ; i ++)
		{
			int chance = rnd.nextInt(100) + 1;
			
			if (chance > chestChance)
			{
				continue;
			}
			
			
			
			Location l = getLoc();
			
			Bukkit.getWorld(SkyBasers.worldName).getBlockAt(l).setType(Material.CHEST);
			
			int X = (int) l.getX();
			int Y = (int) l.getY();
			int Z = (int) l.getZ();
			
			for (int x = -1; x <= 1 ; x ++)
			{
				for (int y = -1; y <= 1 ; y ++)
				{
					for (int z = -1; z <= 1 ; z ++)
					{
						Block block = Bukkit.getWorld(SkyBasers.worldName).getBlockAt(X + x, Y + y, Z + z);
						
						if (block.getType() != Material.CHEST && block.getType() != Material.BEDROCK)
						{
							block.setType(Material.AIR);
						}
					}
				}
			}	
		}
					
		
		fillChests();
	}
	
	private Location getLoc()
	{
		Random rnd = new Random();
		
		int selectorX = rnd.nextInt(71) - 35;
		int selectorY = rnd.nextInt(21) + 1;
		int selectorZ = rnd.nextInt(71) - 35;
		
		int worldX = plugin.getConfig().getInt("mapCentreX");
		int worldY = plugin.getConfig().getInt("mapCentreY");
		int worldZ = plugin.getConfig().getInt("mapCentreZ");
		
		return new Location(Bukkit.getWorld(SkyBasers.worldName), worldX + selectorX, worldY + selectorY, worldZ + selectorZ);
	}
}
