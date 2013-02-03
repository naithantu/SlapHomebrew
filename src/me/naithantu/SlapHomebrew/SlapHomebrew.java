package me.naithantu.SlapHomebrew;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.milkbowl.vault.Vault;
import net.milkbowl.vault.economy.Economy;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import ru.tehkode.permissions.PermissionUser;
import ru.tehkode.permissions.bukkit.PermissionsEx;

public class SlapHomebrew extends JavaPlugin {

	public SlapHomebrew plugin;
	public final Logger logger = Logger.getLogger("Minecraft");

	public final PlayerListener playerListener = new PlayerListener(this);
	public final VehicleListener vehicleListener = new VehicleListener(this);
	public final BlockListener blockListener = new BlockListener(this);

	public static HashMap<String, Integer> usedGrant = new HashMap<String, Integer>();
	public static HashMap<Integer, Integer> vipItems = new HashMap<Integer, Integer>();
	public static HashMap<String, Location> backDeath = new HashMap<String, Location>();
	public static HashMap<String, String> worldGuard = new HashMap<String, String>();
	public static HashMap<Integer, String> blackList = new HashMap<Integer, String>();
	public static HashMap<String, Integer> lottery = new HashMap<String, Integer>();
	HashMap<Integer, String> plots = new HashMap<Integer, String>();
	List<Integer> unfinishedPlots = new ArrayList<Integer>();
	HashMap<Integer, String> forumVip = new HashMap<Integer, String>();
	List<Integer> unfinishedForumVip = new ArrayList<Integer>();

	public HashMap<String, Boolean> safetyTp = new HashMap<String, Boolean>();
	public HashMap<String, Boolean> safetyTpUsed = new HashMap<String, Boolean>();

	private FileConfiguration dataConfig = null;
	private File dataConfigFile = null;
	private FileConfiguration vipConfig = null;
	private File vipConfigFile = null;

	public static HashSet<String> message = new HashSet<String>();
	public static HashSet<String> guides = new HashSet<String>();
	public static HashSet<String> tpBlocks = new HashSet<String>();
	public static HashSet<UUID> mCarts = new HashSet<UUID>();

	public static boolean allowCakeTp;
	public static boolean lotteryPlaying = false;
	public static boolean lotteryEnabled = true;

	boolean safetyTpDebug = false;

	boolean bumpIsDone = true;
	int shortBumpTimer;

	Configuration config;

	String tempString = "";
	ArrayList<String> tempArrayList = new ArrayList<String>();

	boolean reloadChatBot = false;

	public void setReloadChatBot(boolean reloadChatBot) {
		this.reloadChatBot = reloadChatBot;
	}

	int timerTime = 144000;
	PluginManager pm;
	Boolean debug = false;

	static Economy econ = null;

	public static Vault vault = null;
	int used;
	static int amsgId;

	VipForumMarkCommands vipForumMarkCommands = new VipForumMarkCommands(this);

	@Override
	public void onEnable() {
		config = getConfig();
		dataConfig = getDataConfig();
		vipConfig = getVipConfig();
		loadItems();
		tpBlocks = loadHashSet("tpblocks");
		guides = loadHashSet("guides");
		Commands.chatBotBlocks = loadHashSet("chatbotblocks");
		this.getDescription();
		setupEconomy();
		setupCommands();
		loadUses();
		setupChatBot();
		loadworldGuard();
		loadPlots();
		loadUnfinishedForumVip();
		loadForumVip();
		loadUnfinishedPlots();
		// loadBlackList(); //
		bumpTimer();
		lotteryTimer();
		pm = getServer().getPluginManager();
		pm.registerEvents(this.playerListener, this);
		pm.registerEvents(this.vehicleListener, this);
		pm.registerEvents(this.blockListener, this);
		Plugin x = this.getServer().getPluginManager().getPlugin("Vault");
		if (x != null & x instanceof Vault) {
			vault = (Vault) x;
		} else {
			logger.warning(String.format("[%s] Vault was _NOT_ found! Disabling plugin.", getDescription().getName()));
			getPluginLoader().disablePlugin(this);
			return;
		}
		//Create configurationsection if it isn't there yet:
		if (getVipConfig().getConfigurationSection("vipdays") == null) {
			getVipConfig().createSection("vipdays");
		}
		saveConfig();
		//TODO remove invisibility remover.
		removeInvisibility();
	}

	@Override
	public void onDisable() {
		saveItems();
		saveUses();
		saveworldGuard();
		saveHashSet(tpBlocks, "tpblocks");
		saveHashSet(guides, "guides");
		saveHashSet(Commands.chatBotBlocks, "chatbotblocks");
		saveUnfinishedPlots();
		savePlots();
		saveForumVip();
		saveUnfinishedForumVip();
	}

	List<Integer> getUnfinishedPlots() {
		return unfinishedPlots;
	}

	HashMap<Integer, String> getPlots() {
		return plots;
	}

	List<Integer> getUnfinishedForumVip() {
		return unfinishedForumVip;
	}

	HashMap<Integer, String> getForumVip() {
		return forumVip;
	}

	public boolean getBumpIsDone() {
		return bumpIsDone;
	}

	public int getShortBumpTimer() {
		return shortBumpTimer;
	}

	public void setBumpIsDone(boolean bumpIsDone) {
		this.bumpIsDone = bumpIsDone;
	}

	public VipForumMarkCommands getVipForumMarkCommands() {
		return vipForumMarkCommands;
	}

	public void reloadDataConfig() {
		if (dataConfigFile == null) {
			dataConfigFile = new File(getDataFolder(), "data.yml");
		}
		dataConfig = YamlConfiguration.loadConfiguration(dataConfigFile);

		// Look for defaults in the jar
		InputStream defConfigStream = this.getResource("data.yml");
		if (defConfigStream != null) {
			YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
			dataConfig.setDefaults(defConfig);
		}
	}

	public FileConfiguration getDataConfig() {
		if (dataConfig == null) {
			this.reloadDataConfig();
		}
		return dataConfig;
	}

	public void saveDataConfig() {
		if (dataConfig == null || dataConfigFile == null) {
			return;
		}
		try {
			getDataConfig().save(dataConfigFile);
		} catch (IOException ex) {
			this.getLogger().log(Level.SEVERE, "Could not save config to " + dataConfigFile, ex);
		}
	}

	public void reloadVipConfig() {
		if (vipConfigFile == null) {
			vipConfigFile = new File(getDataFolder(), "vip.yml");
		}
		vipConfig = YamlConfiguration.loadConfiguration(vipConfigFile);
		saveVipConfig();

		// Look for defaults in the jar
		InputStream defConfigStream = this.getResource("vip.yml");
		if (defConfigStream != null) {
			YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
			vipConfig.setDefaults(defConfig);
		}
	}

	public FileConfiguration getVipConfig() {
		if (vipConfig == null) {
			this.reloadVipConfig();
		}
		return vipConfig;
	}

	public void saveVipConfig() {
		if (vipConfig == null || vipConfigFile == null) {
			return;
		}
		try {
			getVipConfig().save(vipConfigFile);
		} catch (IOException ex) {
			this.getLogger().log(Level.SEVERE, "Could not save config to " + vipConfigFile, ex);
		}
	}

	private boolean setupEconomy() {
		if (getServer().getPluginManager().getPlugin("Vault") == null) {
			return false;
		}
		RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
		if (rsp == null) {
			return false;
		}
		econ = rsp.getProvider();
		return econ != null;
	}

	public void setupCommands() {
		getCommand("blockfaq").setExecutor(new Commands(this));
		getCommand("vip").setExecutor(new VipCommands(this));
		getCommand("slap").setExecutor(new SlapCommands(this));
		getCommand("minecart").setExecutor(new Commands(this));
		getCommand("backdeath").setExecutor(new VipCommands(this));
		getCommand("te").setExecutor(new Commands(this));
		getCommand("tpblock").setExecutor(new Commands(this));
		getCommand("tpallow").setExecutor(new Commands(this));
		getCommand("warppvp").setExecutor(new Commands(this));
		getCommand("warpcakedefence").setExecutor(new Commands(this));
		getCommand("cakedefence").setExecutor(new Commands(this));
		getCommand("message").setExecutor(new Commands(this));
		getCommand("searchregion").setExecutor(new Commands(this));
		getCommand("roll").setExecutor(new Commands(this));
		getCommand("note").setExecutor(new Commands(this));
		getCommand("mobcheck").setExecutor(new Commands(this));
		getCommand("leavecake").setExecutor(new Commands(this));
		getCommand("sgm").setExecutor(new Commands(this));
		getCommand("group").setExecutor(new Commands(this));
		getCommand("potion").setExecutor(new Commands(this));
		getCommand("ride").setExecutor(new Commands(this));
		getCommand("plot").setExecutor(new PlotCommands(this));
		getCommand("pmark").setExecutor(new PlotCommands(this));
		getCommand("pcheck").setExecutor(new PlotCommands(this));
		getCommand("ptp").setExecutor(new PlotCommands(this));
		getCommand("pdone").setExecutor(new PlotCommands(this));
		getCommand("bumpdone").setExecutor(new Commands(this));
		getCommand("bwoke").setExecutor(new Commands(this));
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void saveHashSet(HashSet hashSet, String configString) {
		List<String> tempList = new ArrayList<String>(hashSet);
		dataConfig.set(configString, null);
		dataConfig.set(configString, tempList);
		saveDataConfig();
	}

	public HashSet<String> loadHashSet(String configString) {
		List<String> tempList = dataConfig.getStringList(configString);
		HashSet<String> hashSet = new HashSet<String>(tempList);
		return hashSet;
	}

	public void lotteryTimer() {
		this.getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
			public void run() {
				if (lotteryEnabled == true) {
					lotteryPlaying = true;
					getServer().broadcastMessage(ChatColor.GOLD + "[SLAP]" + ChatColor.WHITE + " The lottery has started! Type /roll to play!");
					shortLotteryTimer();
					lotteryTimer();
				}
			}
		}, 72000);
	}

	public void shortLotteryTimer() {
		this.getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
			public void run() {
				if (!lottery.isEmpty()) {
					int highestNumber = -1;
					Player winningPlayer = null;
					//Loop through the hashmap from top to bottom (keep original order!)
					for (String playerName : lottery.keySet()) {
						//Get number that the player rolled.
						int rolledNumber = lottery.get(playerName);
						// If number is higher than currently highestNumber and player is online, 
						//make it the new highestNumber and change the winningPlayer.	
						if (rolledNumber > highestNumber && getServer().getPlayer(playerName) != null) {
							highestNumber = rolledNumber;
							winningPlayer = getServer().getPlayer(playerName);
						}
					}

					//If winningPlayer is null, no one played, return.
					if (winningPlayer == null) {
						getServer().broadcastMessage(ChatColor.GOLD + "[SLAP]" + ChatColor.WHITE + " The lottery is over! But no one played...");
						lottery.clear();
						lotteryPlaying = false;
						return;
					}

					//Give reward to winner.
					Random random = new Random();
					if (random.nextInt(101) == 0) {
						winningPlayer.getInventory().addItem(new ItemStack(Material.DIAMOND, 5));
						getServer().broadcastMessage(ChatColor.GOLD + "[SLAP]" + ChatColor.WHITE + " Jackpot! " + winningPlayer.getName() + " gets 5 diamonds!");
					} else if (random.nextInt(101) < 5) {
						winningPlayer.getInventory().addItem(new ItemStack(Material.COOKIE, 64));
						getServer().broadcastMessage(ChatColor.GOLD + "[SLAP]" + ChatColor.WHITE + " Cookies! " + winningPlayer.getName() + " gets a stack of cookies!");
					} else {
						winningPlayer.getInventory().addItem(new ItemStack(Material.CAKE, 1));
						getServer().broadcastMessage(ChatColor.GOLD + "[SLAP]" + ChatColor.WHITE + " The lottery is over! The winner is " + winningPlayer.getName() + "!");
					}

					lottery.clear();
					lotteryPlaying = false;
				} else {
					lotteryPlaying = false;
					getServer().broadcastMessage(ChatColor.GOLD + "[SLAP]" + ChatColor.WHITE + " The lottery is over! But noone played...");
				}
			}
		}, 1200);
	}

	public void saveTheConfig() {
		reloadConfig();
		saveConfig();
	}

	public void bumpTimer() {
		amsgId = this.getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
			public void run() {
				//Aggresive bumping thing here. Start 5 minute timer.
				bumpIsDone = false;
				getServer().dispatchCommand(getServer().getConsoleSender(), "mod-broadcast Post On Yogscast/Minecraftforums, Use /Bumpdone When You Are Going Bump!");
				if (getOnlineStaff() > 0) {
					shortBumpTimer();
				} else {
					bumpTimer();
				}
			}
		}, 144000);
	}

	public void shortBumpTimer() {
		shortBumpTimer = getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
			public void run() {
				if (getOnlineStaff() > 0 && !bumpIsDone) {
					getServer().dispatchCommand(getServer().getConsoleSender(), "mod-broadcast Post On Yogscast/Minecraftforums, Use /Bumpdone When You Are Going To Bump!");
					shortBumpTimer();
				} else {
					bumpTimer();
				}
			}
		}, 1200);
	}

	public int getOnlineStaff() {
		int onlineStaff = 0;
		for (Player player : getServer().getOnlinePlayers()) {
			if (player.hasPermission("slaphomebrew.bump")) {
				onlineStaff++;
			}
		}
		return onlineStaff;
	}

	public void setupChatBot() {
		if (!config.contains("chatmessages.member") || reloadChatBot == true) {
			config.set("chatmessages.member", "&c[FAQ] &3Go to &bwww.slap-gaming.com/apply &3to apply for member!");
		}
		if (!config.contains("chatmessages.vip") || reloadChatBot == true) {
			config.set("chatmessages.vip", "&c[FAQ] &3Go to www.slap-gaming.com/vip for more information about VIP!");
		}
		if (!config.contains("chatmessages.build") || reloadChatBot == true) {
			config.set("chatmessages.build", "&c[FAQ] &3Find an empty plot, then ask a mod/admin for a worldguard! Take the teleport pad at spawn or use the online dynamap (slap-gaming.com/map) to go to empty plots!");
		}
		if (!config.contains("chatmessages.worldguard") || reloadChatBot == true) {
			config.set("chatmessages.worldguard", "&c[FAQ] &3Ask a mod/admin for a worldguard! This also protects chests!");
		}
		if (!config.contains("chatmessages.lockette") || reloadChatBot == true) {
			config.set("chatmessages.lockette", "&c[FAQ] &3We don't use lockette, worldguard also protects your chests!");
		}
		if (!config.contains("chatmessages.shop") || reloadChatBot == true) {
			config.set("chatmessages.shop", "&c[FAQ] &3You need to be a member to use the shop! Right click for an item, shift + right click for a stack of items!");
		}
		if (!config.contains("chatmessages.money") || reloadChatBot == true) {
			config.set("chatmessages.money", "&c[FAQ] &3Type /sell hand with an item you want to sell in your hand, or go to www.slap-gaming.com/money to get money!");
		}
		if (!config.contains("chatmessages.checkwg") || reloadChatBot == true) {
			config.set("chatmessages.checkwg", "&c[FAQ] &3Right click the ground with string to see zones!");
		}
		if (!config.contains("chatmessages.pay") || reloadChatBot == true) {
			config.set("chatmessages.pay", "&c[FAQ] &3Type /money pay [name] [amount]!");
		}

		reloadChatBot = false;
		this.saveConfig();
	}

	public void setupBlackList() {
		tempArrayList.add("7: You are not allowed to place that!");
		tempArrayList.add("46: You are not allowed to place that!");
		tempArrayList.add("49: You are not allowed to place that!");
		tempArrayList.add("49: You are not allowed to place that!");

		// config.set("blacklist", );
	}

	public void saveUses() {
		dataConfig.set("vipuses", null);
		for (Map.Entry<String, Integer> entry : usedGrant.entrySet()) {
			dataConfig.set("vipuses." + entry.getKey(), entry.getValue());
		}
		saveDataConfig();
	}

	public void loadUses() {
		if (dataConfig.getConfigurationSection("vipuses") == null)
			return;
		for (String key : dataConfig.getConfigurationSection("vipuses").getKeys(false)) {
			usedGrant.put(key, dataConfig.getInt("vipuses." + key));
		}
	}

	public void saveItems() {
		dataConfig.set("vipitems", null);
		for (Map.Entry<Integer, Integer> entry : vipItems.entrySet()) {
			dataConfig.set("vipitems." + Integer.toString(entry.getKey()), entry.getValue());
		}
		saveDataConfig();
	}

	public void loadItems() {
		if (dataConfig.getConfigurationSection("vipitems") == null)
			return;
		for (String key : dataConfig.getConfigurationSection("vipitems").getKeys(false)) {
			vipItems.put(Integer.valueOf(key), dataConfig.getInt("vipitems." + key));
		}
	}

	public void savePlots() {
		dataConfig.set("plots", null);
		for (Map.Entry<Integer, String> entry : plots.entrySet()) {
			dataConfig.set("plots." + entry.getKey(), entry.getValue());
		}
		saveDataConfig();
	}

	public void loadPlots() {
		if (dataConfig.getConfigurationSection("plots") == null)
			return;
		for (String key : dataConfig.getConfigurationSection("plots").getKeys(false)) {
			plots.put(Integer.valueOf(key), dataConfig.getString("plots." + key));
		}
	}

	@SuppressWarnings("rawtypes")
	public void saveworldGuard() {
		File worldguard = new File("plugins" + File.separator + "SlapHomebrew" + File.separator + "worldGuard.yml");
		if (!worldguard.exists()) {
			try {
				new File("plugins" + File.separator + "SlapHomebrew").mkdir();
				worldguard.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			worldguard.delete();
			try {
				new File("plugins" + File.separator + "SlapHomebrew").mkdir();
				worldguard.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		try {
			FileWriter fIn = new FileWriter("plugins" + File.separator + "SlapHomebrew" + File.separator + "worldGuard.yml");
			BufferedWriter oIn = new BufferedWriter(fIn);
			Set<?> set = worldGuard.entrySet();
			Iterator<?> i = set.iterator();
			tempArrayList.clear();
			while (i.hasNext()) {
				Map.Entry me = (Map.Entry) i.next();
				oIn.write(me.getKey().toString() + ":" + me.getValue().toString());
				oIn.newLine();
			}
			oIn.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void loadworldGuard() {
		FileReader fRead = null;
		try {
			fRead = new FileReader("plugins" + File.separator + "SlapHomebrew" + File.separator + "worldGuard.yml");
			BufferedReader bRead = new BufferedReader(fRead);
			String tempString;
			while ((tempString = bRead.readLine()) != null) {
				String[] tempList = tempString.split(":", 2);
				worldGuard.put(tempList[0], tempList[1]);
			}
			bRead.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	@SuppressWarnings("unchecked")
	public void loadBlackList() {
		if (config.get("blacklist") != null) {
			tempArrayList = (ArrayList<String>) config.get("blacklist");
			String vipItemConfigString = null;
			Integer i = 0;
			Iterator<String> itr = tempArrayList.iterator();
			while (itr.hasNext()) {
				itr.next();
				try {
					vipItemConfigString = tempArrayList.get(i);
				} catch (IndexOutOfBoundsException e) {
					System.out.println(e);
				}
				String[] vipItemConfigMap = vipItemConfigString.split(":");
				blackList.put(Integer.valueOf(vipItemConfigMap[0]), vipItemConfigMap[1]);
				i += 1;
			}
		} else {
			System.out.println("No vipitems in the config file found!");
		}
	}

	public void promoteVip(String playerName) {
		String[] vipGroup = { "VIP" };
		String[] vipGuideGroup = { "VIPGuide" };
		PermissionUser user = PermissionsEx.getUser(playerName);
		//Remove old homes permission (if required)
		if (vipConfig.getConfigurationSection("homes").contains(playerName)) {
			String permission = "essentials.sethome.multiple." + Integer.toString(getHomes(playerName));
			user.removePermission(permission);
		}
		String[] groupNames = user.getGroupsNames();
		if (groupNames[0].contains("Guide")) {
			user.setGroups(vipGuideGroup);
		} else {
			user.setGroups(vipGroup);
		}
		//Add new homes.
		if (vipConfig.getConfigurationSection("homes").contains(playerName)) {
			String permission = "essentials.sethome.multiple." + Integer.toString(getHomes(playerName));
			user.addPermission(permission);
		}
		getServer().dispatchCommand(getServer().getConsoleSender(), "vip mark " + playerName + " promote");
		getServer().dispatchCommand(getServer().getConsoleSender(), "mail send " + playerName + " " + ChatColor.DARK_AQUA + "[VIP] " + ChatColor.WHITE
				+ "You have been promoted to VIP! For a full list of your new permissions, go to slapgaming.com/vip!");
	}

	public void demoteVip(String playerName) {
		String[] memberGroup = { "member" };
		String[] guideGroup = { "Guide" };
		PermissionUser user = PermissionsEx.getUser(playerName);
		String[] groupNames = user.getGroupsNames();

		//Remove old homes permission (if required)
		if (vipConfig.getConfigurationSection("homes").contains(playerName)) {
			String permission = "essentials.sethome.multiple." + Integer.toString(getHomes(playerName));
			user.removePermission(permission);
		}

		if (groupNames[0].contains("Guide")) {
			user.setGroups(guideGroup);
			getServer().dispatchCommand(getServer().getConsoleSender(), "mail send " + playerName + " " + ChatColor.DARK_AQUA + "[VIP] " + ChatColor.WHITE
					+ "You have been demoted to guide! Please visit slapgaming.com/vip to renew your VIP!");
		} else {
			user.setGroups(memberGroup);
			getServer().dispatchCommand(getServer().getConsoleSender(), "mail send " + playerName + " " + ChatColor.DARK_AQUA + "[VIP] " + ChatColor.WHITE
					+ "You have been demoted to member! Please visit slapgaming.com/vip to renew your VIP!");
		}
		getServer().dispatchCommand(getServer().getConsoleSender(), "vip mark " + playerName + " demote");

		//Add new homes.
		if (vipConfig.getConfigurationSection("homes").contains(playerName)) {
			String permission = "essentials.sethome.multiple." + Integer.toString(getHomes(playerName));
			user.addPermission(permission);
		}
	}

	public void saveUnfinishedPlots() {
		dataConfig.set("unfinishedplots", unfinishedPlots);
		saveDataConfig();
	}

	public void loadUnfinishedPlots() {
		unfinishedPlots = dataConfig.getIntegerList("unfinishedplots");
	}

	public void removeInvisibility() {
		getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
			public void run() {
				for (Player player : getServer().getOnlinePlayers()) {
					Collection<PotionEffect> potionEffects = player.getActivePotionEffects();
					for (PotionEffect effect : potionEffects) {
						if (effect.getType().equals(PotionEffectType.INVISIBILITY)) {
							player.sendMessage(ChatColor.GOLD + "[SLAP]" + ChatColor.WHITE + " Invisibility potions are not allowed, potion effect removed!");
							player.removePotionEffect(PotionEffectType.INVISIBILITY);
						}
					}
				}
			}
		}, 0, 20);
	}

	public void addBumpDone(String name) {
		String date = new SimpleDateFormat("MMM.d HH:mm z").format(new Date());
		date = date.substring(0, 1).toUpperCase() + date.substring(1);
		dataConfig.set("bumps." + date, name);
		saveDataConfig();
	}

	public void saveUnfinishedForumVip() {
		dataConfig.set("unfinishedforumvip", unfinishedForumVip);
		saveDataConfig();
	}

	public void loadUnfinishedForumVip() {
		unfinishedForumVip = dataConfig.getIntegerList("unfinishedforumvip");
	}

	public void saveForumVip() {
		dataConfig.set("forumvip", null);
		for (Map.Entry<Integer, String> entry : forumVip.entrySet()) {
			dataConfig.set("forumvip." + entry.getKey(), entry.getValue());
		}
		saveDataConfig();
	}

	public void loadForumVip() {
		if (dataConfig.getConfigurationSection("forumvip") == null)
			return;
		for (String key : dataConfig.getConfigurationSection("forumvip").getKeys(false)) {
			forumVip.put(Integer.valueOf(key), dataConfig.getString("forumvip." + key));
		}
	}

	public void addHomes(String playerName) {
		if (vipConfig.getConfigurationSection("homes") == null) {
			vipConfig.createSection("homes");
		}
		PermissionUser user = PermissionsEx.getUser(playerName);
		//Remove old homes permission (if required)
		if (vipConfig.getConfigurationSection("homes").contains(playerName)) {
			String permission = "essentials.sethome.multiple." + Integer.toString(getHomes(playerName));
			user.removePermission(permission);
			//If player has already bought homes in the past, add them.
			vipConfig.getConfigurationSection("homes").set(playerName, vipConfig.getConfigurationSection("homes").getInt(playerName) + 1);
		} else {
			//Otherwise, just set it to 1.
			vipConfig.getConfigurationSection("homes").set(playerName, 1);
		}
		saveVipConfig();
		String permission = "essentials.sethome.multiple." + Integer.toString(getHomes(playerName));
		user.addPermission(permission);
	}

	public int getHomes(String playerName) {
		int homes = 0;
		PermissionUser user = PermissionsEx.getUser(playerName);
		String[] group = user.getGroupsNames();
		//Get group homes.
		if (group[0].equalsIgnoreCase("builder")) {
			homes = 1;
		} else if (group[0].equalsIgnoreCase("member") || group[0].equalsIgnoreCase("guide") || group[0].equalsIgnoreCase("vipguide")) {
			homes = 4;
		} else if (group[0].equalsIgnoreCase("slap")) {
			homes = 6;
		} else if (group[0].equalsIgnoreCase("vip")) {
			homes = 8;
		} else if (group[0].equalsIgnoreCase("mod")) {
			homes = 15;
		} else if (group[0].equalsIgnoreCase("admin")) {
			homes = 25;
		}
		//Add bought homes.
		if (vipConfig.getConfigurationSection("homes").contains(playerName)) {
			homes += 20 * vipConfig.getConfigurationSection("homes").getInt(playerName);
		}
		return homes;
	}
}