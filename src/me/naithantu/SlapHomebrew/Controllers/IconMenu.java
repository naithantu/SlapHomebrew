package me.naithantu.SlapHomebrew.Controllers;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

import java.util.Arrays;

public class IconMenu extends AbstractController implements Listener {

	private String name;
	private int size;
	private OptionClickEventHandler handler;

	private String[] optionNames;
	private ItemStack[] optionIcons;
	private String[] optionCommands;
	private String playerName;

	public IconMenu(String name, int size, OptionClickEventHandler handler) {
		this.name = name;
		this.size = size;
		this.handler = handler;
		this.playerName = null;
		this.optionNames = new String[size];
		this.optionIcons = new ItemStack[size];
		this.optionCommands = new String[size];
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}
	
	public IconMenu(String name, int size, OptionClickEventHandler handler, String playerName) {
		this.name = name;
		this.size = size;
		this.handler = handler;
		this.playerName = playerName;
		this.optionNames = new String[size];
		this.optionIcons = new ItemStack[size];
		this.optionCommands = new String[size];
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}
	
	public IconMenu setOption(int position, ItemStack icon, String name, String... info) {
		optionNames[position] = name;
		optionIcons[position] = setItemNameAndLore(icon, name, info);
		return this;
	}
	
	public IconMenu setOption(String command, int position, ItemStack icon, String name, String... info) {
		optionNames[position] = name;
		optionCommands[position] = command;
		optionIcons[position] = setItemNameAndLore(icon, name, info);
		return this;
	}
	
	public void open(Player player) {
		Inventory inventory = Bukkit.createInventory(player, size, name);
		for (int i = 0; i < optionIcons.length; i++) {
			if (optionIcons[i] != null) {
				inventory.setItem(i, optionIcons[i]);
			}
		}
		player.openInventory(inventory);
	}

	public void destroy() {
		HandlerList.unregisterAll(this);
		handler = null;
		plugin = null;
		optionNames = null;
		optionIcons = null;
	}

	@EventHandler(priority = EventPriority.MONITOR)
	void onInventoryClick(InventoryClickEvent event) {
		if (event.getInventory().getTitle().equals(name)) {
			if(playerName == null|| event.getWhoClicked().getName().equals(playerName)){
				event.setCancelled(true);
				int slot = event.getRawSlot();
				String clickedName;
				String command;
				if (slot >= 0 && slot < size && optionIcons[slot] != null) {
					clickedName = optionNames[slot];
					command = optionCommands[slot];
					Plugin plugin = this.plugin;
					OptionClickEvent e = new OptionClickEvent((Player) event.getWhoClicked(), slot, clickedName, optionIcons[slot], command);
					handler.onOptionClick(e);
					if (e.willClose()) {
						final Player p = (Player) event.getWhoClicked();
						Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
							public void run() {
								p.closeInventory();
							}
						}, 1);
					}
					if (e.willDestroy()) {
						destroy();
					}
				}
			}
		}
	}

	public interface OptionClickEventHandler {
		public void onOptionClick(OptionClickEvent event);
	}

	public class OptionClickEvent {
		private Player player;
		private int position;
		private String name;
		private String command;
		private boolean close;
		private boolean destroy;
		ItemStack itemClicked;

		public OptionClickEvent(Player player, int position, String name, ItemStack itemClicked, String command) {
			this.player = player;
			this.position = position;
			this.name = name;
			this.itemClicked = itemClicked;
			this.command = command;
			this.close = true;
			this.destroy = false;
		}

		public Player getPlayer() {
			return player;
		}

		public int getPosition() {
			return position;
		}

		public String getName() {
			return name;
		}
		
		public String getCommand() {
			return command;
		}

		public ItemStack getItemClicked() {
			return itemClicked;
		}

		public boolean willClose() {
			return close;
		}

		public boolean willDestroy() {
			return destroy;
		}

		public void setWillClose(boolean close) {
			this.close = close;
		}

		public void setWillDestroy(boolean destroy) {
			this.destroy = destroy;
		}
	}

	private ItemStack setItemNameAndLore(ItemStack item, String name, String[] lore) {
		ItemMeta im = item.getItemMeta();
		im.setDisplayName(name);
		if (lore != null) {
			im.setLore(Arrays.asList(lore));
		}
		item.setItemMeta(im);
		return item;
	}
	
    @Override
    public void shutdown() {
    	//Not needed
    }

}
