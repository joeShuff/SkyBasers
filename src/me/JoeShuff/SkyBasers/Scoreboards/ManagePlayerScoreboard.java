/*
 * Bukkit Plugin - SkyBasers
 * For use on The Justice Network
 * 
 * @author - Joseph Shufflebotham
 * 
 * © Copyright 2016 
 */
package me.JoeShuff.SkyBasers.Scoreboards;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import me.JoeShuff.SkyBasers.SkyBasers;
import me.JoeShuff.SkyBasers.SkyBasers.GameStage;
import me.JoeShuff.SkyBasers.SkyBasers.GameType;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

public class ManagePlayerScoreboard {

	public class playerSB {
		
		private Scoreboard sb;
		private int kills;
		
		private String name;
		
		private boolean alive = false;
		private boolean playedThisGame = false;
		
		public playerSB(Scoreboard sb, boolean alive, boolean played, String name)
		{
			this.name = name;
			
			this.alive = alive;
			this.sb = sb;
			this.kills = 0;
			this.playedThisGame = played;
		}
		
		public String getName()
		{
			return name;
		}
		
		public boolean isAlive()
		{
			return alive;
		}
		
		public boolean hasPlayed()
		{
			return playedThisGame;
		}
		
		public Scoreboard getScoreboard()
		{
			return sb;
		}
		
		public int getKills()
		{
			return kills;
		}
		
		public void kill()
		{
			this.kills += 1;
		}
	}
	
	private HashMap<String,playerSB> scoreboards = new HashMap<String, playerSB>();
	
	private SkyBasers plugin;
	
	public ManagePlayerScoreboard(SkyBasers plugin)
	{
		scoreboards = new HashMap<String, playerSB>();
		this.plugin = plugin;
		playersLeft = Bukkit.getOnlinePlayers().size();
		timeLeft = String.valueOf(plugin.getConfig().getInt("game-time")) + ":00";
	}
	
	private int playersLeft = 12;
	private int teamsLeft = 12;
	private String timeLeft = "10:00";
	
	public void setPlayers(int players)
	{
		this.playersLeft = players;
		this.update();
	}
	
	public void setTeams(int teams)
	{
		this.teamsLeft = teams;
		this.update();
	}
	
	public int getPlayers()
	{
		return playersLeft;
	}
	
	public void showSB(Player player)
	{
		if (scoreboards.containsKey(player.getName()))
		{
			player.setScoreboard(scoreboards.get(player.getName()).getScoreboard());
		}
	}
	
	public void playerDeath(OfflinePlayer player)
	{
		if (scoreboards.containsKey(player.getName()))
		{
			playerSB data = scoreboards.get(player.getName());
			
			data.alive = false;
		}
	}
	
	public void setTime(String timeleft)
	{
		this.timeLeft = timeleft;
		this.update();
	}
	
	public void death(OfflinePlayer killed, OfflinePlayer killer)
	{
		if (Bukkit.getPlayer(killed.getName()) != null)
		{
			playersLeft --;
		}

		if (killer != null)
		{
			if (scoreboards.containsKey(killer.getName()))
			{
				playerSB pSB = scoreboards.get(killer.getName());
				
				pSB.kill();
				scoreboards.put(killer.getName(), pSB);
			}
		}
		
		update();
	}
	
	public boolean hasAlivePlayer(Player player)
	{
		return hasAlivePlayer(player.getName());
	}
	
	public boolean hasAlivePlayer(String player)
	{
		if (scoreboards.containsKey(player))
		{
			playerSB data = scoreboards.get(player);
			
			if (data.isAlive())
			{
				return true;
			}
			else
			{
				return false;
			}
		}
		else
		{
			return false;
		}
	}
	
	public boolean hasPlayerPlayed(Player player)
	{
		return hasPlayerPlayed(player.getName());
	}
	
	public boolean hasPlayerPlayed(String player)
	{
		if (scoreboards.containsKey(player))
		{
			playerSB data = scoreboards.get(player);
			
			return data.hasPlayed();
		}
		else
		{
			return false;
		}
	}
	
	public List<playerSB> getPlayedPlayers()
	{
		List<playerSB> players = new ArrayList<playerSB>();
		
		for (String key : scoreboards.keySet())
		{
			if (hasPlayerPlayed(key))
			{
				players.add(scoreboards.get(key));
			}
		}
		
		return players;
	}
	
	public boolean hasPlayer(Player player)
	{
		if (scoreboards.containsKey(player.getName()))
		{
			return true;
		}
		
		return false;
	}
	
	private String dateStarted;
	
	public List<String> getAlivePlayers() 
	{
		List<String> players = new ArrayList<String>();
		
		for (String key : scoreboards.keySet())
		{
			if (scoreboards.get(key).alive == true)
			{
				players.add(key);
			}
		}
		
		return players;
	}
	
	public void joinTeam(Player p, String team)
	{
		List<String> keys = new ArrayList<String>();
		
		keys.addAll(scoreboards.keySet());
		
		for (String key : keys)
		{
			scoreboards.get(key).sb.getTeam(team).addEntry(p.getName());
		}
	}
	
	public void bootup()
	{
		Scoreboard sb;
		
		playersLeft = this.plugin.getConfig().getInt("max-players");
		
		this.dateStarted = getDate();
		
		for (Player player : Bukkit.getOnlinePlayers())
		{
			sb = Bukkit.getScoreboardManager().getNewScoreboard();
			
			Team enemies = sb.registerNewTeam("enemies");
			Team friend = sb.registerNewTeam("friend");
			Team spec = sb.registerNewTeam("spec");
			Team dead = sb.registerNewTeam("dead");
			enemies.setPrefix(ChatColor.RED + "");
			friend.setPrefix(ChatColor.GREEN + "");
			spec.setPrefix(ChatColor.GRAY + "");
			dead.setPrefix(ChatColor.DARK_GRAY + "[" + ChatColor.RED + "DEAD" + ChatColor.DARK_GRAY + "] " + ChatColor.RED);
			enemies.setSuffix(ChatColor.RESET + "");
			friend.setSuffix(ChatColor.RESET + "");
			spec.setSuffix(ChatColor.RESET + "");
			dead.setSuffix(ChatColor.RESET + "");
			
			Objective obj = sb.registerNewObjective("skybasers", "dummy");
			Objective health = sb.registerNewObjective("health", "health");
			health.setDisplaySlot(DisplaySlot.PLAYER_LIST);
			
			obj.setDisplayName("" + ChatColor.AQUA + ChatColor.BOLD + "SKYBASERS");
			obj.setDisplaySlot(DisplaySlot.SIDEBAR);
			
			obj.getScore(ChatColor.BLUE + "play.mcjustice.net").setScore(1);
			obj.getScore(" ").setScore(2);
			
			ChatColor color = ChatColor.BLUE;
			
			if (plugin.getType() == GameType.JUDGEMENT)
			{
				color = ChatColor.RED;
			}
			
			String teams = "SOLO";
			if (plugin.isTeams())
			{
				teams = "TEAMS";
			}
			
			obj.getScore(ChatColor.WHITE + "Mode: " + color + plugin.getType().toString() + " " + ChatColor.GOLD + teams).setScore(3);
			obj.getScore("  ").setScore(4);
			
			obj.getScore(ChatColor.WHITE + "Kills: " + ChatColor.GREEN + "0").setScore(5);
			obj.getScore("   ").setScore(6);
			
			if (!plugin.isTeams())
			{
				obj.getScore(ChatColor.WHITE + "Players left: " + ChatColor.GREEN + playersLeft + "/" + plugin.getConfig().getInt("max-players")).setScore(7);
				obj.getScore("     ").setScore(8);
				
				GameStage stage = plugin.getStage();
				
				if (stage == GameStage.GATHER)
				{
					obj.getScore(ChatColor.RED + "Lava in " + ChatColor.GREEN + timeLeft).setScore(9);
				}
				else if (stage == GameStage.LAVA)
				{
					obj.getScore(ChatColor.RED + "Lava rising...").setScore(9);
				}
				else if (stage == GameStage.AFTER)
				{
					obj.getScore(ChatColor.DARK_RED + "Doom in " + ChatColor.GREEN + timeLeft).setScore(9);
				}
				else if (stage == GameStage.DOOM)
				{
					obj.getScore(ChatColor.DARK_RED + "Game Over in " + ChatColor.GREEN + timeLeft).setScore(9);
				}
				
				obj.getScore("      ").setScore(10);
				
				obj.getScore(ChatColor.GRAY + dateStarted).setScore(11);
			}
			else
			{
				obj.getScore(ChatColor.WHITE + "Players left: " + ChatColor.GREEN + playersLeft + "/" + plugin.getConfig().getInt("max-players")).setScore(7);
				obj.getScore(ChatColor.WHITE + "Teams left: " + ChatColor.GREEN + plugin.getTeamManager().amountOfTeams()).setScore(8);
				obj.getScore("     ").setScore(9);
				
				GameStage stage = plugin.getStage();
				
				if (stage == GameStage.GATHER)
				{
					obj.getScore(ChatColor.RED + "Lava in " + ChatColor.GREEN + timeLeft).setScore(10);
				}
				else if (stage == GameStage.LAVA)
				{
					obj.getScore(ChatColor.RED + "Lava rising...").setScore(10);
				}
				else if (stage == GameStage.AFTER)
				{
					obj.getScore(ChatColor.DARK_RED + "Doom in " + ChatColor.GREEN + timeLeft).setScore(10);
				}
				else if (stage == GameStage.DOOM)
				{
					obj.getScore(ChatColor.DARK_RED + "Game Over in " + ChatColor.GREEN + timeLeft).setScore(10);
				}
				
				obj.getScore("      ").setScore(11);
				
				obj.getScore(ChatColor.GRAY + dateStarted).setScore(12);
			}
			
			playerSB thisplayer = new playerSB(sb,true, true, player.getName());
			
			scoreboards.put(player.getName(), thisplayer);
			player.setScoreboard(sb);
			
			setEnemies(player);
		}
	}
	
	private String getDate()
	{
		Calendar cal = Calendar.getInstance();
		DateFormat dateFormat = new SimpleDateFormat("dd MMM, yyyy");
		Date date = new Date();
		String now = dateFormat.format(date);
		
		return now;
	}
	
	public void clear()
	{
		for (Player player: Bukkit.getOnlinePlayers())
		{
			if (!scoreboards.containsKey(player.getName()))
			{
				return;
			}
			
			playerSB playerData = scoreboards.get(player.getName());
			Scoreboard sb = playerData.getScoreboard();
			
			sb.clearSlot(DisplaySlot.SIDEBAR);
		}
	}
	
	public void addNewPlayer(Player player, boolean played)
	{
		Scoreboard sb = Bukkit.getScoreboardManager().getNewScoreboard();
		
		Team enemies = sb.registerNewTeam("enemies");
		Team friendly = sb.registerNewTeam("friend");
		Team dead = sb.registerNewTeam("dead");
		Team spec = sb.registerNewTeam("spec");
		enemies.setPrefix(ChatColor.RED + "");
		friendly.setPrefix(ChatColor.GREEN + "");
		spec.setPrefix(ChatColor.GRAY + "");
		dead.setPrefix(ChatColor.DARK_GRAY + "[" + ChatColor.RED + "DEAD" + ChatColor.DARK_GRAY + "] " + ChatColor.RED);
		enemies.setSuffix(ChatColor.RESET + "");
		friendly.setSuffix(ChatColor.RESET + "");
		spec.setSuffix(ChatColor.GRAY + "");
		dead.setSuffix(ChatColor.RESET + "");
		
		for (Player p : Bukkit.getOnlinePlayers())
		{
			if (p.getName().equals(player.getName())) continue;
			
			if (hasAlivePlayer(p))
			{
				enemies.addEntry(p.getName());
			}
			else if (hasPlayerPlayed(p))
			{
				dead.addEntry(p.getName());
			}
			else
			{
				spec.addEntry(p.getName());
			}
		}
		
		Objective obj = sb.registerNewObjective("skybasers", "dummy");
		Objective health = sb.registerNewObjective("health", "health");
		health.setDisplaySlot(DisplaySlot.PLAYER_LIST);
		obj.setDisplayName("" + ChatColor.AQUA + ChatColor.BOLD + "SKYBASERS");
		obj.setDisplaySlot(DisplaySlot.SIDEBAR);
		
		obj.getScore(ChatColor.BLUE + "play.mcjustice.net").setScore(1);
		obj.getScore(" ").setScore(2);
		
		ChatColor color = ChatColor.BLUE;
		
		if (plugin.getType() == GameType.JUDGEMENT)
		{
			color = ChatColor.RED;
		}
		
		String teams = "SOLO";
		if (plugin.isTeams())
		{
			teams = "TEAMS";
		}
		
		obj.getScore(ChatColor.WHITE + "Mode: " + color + plugin.getType().toString() + " " + ChatColor.GOLD + teams).setScore(3);
		obj.getScore("  ").setScore(4);
		
		obj.getScore(ChatColor.WHITE + "Kills: " + ChatColor.GREEN + "N/A").setScore(5);
		obj.getScore("   ").setScore(6);
		
		if (!plugin.isTeams())
		{
			obj.getScore(ChatColor.WHITE + "Players left: " + ChatColor.GREEN + playersLeft + "/" + plugin.getConfig().getInt("max-players")).setScore(7);
			obj.getScore("     ").setScore(8);
			
			GameStage stage = plugin.getStage();
			
			if (stage == GameStage.GATHER)
			{
				obj.getScore(ChatColor.RED + "Lava in " + ChatColor.GREEN + timeLeft).setScore(9);
			}
			else if (stage == GameStage.LAVA)
			{
				obj.getScore(ChatColor.RED + "Lava rising...").setScore(9);
			}
			else if (stage == GameStage.AFTER)
			{
				obj.getScore(ChatColor.DARK_RED + "Doom in " + ChatColor.GREEN + timeLeft).setScore(9);
			}
			else if (stage == GameStage.DOOM)
			{
				obj.getScore(ChatColor.DARK_RED + "Game Over in " + ChatColor.GREEN + timeLeft).setScore(9);
			}
			
			obj.getScore("      ").setScore(10);
			
			obj.getScore(ChatColor.GRAY + dateStarted).setScore(11);
		}
		else
		{
			obj.getScore(ChatColor.WHITE + "Players left: " + ChatColor.GREEN + playersLeft + "/" + plugin.getConfig().getInt("max-players")).setScore(7);
			obj.getScore(ChatColor.WHITE + "Teams left: " + ChatColor.GREEN + teamsLeft).setScore(8);
			obj.getScore("     ").setScore(9);
			
			GameStage stage = plugin.getStage();
			
			if (stage == GameStage.GATHER)
			{
				obj.getScore(ChatColor.RED + "Lava in " + ChatColor.GREEN + timeLeft).setScore(10);
			}
			else if (stage == GameStage.LAVA)
			{
				obj.getScore(ChatColor.RED + "Lava rising...").setScore(10);
			}
			else if (stage == GameStage.AFTER)
			{
				obj.getScore(ChatColor.DARK_RED + "Doom in " + ChatColor.GREEN + timeLeft).setScore(10);
			}
			else if (stage == GameStage.DOOM)
			{
				obj.getScore(ChatColor.DARK_RED + "Game Over in " + ChatColor.GREEN + timeLeft).setScore(10);
			}
			
			obj.getScore("      ").setScore(11);
			
			obj.getScore(ChatColor.GRAY + dateStarted).setScore(12);
		}
		
		playerSB thisplayer = new playerSB(sb, false, played,player.getName());
		
		scoreboards.put(player.getName(), thisplayer);
		player.setScoreboard(sb);
		
		setEnemies(player);
	}
	
	public void update()
	{
		for (Player player : Bukkit.getOnlinePlayers())
		{
			if (!scoreboards.containsKey(player.getName()))
			{
				return;
			}
			
			playerSB playerData = scoreboards.get(player.getName());
			Scoreboard sb = playerData.getScoreboard();
			
			for (String playerS : sb.getEntries())
			{
				if (playerS.contains("Kills") || playerS.contains("Players left") || playerS.contains("Lava") || playerS.contains("Game Over") || playerS.contains("Teams left") || playerS.contains("Doom in"))
				{
					sb.resetScores(playerS);
				}
			}
			
			Objective obj = sb.getObjective("skybasers");
			
			obj.getScore(ChatColor.BLUE + "play.mcjustice.net").setScore(1);
			obj.getScore(" ").setScore(2);
			
			ChatColor color = ChatColor.BLUE;
			
			if (plugin.getType() == GameType.JUDGEMENT)
			{
				color = ChatColor.RED;
			}
			
			String teams = "SOLO";
			if (plugin.isTeams())
			{
				teams = "TEAMS";
			}
			
			obj.getScore(ChatColor.WHITE + "Mode: " + color + plugin.getType().toString() + " " + ChatColor.GOLD + teams).setScore(3);	
			obj.getScore("  ").setScore(4);
			
			obj.getScore(ChatColor.WHITE + "Kills: " + ChatColor.GREEN + playerData.getKills()).setScore(5);
			obj.getScore("   ").setScore(6);
			
			if (!plugin.isTeams())
			{
				obj.getScore(ChatColor.WHITE + "Players left: " + ChatColor.GREEN + playersLeft + "/" + plugin.getConfig().getInt("max-players")).setScore(7);
				obj.getScore("     ").setScore(8);
				
				GameStage stage = plugin.getStage();
				
				if (stage == GameStage.GATHER)
				{
					obj.getScore(ChatColor.RED + "Lava in " + ChatColor.GREEN + timeLeft).setScore(9);
				}
				else if (stage == GameStage.LAVA)
				{
					obj.getScore(ChatColor.RED + "Lava rising...").setScore(9);
				}
				else if (stage == GameStage.AFTER)
				{
					obj.getScore(ChatColor.DARK_RED + "Doom in " + ChatColor.GREEN + timeLeft).setScore(9);
				}
				else if (stage == GameStage.DOOM)
				{
					obj.getScore(ChatColor.DARK_RED + "Game Over in " + ChatColor.GREEN + timeLeft).setScore(9);
				}
				
				obj.getScore("      ").setScore(10);
				
				obj.getScore(ChatColor.GRAY + dateStarted).setScore(11);
			}
			else
			{
				obj.getScore(ChatColor.WHITE + "Players left: " + ChatColor.GREEN + playersLeft + "/" + plugin.getConfig().getInt("max-players")).setScore(7);
				obj.getScore(ChatColor.WHITE + "Teams left: " + ChatColor.GREEN + teamsLeft).setScore(8);
				obj.getScore("     ").setScore(9);
				
				GameStage stage = plugin.getStage();
				
				if (stage == GameStage.GATHER)
				{
					obj.getScore(ChatColor.RED + "Lava in " + ChatColor.GREEN + timeLeft).setScore(10);
				}
				else if (stage == GameStage.LAVA)
				{
					obj.getScore(ChatColor.RED + "Lava rising...").setScore(10);
				}
				else if (stage == GameStage.AFTER)
				{
					obj.getScore(ChatColor.DARK_RED + "Doom in " + ChatColor.GREEN + timeLeft).setScore(10);
				}
				else if (stage == GameStage.DOOM)
				{
					obj.getScore(ChatColor.DARK_RED + "Game Over in " + ChatColor.GREEN + timeLeft).setScore(10);
				}
				
				obj.getScore("      ").setScore(11);
				
				obj.getScore(ChatColor.GRAY + dateStarted).setScore(12);
			}	
		}
	}
	
	public void setEnemies(Player p) 
	{
		playerSB sb = scoreboards.get(p.getName());
			
		Team enemies = sb.getScoreboard().getTeam("enemies");
		Team friendly = sb.getScoreboard().getTeam("friend");
		
		for (Player p2 : Bukkit.getOnlinePlayers())
		{
			sb.getScoreboard().getObjective("health").getScore(p2.getName()).setScore(20);
			
			if (p.getName().equals(p2.getName()))
			{
				friendly.addEntry(p.getName());
				continue;
			}
			
			if (plugin.isTeams())
			{
				if (plugin.getTeamManager().getTeamMate(p) != null)
				{
					if (plugin.getTeamManager().getTeamMate(p.getName()).equals(p2.getName()))
					{
						friendly.addEntry(p2.getName());
					}
					else
					{
						enemies.addEntry(p2.getName());
					}
				}
				else
				{
					enemies.addEntry(p2.getName());
				}
			}
			else 
			{
				enemies.addEntry(p2.getName());
			}
		}
		
		scoreboards.put(p.getName(), sb);
		
	}
}
