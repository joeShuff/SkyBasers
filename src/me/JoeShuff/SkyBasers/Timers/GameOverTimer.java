/*
 * Bukkit Plugin - SkyBasers
 * For use on The Justice Network
 * 
 * @author - Joseph Shufflebotham
 * 
 * © Copyright 2016 
 */
package me.JoeShuff.SkyBasers.Timers;

import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import me.JoeShuff.SkyBasers.BungeeComm;
import me.JoeShuff.SkyBasers.RankSystemBukkit;
import me.JoeShuff.SkyBasers.SkyBasers;
import me.JoeShuff.SkyBasers.SkyBasers.GameStage;
import me.JoeShuff.SkyBasers.SkyBasers.GameState;
import me.JoeShuff.SkyBasers.SkyBasers.GameType;
import me.JoeShuff.SkyBasers.VisualEffects;
import me.JoeShuff.SkyBasers.Items.HubItem;
import me.JoeShuff.SkyBasers.Items.KitItem;
import me.JoeShuff.SkyBasers.Items.PlayerStatItem;
import me.JoeShuff.SkyBasers.MySQL.SQLManager;
import me.JoeShuff.SkyBasers.MySQL.SQLManager.PlayerData;
import me.JoeShuff.SkyBasers.Scoreboards.ManagePlayerScoreboard.playerSB;
import me.JoeShuff.SkyBasers.WorldGen.ClearArena;
import net.minecraft.server.v1_8_R3.EnumParticle;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Difficulty;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class GameOverTimer extends BukkitRunnable {

	private SkyBasers plugin;
	
	public GameOverTimer(SkyBasers plugin)
	{
		this.plugin = plugin;
		
		timeLeftMin = plugin.getConfig().getInt("game-over-time");
		
		this.runTaskTimer(plugin, 20, 20);
	}
	
	private int timeLeftMin = 5;
	private int timeLeftSec = 0;
	
	private int finalTimer = 0;
	
	private boolean print = false;
	
	@Override
	public void run() {
		
		if (plugin.getState() == GameState.FINISHED)
		{
			plugin.getPlayerScoreboards(false).clear();
			
			plugin.getBosses().killAll();
			
			finalTimer ++;
			
			if (finalTimer == 1)
			{
				SQLManager.removeActives();
			}
			
			if (finalTimer == 12)
			{
				this.cancel();
				
				new ClearArena(plugin);
				
				for (Player player : Bukkit.getOnlinePlayers())
				{
					player.getInventory().clear();
					player.getInventory().setHelmet(null);
					player.getInventory().setChestplate(null);
					player.getInventory().setLeggings(null);
					player.getInventory().setBoots(null);
					
					plugin.setStage(GameStage.GATHER);
					
					plugin.getPlayerScoreboards(false).clear();
					player.setScoreboard(Bukkit.getServer().getScoreboardManager().getMainScoreboard());
					plugin.getScoreBoardManager().restart();
					
					plugin.setStarting(false);
					
					KitItem.getItem(player);
					HubItem.getItem(player, Material.SUGAR);
					PlayerStatItem.getItem(player);
					
					player.setGameMode(GameMode.ADVENTURE);
					Bukkit.getWorld(SkyBasers.worldName).setDifficulty(Difficulty.PEACEFUL);
					player.teleport(plugin.getSpawnLoc());
					
					for (Player p : Bukkit.getOnlinePlayers())
					{
						player.showPlayer(p);
					}
					
					BungeeComm.sendToServer(player, plugin.getConfig().getString("hub"));
				}
			}
			
			if (finalTimer == 3)
			{
				String winMessage = "" + ChatColor.GOLD + ChatColor.BOLD + ChatColor.STRIKETHROUGH + "========================================\n" + ChatColor.RESET + " \n";
				
				if (plugin.getWinner() == null)
				{
					winMessage = winMessage + ChatColor.RED + "                  DRAW                   \n \n" + ChatColor.GOLD + ChatColor.BOLD + ChatColor.STRIKETHROUGH + "========================================";
					Bukkit.broadcastMessage(winMessage);
				}
				else
				{
					winMessage = winMessage + ChatColor.RED;
					int nameLength = "WINNER: ".length();
					
					for (OfflinePlayer p : plugin.getWinner())
					{
						nameLength = nameLength + RankSystemBukkit.getPrefix(p).length();
					}
					
					int spaces = 40 - nameLength;
					spaces = (int) Math.floor(spaces / 2);
					
					for (int i = 0 ; i <= spaces ; i ++)
					{
						winMessage = winMessage + " ";
					}
					
					if (!plugin.isTeams())
					{
						winMessage = winMessage + ChatColor.GOLD + ChatColor.BOLD + "WINNER: " + plugin.getWinner().get(0).getName() + " \n \n";
					}
					else
					{
						winMessage = winMessage + "WINNER: ";
						
						for (OfflinePlayer p : plugin.getWinner())
						{
							winMessage = winMessage + RankSystemBukkit.getPrefix(p) + ", "; 
						}
						
						winMessage = winMessage.trim();
						winMessage = winMessage.substring(0, winMessage.length() - 1);
						
						winMessage = winMessage + "\n \n";
					}
					
					try
					{
						int killerLength = "1st Killer: ".length();
						
						List<playerSB> players = plugin.getPlayerScoreboards(false).getPlayedPlayers();
						
						HashMap<String, Integer> kills = new HashMap<String, Integer>();
						
						for (playerSB player : players)
						{
							kills.put(player.getName(), player.getKills());
						}
						
						HashMap<Integer, PlayerData> positions = new HashMap<Integer, PlayerData>();
						
						for (String key : kills.keySet())
						{
							int total = kills.get(key);
							
							for (int i = 1 ; i <= kills.size() ; i ++)
							{
								if (positions.containsKey(i))
								{
									if (positions.get(i).totalAmount < total)
									{								
										PlayerData pl = new PlayerData(total, Bukkit.getOfflinePlayer(key).getUniqueId().toString());
										
										for (int j = kills.size() ; j >= i ; j --)
										{
											if (positions.containsKey(j))
											{
												positions.put(j + 1, positions.get(j));
											}
										}
										
										positions.put(i, pl);
										break;
									}
								}
								else
								{
									positions.put(i, new PlayerData(total, Bukkit.getOfflinePlayer(key).getUniqueId().toString()));
									break;
								}
							}
						}
						
						PlayerData first = null;
						
						try {first = positions.get(1);} catch(Exception e){}
						
						PlayerData second = null;
						
						try {second = positions.get(2);} catch(Exception e){}
						
						PlayerData third = null;
						
						try {third = positions.get(3);} catch(Exception e){}
						
						if (first != null)
						{
							int firstLength = 40 - killerLength - (Bukkit.getOfflinePlayer(UUID.fromString(first.uuid)).getName() + " » " + first.totalAmount).length();
							
							for (int i = 0 ; i < Math.floor(firstLength / 2) ; i ++)
							{
								winMessage = winMessage + " ";
							}
							
							winMessage = winMessage + ChatColor.GOLD + ChatColor.BOLD + "1st Killer: " + Bukkit.getOfflinePlayer(UUID.fromString(first.uuid)).getName() + " » " + first.totalAmount;
						
							winMessage = winMessage + "\n";
						}
						
						if (second != null)
						{
							int secondLength = 40 - killerLength - (Bukkit.getOfflinePlayer(UUID.fromString(second.uuid)).getName() + " » " + second.totalAmount).length();
							
							for (int i = 0 ; i < Math.floor(secondLength / 2) ; i ++)
							{
								winMessage = winMessage + " ";
							}
							
							winMessage = winMessage + ChatColor.GRAY + ChatColor.BOLD + "2nd Killer: " + Bukkit.getOfflinePlayer(UUID.fromString(second.uuid)).getName() + " » " + second.totalAmount;
						
							winMessage = winMessage + "\n";
						}
						
						if (third != null)
						{
							int thirdLength = 40 - killerLength - (Bukkit.getOfflinePlayer(UUID.fromString(third.uuid)).getName() + " » " + third.totalAmount).length();
							
							for (int i = 0 ; i < Math.floor(thirdLength / 2) ; i ++)
							{
								winMessage = winMessage + " ";
							}
							
							winMessage = winMessage + ChatColor.DARK_RED + ChatColor.BOLD + "3rd Killer: " + Bukkit.getOfflinePlayer(UUID.fromString(third.uuid)).getName() + " » " + third.totalAmount;
						
							winMessage = winMessage + "\n \n";
						}
					} catch (Exception e)
					{
						if (!print)
						{
							e.printStackTrace();
							print = true;
						}
					}
					
					winMessage = winMessage + ChatColor.GOLD + ChatColor.BOLD + ChatColor.STRIKETHROUGH + "========================================";
					Bukkit.broadcastMessage(winMessage);
				}
			}
			
			if (plugin.getWinner() != null)
			{
				for (OfflinePlayer p : plugin.getWinner())
				{
					Player player = Bukkit.getPlayer(p.getUniqueId());
					
					if (player != null)
					{
						VisualEffects.sendParticle(true, player, EnumParticle.NOTE, 1f, 1f, 1f, 2f, 150);
						VisualEffects.playRandomFirework(player);
					}
				}
			}
			
			return;
		}
		
		timeLeftSec --;
		
		if (timeLeftSec < 0)
		{
			timeLeftMin --;
			timeLeftSec = 59;
		}
	
		if (timeLeftSec % 30 == 0)
		{
			spreadPlayers();
			
			if (plugin.getType() == GameType.JUDGEMENT && plugin.getStage() == GameStage.DOOM)
			{
				plugin.getBosses().addBoss();
			}
			else if (plugin.getType() == GameType.NORMAL && plugin.getStage() == GameStage.DOOM)
			{
				plugin.getBosses().addNormalBoss();
			}
		}
		else if (timeLeftSec % 30 == 5)
		{
			for (Player p : Bukkit.getOnlinePlayers())
			{
				VisualEffects.sendTitle(p, "" + ChatColor.BLUE + ChatColor.BOLD + "5" + ChatColor.GOLD + ChatColor.BOLD + " SECONDS", "" + ChatColor.GOLD + ChatColor.BOLD + "Until Player Spread");
			}
		}

		if (timeLeftMin == 0 && timeLeftSec == 0)
		{
			if (plugin.getStage() == GameStage.DOOM)
			{
				plugin.setWinner(null);
				
				for (Player player : Bukkit.getOnlinePlayers())
				{
					VisualEffects.sendTitle(player, "" + ChatColor.RED + ChatColor.BOLD + "GAME OVER", "" + ChatColor.GOLD + ChatColor.BOLD + "DRAW");
					
					player.setGameMode(GameMode.CREATIVE);
				}
				
				plugin.setState(GameState.FINISHED);
			}
			else if (plugin.getStage() == GameStage.AFTER)
			{
				timeLeftMin = plugin.getConfig().getInt("game-over-time");
				timeLeftSec = 0;
				
				plugin.setStage(GameStage.DOOM);
			}
		}
		
		String timeLeft;
		
		if (timeLeftMin > 0)
		{
			timeLeft = String.format("%02d", timeLeftMin) + ":" + String.format("%02d", timeLeftSec);
			plugin.getPlayerScoreboards(false).setTime(timeLeft);
		}
		else
		{
			timeLeft = "00:" + String.format("%02d", timeLeftSec);
			plugin.getPlayerScoreboards(false).setTime(timeLeft);
		}
	}

	private void spreadPlayers()
	{
		Location centre = plugin.getCentreMap(false, 0);
		
		Random rnd = new Random();
		
		for (Player p : Bukkit.getOnlinePlayers())
		{
			if (!plugin.getPlayerScoreboards(false).hasAlivePlayer(p))
			{
				continue;
			}
			
			boolean validSpot = false;
			
			Location l = null;
			
			int iterations = 0;
			
			while (!validSpot)
			{
				int x = (int) (centre.getX() + (rnd.nextInt(70) + 1 - 35));
				int z = (int) (centre.getZ() + (rnd.nextInt(70) + 1 - 35));
				
				int y = Bukkit.getWorld(plugin.worldName).getHighestBlockYAt(x, z);
				
				if (!Bukkit.getWorld(plugin.worldName).getBlockAt(x, y, z).getType().toString().contains("LAVA"))
				{
					validSpot = true;
					l = new Location(Bukkit.getWorld(plugin.worldName), x, y + 1, z);
				}
				
				iterations ++;
				
				if (iterations >= 50 || p.getLocation().getY() >= LavaTimer.height)
				{
					validSpot = true;
				}
			}				
			
			if (l != null)
			{
				VisualEffects.sendTitle(p, "" + ChatColor.RED + ChatColor.BOLD + "SPREAD", "");
				p.teleport(l);
			}
			else
			{
				VisualEffects.sendTitle(p, "" + ChatColor.RED + ChatColor.BOLD + "Stay here!","");
			}
		}
	}
	
}
