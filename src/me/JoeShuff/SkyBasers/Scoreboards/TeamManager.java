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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import me.JoeShuff.SkyBasers.SkyBasers;
import me.JoeShuff.SkyBasers.VisualEffects;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class TeamManager extends BukkitRunnable {

	public class Team
	{
		private List<String> players = new ArrayList<String>();
		
		private String prefix = "";
		
		public Team(List<String> players)
		{
			if (players.get(0).equals(players.get(1)))
			{
				this.players = Arrays.asList(players.get(0));
			}
			else
			{
				this.players = players;
			}
			
			prefix = getTeamColor(false);
		}
		
		public String getPrefix()
		{
			return prefix;
		}
		
		public boolean hasPlayer(Player player)
		{
			return hasPlayer(player.getName());
		}
		
		public List<String> getPlayers()
		{
			return players;
		}
		
		public boolean hasPlayer(String playername)
		{
			if (players.contains(playername))
			{
				return true;
			}
			
			return false;
		}

		public void disband()
		{
			for (String player : players)
			{
				if (Bukkit.getPlayer(player) != null)
				{
					Bukkit.getPlayer(player).sendMessage(ChatColor.RED + "Your team has been disbanded");
				}
			}
		}
	}
	
	private SkyBasers plugin;
	
	private List<Team> teams;
	
	public TeamManager(SkyBasers plugin)
	{
		this.teams = new ArrayList<Team>();
		
		this.plugin = plugin;
		
		this.runTaskTimer(plugin, 2, 2);
	}
	
	@Override
	public void run() 
	{
		List<String> keys = new ArrayList<String>();
		keys.addAll(requests.keySet());
		
		for (String key : keys)
		{
			if (requests.containsKey(key))
			{
				if (!requests.get(key).reduce())
				{
					disbandTeam(key, ChatColor.RED + "Your team request has timed out to ");
				}
			}
		}
	}
	
	public Team getPlayerTeam(Player player)
	{
		return getPlayerTeam(player.getName());
	}
	
	public String getTeamMate(Player player)
	{
		return getTeamMate(player.getName());
	}
	
	public boolean hasRequest(String player, String other)
	{
		if (requests.containsKey(other))
		{
			if (requests.get(other).getPlayer().equals(player))
			{
				return true;
			}
		}
		
		return false;
	}
	
	public String getTeamMate(String player)
	{
		Team team = getPlayerTeam(player);
		
		if (team == null) return null;
		
		for (String member : team.getPlayers())
		{
			if (!member.equals(player))
			{
				return member;
			}
		}
		
		return null;
	}
	
	public int amountOfTeams()
	{
		return teams.size();
	}
	
	public Team getPlayerTeam(String playername)
	{
		for (Team team : teams)
		{
			if (team.hasPlayer(playername))
			{
				return team;
			}
		}
		
		return null;
	}
	
	public void createTeam(String creator, String teammate)
	{	
		if (Bukkit.getPlayer(creator) != null)
		{
			if (getPlayerTeam(creator) != null)
			{
				getPlayerTeam(creator).disband();
				teams.remove(getPlayerTeam(creator));
			}
			
			if (requests.containsKey(creator))
			{
				requests.remove(creator);
			}
			
			if (!creator.equals(teammate))
			{
				Bukkit.getPlayer(creator).sendMessage(ChatColor.GREEN + "You are now on a team with " + teammate);
				Bukkit.getPlayer(creator).playSound(Bukkit.getPlayer(creator).getLocation(), Sound.LEVEL_UP, 1f, 1f);
			}

			if (Bukkit.getPlayer(teammate) != null)
			{
				if (requests.containsKey(teammate))
				{
					requests.remove(teammate);
				}
				
				if (getPlayerTeam(teammate) != null)
				{
					getPlayerTeam(teammate).disband();
					teams.remove(getPlayerTeam(teammate));
				}
				
				if (!creator.equals(teammate))
				{
					Bukkit.getPlayer(teammate).sendMessage(ChatColor.GREEN + "You are now on a team with " + creator);
					Bukkit.getPlayer(teammate).playSound(Bukkit.getPlayer(teammate).getLocation(), Sound.LEVEL_UP, 1f, 1f);
				}
				else
				{
					Bukkit.getPlayer(teammate).sendMessage(ChatColor.GREEN + "You are now on a team with yourself... loner!");
					Bukkit.getPlayer(teammate).playSound(Bukkit.getPlayer(teammate).getLocation(), Sound.LEVEL_UP, 1f, 1f);
				}
				
				teams.add(new Team(Arrays.asList(creator, teammate)));
			}
		}
	}
	
	public void disbandTeam(String player)
	{
		disbandTeam(player, ChatColor.GREEN + "You have cancelled your request to ");
	}
	
	public void disbandTeam(String player, String reason)
	{
		if (getPlayerTeam(player) != null)
		{
			getPlayerTeam(player).disband();
			teams.remove(getPlayerTeam(player));
		}
		else
		{
			if (requests.containsKey(player))
			{
				if (Bukkit.getPlayer(player) != null)
				{
					Bukkit.getPlayer(player).sendMessage(reason + requests.get(player).getPlayer());
				}
				
				requests.remove(player);
			}
		}
	}
	
	public void createRemaindingTeams()
	{
		int playersLeft = 0;
		
		List<Player> remainingPlayers = new ArrayList<Player>();
		
		for (Player player : Bukkit.getOnlinePlayers())
		{
			if (getPlayerTeam(player) == null)
			{
				remainingPlayers.add(player);
				playersLeft ++;
			}
		}
		
		int teamsToCreate;
		
		if (playersLeft % 2 == 0)
		{
			teamsToCreate = playersLeft / 2;
		}
		else
		{
			teamsToCreate = (playersLeft / 2) + 1;
		}
		
		Collections.shuffle(remainingPlayers);
		
		for (int i = 0 ; i < teamsToCreate ; i ++)
		{
			createTeam(remainingPlayers.get(i).getName(), remainingPlayers.get(remainingPlayers.size() - 1 - i).getName());
		}
	}
	
	HashMap<String, Request> requests = new HashMap<String, Request>();
	
	public class Request {
		
		private String player;
		private int cooldown;  //measures in 10th of a second
		
		public Request(String player)
		{
			this.player = player;
			this.cooldown = 50;
		}
		
		public boolean reduce()
		{
			cooldown --;
			
			if (cooldown > 0)
			{
				return true;
			}
			
			return false;
		}
		
		public String getPlayer()
		{
			return this.player;
		}
		
	}
	
	public void requestTeam(String creator, String teammate)
	{
		if (requests.containsKey(creator))
		{
			if (Bukkit.getPlayer(creator) != null)
			{
				Bukkit.getPlayer(creator).sendMessage(ChatColor.RED + "You have already sent a request to " + requests.get(creator) + "\nDo /team disband to remove the request");
				return;
			}
		}
		else
		{
			if (Bukkit.getPlayer(creator) == null)
			{
				return;
			}
			
			if (Bukkit.getPlayer(teammate) == null)
			{
				Bukkit.getPlayer(creator).sendMessage(ChatColor.RED + "Cannot find player " + teammate);
				return;
			}
			
			String message = "{'text':'" + ChatColor.GOLD + "TEAM REQUEST " + ChatColor.GREEN + "from " + creator + ", Click here to Accept" + "','color':'dark_red','clickEvent':{'action':'run_command','value':'/team accept " + creator + "'},'hoverEvent':{'action':'show_text','value':{'text':'','extra':[{'text':'Click to accept team request from " + creator + "','color':'gold'}]}}}";
			
			VisualEffects.sendJSONMessage(Bukkit.getPlayer(teammate), message);
			
			Bukkit.getPlayer(creator).sendMessage(ChatColor.GREEN + "Team request sent to " + teammate);
			
			requests.put(creator, new Request(teammate));
		}
	}
	
	public void acceptRequest(String player, String requestor)
	{
		if (!requests.containsKey(requestor))
		{
			if (Bukkit.getPlayer(player) != null)
			{
				Bukkit.getPlayer(player).sendMessage(ChatColor.RED + "That player has not sent you a request");
				return;
			}
			
			return;
		}
		
		Request playerSentRequest = requests.get(requestor);
		
		if (playerSentRequest.getPlayer().equals(player))
		{
			requests.remove(requestor);
			createTeam(requestor, player);
		}
		else
		{
			Bukkit.getPlayer(player).sendMessage(ChatColor.RED + "That player has not sent you a request");
			return;
		}
	}
	
	boolean[] alreadySelected = new boolean[48];
	
	private String getTeamColor(Boolean forced)
	{
		Random generator = new Random();
		
		boolean found = false;
		
		do
		{
			int selector = generator.nextInt(480);
			
			if (selector <= 10)
			{
				if (alreadySelected[0] == false || forced)
				{
					found = true;
					alreadySelected[0] = true;
					return "" + ChatColor.AQUA;
				}
			}
//			else if (selector <=20)
//			{
//				if (alreadySelected[1] == false)
//				{
//					found = true;
//					alreadySelected[1] = true;
//					return "" + ChatColor.BLACK;
//				}
//			}
			else if (selector <=30)
			{
				if (alreadySelected[2] == false || forced)
				{
					found = true;
					alreadySelected[2] = true;
					return "" + ChatColor.BLUE;
				}
			}
			else if (selector <=40)
			{
				if (alreadySelected[3] == false || forced)
				{
					found = true;
					alreadySelected[3] = true;
					return "" + ChatColor.DARK_AQUA;
				}
			}
			else if (selector <=50)
			{
				if (alreadySelected[4] == false || forced)
				{
					found = true;
					alreadySelected[4] = true;
					return "" + ChatColor.DARK_BLUE;
				}
			}
			else if (selector <=60)
			{
				if (alreadySelected[5] == false || forced)
				{
					found = true;
					alreadySelected[5] = true;
					return "" + ChatColor.DARK_GRAY;
				}
			}
			else if (selector <=70)
			{
				if (alreadySelected[6] == false || forced)
				{
					found = true;
					alreadySelected[6] = true;
					return "" + ChatColor.DARK_GREEN;
				}
			}
			else if (selector <=80)
			{
				if (alreadySelected[7] == false || forced)
				{
					found = true;
					alreadySelected[7] = true;
					return "" + ChatColor.DARK_PURPLE;
				}
			}
			else if (selector <=90)
			{
				if (alreadySelected[8] == false || forced)
				{
					found = true;
					alreadySelected[8] = true;
					return "" + ChatColor.DARK_RED;
				}
			}
			else if (selector <=100)
			{
				if (alreadySelected[9] == false || forced)
				{
					found = true;
					alreadySelected[9] = true;
					return "" + ChatColor.GOLD;
				}
			}
			else if (selector <=110)
			{
				if (alreadySelected[10] == false || forced)
				{
					found = true;
					alreadySelected[10] = true;
					return "" + ChatColor.GRAY;
				}
			}
			else if (selector <=120)
			{
				if (alreadySelected[11] == false || forced)
				{
					found = true;
					alreadySelected[11] = true;
					return "" + ChatColor.GREEN;
				}
			}
			else if (selector <=130)
			{
				if (alreadySelected[12] == false || forced)
				{
					found = true;
					alreadySelected[12] = true;
					return "" + ChatColor.RED;
				}
			}
			else if (selector <=140)
			{
				if (alreadySelected[13] == false || forced)
				{
					found = true;
					alreadySelected[13] = true;
					return "" + ChatColor.WHITE;
				}
			}
			else if (selector <=150)
			{
				if (alreadySelected[14] == false || forced)
				{
					found = true;
					alreadySelected[14] = true;
					return "" + ChatColor.YELLOW;
				}
			}
			else if (selector <=160)
			{
				if (alreadySelected[15] == false || forced)
				{
					found = true;
					alreadySelected[15] = true;
					return "" + ChatColor.AQUA + ChatColor.BOLD;
				}
			}
//			else if (selector <=170)
//			{
//				if (alreadySelected[16] == false)
//				{
//					found = true;
//					alreadySelected[16] = true;
//					return "" + ChatColor.BLACK + ChatColor.BOLD;
//				}
//			}
			else if (selector <=180)
			{
				if (alreadySelected[17] == false || forced)
				{
					found = true;
					alreadySelected[17] = true;
					return "" + ChatColor.BLUE + ChatColor.BOLD;
				}
			}
			else if (selector <=190)
			{
				if (alreadySelected[18] == false || forced)
				{
					found = true;
					alreadySelected[18] = true;
					return "" + ChatColor.DARK_AQUA + ChatColor.BOLD;
				}
			}
			else if (selector <=200)
			{
				if (alreadySelected[19] == false || forced)
				{
					found = true;
					alreadySelected[19] = true;
					return "" + ChatColor.DARK_BLUE + ChatColor.BOLD;
				}
			}
			else if (selector <=210)
			{
				if (alreadySelected[20] == false || forced)
				{
					found = true;
					alreadySelected[20] = true;
					return "" + ChatColor.DARK_GRAY + ChatColor.BOLD;
				}
			}
			else if (selector <=220)
			{
				if (alreadySelected[21] == false || forced)
				{
					found = true;
					alreadySelected[21] = true;
					return "" + ChatColor.DARK_GREEN + ChatColor.BOLD;
				}
			}
			else if (selector <=230)
			{
				if (alreadySelected[22] == false || forced)
				{
					found = true;
					alreadySelected[22] = true;
					return "" + ChatColor.DARK_PURPLE + ChatColor.BOLD;
				}
			}
			else if (selector <=240)
			{
				if (alreadySelected[23] == false || forced)
				{
					found = true;
					alreadySelected[23] = true;
					return "" + ChatColor.DARK_RED + ChatColor.BOLD;
				}
			}
			else if (selector <=250)
			{
				if (alreadySelected[24] == false || forced)
				{
					found = true;
					alreadySelected[24] = true;
					return "" + ChatColor.GOLD + ChatColor.BOLD;
				}
			}
			else if (selector <=260)
			{
				if (alreadySelected[25] == false || forced)
				{
					found = true;
					alreadySelected[25] = true;
					return "" + ChatColor.GRAY + ChatColor.BOLD;
				}
			}
			else if (selector <=270)
			{
				if (alreadySelected[26] == false || forced)
				{
					found = true;
					alreadySelected[26] = true;
					return "" + ChatColor.GREEN + ChatColor.BOLD;
				}
			}
			else if (selector <=280)
			{
				if (alreadySelected[27] == false || forced)
				{
					found = true;
					alreadySelected[27] = true;
					return "" + ChatColor.LIGHT_PURPLE + ChatColor.BOLD;
				}
			}
			else if (selector <=290)
			{
				if (alreadySelected[28] == false || forced)
				{
					found = true;
					alreadySelected[28] = true;
					return "" + ChatColor.LIGHT_PURPLE;
				}
			}
			else if (selector <=300)
			{
				if (alreadySelected[29] == false || forced)
				{
					found = true;
					alreadySelected[29] = true;
					return "" + ChatColor.RED + ChatColor.BOLD;
				}
			}
			else if (selector <=310)
			{
				if (alreadySelected[30] == false || forced)
				{
					found = true;
					alreadySelected[30] = true;
					return "" + ChatColor.WHITE + ChatColor.BOLD;
				}
			}
			else if (selector <=320)
			{
				if (alreadySelected[31] == false || forced)
				{
					found = true;
					alreadySelected[31] = true;
					return "" + ChatColor.YELLOW + ChatColor.BOLD;
				}
			}
			else if (selector <=330)
			{
				if (alreadySelected[32] == false || forced)
				{
					found = true;
					alreadySelected[32] = true;
					return "" + ChatColor.AQUA + ChatColor.ITALIC;
				}
			}
//			else if (selector <=340)
//			{
//				if (alreadySelected[33] == false)
//				{
//					found = true;
//					alreadySelected[33] = true;
//					return "" + ChatColor.BLACK + ChatColor.ITALIC;
//				}
//			}
			else if (selector <=350)
			{
				if (alreadySelected[34] == false || forced)
				{
					found = true;
					alreadySelected[34] = true;
					return "" + ChatColor.BLUE + ChatColor.ITALIC;
				}
			}
			else if (selector <=360)
			{
				if (alreadySelected[35] == false || forced)
				{
					found = true;
					alreadySelected[35] = true;
					return "" + ChatColor.DARK_AQUA + ChatColor.ITALIC;
				}
			}
			else if (selector <=370)
			{
				if (alreadySelected[36] == false || forced)
				{
					found = true;
					alreadySelected[36] = true;
					return "" + ChatColor.DARK_BLUE + ChatColor.ITALIC;
				}
			}
			else if (selector <=380)
			{
				if (alreadySelected[37] == false || forced)
				{
					found = true;
					alreadySelected[37] = true;
					return "" + ChatColor.DARK_GRAY + ChatColor.ITALIC;
				}
			}
			else if (selector <=390)
			{
				if (alreadySelected[38] == false || forced)
				{
					found = true;
					alreadySelected[38] = true;
					return "" + ChatColor.DARK_GREEN + ChatColor.ITALIC;
				}
			}
			else if (selector <=400)
			{
				if (alreadySelected[39] == false || forced)
				{
					found = true;
					alreadySelected[39] = true;
					return "" + ChatColor.DARK_PURPLE + ChatColor.ITALIC;
				}
			}
			else if (selector <=410)
			{
				if (alreadySelected[40] == false || forced)
				{
					found = true;
					alreadySelected[40] = true;
					return "" + ChatColor.DARK_RED + ChatColor.ITALIC;
				}
			}
			else if (selector <=420)
			{
				if (alreadySelected[41] == false || forced)
				{
					found = true;
					alreadySelected[41] = true;
					return "" + ChatColor.GOLD + ChatColor.ITALIC;
				}
			}
			else if (selector <=430)
			{
				if (alreadySelected[42] == false || forced)
				{
					found = true;
					alreadySelected[42] = true;
					return "" + ChatColor.GRAY + ChatColor.ITALIC;
				}
			}
			else if (selector <=440)
			{
				if (alreadySelected[43] == false || forced)
				{
					found = true;
					alreadySelected[43] = true;
					return "" + ChatColor.GREEN + ChatColor.ITALIC;
				}
			}
			else if (selector <=450)
			{
				if (alreadySelected[44] == false || forced)
				{
					found = true;
					alreadySelected[44] = true;
					return "" + ChatColor.LIGHT_PURPLE + ChatColor.ITALIC;
				}
			}
			else if (selector <=460)
			{
				if (alreadySelected[45] == false || forced)
				{
					found = true;
					alreadySelected[45] = true;
					return "" + ChatColor.RED + ChatColor.ITALIC;
				}
			}
			else if (selector <=470)
			{
				if (alreadySelected[46] == false || forced)
				{
					found = true;
					alreadySelected[46] = true;
					return "" + ChatColor.WHITE + ChatColor.ITALIC;
				}
			}
			else if (selector <=480)
			{
				if (alreadySelected[47] == false || forced)
				{
					found = true;
					alreadySelected[47] = true;
					return "" + ChatColor.YELLOW + ChatColor.ITALIC;
				}
			}
		} while (!found);
		
		return "" + ChatColor.AQUA;
	}
}
