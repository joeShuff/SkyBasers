/*
 * Bukkit Plugin - SkyBasers
 * For use on The Justice Network
 * 
 * @author - Joseph Shufflebotham
 * 
 * © Copyright 2016 
 */
package me.JoeShuff.SkyBasers.Items;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import me.JoeShuff.SkyBasers.SkyBasers;
import me.JoeShuff.SkyBasers.VisualEffects;
import me.JoeShuff.SkyBasers.Scoreboards.TeamManager.Team;
import me.JoeShuff.SkyBasers.SkyBasers.GameState;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.SkullType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

public class SpectatorCompass implements Listener {
	
	SkyBasers plugin;
	
	public SpectatorCompass(SkyBasers plugin)
	{
		this.plugin = plugin;
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}

	@EventHandler
	public void playerInteract(PlayerInteractEvent event)
	{
		if (event.getPlayer().getItemInHand().getItemMeta() == null)
		{
			return;
		}
		
		if (!event.getPlayer().getItemInHand().getItemMeta().hasLore())
		{
			return;
		}
		
		if (!(event.getPlayer().getItemInHand().getItemMeta().getLore().get(0).contains("Teleport")))
		{
			return;
		}
		
		if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)
		{
			if (event.getPlayer().getItemInHand().getType() == Material.COMPASS)
			{
				generateInv(event.getPlayer());
			}
		}
		
		if (event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK)
		{
			Player chosenPlayer = null;
			
			if (plugin.isTeams())
			{
				Team team = plugin.getTeamManager().getPlayerTeam(event.getPlayer());
				
				if (team != null)
				{
					for (String teammate : team.getPlayers())
					{
						if (!teammate.equals(event.getPlayer().getName()))
						{
							if (plugin.getPlayerScoreboards(false).hasAlivePlayer(teammate))
							{
								chosenPlayer = Bukkit.getPlayerExact(teammate);
							}
						}
					}
				}
			}
			
			if (chosenPlayer == null)
			{
				List<String> players = plugin.getPlayerScoreboards(false).getAlivePlayers();
				
				int player = (new Random().nextInt(players.size()));
				
				chosenPlayer = Bukkit.getPlayerExact(players.get(player));
			}
			
			if (chosenPlayer == null)
			{
				VisualEffects.sendActionBar(event.getPlayer(), "" + ChatColor.RED + ChatColor.BOLD + "Cannot find an alive player");
			}
			else
			{
				event.getPlayer().teleport(chosenPlayer);
				VisualEffects.sendActionBar(event.getPlayer(), ChatColor.AQUA + "You have been teleported to " + chosenPlayer.getName());
			}
		}
	}
	
	public void ShowMenu(Player p,Inventory inv)
	{
		p.openInventory(inv);
	}
	
	public void generateInv(Player p)
	{			
		Inventory inv = Bukkit.getServer().createInventory(null, 9, "Player Selector");
		
		if (plugin.getState() != GameState.LIVE)
		{
			return;
		}
		
		if (!plugin.isTeams())
		{
			List<String> players = plugin.getPlayerScoreboards(false).getAlivePlayers();
			
			if (players.size() <= 9)
			{
				inv = Bukkit.getServer().createInventory(null, 9, "Player Selector");
			}
			else if (players.size() <= 18)
			{
				inv = Bukkit.getServer().createInventory(null, 18, "Player Selector");
			}
			else if (players.size() <= 27)
			{
				inv = Bukkit.getServer().createInventory(null, 27, "Player Selector");
			}
			else if (players.size() <= 36)
			{
				inv = Bukkit.getServer().createInventory(null, 36, "Player Selector");
			}
			else if (players.size() <= 45)
			{
				inv = Bukkit.getServer().createInventory(null, 45, "Player Selector");
			}
			else if (players.size() <= 54)
			{
				inv = Bukkit.getServer().createInventory(null, 54, "Player Selector");
			}
			else
			{
				inv = Bukkit.getServer().createInventory(null, 54, "Player Selector");
			}
		}
		
		
		
//		if (plugin.isTeams())
//		{
//			Team team = plugin.getTeamManager().getPlayerTeam(p);
//			
//			if (team != null)
//			{
//				String teammate = plugin.getTeamManager().getTeamMate(p);
//				
//				String prefix = team.getPrefix();
//				
//				if (teammate != null)
//				{
//					ItemStack skull = new ItemStack(Material.SKULL_ITEM, 1, (short) SkullType.PLAYER.ordinal());
//					
//					SkullMeta skullMeta = (SkullMeta) skull.getItemMeta();
//					
//					skullMeta.setDisplayName(prefix + teammate);
//					skullMeta.setLore(Arrays.asList(ChatColor.RED + "Left Click to TP to " + teammate,ChatColor.RED + "Right click to see " + teammate + "'s inventory",ChatColor.GREEN + "Teammate : " + prefix + teammate));
//					skullMeta.setOwner(teammate);
//					skull.setItemMeta(skullMeta);
//					
//					try {
//						inv.addItem(skull);
//					} catch (Exception e) {
//						p.sendMessage(ChatColor.RED + "Too many players to show in menu, showing first 54!");
//					}
//				}
//			}
//		}
//		else
//		{
			for (String player : plugin.getPlayerScoreboards(false).getAlivePlayers())
			{
				if (Bukkit.getPlayer(player) == null)
				{
					continue;
				}
				
				ItemStack skull = new ItemStack(Material.SKULL_ITEM, 1, (short) SkullType.PLAYER.ordinal());
				
				SkullMeta skullMeta = (SkullMeta) skull.getItemMeta();
				
				skullMeta.setDisplayName(ChatColor.YELLOW + player);
				skullMeta.setLore(Arrays.asList(ChatColor.RED + "Left Click to TP to " + player,ChatColor.RED + "Right click to see " + player + "'s inventory"));
				skullMeta.setOwner(player);
				skull.setItemMeta(skullMeta);
				
				try {
					inv.addItem(skull);
				} catch (Exception e) {
					p.sendMessage(ChatColor.RED + "Too many players to show in menu, showing first 54!");
					break;
				}
				
			}
//		}
		
		
		
		ShowMenu(p, inv);
	}
	
	public static void getCompass(Player player)
	{
		ItemStack compass = new ItemStack(Material.COMPASS);
		
		ItemMeta cMeta = compass.getItemMeta();
		cMeta.setDisplayName(ChatColor.BLUE + "Player Teleporter");
		cMeta.setLore(Arrays.asList(ChatColor.AQUA + "Used to Teleport to Players", ChatColor.GREEN + "Right Click"));
		compass.setItemMeta(cMeta);
		
		player.getInventory().setItem(0, compass);
	}
	
	@EventHandler
	public void clickInventory(InventoryClickEvent event)
	{
		try {
			if (event.getCurrentItem().getItemMeta() == null) return;
		} catch (NullPointerException e) 
		{
			return;
		}
		
		if (!(event.getCurrentItem().getType() == Material.SKULL_ITEM))
		{
			if (event.getCurrentItem().getType() == Material.COMPASS)
			{
				if (event.getCurrentItem().getItemMeta().hasLore())
				{
					if (event.getCurrentItem().getItemMeta().getLore().get(0).contains("Teleport"))
					{
						generateInv((Player) event.getWhoClicked());
					}
				}
				
				return;
			}
			else
			{
				return;
			}
		}
		
		if (!(event.getInventory().getName().contains("Player Selector")))
		{
			return;
		}
		
		event.setCancelled(true);
		
		if (event.getClick() == ClickType.LEFT)
		{
			SkullMeta meta = (SkullMeta) event.getCurrentItem().getItemMeta();
			String playername = meta.getOwner();
			
			Player player = Bukkit.getPlayer(playername);
			
			event.getWhoClicked().teleport(player);
			
			event.getWhoClicked().sendMessage(ChatColor.GREEN + "You were teleported to " + playername);
		}
		else if (event.getClick() == ClickType.RIGHT)
		{
			SkullMeta meta = (SkullMeta) event.getCurrentItem().getItemMeta();
			String playername = meta.getOwner();
			
			Player clickedPlayer = Bukkit.getPlayer(playername);
			
			event.getWhoClicked().openInventory(clickedPlayer.getInventory());
		}
		
		return;
	}
	
}
