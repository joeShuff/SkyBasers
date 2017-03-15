/*
 * Bukkit Plugin - SkyBasers
 * For use on The Justice Network
 * 
 * @author - Joseph Shufflebotham
 * 
 * © Copyright 2016 
 */
package me.JoeShuff.SkyBasers.Listeners;

import java.util.Arrays;
import java.util.List;

import me.JoeShuff.SkyBasers.BungeeComm;
import me.JoeShuff.SkyBasers.SkyBasers;
import me.JoeShuff.SkyBasers.Permissions.PermissionGroups;
import me.JoeShuff.SkyBasers.RankSystemBukkit;
import me.JoeShuff.SkyBasers.SkyBasers.GameStage;
import me.JoeShuff.SkyBasers.SkyBasers.GameState;
import me.JoeShuff.SkyBasers.VisualEffects;
import me.JoeShuff.SkyBasers.Items.HubItem;
import me.JoeShuff.SkyBasers.Items.KitItem;
import me.JoeShuff.SkyBasers.Items.PlayerStatItem;
import me.JoeShuff.SkyBasers.Items.SpectatorCompass;
import me.JoeShuff.SkyBasers.Items.SpectatorOptions;
import me.JoeShuff.SkyBasers.Items.TeamStick;
import me.JoeShuff.SkyBasers.MySQL.SQLManager;
import me.JoeShuff.SkyBasers.Timers.PreGameTimer;
import net.minecraft.server.v1_8_R3.EntityArmorStand;
import net.minecraft.server.v1_8_R3.EntityHorse;
import net.minecraft.server.v1_8_R3.EntityPlayer;
import net.minecraft.server.v1_8_R3.MinecraftServer;
import net.minecraft.server.v1_8_R3.PacketPlayOutSpawnEntityLiving;
import net.minecraft.server.v1_8_R3.PlayerInteractManager;
import net.minecraft.server.v1_8_R3.World;
import net.minecraft.server.v1_8_R3.WorldServer;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.Skull;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.inventory.FurnaceBurnEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import com.google.common.collect.Iterables;
import com.mojang.authlib.GameProfile;

public class PreListener implements Listener
{
	SkyBasers plugin;
	
	public PreListener(SkyBasers plugin)
	{
		Bukkit.getServer().getPluginManager().registerEvents(this, plugin);
		this.plugin = plugin;
	}
	
	@EventHandler
	public void furnaceBurn(FurnaceBurnEvent event)
	{
		if (event.getBlock().getLocation().distance(plugin.getSpawnLoc()) <= 100)
		{
			//event.setCancelled(true);
			event.setBurning(true);
			event.setBurnTime(60 * 20 * 60);
		}
	}
	
	@EventHandler
	public void chunkUnload(ChunkUnloadEvent event)
	{
		if (event.getChunk().getX() == -1 && event.getChunk().getZ() == 0)
		{
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void playerJoin(PlayerJoinEvent event)
	{		
		if (plugin.getState() == GameState.PREGAME)
		{
			Integer players = plugin.getConfig().getInt("max-players");
			event.setJoinMessage(null);
			VisualEffects.sendTitle(event.getPlayer(), ChatColor.GOLD + "Welcome to" , ChatColor.BLUE + "SkyBasers");
			VisualEffects.sendTabList(event.getPlayer(), "" + ChatColor.AQUA + ChatColor.BOLD + "SKYBASERS", ChatColor.BLUE + "play.mcjustice.net");
			Bukkit.getServer().broadcastMessage(ChatColor.GOLD + event.getPlayer().getName() + ChatColor.YELLOW + " has joined the game (" + Bukkit.getOnlinePlayers().size() + "/" + players + ")");
			event.getPlayer().teleport(plugin.getSpawnLoc());
			
			event.getPlayer().setGameMode(GameMode.ADVENTURE);
			
			event.getPlayer().setFoodLevel(20);
			event.getPlayer().setHealth(20);
			
			Integer minPlayers = plugin.getConfig().getInt("min-players");
			
			for (PotionEffect effect : event.getPlayer().getActivePotionEffects())
			{
				event.getPlayer().removePotionEffect(effect.getType());
			}
			
			if (Bukkit.getOnlinePlayers().size() >= minPlayers)
			{
				if (!plugin.isStarting())
				{
					plugin.setStarting(true);
					new PreGameTimer(plugin);
				}
			}
			
			event.getPlayer().getInventory().clear();
			event.getPlayer().getInventory().setHelmet(null);
			event.getPlayer().getInventory().setChestplate(null);
			event.getPlayer().getInventory().setLeggings(null);
			event.getPlayer().getInventory().setBoots(null);
			
			event.getPlayer().setExp(0f);
			event.getPlayer().setLevel(0);
			
			KitItem.getItem(event.getPlayer());
			HubItem.getItem(event.getPlayer(), Material.SUGAR);
			PlayerStatItem.getItem(event.getPlayer());
			
			if (plugin.isTeams())
			{
				event.getPlayer().getInventory().setItem(1, TeamStick.getItem());
			}
			
			plugin.getScoreBoardManager().updateScores(0);
		}
		else
		{
			if (plugin.getPlayerScoreboards(false) == null)
			{
				BungeeComm.sendMessage(Iterables.getFirst(Bukkit.getOnlinePlayers(),null), event.getPlayer().getName(), ChatColor.RED + "The game is starting");
				BungeeComm.sendToServer(Iterables.getFirst(Bukkit.getOnlinePlayers(),null), event.getPlayer().getName(), plugin.getConfig().getString("hub"));
				
				return;
			}
			
			if (!plugin.getPlayerScoreboards(false).hasAlivePlayer(event.getPlayer()))
			{
				event.setJoinMessage(null);
				VisualEffects.sendTitle(event.getPlayer(), ChatColor.RED + "Game has already begun!", "Watch quietly!");
				event.getPlayer().setAllowFlight(true);
				event.getPlayer().setGameMode(GameMode.CREATIVE);
				event.getPlayer().teleport(plugin.getCentreMap(true, 150));
				
				event.getPlayer().getInventory().clear();
				event.getPlayer().getInventory().setHelmet(null);
				event.getPlayer().getInventory().setChestplate(null);
				event.getPlayer().getInventory().setLeggings(null);
				event.getPlayer().getInventory().setBoots(null);	
				
				SpectatorCompass.getCompass(event.getPlayer());
				SpectatorOptions.getBlaze(event.getPlayer());
				HubItem.getItem(event.getPlayer(), Material.SUGAR);
				
				if (!plugin.getPlayerScoreboards(false).hasPlayer(event.getPlayer()))
				{
					plugin.getPlayerScoreboards(false).addNewPlayer(event.getPlayer(), false);	
					plugin.getPlayerScoreboards(false).joinTeam(event.getPlayer(), "spec");
				}
				else
				{
					if (plugin.getPlayerScoreboards(false).hasPlayerPlayed(event.getPlayer()))
					{
						plugin.getPlayerScoreboards(false).showSB(event.getPlayer());
						plugin.getPlayerScoreboards(false).joinTeam(event.getPlayer(), "dead");
					}
					else
					{
						plugin.getPlayerScoreboards(false).showSB(event.getPlayer());
						plugin.getPlayerScoreboards(false).joinTeam(event.getPlayer(), "spec");
					}
				}
				
				for (Player player : Bukkit.getOnlinePlayers())
				{
					if (!player.getName().equals(event.getPlayer().getName()))
					{
						if (plugin.getPlayerScoreboards(false).hasAlivePlayer(player))
						{
							player.hidePlayer(event.getPlayer());
						}
					}
				}
			}
			else
			{
				plugin.getPlayerScoreboards(false).showSB(event.getPlayer());
				plugin.getPlayerScoreboards(false).setPlayers(plugin.getPlayerScoreboards(false).getPlayers() + 1);
				event.setJoinMessage(ChatColor.RED + event.getPlayer().getName() + ChatColor.YELLOW + " has joined the game");
				
				for (Player player : Bukkit.getOnlinePlayers())
				{
					if (!plugin.getPlayerScoreboards(false).hasAlivePlayer(player))
					{
						event.getPlayer().hidePlayer(player);
					}
				}
				
				if (plugin.getStage() != GameStage.GATHER)
				{
					event.setJoinMessage(null);
					plugin.getGameListener().playerDeath(event.getPlayer(), DamageCause.CUSTOM);
					event.getPlayer().sendMessage(ChatColor.RED + "Since you joined after lava started to rise, you have been killed...");
				}
			}
		}
		
		SQLManager.checkPlayer(event.getPlayer());
	}
	
	@EventHandler
	public void playerLeave(PlayerQuitEvent event)
	{
		if (plugin.getState() == GameState.PREGAME)
		{
			Integer players = plugin.getConfig().getInt("max-players");
			event.setQuitMessage(null);
			Bukkit.getServer().broadcastMessage(ChatColor.GOLD + event.getPlayer().getName() + ChatColor.YELLOW + " has left the game (" + (Bukkit.getOnlinePlayers().size() - 1) + "/" + players + ")");
			plugin.getScoreBoardManager().updateScores(-1);
		}
		else
		{
			event.setQuitMessage(null);
			
			if (event.getPlayer().getGameMode() == GameMode.SURVIVAL)
			{
				Bukkit.broadcastMessage(ChatColor.RED + event.getPlayer().getName() + ChatColor.YELLOW + " has left the game");
				
				if (plugin.getStage() == GameStage.LAVA || plugin.getStage() == GameStage.AFTER || plugin.getPlayerScoreboards(false).getPlayers() - 1 <= 1 || plugin.getGameListener().damagers.containsKey(event.getPlayer().getName()))
				{
					plugin.getGameListener().playerDeath(event.getPlayer(), DamageCause.CUSTOM);
				}
				
				if (plugin.getPlayerScoreboards(false) != null)
				{
					plugin.getPlayerScoreboards(false).setPlayers(plugin.getPlayerScoreboards(false).getPlayers() - 1);
				}	
			}
		}
	}
	
	@EventHandler
	public void commandRun(PlayerCommandPreprocessEvent event)
	{
		String message;
		
		try {message = event.getMessage().substring(0, 5);} catch(Exception ex) {return;}
		
		message = message.toLowerCase();
		
		if (message.contains("/me") || message.contains("/tell") || message.contains("/say"))
		{
			event.getPlayer().sendMessage(ChatColor.RED + "You cannot use that command...");
			event.setCancelled(true);
			return;
		}
	}
	
	@EventHandler
	public void playerMove(PlayerMoveEvent event)
	{
		if (plugin.getState() == GameState.PREGAME)
		{
			if (event.getPlayer().getLocation().add(0, -1, 0).getBlock().getType() == Material.SLIME_BLOCK)
			{
				event.getPlayer().setVelocity(new Vector(-0.5,2,4));
			}
			
			if (((int) event.getPlayer().getLocation().distance(plugin.getSpawnLoc())) >= 100)
			{
				event.getPlayer().teleport(plugin.getSpawnLoc());
			}
			
			return;
		}
		
		Location l = new Location(Bukkit.getWorld(SkyBasers.worldName), -1000, 100, -1000);
		if (event.getPlayer().getLocation().distance(l) <= 300)
		{
			event.getPlayer().teleport(plugin.getSpawnLoc());
		}
	}
	
	@EventHandler
	public void playerDamage(EntityDamageEvent event)
	{
		if (plugin.getState() == GameState.PREGAME || plugin.getState() == GameState.LOADING)
		{
			event.setCancelled(true);			
			if (event.getEntity() instanceof Player)
			{
				Player player = (Player) event.getEntity();
				
				if (player.getLocation().getY() <= 0)
				{
					player.teleport(plugin.getSpawnLoc());
				}
			}
		}
	}
	
	@EventHandler
	public void itemDrop(PlayerDropItemEvent event)
	{
		if (plugin.getState() == GameState.PREGAME)
		{
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void playerInteract(PlayerInteractEvent event)
	{
		if (event.getClickedBlock() != null)
		{
			Block block = event.getClickedBlock();
			
			if (block.getType() == Material.SKULL)
			{
				Skull s = (Skull) block.getState();
				
				String owner = s.getOwner();
				
				if (owner.equalsIgnoreCase("MHF_Chest"))
				{
					event.getPlayer().playSound(event.getPlayer().getLocation(), Sound.LEVEL_UP, 1, 1);
				}
			}
			
			if (block.getType() == Material.ACACIA_DOOR)
			{
				event.setCancelled(true);
			}
		}
	}
	
	@EventHandler
	public void playerClick(PlayerInteractAtEntityEvent event)
	{
		if (event.getRightClicked() instanceof Player)
		{
			if (plugin.getState() == GameState.PREGAME)
			{
				Player player = (Player) event.getRightClicked();
				
				if (player.getName().equals("OmegaGamer777"))
				{
					Inventory inv = Bukkit.createInventory(null, 9, "Green Arrow's Quiver");
					
					for (int i = 0 ; i <= 8 ; i ++)
					{
						ItemStack arrow = new ItemStack(Material.ARROW);
						ItemMeta meta = arrow.getItemMeta();
						
						meta.setDisplayName(ChatColor.GREEN + "Green Arrow's Arrow");
						meta.setLore(Arrays.asList(ChatColor.RED + "Only to be used by Green Arrow"));
						
						arrow.setItemMeta(meta);
						
						inv.setItem(i, arrow);
					}
					
					event.getPlayer().openInventory(inv);
				}
			}
		}
		
		if (event.getRightClicked().getType() == EntityType.HORSE)
		{
			if (plugin.getState() == GameState.PREGAME)
			{
				if (event.getRightClicked().getLocation().distance(plugin.getSpawnLoc()) < 50)
				{
					event.getPlayer().sendMessage(ChatColor.GREEN + "Marcus: Oh hey, got any wheat?");
				}
			}
		}
		
		
	}
	
	@EventHandler
	public void blockBreak(BlockBreakEvent event)
	{
		if (plugin.getState() == GameState.PREGAME)
		{
			if (!PermissionGroups.playerHasPerm(event.getPlayer().getName(), "skybasers.Break"))
			{
				event.setCancelled(true);
			}	
		}
		
		if (plugin.getState() != GameState.LIVE)
		{
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void blockPlace(BlockPlaceEvent event)
	{
		if (plugin.getState() == GameState.PREGAME)
		{
			if (!PermissionGroups.playerHasPerm(event.getPlayer().getName(), "skybasers.Place"))
			{
				event.setCancelled(true);
			}
		}
		
		if (plugin.getState() == GameState.FINISHED)
		{
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void weatherChange(WeatherChangeEvent event)
	{
		event.setCancelled(true);
	}
	
	@EventHandler
	public void foodChange(FoodLevelChangeEvent event)
	{
		if (plugin.getState() != GameState.LIVE)
		{
			event.setCancelled(true);
			
			if (event.getEntity() instanceof Player)
			{
				Player player = (Player) event.getEntity();
				player.setFoodLevel(20);
			}
		}
	}
	
	@EventHandler
	public void clickInv(InventoryClickEvent event)
	{
		if (plugin.getState() == GameState.PREGAME)
		{
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void pickupItem(PlayerPickupItemEvent event)
	{
		if (plugin.getState() != GameState.LIVE)
		{
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void chatEvent(PlayerChatEvent event)
	{
		if (plugin.getState() != GameState.LIVE)
		{
			event.setCancelled(true);
			
			Bukkit.broadcastMessage(RankSystemBukkit.getMessage(event.getPlayer(), event.getMessage(), true));
		}
	}
}
