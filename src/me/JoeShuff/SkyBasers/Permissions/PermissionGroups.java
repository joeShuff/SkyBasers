/*
 * Bukkit Plugin - SkyBasers
 * For use on The Justice Network
 * 
 * @author - Joseph Shufflebotham
 * 
 * © Copyright 2016 
 */
package me.JoeShuff.SkyBasers.Permissions;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import me.JoeShuff.SkyBasers.SkyBasers;

public class PermissionGroups implements Listener {
	
	private static List<Group> groups = new ArrayList<Group>();
	
	private static File groupsFile = null;
	private static FileConfiguration groupsConfig = null;
	
	private final SkyBasers plugin;
	
	public PermissionGroups(SkyBasers plugin)
	{
		this.plugin = plugin;
		
		Bukkit.getServer().getPluginManager().registerEvents(this, plugin);
		
		LoadPermGroups(plugin);
	}
	
	public static void refreshGroups(SkyBasers plugin)
	{
		groups = new ArrayList<Group>();
		LoadPermGroups(plugin);
		plugin.getServer().broadcastMessage(ChatColor.GREEN + "Groups Refreshed");
	}
	
	public static void LoadPermGroups(SkyBasers plugin)
	{
		groupsFile = new File(plugin.getDataFolder(),"groups.yml");
		if (!groupsFile.exists()) 
		{
		     try
		     {
		       plugin.saveResource("groups.yml", true);
		       groupsFile.createNewFile();
		     }
		     catch (Exception e)
		     {
		       System.out.println("Couldn't create file groups.yml");
		     }
		}
		
		groupsConfig = YamlConfiguration.loadConfiguration(groupsFile);
		
		List<String> groupNames = groupsConfig.getStringList("groups");
		
		for (String group : groupNames)
		{
			if (group.equals("DefaultGroup"))
			{
				List<String> players = new ArrayList<String>();
				for (Player player : Bukkit.getServer().getOnlinePlayers())
				{
					players.add(player.getName());
				}
				
				List<String> perms = new ArrayList<String>();
				
				groups.add(new Group(group,players,perms,plugin));
				
				continue;
			}
			
			try 
			{
				List<String> players = groupsConfig.getStringList(group + ".players");
				List<String> perms = groupsConfig.getStringList(group + ".permissions");

				groups.add(new Group(group,players,perms,plugin));
			} catch (Exception e) 
			{
				plugin.getLogger().info("Unable to create group " + group);
			}
		}
	}
	
	public static Group getGroup(String groupname)
	{
		for (Group group : groups)
		{
			if (group.getGroupName().equals(groupname))
			{
				return group;
			}
		}
		
		return null;
	}
	
	public static List<Group> playerGroups(String playername)
	{
		List<Group> foundGroups = new ArrayList<Group>();
		
		for (Group group : groups)
		{
			for (String player : group.getPlayers())
			{
				if (playername.equals(player))
				{
					foundGroups.add(group);
				}
			}
		}
		
		return foundGroups;
	}
	
	public static List<Group> getGroups()
	{
		return groups;
	}
	
	public static boolean playerHasPerm(String playername, String permission)
	{
		for (Group group : playerGroups(playername))
		{
			if (group.hasPermission(permission))
			{
				return true;
			}
		}
		
		return false;
	}
	
	public static boolean groupHasPlayer(String groupname, String playername)
	{
		for (Group group : groups)
		{
			if (group.getGroupName().equals(groupname))
			{
				return group.hasPlayer(playername);
			}
		}
		
		
		return false;
	}
	
	@EventHandler
	public void playerJoin(PlayerJoinEvent event)
	{
		if (getGroup("DefaultGroup") == null)
		{
			plugin.getLogger().info("Your groups manager doesnt have a 'DefaultGroup' group, therefore, you cannot toggle a permission globally");
			return;
		}
		
		if (!getGroup("DefaultGroup").getPlayers().contains(event.getPlayer().getName()))
		{
			getGroup("DefaultGroup").addPlayer(event.getPlayer().getName());
		}
	}
}
