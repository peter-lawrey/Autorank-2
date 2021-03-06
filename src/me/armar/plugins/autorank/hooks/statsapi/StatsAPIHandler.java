package me.armar.plugins.autorank.hooks.statsapi;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;

import me.armar.plugins.autorank.Autorank;
import me.armar.plugins.autorank.hooks.DependencyHandler;
import nl.lolmewn.stats.api.StatsAPI;
import nl.lolmewn.stats.api.stat.Stat;
import nl.lolmewn.stats.api.stat.StatEntry;
import nl.lolmewn.stats.api.user.StatsHolder;
import nl.lolmewn.stats.bukkit.BukkitMain;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.plugin.Plugin;

/**
 * Handles all connections with Stats
 * <p>
 * Date created: 21:02:34 15 mrt. 2014 
 * 
 * @author Staartvin
 * 
 */
public class StatsAPIHandler implements DependencyHandler {

	private StatsAPI api;
	private BukkitMain stats;
	private final Autorank plugin;

	public StatsAPIHandler(final Autorank instance) {
		plugin = instance;
	}

	/* (non-Javadoc)
	 * @see me.armar.plugins.autorank.hooks.DependencyHandler#get()
	 */
	@Override
	public Plugin get() {
		final Plugin plugin = this.plugin.getServer().getPluginManager()
				.getPlugin("Stats");

		try {
			// WorldGuard may not be loaded
			if (plugin == null || !(plugin instanceof BukkitMain)) {
				return null; // Maybe you want throw an exception instead
			}
		} catch (final NoClassDefFoundError exception) {
			this.plugin
					.getLogger()
					.info("Could not find Stats because it's probably disabled! Does Stats properly connect to your MySQL database?");
			return null;
		}

		return plugin;
	}

	/**
	 * Gets the total blocks of a certain id and damage value placed/broken
	 * 
	 * @param playerName Player to check for
	 * @param id Item ID to check for
	 * @param damageValue Damage value to check for. (negative number to not
	 *            skip check)
	 * @param worldName World to check in. Null for global.
	 * @param statType Either "Block break" or "Block place"
	 * @return amount player placed/broke of a block
	 */
	@SuppressWarnings("deprecation")
	public int getBlocksStat(final UUID uuid, final int id,
			final int damageValue, final String worldName, final String statName) {
		if (!isAvailable())
			return 0;

		final Collection<StatEntry> stat = getStatType(statName, uuid);
		boolean checkDamageValue = false;
		
		if (damageValue > 0) {
			checkDamageValue = true;
		}
		
		int value = 0;

		for (StatEntry s : stat) {
			Map<String, Object> metadata = s.getMetadata();

			// Check world
			if (worldName != null && metadata.containsKey("world")) {
				// Not in the world we look for
				if (!metadata.get("world").equals(worldName))
					continue;
			}
			
			// Check damage value
			if (checkDamageValue) {
				if (metadata.containsKey("data")) {
					if (!metadata.get("data").equals(damageValue))
						continue;
				}
			}
			
			// Check correct id
			if (metadata.containsKey("name")) {
				Material material = Material.matchMaterial(metadata.get("name").toString());
				
				if (material.getId() != id) continue;
			}
			
			value += s.getValue();
		}

		return value;
		
		/*int value = 0;
		

		if (damageValue > 0) {
			checkDamageValue = true;
		}

		for (final Object[] vars : blockStat.getAllVariables()) {

			if (checkDamageValue) {
				// VAR 0 = blockID INT, VAR 1 = damageValue BYTE, VAR 2 = (true = break, false = place) BOOLEAN

				final byte byteValue = (Byte) vars[1];

				if ((Integer) vars[0] == id && byteValue == damageValue) {
					value += blockStat.getValue(vars);
				}
			} else {
				if ((Integer) vars[0] == id) {
					value += blockStat.getValue(vars);
				}
			}
		}

		return value;*/
	}

	public EntityType getEntityType(final String entityName) {
		try {
			return EntityType.valueOf(entityName.toUpperCase());
		} catch (final Exception e) {
			return null;
		}
	}

	public int getNormalStat(final UUID uuid, final String statName,
			final String worldName) {
		if (!isAvailable())
			return 0;

		final Collection<StatEntry> stat = getStatType(statName, uuid);

		int value = 0;

		for (StatEntry s : stat) {
			Map<String, Object> metadata = s.getMetadata();

			if (worldName != null && metadata.containsKey("world")) {
				// Not in the world we look for
				if (!metadata.get("world").equals(worldName))
					continue;
			}

			value += s.getValue();
		}

		return value;

		/*

		for (final Object[] vars : stat.getAllVariables()) {
			value += stat.getValue(vars);
		}

		return value;*/
	}

	/**
	 * Get the stats of a player, a new stat will be created if it didn't exist
	 * yet.
	 * 
	 * @param statName Name of the stat to get
	 * @param playerName Player to get the stats of.
	 * @param worldName World to check for.
	 * @return Requested stat of the player
	 */
	public Collection<StatEntry> getStatType(final String statName,
			final UUID uuid) {

		StatsHolder holder = stats.getUserManager().getUser(uuid);

		Stat stat = stats.getStatManager().getStat(statName);

		if (stat == null)
			throw new IllegalArgumentException("Unknown stat '" + statName
					+ "'!");

		return holder.getStats(stat);
	}

	public int getTotalBlocksBroken(final UUID uuid, final String worldName) {
		if (!isAvailable())
			return 0;
		
		
		return this.getNormalStat(uuid, "Blocks broken", worldName);
	}

	public int getTotalBlocksMoved(final UUID uuid, final int type,
			final String worldName) {
		if (!isAvailable())
			return 0;

		final String statName = "Move";

		final Collection<StatEntry> stat = getStatType(statName, uuid);

		int value = 0;

		for (StatEntry s : stat) {

			Map<String, Object> metadata = s.getMetadata();

			if (worldName != null && metadata.containsKey("world")) {
				// Not in the world we look for
				if (!metadata.get("world").equals(worldName))
					continue;
			}

			if (metadata.containsKey("type")
					&& (Integer) metadata.get("type") != type)
				continue;

			value += s.getValue();

		}
//		for (final Object[] vars : stat.getAllVariables()) {
//			if ((Integer) vars[0] == type) {
//				value += stat.getValue(vars);
//			}
//		}

		return value;
	}

	public int getTotalBlocksPlaced(final UUID uuid, final String worldName) {
		if (!isAvailable())
			return 0;

		return this.getNormalStat(uuid, "Blocks placed", worldName);
	}

	public int getTotalMobsKilled(final UUID uuid, final String mobName,
			final String worldName) {
		if (!isAvailable())
			return 0;

		// TODO NOT DONE BY STATS 3.0 AUTHOR
		
		
		/*final String statName = "Kill";

		final StatData data = getStatType(statName, uuid, worldName);

		final EntityType mob = getEntityType(mobName);
		boolean checkEntityType = false;
		int value = 0;

		if (mob != null) {
			checkEntityType = true;
		}

		for (final Object[] vars : data.getAllVariables()) {

			// var 0 is mob type
			if (checkEntityType) {
				if (getEntityType(vars[0].toString()) != null
						&& getEntityType(vars[0].toString()).equals(mob)) {
					value += data.getValue(vars);
				}
			} else {
				value += data.getValue(vars);
			}
		}

		return value;*/
		return 0;
	}

	public int getTotalPlayTime(final UUID uuid, final String worldName) {
		if (!isAvailable())
			return 0;

		return this.getNormalStat(uuid, "Playtime", worldName);
	}

	/* (non-Javadoc)
	 * @see me.armar.plugins.autorank.hooks.DependencyHandler#isAvailable()
	 */
	@Override
	public boolean isAvailable() {
		return api != null;
	}

	/* (non-Javadoc)
	 * @see me.armar.plugins.autorank.hooks.DependencyHandler#isInstalled()
	 */
	@Override
	public boolean isInstalled() {
		final Plugin plugin = get();

		return plugin != null && plugin.isEnabled();
	}

	/* (non-Javadoc)
	 * @see me.armar.plugins.autorank.hooks.DependencyHandler#setup()
	 */
	@Override
	public boolean setup(final boolean verbose) {
		if (!isInstalled()) {
			if (verbose) {
				plugin.getLogger().info("Stats has not been found!");
			}
			return false;
		} else {

			api = plugin.getServer().getServicesManager()
					.getRegistration(StatsAPI.class).getProvider();

			stats = (BukkitMain) get();

			if (api != null) {
				if (verbose) {
					plugin.getLogger().info(
							"Stats has been found and can be used!");
				}
				return true;
			} else {
				if (verbose) {
					plugin.getLogger().info(
							"Stats has been found but cannot be used!");
				}
				return false;
			}
		}
	}
}
