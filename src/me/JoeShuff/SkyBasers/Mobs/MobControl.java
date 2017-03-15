/*
 * Bukkit Plugin - SkyBasers
 * For use on The Justice Network
 * 
 * @author - Joseph Shufflebotham
 * 
 * © Copyright 2016 
 */
package me.JoeShuff.SkyBasers.Mobs;

import java.util.HashMap;
import java.util.List;

import me.JoeShuff.SkyBasers.SkyBasers;
import me.JoeShuff.SkyBasers.SkyBasers.GameState;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class MobControl extends BukkitRunnable implements Listener  {

	private class MobData
	{
		private EntityType type;
		
		private int maxAmount;
		private int minRadius;
		
		private int currentAmount;
		
		public MobData(EntityType type, int amount, int radius)
		{
			this.type = type;
			
			this.maxAmount = amount;
			
			this.minRadius = radius;
			
			currentAmount = 0;
			
			update();
		}
		
		public void update()
		{
			currentAmount = 0;
			
			World w = Bukkit.getWorld(SkyBasers.worldName);
			
			for (Entity e : w.getEntities())
			{
				if (e.getType() == this.type)
				{
					currentAmount ++;
				}
			}
		}
		
		public boolean canSpawn(Location l)
		{
			update();
			
			if (currentAmount >= maxAmount)
			{
				return false;
			}
			
			World w = Bukkit.getWorld(SkyBasers.worldName);
			
			for (Entity e : w.getEntities())
			{
				if (e.getType() == type)
				{
					int distance = (int) e.getLocation().distance(l);
					
					if (distance <= minRadius)
					{
						return false;
					}
				}
			}
			
			return true;
		}
		
		public void clear()
		{
			currentAmount = 0;
			
			World w = Bukkit.getWorld(SkyBasers.worldName);
			
			for (Entity e : w.getEntities())
			{
				if (e.getType() == this.type)
				{
					e.remove();
				}
			}
		}
		
		
	}
	
	private SkyBasers plugin;
	
	private HashMap<EntityType, MobData> mobInfo = new HashMap<EntityType, MobData>();
	
	public MobControl(SkyBasers plugin)
	{
		mobInfo = new HashMap<EntityType, MobData>();
		
		this.plugin = plugin;
		
		initAmounts();
		
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}
	
	private void initAmounts()
	{
		FileConfiguration amountFile = plugin.getMobAmountData();
		
		List<String> mobs = amountFile.getStringList("mobs");
		
		String gameType = plugin.getType().toString().toLowerCase();
		
		for (String mob : mobs)
		{
			int max = amountFile.getInt(gameType + "." + mob + ".max");
			int radius = amountFile.getInt(gameType + "." + mob + ".radius");
			
			EntityType mobType = null;
			
			try {mobType = EntityType.valueOf(mob.toUpperCase());} catch (Exception e){}
			
			if (mobType == null)
			{
				System.err.println("Unable to find mob of type : " + mob);
			}
			else
			{
				MobData data = new MobData(mobType, max, radius);
				mobInfo.put(mobType, data);
			}
		}
	}
	
	
	@EventHandler
	public void mobSpawn(CreatureSpawnEvent event)
	{
		if (mobInfo.containsKey(event.getEntityType()))
		{
			if (!mobInfo.get(event.getEntityType()).canSpawn(event.getLocation()))
			{
				event.setCancelled(true);
			}
		}
	}

	@Override
	public void run() {
		
		if (plugin.getState() == GameState.FINISHED)
		{
			this.cancel();
			HandlerList.unregisterAll(this);
			mobInfo = new HashMap<EntityType, MobData>();
		}
	}
	
	
}
