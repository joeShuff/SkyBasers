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
import java.util.Calendar;
import java.util.Date;

import me.JoeShuff.SkyBasers.SkyBasers;
import me.JoeShuff.SkyBasers.SkyBasers.GameType;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;

public class ManageHubScoreboard {
	
	private SkyBasers plugin;
	
	public ManageHubScoreboard(SkyBasers plugin)
	{
		this.plugin = plugin;
		init();
	}
	
	public void restart()
	{
		init();
	}
	
	private void init()
	{
		Objective hubObj = Bukkit.getServer().getScoreboardManager().getMainScoreboard().getObjective("hub");
		
		if (hubObj == null)
		{
			Bukkit.getServer().getScoreboardManager().getMainScoreboard().registerNewObjective("hub", "dummy");
		}
		
		hubObj.unregister();
		hubObj = Bukkit.getScoreboardManager().getMainScoreboard().registerNewObjective("hub", "dummy");
		hubObj.setDisplayName("" + ChatColor.AQUA + ChatColor.BOLD + "SKYBASERS");
		hubObj.setDisplaySlot(DisplaySlot.SIDEBAR);
		
		time = "Waiting for players...";
		
		updateScores(0);
	}

	public void setStartTime(String time,int change)
	{
		this.time = time;
		updateScores(change);
	}
	
	private String time = "Waiting for players...";
	
	private String OldPlayers = "";
	
	public void updateScores(int change)
	{	
		Objective hubObj = Bukkit.getScoreboardManager().getMainScoreboard().getObjective("hub");
		
		for (String player : Bukkit.getScoreboardManager().getMainScoreboard().getEntries())
		{
			if (player.contains("Waiting") || player.contains("/") || player.contains("Starting in"))
			{
				Bukkit.getScoreboardManager().getMainScoreboard().resetScores(player);
			}
		}
		
		hubObj.getScore(ChatColor.BLUE + "play.mcjustice.net").setScore(1);
		hubObj.getScore(" ").setScore(2);
		
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
		
		hubObj.getScore(ChatColor.WHITE + "Mode: " + color + plugin.getType().toString() + " " + ChatColor.GOLD + teams).setScore(3);
		hubObj.getScore("   ").setScore(4);
		hubObj.getScore("" + ChatColor.WHITE + "Players: " + ChatColor.GREEN + String.valueOf(Bukkit.getOnlinePlayers().size() + change) + "/" + plugin.getConfig().getInt("max-players")).setScore(5);
		hubObj.getScore("  ").setScore(6);
		//Time till start
		if (time.contains("Waiting"))
		{
			hubObj.getScore(ChatColor.WHITE + "Waiting for players...").setScore(7);
		}
		else
		{
			hubObj.getScore(ChatColor.WHITE + "Starting in " + ChatColor.GREEN + time + "s").setScore(7);
		}
		hubObj.getScore("    ").setScore(8);
	}
	
	private String stage = "";
	
	private String getDate()
	{
		Calendar cal = Calendar.getInstance();
		DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
		Date date = new Date();
		String now = dateFormat.format(date);
		
		return now;
	}
	
	public void setStage(String stage)
	{
		this.stage = stage;
		updatePrep();
	}
	
	public void setGenning()
	{
		Objective startObj = Bukkit.getScoreboardManager().getMainScoreboard().getObjective("prep");
		
		if (startObj == null)
		{
			Bukkit.getServer().getScoreboardManager().getMainScoreboard().registerNewObjective("prep", "dummy");
		}
		
		startObj.unregister();
		startObj = Bukkit.getScoreboardManager().getMainScoreboard().registerNewObjective("prep", "dummy");
		
		startObj.setDisplayName("" + ChatColor.AQUA + ChatColor.BOLD + "SKYBASERS");
		startObj.setDisplaySlot(DisplaySlot.SIDEBAR);		
	}
	
	private void updatePrep()
	{
		Objective startObj = Bukkit.getScoreboardManager().getMainScoreboard().getObjective("prep");
		
		for (String player : Bukkit.getScoreboardManager().getMainScoreboard().getEntries())
		{
			if (player.contains("Stage"))
			{
				Bukkit.getScoreboardManager().getMainScoreboard().resetScores(player);
			}
		}
		
		startObj.getScore(ChatColor.BLUE + "play.mcjustice.net").setScore(1);
		startObj.getScore(" ").setScore(2);
		
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
		
		startObj.getScore(ChatColor.WHITE + "Mode: " + color + plugin.getType().toString() + " " + ChatColor.GOLD + teams).setScore(3);
		startObj.getScore("  ").setScore(4);
		startObj.getScore(ChatColor.YELLOW + "Stage: " + ChatColor.GOLD + stage).setScore(5);
		startObj.getScore("   ").setScore(6);
	}
}
