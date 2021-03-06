package me.armar.plugins.autorank.commands;

import java.util.UUID;

import me.armar.plugins.autorank.Autorank;
import me.armar.plugins.autorank.commands.manager.AutorankCommand;
import me.armar.plugins.autorank.language.Lang;
import me.armar.plugins.autorank.util.AutorankTools;
import me.armar.plugins.autorank.util.AutorankTools.Time;
import me.armar.plugins.autorank.util.uuid.UUIDManager;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class AddCommand extends AutorankCommand {

	private final Autorank plugin;

	public AddCommand(final Autorank instance) {
		this.setUsage("/ar add [player] [value]");
		this.setDesc("Add [value] to [player]'s time");
		this.setPermission("autorank.add");

		plugin = instance;
	}

	@Override
	public boolean onCommand(final CommandSender sender, final Command cmd,
			final String label, final String[] args) {

		if (!plugin.getCommandsManager().hasPermission("autorank.add", sender)) {
			return true;
		}

		if (args.length < 3) {
			sender.sendMessage(Lang.INVALID_FORMAT
					.getConfigValue("/ar add <player> <value>"));
			return true;
		}

		final UUID uuid = UUIDManager.getUUIDFromPlayer(args[1]);

		if (uuid == null) {
			sender.sendMessage(Lang.UNKNOWN_PLAYER.getConfigValue(args[1]));
			return true;
		}

		int value = -1;

		if (args.length > 2) {

			final StringBuilder builder = new StringBuilder();

			for (int i = 2; i < args.length; i++) {
				builder.append(args[i]);
			}

			if (!builder.toString().contains("m")
					&& !builder.toString().contains("h")
					&& !builder.toString().contains("d")) {
				value = AutorankTools.stringtoInt(builder.toString().trim());
				value += plugin.getPlaytimes().getLocalTime(uuid);
			} else {
				value = AutorankTools.stringToTime(builder.toString(),
						Time.MINUTES);
				value += plugin.getPlaytimes().getLocalTime(uuid);
			}
		}

		if (value >= 0) {
			plugin.getPlaytimes().setLocalTime(uuid, value);
			AutorankTools.sendColoredMessage(sender,
					Lang.PLAYTIME_CHANGED.getConfigValue(args[1], value + ""));
		} else {
			AutorankTools.sendColoredMessage(sender, Lang.INVALID_FORMAT
					.getConfigValue("/ar add [player] [value]"));
		}

		return true;
	}

}
