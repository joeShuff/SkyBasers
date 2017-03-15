/*
 * Bukkit Plugin - SkyBasers
 * For use on The Justice Network
 * 
 * @author - Joseph Shufflebotham
 * 
 * © Copyright 2016 
 */
package me.JoeShuff.SkyBasers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

public class EditTabCompleter implements TabCompleter 
{
	private SkyBasers plugin;
	
	public EditTabCompleter(SkyBasers plugin) {
		this.plugin = plugin;
	}
	
	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) 
	{
		if (cmd.getName().equalsIgnoreCase("editmode") && args.length >= 1)
		{
			if (sender instanceof Player)
			{
				Player player = (Player) sender;
				
				List<String> list = Arrays.asList("teams","mode","kits","game-time");
				
				List<String> newList = new ArrayList<String>();
				
				for (String name : list)
				{
					if (name.contains(args[0]))
					{
						newList.add(name);
					}
				}
				
				return newList;
			}
		}
		return null;
	}

}
