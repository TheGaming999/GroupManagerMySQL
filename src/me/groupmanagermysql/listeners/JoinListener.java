package me.groupmanagermysql.listeners;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import me.groupmanagermysql.GroupManagerHook;
import me.groupmanagermysql.GroupManagerMySQL;

public class JoinListener implements Listener {

	private GroupManagerMySQL plugin;
	private GroupManagerHook groupManager;
	private Connection connection;
	private String database;
	
	public JoinListener(GroupManagerMySQL plugin) {
		this.plugin = plugin;
		this.groupManager = this.plugin.getGroupManager();
		this.database = this.plugin.getDB();
		this.connection = this.plugin.getConnection();
	}
	
	public void unregister() {
		PlayerJoinEvent.getHandlerList().unregister(plugin);
	}
	
	@EventHandler(priority=EventPriority.HIGHEST)
	public void onJoin(PlayerJoinEvent e) {
		Player p = e.getPlayer();
		String uuid = p.getUniqueId().toString();
		plugin.runAsync(() -> {
			try {
				if(connection == null) return;
				Statement statement = connection.createStatement();
				ResultSet result = statement.executeQuery("SELECT * FROM " + database + ".GroupManagerData WHERE `uuid` = '" + uuid + "'");
				if(result.next()) {
					String resultGroup = result.getString("group");
					if(!groupManager.getGroup(p).equals(resultGroup)) groupManager.setGroup(p, resultGroup);
				}
				statement.close();
			} catch (SQLException ex) {
				ex.printStackTrace();
			}
		});
	}
	
}
