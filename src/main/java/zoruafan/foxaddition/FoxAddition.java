package zoruafan.foxaddition;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandExecutor;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import zoruafan.foxaddition.utils.CommandManager;

public final class FoxAddition extends JavaPlugin {
	private boolean dFC = false;
	FoxAdditionAPI api = FoxAdditionAPI.INSTANCE;
	
    @Override
    public void onLoad() {
    	try {
    		api.load(this);
    	} catch (Exception e) {
	        getLogger().severe("[STARTUP] Seems like PacketEvents don't load correctly.");
	        getLogger().severe("[STARTUP] An error has been found with it, please, report");
	        getLogger().severe("[STARTUP] this error to the support of PacketEvents.");
	        getLogger().severe("[STARTUP] FoxAddition can't work without it.");
	        getLogger().severe("[STARTUP] Disabling...");
        	Bukkit.getPluginManager().disablePlugin((Plugin)this);
            try { Thread.sleep(2_000); } catch (InterruptedException e2) { throw new RuntimeException(e); }
            return;
    	}
    }
    @Override
    public void onEnable() {
    	try {
    		header();
    	} catch (Exception e) {
        	logCentered("");
        	logCentered("______                         _      _  _  _    _                 ");
        	logCentered("|  ____|             /\\       | |    | |(_)| |  (_)              ");
        	logCentered("| |__  ___ __  __   /  \\    __| |  __| | _ | |_  _   ___   _ __  ");
        	logCentered("|  __|/ _ \\\\ \\/ /  / /\\ \\  / _` | / _` || || __|| | / _ \\ | '_ \\ ");
        	logCentered("| |  | (_) |>  <  / ____ \\| (_| || (_| || || |_ | || (_) || | | |");
        	logCentered("|_|   \\___//_/\\_\\/_/    \\_\\\\__,_| \\__,_||_| \\__||_| \\___/ |_| |_|");
        	logCentered("Powered by https://www.idcteam.xyz/");
        	logCentered("");
            logCentered("    Created by NovaCraft254");
            logCentered("    Running on: "+Bukkit.getVersion());
            logCentered("    Version cached: N/A");
            logCentered("");
	        getLogger().severe("[STARTUP] Seems like PacketEvents don't load correctly.");
	        getLogger().severe("[STARTUP] An error has been found with it, please, report");
	        getLogger().severe("[STARTUP] this error to the support of PacketEvents.");
	        getLogger().severe("[STARTUP] FoxAddition can't work without it.");
	        getLogger().severe("[STARTUP] Disabling...");
        	Bukkit.getPluginManager().disablePlugin((Plugin)this);
            try { Thread.sleep(2_000); } catch (InterruptedException e2) { throw new RuntimeException(e); }
            return;
    	}
	    try {
	    	final String bukkitVersion = Bukkit.getBukkitVersion();
	    	final String[] versionParts = bukkitVersion.split("\\.");
		  	final String minor = versionParts[1];
		  	String minor_1;
		 	if(minor.contains("-")) {
		    	final String[] minorParts = minor.split("-");
		     	minor_1 = minorParts[0];
		  	} else minor_1 = minor;
		  		if (Integer.parseInt(minor_1) < 7) dFC = true;
		  		if (dFC) {
		  			try {
		  				getLogger().severe("[CHECK] You're using a old version than 1.7.");
		  				getLogger().severe("[CHECK] The plugin doesn't been tested in older versions than 1.7.");
		  				getLogger().severe("[CHECK] To avoid problems, the plugin has been disabled.");
		  				getLogger().severe("[CHECK] Update your server version for a better experience.");
		  			} catch (Exception e) {}
		      	Bukkit.getPluginManager().disablePlugin((Plugin)this);
		     	return;
			}
	 	} catch (Exception e) {
	      	getLogger().severe("[CHECK] Your server don't return a version.");
	     	getLogger().severe("[CHECK] The plugin probably can't work correctly because");
	      	getLogger().severe("[CHECK] this can't check if this is supported, continue");
	       	getLogger().severe("[CHECK] with your risk!");
	  	}
		if (!isPresent("packetevents")) { getLogger().severe("[STARTUP] PacketEvents is not installed."); dFC = true; }
		if (dFC) {
	        getLogger().severe("[STARTUP] You don't installed the dependencies.");
	        getLogger().severe("[STARTUP] The plugin don't work without the neccesary plugins.");
	        getLogger().severe("[STARTUP] you need the important plugin: PacketEvents.");
	        getLogger().severe("[STARTUP] Disabling...");
        	Bukkit.getPluginManager().disablePlugin((Plugin)this);
        	return;
		}
		try {
	    	api.enable(this);
	    	api.getLog().log("");
	    	api.getLog().log("[STARTUP] The server started here...");
	    	api.getLog().log("[STARTUP] Running on: "+Bukkit.getVersion());
	    	api.getLog().log("[STARTUP] Version cached: "+FoxAdditionAPI.INSTANCE.getVersion());
	    	api.getLog().log("");
		} catch (Exception e) {}
		getCommand("foxaddition").setExecutor((CommandExecutor) new CommandManager());
		api.checkForUpdates();
    }
    
    private boolean isPresent(String pluginName) {
        Plugin plugin = getServer().getPluginManager().getPlugin(pluginName);
        return plugin != null && plugin.isEnabled();
    }
    
    private void header() {
    	String ver = String.valueOf(FoxAdditionAPI.INSTANCE.getVersion());
    	logCentered("");
    	logCentered("______                         _      _  _  _    _                 ");
    	logCentered("|  ____|             /\\       | |    | |(_)| |  (_)              ");
    	logCentered("| |__  ___ __  __   /  \\    __| |  __| | _ | |_  _   ___   _ __  ");
    	logCentered("|  __|/ _ \\\\ \\/ /  / /\\ \\  / _` | / _` || || __|| | / _ \\ | '_ \\ ");
    	logCentered("| |  | (_) |>  <  / ____ \\| (_| || (_| || || |_ | || (_) || | | |");
    	logCentered("|_|   \\___//_/\\_\\/_/    \\_\\\\__,_| \\__,_||_| \\__||_| \\___/ |_| |_|");
    	logCentered("Powered by https://www.idcteam.xyz/");
    	logCentered("");
        logCentered("    Created by NovaCraft254");
        logCentered("    Running on: "+Bukkit.getVersion());
        logCentered("    Version cached: "+ver);
        logCentered("");
    }
    private void logCentered(String message) {
        int lineWidth = 53;
        int messageWidth = message.length();
        int padding = (lineWidth - messageWidth) / 2;
        StringBuilder paddedMessage = new StringBuilder();
        for (int i = 0; i < padding; i++) {
            paddedMessage.append(" ");
        }
        paddedMessage.append(message);
		getLogger().info(paddedMessage.toString());
    }
    
    public void onPluginDisable(PluginDisableEvent event) {
    	api.disable(this);
    }
}
