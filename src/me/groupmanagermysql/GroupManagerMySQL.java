package me.groupmanagermysql;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import me.groupmanagermysql.command.GroupManagerMySQLCommand;
import me.groupmanagermysql.listeners.GMUserListener;
import me.groupmanagermysql.listeners.JoinListener;

public class GroupManagerMySQL extends JavaPlugin {

	private Connection connection;
	private String host, database, username, password;
	private int port;

	private GroupManagerHook groupManager;

	private GMUserListener groupManagerUserListener;
	private JoinListener joinListener;

	private GroupManagerMySQLCommand command;

	private File configFile;
	
	@Override
	public void onEnable() {
		this.command = new GroupManagerMySQLCommand(this);
		this.configFile = new File(this.getDataFolder(), "config.yml");
		boolean firstRun = false;
		if(!this.configFile.exists()) firstRun = true;
		getCommand("groupmanagermysql").setExecutor(command);
		getConfig().options().copyDefaults(true);
		saveDefaultConfig();
		if(!firstRun)
			load();
		else
			getLogger().warning("Please edit the config with the desired settings then reload using /gmmysql reload");
		this.command.loadInfo();
		getLogger().info("Enabled");
	}

	@Override
	public void onDisable() {
		getLogger().info("Disabled");
	}

	public BukkitTask runAsync(Runnable runnable) {
		return this.getServer().getScheduler().runTaskAsynchronously(this, runnable);
	}

	public boolean openConnection() throws SQLException, ClassNotFoundException {
		if (connection != null && !connection.isClosed()) {
			return true;
		}
		synchronized (this) {
			if (connection != null && !connection.isClosed()) {
				return true;
			}
			Class.forName("com.mysql.jdbc.Driver");
			Properties prop = new Properties();
			prop.setProperty("user", username);
			prop.setProperty("password", password);
			prop.setProperty("autoReconnect", String.valueOf(true));
			connection = DriverManager.getConnection("jdbc:mysql://" + this.host + ":" + this.port + "/" + this.database + "?characterEncoding=utf8", prop);
			Statement enableStatement = connection.createStatement();  
			enableStatement.executeUpdate("CREATE TABLE IF NOT EXISTS " + database + ".GroupManagerData (`uuid` varchar(255), `name` varchar(255), `group` varchar(255));");
			getLogger().info("MySQL: successfully connected to database.");
			return connection != null;
		}
	}

	public boolean setupMySQL() {
		host = getConfig().getString("host");
		port = getConfig().getInt("port");
		database = getConfig().getString("database");
		username = getConfig().getString("username");
		password = getConfig().getString("password");   
		try {    
			return openConnection();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			getLogger().severe("MySQL: connection failed.");
			return false;
		} catch (SQLException e) {
			e.printStackTrace();
			getLogger().severe("MySQL: an error has occurred.");
			return false;
		}
	}

	public GroupManagerHook getGroupManager() {
		return this.groupManager;
	}

	public Connection getConnection() {
		return this.connection;
	}

	public String getDB() {
		return this.database;
	}

	public String getUsername() {
		return this.username;
	}

	public String getPassword() {
		return this.password;
	}

	public String getHost() {
		return this.host;
	}

	public int getPort() {
		return this.port;
	}

	public String getPortAsString() {
		return String.valueOf(this.port);
	}

	public boolean reload() {
		unregisterListeners();
		reloadConfig();
		if(connection != null) {
			try {
				connection.close();
				getLogger().info("MySQL: closed database connection.");
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return load();
	}

	public void unregisterListeners() {
		if(this.joinListener != null)
			this.joinListener.unregister();
		if(this.groupManagerUserListener != null)
			this.groupManagerUserListener.unregister();
	}

	public boolean load() {
		if(setupMySQL()) { 
			this.groupManager = new GroupManagerHook(this);
			this.joinListener = new JoinListener(this);
			this.groupManagerUserListener = new GMUserListener(this);
			Bukkit.getPluginManager().registerEvents(joinListener, this);
			Bukkit.getPluginManager().registerEvents(groupManagerUserListener, this);
			return true;
		} else {
			getLogger().warning("MySQL: failed to connect to database.");
			return false;
		}
	}

}
