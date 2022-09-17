package it.elsalamander.jpanel.bungee;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

public class MyConfig{
	
	private Plugin plugin;
	private Configuration config;
	private ConfigurationProvider provider;
	
	public MyConfig(Plugin plugin) {
		this.plugin = plugin;
		this.provider = ConfigurationProvider.getProvider(YamlConfiguration.class);
	}
	
	/**
	 * Ottieni il file di config
	 * @return
	 */
	public Configuration loadConfig() {
		try{
			this.config = provider.load(new File(plugin.getDataFolder(), "config.yml"));
			return this.config;
		}catch(IOException e){
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * Salva il config
	 */
	public void saveConfig() {
		try{
			provider.save(this.config, new File(this.plugin.getDataFolder(), "config.yml"));
		}catch(IOException e){
			e.printStackTrace();
		}
	}
	
	public void makeConfig() throws IOException {
	    // Create plugin config folder if it doesn't exist
	    if (!this.plugin.getDataFolder().exists()) {
	    	this.plugin.getLogger().info("Created config folder: " + this.plugin.getDataFolder().mkdir());
	    }

	    File configFile = new File(this.plugin.getDataFolder(), "config.yml");

	    // Copy default config if it doesn't exist
	    if (!configFile.exists()) {
	        FileOutputStream outputStream = new FileOutputStream(configFile); // Throws IOException
	        InputStream in = this.plugin.getResourceAsStream("config.yml"); // This file must exist in the jar resources folder
	        in.transferTo(outputStream); // Throws IOException
	    }
	}
}
