package it.elsalamander.jpanel.spigot.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import it.elsalamander.jpanel.all.PanelPlugin;
import it.elsalamander.jpanel.all.PanelSessions;
import it.elsalamander.jpanel.all.Utils.PasswordHash;
import it.elsalamander.jpanel.all.PanelUser;

public class MyCommand implements CommandExecutor {
	
	@Override
	public boolean onCommand(CommandSender sender,Command cmd,String label,String[] args){
		PanelPlugin plugin = PanelPlugin.getInstance();
		PanelSessions sessions = plugin.getSession();
		JavaPlugin pl = (JavaPlugin) plugin.getServer();
		FileConfiguration config = pl.getConfig();
		
		if(cmd.getName().equalsIgnoreCase("addlogin")){
			if(args.length < 1){
				sender.sendMessage("You must specify a username and a password!");
				return true;
			}
			
			if(sender instanceof Player){
				sender.sendMessage("This must be run by the console!");
				return true;
			}
			
			try{
				sender.sendMessage("Creating user....");
				String password = PasswordHash.generateStrongPasswordHash(args[1]);
				
				PanelUser user = new PanelUser(password, false, false, false);
				
				sessions.addUser(args[0], user);
				
				config.set("users." + args[0] + ".password", user.password);
				config.set("users." + args[0] + ".canEditFiles", user.canEditFiles);
				config.set("users." + args[0] + ".canChangeGroups", user.canChangeGroups);
				config.set("users." + args[0] + ".canSendCommands", user.canSendCommands);
				pl.saveConfig();
				
				sender.sendMessage("User created!");
				
			}catch(Exception e){
				sender.sendMessage("Failed to create user!");
				e.printStackTrace();
				return true;
			}
			
			return true;
		}else if(cmd.getName().equalsIgnoreCase("passwd")){
			if(args.length < 2){
				sender.sendMessage("You must specify a username, the old password and the new password!");
				return true;
			}
			
			if(sender instanceof Player){
				sender.sendMessage("This must be run by the console!");
				return true;
			}
			
			try{
				PanelUser user = sessions.getUser(args[0]);
				String oldPassword = args[1];
				
				if(PasswordHash.validatePassword(oldPassword, user.password)){
					String newPassword = PasswordHash.generateStrongPasswordHash(args[2]);
					
					PanelUser newUser = new PanelUser(newPassword, user.canEditFiles, user.canChangeGroups,
							user.canSendCommands);
					
					sessions.addUser(args[0], newUser);
					config.set("users." + args[0] + ".password", newUser.password);
					config.set("users." + args[0] + ".canEditFiles", newUser.canEditFiles);
					config.set("users." + args[0] + ".canChangeGroups", newUser.canChangeGroups);
					config.set("users." + args[0] + ".canSendCommands", newUser.canSendCommands);
					
					pl.saveConfig();
					
					sender.sendMessage("Password for " + args[0] + " changed!");
					
				}else{
					sender.sendMessage("Old password incorrect!");
				}
				
			}catch(Exception e){
				sender.sendMessage("Failed to create user!");
				e.printStackTrace();
				return true;
			}
			
			return true;
		}else if(cmd.getName().equalsIgnoreCase("jpanel")){
			if(sender instanceof Player){
				sender.sendMessage("This must be run by the console!");
				return true;
			}
			
			sender.sendMessage("This server is running JPanel " + pl.getDescription().getVersion());
			sender.sendMessage("Made by rymate1234");
			
			sender.sendMessage("------ Commands ------");
			sender.sendMessage("Command   | Description");
			sender.sendMessage("/addlogin | adds a user to JPanel");
			sender.sendMessage("/passwd   | change the password of a JPanel user");
			return true;
		}
		return false;
	}
}
