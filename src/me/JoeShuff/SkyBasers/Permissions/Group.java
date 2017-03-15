/*
 * Bukkit Plugin - SkyBasers
 * For use on The Justice Network
 * 
 * @author - Joseph Shufflebotham
 * 
 * © Copyright 2016 
 */
package me.JoeShuff.SkyBasers.Permissions;

import java.util.ArrayList;
import java.util.List;

import me.JoeShuff.SkyBasers.SkyBasers;

public class Group {

	private List<String> players = new ArrayList<String>();
	private String GroupName = "";
	private List<String> permissions = new ArrayList<String>();
	
	private SkyBasers plugin;
	
	public Group(String groupName, List<String> playerList, List<String> perms, SkyBasers plugin)
	{
		GroupName = groupName;
		
		for (String playername : playerList)
		{
			players.add(playername);
		}
		
		permissions = perms;
		
		this.plugin = plugin;
	}
	
	public boolean hasPermission(String permission)
	{
		return permissions.contains(permission);
	}
	
	public void addPlayer(String playername)
	{
		players.add(playername);
	}
	
	public void addPermission(String permission)
	{
		permissions.add(permission);
	}
	
	public String getGroupName()
	{
		return GroupName;
	}
	
	public List<String> getPlayers()
	{
		return players;
	}
	
	public boolean hasPlayer(String playername)
	{
		return players.contains(playername);
	}
}
