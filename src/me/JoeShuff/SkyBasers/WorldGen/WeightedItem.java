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

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;

public class WeightedItem {
	
	private ItemStack item = null;
	private Integer weight = null;
	private Integer minAmount = null;
	private Integer maxAmount = null;
	private String name = null;
	
	public WeightedItem(Material itemID, int Weight, int damage, int minAmount, int maxAmount)
	{
		this(itemID, Weight, damage, minAmount, maxAmount, null);
	}
	
	public WeightedItem(Material itemID, int Weight, int damage, int minAmount, int maxAmount, String name)
	{
		if (itemID == null)
		{
			System.out.println("Error generating a weighted item with other data (" + Weight + "," + damage + "," + minAmount + "," + maxAmount + "), replacing with stone");
			this.item = new ItemStack(Material.STONE, 1, (short) damage);
		}
		else
		{
			this.item = new ItemStack(itemID, 1, (short) damage);
		}

		this.weight = Weight;
		this.minAmount = minAmount;
		this.maxAmount = maxAmount;
		this.name = name;
	}
	
	public ItemStack getItemStack()
	{
		this.item.setAmount(getAmount());
		
		if (this.item.getType() == Material.ENCHANTED_BOOK)
		{
			Random rnd = new Random();
			
			EnchantmentStorageMeta meta = (EnchantmentStorageMeta) this.item.getItemMeta();
			
			for (Enchantment ench : meta.getStoredEnchants().keySet())
			{
				meta.removeStoredEnchant(ench);
			}
			
			int amountofEnchs = rnd.nextInt(3) + 1;
			
			for (int i = 0 ; i < amountofEnchs ; i ++)
			{
				meta = WeightedItem.addEnchant(meta, rnd);
			}
			
			this.item.setItemMeta(meta);
		}	
		
		if (this.name != null)
		{
			ItemMeta meta = this.item.getItemMeta();
			meta.setDisplayName(ChatColor.GOLD + this.name);
			this.item.setItemMeta(meta);
			
			if (this.name.contains("Lasso"))
			{
				this.item.addUnsafeEnchantment(Enchantment.KNOCKBACK, 10);
			}
		}
		
		return this.item;
	}
	
	private int getAmount()
	{
		Random rnd = new Random();
		
		if (maxAmount == minAmount)
		{
			return minAmount;
		}
		
		return rnd.nextInt(maxAmount - minAmount) + minAmount;
	}
	
	public int getWeight()
	{
		return this.weight;
	}
	
	public String getName()
	{
		if (this.name == null)
		{
			return null;
		}
		
		return this.name;
	}
	
	public String printItem()
	{
		return "Weighted Item : " + String.valueOf(this.item.getType()) + " (" + weight + "," + minAmount + "," + maxAmount + ")";
	}
	
	public static EnchantmentStorageMeta addEnchant(EnchantmentStorageMeta meta, Random rnd)
	{
		int selector = rnd.nextInt(4) + 1;
		
		if (selector == 1)
		{
			int id = rnd.nextInt(8);
			
			if (!meta.hasConflictingEnchant(Enchantment.getById(id)))
			{
				meta.addStoredEnchant(Enchantment.getById(id), rnd.nextInt(3) + 1,true);
			}
		}
		else if (selector == 2)
		{
			int id = rnd.nextInt(6) + 16;
			
			if (!meta.hasConflictingEnchant(Enchantment.getById(id)))
			{
				meta.addStoredEnchant(Enchantment.getById(id), rnd.nextInt(2) + 1,true);
			}
		}
		else if (selector == 3)
		{
			int id = rnd.nextInt(4) + 32;
			if (!meta.hasConflictingEnchant(Enchantment.getById(id)))
			{
				meta.addStoredEnchant(Enchantment.getById(id), rnd.nextInt(2) + 1,true);
			}
		}
		else
		{
			int id = rnd.nextInt(4) + 48;

			if (!meta.hasConflictingEnchant(Enchantment.getById(id)))
			{
				meta.addStoredEnchant(Enchantment.getById(id), rnd.nextInt(2) + 1,true);
			}
		}
		
		return meta;
	}
}
