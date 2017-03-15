/*
 * Bukkit Plugin - SkyBasers
 * For use on The Justice Network
 * 
 * @author - Joseph Shufflebotham
 * 
 * © Copyright 2016 
 */
package me.JoeShuff.SkyBasers.Scoreboards;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import me.JoeShuff.SkyBasers.SkyBasers;
import me.JoeShuff.SkyBasers.SkyBasers.GameState;
import me.JoeShuff.SkyBasers.SkyBasers.GameType;
import me.JoeShuff.SkyBasers.MySQL.MySQL;
import me.JoeShuff.SkyBasers.MySQL.SQLManager;
import me.JoeShuff.SkyBasers.MySQL.SQLManager.PlayerData;
import net.minecraft.server.v1_8_R3.EntityArmorStand;
import net.minecraft.server.v1_8_R3.EntityPlayer;
import net.minecraft.server.v1_8_R3.MinecraftServer;
import net.minecraft.server.v1_8_R3.NBTTagCompound;
import net.minecraft.server.v1_8_R3.PacketPlayOutEntityDestroy;
import net.minecraft.server.v1_8_R3.PacketPlayOutEntityMetadata;
import net.minecraft.server.v1_8_R3.PacketPlayOutSpawnEntityLiving;
import net.minecraft.server.v1_8_R3.PacketPlayOutUpdateEntityNBT;
import net.minecraft.server.v1_8_R3.WorldServer;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class LeaderBoardHolograms extends BukkitRunnable implements Listener  {

	private HashMap<String, Hologram> holograms = new HashMap<String, Hologram>(); 
	
	private List<String> currentValues = Arrays.asList("NSoloKills");
	
	private SkyBasers plugin;
	
	public LeaderBoardHolograms(SkyBasers plugin)
	{
		Bukkit.getServer().getPluginManager().registerEvents(this, plugin);
		
		this.plugin = plugin;
		
		for (Entity e : Bukkit.getWorld(SkyBasers.worldName).getEntities())
		{
			if (e instanceof ArmorStand)
			{
				if (e.getLocation().distance(plugin.getSpawnLoc()) < 50)
				{
					if (((ArmorStand) e).isMarker())
					{
						e.remove();
					}
				}
			}
		}
		
		if (plugin.getType() == GameType.NORMAL)
		{
			if (plugin.isTeams())
			{
				currentValues = Arrays.asList("NTeamKills");
			}
			else
			{
				currentValues = Arrays.asList("NSoloKills");
			}
		}
		else
		{
			if (plugin.isTeams())
			{
				currentValues = Arrays.asList("JTeamKills");
			}
			else
			{
				currentValues = Arrays.asList("JSoloKills");
			}
		}

		holograms.put("Title", new Hologram("Skybasers","" + ChatColor.AQUA + ChatColor.BOLD + "SKYBASERS" ,new Location(Bukkit.getWorld(SkyBasers.worldName),-8,23,0).add(0.5,0.6,0.5)));
		ChatColor color = ChatColor.BLUE;
		
		if (plugin.getType() == GameType.JUDGEMENT)
		{
			color = ChatColor.RED;
		}
		
		String teams = "SOLO";
		
		if (plugin.isTeams())
		{
			teams = "TEAMS";
		}
		
		holograms.put("Mode", new Hologram("Mode", "" + color + ChatColor.BOLD + plugin.getType().toString() + ChatColor.GOLD + ChatColor.BOLD + " " + teams,new Location(Bukkit.getWorld(SkyBasers.worldName),-8,23,0).add(0.5,0.3,0.5)));
		
		holograms.put("Top", new Hologram("Top", "" + ChatColor.BLUE + ChatColor.BOLD + "Lobby's " + ChatColor.GOLD + ChatColor.BOLD + "Top " + ChatColor.YELLOW + ChatColor.BOLD + "Killers", new Location(Bukkit.getWorld(SkyBasers.worldName),-8,23,0).add(0.5,0,0.5)));
		
		getTop3(true, currentValues);
		
		this.runTaskTimer(plugin, 200, 200);
	}

	private List<ChatColor> colors = Arrays.asList(ChatColor.GOLD, ChatColor.GRAY, ChatColor.RED);
	
	public void changeMode()
	{
		ChatColor color = ChatColor.BLUE;
		
		if (plugin.getType() == GameType.JUDGEMENT)
		{
			color = ChatColor.RED;
		}
		
		String teams = "SOLO";
		
		if (plugin.isTeams())
		{
			teams = "TEAMS";
		}
		
		holograms.get("Mode").setText("" + color + ChatColor.BOLD + plugin.getType().toString() + ChatColor.GOLD + ChatColor.BOLD + " " + teams);
		getTop3(false, currentValues);
		
		int currentPlayers = Bukkit.getOnlinePlayers().size();
		int maxPlayers = plugin.getConfig().getInt("max-players");
		
		String type = plugin.getType().toString();
		
		if (plugin.isTeams())
		{
			type = type + "_TEAMS";
		}
		else
		{
			type = type + "_SOLO";
		}
		
		int playersToRequest = maxPlayers - currentPlayers;
				
		if (playersToRequest == 0) return;
		
		SQLManager.sendServerRequest(Bukkit.getServerName(), playersToRequest, type, "true",false);
	}
	
	private void getTop3(Boolean init, List<String> values)
	{
		getTop3(init, values, null);
	}
	
	private void getTop3(Boolean init, List<String> values,Player p1)
	{
		int totalValue = 0;
		
		List<String> uids = new ArrayList<String>();
		
		for(Player p : Bukkit.getOnlinePlayers())
		{
			if (p1 != null)
			{
				if (!p1.getName().equals(p.getName()))
				{
					uids.add(p.getUniqueId().toString());
				}
			}
			else
			{
				uids.add(p.getUniqueId().toString());
			}	
		}
		
		HashMap<Integer, PlayerData> top3 = SQLManager.getTop(3, values, uids);
		
		if (MySQL.getConnection() == null)
		{
			return;
		}
		
		for (int i = 1 ; i <= 3 ; i ++)
		{
			if (!top3.containsKey(i))
			{
				if (holograms.containsKey(String.valueOf(i)))
				{
					holograms.get(String.valueOf(i)).removeEntity();
					holograms.remove(String.valueOf(i));
				}
				
				continue;
			}
			
			if (init)
			{
				OfflinePlayer player = Bukkit.getOfflinePlayer(UUID.fromString(top3.get(i).uuid));
				holograms.put(String.valueOf(i), new Hologram(String.valueOf(i),colors.get(i - 1) + player.getName() + " » " + top3.get(i).totalAmount,new Location(Bukkit.getWorld(SkyBasers.worldName),-8,23 - (i * 0.5),0).add(0.5, 0, 0.5)));
			}
			else
			{
				OfflinePlayer player = Bukkit.getOfflinePlayer(UUID.fromString(top3.get(i).uuid));
				if (holograms.containsKey(String.valueOf(i)))
				{
					holograms.get(String.valueOf(i)).setText(colors.get(i - 1) + player.getName() + " » " + top3.get(i).totalAmount);
				}
				else
				{
					holograms.put(String.valueOf(i), new Hologram(String.valueOf(i),colors.get(i - 1) + player.getName() + " » " + top3.get(i).totalAmount,new Location(Bukkit.getWorld(SkyBasers.worldName),-8,23 - (i * 0.5),0).add(0.5, 0, 0.5)));
				}
			}
		}	
		
		for (Player player : Bukkit.getOnlinePlayers())
		{
			updateOffChart(player);
		}
	}
	
	@Override
	public void run() {
		
		if (currentValues.get(0).contains("Kills"))
		{
			if (plugin.getType() == GameType.NORMAL)
			{
				if (plugin.isTeams())
				{
					currentValues = Arrays.asList("NTeamWins");
				}
				else
				{
					currentValues = Arrays.asList("NSoloWins");
				}
			}
			else
			{
				if (plugin.isTeams())
				{
					currentValues = Arrays.asList("JTeamWins");
				}
				else
				{
					currentValues = Arrays.asList("JSoloWins");
				}
			}

			holograms.get("Top").setText("" + ChatColor.BLUE + ChatColor.BOLD + "Lobby's " + ChatColor.GOLD + ChatColor.BOLD + "Top " + ChatColor.YELLOW + ChatColor.BOLD + "Winners");
		}
		else
		{
			if (plugin.getType() == GameType.NORMAL)
			{
				if (plugin.isTeams())
				{
					currentValues = Arrays.asList("NTeamKills");
				}
				else
				{
					currentValues = Arrays.asList("NSoloKills");
				}
			}
			else
			{
				if (plugin.isTeams())
				{
					currentValues = Arrays.asList("JTeamKills");
				}
				else
				{
					currentValues = Arrays.asList("JSoloKills");
				}
			}
			
			holograms.get("Top").setText("" + ChatColor.BLUE + ChatColor.BOLD + "Lobby's " + ChatColor.GOLD + ChatColor.BOLD + "Top " + ChatColor.YELLOW + ChatColor.BOLD + "Killers");
		}
		
		getTop3(false, currentValues);
	}
	
	@EventHandler
	public void playerJoin(PlayerJoinEvent event)
	{
		if (plugin.getState() == GameState.PREGAME)
		{
			getTop3(false, currentValues);
		}
	}
	
	public HashMap<String, Integer> id = new HashMap<String, Integer>();
	
	public void updateOffChart(Player player)
	{
		if (id.containsKey(player.getName()))
		{
			PacketPlayOutEntityDestroy packet = new PacketPlayOutEntityDestroy(id.get(player.getName()));
			((CraftPlayer)player).getHandle().playerConnection.sendPacket(packet);
		}
		
		List<Integer> values = SQLManager.getPlayerPos(player.getUniqueId().toString(), currentValues); 
		
		if (values == null)
		{
			return;
		}
		
		if (values.get(0) > 3)
		{
			WorldServer w = ((CraftWorld)player.getWorld()).getHandle();
			EntityArmorStand stand = new EntityArmorStand(w);
			
			Location l = new Location(Bukkit.getWorld(SkyBasers.worldName),-8,20,0).add(0.5, 0, 0.5);
			
			id.put(player.getName(), stand.getId());
			stand.setSmall(true);
			stand.setLocation(l.getX(), l.getY(), l.getZ(), 0f, 0f);
			stand.setCustomName(ChatColor.YELLOW + "You are " + values.get(0) + "th » " + values.get(1));
			stand.setCustomNameVisible(true);
			stand.setInvisible(true);
			stand.setGravity(false);
			
			PacketPlayOutSpawnEntityLiving packet = new PacketPlayOutSpawnEntityLiving(stand);
			((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);
		}
	}
	
	@EventHandler
	public void playerLeave(PlayerQuitEvent event)
	{
		if (plugin.getState() == GameState.PREGAME)
		{
			getTop3(false, currentValues, event.getPlayer());
		}
	}
	
	private class Hologram {

		private ArmorStand entity = null;
		private String text;
		private Location loc;
		
		private String ID;
		
		public Hologram(String ID, String text, Location l)
		{
			this.ID = ID;
			
			this.loc = l;
			World world = l.getWorld();
			
			world.loadChunk(-1, 0);
			
			this.entity = (ArmorStand) world.spawnEntity(l, EntityType.ARMOR_STAND);
			
			this.text = text;
			
			updateEntity();
			
		}
		
		private void updateEntity()
		{
			if (this.entity == null) return;
			
			this.entity.setVisible(false);
			this.entity.setCustomName(text);
			this.entity.setCustomNameVisible(true);
			this.entity.setGravity(false);
			this.entity.setMarker(true);
			this.entity.setSmall(true);
			this.entity.setCanPickupItems(false);
			
		}
		
		public void removeEntity()
		{
			this.entity.remove();
		}
		
		public String getID()
		{
			return this.ID;
		}
		
		public void setText(String text)
		{
			this.text = text;
			updateEntity();
		}
	}

	
	
}
