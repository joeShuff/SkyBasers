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
import java.util.HashMap;
import java.util.List;

import me.JoeShuff.SkyBasers.BungeeComm;
import me.JoeShuff.SkyBasers.SkyBasers;
import me.JoeShuff.SkyBasers.VisualEffects;
import me.JoeShuff.SkyBasers.SkyBasers.GameState;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Instrument;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

public class HubItem extends BukkitRunnable implements Listener
{
	private SkyBasers plugin;
	
	public HubItem(SkyBasers plugin)
	{
		Bukkit.getServer().getPluginManager().registerEvents(this, plugin);
		this.plugin = plugin;
		
		this.runTaskTimer(plugin, 2, 2);
	}
	
	public static void getItem(Player player, Material mat)
	{
		player.getInventory().setItem(8, getHubItem(mat));
	}
	
	private static ItemStack getHubItem(Material mat)
	{
		ItemStack item = new ItemStack(mat, 1);
		
		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName(ChatColor.RED + "Return to Hub");
		meta.setLore(Arrays.asList(ChatColor.AQUA + "Right Click to return to the Hub!"));
		item.setItemMeta(meta);
		
		return item;
	}
	
	@EventHandler
	public void rightClick(PlayerInteractEvent event)
	{
		if (event.getAction() != Action.PHYSICAL || event.getAction() == Action.LEFT_CLICK_BLOCK)
		{
			if (event.getPlayer().getInventory().getItemInHand().hasItemMeta())
			{
				if (event.getPlayer().getInventory().getItemInHand().getItemMeta().hasDisplayName())
				{
					if (event.getPlayer().getInventory().getItemInHand().getItemMeta().getDisplayName().equalsIgnoreCase(ChatColor.RED + "Return to Hub"))
					{
						if (countdown.containsKey(event.getPlayer().getName()))
						{
							countdown.remove(event.getPlayer().getName());
							event.getPlayer().sendMessage(ChatColor.RED + "Teleportation cancelled!");
							getItem(event.getPlayer(), Material.SUGAR);
						}
						else
						{
							event.getPlayer().sendMessage(ChatColor.RED + "Teleporting in 3 seconds... Click again to cancel!");
							VisualEffects.sendActionBar(event.getPlayer(), ChatColor.WHITE + "Teleporting in 3...");
							event.getPlayer().playSound(event.getPlayer().getLocation(), Sound.CAT_MEOW, 1f, 1f);
							countdown.put(event.getPlayer().getName(), -1);
						}
					}
				}
			}
		}
	}

	public static HashMap<String, Integer> countdown = new HashMap<String, Integer>();
	
	private int counter = 0;
	
	@Override
	public void run() {
		
		if (plugin.getState() == GameState.LOADING)
		{	
			countdown = new HashMap<String, Integer>();
			return;
		}
		
		List<String> keys = new ArrayList<String>();
		
		keys.addAll(countdown.keySet());
		
		for (String key : keys)
		{
			if (countdown.containsKey(key))
			{
				int amount = countdown.get(key);
				
				amount ++;
				
				countdown.put(key, amount);
				
				if (amount % 10 != 0)
				{
					continue;
				}
				
				Player player = null;
				if (Bukkit.getPlayer(key) != null)
				{
					player = Bukkit.getPlayer(key);
				}
				else
				{
					continue;
				}
				
				if (amount / 10 == 1)
				{
					getItem(player, Material.GLOWSTONE_DUST);
					VisualEffects.sendActionBar(player, ChatColor.GOLD + "Teleporting in 2...");
					player.playSound(player.getLocation(), Sound.CAT_MEOW, 1f, 0.5f);
				}
				else if (amount / 10 == 2)
				{
					getItem(player, Material.REDSTONE);
					VisualEffects.sendActionBar(player, ChatColor.RED + "Teleporting in 1...");
					player.playSound(player.getLocation(), Sound.CAT_MEOW, 1f, 0.1f);
				}
				
				if (amount / 10 == 3)
				{
					getItem(player, Material.SUGAR);
					countdown.remove(player.getName());
					BungeeComm.sendToServer(Bukkit.getPlayer(key), plugin.getConfig().getString("hub"));
					BungeeComm.sendMessage(player, key, ChatColor.GREEN + "You have been teleported!");
				}
			}
		}
	}
	
}
