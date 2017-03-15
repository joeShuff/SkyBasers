/*
 * Bukkit Plugin - SkyBasers
 * For use on The Justice Network
 * 
 * @author - Joseph Shufflebotham
 * 
 * © Copyright 2016 
 */
package me.JoeShuff.SkyBasers.WorldGen;

import java.util.Random;

import me.JoeShuff.SkyBasers.SkyBasers;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;

public class CaveGen {

	SkyBasers plugin;
	
	public enum Direction {
		NORTH, EAST, SOUTH, WEST, NORTH_EAST, NORTH_WEST, SOUTH_EAST, SOUTH_WEST;
	}
	 
	public CaveGen(SkyBasers plugin)
	{
		this.plugin = plugin;
		nextCave();
		
		Random rnd = new Random();
		
		int selectorX = rnd.nextInt(71) - 35;
		int selectorY = rnd.nextInt(21) + 1;
		int selectorZ = rnd.nextInt(71) - 35;
		
		int worldX = plugin.getConfig().getInt("mapCentreX");
		int worldY = plugin.getConfig().getInt("mapCentreY");
		int worldZ = plugin.getConfig().getInt("mapCentreZ");
		
		boundaryX = worldX;
		boundaryY = worldY;
		boundaryZ = worldZ;
		
		change = 35;
		
		Loc = new Location(Bukkit.getWorld(SkyBasers.worldName), worldX + selectorX, worldY + selectorY, worldZ + selectorZ);
	}
	
	private Direction getDirection(Boolean Diagonal)
	{
		int selector;
		Random rnd = new Random();
		
		if (Diagonal == true)
		{
			selector = rnd.nextInt(4) + 1;
		}
		else
		{
			selector = rnd.nextInt(8) + 1;
		}
		
		switch (selector) {
			case 1 : return Direction.NORTH;
			case 2 : return Direction.EAST;
			case 3 : return Direction.SOUTH;
			case 4 : return Direction.WEST;
			
			case 5 : return Direction.NORTH_EAST;
			case 6 : return Direction.NORTH_WEST;
			case 7 : return Direction.SOUTH_EAST;
			case 8 : return Direction.SOUTH_WEST;		
		}
		
		return Direction.NORTH;
	}
	
	private int boundaryX;
	private int boundaryY;
	private int boundaryZ;
	
	private int change;
	
	public void nextCave()
	{
		Random rnd = new Random();
		
		int selector = rnd.nextInt(4);
		
		switch (selector){
			case 0 : nextCave = CaveType.JUNCTION;
					 break;
			default : nextCave = CaveType.TUNNEL;
			         break;
		}
	}
	
	private CaveType nextCave;
	
	private int turns = 0;
	
	public enum CaveType {
		TUNNEL, JUNCTION
	}
	
	Location Loc = null;
	
	public boolean generateSection()
	{
		turns ++;
		
		if (nextCave == CaveType.TUNNEL)
		{
			Random rnd = new Random();
			
			int tunnelLength = rnd.nextInt(7) + 8;
			
			Direction dir = getDirection(true);
			
			setLoc();
			
			for (int i = 0 ; i < tunnelLength ; i ++)
			{	
				Bukkit.getWorld(SkyBasers.worldName).createExplosion(Loc.getX(), Loc.getY(), Loc.getZ(), 6f, false, true);
				
				int yChange = rnd.nextInt(1) + 1;
				int change1 = rnd.nextInt(2) + 1;
				int change2 = rnd.nextInt(2) + 1;
				
				switch (dir) {
					case NORTH : Loc = Loc.add(0, yChange, -change1);
								 break;
					case EAST :  Loc = Loc.add(change1, yChange, 0);
					 			 break;
					case SOUTH : Loc = Loc.add(0, yChange, change1);
					 			 break;
					case WEST :  Loc = Loc.add(-change1, yChange, 0);
								 break;
								 
					case NORTH_EAST : Loc = Loc.add(change2, yChange, -change1);
									  break;
					case NORTH_WEST : Loc = Loc.add(-change2, yChange, -change1);
									  break;
					case SOUTH_EAST : Loc = Loc.add(change2, yChange, change1);
					 				  break;
					case SOUTH_WEST : Loc = Loc.add(-change2, yChange, change1);
					 				  break;
				}
			}	
		}
		else if (nextCave == CaveType.JUNCTION)
		{
			setLoc();
			
			Bukkit.getWorld(SkyBasers.worldName).createExplosion(Loc.getX(), Loc.getY(), Loc.getZ(), 10f, false, true);
		}
		
		nextCave();
		
		checkLoc();
		
		for (Entity e : Bukkit.getWorld(SkyBasers.worldName).getEntities())
		{
			if (e instanceof Player)
			{
				continue;
			}
			
			if (e instanceof Item  && (int) e.getLocation().distance(plugin.getCentreMap(false, 0)) <= 250)
			{
				e.remove();
			}
		}
		
		if (turns > 25)
		{
			Random rnd = new Random();
			int chance = rnd.nextInt(100) + 1;
			
			if (chance <= 10)
			{
				return true;
			}
			
		}
		
		return false;
	}
	
	private void checkLoc()
	{
		boolean valid = true;
		
		if (Loc.getX() < boundaryX - change || Loc.getX() > boundaryX + change)
		{
			valid = false;
		}
		
		if (Loc.getZ() < boundaryZ - change || Loc.getZ() > boundaryZ + change)
		{
			valid = false;
		}
		
		if (Loc.getY() < boundaryY || Loc.getY() > boundaryY + 21)
		{
			valid = false;
		}
		
		if (valid == false)
		{
			setLoc();
		}
	}
	
	private void setLoc()
	{
		Random rnd = new Random();
		
		int selectorX = rnd.nextInt(71) - 35;
		int selectorY = rnd.nextInt(21) + 1;
		int selectorZ = rnd.nextInt(71) - 35;
		
		int worldX = plugin.getConfig().getInt("mapCentreX");
		int worldY = plugin.getConfig().getInt("mapCentreY");
		int worldZ = plugin.getConfig().getInt("mapCentreZ");
		
		Loc = new Location(Bukkit.getWorld(SkyBasers.worldName), worldX + selectorX, worldY + selectorY, worldZ + selectorZ);
	}
}
