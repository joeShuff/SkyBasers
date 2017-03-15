/*
 * Bukkit Plugin - SkyBasers
 * For use on The Justice Network
 * 
 * @author - Joseph Shufflebotham
 * 
 * © Copyright 2016 
 */
package me.JoeShuff.SkyBasers.WorldGen;

import java.util.List;

import me.JoeShuff.SkyBasers.Kits;
import me.JoeShuff.SkyBasers.SkyBasers;
import me.JoeShuff.SkyBasers.SkyBasers.GameState;
import me.JoeShuff.SkyBasers.SkyBasers.GameType;
import me.JoeShuff.SkyBasers.VisualEffects;
import me.JoeShuff.SkyBasers.MySQL.SQLManager;
import me.JoeShuff.SkyBasers.Timers.GameTimer;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Difficulty;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;

public class Generator extends BukkitRunnable {
	
	public enum genStage
	{
		GROUND,
		LOOT,
		CAVES,
		TERRAIN
	}
	
	private genStage stage = genStage.GROUND;
	
	public SkyBasers plugin;
	
	public Generator(SkyBasers plugin)
	{
		this.plugin = plugin;
		generateMap();
	}
	
	GroundGen groundGen = null;
	ChestGen chestGen = null;
	TerrainGen terrainGen = null;
	CaveGen caveGen = null;
	
	public void generateMap()
	{
		plugin.getScoreBoardManager().setGenning();
		plugin.getScoreBoardManager().setStage("Ground");
		stage = genStage.GROUND;
		
		Bukkit.getWorld(SkyBasers.worldName).setDifficulty(Difficulty.PEACEFUL);
		
		groundGen = new GroundGen(plugin);
		chestGen = new ChestGen(plugin);
		terrainGen = new TerrainGen(plugin);
		caveGen = new CaveGen(plugin);
		
		this.runTaskTimer(plugin, 10, 10);
	}

	@Override
	public void run()
	{
		if (stage == genStage.GROUND)
		{
			if (groundGen != null)
			{
				if (groundGen.generateGround() == 21)
				{
					groundGen = null;
					plugin.getScoreBoardManager().setStage("Caves");
					stage = genStage.CAVES;
				}
			}
		}
		else if (stage == genStage.TERRAIN)
		{
			if (terrainGen != null)
			{
				if (terrainGen.generateLevel() == 30)
				{
					terrainGen = null;
					plugin.getScoreBoardManager().setStage("Loot");
					stage = genStage.LOOT;
				}
			}
			
		}
		else if (stage == genStage.CAVES)
		{
			if (caveGen.generateSection() == true)
			{
				caveGen = null;
				plugin.getScoreBoardManager().setStage("Terrain");
				stage = genStage.TERRAIN;
			}
		}
		else if (stage == genStage.LOOT)
		{
			if (chestGen != null)
			{
				chestGen.generateChests();

				this.cancel();
				
				chestGen = null;
				
				//Bootup the game
				
				Location loc = plugin.getCentreMap(false, 0);
				//Set the bottom layer to lava
				for (int i = -35 ; i <= 35 ; i ++)
				{
					for (int j = -35 ; j <= 35 ; j ++)
					{
						if (Bukkit.getWorld(SkyBasers.worldName).getBlockAt((int) loc.getX() + i,  (int) loc.getY() + 1, (int) loc.getZ() + j).getType() == Material.AIR)
						{
							Bukkit.getWorld(SkyBasers.worldName).getBlockAt((int) loc.getX() + i,  (int) loc.getY() + 1, (int) loc.getZ() + j).setType(Material.LAVA);
						}
					}
				}
				
				Location loc2 = plugin.getCentreMap(true, 150);
				//Remove all of the pods
				for (int h = 0 ; h <= 5; h ++)
				{
					for (int i = -35 ; i <= 35 ; i ++)
					{
						for (int j = -35 ; j <= 35 ; j ++)
						{
							Bukkit.getWorld(SkyBasers.worldName).getBlockAt((int) loc2.getX() + i,  150 + h, (int) loc2.getZ() + j).setType(Material.AIR);
						}
					}
				}
				
				for (Player player : Bukkit.getOnlinePlayers())
				{
					player.getInventory().clear();
					
					player.setFoodLevel(20);
					player.setSaturation(20.0f);
					
					player.setGameMode(GameMode.SURVIVAL);
					player.getInventory().addItem(new ItemStack(Material.COOKED_BEEF,5));
					
					VisualEffects.sendTitle(player, ChatColor.GREEN + "GAME START", ChatColor.GOLD + "Gather Resources");
					
					SQLManager.sendActivePlayer(player);
					
					if (plugin.kitsEnabled())
					{
						String kit = SQLManager.getClass(player);
						
						List<ItemStack> chosenKit = Kits.getKit(kit);
						
						if (chosenKit != null)
						{
							for (ItemStack item : chosenKit)
							{
								player.getInventory().addItem(item);
							}	
						}
					}
					
					for (PotionEffect effect : player.getActivePotionEffects())
					{
						player.removePotionEffect(effect.getType());
					}
				}
				
				for (Entity e : Bukkit.getWorld(SkyBasers.worldName).getEntities())
				{
					if (e instanceof Player)
					{
						continue;
					}
					
					if (e instanceof Item || (int) e.getLocation().distance(plugin.getCentreMap(false, 0)) <= 250)
					{
						e.remove();
					}
				}
				
				if (plugin.getType() == GameType.NORMAL)
				{
					Bukkit.getWorld(SkyBasers.worldName).setDifficulty(Difficulty.EASY);
				}
				else
				{
					Bukkit.getWorld(SkyBasers.worldName).setDifficulty(Difficulty.NORMAL);
				}
				
				plugin.setState(GameState.LIVE);
				this.plugin.getPlayerScoreboards(true).setPlayers(Bukkit.getOnlinePlayers().size());
				
				if (this.plugin.isTeams())
				{
					this.plugin.getPlayerScoreboards(false).setTeams(plugin.getTeamManager().amountOfTeams());
				}
				
				//Send spectator request
				SQLManager.sendServerRequest(Bukkit.getServerName(), plugin.getConfig().getInt("max-players"), "SPECTATOR", "true",false);
				
				this.plugin.getGameListener().defaultChannels();
				new GameTimer(plugin);
			}
		}

	}
	
}
