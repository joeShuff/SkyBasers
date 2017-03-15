/*
 * Bukkit Plugin - SkyBasers
 * For use on The Justice Network
 * 
 * @author - Joseph Shufflebotham
 * 
 * © Copyright 2016 
 */
package me.JoeShuff.SkyBasers.Items;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import me.JoeShuff.SkyBasers.SkyBasers;
import me.JoeShuff.SkyBasers.MySQL.MySQL;
import me.JoeShuff.SkyBasers.SkyBasers.GameState;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.SkullType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

public class PlayerStatItem implements Listener {

	private final String invName = "Stat Viewer";
	
	private SkyBasers plugin;
	
	public PlayerStatItem(SkyBasers plugin)
	{
		Bukkit.getServer().getPluginManager().registerEvents(this, plugin);
		this.plugin = plugin;
	}
	
	public static void getItem(Player player)
	{
		player.getInventory().setItem(4, getStatItem(player));
	}
	
	private static ItemStack getStatItem(Player player)
	{
		ItemStack head = new ItemStack(Material.SKULL_ITEM,1, (short) SkullType.PLAYER.ordinal());
		SkullMeta meta = (SkullMeta) head.getItemMeta();
		
		meta.setOwner(player.getName());
		meta.setDisplayName(ChatColor.GOLD + player.getName());
		head.setItemMeta(meta);
		
		return head;
	}
	
	private void showInventory(Player player)
	{
		Inventory inv = getData(player);
		
		player.openInventory(inv);
	}
	
	private Inventory getData(Player player)
	{
		Inventory inv = Bukkit.createInventory(null, 45, invName);
		
		boolean foundPlayer = false;
		
		int NSWin = 0;
		int NTWin = 0;
		int JSWin = 0;
		int JTWin = 0;
		
		int NSKill = 0;
		int NTKill = 0;
		int JSKill = 0;
		int JTKill = 0;
		
		int totalKill = 0;
		int totalWin = 0;
		
		int coins = 0;
		
		int deaths = 0;
		
		String UUID = player.getUniqueId().toString();
		
		ItemStack blank = new ItemStack(Material.STAINED_GLASS_PANE,1);
		blank.setDurability((short) 0);
		ItemMeta blankMeta = blank.getItemMeta();
		blankMeta.setDisplayName("-");
		blank.setItemMeta(blankMeta);
		
		for (int i = 0 ; i <= 44; i ++)
		{
			inv.setItem(i, blank);
		}
		
		Statement s = null;
		ResultSet results = null;
		
		try
		{
			Connection conn = MySQL.getConnection();
			s = conn.createStatement();
			results = s.executeQuery("SELECT * FROM `SkyBasers` WHERE `UUID`='" + UUID + "'");
			
			if (results.next())
			{
				foundPlayer = true;
				
				NSKill = results.getInt("NSoloKills");
				NSWin = results.getInt("NSoloWins");
				NTKill = results.getInt("NTeamKills");
				NTWin = results.getInt("NTeamWins");
				
				JSKill = results.getInt("JSoloKills");
				JSWin = results.getInt("JSoloWins");
				JTKill = results.getInt("JTeamKills");
				JTWin = results.getInt("JTeamWins");
				
				deaths = results.getInt("Deaths");
				
				totalKill = NSKill + NTKill + JSKill + JTKill;
				totalWin = NSWin + NTWin + JSWin + JTWin;
			}
		}
		catch (Exception e)
		{
			
		}
		finally
		{
			try {results.close();} catch (Exception e){}
			try {s.close();} catch (Exception e){}
		}
		
		try
		{
			Connection conn = MySQL.getConnection();
			s = conn.createStatement();
			results = s.executeQuery("SELECT * FROM `Justice Coins` WHERE `UUID`='" + UUID + "'");
			
			if (results.next())
			{
				coins = results.getInt("balance");
			}
		}
		catch (Exception e)
		{
			
		}
		finally
		{
			try {results.close();} catch (Exception e){}
			try {s.close();} catch (Exception e){}
		}
		
		ItemStack SNWins = new ItemStack(Material.WOOL,1);
		SNWins.setDurability((short) 11);
		ItemMeta SNWinsMeta = SNWins.getItemMeta();
		SNWinsMeta.setDisplayName(ChatColor.GREEN + "NORMAL Solo Stats");
		List<String> lore = new ArrayList<String>();
		if (foundPlayer)
		{
			if (NSWin == 1)
			{
				lore.add(ChatColor.GOLD + "You have " + NSWin + " win");
			}
			else
			{
				lore.add(ChatColor.GOLD + "You have " + NSWin + " wins");
			}
			
			lore.add(ChatColor.YELLOW + "--------------------");
			
			if (NSKill == 1)
			{
				lore.add(ChatColor.GOLD + "You have " + NSKill + " kill");
			}
			else
			{
				lore.add(ChatColor.GOLD + "You have " + NSKill + " kills");
			}
		}
		else
		{
			lore.add(ChatColor.RED + "Cannot find player!");
		}
		SNWinsMeta.setLore(lore);
		SNWins.setItemMeta(SNWinsMeta);
		inv.setItem(11, SNWins);
		
		ItemStack TNWins = new ItemStack(Material.LAPIS_BLOCK,1);
		ItemMeta TNWinsMeta = TNWins.getItemMeta();
		TNWinsMeta.setDisplayName(ChatColor.GREEN + "NORMAL Team Stats");
		List<String> lore2 = new ArrayList<String>();
		if (foundPlayer)
		{
			if (NTWin == 1)
			{
				lore2.add(ChatColor.GOLD + "You have " + NTWin + " win");
			}
			else
			{
				lore2.add(ChatColor.GOLD + "You have " + NTWin + " wins");
			}
			
			lore2.add(ChatColor.YELLOW + "--------------------");
			
			if (NTKill == 1)
			{
				lore2.add(ChatColor.GOLD + "You have " + NTKill + " kill");
			}
			else
			{
				lore2.add(ChatColor.GOLD + "You have " + NTKill + " kills");
			}
		}
		else
		{
			lore2.add(ChatColor.RED + "Cannot find player!");
		}
		TNWinsMeta.setLore(lore2);
		TNWins.setItemMeta(TNWinsMeta);
		inv.setItem(29, TNWins);
		
		ItemStack SJWins = new ItemStack(Material.WOOL,1);
		SJWins.setDurability((short) 14);
		ItemMeta SJWinsMeta = SJWins.getItemMeta();
		SJWinsMeta.setDisplayName(ChatColor.GREEN + "JUDGEMENT Solo Stats");
		List<String> lore3 = new ArrayList<String>(); 
		if (foundPlayer)
		{
			if (JSWin == 1)
			{
				lore3.add(ChatColor.GOLD + "You have " + JSWin + " win");
			}
			else
			{
				lore3.add(ChatColor.GOLD + "You have " + JSWin + " wins");
			}		
			
			lore3.add(ChatColor.YELLOW + "--------------------");
			
			if (JSKill == 1)
			{
				lore3.add(ChatColor.GOLD + "You have " + JSKill + " kill");
			}
			else
			{
				lore3.add(ChatColor.GOLD + "You have " + JSKill + " kills");
			}
		}
		else
		{
			lore3.add(ChatColor.RED + "Cannot find player!");
		}
		SJWinsMeta.setLore(lore3);
		SJWins.setItemMeta(SJWinsMeta);
		inv.setItem(15, SJWins);
		
		ItemStack TJWins = new ItemStack(Material.REDSTONE_BLOCK,1);
		ItemMeta TJWinsMeta = TJWins.getItemMeta();
		TJWinsMeta.setDisplayName(ChatColor.GREEN + "JUDGEMENT Team Stats");
		List<String> lore4 = new ArrayList<String>();
		if (foundPlayer)
		{
			if (JTWin == 1)
			{
				lore4.add(ChatColor.GOLD + "You have " + JTWin + " win");
			}
			else
			{
				lore4.add(ChatColor.GOLD + "You have " + JTWin + " wins");
			}
			
			lore4.add(ChatColor.YELLOW + "--------------------");
			
			if (JTKill == 1)
			{
				lore4.add(ChatColor.GOLD + "You have " + JTKill + " kill");
			}
			else
			{
				lore4.add(ChatColor.GOLD + "You have " + JTKill + " kills");
			}
		}
		else
		{
			lore4.add(ChatColor.RED + "Cannot find player!");
		}
		TJWinsMeta.setLore(lore4);
		TJWins.setItemMeta(TJWinsMeta);
		inv.setItem(33, TJWins);
		
		ItemStack shop_diamond = new ItemStack(Material.DIAMOND, 1);
		ItemMeta shopMeta = shop_diamond.getItemMeta();
		
		shopMeta.setDisplayName(ChatColor.GOLD + "Overall Stats");
		shopMeta.setLore(Arrays.asList(ChatColor.GREEN + "Total Wins: " + totalWin,ChatColor.BLUE + "Total Kills: " + totalKill,ChatColor.RED + "Total Deaths: " + deaths, "",ChatColor.GOLD + "Justice Coins: " + coins));
		shop_diamond.setItemMeta(shopMeta);
		inv.setItem(22, shop_diamond);
		
		ItemStack skull = new ItemStack(Material.SKULL_ITEM,1, (short) SkullType.PLAYER.ordinal());
		SkullMeta meta = (SkullMeta) skull.getItemMeta();
		
		meta.setOwner(player.getName());
		meta.setDisplayName(ChatColor.GOLD + "Player: " + player.getName());
		skull.setItemMeta(meta);
		
		inv.setItem(4, skull);
		inv.setItem(40, skull);
		
		return inv;
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
			if (event.getPlayer().getInventory().getItemInHand().getType() != Material.SKULL_ITEM)
			{
				return;
			}
			
			if (event.getPlayer().getInventory().getItemInHand().hasItemMeta())
			{
				if (event.getPlayer().getInventory().getItemInHand().getItemMeta().hasDisplayName())
				{
					if (event.getPlayer().getInventory().getItemInHand().getItemMeta().getDisplayName().contains("" + ChatColor.GOLD))
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

		event.setCancelled(true);
	}
}
