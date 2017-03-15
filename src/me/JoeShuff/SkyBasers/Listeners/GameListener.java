/*
 * Bukkit Plugin - SkyBasers
 * For use on The Justice Network
 * 
 * @author - Joseph Shufflebotham
 * 
 * © Copyright 2016 
 */
package me.JoeShuff.SkyBasers.Listeners;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import me.JoeShuff.SkyBasers.BungeeComm;
import me.JoeShuff.SkyBasers.RankSystemBukkit;
import me.JoeShuff.SkyBasers.SkyBasers;
import me.JoeShuff.SkyBasers.SkyBasers.GameStage;
import me.JoeShuff.SkyBasers.SkyBasers.GameState;
import me.JoeShuff.SkyBasers.VisualEffects;
import me.JoeShuff.SkyBasers.Items.HubItem;
import me.JoeShuff.SkyBasers.Items.SpectatorCompass;
import me.JoeShuff.SkyBasers.Items.SpectatorOptions;
import me.JoeShuff.SkyBasers.MySQL.SQLManager;
import me.JoeShuff.SkyBasers.Scoreboards.TeamManager;
import me.JoeShuff.SkyBasers.Scoreboards.TeamManager.Team;
import me.JoeShuff.SkyBasers.Timers.GameTimer;
import me.JoeShuff.SkyBasers.Timers.LavaTimer;
import me.JoeShuff.SkyBasers.WorldGen.WeightedItemGen;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Blaze;
import org.bukkit.entity.CaveSpider;
import org.bukkit.entity.Chicken;
import org.bukkit.entity.Cow;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Egg;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Enderman;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Ghast;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Pig;
import org.bukkit.entity.PigZombie;
import org.bukkit.entity.Player;
import org.bukkit.entity.Rabbit;
import org.bukkit.entity.Sheep;
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.Snowball;
import org.bukkit.entity.Spider;
import org.bukkit.entity.Villager;
import org.bukkit.entity.Wither;
import org.bukkit.entity.WitherSkull;
import org.bukkit.entity.Zombie;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityInteractEvent;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerVelocityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class GameListener extends BukkitRunnable implements Listener
{
	SkyBasers plugin;		
	
	public GameListener(SkyBasers plugin)
	{
		Bukkit.getServer().getPluginManager().registerEvents(this, plugin);
		this.plugin = plugin;
		
		this.runTaskTimer(plugin, 20, 20);
	}
	
	public class Coord
	{
		public int x;
		public int z;
		
		public Coord(int x, int z)
		{
			this.x = x;
			this.z = z;
		}
		
		public boolean is(int x, int z)
		{
			if (x == this.x && z == this.z)
			{
				return true;
			}
			
			if (Math.abs(x - this.x) == 1 || Math.abs(z - this.z) == 1)
			{
				return true;
			}
			
			return false;
		}
	}
	
	HashMap<Coord, String> anvilDroppers = new HashMap<Coord, String>();
	
	public void playerDeath(OfflinePlayer killed, DamageCause cause) 
	{
		plugin.getPlayerScoreboards(false).playerDeath(killed);

		if (damagers.containsKey(killed.getName()))
		{
			OfflinePlayer nkiller = Bukkit.getOfflinePlayer(damagers.get(killed.getName()).player);
				
			Bukkit.getServer().broadcastMessage(ChatColor.GOLD + killed.getName() + ChatColor.YELLOW + " has fallen to " + ChatColor.RED + nkiller.getName());
				
			if (damagers.get(killed.getName()).isValidPlayer())
			{
				SQLManager.playerKill(nkiller,killed);
			}
		}
		else
		{
			if (cause == DamageCause.LAVA || cause == DamageCause.FIRE_TICK || cause == DamageCause.FIRE)
			{
				Bukkit.getServer().broadcastMessage(ChatColor.GOLD + killed.getName() + ChatColor.YELLOW + " has fallen to lava");
			}
			else if (cause == DamageCause.CUSTOM)
			{
				Bukkit.getServer().broadcastMessage(ChatColor.GOLD + killed.getName() + ChatColor.YELLOW + " has died whilst disconnected");
			}
			else
			{
				Bukkit.getServer().broadcastMessage(ChatColor.GOLD + killed.getName() + ChatColor.YELLOW + " has fallen to nature");
			}
				
			SQLManager.playerDeath(killed);
		}
	
		plugin.getPlayerScoreboards(false).death(killed, null);
		
		SQLManager.removeActive(killed);
		
		testGameOver();
	}
	
	@EventHandler
	public void playerVelo(PlayerVelocityEvent event)
	{
		if (plugin.getStage() == GameStage.LAVA)
		{
			if (plugin.getPlayerScoreboards(false).hasAlivePlayer(event.getPlayer()))
			{
				event.setVelocity(new Vector());
			}
		}
	}
	
	public void playerDeath(Player killed, DamageCause cause)
	{
		OfflinePlayer killer = null;
		
		killed.setGameMode(GameMode.CREATIVE);
		VisualEffects.sendTitle(killed, "" + ChatColor.RED + ChatColor.BOLD + "You Died!", ChatColor.GOLD + "Better luck next time!");
		
		SQLManager.removeActive(killed);

		plugin.getPlayerScoreboards(false).joinTeam(killed, "dead");
		
		for (Player player : Bukkit.getOnlinePlayers())
		{
			if (player.getGameMode() == GameMode.CREATIVE)
			{
				killed.showPlayer(player);
			}
			else if (player.getGameMode() == GameMode.SURVIVAL)
			{
				player.hidePlayer(killed);
			}
		}
		
		for (ItemStack item : killed.getInventory().getContents())
		{
			if (item == null)
			{
				continue;
			}
			
			killed.getWorld().dropItemNaturally(killed.getLocation(), item);
		}
		
		try {killed.getWorld().dropItemNaturally(killed.getLocation(),killed.getInventory().getHelmet());} catch(Exception e) {}
		try {killed.getWorld().dropItemNaturally(killed.getLocation(),killed.getInventory().getChestplate());} catch(Exception e) {}
		try {killed.getWorld().dropItemNaturally(killed.getLocation(),killed.getInventory().getLeggings());} catch(Exception e) {}
		try {killed.getWorld().dropItemNaturally(killed.getLocation(),killed.getInventory().getBoots());} catch(Exception e) {}
		
		killed.getInventory().clear();
		killed.getInventory().setHelmet(null);
		killed.getInventory().setChestplate(null);
		killed.getInventory().setLeggings(null);
		killed.getInventory().setBoots(null);
		
		SpectatorCompass.getCompass(killed);
		SpectatorOptions.getBlaze(killed);
		HubItem.getItem(killed, Material.SUGAR);
		
		if (killed.getKiller() != null)
		{
			if (killed.getKiller() instanceof Player)
			{
				killer = (Player) killed.getKiller();
			}
		}
		
		plugin.getPlayerScoreboards(false).playerDeath(killed);
		
		String gameType = plugin.getType().toString();
		
		if (plugin.isTeams())
		{
			gameType = gameType + "_TEAMS";
		}
		else
		{
			gameType = gameType + "_SOLO";
		}
		
		String deathMessage = "{'text':'" + ChatColor.RED + "You have died! " + ChatColor.AQUA + ChatColor.BOLD + "Click here to join another game!" + "','color':'dark_green','clickEvent':{'action':'run_command','value':'/sbjoin " + gameType + " true'},'hoverEvent':{'action':'show_text','value':{'text':'','extra':[{'text':'Click to join another game','color':'gold'}]}}}";
		VisualEffects.sendJSONMessage(killed, deathMessage);
		
		if (killer == null)
		{
			if (damagers.containsKey(killed.getName()) && cause != DamageCause.FALLING_BLOCK)
			{
				OfflinePlayer nkiller = Bukkit.getOfflinePlayer(damagers.get(killed.getName()).player);
				
				killer = nkiller;
				
				Bukkit.getServer().broadcastMessage(ChatColor.GOLD + killed.getName() + ChatColor.YELLOW + " has fallen to " + ChatColor.RED + nkiller.getName());
				
				if (damagers.get(killed.getName()).isValidPlayer())
				{
					SQLManager.playerKill(nkiller,killed);
				}
			}
			else
			{
				if (cause == DamageCause.LAVA || cause == DamageCause.FIRE_TICK || cause == DamageCause.FIRE)
				{
					Bukkit.getServer().broadcastMessage(ChatColor.GOLD + killed.getName() + ChatColor.YELLOW + " has fallen to lava");
				}
				else if (cause == DamageCause.CUSTOM)
				{
					Bukkit.getServer().broadcastMessage(ChatColor.GOLD + killed.getName() + ChatColor.YELLOW + " has died while disconnected");
				}
				else if (cause == DamageCause.FALLING_BLOCK)
				{
					Location deathloc = killed.getLocation();
					
					int x = (int) deathloc.getX();
					int z = (int) deathloc.getZ();
					
					String Akiller = "";
					
					for (Coord coord : anvilDroppers.keySet())
					{
						if (coord.is(x,z))
						{
							Akiller = anvilDroppers.get(coord);
							anvilDroppers.remove(coord);
						}
					}
					
					killer = Bukkit.getOfflinePlayer(Akiller);
					
					Bukkit.getServer().broadcastMessage(ChatColor.GOLD + killed.getName() + ChatColor.YELLOW + " was flattened by " + ChatColor.RED + Akiller + "'s " + ChatColor.YELLOW + "anvil");
				
					SQLManager.playerKill(killer,killed);
				}
				else 
				{
					Bukkit.getServer().broadcastMessage(ChatColor.GOLD + killed.getName() + ChatColor.YELLOW + " has fallen to nature");
				}
				
				SQLManager.playerDeath(killed);
			}
		}
		else
		{
			Bukkit.getServer().broadcastMessage(ChatColor.GOLD + killed.getName() + ChatColor.YELLOW + " has fallen to " + ChatColor.RED + killer.getName());
		
			SQLManager.playerKill(killer,killed);
		}	
		
		int y = 0;
		if (plugin.getStage() != GameStage.GATHER)
		{
			if ((LavaTimer.height + 10) < killed.getWorld().getHighestBlockYAt(killed.getLocation()))
			{
				y = killed.getWorld().getHighestBlockYAt(killed.getLocation()) + 10;
			}
			else
			{
				y = LavaTimer.height + 10;
			}
		}
		else
		{
			y = killed.getWorld().getHighestBlockYAt(killed.getLocation()) + 10;
		}

		Location l = killed.getLocation();
		l.setY(y);
		killed.teleport(l);

		plugin.getPlayerScoreboards(false).death(killed, killer);
		
		testGameOver();
	}
	
	@EventHandler
	public void playerInt(PlayerInteractEvent event)
	{
		if (event.getPlayer().getGameMode() == GameMode.CREATIVE)
		{
			event.setCancelled(true);
		}
	}
	
	@EventHandler(priority = EventPriority.LOW)
	public void playerDeath(EntityDamageEvent event)
	{	
		if (event.getCause() == DamageCause.PROJECTILE || event.getCause() == DamageCause.ENTITY_ATTACK)
		{
			return;
		}
		
		if (plugin.getState() != GameState.LIVE)
		{
			return;
		}
		
		if (GameTimer.timePassed <= 10 && event.getEntity() instanceof Player)
		{
			event.setCancelled(true);
			return;
		}
		
		Player killed;
		
		if (event.getEntity() instanceof Player)
		{
			killed = (Player) event.getEntity();
		}
		else
		{
			return;
		}

		Player killer = null;
		
		try
		{
			if (killed.getHealth() - event.getDamage() < 1)
			{
				event.setCancelled(true);
				
				playerDeath(killed, event.getCause());
			}
		}
		catch (Exception e)
		{
			System.err.println("ERROR WITH EntityDamageEvent to kill");
			e.printStackTrace();
			playerDeath(killed, DamageCause.LAVA);
		}
	}
	
	@EventHandler
	public void itemSpawn(ItemSpawnEvent event)
	{
		if (plugin.getState() != GameState.LIVE)
		{
			return;
		}
		
		Material mat = event.getEntity().getItemStack().getType();
		
		if (mat == Material.IRON_ORE)
		{
			event.getEntity().getItemStack().setType(Material.IRON_INGOT);
			event.getEntity().getItemStack().setAmount(new Random().nextInt(3) + 1);
		}
		else if (mat == Material.GOLD_ORE)
		{
			event.getEntity().getItemStack().setType(Material.GOLD_INGOT);
			event.getEntity().getItemStack().setAmount(new Random().nextInt(3) + 1);
		}
	}
	
	public void testGameOver()
	{
		if (plugin.isTeams() == false)
		{
			int playersLeft = 0;
			
			Player finalPlayer = null;
			
			for (Player player : Bukkit.getOnlinePlayers())
			{
				if (plugin.getPlayerScoreboards(false).hasAlivePlayer(player))
				{
					playersLeft ++;
					finalPlayer = player;
				}
			}
			
			if (playersLeft != 1)
			{
				return;
			}
			else
			{
				plugin.setState(GameState.FINISHED);
				SQLManager.sendServerRequest(Bukkit.getServerName(), plugin.getConfig().getInt("max-players"), "null", "false", true);
				
				finalPlayer.setGameMode(GameMode.CREATIVE);

				HandlerList.unregisterAll(this);
				
				List<OfflinePlayer> winner = Arrays.asList(Bukkit.getOfflinePlayer(finalPlayer.getUniqueId()));

				plugin.setWinner(winner);
				
				SQLManager.win(winner);
				
				for (Player player : Bukkit.getOnlinePlayers())
				{
					if (this.plugin.getWinner().contains(((OfflinePlayer) player)))
					{
						VisualEffects.sendTitle(player, "" + ChatColor.GREEN + ChatColor.BOLD + "YOU WIN", "" + ChatColor.GOLD + ChatColor.BOLD + "Congratulations!");
					}
					else
					{
						if (plugin.getPlayerScoreboards(false).hasPlayerPlayed(player))
						{
							VisualEffects.sendTitle(player, "" + ChatColor.RED + ChatColor.BOLD + "YOU LOST", "" + ChatColor.GOLD + ChatColor.BOLD + "Better Luck Next Time!");
						}
						else
						{
							VisualEffects.sendTitle(player, "" + ChatColor.RED + ChatColor.BOLD + "GAME OVER", "" + ChatColor.GOLD + ChatColor.BOLD + "Join a game to play for yourself!");
						}
					}
					
					finalPlayer.showPlayer(player);
					player.showPlayer(finalPlayer);
				}
			}
		}
		else
		{
			List<Team> aliveTeams = new ArrayList<Team>();
			
			Player finalPlayer = null;
			
			for (Player player : Bukkit.getOnlinePlayers())
			{
				if (!plugin.getPlayerScoreboards(false).hasAlivePlayer(player))
				{
					continue;
				}
				
				TeamManager teamManager = plugin.getTeamManager();
				
				Team team = teamManager.getPlayerTeam(player);
				
				if (team != null)
				{
					if (!aliveTeams.contains(team))
					{
						aliveTeams.add(team);
						
						finalPlayer = player;
					}
				}
			}
			
			plugin.getPlayerScoreboards(false).setTeams(aliveTeams.size());
			
			if (aliveTeams.size() == 1)
			{	
				plugin.setState(GameState.FINISHED);
				SQLManager.sendServerRequest(Bukkit.getServerName(), plugin.getConfig().getInt("max-players"), "null", "false", true);
				
				finalPlayer.setGameMode(GameMode.CREATIVE);
				
				HandlerList.unregisterAll(this);
				
				Team team = aliveTeams.get(0);
				
				if (team == null)
				{
					return;
				}
				
				List<OfflinePlayer> winners = new ArrayList<OfflinePlayer>();
				
				for (String p : team.getPlayers())
				{
					winners.add(Bukkit.getOfflinePlayer(p));
				}
				
				plugin.setWinner(winners);
				
				for (Player player : Bukkit.getOnlinePlayers())
				{
					if (this.plugin.getWinner().contains(((OfflinePlayer) player)))
					{
						VisualEffects.sendTitle(player, "" + ChatColor.GREEN + ChatColor.BOLD + "YOU WIN", "" + ChatColor.GOLD + ChatColor.BOLD + "Congratulations!");
					}
					else
					{
						VisualEffects.sendTitle(player, "" + ChatColor.RED + ChatColor.BOLD + "YOU LOST", "" + ChatColor.GOLD + ChatColor.BOLD + "Better Luck Next Time!");
					}
					
					for (Player p : Bukkit.getOnlinePlayers())
					{
						p.showPlayer(player);
						player.showPlayer(p);	
					}
					
					player.setGameMode(GameMode.CREATIVE);
				}
				
				SQLManager.win(winners);
			}
			
		}

	}
	
	@EventHandler
	public void placeBlock(BlockPlaceEvent event)
	{
		if (plugin.getState() != GameState.LIVE)
		{
			if (plugin.getState() != GameState.FINISHED)
			{
				return;
			}
		}
		
		if (event.getBlockAgainst().getType() == Material.BARRIER)
		{
			event.getPlayer().sendMessage(ChatColor.RED + "You cannot place blocks on barriers!");
			event.setCancelled(true);
			return;
		}
		
		if (event.getBlock().getLocation().getY() >= 253)
		{
			event.getPlayer().sendMessage(ChatColor.RED + "You cannot place blocks that high");
			event.setCancelled(true);
		}
		
		if (event.getBlock().getType() == Material.ANVIL)
		{
			Location loc = event.getBlock().getLocation();
			
			int x = (int) loc.getX();
			int z = (int) loc.getZ();
			
			boolean found = false;
			
			for (Coord coord : anvilDroppers.keySet())
			{
				if (coord.is(x, z))
				{
					found = true;
				}
			}
			
			if (found == false)
			{
				anvilDroppers.put(new Coord(x,z),event.getPlayer().getName());
			}
		}
	}
	
	WeightedItemGen itemGenerator;
	
	@EventHandler
	public void entityDeath(EntityDeathEvent event)
	{
		/*
		 * Event handler to make all animals drop cooked versions of their meat
		 */
		
		if (plugin.getState() != GameState.LIVE)
		{
			return;
		}
		
		if (event.getEntity() instanceof Player) {
            return;
        }

        if (event.getEntity() instanceof Cow) 
        {
            event.getDrops().clear();
            event.getDrops().add(new ItemStack(Material.COOKED_BEEF, 3));
            event.getDrops().add(new ItemStack(Material.LEATHER, 1));
        } else if (event.getEntity() instanceof Pig) 
        {
            event.getDrops().clear();
            event.getDrops().add(new ItemStack(Material.GRILLED_PORK, 3));
        } else if (event.getEntity() instanceof Chicken) 
        {
            event.getDrops().clear();
            event.getDrops().add(new ItemStack(Material.COOKED_CHICKEN, 3));
            event.getDrops().add(new ItemStack(Material.FEATHER, 2));
        } else if (event.getEntity() instanceof Rabbit)
        {
        	event.getDrops().clear();
        	event.getDrops().add(new ItemStack(Material.COOKED_RABBIT,3));
        } else if (event.getEntity() instanceof Sheep)
        {
        	event.getDrops().clear();
        	event.getDrops().add(new ItemStack(Material.COOKED_MUTTON,3));
        } else if (event.getEntity() instanceof Villager) 
        {
            if (new Random().nextInt(99) < 50) {
                event.getDrops().clear();
                event.getDrops().add(new ItemStack(Material.BOOK, 1));
            }
        } else if (event.getEntity() instanceof Horse) 
        {
            event.getDrops().clear();
            event.getDrops().add(new ItemStack(Material.LEATHER, 2));
        } else if (event.getEntity() instanceof PigZombie) 
        {
            event.getDrops().clear();
            event.getDrops().add(new ItemStack(Material.GOLD_NUGGET, 1));
            event.getDrops().add(new ItemStack(Material.ROTTEN_FLESH, 1));
        } else if (event.getEntity() instanceof Spider || event.getEntity() instanceof CaveSpider) 
        {
            event.getDrops().clear();
            
            if (new Random().nextInt(99) < 25)
            {
            	event.getDrops().add(new ItemStack(Material.STRING, new Random().nextInt(1) + 1));
            }
            
        } else if (event.getEntity() instanceof Zombie) 
        {
            event.getDrops().clear();
            event.getDrops().add(new ItemStack(Material.ROTTEN_FLESH, 2));
        } else if (event.getEntity() instanceof Skeleton) 
        {
            event.getDrops().clear();
            event.getDrops().add(new ItemStack(Material.ARROW, 2));
            event.getDrops().add(new ItemStack(Material.BONE, 1));
        } else if (event.getEntity() instanceof Creeper) 
        {
            event.getDrops().clear();
            event.getDrops().add(new ItemStack(Material.SULPHUR, 2));
        } else if (event.getEntity() instanceof Ghast)
        {
        	event.getDrops().clear();
        	
        	Ghast g = (Ghast) event.getEntity().getWorld().spawnEntity(event.getEntity().getLocation().add(new Random().nextInt(5) - 2, 0, new Random().nextInt(5) - 2), EntityType.GHAST);
        	plugin.getBosses().addInstanceGhast(g);
        	
        	Ghast g2 = (Ghast) event.getEntity().getWorld().spawnEntity(event.getEntity().getLocation().add(new Random().nextInt(5) - 2, 0, new Random().nextInt(5) - 2), EntityType.GHAST);
        	plugin.getBosses().addInstanceGhast(g2);
        } else if (event.getEntity() instanceof Blaze)
        {
        	event.getDrops().clear();
        	
        	Blaze b = (Blaze) event.getEntity().getWorld().spawnEntity(event.getEntity().getLocation().add(new Random().nextInt(5) - 2, 0, new Random().nextInt(5) - 2), EntityType.BLAZE);
        	plugin.getBosses().addInstanceBlaze(b);
        	
        	Blaze b2 = (Blaze) event.getEntity().getWorld().spawnEntity(event.getEntity().getLocation().add(new Random().nextInt(5) - 2, 0, new Random().nextInt(5) - 2), EntityType.BLAZE);
        	plugin.getBosses().addInstanceBlaze(b2);
        } else if (event.getEntity() instanceof EnderDragon)
        {       	
        	Player killer = event.getEntity().getKiller();
        	
        	if (killer != null)
        	{
        		killer.sendMessage("" + ChatColor.GOLD + ChatColor.MAGIC + "asdasas" + ChatColor.RESET + ChatColor.RED + "CONGRATULATIONS!!" + ChatColor.GOLD + ChatColor.MAGIC + "asdasas");
        		BungeeComm.sendMessage(killer, killer.getName(), ChatColor.GREEN + "+ 100 Justice Coints (Killing Ender Dragon)");
        		SQLManager.addJusticeCoin(killer, 100);
        	}
        	
        	for (int i = 0 ; i <= 10 ; i ++)
        	{
        		VisualEffects.playRandomFirework(event.getEntity().getLocation());
        	}
        	
        	event.getEntity().remove();
        }
	}
	
	public enum ChatChannel {
		GLOBAL, TEAM
	}
	
	HashMap<String, ChatChannel> channels = new HashMap<String, ChatChannel>();
	
	public void defaultChannels()
	{
		ChatChannel defaultChannel = ChatChannel.GLOBAL;
		
		if (plugin.isTeams())
		{
			defaultChannel = ChatChannel.TEAM;
		}
		
		for (Player player : Bukkit.getOnlinePlayers())
		{
			channels.put(player.getName(), defaultChannel);
		}
	}
	
	public void setChannel(Player player, ChatChannel Channel)
	{
		channels.put(player.getName(), Channel);
	}
	
	@EventHandler
	public void onChat(PlayerChatEvent event)
	{
		if (plugin.getState() != GameState.LIVE)
		{
			return;
		}
		
		if (!plugin.getPlayerScoreboards(false).hasAlivePlayer(event.getPlayer()))
		{
			event.setCancelled(true);
			
			for (Player player : Bukkit.getOnlinePlayers())
			{
				if (!plugin.getPlayerScoreboards(false).hasAlivePlayer(player))
				{
					player.sendMessage(ChatColor.GRAY + "[SPECTATOR] " + RankSystemBukkit.getMessage(event.getPlayer(), event.getMessage(), false));
				}
			}
			
			return;
		}
		
		if (channels.containsKey(event.getPlayer().getName()))
		{
			ChatChannel channel = channels.get(event.getPlayer().getName());
			
			if (!plugin.isTeams())
			{
				for (Player player : Bukkit.getOnlinePlayers())
				{
					player.sendMessage(RankSystemBukkit.getMessage(event.getPlayer(), event.getMessage(), false));
				}
				
				event.setCancelled(true);
				return;
			}
			
			if (channel == ChatChannel.GLOBAL)
			{	
				for (Player player : Bukkit.getOnlinePlayers())
				{
					String prefix = plugin.getTeamManager().getPlayerTeam(event.getPlayer()).getPrefix();
					player.sendMessage(ChatColor.GOLD + "[" + ChatColor.YELLOW + "GLOBAL" + ChatColor.GOLD + "] " + RankSystemBukkit.getMessage(event.getPlayer(), event.getMessage(), false));
				}
				
				event.setCancelled(true);
				return;
			}
			else if (channel == ChatChannel.TEAM)
			{
				for (Player player : Bukkit.getOnlinePlayers())
				{
					if (plugin.getTeamManager().getPlayerTeam(event.getPlayer().getName()).hasPlayer(player))
					{
						String prefix = plugin.getTeamManager().getPlayerTeam(event.getPlayer()).getPrefix();
						player.sendMessage(ChatColor.BLUE + "[" + ChatColor.AQUA + "TEAM" + ChatColor.BLUE + "] " + RankSystemBukkit.getMessage(event.getPlayer(), event.getMessage(), false));
					}
				}
				
				event.setCancelled(true);
				return;
			}
		}
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void entityDamagebyEnt(EntityDamageByEntityEvent event)
	{
		if (plugin.getState() != GameState.LIVE)
		{
			return;
		}
		
		try
		{
			if (event.getEntity() instanceof Player)
			{
				if (event.getDamager() instanceof Player)
				{
					Player damaged = (Player) event.getEntity();
					Player damager = (Player) event.getDamager();
					
					if (plugin.getStage() == GameStage.GATHER)
					{
						event.setCancelled(true);
						event.getDamager().sendMessage(ChatColor.RED + "You cannot damage players before the lava rises!");
					}
					else
					{
						if (damaged.getGameMode() == GameMode.CREATIVE || damager.getGameMode() == GameMode.CREATIVE)
						{
							return;
						}
						
						if (plugin.isTeams())
						{
							if (plugin.getTeamManager().getPlayerTeam(damaged.getName()).hasPlayer(damager.getName()))
							{
								event.setCancelled(true);
								event.getDamager().sendMessage(ChatColor.RED + "You cannot damage teammates");
								return;
							}
						}
						
						if (damagers.containsKey(damaged.getName()))
						{
							Damager damage = damagers.get(damaged.getName());
							if (!damage.player.equals(damager.getName()))
							{
								damage.setDamager(damager.getName());
								damage.setValid(true);
							}
						}
						else
						{
							damagers.put(damaged.getName(), new Damager(damager.getName()));
						}
						
						if (damaged.getHealth() - event.getDamage() <= 0)
						{
							event.setCancelled(true);
							playerDeath(damaged, event.getCause());
						}
					}
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
			System.out.println("Error in entityDamageByEntity");
		}
		
		try
		{
			if (event.getEntity() instanceof Player)
			{
				if (event.getDamager() instanceof Arrow)
				{
					Player damaged = (Player) event.getEntity();
					Arrow arrow = (Arrow) event.getDamager();
					
					if (arrow.getShooter() instanceof Player)
					{
						Player damager = (Player) arrow.getShooter();
						
						if (plugin.isTeams())
						{
							if (plugin.getTeamManager().getPlayerTeam(damaged.getName()).hasPlayer(damager.getName()))
							{
								event.setCancelled(true);
								event.getDamager().sendMessage(ChatColor.RED + "You cannot damage teammates");
								return;
							}
						}
						
						if (plugin.getStage() == GameStage.GATHER)
						{
							Arrow shot = (Arrow) event.getDamager();
							
							if (shot.getShooter() instanceof Player)
							{
								event.setCancelled(true);
								((Player) shot.getShooter()).sendMessage(ChatColor.RED + "You cannot shoot people before lava starts rising...");
							}
						}
						
						if (damagers.containsKey(damaged.getName()))
						{
							Damager damage = damagers.get(damaged.getName());
							if (!damage.player.equals(damager.getName()))
							{
								damage.setDamager(damager.getName());
								damage.setValid(true);
							}
						}
						else
						{
							damagers.put(damaged.getName(), new Damager(damager.getName()));
						}
						
						if (damaged.getHealth() - event.getDamage() <= 0)
						{
							event.setCancelled(true);
							playerDeath(damaged, event.getCause());
						}
					}
					else
					{
						if (arrow.getShooter() instanceof Skeleton)
						{															
							if (damagers.containsKey(damaged.getName()))
							{
								Damager damage = damagers.get(damaged.getName());
								if (!damage.player.equals("Skeleton"))
								{
									damage.setDamager("Skeleton");
									damage.setValid(false);
								}
							}
							else
							{
								damagers.put(damaged.getName(), new Damager("Skeleton", false));
							}
							
							if (damaged.getHealth() - event.getDamage() <= 0)
							{
								event.setCancelled(true);
								playerDeath(damaged, event.getCause());
							}
						}
					}
				}
				else if (event.getDamager() instanceof Snowball)
				{
					Snowball snow = (Snowball) event.getDamager();
					
					Player damaged = (Player) event.getEntity();
					
					if (snow.getShooter() instanceof Player)
					{
						Player damager = (Player) snow.getShooter();
						
						if (plugin.isTeams())
						{
							if (plugin.getTeamManager().getPlayerTeam(damaged.getName()).hasPlayer(damager.getName()))
							{
								event.setCancelled(true);
								event.getDamager().sendMessage(ChatColor.RED + "You cannot damage teammates");
								return;
							}
						}
						
						if (plugin.getStage() == GameStage.GATHER)
						{
							Arrow shot = (Arrow) event.getDamager();
							
							if (shot.getShooter() instanceof Player)
							{
								event.setCancelled(true);
								((Player) shot.getShooter()).sendMessage(ChatColor.RED + "You cannot shoot people before lava starts rising...");
							}
						}
						
						if (damagers.containsKey(damaged.getName()))
						{
							Damager damage = damagers.get(damaged.getName());
							if (!damage.player.equals(damager.getName()))
							{
								damage.setDamager(damager.getName());
								damage.setValid(true);
							}
						}
						else
						{
							damagers.put(damaged.getName(), new Damager(damager.getName()));
						}
						
						if (damaged.getHealth() - event.getDamage() <= 0)
						{
							event.setCancelled(true);
							playerDeath(damaged, event.getCause());
						}
					}
				}				
				else if (event.getDamager() instanceof Egg)
				{
					Egg egg = (Egg) event.getDamager();
					
					Player damaged = (Player) event.getEntity();
					
					if (egg.getShooter() instanceof Player)
					{
						Player damager = (Player) egg.getShooter();
						
						if (plugin.isTeams())
						{
							if (plugin.getTeamManager().getPlayerTeam(damaged.getName()).hasPlayer(damager.getName()))
							{
								event.setCancelled(true);
								event.getDamager().sendMessage(ChatColor.RED + "You cannot damage teammates");
								return;
							}
						}
						
						if (plugin.getStage() == GameStage.GATHER)
						{
							Arrow shot = (Arrow) event.getDamager();
							
							if (shot.getShooter() instanceof Player)
							{
								event.setCancelled(true);
								((Player) shot.getShooter()).sendMessage(ChatColor.RED + "You cannot shoot people before lava starts rising...");
							}
						}
					
						if (damagers.containsKey(damaged.getName()))
						{
							Damager damage = damagers.get(damaged.getName());
							if (!damage.player.equals(damager.getName()))
							{
								damage.setDamager(damager.getName());
								damage.setValid(true);
							}
						}
						else
						{
							damagers.put(damaged.getName(), new Damager(damager.getName()));
						}
						
						if (damaged.getHealth() - event.getDamage() <= 0)
						{
							event.setCancelled(true);
							playerDeath(damaged, event.getCause());
						}	
					}
				}
				else if (event.getDamager() instanceof EnderDragon)
				{
					Player damaged = (Player) event.getEntity();
					
					if (damagers.containsKey(damaged.getName()))
					{
						Damager damage = damagers.get(damaged.getName());
						if (!damage.player.equals("Ender Dragon"))
						{
							damage.setDamager("Ender Dragon");
							damage.setValid(false);
						}
					}
					else
					{
						damagers.put(damaged.getName(), new Damager("Ender Dragon", false));
					}
					
					if (damaged.getHealth() - event.getDamage() <= 0)
					{
						event.setCancelled(true);
						playerDeath(damaged, event.getCause());
					}
				}
				else if (event.getDamager() instanceof Wither || event.getDamager() instanceof WitherSkull)
				{
					Player damaged = (Player) event.getEntity();
					
					if (damagers.containsKey(damaged.getName()))
					{
						Damager damage = damagers.get(damaged.getName());
						if (!damage.player.equals("Wither Boss"))
						{
							damage.setDamager("Wither Boss");
							damage.setValid(false);
						}
					}
					else
					{
						damagers.put(damaged.getName(), new Damager("Wither Boss", false));
					}
					
					if (damaged.getHealth() - event.getDamage() <= 0)
					{
						event.setCancelled(true);
						playerDeath(damaged, event.getCause());
					}
				}
				else if (event.getDamager() instanceof Zombie)
				{
					Player damaged = (Player) event.getEntity();
					
					if (damagers.containsKey(damaged.getName()))
					{
						Damager damage = damagers.get(damaged.getName());
						if (!damage.player.equals("Zombie"))
						{
							damage.setDamager("Zombie");
							damage.setValid(false);
						}
					}
					else
					{
						damagers.put(damaged.getName(), new Damager("Zombie", false));
					}
					
					if (damaged.getHealth() - event.getDamage() <= 0)
					{
						event.setCancelled(true);
						playerDeath(damaged, event.getCause());
					}
				}
				else if (event.getDamager() instanceof Spider || event.getDamager() instanceof CaveSpider)
				{
					Player damaged = (Player) event.getEntity();
					
					if (damagers.containsKey(damaged.getName()))
					{
						Damager damage = damagers.get(damaged.getName());
						if (!damage.player.equals("Spider"))
						{
							damage.setDamager("Spider");
							damage.setValid(false);
						}
					}
					else
					{
						damagers.put(damaged.getName(), new Damager("Spider", false));
					}
					
					if (damaged.getHealth() - event.getDamage() <= 0)
					{
						event.setCancelled(true);
						playerDeath(damaged, event.getCause());
					}
				}
				else if (event.getDamager() instanceof Enderman)
				{
					Player damaged = (Player) event.getEntity();
					
					if (damagers.containsKey(damaged.getName()))
					{
						Damager damage = damagers.get(damaged.getName());
						if (!damage.player.equals("Enderman"))
						{
							damage.setDamager("Enderman");
							damage.setValid(false);
						}
					}
					else
					{
						damagers.put(damaged.getName(), new Damager("Enderman", false));
					}
					
					if (damaged.getHealth() - event.getDamage() <= 0)
					{
						event.setCancelled(true);
						playerDeath(damaged, event.getCause());
					}
				}
				else if (event.getDamager() instanceof Fireball)
				{
					Fireball ball = (Fireball) event.getDamager();
					
					if (ball.getShooter() instanceof Ghast)
					{
						Player damaged = (Player) event.getEntity();
						
						if (damagers.containsKey(damaged.getName()))
						{
							Damager damage = damagers.get(damaged.getName());
							if (!damage.player.equals("Ghast"))
							{
								damage.setDamager("Ghast");
								damage.setValid(false);
							}
						}
						else
						{
							damagers.put(damaged.getName(), new Damager("Ghast", false));
						}
						
						if (damaged.getHealth() - event.getDamage() <= 0)
						{
							event.setCancelled(true);
							playerDeath(damaged, event.getCause());
						}
					}
					else if (ball.getShooter() instanceof Blaze)
					{
						Player damaged = (Player) event.getEntity();
						
						if (damagers.containsKey(damaged.getName()))
						{
							Damager damage = damagers.get(damaged.getName());
							if (!damage.player.equals("Blaze"))
							{
								damage.setDamager("Blaze");
								damage.setValid(false);
							}
						}
						else
						{
							damagers.put(damaged.getName(), new Damager("Blaze", false));
						}
						
						if (damaged.getHealth() - event.getDamage() <= 0)
						{
							event.setCancelled(true);
							playerDeath(damaged, event.getCause());
						}
					}
				}
			}
		}
		catch (Exception e) {}
	}
	
	@EventHandler
	public void craftItem(CraftItemEvent event)
	{
		if (plugin.getState() != GameState.LIVE)
		{
			return;
		}
		
		ItemStack result = event.getRecipe().getResult();
		
		Material type = result.getType();
		
		String itemType = type.toString();
		
		List<String> noCraftItems = Arrays.asList("DOOR","FENCE","LADDER","BED","BUCKET","SIGN","REPEATER");
		
		for (String ban : noCraftItems)
		{
			if (itemType.contains(ban))
			{
				event.setCancelled(true);
				event.getWhoClicked().sendMessage(ChatColor.RED + "This item is not craftable!");
			}
		}
	}
	
	/*
	 * Blocking all people in gamemode 1 from messing with arena
	 */
	
	@EventHandler
	public void gm1_clickInv(InventoryClickEvent event)
	{
		if (plugin.getState() == GameState.PREGAME)
		{
			return;
		}
		
		if (plugin.getState() != GameState.LIVE)
		{
			return;
		}
		
		if (event.getWhoClicked().getGameMode() == GameMode.CREATIVE)
		{
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void gm1_breakBlock(BlockBreakEvent event)
	{
		if (plugin.getState() != GameState.LIVE)
		{
			if (plugin.getState() != GameState.FINISHED)
			{
				return;	
			}
		}
		
		if (event.getPlayer().getGameMode() == GameMode.CREATIVE)
		{
			event.setCancelled(true);
			return;
		}
	}
	
	@EventHandler
	public void gm1_placeBlock(BlockPlaceEvent event)
	{
		if (plugin.getState() != GameState.LIVE)
		{
			if (plugin.getState() != GameState.FINISHED)
			{
				return;	
			}
		}
		
		if (event.getPlayer().getGameMode() == GameMode.CREATIVE)
		{
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void gm1_playerMove(PlayerMoveEvent event)
	{
		if (plugin.getState() != GameState.LIVE)
		{
			return;
		}
		
		if (event.getPlayer().getGameMode() == GameMode.CREATIVE)
		{
			if (event.getTo().getY() >= 256)
			{
				Location l = event.getTo();
				l.setY(254);
				event.getPlayer().teleport(l);
			}
		}
	}
	
	@EventHandler
	public void gm1_entityDamage(EntityDamageByEntityEvent event)
	{
		if (plugin.getState() != GameState.LIVE)
		{
			return;
		}
		
		if (event.getDamager() instanceof Player)
		{
			if (((Player) event.getDamager()).getGameMode() == GameMode.CREATIVE)
			{
				event.setCancelled(true);
			}
		}
	}
	
	@EventHandler
	public void gm1_entityTrack(EntityTargetLivingEntityEvent event)
	{
		if (event.getTarget() instanceof Player)
		{
			if (((Player) event.getTarget()).getGameMode() == GameMode.CREATIVE)
			{
				event.setCancelled(true);
			}
		}
		
		if (event.getEntity() instanceof Wither)
		{
			if (event.getTarget() instanceof Wither)
			{
				for (Player player : Bukkit.getOnlinePlayers())
				{
					event.setTarget(player);
					break;
				}
			}
		}
	}
	
	@EventHandler
	public void gm1_blockClick(EntityInteractEvent event)
	{
		if (event.getEntity() instanceof Player)
		{
			Player player = (Player) event.getEntity();
			
			if (player.getGameMode() != GameMode.CREATIVE)
			{
				return;
			}
			
			if (event.getBlock() != null)
			{
				Block block = event.getBlock();
				
				if (block.getType() == Material.CHEST)
				{
					event.setCancelled(true);
					player.openInventory(((Chest) block).getBlockInventory());
				}
			}
		}
	}
	
	@EventHandler
	public void gm1_itemCollect(PlayerPickupItemEvent event)
	{	
		if (plugin.getState() != GameState.LIVE)
		{
			if (plugin.getState() != GameState.FINISHED)
			{
				return;	
			}
		}
		
		if (event.getPlayer().getGameMode() == GameMode.CREATIVE)
		{
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void gm1_itemDrop(PlayerDropItemEvent event)
	{
		if (plugin.getState() != GameState.LIVE)
		{
			if (plugin.getState() != GameState.FINISHED)
			{
				return;	
			}
		}
		
		if (event.getPlayer().getGameMode() == GameMode.CREATIVE)
		{
			event.setCancelled(true);
		}
	}
	
	private class Damager {
		
		private String player = "";
		private int time = 0;
		private boolean isPlayer = true;
		
		public Damager(String playername, boolean isPlayer)
		{
			this.player = playername;
			time = 0;
			this.isPlayer = isPlayer;
		}
		
		public Damager(String playername)
		{
			this(playername, true);
		}
		
		public boolean isValidPlayer()
		{
			return isPlayer;
		}
		
		public void setValid(boolean valid)
		{
			this.isPlayer = valid;
		}
		
		public boolean incrementTime()
		{
			time ++;
			
			return timeOver();
		}
		
		public void setDamager(String player)
		{
			this.player = player;
			time = 0;
		}
		
		public boolean timeOver()
		{
			if (time > 15)
			{
				return true;
			}
			
			return false;
		}
	}
	
	public HashMap<String, Damager> damagers = new HashMap<String, Damager>();
	
	@Override
	public void run() {

		if (plugin.getState() == GameState.FINISHED)
		{
			this.cancel();
		}
		
		if (plugin.getState() != GameState.LIVE)
		{
			return;
		}
		
		List<String> keys = new ArrayList<String>();
		keys.addAll(damagers.keySet());
		
		for (String key : keys)
		{
			Damager damager = damagers.get(key);
			
			if (damager.incrementTime())
			{
				damagers.remove(key);
			}
		}
		
	}


}
