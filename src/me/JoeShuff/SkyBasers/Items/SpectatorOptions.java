/*
 * Bukkit Plugin - SkyBasers
 * For use on The Justice Network
 * 
 * @author - Joseph Shufflebotham
 * 
 * © Copyright 2016 
 */
package me.JoeShuff.SkyBasers.Items;

import java.util.Arrays;

import me.JoeShuff.SkyBasers.SkyBasers;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class SpectatorOptions implements Listener {
	
	private SkyBasers plugin;
	
	public SpectatorOptions(SkyBasers plugin)
	{
		this.plugin = plugin;
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}
	
	@EventHandler
	public void playerInteract(PlayerInteractEvent event)
	{
		if (event.getPlayer().getItemInHand().getItemMeta() == null)
		{
			return;
		}
		
		if (!event.getPlayer().getItemInHand().getItemMeta().hasLore())
		{
			return;
		}
		
		if (!(event.getPlayer().getItemInHand().getItemMeta().getLore().get(0).contains("Options")))
		{
			return;
		}
		
		if (event.getAction() != Action.PHYSICAL || event.getAction() == Action.LEFT_CLICK_BLOCK)
		{
			if (event.getPlayer().getItemInHand().getType() == Material.BLAZE_POWDER)
			{
				generateInv(event.getPlayer());
			}
		}
	}
	
	public void ShowMenu(Player p,Inventory inv)
	{
		p.openInventory(inv);
	}
	
	public static void getBlaze(Player player)
	{
		ItemStack blaze = new ItemStack(Material.BLAZE_POWDER);
		
		ItemMeta bMeta = blaze.getItemMeta();
		bMeta.setDisplayName(ChatColor.BLUE + "Options");
		bMeta.setLore(Arrays.asList(ChatColor.AQUA + "Options for spectators", ChatColor.GREEN + "Right Click"));
		blaze.setItemMeta(bMeta);
		
		player.getInventory().setItem(4, blaze);
	}
	
	public void generateInv(Player p)
	{		
		
		Inventory inv = Bukkit.getServer().createInventory(null, 18, "Spectator Options");
	
		ItemStack noSpeed = new ItemStack(Material.BARRIER,1);
		ItemMeta speed0Meta = noSpeed.getItemMeta();
		
		speed0Meta.setDisplayName(ChatColor.RED + "No Speed");
		speed0Meta.setLore(Arrays.asList(ChatColor.GREEN + "Click to remove speed"));
		
		noSpeed.setItemMeta(speed0Meta);
		
		ItemStack speed1 = new ItemStack(Material.FEATHER,1);
		ItemMeta speed1Meta = speed1.getItemMeta();
		
		speed1Meta.setDisplayName(ChatColor.AQUA + "Speed I");
		speed1Meta.setLore(Arrays.asList(ChatColor.GREEN + "Click to get speed I"));
		
		speed1.setItemMeta(speed1Meta);
		
		ItemStack speed2 = new ItemStack(Material.FEATHER,1);
		ItemMeta speed2Meta = speed2.getItemMeta();
		
		speed2Meta.setDisplayName(ChatColor.DARK_AQUA + "Speed II");
		speed2Meta.setLore(Arrays.asList(ChatColor.GREEN + "Click to get speed II"));
		
		speed2.setItemMeta(speed2Meta);
		
		ItemStack speed3 = new ItemStack(Material.FEATHER,1);
		ItemMeta speed3Meta = speed3.getItemMeta();
		
		speed3Meta.setDisplayName(ChatColor.BLUE + "Speed III");
		speed3Meta.setLore(Arrays.asList(ChatColor.GREEN + "Click to get speed III"));
		
		speed3.setItemMeta(speed3Meta);
		
		ItemStack speed4 = new ItemStack(Material.FEATHER,1);
		ItemMeta speed4Meta = speed4.getItemMeta();
		
		speed4Meta.setDisplayName(ChatColor.DARK_BLUE + "Speed IV");
		speed4Meta.setLore(Arrays.asList(ChatColor.GREEN + "Click to get speed IV"));
		
		speed4.setItemMeta(speed4Meta);
		
		ItemStack nvOn = new ItemStack(Material.EYE_OF_ENDER,1);
		ItemMeta onMeta = nvOn.getItemMeta();
		
		onMeta.setDisplayName(ChatColor.GREEN + "Enable Night Vision");
		onMeta.setLore(Arrays.asList(ChatColor.GOLD + "Click to Enable night vision!"));
		
		nvOn.setItemMeta(onMeta);
		
		ItemStack nvOff = new ItemStack(Material.ENDER_PEARL,1);
		ItemMeta offMeta = nvOff.getItemMeta();
		
		offMeta.setDisplayName(ChatColor.RED + "Disable Night Vision");
		offMeta.setLore(Arrays.asList(ChatColor.GOLD + "Click to Disable night vision!"));
		
		nvOff.setItemMeta(offMeta);
		
		inv.setItem(0, noSpeed);
		inv.setItem(2, speed1);
		inv.setItem(4, speed2);
		inv.setItem(6, speed3);
		inv.setItem(8, speed4);
		inv.setItem(10, nvOn);
		inv.setItem(16, nvOff);
		
		
		ShowMenu(p, inv);
	}
	
	@EventHandler
	public void clickInventory(InventoryClickEvent event)
	{
		if (!(event.getInventory().getName().contains("Spectator Options")))
		{
			return;
		}
		
		try {
			if (event.getCurrentItem().getItemMeta() == null) return;
		} catch (NullPointerException e) 
		{
			return;
		}
		
		event.setCancelled(true);
		
		if (event.getCurrentItem().getItemMeta().getDisplayName().contains("No Speed"))
		{
			event.getWhoClicked().removePotionEffect(PotionEffectType.SPEED);
			event.getWhoClicked().closeInventory();
		}
		
		if (event.getCurrentItem().getItemMeta().getDisplayName().contains("Speed I"))
		{
			event.getWhoClicked().removePotionEffect(PotionEffectType.SPEED);
			event.getWhoClicked().addPotionEffect(new PotionEffect(PotionEffectType.SPEED,100000000,0));
			event.getWhoClicked().closeInventory();
		}
		
		if (event.getCurrentItem().getItemMeta().getDisplayName().contains("Speed II"))
		{
			event.getWhoClicked().removePotionEffect(PotionEffectType.SPEED);
			event.getWhoClicked().addPotionEffect(new PotionEffect(PotionEffectType.SPEED,100000000,1));
			event.getWhoClicked().closeInventory();
		}
		
		if (event.getCurrentItem().getItemMeta().getDisplayName().contains("Speed III"))
		{
			event.getWhoClicked().removePotionEffect(PotionEffectType.SPEED);
			event.getWhoClicked().addPotionEffect(new PotionEffect(PotionEffectType.SPEED,100000000,2));
			event.getWhoClicked().closeInventory();
		}
		
		if (event.getCurrentItem().getItemMeta().getDisplayName().contains("Speed IV"))
		{
			event.getWhoClicked().removePotionEffect(PotionEffectType.SPEED);
			event.getWhoClicked().addPotionEffect(new PotionEffect(PotionEffectType.SPEED,100000000,3));
			event.getWhoClicked().closeInventory();
		}
		
		if (event.getCurrentItem().getItemMeta().getDisplayName().contains("Disable"))
		{
			event.getWhoClicked().removePotionEffect(PotionEffectType.NIGHT_VISION);
			event.getWhoClicked().closeInventory();
		}
		
		if (event.getCurrentItem().getItemMeta().getDisplayName().contains("Enable"))
		{
			event.getWhoClicked().addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION,100000000,0));
			event.getWhoClicked().closeInventory();
		}
		
		return;
	}
}
