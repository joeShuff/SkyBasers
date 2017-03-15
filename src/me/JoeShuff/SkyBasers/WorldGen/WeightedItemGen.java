/*
 * Bukkit Plugin - SkyBasers
 * For use on The Justice Network
 * 
 * @author - Joseph Shufflebotham
 * 
 * © Copyright 2016 
 */
package me.JoeShuff.SkyBasers.WorldGen;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import me.JoeShuff.SkyBasers.SkyBasers;
import me.JoeShuff.SkyBasers.SkyBasers.GameType;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;

public class WeightedItemGen {

	private List<String> configItems = new ArrayList<String>();
	
	private HashMap<Integer, WeightedItem> weightedItems = new HashMap<Integer,WeightedItem>();
	
	private SkyBasers plugin;
	
	public WeightedItemGen(SkyBasers plugin)
	{
		this.plugin = plugin;
		getAllItems();
	}
	
	private boolean gotLeash = false;
	
	public ItemStack getItem()
	{
		ItemStack result = null;
		
		int maxWeight = getMaxWeight();
		
		Random rnd = new Random();
		
		int selector = rnd.nextInt(maxWeight) + 1;
		
		Boolean found = false;
		
		while (found == false)
		{	
			if (weightedItems.containsKey(selector))
			{
				found = true;	
			}
			else
			{
				selector ++;
			}
		}
		
		WeightedItem generatedItem = weightedItems.get(selector);
		
		if (generatedItem.getName() != null)
		{
			if (generatedItem.getName().contains("Lasso"))
			{
				if (gotLeash == true)
				{
					return getItem();
				}
				else
				{
					gotLeash = true;
				}
			}
		}
		
		result = generatedItem.getItemStack();
		
		return result;
	}
	
	private int getMaxWeight()
	{		
		List<Integer> keyList = new ArrayList<Integer>();
		keyList.addAll(weightedItems.keySet());
		
		int maxWeight = 0;
		
		if (keyList.size() == 0)
		{
			maxWeight = 0;
		}
		else
		{
			Collections.sort(keyList);
			maxWeight = keyList.get(keyList.size() - 1);
		}
		
		return maxWeight;
	}
	
	private void getAllItems()
	{
		if (plugin.getType() == GameType.NORMAL)
		{
			configItems = plugin.getchestLootData().getStringList("normal-loot");
		}
		else
		{
			configItems = plugin.getchestLootData().getStringList("insane-loot");
		}
		
		for (String str : configItems)
		{
			try
			{
				//ItemName,Weight,Damage,minAmount,maxAmount
				List<String> results = new ArrayList<String>();
				String current = "";
				
				for (int i = 0 ; i <= str.length() ; i ++)
				{
					if (i == str.length())
					{
						results.add(current);
						current = "";
						break;
					}
					else if (String.valueOf(str.charAt(i)).equals(","))
					{
						results.add(current);
						current = "";
					}
					else
					{
						current = current + String.valueOf(str.charAt(i));
					}
				}
				
				Material mat = Material.getMaterial(results.get(0).toUpperCase());
				
				if (mat == null)
				{	
					System.out.println("Error generating weighted item : " + results.get(0));
				}
				
				WeightedItem item = null;
				
				if (results.size() == 6)
				{
					item = new WeightedItem(Material.getMaterial(results.get(0).toUpperCase()), Integer.valueOf(results.get(1)), Integer.valueOf(results.get(2)), Integer.valueOf(results.get(3)), Integer.valueOf(results.get(4)), results.get(5));
				}
				else if (results.size() == 5)
				{
					item = new WeightedItem(Material.getMaterial(results.get(0).toUpperCase()), Integer.valueOf(results.get(1)), Integer.valueOf(results.get(2)), Integer.valueOf(results.get(3)), Integer.valueOf(results.get(4)));
				}
				
				int maxWeight = getMaxWeight();
				 			
				Integer newWeight = maxWeight + Integer.valueOf(results.get(1));
				
				weightedItems.put(newWeight, item);
				
			} catch (Exception e)
			{
				e.printStackTrace();
				System.out.println("Unable to convert itemWeight string '" + str + "' to a weighted item");
			}
			
		}
	}
}
