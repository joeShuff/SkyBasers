/*
 * Bukkit Plugin - SkyBasers
 * For use on The Justice Network
 * 
 * @author - Joseph Shufflebotham
 * 
 * © Copyright 2016 
 */
package me.JoeShuff.SkyBasers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;

public class Kits {

	private static HashMap<String, Kit> kits = new HashMap<String, Kit>();
	private static List<String> kitNames = new ArrayList<String>();
	private static SkyBasers plugin;
	
	public static class Kit
	{
		ItemStack displayItem;
		List<ItemStack> items = new ArrayList<ItemStack>();
		private String name = "";
		List<String> contents = new ArrayList<String>();
		boolean defaultUnlocked = false;
		String unlockLevel = "";
		Integer cost = 0;
		
		public Kit(List<ItemStack> items, String name, ItemStack displayItem, List<String> contents, boolean unlocked, String level, String cost)
		{
			this.displayItem = displayItem;
			this.items = items;
			this.name = name;
			this.contents = contents;
			this.defaultUnlocked = unlocked;
			this.unlockLevel = level;
			
			if (cost.equalsIgnoreCase("N/A"))
			{
				this.cost = 0;
			}
			else
			{
				try
				{
					this.cost = Integer.valueOf(cost);
				}
				catch (Exception e)
				{
					System.out.println("Error with cost of kit : " + name);
					this.cost = 0;
				}
			}
		}
		
		public String getName()
		{
			return name;
		}
		
		public List<ItemStack> getItems()
		{
			return items;
		}
		
		public ItemStack getDisplayItem()
		{
			return displayItem;
		}
		
		public List<String> getContents()
		{
			return contents;
		}
		
		public boolean isDefaultUnlocked()
		{
			return defaultUnlocked;
		}
		
		public String getUnlockLevel()
		{
			return unlockLevel;
		}
		
		public int getCost()
		{
			return cost;
		}
	}
	
	public static void init(SkyBasers plugin)
	{
		Kits.plugin = plugin;
		
		FileConfiguration file = plugin.getKitsConfig();
		
		List<String> kitnames = file.getStringList("kits");
		
		for (String kit : kitnames)
		{
			List<String> contents = file.getStringList(kit + ".DisplayList");
			
			List<String> kitData = file.getStringList(kit + ".Items");
			
			List<ItemStack> items = new ArrayList<ItemStack>();
			
			ItemStack displayItem = null;
			
			String display = file.getString(kit + ".DisplayItem");
			
			List<String> displaystuff = new ArrayList<String>();
			
			displaystuff = Arrays.asList(display.split(","));
			Material displayMat = null;
			
			try {displayMat = Material.getMaterial(displaystuff.get(0).toUpperCase());}
			catch (Exception e) {System.err.println("Error finding item " + display); continue;}
			
			if (displayMat== null)
			{
				System.err.println("Error finding item " + display);
				continue;
			}
			
			int damount = 0;
			
			try{damount = Integer.valueOf(displaystuff.get(2));}
			catch (Exception e) {System.err.println("Error finding item " + display); continue;}
			
			ItemStack titem = new ItemStack(displayMat, damount);
			
			try{titem.setDurability(Short.valueOf(displaystuff.get(1)));}
			catch (Exception e) {System.err.println("Error finding item " + display); continue;}
			
			displayItem = titem;
			
			for (String kitItem : kitData)
			{
				List<String> data = Arrays.asList(kitItem.split(","));
				ItemStack item;
				
				System.out.println("Attempting item " + data.get(0));
				
				Material mat = null;
				
				try {mat = Material.getMaterial(data.get(0).toUpperCase());}
				catch (Exception e) {System.err.println("Error finding item " + kitItem); continue;}
				
				if (mat == null)
				{
					System.err.println("Error finding item " + kitItem);
					continue;
				}
				
				int amount = 0;
				
				try{amount = Integer.valueOf(data.get(2));}
				catch (Exception e) {System.err.println("Error finding item " + kitItem); continue;}
				
				item = new ItemStack(mat, amount);
				
				try{item.setDurability(Short.valueOf(data.get(1)));}
				catch (Exception e) {System.err.println("Error finding item " + kitItem); continue;}
				
				items.add(item);
			}
			
			String unlockedDefault = file.getString(kit + ".DefaultUnlocked");
			boolean isUnlocked = false;
			
			try {isUnlocked = Boolean.valueOf(unlockedDefault);}
			catch (Exception e) {System.out.println("Error with default unlocked kit " + kit);}
			
			String level = file.getString(kit + ".Level");
			String cost = file.getString(kit + ".Cost");
			
			Kit Kkit = new Kit(items,kit, displayItem, contents, isUnlocked, level, cost);
			kits.put(kit, Kkit);
			kitNames.add(kit);
			System.out.println("Successfully added kit " + kit);
		}	
		
		System.out.println("Enabled ALL kits");
		
	}
	
	public static List<ItemStack> getKit(String name)
	{
		if (!kits.containsKey(name))
		{
			return null;
		}
		
		return kits.get(name).getItems();
	}
	
	public static HashMap<String, Kit> getKits()
	{
		return kits;
	}
	
	public static List<String> getKitNames()
	{
		return kitNames;
	}
	
}
