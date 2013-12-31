package me.naithantu.SlapHomebrew.Commands.Basics;

import me.naithantu.SlapHomebrew.Commands.AbstractCommand;
import me.naithantu.SlapHomebrew.Commands.Exception.CommandException;

import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SpawnCommand extends AbstractCommand {

	private Player p;
	private static String resourceWorldName = null;
	
	public SpawnCommand(CommandSender sender, String[] args) {
		super(sender, args);
	}
	
	/**
	 * Set the name of the current resource world
	 * @param name The name of the rw-world
	 */
	public static void setResourceWorldName(String name) {
		resourceWorldName = name;
	}

	@Override
	public boolean handle() throws CommandException {
		p = getPlayer();
		testPermission("spawn");
				
		if (args.length == 0) {
			teleportToSpawn("world_start", "the lobby world", -180F);
		} else {
			switch (args[0].toLowerCase()) {
			case "old": case "oldsurvival":
				teleportToSpawn("world", "old survival world.", -90F);
				break;
			case "new": case "newsurvival": case "1.7": case "7":
				teleportToSpawn("world_survival3", "new 1.7 survival world.", -90F);
				break;
			case "disabled": case "locked": case "blocked": case "6": case "1.6": case "lockedsurvival":
				teleportToSpawn("world_survival2", "disabled 1.6 survival world.", -90F);
				break;
			case "creative": case "c":
				teleportToSpawn("world_creative", "creative world.", 90F);
				break;
			case "nether": case "thenether":
				teleportToSpawn("world_nether", "nether.", 90F);
				break;
			case "end": case "theend":
				teleportToSpawn("world_the_end", "end.", 0F);
				break;
			case "pvp":
				teleportToSpawn("world_pvp", "PVP world.", -90F);
				break;
			case "resource": case "rw": case "resourceworld":
				teleportToSpawn(resourceWorldName, "resource world.", -90F);
				break;
			case "games": case "sonic": case "game": case "mini": case "mini-games": case "minigames":
				teleportToSpawn("world_sonic", "games world.", 0F);
				break;
			default:
				teleportToSpawn("world_start", "lobby world", -180F);
			}			
		}		
		return true;
	}
	
	/**
	 * Teleport the player to the spawn of a world
	 * @param worldname The code-wise name of the world
	 * @param teleportString The common-speak name of the world 
	 * @param yaw The yaw the player should be at
	 * @throws CommandException if world is disabled
	 */
	private void teleportToSpawn(String worldname, String teleportString, Float yaw) throws CommandException {
		try {
			Location loc = plugin.getServer().getWorld(worldname).getSpawnLocation();
			loc.setYaw(yaw);
			p.teleport(loc);
			hMsg("You have been teleported to the " + teleportString);
		} catch (NullPointerException e) {
			throw new CommandException("Sorry! Teleporting to that world is currently disabled.");
		}
	}

}
