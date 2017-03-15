/*
 * Bukkit Plugin - SkyBasers
 * For use on The Justice Network
 * 
 * @author - Joseph Shufflebotham
 * 
 * © Copyright 2016 
 */
package me.JoeShuff.SkyBasers.Items;

import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import me.JoeShuff.SkyBasers.Kits;
import me.JoeShuff.SkyBasers.Kits.Kit;
import me.JoeShuff.SkyBasers.MySQL.SQLManager;
import me.JoeShuff.SkyBasers.SkyBasers;
import me.JoeShuff.SkyBasers.SkyBasers.GameState;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class KitItem implements Listener
{
	private final String invName = "Kit Selector";
	
	private SkyBasers plugin;
	
	public KitItem(SkyBasers plugin)
	{
		Bukkit.getServer().getPluginManager().registerEvents(this, plugin);
		this.plugin = plugin;
	}
	
	public static void getItem(Player player)
	{
		player.getInventory().setItem(0, getKitItem());
	}
	
	private static ItemStack getKitItem()
	{
		ItemStack item = new ItemStack(Material.BLAZE_POWDER, 1);
		
		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName(ChatColor.GREEN + "Kit Selector");
		meta.setLore(Arrays.asList(ChatColor.AQUA + "Right Click to pick a kit!"));
		item.setItemMeta(meta);
		
		return item;
	}
	
	private void showInventory(Player player)
	{
		Inventory inv = getClasses(player);
		
		player.openInventory(inv);
	}
	
	@EventHandler
	public void rightClick(PlayerInteractEvent event)
	{
		if (plugin.getState() != GameState.PREGAME)
		{
			return;
		}
		
		if (event.getAction() != Action.PHYSICAL || event.getAction() == Action.LEFT_CLICK_BLOCK)
		{
			if (event.getPlayer().getInventory().getItemInHand().hasItemMeta())
			{
				if (event.getPlayer().getInventory().getItemInHand().getItemMeta().hasDisplayName())
				{
					if (event.getPlayer().getInventory().getItemInHand().getItemMeta().getDisplayName().equalsIgnoreCase(ChatColor.GREEN + "Kit Selector"))
					{
						showInventory(event.getPlayer());
					}
				}
			}
		}
	}
	
	@EventHandler
	public void inventoryClick(InventoryClickEvent event)
	{
		if (!event.getInventory().getName().equals(invName))
		{
			return;
		}
		
		ItemStack clicked;
		try {clicked = event.getCurrentItem();}
		catch (Exception ex) {return;}
		
		if (clicked == null) {return;}
		
		if (!clicked.hasItemMeta()) {return;}
		
		if (!clicked.getItemMeta().hasDisplayName()) {return;}

		String kitName = clicked.getItemMeta().getDisplayName().substring(2, clicked.getItemMeta().getDisplayName().length());
		
		if (clicked.getType() == Material.STAINED_GLASS_PANE)
		{
			event.setCancelled(true);
			return;
		}
		
		SQLManager.setClass((OfflinePlayer) event.getWhoClicked(), kitName);
		
		event.getWhoClicked().sendMessage(ChatColor.GREEN + "Successfully chosen kit " + kitName);
		
		event.setCancelled(true);
		
		event.getWhoClicked().closeInventory();
	}
	
	private Inventory getClasses(Player player)
	{
		Inventory inv = Bukkit.createInventory(null, 9, invName);
		
		int kitAmount = Kits.getKits().size();
		
		if (kitAmount <= 9)
		{
			inv = Bukkit.createInventory(null, 9, invName);
		}
		else if (kitAmount <= 18)
		{
			inv = Bukkit.createInventory(null, 18, invName);
		}
		else if (kitAmount <= 27)
		{
			inv = Bukkit.createInventory(null, 27, invName);
		}
		else if (kitAmount <= 36)
		{
			inv = Bukkit.createInventory(null, 36, invName);
		}
		else if (kitAmount <= 45)
		{
			inv = Bukkit.createInventory(null, 45, invName);
		}
		else if (kitAmount <= 54)
		{
			inv = Bukkit.createInventory(null, 54, invName);
		}
		else 
		{
			inv = Bukkit.createInventory(null, 54, invName);
		}
		
		if (!plugin.kitsEnabled())
		{
			for (int i = 0 ; i < kitAmount ; i ++)
			{
				inv.setItem(i, comingSoon());
			}	
		}
		else
		{
			HashMap<String, Kit> kits = Kits.getKits();
			List<String> kitNames = Kits.getKitNames();
			
			for (String kitname : kitNames)
			{
				Kit kit = kits.get(kitname);
				
				ItemStack kitStack = new ItemStack(kit.getDisplayItem());
				ItemMeta meta = kitStack.getItemMeta();
				
				if (!SQLManager.playerHasKit(kit.getName(), player) && !kit.isDefaultUnlocked())
				{
					meta.setDisplayName("" + ChatColor.RED + ChatColor.MAGIC + kit.getName());
					kitStack.setType(Material.STAINED_GLASS_PANE);
					kitStack.setDurability((short) 14);
				}
				else
				{
					meta.setDisplayName(ChatColor.BLUE + kit.getName());

					List<String> items = new ArrayList<String>();
					for (String item : kit.getContents())
					{
						items.add(ChatColor.AQUA + item);
					}
					
					meta.setLore(items);
				}
				
				kitStack.setItemMeta(meta);
				
				inv.addItem(kitStack);
			}
		}
		
		return inv;
	}
	
	private ItemStack comingSoon()
	{
		ItemStack comingSoon = new ItemStack(Material.STAINED_GLASS_PANE, 1);
		comingSoon.setDurability((short) 14);
		
		ItemMeta meta = comingSoon.getItemMeta();
		meta.setDisplayName(ChatColor.RED + "Coming Soon!");
		
		comingSoon.setItemMeta(meta);
		
		return comingSoon;
	}
}
