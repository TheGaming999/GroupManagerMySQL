package me.groupmanagermysql.command;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import me.groupmanagermysql.GroupManagerMySQL;

public class GroupManagerMySQLCommand implements CommandExecutor {

	private final List<String> helpMessage = colorize("&c[&7GroupManagerMySQL&c]"
			, "&c/gmmysql reload &4- &7reloads the config file"
			, "&c/gmmysql info &4- &7shows database config settings");
	private final String noPermissionMessage = colorize("&cSorry, but you don't have permission to use this command.");
	private final String reloadingMessage = colorize("&cReloading...");
	private final String reloadSuccessfulMessage = colorize("&cConfig file has been reloaded successfully!");
	private final String reloadFailedMessage = colorize("&cFailed to reload config file! Please report the errors in your console.");
	private final List<String> infoMessage = colorize("&cHost: &7", "&cPort: &7", "&cDatabase: &7"
			, "&cUsername: &7", "&cPassword: &7");
	private List<String> infoMessageResult;
	private GroupManagerMySQL plugin;
	public boolean showPassword = false;

	public GroupManagerMySQLCommand(GroupManagerMySQL plugin) {
		this.plugin = plugin;
	}

	public void loadInfo() {
		String password = showPassword ? this.plugin.getPassword() : "<hidden>";
		this.infoMessageResult = Arrays.asList(this.plugin.getHost(), this.plugin.getPortAsString()
				, this.plugin.getDB(), this.plugin.getUsername(), password);
	}
	
	private String colorize(String textToColorize) {
		return ChatColor.translateAlternateColorCodes('&', textToColorize);
	}
	
	private List<String> colorize(List<String> textToColorize) {
		return textToColorize.stream().map(this::colorize).collect(Collectors.toCollection(LinkedList::new));
	}
	
	private List<String> colorize(String... textToColorize) {
		return colorize(Arrays.asList(textToColorize));
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if(!sender.hasPermission("groupmanagermysql.*")) {
			sender.sendMessage(noPermissionMessage);
			return true;
		}
		if(cmd.getLabel().equalsIgnoreCase("groupmanagermysql")) {
			switch (args.length) {
			case 0:
				helpMessage.forEach(sender::sendMessage);
				break;
			case 1:
				switch(args[0].toLowerCase()) {
				case "reload": case "rl":
					plugin.runAsync(() -> {
						sender.sendMessage(reloadingMessage);
						if(plugin.reload()) {
							loadInfo();
							sender.sendMessage(reloadSuccessfulMessage);
						} else {
							sender.sendMessage(reloadFailedMessage);
						}
					});
					break;
				case "info":
					sender.sendMessage(helpMessage.get(0));
					for (int i = 0; i < infoMessage.size(); i++) {
						sender.sendMessage(infoMessage.get(i) + infoMessageResult.get(i));
					}
					break;
				default:
					helpMessage.forEach(sender::sendMessage);
					break;
				}
				break;
			default:
				helpMessage.forEach(sender::sendMessage);
				break;
			}
		}
		return true;
	}

}
