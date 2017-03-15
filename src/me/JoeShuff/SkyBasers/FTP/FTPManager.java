package me.JoeShuff.SkyBasers.FTP;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import javax.sound.sampled.Line;

import me.JoeShuff.SkyBasers.SkyBasers;

import org.bukkit.plugin.java.JavaPlugin;

public class FTPManager {
	
	public FTPClient client;
	
	public SkyBasers plugin;
	
	public FTPManager(SkyBasers plugin, String host, String username, String password) 
	{
		this.plugin = plugin;
		client = new FTPClient(host, username, password);
		System.out.println("Connecting to " + host + " with " + username + ":" + password);
	}

	public void downloadFileUpdates()
	{
		List<String> textFiles = Arrays.asList("kits.yml","chestLoot.yml","groups.yml","mobAmount.yml","MySQL.yml","rules.yml","spawnRates.yml");
		
		List<String> byteFiles = Arrays.asList("pod.schematic","teamPod.schematic");
		
		for (String file : textFiles)
		{
			if (client.testConnectionToFile("/config/SkyBasers/" + file))
			{
				client.downloadTextToLocalFile("/config/SkyBasers/" + file, new File(plugin.getDataFolder(), file).toPath().toString());
			}
		}
		
		for (String byteFile : byteFiles)
		{
			if (client.testConnectionToFile("/config/SkyBasers/" + byteFile))
			{
				client.downloadBytesToLocalFile("/config/SkyBasers/" + byteFile, new File(plugin.getDataFolder(), byteFile).toPath().toString());
			}
		}
		
		int maps = 0;
		
		List<String> lines =  client.getFileStrings("/config/SkyBasers/config.yml");
		
		if (lines == null)
		{
			return;
		}
		
		for (String line : lines)
		{
			if (line.contains("map-amount"))
			{
				line = line.replace("map-amount :", "");
				line = line.trim();
				
				try {maps = Integer.valueOf(line);} catch (Exception e){}
			}
		}
		
		if (maps != 0)
		{
			plugin.getConfig().set("map-amount", maps);
			plugin.saveConfig();
		}
		
		for (int i = 1 ; i <= maps ; i ++)
		{
			if (client.testConnectionToFile("/config/SkyBasers/Maps/" + i + ".schematic"))
			{
				client.downloadBytesToLocalFile("/config/SkyBasers/Maps/" + i + ".schematic", new File(plugin.getDataFolder(), "/maps/" + i + ".schematic").toPath().toString());
			}
		}
		
		
	}
}
