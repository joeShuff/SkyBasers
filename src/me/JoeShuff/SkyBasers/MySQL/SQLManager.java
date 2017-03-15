/*
 * Bukkit Plugin - SkyBasers
 * For use on The Justice Network
 * 
 * @author - Joseph Shufflebotham
 * 
 * © Copyright 2016 
 */
package me.JoeShuff.SkyBasers.MySQL;

import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import me.JoeShuff.SkyBasers.BungeeComm;
import me.JoeShuff.SkyBasers.Kits;
import me.JoeShuff.SkyBasers.SkyBasers;
import me.JoeShuff.SkyBasers.SkyBasers.GameType;
import me.JoeShuff.SkyBasers.WorldGen.TerrainGen.Schematic;
import net.minecraft.server.v1_8_R3.NBTCompressedStreamTools;
import net.minecraft.server.v1_8_R3.NBTTagCompound;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public class SQLManager {

	private static SkyBasers plugin;
	
	public static void init(SkyBasers plugin)
	{
		SQLManager.plugin = plugin;
	}
	
	public static void checkPlayer(Player player)
	{
		String UUID = player.getUniqueId().toString();
		
		Statement s = null;
		ResultSet results = null;
		
		try
		{
			Connection conn = MySQL.getConnection();
			s = conn.createStatement();
			
			results = s.executeQuery("SELECT * FROM `SkyBasers` WHERE `UUID`='" + UUID + "'");
			
			if (!results.next())
			{
				s.executeUpdate("INSERT INTO `SkyBasers` (`UUID`,`NSoloWins`,`NSoloKills`,`NTeamWins`,`NTeamKills`,`JSoloWins`,`JSoloKills`,`JTeamWins`,`JTeamKills`,`Deaths`,`Kit`,`Kits`,`Block`) VALUES ('" + UUID + "','0','0','0','0','0','0','0','0','0','Default','null','STAINED_GLASS:1,1')");
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
	}
	
	public static class PlayerData
	{
		public int totalAmount;
		public String uuid;
		
		public PlayerData(int Amount, String uuid)
		{
			this.totalAmount = Amount;
			this.uuid = uuid;
		}
	}
	
	public static int attempts = 0;
	
	public static void sendServerRequest(String servername, int amountofPlayers, String type, String force, Boolean cancel)
	{
		Statement s = null;
		ResultSet results = null;
		
		try
		{
			Connection conn = MySQL.getConnection();
			s = conn.createStatement();
			
			results = s.executeQuery("SELECT * FROM `SBRequests` WHERE `mode`='server'");
			
			boolean foundServer = false;
			
			while (results.next())
			{
				String loopServer = results.getString("name");
				
				if (loopServer.equals(servername));
				{
					foundServer = true;
				}
			}
			
			if (foundServer)
			{
				if (force.equals("true"))
				{
					s.executeUpdate("UPDATE `SBRequests` SET `amount`='" + amountofPlayers + "', `forceUp`='" + force + "',`type`='" + type + "',`cancel`='" + String.valueOf(cancel) + "' WHERE `name`='" + servername + "' AND `mode`='server';");
					System.out.println("Successfully updated server request!");
				}	
			}
			else
			{
				if (force.equalsIgnoreCase("true") || cancel)
				{
					s.executeUpdate("INSERT INTO `SBRequests` (`mode`,`name`,`value`, `amount`,`forceUp`,`type`,`cancel`) VALUES ('server', '" + servername + "','NULL', '" + amountofPlayers + "','" + String.valueOf(force) + "','" + type + "','" + String.valueOf(cancel) + "');");	
					System.out.println("Successfully sent server request!");
				}
				
			}
		} catch (Exception e) {
			
			plugin.getLogger().info("Error sending player request!");
			attempts ++;
			
			Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "reload");
			
			if (attempts == 1)
			{
				e.printStackTrace();	
			}
		}
		finally
		{
			try {s.close();} catch (Exception e){}
		}
	}
	
	public static String getClass(OfflinePlayer player)
	{
		Statement s = null;
		ResultSet SQLresult = null;
		
		String UUID = player.getUniqueId().toString();
		
		String chosenClass = "";
		
		try
		{
			Connection conn = MySQL.getConnection();
			s = conn.createStatement();
			
			SQLresult = s.executeQuery("SELECT `Kit` FROM `SkyBasers` WHERE `UUID`='" + UUID + "'");
			
			if (SQLresult.next())
			{
				chosenClass = SQLresult.getString("Kit");
			}
		}
		catch (Exception e){}
		finally
		{
			try {SQLresult.close();} catch (Exception e){}
			try {s.close();} catch (Exception e){}
		}
		
		if (chosenClass.equals("null") || chosenClass == null || Kits.getKit(chosenClass) == null)
		{
			setClass(player, "Default");
			return getClass(player);
		}
		
		return chosenClass;
	}
	
	public static void setClass(OfflinePlayer player, String kit)
	{
		Statement s = null;
		ResultSet SQLresult = null;
		
		String UUID = player.getUniqueId().toString();
		
		try
		{
			Connection conn = MySQL.getConnection();
			s = conn.createStatement();
			
			SQLresult = s.executeQuery("SELECT `Kit` FROM `SkyBasers` WHERE `UUID`='" + UUID + "'");
			
			if (SQLresult.next())
			{
				s.executeUpdate("UPDATE `SkyBasers` SET `Kit`= '" + kit + "' WHERE `UUID` = '" + UUID + "';");
			}
		}
		catch (Exception e){}
		finally
		{
			try {SQLresult.close();} catch (Exception e){}
			try {s.close();} catch (Exception e){}
		}
	}
	
	public static List<Integer> getPlayerPos(String UUID,List<String> data)
	{
		List<String> UUids = new ArrayList<String>();
		
		for (Player player : Bukkit.getOnlinePlayers())
		{
			UUids.add(player.getUniqueId().toString());
		}
		
		HashMap<Integer, PlayerData> result = getTop(UUids.size(), data, UUids);
		
		if (MySQL.getConnection() == null)
		{
			return null;
		}
		
		for (Integer key : result.keySet())
		{
			PlayerData player = result.get(key);
			
			if (player.uuid.equals(UUID))
			{
				return Arrays.asList(key, player.totalAmount);
			}
		}
		
		return null;
	}
	
	public static HashMap<Integer, PlayerData> getTop(int amount, List<String> data,List<String> UUIDs)
	{
		HashMap<String, Integer> results = new HashMap<String, Integer>();
		
		HashMap<Integer, PlayerData> send = new HashMap<Integer, PlayerData>();
		
		Statement s = null;
		ResultSet SQLresult = null;
		
		try
		{
			for (String uuid : UUIDs)
			{
				for (String value : data)
				{
					Connection conn = MySQL.getConnection();
					s = conn.createStatement();
					
					SQLresult = s.executeQuery("SELECT `" + value + "` FROM `SkyBasers` WHERE `UUID`='" + uuid + "'");
					
					while (SQLresult.next())
					{
						int intResult = SQLresult.getInt(value);
						
						if (results.containsKey(uuid))
						{
							int current = results.get(uuid);
							current = current + intResult;
							results.put(uuid, current);
						}
						else
						{
							results.put(uuid, intResult);
						}
					}	
				}
			}
			
			for (String key : results.keySet())
			{
				int total = results.get(key);
				
				for (int i = 1 ; i <= amount ; i ++)
				{
					if (send.containsKey(i))
					{
						if (send.get(i).totalAmount < total)
						{
							PlayerData pl = new PlayerData(total, key);
							
							for (int j = amount ; j >= i ; j --)
							{
								if (send.containsKey(j))
								{
									send.put(j + 1, send.get(j));
								}
							}
							
//							if (send.containsKey(amount + 1))
//							{
//								send.remove(amount + 1);
//							}
							
							send.put(i, pl);
							break;
						}
					}
					else
					{
						send.put(i, new PlayerData(total,key));
						break;
					}
				}
			}
			
		}
		catch (Exception e) {send = null;}
		finally
		{
			try {SQLresult.close();} catch (Exception e){}
			try {s.close();} catch (Exception e){}
		}
		
		if (send == null)
		{
			return null;
		}
		
//		HashMap<Integer, PlayerData> sendData = new HashMap<Integer, PlayerData>();
//		
//		for (int i = 1 ; i <= amount ; i ++)
//		{
//			sendData.put(i, send.get(i));
//		}
		
		return send;
	}
	
	public static int getData(Player player, String data)
	{
		int result = -1;
		
		String UUID = player.getUniqueId().toString();
		
		Statement s = null;
		ResultSet results = null;
		
		try
		{
			Connection conn = MySQL.getConnection();
			s = conn.createStatement();
			
			results = s.executeQuery("SELECT * FROM `SkyBasers` WHERE `UUID`='" + UUID + "'");
			
			if (results.next())
			{
				result = results.getInt(data);
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
		
		return result;
	}
	
	public static void playerKill(OfflinePlayer player, OfflinePlayer killed)
	{
		GameType type = plugin.getType();
		boolean teams = plugin.isTeams();
		
		for (Player p : Bukkit.getOnlinePlayers())
		{
			BungeeComm.sendMessage(p, player.getName(),"" + ChatColor.GREEN + ChatColor.BOLD + "+" + plugin.killReward + " Justice Coin (Kill)");
			break;
		}
		
		if (Bukkit.getPlayer(player.getName()) != null)
		{
			Bukkit.getPlayer(player.getName()).playSound(Bukkit.getPlayer(player.getName()).getLocation(), Sound.ORB_PICKUP, 1f, 1f);
			
			if (plugin.isTeams())
			{
				String teammate = plugin.getTeamManager().getTeamMate((Player) player);
				
				if (teammate != null)
				{
					if (Bukkit.getPlayer(teammate) != null)
					{
						Bukkit.getPlayer(teammate).playSound(Bukkit.getPlayer(teammate).getLocation(), Sound.ORB_PICKUP, 1f, 1f);
					}
				}
			}
		}
		
		String UUID = player.getUniqueId().toString();
		
		Statement s = null;
		ResultSet results = null;
		
		addJusticeCoin(player, plugin.killReward);
		
		try
		{
			Connection conn = MySQL.getConnection();
			s = conn.createStatement();
			
			results = s.executeQuery("SELECT * FROM `SkyBasers` WHERE `UUID`='" + UUID + "'");
			
			if (!results.next())
			{
				if (type == GameType.NORMAL)
				{
					if (teams == true)
					{
						s.executeUpdate("INSERT INTO `SkyBasers` (`UUID`,`NSoloWins`,`NSoloKills`,`NTeamWins`,`NTeamKills`,`JSoloWins`,`JSoloKills`,`JTeamWins`,`JTeamKills`,`Deaths`,`Kit`,`Kits`) VALUES ('" + UUID + "','0','0','0','1','0','0','0','0','0','null','0')");
					}
					else
					{
						s.executeUpdate("INSERT INTO `SkyBasers` (`UUID`,`NSoloWins`,`NSoloKills`,`NTeamWins`,`NTeamKills`,`JSoloWins`,`JSoloKills`,`JTeamWins`,`JTeamKills`,`Deaths`,`Kit`,`Kits`) VALUES ('" + UUID + "','0','1','0','0','0','0','0','0','0','null','0')");
					}
				}
				else
				{
					if (teams == true)
					{
						s.executeUpdate("INSERT INTO `SkyBasers` (`UUID`,`NSoloWins`,`NSoloKills`,`NTeamWins`,`NTeamKills`,`JSoloWins`,`JSoloKills`,`JTeamWins`,`JTeamKills`,`Deaths`,`Kit`,`Kits`) VALUES ('" + UUID + "','0','0','0','0','0','0','0','1','0','null','0')");
					}
					else
					{
						s.executeUpdate("INSERT INTO `SkyBasers` (`UUID`,`NSoloWins`,`NSoloKills`,`NTeamWins`,`NTeamKills`,`JSoloWins`,`JSoloKills`,`JTeamWins`,`JTeamKills`,`Deaths`,`Kit`,`Kits`) VALUES ('" + UUID + "','0','0','0','0','0','1','0','0','0','null','0')");
					}
				}
			}
			else
			{
				if (type == GameType.NORMAL)
				{
					if (teams == true)
					{
						int kills = results.getInt("NTeamKills");
						
						kills ++;
						
						s.executeUpdate("UPDATE `SkyBasers` SET `NTeamKills`='" + kills + "' WHERE `UUID`='" + UUID + "';");
					}
					else
					{
						int kills = results.getInt("NSoloKills");
						
						kills ++;
						
						s.executeUpdate("UPDATE `SkyBasers` SET `NSoloKills`='" + kills + "' WHERE `UUID`='" + UUID + "';");
					}
				}
				else
				{
					if (teams == true)
					{
						int kills = results.getInt("JTeamKills");
						
						kills ++;
						
						s.executeUpdate("UPDATE `SkyBasers` SET `JTeamKills`='" + kills + "' WHERE `UUID`='" + UUID + "';");
					}
					else
					{
						int kills = results.getInt("JSoloKills");
						
						kills ++;
						
						s.executeUpdate("UPDATE `SkyBasers` SET `JSoloKills`='" + kills + "' WHERE `UUID`='" + UUID + "';");
					}
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
		
		
		//playerDeath(killed);		
	}
	
	public static void playerDeath(OfflinePlayer killed)
	{
		for (Player p : Bukkit.getOnlinePlayers())
		{
			BungeeComm.sendMessage(p, killed.getName(),"" + ChatColor.GREEN + ChatColor.BOLD + "+" + plugin.playReward + " Justice Coin (Playing)");
			break;
		}
		
		addJusticeCoin(killed, plugin.playReward);
		
		String deadUUID = killed.getUniqueId().toString();
		
		Statement s = null;
		ResultSet results = null;
		
		try
		{
			Connection conn = MySQL.getConnection();
			s = conn.createStatement();
			
			results = s.executeQuery("SELECT * FROM `SkyBasers` WHERE `UUID`='" + deadUUID + "'");
			
			if (results.next())
			{
				int deaths = results.getInt("Deaths");
				
				deaths ++;
				
				s.executeUpdate("UPDATE `SkyBasers` SET `Deaths`='" + deaths + "' WHERE `UUID`='" + deadUUID + "';");
			}
		}
		catch (Exception e) {}
		finally 
		{
			try {results.close();} catch (Exception e){}
			try {s.close();} catch (Exception e){}
		}
	}
	
	public static void win(List<OfflinePlayer> winners)
	{
		GameType type = plugin.getType();
		boolean teams = plugin.isTeams();
		
		for (OfflinePlayer p : winners)
		{			
			for (Player player : Bukkit.getServer().getOnlinePlayers())
			{						
				BungeeComm.sendMessage(player, p.getName(), "" + ChatColor.GREEN + ChatColor.BOLD + "+" + plugin.winReward + " Justice Coins (Win)");
				BungeeComm.sendMessage(player, p.getName(), "" + ChatColor.GREEN + ChatColor.BOLD + "+" + plugin.playReward + " Justice Coins (Playing)");
				break;
			}
			
			Statement s = null;
			ResultSet results = null;
			
			addJusticeCoin(p, plugin.winReward);
			addJusticeCoin(p, plugin.playReward);
			
			try
			{
				Connection conn = MySQL.getConnection();
				s = conn.createStatement();
				
				String UUID = p.getUniqueId().toString();
				
				results = s.executeQuery("SELECT * FROM `SkyBasers` WHERE `UUID`='" + UUID + "'");
				
				if (!results.next())
				{
					if (type == GameType.NORMAL)
					{
						if (teams == true)
						{
							s.executeUpdate("INSERT INTO `SkyBasers` (`UUID`,`NSoloWins`,`NSoloKills`,`NTeamWins`,`NTeamKills`,`JSoloWins`,`JSoloKills`,`JTeamWins`,`JTeamKills`,`Deaths`,`Kit`,`Kits`) VALUES ('" + UUID + "','0','0','1','0','0','0','0','0','0','null','0')");
						}
						else
						{
							s.executeUpdate("INSERT INTO `SkyBasers` (`UUID`,`NSoloWins`,`NSoloKills`,`NTeamWins`,`NTeamKills`,`JSoloWins`,`JSoloKills`,`JTeamWins`,`JTeamKills`,`Deaths`,`Kit`,`Kits`) VALUES ('" + UUID + "','1','0','0','0','0','0','0','0','0','null','0')");
						}
					}
					else
					{
						if (teams == true)
						{
							s.executeUpdate("INSERT INTO `SkyBasers` (`UUID`,`NSoloWins`,`NSoloKills`,`NTeamWins`,`NTeamKills`,`JSoloWins`,`JSoloKills`,`JTeamWins`,`JTeamKills`,`Deaths`,`Kit`,`Kits`) VALUES ('" + UUID + "','0','0','0','0','0','0','1','0','0','null','0')");
						}
						else
						{
							s.executeUpdate("INSERT INTO `SkyBasers` (`UUID`,`NSoloWins`,`NSoloKills`,`NTeamWins`,`NTeamKills`,`JSoloWins`,`JSoloKills`,`JTeamWins`,`JTeamKills`,`Deaths`,`Kit`,`Kits`) VALUES ('" + UUID + "','0','0','0','0','1','0','0','0','0','null','0')");
						}
					}
				}
				else
				{
					if (type == GameType.NORMAL)
					{
						if (teams == true)
						{
							int wins = results.getInt("NTeamWins");
							
							wins ++;
							
							s.executeUpdate("UPDATE `SkyBasers` SET `NTeamWins`='" + wins + "' WHERE `UUID`='" + UUID + "';");
						}
						else
						{
							int wins = results.getInt("NSoloWins");
							
							wins ++;
							
							s.executeUpdate("UPDATE `SkyBasers` SET `NSoloWins`='" + wins + "' WHERE `UUID`='" + UUID + "';");
						}
					}
					else
					{
						if (teams == true)
						{
							int wins = results.getInt("JTeamWins");
							
							wins ++;
							
							s.executeUpdate("UPDATE `SkyBasers` SET `JTeamWins`='" + wins + "' WHERE `UUID`='" + UUID + "';");
						}
						else
						{
							int wins = results.getInt("JSoloWins");
							
							wins ++;
							
							s.executeUpdate("UPDATE `SkyBasers` SET `JSoloWins`='" + wins + "' WHERE `UUID`='" + UUID + "';");
						}
					}
				}
			} catch (Exception ex)
			{
				
			} finally
			{
				try {results.close();} catch (Exception e){}
				try{s.close();} catch (Exception e){}
			}
		}
	}
	
	public static void addJusticeCoin(OfflinePlayer player, int amount)
	{
		String UUID = player.getUniqueId().toString();
		
		Statement s = null;
		ResultSet results = null;
		
		try
		{
			Connection conn = MySQL.getConnection();
			s = conn.createStatement();
			
			results = s.executeQuery("SELECT * FROM `Justice Coins` WHERE `UUID`='" + UUID + "'");
			
			if (!results.next())
			{
				s.executeUpdate("INSERT INTO `Justice Coins` (`UUID`,`balance`) VALUES ('" + UUID + "','" + amount + "')");
			}
			else
			{
				int coins = results.getInt("balance");
				
				coins = coins + amount;

				s.executeUpdate("UPDATE `Justice Coins` SET `balance`='" + coins + "' WHERE `UUID`='" + UUID + "';");
			}
			
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			try {results.close();} catch (Exception e){}
			try {s.close();} catch (Exception e){}
		}
	}

	public static boolean playerHasKit(String kitName, Player player) {
		
		String UUID = player.getUniqueId().toString();
		
		Statement s = null;
		ResultSet results = null;
		
		boolean hasKit = false;
		
		try
		{
			Connection conn = MySQL.getConnection();
			s = conn.createStatement();
			
			results = s.executeQuery("SELECT `Kits` FROM `SkyBasers` WHERE `UUID`='" + UUID + "'");
			
			if (results.next())
			{
				String kits = results.getString("Kits");
				
				if (kits.toLowerCase().contains(kitName.toLowerCase()))
				{
					hasKit = true;
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			try {results.close();} catch (Exception e){}
			try {s.close();} catch (Exception e){}
		}
		
		return hasKit;
	}
	
	public static class MBlock {
		
		private Material m;
		private byte data;
		
		public MBlock(Material m, byte data)
		{
			this.m = m;
			this.data = data;
		}
		
		public Material getMaterial()
		{
			return m;
		}
		
		public byte getData()
		{
			return data;
		}
	}
	
	public static MBlock getPodBlock(OfflinePlayer p)
	{
		Material m = Material.DIRT;
		byte data = 0;
		
		String UUID = p.getUniqueId().toString();
		
		Statement s = null;
		ResultSet results = null;
		
		try
		{
			Connection conn = MySQL.getConnection();
			s = conn.createStatement();
			
			results = s.executeQuery("SELECT `Block` FROM `SkyBasers` WHERE `UUID`='" + UUID + "'");
			
			String chosenblock = "";
			
			if (results.next())
			{
				chosenblock = results.getString("Block");
			}
			
			if (chosenblock.contains(","))
			{
				String split[] = chosenblock.split(",");
				chosenblock = split[0];
			}
			
			if (chosenblock.contains(":"))
			{
				String parts[] = chosenblock.split(":");
				chosenblock = parts[0];
				data = Byte.valueOf(parts[1]);
				m = Material.valueOf(chosenblock.toUpperCase());
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			try {results.close();} catch (Exception e){}
			try {s.close();} catch (Exception e){}
		}
		
		
		return new MBlock(m,data);
	}
	
	public static byte getPodColor(OfflinePlayer p)
	{
		byte data = 0;
		
		String UUID = p.getUniqueId().toString();
		
		Statement s = null;
		ResultSet results = null;
		
		try
		{
			Connection conn = MySQL.getConnection();
			s = conn.createStatement();
			
			results = s.executeQuery("SELECT `Block` FROM `SkyBasers` WHERE `UUID`='" + UUID + "'");
			
			String chosenblock = "";
			
			if (results.next())
			{
				chosenblock = results.getString("Block");
			}
			
			if (chosenblock.contains(","))
			{
				String split[] = chosenblock.split(",");
				data = Byte.valueOf(split[1]);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			try {results.close();} catch (Exception e){}
			try {s.close();} catch (Exception e){}
		}
		
		return data;
	}
	
	public static void setPodData(OfflinePlayer p, Material block, Byte data, Byte glassColor)
	{
		String UUID = p.getUniqueId().toString();
		
		Statement s = null;
		ResultSet results = null;
		
		try
		{
			Connection conn = MySQL.getConnection();
			s = conn.createStatement();
			
			String toSet = block.toString() + ":" + data.toString() + "," + glassColor.toString();
			
			s.executeUpdate("UPDATE `SkyBasers` SET `Block`='" + toSet + "' WHERE `UUID`='" + UUID + "'");
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			try {results.close();} catch (Exception e){}
			try {s.close();} catch (Exception e){}
		}
	}
	
	public static void sendPlayerRequest(Player player, String mode) 
	{
		String name = player.getName();
		
		Statement s = null;
		Statement s2 = null;
		ResultSet results = null;
		
		String sendMode = mode;
		
		if (sendMode.contains("§"))
		{
			sendMode = sendMode.substring(2, sendMode.length());
		}

		if (sendMode.equals("SPECTATE"))
		{
			sendMode = "SPECTATOR";
		}
		else if (sendMode.equals("RANDOM"))
		{
			sendMode = "ANY";
		}
		else if (sendMode.contains("TEAMS"))
		{
			sendMode = sendMode.replace("TEAMS", "TEAM");
		}
		
		
		try
		{
			Connection conn = MySQL.getConnection();
			s = conn.createStatement();
			s2 = conn.createStatement();
			
			results = s2.executeQuery("SELECT * FROM `SBRequests` WHERE `mode`='player' AND `name`='" + name + "'");
			
			if (results.next())
			{
				s.executeUpdate("UPDATE `SBRequests` SET `mode`='player', `name`='" + name + "', `value`='" + sendMode + "' WHERE `name`='" + name + "' AND `mode`='player'");
			}
			else
			{
				s.executeUpdate("INSERT INTO `SBRequests` (`mode`,`name`,`value`) VALUES ('player','" + name + "','" + sendMode + "')");
			}
			
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			try {results.close();} catch (Exception e){}
			try {s.close();} catch (Exception e){}
			try {s2.close();} catch (Exception e){}
		}
		
	}
	
	public static boolean buildPod(Player player,Location l)
	{
		Statement s = null;
		ResultSet results = null;
		
		String chosenblock = "";
		Byte blockdata = 0;
		
		Byte stainedGlassColor = 0;
		
		String UUID = player.getUniqueId().toString();
		
		Schematic pod = null;		
		
		try
		{
			Connection conn = MySQL.getConnection();
			s = conn.createStatement();
			
			results = s.executeQuery("SELECT `Block` FROM `SkyBasers` WHERE `UUID`='" + UUID + "'");
			
			if (results.next())
			{
				chosenblock = results.getString("Block");
			}
			
			if (chosenblock.contains(","))
			{
				String split[] = chosenblock.split(",");
				chosenblock = split[0];
				stainedGlassColor = Byte.valueOf(split[1]);
			}
			
			if (chosenblock.contains(":"))
			{
				String parts[] = chosenblock.split(":");
				chosenblock = parts[0];
				blockdata = Byte.valueOf(parts[1]);
			}
		}
		catch (Exception e)
		{
			return false;
		}
		finally 
		{
			try {results.close();} catch (Exception e){}
			try {s.close();} catch (Exception e){}
		}
		
		String filename = "/pod.schematic";
		
		if (plugin.isTeams())
		{
			filename = "/teamPod.schematic";
		}
		
		try {
			FileInputStream fis = new FileInputStream(plugin.getDataFolder() + filename);
			NBTTagCompound nbtdata = NBTCompressedStreamTools.a(fis);
           
            short width = nbtdata.getShort("Width");
            short height = nbtdata.getShort("Height");
            short length = nbtdata.getShort("Length");
 
            byte[] blocks = nbtdata.getByteArray("Blocks");
            byte[] data = nbtdata.getByteArray("Data");
           
            fis.close();
            
            pod = new Schematic(blocks, data, width, length, height);  
            
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
		
		
		
		int startx = (int) l.getX() - 1;
		int startz = (int) l.getZ() - 1;
		
		if (plugin.isTeams())
		{
			startx --;
			startz --;
		}
		
		for (int level = 0 ; level <= pod.getHeight() - 1 ; level ++)
		{
			for (int x = 0 ; x <= pod.getWidth() - 1 ; x ++)
			{
				for (int z = 0 ; z <= pod.getLength() - 1 ; z ++)
				{
					int index = level * pod.getWidth() * pod.getLength() + z * pod.getWidth() + x;
					
		            final Location loc = new Location(Bukkit.getWorld(SkyBasers.worldName), x + startx, level + l.getY(), z + startz);
		            int b = pod.getBlocks()[index] & 0xFF;
		            final Block block = loc.getBlock();

		            if (block.getType() != Material.BARRIER)
		            {
		            	Byte data = pod.getData()[index];
		            	
			            Material set = Material.getMaterial(b);
			            	
			            if (set == Material.DIRT)
			            {
			            	set = Material.valueOf(chosenblock.toUpperCase());
			            	data = blockdata;
			            }
			            	
			            if (set == Material.STAINED_GLASS_PANE)
			            {
			            	data = stainedGlassColor;
			            }
			            	
			            block.setType(set);
				        block.setData(data);
		            }
				}
			}	
		}
		
		
		
		return true;
	}

	public static void sendActivePlayer(Player player) 
	{
		Statement s = null;
		ResultSet results = null;
		
		try
		{
			Connection conn = MySQL.getConnection();
			s = conn.createStatement();
			
			results = s.executeQuery("SELECT * FROM `PlayerINFO` WHERE `UUID` = '" + player.getUniqueId().toString().replace("-", "") + "'");
			
			if (results.next())
			{
				s.executeUpdate("UPDATE `PlayerINFO` SET `default` = '" + Bukkit.getServerName() + "' WHERE `UUID`='" + player.getUniqueId().toString().replace("-", "") + "';");
			}
			else
			{
				s.executeUpdate("INSERT INTO `SBRequests` (`UUID`,`default`) VALUES ('" + player.getUniqueId().toString().replace("-", "") + "','" + Bukkit.getServerName() + "');");
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			try {results.close();} catch (Exception e){}
			try {s.close();} catch (Exception e){}
		}
		
		
	}

	public static void removeActives() 
	{
		Statement s = null;
		ResultSet results = null;
		
		try
		{
			Connection conn = MySQL.getConnection();
			s = conn.createStatement();
			
			s.executeUpdate("UPDATE `PlayerINFO` SET `default` = '" + plugin.getConfig().getString("hub") + "' WHERE `default`='" + Bukkit.getServerName() + "';");
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			try {results.close();} catch (Exception e){}
			try {s.close();} catch (Exception e){}
		}
		
	}

	public static void removeActive(OfflinePlayer player) 
	{
		Statement s = null;
		ResultSet results = null;
		
		try
		{
			Connection conn = MySQL.getConnection();
			s = conn.createStatement();
			
			results = s.executeQuery("SELECT * FROM `PlayerINFO` WHERE `UUID` = '" + player.getUniqueId().toString().replace("-", "") + "'");
			
			if (results.next())
			{
				s.executeUpdate("UPDATE `PlayerINFO` SET `default` = '" + plugin.getConfig().getString("hub") + "' WHERE `UUID`='" + player.getUniqueId().toString().replace("-", "") + "';");
			}
			else
			{
				s.executeUpdate("INSERT INTO `SBRequests` (`UUID`,`default`) VALUES ('" + player.getUniqueId().toString().replace("-", "") + "','" + plugin.getConfig().getString("hub-world") + "');");
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			try {results.close();} catch (Exception e){}
			try {s.close();} catch (Exception e){}
		}
		
	}
	
	public static boolean hasBungeeRestarted()
	{
		Statement s = null;
		ResultSet results = null;
		
		try
		{
			Connection conn = MySQL.getConnection();
			s = conn.createStatement();
			
			results = s.executeQuery("SELECT * FROM `SBRequests` WHERE `mode`='bungee'");
			
			return results.next();
		}
		catch (Exception e)
		{
			return false;
		}
		finally
		{
			try {results.close();} catch (Exception e){}
			try {s.close();} catch (Exception e){}
		}
	}
}
