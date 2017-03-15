/*
 * Bukkit Plugin - SkyBasers
 * For use on The Justice Network
 * 
 * @author - Joseph Shufflebotham
 * 
 * © Copyright 2016 
 */
package me.JoeShuff.SkyBasers.WorldGen;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import me.JoeShuff.SkyBasers.SkyBasers;
import net.minecraft.server.v1_8_R3.NBTCompressedStreamTools;
import net.minecraft.server.v1_8_R3.NBTTagCompound;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

public class TerrainGen {

	public static class Schematic {
		
		private byte[] blocks;
		private byte[] data;
		
		private short width;
		private short length;
		private short height;
		
		public Schematic(byte[] blocks, byte[] data, short width, short length, short height)
		{
			this.setBlocks(blocks);
			this.setData(data);
			
			this.setWidth(width);
			this.setLength(length);
			this.setHeight(height);
		}

		public short getWidth() {
			return width;
		}

		public void setWidth(short width) {
			this.width = width;
		}

		public short getLength() {
			return length;
		}

		public void setLength(short length) {
			this.length = length;
		}

		public byte[] getBlocks() {
			return blocks;
		}

		public void setBlocks(byte[] blocks) {
			this.blocks = blocks;
		}

		public byte[] getData() {
			return data;
		}

		public void setData(byte[] data) {
			this.data = data;
		}

		public short getHeight() {
			return height;
		}

		public void setHeight(short height) {
			this.height = height;
		}
	}
	
	SkyBasers plugin;
	
	public TerrainGen(SkyBasers plugin)
	{
		this.plugin = plugin;
		
		generateTerrain();
	}
	
	public void generateTerrain()
	{
		schematic = pickTerrain();
		
		if (schematic == null)
		{
			System.err.println("ERROR FETCHING SCHEMATIC FILE");
		}
	}
	
	private Schematic schematic;
	
	private Schematic pickTerrain()
	{	
		int amountofMaps = plugin.getConfig().getInt("map-amount");
		
		int selectedMap = new Random().nextInt(amountofMaps) + 1;
		
		try {
			FileInputStream fis = new FileInputStream(plugin.getDataFolder() + "/maps/" + selectedMap + ".schematic");
			NBTTagCompound nbtdata = NBTCompressedStreamTools.a(fis);
           
            short width = nbtdata.getShort("Width");
            short height = nbtdata.getShort("Height");
            short length = nbtdata.getShort("Length");
 
            byte[] blocks = nbtdata.getByteArray("Blocks");
            byte[] data = nbtdata.getByteArray("Data");
           
            fis.close();
            
            return new Schematic(blocks, data, width, length, height);    
        } catch (Exception e) {
            e.printStackTrace();
        }
		
		return null;
	}
	
	private int level = 0;
	
	public Integer generateLevel()
	{	
		int startX = plugin.getConfig().getInt("mapCentreX") - 36;  //1000 + 35
		int startY = plugin.getConfig().getInt("mapCentreY") + 21;  //90 + 23
		int startZ = plugin.getConfig().getInt("mapCentreZ") - 36;  //1000 + 35

		for (int x = 0 ; x <= 71 ; x ++)
		{
			for (int z = 0 ; z <= 71 ; z ++)
			{
				int index = level * schematic.getWidth() * schematic.getLength() + z * schematic.getWidth() + x;
	            final Location l = new Location(Bukkit.getWorld(SkyBasers.worldName), x + startX, level + startY, z + startZ);
	            int b = schematic.getBlocks()[index] & 0xFF;
	            final Block block = l.getBlock();
	            
	            if (block.getType() != Material.BARRIER)
	            {
	            	block.setType(Material.getMaterial(b));
		            block.setData(schematic.getData()[index]);
	            }
			}
		}	
		
		
		level ++;
		return level;
	}
}
