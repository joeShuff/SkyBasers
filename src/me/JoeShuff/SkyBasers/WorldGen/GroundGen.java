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

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;

import me.JoeShuff.SkyBasers.SkyBasers;

public class GroundGen {
	
	SkyBasers plugin;
	
	public GroundGen(SkyBasers plugin)
	{
		this.plugin = plugin;
		getChances();
		getWorldData();
	}
	
	int oreChance;
	
	int CoalChance;
	int IronChance;
	int GoldChance;
	int RedstoneChance;
	int LapisChance;
	int DiamondChance;
	int EmeraldChance;
	
	double chestChance;
	double lootChance;
	
	int xCentre;
	int yCentre;
	int zCentre;
	
	int height = 1;
	
	int coal = 0;
	int iron = 0;
	int gold = 0;
	int redstone = 0;
	int lapis = 0;
	int diamond = 0;
	int emerald = 0;
	
	public int generateGround()
	{
		Random rnd = new Random();
		
		int y = yCentre + height;
		
		for (int p = -35 ; p <= 35 ; p ++)
		{
			for (int q = -35 ; q <= 35 ; q ++)
			{
				Material generatedMat = null;
				
				int ore = rnd.nextInt(100) + 1;
				
				if (ore <= oreChance)
				{
					int whichOre = rnd.nextInt(100) + 1;
					
					if (whichOre <= CoalChance)
					{
						generatedMat = Material.COAL_ORE;
						coal ++;
					}
					else if (whichOre <= IronChance)
					{
						generatedMat = Material.IRON_ORE;
						iron ++;
					}
					else if (whichOre <= GoldChance)
					{
						generatedMat = Material.GOLD_ORE;
						gold ++;
					}
					else if (whichOre <= RedstoneChance)
					{
						generatedMat = Material.REDSTONE_ORE;
						redstone ++;
					}
					else if (whichOre <= LapisChance)
					{
						generatedMat = Material.LAPIS_ORE;
						lapis ++;
					}
					else if (whichOre <= DiamondChance)
					{
						if (height <= 10)
						{
							generatedMat = Material.DIAMOND_ORE;
							diamond ++;
						}
						else
						{
							generatedMat = Material.STONE;
						}
					}
					else if (whichOre <= EmeraldChance)
					{
						generatedMat = Material.EMERALD_ORE;
						emerald ++;
					}
					else
					{
						generatedMat = Material.STONE;
					}
				}
				else
				{
					generatedMat = Material.STONE;
				}
				
				
				Bukkit.getWorld(SkyBasers.worldName).getBlockAt(xCentre + p, y, zCentre + q).setType(generatedMat);
			}
		}
		
		height ++;
		
		
//		if (height == 21)
//		{
//			Bukkit.broadcastMessage("Coal : " + coal);
//			Bukkit.broadcastMessage("Iron : " + iron);
//			Bukkit.broadcastMessage("Gold : " + gold);
//			Bukkit.broadcastMessage("Redstone : " + redstone);
//			Bukkit.broadcastMessage("Lapis : " + lapis);
//			Bukkit.broadcastMessage("Diamond : " + diamond);
//		}
		return height;
	}
	
	private void getChances()
	{
		FileConfiguration rates = plugin.getspawnRatesData();
		
		oreChance = rates.getInt("ore");
		
		CoalChance = rates.getInt("coal");
		IronChance = rates.getInt("iron") + CoalChance;
		GoldChance = rates.getInt("gold") + IronChance;
		RedstoneChance = rates.getInt("redstone") + GoldChance;
		LapisChance = rates.getInt("lapis") + RedstoneChance;
		DiamondChance = rates.getInt("diamond") + LapisChance;
		EmeraldChance = rates.getInt("emerald") + DiamondChance;
		
		if (EmeraldChance != 100)
		{
			Bukkit.getServer().getLogger().info("Ore generator is not 100% filled! FIX IT!");
			Bukkit.getServer().getLogger().info("It is : " + EmeraldChance);
		}
		
		chestChance = rates.getDouble("chest");
		lootChance = rates.getDouble("loot") + chestChance;
	}
	
	private void getWorldData()
	{
		FileConfiguration config = plugin.getConfig();
		
		xCentre = config.getInt("mapCentreX");
		yCentre = config.getInt("mapCentreY");
		zCentre = config.getInt("mapCentreZ");
	}
}
