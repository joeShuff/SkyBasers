/*
 * Bukkit Plugin - SkyBasers
 * For use on The Justice Network
 * 
 * @author - Joseph Shufflebotham
 * 
 * © Copyright 2016 
 */
package me.JoeShuff.SkyBasers;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import me.JoeShuff.SkyBasers.FTP.FTPManager;
import me.JoeShuff.SkyBasers.Items.HubItem;
import me.JoeShuff.SkyBasers.Items.KitItem;
import me.JoeShuff.SkyBasers.Items.PlayerStatItem;
import me.JoeShuff.SkyBasers.Items.SpectatorCompass;
import me.JoeShuff.SkyBasers.Items.SpectatorOptions;
import me.JoeShuff.SkyBasers.Items.TeamStick;
import me.JoeShuff.SkyBasers.Listeners.GameListener;
import me.JoeShuff.SkyBasers.Listeners.GameListener.ChatChannel;
import me.JoeShuff.SkyBasers.Listeners.PreListener;
import me.JoeShuff.SkyBasers.Mobs.Bosses;
import me.JoeShuff.SkyBasers.MySQL.MySQL;
import me.JoeShuff.SkyBasers.MySQL.SQLManager;
import me.JoeShuff.SkyBasers.MySQL.SQLManager.MBlock;
import me.JoeShuff.SkyBasers.Permissions.Group;
import me.JoeShuff.SkyBasers.Permissions.PermissionGroups;
import me.JoeShuff.SkyBasers.Scoreboards.LeaderBoardHolograms;
import me.JoeShuff.SkyBasers.Scoreboards.ManageHubScoreboard;
import me.JoeShuff.SkyBasers.Scoreboards.ManagePlayerScoreboard;
import me.JoeShuff.SkyBasers.Scoreboards.TeamManager;
import me.JoeShuff.SkyBasers.Timers.GameOverTimer;
import me.JoeShuff.SkyBasers.Timers.PreGameTimer;
import me.JoeShuff.SkyBasers.Timers.RequestTimer;
import net.minecraft.server.v1_8_R3.PacketPlayOutEntityDestroy;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.PluginMessageListener;

import com.google.common.io.Files;


public class SkyBasers extends JavaPlugin implements PluginMessageListener
{
	public static String worldName; 
	
	public enum GameState {
	    PREGAME, LOADING, LIVE, FINISHED
	}
	
	public enum GameType {
		NORMAL, JUDGEMENT
	}
	
	public enum GameStage {
		GATHER, LAVA, AFTER, DOOM
	}
	
	private boolean teams;
	
	private boolean sendRequests = true;
	
	public int playReward = 0;
	public int killReward = 0;
	public int winReward = 0;
	
	private boolean kitsEnabled = false;
	
	private boolean starting = false;
	
	private List<String> alivePlayers = new ArrayList<String>();
	
	private GameState gameState;
	
	private GameType gameType;
	
	private GameStage gameStage;
	
	private ManageHubScoreboard scoreboard;
	
	private ManagePlayerScoreboard playerScoreboards;
	
	private TeamManager teamManager;
	
	private GameListener listener;
	
	private GameOverTimer gameOverTimer = null;
	
	private Bosses bosses;
	
	private LeaderBoardHolograms leaderboard;
	
	public static FTPManager ftpManager;
	
	private List<OfflinePlayer> WINNER = new ArrayList<OfflinePlayer>();
	
	public boolean kitsEnabled()
	{
		return kitsEnabled;
	}
	
	public void setStarting(boolean starting)
	{
		this.starting = starting;
	}
	
	public boolean isStarting()
	{
		return starting;
	}
	
	public GameState getState()
	{
		return gameState;
	}
	
	public void setWinner(List<OfflinePlayer> winner)
	{
		this.WINNER = winner;
	}
	
	public List<OfflinePlayer> getWinner()
	{
		return WINNER;
	}
	
	public GameListener getGameListener()
	{
		return this.listener;
	}
	
	public TeamManager getTeamManager()
	{
		return this.teamManager;
	}
	
	public void setState(GameState state)
	{
		this.gameState = state;
		
		if (state == GameState.FINISHED)
		{
			if (gameOverTimer == null)
			{
				gameOverTimer = new GameOverTimer(this);
			}
		}
	}
	
	public GameStage getStage()
	{
		return gameStage;
	}
	
	public void setStage(GameStage stage)
	{
		this.gameStage = stage;
		playerScoreboards.update();
		
		if (getStage() == GameStage.AFTER)
		{
			if (gameOverTimer == null)
			{
				gameOverTimer = new GameOverTimer(this);
			}
		}
	}
	
	public GameType getType()
	{
		return gameType;
	}
	
	public boolean canRequest()
	{
		return sendRequests;
	}
	
	public void setCanRequest(boolean canRequest)
	{
		sendRequests = canRequest;
	}
	
	public Bosses getBosses()
	{
		return bosses;
	}
	
	public ManageHubScoreboard getScoreBoardManager()
	{
		return scoreboard;
	}
	
	public ManagePlayerScoreboard getPlayerScoreboards(Boolean boot)
	{
		if (boot)
		{
			playerScoreboards = new ManagePlayerScoreboard(this);
			playerScoreboards.bootup();
		}
		
		return playerScoreboards;
	}
	
	public boolean isTeams()
	{
		return teams;
	}
	
	public void onEnable()
	{
		gameStage = GameStage.GATHER;
		gameState = GameState.PREGAME;
		
		File sqlFile = new File(this.getDataFolder(),"MySQL.yml");
		if (!sqlFile.exists()) 
		{
		     try
		     {
		       this.saveResource("MySQL.yml", true);
		       sqlFile.createNewFile();
		     }
		     catch (Exception e)
		     {
		       System.out.println("Couldn't create file MySQL.yml");
		     }
		}
		
		File ruleFile = new File(this.getDataFolder(),"rules.yml");
		if (!ruleFile.exists()) 
		{
		     try
		     {
		       this.saveResource("rules.yml", true);
		       ruleFile.createNewFile();
		     }
		     catch (Exception e)
		     {
		       System.out.println("Couldn't create file rules.yml");
		     }
		}
		
		File spawnFile = new File(this.getDataFolder(),"spawnRates.yml");
		if (!spawnFile.exists()) 
		{
		     try
		     {
		       this.saveResource("spawnRates.yml", true);
		       spawnFile.createNewFile();
		     }
		     catch (Exception e)
		     {
		       System.out.println("Couldn't create file spawnRates.yml");
		     }
		}
		
		File chestFile = new File(this.getDataFolder(),"chestLoot.yml");
		if (!chestFile.exists()) 
		{
		     try
		     {
		       this.saveResource("chestLoot.yml", true);
		       chestFile.createNewFile();
		     }
		     catch (Exception e)
		     {
		       System.out.println("Couldn't create file chestLoot.yml");
		     }
		}
		
		File conFile = new File(this.getDataFolder(),"config.yml");
		if (!conFile.exists()) 
		{
		     try
		     {
		       this.saveResource("config.yml", true);
		       conFile.createNewFile();
		     }
		     catch (Exception e)
		     {
		       System.out.println("Couldn't create file config.yml");
		     }
		}
		
		File mobFile = new File(this.getDataFolder(),"mobAmount.yml");
		if (!mobFile.exists()) 
		{
		     try
		     {
		       this.saveResource("mobAmount.yml", true);
		       mobFile.createNewFile();
		     }
		     catch (Exception e)
		     {
		       System.out.println("Couldn't create file mobAmount.yml");
		     }
		}
		
		File kitFile = new File(this.getDataFolder(), "kits.yml");
		if (!kitFile.exists())
		{
			try
			{
				this.saveResource("kits.yml", true);
				kitFile.createNewFile();
			} catch (Exception e)
			{
				System.err.println("Couldn't create file kits.yml");
			}
		}
		
		File podFile = new File(this.getDataFolder(), "pod.schematic");
		if (!podFile.exists())
		{
			try
			{
				this.saveResource("pod.schematic", true);
				podFile.createNewFile();
			} catch (Exception e)
			{
				System.err.println("Couldn't create file pod.schematic");
			}
		}
		
		File TpodFile = new File(this.getDataFolder(), "teamPod.schematic");
		if (!TpodFile.exists())
		{
			try
			{
				this.saveResource("teamPod.schematic", true);
				TpodFile.createNewFile();
			} catch (Exception e)
			{
				System.err.println("Couldn't create file teamPod.schematic");
			}
		}
		
		File ftpFile = new File(this.getDataFolder(), "FTP.yml");
		if (!ftpFile.exists())
		{
			try
			{
				this.saveResource("FTP.yml", true);
				ftpFile.createNewFile();
			} catch (Exception e)
			{
				System.err.println("Couldn't create file FTP.yml");
			}
		}
		
		ftpManager = new FTPManager(this, getFTPData().getString("host"), getFTPData().getString("username"), getFTPData().getString("password"));
		
		ftpManager.downloadFileUpdates();
		
		saveMaps();
		
		new KitItem(this);
		new HubItem(this);
		new PlayerStatItem(this);
		new SpectatorCompass(this);
		new TeamStick(this);
		new SpectatorOptions(this);
		new PermissionGroups(this);
		
		Kits.init(this);
		
		BungeeComm.init(this);
		
		setupMySQL();
		
		getMode();
		
		getData();
		
		addRecipes();
		
		sendRequests = true;
		
		try
		{
			Bukkit.getWorld(worldName).setGameRuleValue("doDaylightCycle", "false");
			Bukkit.getWorld(worldName).setTime(0);
		}
		catch (Exception e){};
		
		
		scoreboard = new ManageHubScoreboard(this);
		
		for (Player player : Bukkit.getOnlinePlayers())
		{
			player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
			
			player.setLevel(0);
			player.setExp(0);
			
			for (Player player2 : Bukkit.getOnlinePlayers())
			{
				if (!player.getName().equals(player2.getName()))
				{
					player.showPlayer(player2);
				}
			}
		}
		
		this.getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
	    this.getServer().getMessenger().registerIncomingPluginChannel(this, "BungeeCord", this);
	    
		new PreListener(this);
		listener = new GameListener(this);
		teamManager = new TeamManager(this);
		bosses = new Bosses(this);
		
		SQLManager.init(this);
		
		leaderboard = new LeaderBoardHolograms(this);
		
		RankSystemBukkit.bootup(this);
		
		new RequestTimer(this);
		
		getCommand("editmode").setTabCompleter(new EditTabCompleter(this));
		
		getLogger().info("========================================");
		getLogger().info("Joe Shuff's SkyBasers Plugin has enabled");
		getLogger().info("========================================");
	}
	
	public void onDisable()
	{
		try
		{
			for (Player player : Bukkit.getOnlinePlayers())
			{
				if (leaderboard.id.containsKey(player.getName()))
				{
					PacketPlayOutEntityDestroy packet = new PacketPlayOutEntityDestroy(leaderboard.id.get(player.getName()));
					((CraftPlayer)player).getHandle().playerConnection.sendPacket(packet);
				}
			}
			
			SQLManager.sendServerRequest(Bukkit.getServerName(), getConfig().getInt("max-players"), "null", "false", true);
		}
		catch (Exception e) {}
	}
	
	public void getData()
	{
		worldName = this.getConfig().getString("world");
		int isteams = this.getConfig().getInt("teams");
		
		if (isteams == 0)
		{
			teams = false;
		}
		else
		{
			teams = true;
		}
		
		int isKits = this.getConfig().getInt("kits");
		
		if (isKits == 0)
		{
			kitsEnabled = false;
		}
		else
		{
			kitsEnabled = true;
		}
		
		playReward = this.getConfig().getInt("play-reward");
		killReward = this.getConfig().getInt("kill-reward");
		winReward = this.getConfig().getInt("win-reward");
		
	}
	
	private void getMode()
	{
		int selection = getConfig().getInt("mode");
		
		if (selection == 0)
		{
			gameType = GameType.NORMAL;
		}
		else if (selection == 1)
		{
			gameType = GameType.JUDGEMENT;
		}
	}
	
	public Location getSpawnLoc()
	{	
		int centreX = getConfig().getInt("hubCentreX");
		int centreY = getConfig().getInt("hubCentreY");
		int centreZ = getConfig().getInt("hubCentreZ");
		
		Location centre = new Location(Bukkit.getWorld(SkyBasers.worldName),centreX,centreY,centreZ);
		centre = centre.add(0.5,0,0.5);
		return centre;
	}
	
	public Location getCentreMap(boolean forceY, int Y)
	{
		
		int centreX = getConfig().getInt("mapCentreX");
		int centreY;
		
		if (!forceY)
		{
			centreY  = getConfig().getInt("mapCentreY");
		}
		else
		{
			centreY = Y;
		}
		
		int centreZ = getConfig().getInt("mapCentreZ");
		
		Location centre = new Location(Bukkit.getWorld(worldName), centreX, centreY, centreZ);
		
		if (!forceY)
		{
			return centre;
		}
		
		centre = centre.add(0.5, 5, 0.5);
		return centre;
	}
	
	public void setupMySQL()
	  {
		String ip;
		int port;
		String name;
		String user;
		String pass;
		
		File sqlFile = new File(this.getDataFolder(),"MySQL.yml");
		FileConfiguration sqlData = YamlConfiguration.loadConfiguration(sqlFile);
		
	    try
	    {
	      ip = sqlData.getString("ip");
	      port = sqlData.getInt("port");
	      name = sqlData.getString("name");
	      user = sqlData.getString("username");
	      pass = sqlData.getString("password");
	    }
	    catch (NullPointerException e)
	    {
	      ip = "";
	      port = 0;
	      name = "";
	      user = "";
	      pass = "";
	    }
	    new MySQL(ip, name, user, pass, this);
	  }
	
	public FileConfiguration getMySQLData()
	{
		File sqlFile = new File(this.getDataFolder(),"MySQL.yml");
		FileConfiguration sqlData = YamlConfiguration.loadConfiguration(sqlFile);
		
		return sqlData;
	}

	public FileConfiguration getspawnRatesData()
	{
		File spawnFile = new File(this.getDataFolder(),"spawnRates.yml");
		FileConfiguration spawnData = YamlConfiguration.loadConfiguration(spawnFile);
		
		return spawnData;
	}
	
	public FileConfiguration getchestLootData()
	{
		File spawnFile = new File(this.getDataFolder(),"chestLoot.yml");
		FileConfiguration spawnData = YamlConfiguration.loadConfiguration(spawnFile);
		
		return spawnData;
	}
	
	public FileConfiguration getMobAmountData()
	{
		File mobFile = new File(this.getDataFolder(), "mobAmount.yml");
		FileConfiguration mobConfig = YamlConfiguration.loadConfiguration(mobFile);
		
		return mobConfig;
	}
	
	public FileConfiguration getKitsConfig()
	{
		File kitFile = new File(this.getDataFolder(), "kits.yml");
		FileConfiguration kitConfig = YamlConfiguration.loadConfiguration(kitFile);
		
		return kitConfig;
	}
	
	public FileConfiguration getRuleConfig()
	{
		File ruleFile = new File(this.getDataFolder(), "rules.yml");
		FileConfiguration ruleConfig = YamlConfiguration.loadConfiguration(ruleFile);
		
		return ruleConfig;
	}
	
	public FileConfiguration getFTPData()
	{
		File ftpFile = new File(this.getDataFolder(), "FTP.yml");
		FileConfiguration ftpConfig = YamlConfiguration.loadConfiguration(ftpFile);
		
		return ftpConfig;
	}
	
	List<String> leaving = new ArrayList<String>();
	
	public boolean onCommand(CommandSender sender,Command cmd,String label,String[] args) 
	{	
		if (cmd.getName().equalsIgnoreCase("sbjoin") && sender instanceof Player)
		{
			if (args.length == 2)
			{
				String mode = args[0];
				String valid = args[1];
				
				if (!valid.equalsIgnoreCase("true"))
				{
					sender.sendMessage(ChatColor.RED + "Invalid use of the command!");
					return true;
				}
				
				SQLManager.sendPlayerRequest((Player) sender, mode);
				return true;
			}
			else
			{
				sender.sendMessage(ChatColor.RED + "Invalid use of the command!");
				return true;
			}
		}
		
		if (cmd.getName().equalsIgnoreCase("leave") && sender instanceof Player)
		{
			Player player = (Player) sender;
			
			if (getState() == GameState.PREGAME || getState() == GameState.FINISHED)
			{
				BungeeComm.sendToServer(player, getConfig().getString("hub"));
				return true;
			}
			
			if (getState() == GameState.LIVE && !getPlayerScoreboards(false).hasAlivePlayer(player))
			{
				BungeeComm.sendToServer(player, getConfig().getString("hub"));
				return true;
			}
			
			if (!leaving.contains(player.getName()))
			{
				sender.sendMessage(ChatColor.RED + "Are you sure you want to leave? Do /leave again to leave.");
				leaving.add(player.getName());
				
				return true;
			}
			else
			{
				if (getState() == GameState.LIVE)
				{
					if (getPlayerScoreboards(false).hasAlivePlayer(player))
					{
						getGameListener().playerDeath(player, DamageCause.CUSTOM);
					}
				}	
				
				leaving.remove(player.getName());
				
				BungeeComm.sendToServer(player, getConfig().getString("hub"));
				return true;
			}		
		}
		
		if (cmd.getName().equalsIgnoreCase("rules") && sender instanceof Player)
		{
			sender.sendMessage("" + ChatColor.GREEN + ChatColor.BOLD + "===== " + ChatColor.AQUA + "SKYBASERS " + ChatColor.GOLD + "RULES " + ChatColor.GREEN + ChatColor.BOLD + "=====");
			
			List<String> rules = getRuleConfig().getStringList("rules");
			
			for (String rule : rules)
			{
				String showRule = ChatColor.translateAlternateColorCodes('&', rule);
				
				sender.sendMessage(ChatColor.YELLOW + "- " + ChatColor.GREEN + showRule);
			}
			
			sender.sendMessage("" + ChatColor.GREEN + ChatColor.BOLD + "========================");
			
			return true;
		}
		
		if (cmd.getName().equalsIgnoreCase("team"))
		{	
			if (sender instanceof Player)
			{
				if (!isTeams())
				{
					sender.sendMessage(ChatColor.RED + "This is solo mode! You cannot team!");
					return true;
				}
				
				if (getState() != GameState.PREGAME)
				{
					sender.sendMessage(ChatColor.RED + "You can only use /team commands before the game");
					return true;
				}
				
				Player playerRan = (Player) sender;
				
				if (args.length == 0)
				{
					playerRan.sendMessage(ChatColor.RED + "Insufficient arguments - \n/team <player>\n"
							                            + "/team accept <player>\n"
							                            + "/team disband");
					
					return true;
				}
				
				if (args.length == 1)
				{
					if (args[0].equalsIgnoreCase("disband"))
					{
						getTeamManager().disbandTeam(playerRan.getName());
						return true;
					}
					else
					{
						if (args[0].equals(playerRan.getName()))
						{
							playerRan.sendMessage(ChatColor.RED + "You cannot team with yourself!");
							return true;
						}
						
						getTeamManager().requestTeam(playerRan.getName(), args[0]);
						return true;
					}
				}
				else if (args.length == 2)
				{
					if (args[0].equals("accept"))
					{
						String player = args[1];
						
						if (player.equals(playerRan.getName()))
						{
							playerRan.sendMessage(ChatColor.RED + "You cannot team with yourself!");
							return true;
						}
						
						getTeamManager().acceptRequest(playerRan.getName(), player);
						
						return true;
					}
					else
					{
						playerRan.sendMessage(ChatColor.RED + "Insufficient arguments - \n/team <player>\n"
	                            + "/team accept <player>\n"
	                            + "/team disband");

						return true;
					}
				}
				else
				{
					playerRan.sendMessage(ChatColor.RED + "Insufficient arguments - \n/team <player>\n"
                            + "/team accept <player>\n"
                            + "/team disband");
					
					return true;
				}
			}
			
			return true;
		}
		
		if (cmd.getName().equalsIgnoreCase("start"))
		{
			if (sender instanceof Player)
			{
				Player player = (Player) sender;
				
				if (player.isOp())
				{
					if (!isStarting())
					{
						setStarting(true);
						new PreGameTimer(this, true, Bukkit.getOnlinePlayers().size());
						player.sendMessage(ChatColor.GREEN + "Forced game start!");
						return true;
					}
					else
					{
						player.sendMessage(ChatColor.RED + "Game already starting!");
					}
				}
				else
				{
					player.sendMessage(ChatColor.RED + "You do not have permission to use that!");
					return true;
				}
			}
			
			return true;
		}
		
		if (cmd.getName().equalsIgnoreCase("tchat") && sender instanceof Player)
		{
			if (getState() == GameState.LIVE)
			{
				if (isTeams())
				{
					getGameListener().setChannel((Player) sender, ChatChannel.TEAM);
					sender.sendMessage(ChatColor.GREEN + "You are now in the team channel");
					
					return true;
				}
				else
				{
					sender.sendMessage(ChatColor.RED + "You cannot go into team channel in solo mode!");
					return true;
				}
			}
			else
			{
				sender.sendMessage(ChatColor.RED + "You cannot switch channel outside of gameplay");
				return true;
			}
		}
		
		if (cmd.getName().equalsIgnoreCase("gchat") && sender instanceof Player)
		{
			if (getState() == GameState.LIVE)
			{
				if (!isTeams())
				{
					sender.sendMessage(ChatColor.RED + "You cannot change chat channel in solo mode");
					
					return true;
				}
				
				getGameListener().setChannel((Player) sender, ChatChannel.GLOBAL);
				sender.sendMessage(ChatColor.GREEN + "You are now in the global channel");
				
				return true;
			}
			else
			{
				sender.sendMessage(ChatColor.RED + "You cannot switch channels outside of gameplay");
				return true;
			}
		}
		
		if (cmd.getName().equalsIgnoreCase("groups"))
		{
			if (sender instanceof Player)
			{
				if (!PermissionGroups.playerHasPerm(((Player) sender).getName(), "skybasers.OP"))
				{
					sender.sendMessage(ChatColor.RED + "You do not have permission to use this command");
					return true;
				}
			}
			
			if (args.length != 1)
			{
				sender.sendMessage(ChatColor.RED + "Insufficient arguments: /groups refresh | list");
				return true;
			}
			
			if (args[0].equals("refresh"))
			{
				PermissionGroups.refreshGroups(this);
				return true;
			}
			
			if (args[0].equals("list"))
			{
				sender.sendMessage(ChatColor.BLUE + "Displaying " + ChatColor.YELLOW + PermissionGroups.getGroups().size() + ChatColor.BLUE + " groups:");
				for (Group group : PermissionGroups.getGroups())
				{
					sender.sendMessage(ChatColor.YELLOW + "- " + ChatColor.GREEN + group.getGroupName());
				}
				return true;
			}
			
			sender.sendMessage(ChatColor.RED + "Insufficient arguments: /groups refresh | list");
			return true;
		}                                                                                                     
		
		if (cmd.getName().equalsIgnoreCase("editmode"))
		{
			if (getState() != GameState.PREGAME)
			{
				sender.sendMessage(ChatColor.RED + "Cannot change mode whilst ingame!");
				return true;
			}
			
			if (sender instanceof Player)
			{
				Player player = (Player) sender;
				
				if (!PermissionGroups.playerHasPerm(player.getName(), "skybasers.OP"))
				{
					player.sendMessage(ChatColor.RED + "You do not have permission to use that command!");
					return true;
				}
			}
			
			if (args.length != 2)
			{
				sender.sendMessage(ChatColor.RED + "Insufficient Arguments - /editmode <mode> <value>");
				return true;
			}
				
			String toEdit = args[0];
			String value = args[1];
			
			Integer newvalue = null;
			
			try{
				newvalue = Integer.valueOf(value);
			}
			catch (Exception e)
			{
				if (value.equals("true") && (toEdit.equalsIgnoreCase("teams") || toEdit.equalsIgnoreCase("kits")))
				{
					newvalue = 1;
				}
				else if ( value.equals("false") && (toEdit.equalsIgnoreCase("teams") || toEdit.equalsIgnoreCase("kits")))
				{
					newvalue = 0;
				}
				else if (value.equalsIgnoreCase("normal") && toEdit.equalsIgnoreCase("mode"))
				{
					newvalue = 0;
				}
				else if (value.equalsIgnoreCase("judgement")  && toEdit.equalsIgnoreCase("mode"))
				{
					newvalue = 1;
				}
			}
			
			
			if (newvalue == null)
			{
				sender.sendMessage(ChatColor.RED + "Invalid new value - " + value);
				return true;
			}
			
			List<String> list = Arrays.asList("teams","mode","kits","game-time");
			
			if (!list.contains(toEdit.toLowerCase()))
			{
				sender.sendMessage(ChatColor.RED + "Cannot find configurable mode " + toEdit);
				return true;
			}
			
			getConfig().set(toEdit, newvalue);
			saveConfig();
			
			getMode();
			
			getData();
			
			getScoreBoardManager().restart();
			
			leaderboard.changeMode();
			
			ChatColor color = ChatColor.BLUE;
			
			if (getType() == GameType.JUDGEMENT)
			{
				color = ChatColor.RED;
			}
			
			String teams = "SOLO";
			
			if (isTeams())
			{
				teams = "TEAMS";
			}
			
			Bukkit.broadcastMessage("" + ChatColor.GOLD + ChatColor.BOLD + ChatColor.STRIKETHROUGH + "==============================");
			Bukkit.broadcastMessage(ChatColor.GREEN + "Mode Changed to: " + color + getType().toString() + ChatColor.GOLD + " " + teams);
			Bukkit.broadcastMessage("" + ChatColor.GOLD + ChatColor.BOLD + ChatColor.STRIKETHROUGH + "==============================");
			
			return true;
		}
		
		if (cmd.getName().equalsIgnoreCase("setpod") && sender instanceof Player)
		{
			Player player = (Player) sender;
			
			if (player.getItemInHand() == null)
			{
				player.sendMessage(ChatColor.RED + "Not a valid item");
				return true;
			}
			
			ItemStack newItem = player.getItemInHand();
			
			if (newItem.getType() == Material.STAINED_GLASS_PANE)
			{
				MBlock currentBlock = SQLManager.getPodBlock(player);
				SQLManager.setPodData(player, currentBlock.getMaterial(), currentBlock.getData(), (byte) newItem.getDurability());
				player.sendMessage(ChatColor.GREEN + "Set pod color");
				return true;
			}
			
			byte color = SQLManager.getPodColor(player);
		
			if (!newItem.getType().isBlock())
			{
				player.sendMessage(ChatColor.RED + "Thats not a block silly");
				return true;
			}
			
			SQLManager.setPodData(player, newItem.getType(), (byte) newItem.getDurability(), color);
			player.sendMessage(ChatColor.GREEN + "Set block");
			
			return true;
		}
		
		if (cmd.getName().equalsIgnoreCase("testdrop") && sender instanceof Player)
		{
			sender.sendMessage(ChatColor.RED + "Undergoing maintenance");
			return true;
		}
		
		return false;
	}

	@Override
	public void onPluginMessageReceived(String arg0, Player arg1, byte[] arg2) {
		// TODO Auto-generated method stub
		
	}
	
	public static ItemStack createItem(Material material, int amount, String name, List<String> lore)
	{
	    ItemStack item = new ItemStack(material, amount);
	    ItemMeta meta = item.getItemMeta();
	    meta.setLore(lore);
	    meta.setDisplayName(name);
	    item.setItemMeta(meta);
	    
	    return item;
	 }
	
	public void addRecipes()
	{
		ItemStack diamond = new ItemStack(Material.DIAMOND,1);
		ShapedRecipe diamondRecipe = new ShapedRecipe(diamond).shape("CC","CC").setIngredient('C',Material.COAL_BLOCK);
		getServer().addRecipe(diamondRecipe);
		
		ItemStack Kryptonite = createItem(Material.EMERALD, 1, ChatColor.AQUA + "Kryptonite", Arrays.asList(ChatColor.RED + "Used to kill Superman"));
		Kryptonite.addUnsafeEnchantment(Enchantment.KNOCKBACK,4);
		ShapedRecipe kryptonite = new ShapedRecipe(Kryptonite).shape("GGG","GEG","GGG").setIngredient('G', Material.GOLD_INGOT).setIngredient('E', Material.EMERALD);
		getServer().addRecipe(kryptonite);
	}
	
	public void saveMaps()
	{
		(new File(this.getDataFolder(), "/maps")).mkdirs();
		
		for (int i = 1 ; i <= 5 ; i ++)
		{
			File file = new File(this.getDataFolder(), "/maps/" + i + ".schematic");
			
			if (!file.exists()) 
			{
			     try
			     {
			    	 this.saveResource(i + ".schematic", true);
				     file.createNewFile();
				     Files.copy(new File(this.getDataFolder(), i + ".schematic"), file);
				     (new File(this.getDataFolder(), i + ".schematic")).delete();
			     }
			     catch (Exception e)
			     {
			    	 System.out.println("Couldn't create file " + i + ".schematic");
			     }
			}
		}
	}
}
