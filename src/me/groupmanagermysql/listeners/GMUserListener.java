package me.groupmanagermysql.listeners;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.anjocaido.groupmanager.data.User;
import org.anjocaido.groupmanager.events.GMUserEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import me.groupmanagermysql.GroupManagerMySQL;

public class GMUserListener implements Listener {

	private GroupManagerMySQL plugin;
	private String database;
	private Connection connection;

	public GMUserListener(GroupManagerMySQL plugin) {
		this.plugin = plugin;
		this.connection = this.plugin.getConnection();
		this.database = this.plugin.getDB();
	}
	
	public void unregister() {
		GMUserEvent.getHandlerList().unregister(plugin);
	}

	@EventHandler
	public void onUserGroupChange(GMUserEvent e) {
		if(e.getAction() != GMUserEvent.Action.USER_GROUP_CHANGED) return;
		plugin.runAsync(() -> {
			try {
				User user = e.getUser();
				String playerName = user.getBukkitPlayer().getName();
				String uuid = user.getUUID();
				String groupName = user.getGroupName();
				Statement statement = connection.createStatement();
				ResultSet result = statement.executeQuery("SELECT * FROM " + database + ".GroupManagerData WHERE `uuid` = '" + uuid + "'");
				if(result.next()) {
					statement.executeUpdate("UPDATE " + database + ".GroupManagerData SET `group` = '" + groupName + "', `name` = '" + playerName + "' WHERE `uuid` = '" + uuid + "';");
				} else {
					statement.executeUpdate("INSERT INTO " + database + ".GroupManagerData (`uuid`, `name`, `group`) VALUES ('" + uuid + "', '" + playerName + "', '" + groupName + "');");
				}
				statement.close();
			} catch (SQLException e1) {
				e1.printStackTrace();
				this.plugin.getLogger().warning("Unable to update player MySQL data");
			}
		});
	}

}
