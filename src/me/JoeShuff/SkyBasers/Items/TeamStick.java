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

import me.JoeShuff.SkyBasers.SkyBasers;
import me.JoeShuff.SkyBasers.SkyBasers.GameState;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

public class TeamStick extends BukkitRunnable implements Listener {
	
	private SkyBasers plugin;
	
	public TeamStick(SkyBasers plugin)
	{
		this.plugin = plugin;
		
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
		
		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName(name);
		meta.setLore(Arrays.asList(ChatColor.GREEN + "Hit someone to send/accept a team request!",ChatColor.BLUE + "Right click to disband a team!"));	
		item.setItemMeta(meta);
		
		runTaskTimer(plugin, 2, 2);
	}
	
	private static final ItemStack item = new ItemStack(Material.BLAZE_ROD,1);
	private static final String name = ChatColor.GOLD + "Team Stick";
	
	public static ItemStack getItem()
	{
		return item;
	}
	
	public class wait {
		private String player;
		private int counter;
		
		public wait(String player)
		{
			this.player = player;
			counter = 20;
		}
		
		public boolean reduce()
		{
			counter --;
			
			if (counter > 0)
			{
				return true;
			}
			
			return false;
		}
	}
	
	private HashMap<String, wait> cancelling = new HashMap<String, wait>();
	
	@Override
	public void run() 
	{
		List<String> keys = new ArrayList<String>();
		keys.addAll(cancelling.keySet());
		
		for (String key : keys)
		{
			if (!cancelling.get(key).reduce())
			{
				cancelling.remove(key);
			}
		}	
	}
	
	@EventHandler
	public void playerHit(EntityDamageByEntityEvent event)
	{		
		if (plugin.getState() == GameState.PREGAME)
		{
			if (event.getDamager() instanceof Player)
			{
				Player damager = (Player) event.getDamager();
				
				if (event.getEntity() instanceof Player)
				{
					Player damaged = (Player) event.getEntity();
					
					if (damager.getInventory().getItemInHand().hasItemMeta())
					{
						if (damager.getInventory().getItemInHand().getItemMeta().hasDisplayName())
						{
							if (damager.getInventory().getItemInHand().getItemMeta().getDisplayName().contains("Team Stick")) {}
							else
							{
								return;
							}
						}
						else
						{
							return;
						}
					}
					else
					{
						return;
					}
					
					if (!plugin.isTeams())
					{
						event.getDamager().sendMessage(ChatColor.RED + "You cannot team in solo mode!");
						return;
					}
					
					if (plugin.getTeamManager().getPlayerTeam(damager) != null)
					{
						damager.sendMessage(ChatColor.RED + "You are already on a team, right click to disband");
						return;
					}
					if (plugin.getTeamManager().hasRequest(damager.getName(), damaged.getName()))
					{
						plugin.getTeamManager().acceptRequest(damager.getName(), damaged.getName());
					}
					else if (plugin.getTeamManager().hasRequest(damaged.getName(), damager.getName()))
					{
						plugin.getTeamManager().disbandTeam(damager.getName());
					}
					else
					{
						plugin.getTeamManager().requestTeam(damager.getName(), damaged.getName());
					}
				}
			}
		}
	}
	
	@EventHandler
	public void playerClick(PlayerInteractEvent event)
	{
		if (event.getPlayer().getItemInHand() == null) return;
		
		if (event.getPlayer().getItemInHand().getType() != Material.BLAZE_ROD) return;
		
		if (!event.getPlayer().getItemInHand().hasItemMeta()) return;
		
		if (!event.getPlayer().getItemInHand().getItemMeta().hasDisplayName()) return;
		
		if (event.getPlayer().getItemInHand().getItemMeta().getDisplayName().equals(name))
		{
			if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)
			{
				if (plugin.getTeamManager().getPlayerTeam(event.getPlayer()) == null)
				{
					event.getPlayer().sendMessage(ChatColor.RED + "You are not on a team!");
					return;
				}
				
				if (!cancelling.containsKey(event.getPlayer().getName()))
				{
					cancelling.put(event.getPlayer().getName(), new wait(event.getPlayer().getName()));
					event.getPlayer().sendMessage(ChatColor.RED + "Now sneak to confirm your disband request!");
				}
			}
		}
	}
	
	@EventHandler
	public void playerSneak(PlayerToggleSneakEvent event)
	{
		if (!cancelling.containsKey(event.getPlayer().getName())) return;
		
		if (!event.getPlayer().isSneaking())
		{
			plugin.getTeamManager().disbandTeam(event.getPlayer().getName());
			cancelling.remove(event.getPlayer().getName());
		}
	}
}
