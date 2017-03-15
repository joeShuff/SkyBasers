package me.JoeShuff.SkyBasers;

import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import me.JoeShuff.SkyBasers.MySQL.MySQL;

import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public class RankSystemBukkit implements Listener {

	private static SkyBasers plugin;
	
	private static HashMap<String, Rank> ranks = new HashMap<String, Rank>();
	
	public static void bootup(SkyBasers plugin)
	{
		RankSystemBukkit.plugin = plugin;
		
		plugin.ftpManager.client.downloadTextToLocalFile("/config/ranks.yml", new File(plugin.getDataFolder(), "ranks.yml").toPath().toString());
	
		FileConfiguration config = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), "ranks.yml"));
		
		List<String> listRank = new ArrayList<String>();
		listRank = config.getStringList("ranks");
		
		for (String rank : listRank)
		{
			String prefix = config.getString(rank + ".prefix");
			String color = config.getString(rank + ".chat");
			
			ranks.put(rank, new Rank(prefix, color));
		}
	}
	
	public static String getMessage(Player player, String message, Boolean showPrefix)
	{
		String uuid = player.getUniqueId().toString().replace("-", "");
		
		Statement s = null;
		ResultSet results = null;
		
		String finalMessage = "";
		
		try
		{
			Connection conn = MySQL.getConnection();
			s = conn.createStatement();
			
			results = s.executeQuery("SELECT `Rank` FROM `PlayerINFO` WHERE `UUID`='" + uuid + "'");
			
			if (results.next())
			{
				Rank rank = null;
				
				if (ranks.containsKey(results.getString("Rank")))
				{
					rank = ranks.get(results.getString("Rank"));
				}
				else
				{
					rank = ranks.get("Default");
				}
				
				if (rank != null)
				{
					if (showPrefix)
					{
						finalMessage = ChatColor.translateAlternateColorCodes('&', rank.prefix) + player.getName() + ChatColor.translateAlternateColorCodes('&', rank.color) + ": " + message;
					}
					else
					{
						finalMessage = getPrefixColor(player) + player.getName() + ChatColor.translateAlternateColorCodes('&', rank.color) + ": " + message;
					}	
				}
				else
				{
					finalMessage = player.getName() + ": " + message;
				}
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
		
		return finalMessage;
		
	}
	
	public static String getPrefix(OfflinePlayer player)
	{
		String uuid = player.getUniqueId().toString().replace("-", "");
		
		Statement s = null;
		ResultSet results = null;
		
		String finalMessage = "";
		
		try
		{
			Connection conn = MySQL.getConnection();
			s = conn.createStatement();
			
			results = s.executeQuery("SELECT `Rank` FROM `PlayerINFO` WHERE `UUID`='" + uuid + "'");
			
			if (results.next())
			{
				Rank rank = null;
				
				if (ranks.containsKey(results.getString("Rank")))
				{
					rank = ranks.get(results.getString("Rank"));
				}
				else
				{
					rank = ranks.get("Default");
				}
				
				if (rank != null)
				{
					finalMessage = ChatColor.translateAlternateColorCodes('&', rank.prefix) + player.getName();
				}
				else
				{
					finalMessage = "" + ChatColor.WHITE + player.getName();
				}
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
		
		return finalMessage;
	}
	
	public static String getPrefixColor(OfflinePlayer player)
	{
		String uuid = player.getUniqueId().toString().replace("-", "");
		
		Statement s = null;
		ResultSet results = null;
		
		String finalMessage = "";
		
		try
		{
			Connection conn = MySQL.getConnection();
			s = conn.createStatement();
			
			results = s.executeQuery("SELECT `Rank` FROM `PlayerINFO` WHERE `UUID`='" + uuid + "'");
			
			if (results.next())
			{
				Rank rank = null;
				
				if (ranks.containsKey(results.getString("Rank")))
				{
					rank = ranks.get(results.getString("Rank"));
				}
				else
				{
					rank = ranks.get("Default");
				}
				
				if (rank != null)
				{
					String color = rank.prefix.trim();
					
					color = color.substring(color.length() - 2, color.length());
					
					finalMessage = ChatColor.translateAlternateColorCodes('&', color);
				}
				else
				{
					finalMessage = "" + ChatColor.WHITE;
				}
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
		
		return finalMessage;
	}
	
	public static class Rank
	{
		private String prefix = "";
		private String color = "";
		
		public Rank(String prefix, String color)
		{
			this.prefix = prefix;
			this.color = color;
		}
		
	}
}
