package me.naithantu.SlapHomebrew.Controllers;

import java.sql.ResultSet;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import com.earth2me.essentials.User;
import com.earth2me.essentials.UserMap;
import com.nyancraft.reportrts.ReportRTS;
import com.nyancraft.reportrts.persistence.Database;
import com.nyancraft.reportrts.persistence.DatabaseManager;

import ru.tehkode.permissions.PermissionManager;
import ru.tehkode.permissions.PermissionUser;
import ru.tehkode.permissions.bukkit.PermissionsEx;

import me.naithantu.SlapHomebrew.SlapHomebrew;
import me.naithantu.SlapHomebrew.Controllers.TabController.TabGroup;
import me.naithantu.SlapHomebrew.Storage.YamlStorage;
import me.naithantu.SlapHomebrew.Util.Util;

public class PlayerLogger {

	private SlapHomebrew plugin;
	
	private YamlStorage logYML;
	private FileConfiguration logConfig;
	
	private SimpleDateFormat format;
	private SimpleDateFormat onlineFormat;
	
	private Comparator<TimePlayer> comp;
	
	private HashMap<String, Boolean> minechatMoved;
	
	private HashMap<String, Long> lastActivity;
	
	private HashMap<String, String> doubleMessage;
	
	private HashSet<String> commandSpy;
	
	private HashSet<String> doingCommand;
	private HashSet<String> suicides;
	
	private Database db;
	private boolean reportRTSfound;
	private HashMap<String, Integer> modreqs;
	
	private HashMap<String, SavedPlayer> modInventories;
	
	
	public PlayerLogger(SlapHomebrew plugin) {
		this.plugin = plugin;
		logYML = new YamlStorage(plugin, "playerlog");
		logConfig = logYML.getConfig();
		onlineFormat = new SimpleDateFormat("dd:HH:mm:ss");
		onlineFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
		format = new SimpleDateFormat("dd-MM-yyyy");
		format.setTimeZone(TimeZone.getTimeZone("GMT"));
		minechatMoved = new HashMap<>();
		doubleMessage = new HashMap<>();
		lastActivity = new HashMap<>();
		modreqs = new HashMap<>();
		ReportRTS rts = (ReportRTS) plugin.getServer().getPluginManager().getPlugin("ReportRTS");
		reportRTSfound = false;
		if (rts != null) {
			reportRTSfound = true;
			db = DatabaseManager.getDatabase();
		}
		createComp();
		
		commandSpy = new HashSet<>();
		List<String> list = logConfig.getStringList("commandspy");
		for (String player : list) {
			commandSpy.add(player);
		}
		
		suicides = new HashSet<>();
		doingCommand = new HashSet<>();
		
		modInventories = new HashMap<>();
		
		onEnable();
	}
	
	/*
	 * SET IN CONFIG
	 */
	public void setLoginTime(final String playername) {
		if (PermissionsEx.getUser(playername).has("reportrts.mod") && reportRTSfound) {
			Util.runASync(plugin, new Runnable() {
				
				@Override
				public void run() {
					try {
						ResultSet rs = db.getHandledBy(playername);
	                    int totalCompleted = 0;
	                    while(rs.next()){
	                        if(rs.getInt("status") == 3) totalCompleted++;
	                    }
	                    modreqs.put(playername, totalCompleted);
					} catch (Exception e) {
						
					}
				}
			});
		}
		logConfig.set("time." + playername + "." + format.format(new Date()) + ".login", System.currentTimeMillis()); 
		save();
	}
	
	public void setLogoutTime(final String playername) {
		long loginTime = 0; long timeToday = 0; 
		long currentTime = System.currentTimeMillis();
		final String todayString = "time." + playername + "." + format.format(new Date(currentTime)) + ".";
		if (logConfig.contains(todayString + "login")) {
			loginTime = logConfig.getLong(todayString + "login");
			logConfig.set(todayString + "login", null);
		} else {
			String yesterdayString = "time." + playername + "." + format.format(new Date(currentTime - 1000*60*60*24 + 1000)) + ".login";
			loginTime = logConfig.getLong(yesterdayString);
			logConfig.set(yesterdayString, null);
		}
		
		if (logConfig.contains(todayString + "timetoday")) {
			timeToday = logConfig.getLong(todayString + "timetoday");
		}
		
		if (loginTime != 0) {
			Long timePlayed = currentTime - loginTime;
			timePlayed = timePlayed + timeToday;
			logConfig.set(todayString + "timetoday", timePlayed);
		}
		if (PermissionsEx.getUser(playername).has("reportrts.mod") && reportRTSfound) {
			if (plugin.isEnabled()) {
			Util.runASync(plugin, new Runnable() {
				
				@Override
				public void run() {
					logoutTask(playername, todayString);
				}
			});
			} else {
				logoutTask(playername, todayString);
			}
		}
		save();
	}
	
	private void logoutTask(String playername, String todayString) {
		try {
			ResultSet rs = db.getHandledBy(playername);
            int totalCompleted = 0;
            while(rs.next()){
                if(rs.getInt("status") == 3) totalCompleted++;
            }
            if (!modreqs.containsKey(playername)) return;
            int nrOfModreqs = totalCompleted - modreqs.get(playername);
            modreqs.remove(playername);
            if (logConfig.contains(todayString + "modreqs")) {
            	nrOfModreqs = nrOfModreqs + logConfig.getInt(todayString + "modreqs");
            }
            logConfig.set(todayString + "modreqs", nrOfModreqs);
            save();
		} catch (Exception e) {
			
		}
	}
	
	
	/*
	 * TIME CALCULATORS
	 */
	private long getPlayTime(String playername, boolean isOnline) {
		ConfigurationSection playerConfig = logConfig.getConfigurationSection("time." + playername);
		long timePlayed = -1;
		if (playerConfig != null) {
			timePlayed = 0;
			Set<String> keys = playerConfig.getKeys(false);
			for (String key : keys) {
				Long timeToday = playerConfig.getLong(key + ".timetoday");
				timePlayed = timePlayed + timeToday;
			}
			if (isOnline) {
				long currentTime = System.currentTimeMillis();
				timePlayed = timePlayed + getOnlinePlayTime(playername, playerConfig, currentTime);
			}
		}
		return timePlayed;
	}
	
	private long getPlayTime(String playername, boolean isOnline, Date fromDate) {
		ConfigurationSection playerConfig = logConfig.getConfigurationSection("time." + playername);
		long timePlayed = -1;
		if (playerConfig != null) {
			timePlayed = 0;
			long currentTime = System.currentTimeMillis();
			Set<String> keys = playerConfig.getKeys(false);
			for (String key : keys) {
				try {
					if (format.parse(key).after(fromDate)) {
						timePlayed = timePlayed + playerConfig.getLong(key + ".timetoday");
					}
				} catch (Exception e) {}
			}
			if (isOnline) {
				timePlayed = timePlayed + getOnlinePlayTime(playername, playerConfig, currentTime);
			}
		}
		return timePlayed;
	}
	
	private long getOnlinePlayTime(String playername, ConfigurationSection section, long currentTime) {
		long timePlayed = 0;
		String todayString = format.format(new Date(currentTime)) + ".login";
		if (section.contains(todayString)) {
			timePlayed = timePlayed + (currentTime - section.getLong(todayString));
		} else {
			String yesterdayString = format.format(new Date(currentTime - 1000 * 60 * 60 * 24 + 1000)) + ".login";
			if (section.contains(yesterdayString)) {
				timePlayed = timePlayed + (currentTime - section.getLong(yesterdayString));
			}
		}
		return timePlayed;
	}
	
	
	/*
	 * COMMANDS
	 */
	public void getOnlineTime(final CommandSender sender, final String playername, final boolean isOnline) {
		Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
			
			@Override
			public void run() {
				long timePlayed = getPlayTime(playername, isOnline);
				if (timePlayed < 1) {
					sender.sendMessage(ChatColor.RED + "This player hasn't been online since 11th of august 2013");
				} else {
					sender.sendMessage(Util.getHeader() + playername + " has played: " + Util.getTimePlayedString(timePlayed) + ".");
				}
			}
		});
	}
	
	public void getOnlineTime(final CommandSender sender, final String playername, final boolean isOnline, final Date from) {
		Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
			
			@Override
			public void run() {
				long timePlayed = getPlayTime(playername, isOnline, from);
				if (timePlayed < 1) {
					sender.sendMessage(ChatColor.RED + "This player hasn't been online since " + format.format(from));
				} else {
					sender.sendMessage(Util.getHeader() + playername + " has played: " + Util.getTimePlayedString(timePlayed) + ".");
				}
			}
		});
	}
	
	public String getOnlineTime(String playername, boolean isOnline, Date from, Date till) {
		return ChatColor.RED + "Not supported yet.";
	}
	
	public void getTimeList(final CommandSender sender, final boolean staff, final int nr, final Date fromDate) {
		Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
			
			@Override
			public void run() {
				ConfigurationSection pConfig = logConfig.getConfigurationSection("time");
				if (pConfig != null) {
					String msgString = Util.getHeader() + "Checking time since 11th of August";
					if (fromDate != null) {
						msgString = msgString + " since " + format.format(fromDate);
					}
					sender.sendMessage(msgString + "..");
					int sendPlayers = 0;
					UserMap eMap = plugin.getEssentials().getUserMap();
					ArrayList<TimePlayer> players = new ArrayList<>();
					if (staff) {
						PermissionManager pManager = PermissionsEx.getPermissionManager();
						String[] groups = new String[]{"SuperAdmin", "Admin", "VIPGuide", "Guide", "Mod"};
						for (String group : groups) {
							for (PermissionUser user : pManager.getGroup(group).getUsers()) {
								User u = eMap.getUser(user.getName());
								if (u != null) {
									String playername = u.getName();
									if (fromDate == null) {
										players.add(new TimePlayer(playername, getPlayTime(playername, u.isOnline())));
									} else {
										players.add(new TimePlayer(playername, getPlayTime(playername, u.isOnline(), fromDate)));
									}
								}
							}
						}
						sendPlayers = players.size();
					} else {
						for (String player : pConfig.getKeys(false)) {
							User u = eMap.getUser(player);
							if (u != null) {
								String playername = u.getName();
								if (fromDate == null) {
									players.add(new TimePlayer(playername, getPlayTime(playername, u.isOnline())));
								} else {
									players.add(new TimePlayer(playername, getPlayTime(playername, u.isOnline(), fromDate)));
								}
							}
						}
						sendPlayers = nr;
					}
					Collections.sort(players, comp);
					int x = 0; int arraySize = players.size();
					while (x < sendPlayers && x < arraySize) {
						TimePlayer p = players.get(x);
						sender.sendMessage(ChatColor.GREEN + String.valueOf(x + 1) + ChatColor.GRAY + "-" + p.playername + ": " + ChatColor.WHITE + Util.getTimePlayedString(p.timePlayed));
						x++;
					}
				} else {
					Util.badMsg(sender, "No times found.");
				}
				
				//Remove from doingCommand
				if (doingCommand.contains(sender.getName())) {
					doingCommand.remove(sender.getName());
				}
			}
		});
	}
	
	private class TimePlayer {
		String playername;
		long timePlayed;
		
		public TimePlayer(String playerName, long timePlayed) {
			this.playername = playerName;
			this.timePlayed = timePlayed;
		}
	}
	
	private void createComp() {
		comp = new Comparator<TimePlayer>() {
			
			@Override
			public int compare(TimePlayer o1, TimePlayer o2) {
				if (o1.timePlayed < o2.timePlayed) return 1;
				else if (o1.timePlayed > o2.timePlayed) return -1;
				return 0;
			}
		};
	}
	
	private void save(){
		logYML.saveConfig();
	}
	
	public void onEnable(){
		for (Player onlinePlayer : plugin.getServer().getOnlinePlayers()) {
			setLoginTime(onlinePlayer.getName());
			setMoved(onlinePlayer.getName(), true);
		}
	}

	public void onDisable(){
		for (Player onlinePlayer : plugin.getServer().getOnlinePlayers()) {
			setLogoutTime(onlinePlayer.getName());
		}
	}
	
	public Date parseDate(String dateString){
		Date date = null;
		try {
			date = format.parse(dateString);
		} catch (ParseException e) {}
		return date;
	}
	
	/*
	 * Playtime commands
	 * -Commands can be done by any player
	 */
	/**
	 * Send a player his playtime
	 * @param p The player
	 */
	public void sendPlaytime(final Player p) {
		if (isRunningCommand(p)) return;
		final String playername = p.getName();
		doingCommand.add(playername);
		Util.runASync(plugin, new Runnable() {
			@Override
			public void run() {
				long timePlayed = getPlayTime(playername, p.isOnline());
				p.sendMessage(Util.getHeader() + "You have played " + Util.getTimePlayedString(timePlayed) + " since 11th of August.");
				doingCommand.remove(playername);
			}
		});
	}
	
	/**
	 * Send a list of the top playtimes to the Player
	 * @param p The player
	 */
	public void sendPlaytimeList(final Player p) {
		if (isRunningCommand(p)) return;
		final String playername = p.getName();
		doingCommand.add(playername);
		getTimeList(p, false, 10, null);
	}
	
	
	/*
	 * Minechat prevention
	 */
	public boolean hasMoved(String playername) {
		if (minechatMoved == null || playername == null) return false;
		return minechatMoved.get(playername);
	}
	
	public void setMoved(String playername, boolean moved) {
		minechatMoved.put(playername, moved);
	}
	
	public void joinedMinechatChecker(Player p) {
		if (!p.hasPermission("slaphomebrew.staff")) {
			setMoved(p.getName(), false);
		} else {
			setMoved(p.getName(), true); //Staff
		}
	}
	
	public void removeFromMoved(String playername) {
		minechatMoved.remove(playername);
	}
	
	public void sendNotMovedMessage(Player p) {
		p.sendMessage(ChatColor.GRAY + "You're not allowed to do commands/chat until you have moved.");
	}
	
	public boolean inMovedHashMap(String playername) {
		if (minechatMoved.containsKey(playername)) {
			return true;
		} else {
			return false;
		}
	}
	
	/*
	 * Log Promotions
	 */
	public void logPromotion(String sender, String player, String fromRank, String toRank, boolean promotion) {
		String today = format.format(new Date());
		List<String> promotions;
		if (logConfig.contains("promotions." + today)) {
			promotions = logConfig.getStringList("promotions." + today);
		} else {
			promotions = new ArrayList<>();
		}
		String promotionString;
		if (promotion) promotionString = " promoted ";
		else promotionString = " demoted ";
		promotions.add(sender + promotionString + player + " from " + fromRank + " to " + toRank);
		logConfig.set("promotions." + today, promotions);
		save();
	}
	
	public void getPromotions(CommandSender sender, int ammount) {
		ConfigurationSection promotions = logConfig.getConfigurationSection("promotions");
		if (promotions == null) {
			sender.sendMessage(ChatColor.RED + "No promotions.");
			return;
		}
		int x = 0;
		ArrayList<String> allPromotions = new ArrayList<>();
		for (String key : promotions.getKeys(false)) {
			for (String promotion : promotions.getStringList(key)) {
				allPromotions.add(0, "(" + key + ") " + promotion);
				x++;
				if (x >= ammount) {
					break;
				}
			}
			if (x >= ammount) {
				break;
			}
		}
		sender.sendMessage(allPromotions.toArray(new String[allPromotions.size()]));
	}
	
	/*
	 * Last Activity
	 */
	public void setLastActivity(String player) {
		lastActivity.put(player, System.currentTimeMillis());
	}
	
	public long getLastActivity(String player) {
		Long a = lastActivity.get(player);
		if (a == null) return 0;
		else return a;
	}
	
	public void removeFromLastActivity(String player) {
		lastActivity.remove(player);
	}
	
	
	/*
	 * Double message
	 */
	public void setFirstMessage(String player, String message) {
		PermissionUser user = PermissionsEx.getUser(player);
		String tag = "<" + user.getPrefix() + player + ChatColor.WHITE + "> ";
		doubleMessage.put(player, tag + message.replace("*--", " "));
	}
	
	public void sendSecondMessage(String player, String message) {
		plugin.getServer().broadcastMessage(ChatColor.translateAlternateColorCodes('&', doubleMessage.get(player) + message));
		doubleMessage.remove(player);
	}
	
	public boolean hasMessage(String player) {
		return doubleMessage.containsKey(player);
	}
	
	/*
	 * CommandSpy
	 */
	public void addCommandSpy(String player) {
		if (commandSpy.contains(player)) return;
		commandSpy.add(player);
		List<String> list = logConfig.getStringList("commandspy");
		list.add(player);
		logConfig.set("commandspy", list);
		save();
	}
	
	public void removeFromCommandSpy(String player) {
		if (!commandSpy.contains(player)) return;
		commandSpy.remove(player);
		List<String> list = logConfig.getStringList("commandspy");
		list.remove(player);
		logConfig.set("commandspy", list);
		save();		
	}
	
	public boolean isCommandSpy(String player) {
		return commandSpy.contains(player);
	}
	
	public void sendToCommandSpies(String player, String command, boolean social) {
		for (String spyname : commandSpy) {
			Player spy = plugin.getServer().getPlayer(spyname);
			if (spy != null) {
				if (!spyname.equals(player)) {
					if (social) spy.sendMessage(ChatColor.GRAY + "[Social] " + player + ": " + command);
					else spy.sendMessage(ChatColor.GRAY + "[CS] " + player + ": " + command);
				}
			}
		}
	}
	
	/*
	 * SuperAdmin control
	 */
	public boolean setSuperAdminGroup(String player, String group) {
		try {
			TabGroup tGroup = TabController.TabGroup.valueOf(group);
			if (tGroup == null) return false;
			logConfig.set("grouptab." + player, tGroup.toString());
			save();
			return true;
		} catch (Exception e) {
			return false;
		}
	}
	
	public TabGroup getSuperAdminGroup(String player) {
		String tabGroup = logConfig.getString("grouptab." + player);
		if (tabGroup == null) return null;
		return TabGroup.valueOf(tabGroup);
	}
	
	/*
	 *********************
	 *   Death control   *
	 *********************
	 */
	
	/**
	 * Add a death to a player
	 * @param type Type of death
	 * @param player The player
	 */
	public void addDeath(final DeathType type, final String player) {
		Util.runASync(plugin, new Runnable() {
			@Override
			public void run() {
				String path = "deaths." + player + "." + type.toString();
				int deaths = logConfig.getInt(path);
				deaths++;
				logConfig.set(path, deaths);
				save();
			}
		});
	}
	
	/**
	 * Send a player the number of deaths
	 * @param p The player
	 */
	public void getDeaths(final Player p) {
		final String playername = p.getName();
		if (isRunningCommand(p)) return;
		doingCommand.add(playername);
		Util.runASync(plugin, new Runnable() {
			@Override
			public void run() {
				String path = "deaths." + playername + ".";
				int suicide = logConfig.getInt(path + "suicide");
				int mob = logConfig.getInt(path + "mob");
				int player = logConfig.getInt(path + "player");
				int other = logConfig.getInt(path + "other");
				int totalDeaths = suicide + mob + player + other;
				if (totalDeaths == 0) {
					p.sendMessage(Util.getHeader() + "You don't have any deaths yet since this function has been added.");
				} else {
					p.sendMessage(Util.getHeader() + "Total Deaths: " + totalDeaths + " | Suicides: " + suicide + " | By Mobs: " + mob + " | By Players: " + player);
				}
				doingCommand.remove(playername);
			}
		});
	}
	
	/**
	 * Send a player the number of kills
	 * @param p The player
	 */
	public void getKills(final Player p) {
		if (isRunningCommand(p)) return;
		final String playername = p.getName();
		doingCommand.add(playername);
		Util.runASync(plugin, new Runnable() {
			@Override
			public void run() {
				int kills = logConfig.getInt("kills." + playername);
				if (kills == 0) {
					p.sendMessage(Util.getHeader() + "You don't have any kills yet since this function has been added.");
				} else {
					p.sendMessage(Util.getHeader() + "Kills: " + kills);
				}
				doingCommand.remove(playername);
			}
		});
	}
	
	public boolean isRunningCommand(Player p) {
		if (doingCommand.contains(p.getName())) {
			Util.badMsg(p, "Your previous command is still running.");
			return true;
		} else {
			return false;
		}
	}
		
	/**
	 * DeathTypes
	 */
	public enum DeathType {
		suicide, mob, player, other;
	}
	
	/**
	 * Check if a player just commited suicide
	 * @param playername The player
	 * @return commited suicide
	 */
	public boolean hasCommitedSuicide(String playername) {
		if (suicides.contains(playername)) {
			suicides.remove(playername);
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * Add a player to the suicide set
	 * @param playername The player
	 */
	public void commitsSuicide(String playername) {
		suicides.add(playername);
		addDeath(DeathType.suicide, playername);
	}
	
	/**
	 * Add a kill
	 * @param playername killer
	 */
	public void addKill(String playername) {
		String path = "kills." + playername;
		int kills = logConfig.getInt(path);
		kills++;
		logConfig.set(path, kills);
	}
	
	/*
	 *********************
	 *    AFK Control    *
	 *********************
	 */
	
	/**
	 * Add AFK Time to a players log
	 * @param player The playername
	 * @param time The time that the player spent AFK
	 */
	public void addAFKTime(final String player, final long time) {
		Util.runASync(plugin, new Runnable() {
			
			@Override
			public void run() {
				String path = "time." + player + "." + format.format(new Date()) + ".afk";
				long foundTime = 0;
				if (logConfig.contains(path)) {
					foundTime = logConfig.getLong(path);
				}
				logConfig.set(path, time + foundTime);
				save();
			}
		});
	}
	
	
	/*
	 *************************************
	 *    Mod+ Sonic Inventory Control   *
	 *************************************
	 */
	
	/**
	 * Save the inventory, wipe the inventory
	 * @param p the player
	 */
	public void toSonicWorld(Player p) {
		modInventories.put(p.getName(), new SavedPlayer(p));
	}
	
	/**
	 * Wipe the inventory & all stats, restore old inventory
	 * @param p the player
	 */
	public void fromSonicWorld(Player p) {
		SavedPlayer sP = modInventories.get(p.getName());
		if (sP != null) {
			sP.restorePlayer(p);
			modInventories.remove(p.getName());
		} else {
			p.getInventory().clear();
			Util.wipeAllPotionEffects(p);
		}
	}
	
	/**
	 * Stores information about a Mod (Inventory & XP)
	 * @author Stoux
	 */
	private class SavedPlayer {
		
		private ItemStack[] inventoryContent;
		private ItemStack[] armorContent;
		private int xpLevel;
		
		public SavedPlayer(Player p) {
			//Inventory
			PlayerInventory inv = p.getInventory();
			inventoryContent = inv.getContents().clone();
			armorContent = inv.getArmorContents().clone();
			inv.clear();
			xpLevel = p.getLevel();
			Util.wipeAllPotionEffects(p);
		}
		
		public void restorePlayer(Player p) {
			PlayerInventory inv = p.getInventory();
			inv.setArmorContents(armorContent);
			inv.setContents(inventoryContent);
			p.setLevel(xpLevel);
			Util.wipeAllPotionEffects(p);
		}
		
	}
	
}
