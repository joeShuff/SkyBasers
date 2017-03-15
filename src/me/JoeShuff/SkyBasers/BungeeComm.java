/*
 * Bukkit Plugin - SkyBasers
 * For use on The Justice Network
 * 
 * @author - Joseph Shufflebotham
 * 
 * © Copyright 2016 
 */
package me.JoeShuff.SkyBasers;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

public class BungeeComm {

	private static JavaPlugin plugin;
	
	public static void init(JavaPlugin plugin)
	{
		BungeeComm.plugin = plugin;
	}
	
	public static void sendToServer(Player player, String server)
	{
		sendToServer(player, player.getName(), server);
	}
	
	public static void sendToServer(Player player, String toSend, String server)
	{
		if (plugin == null)
		{
			System.err.println("Cannot send bungee message, plugin cannot be null");
			System.err.println("Make sure to call BungeeComm.init(plugin)");
			
			return;
		}
		
		if (player == null)
		{
			return;
		}
		
		ByteArrayDataOutput out = ByteStreams.newDataOutput();
		out.writeUTF("ConnectOther");
		out.writeUTF(toSend);
		out.writeUTF(server);

		player.sendPluginMessage(plugin, "BungeeCord", out.toByteArray());
	}
	
	public static void sendMessage(Player player, String receiver, String message)
	{
		if (plugin == null)
		{
			System.err.println("Cannot send bungee message, plugin cannot be null");
			System.err.println("Make sure to call BungeeComm.init(plugin)");
			
			return;
		}
		
		ByteArrayDataOutput out2 = ByteStreams.newDataOutput();
		out2.writeUTF("Message");
		out2.writeUTF(receiver);
		out2.writeUTF(message);
		
		player.sendPluginMessage(plugin, "BungeeCord", out2.toByteArray());
	}
}
