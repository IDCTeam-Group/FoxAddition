package zoruafan.foxaddition.utils;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import zoruafan.foxaddition.FoxAdditionAPI;

public class GeyserManager {
	FoxAdditionAPI api = FoxAdditionAPI.INSTANCE;
	JavaPlugin plugin = api.getPlugin();
	private final FileConfiguration file = api.getFiles().getST();
	private Boolean floodgate = cF();
	
    public GeyserManager() { this.floodgate = cF(); }
	
	public boolean iB(Player e) {
	    boolean isBedrock = false;
	    this.floodgate = cF();
	    if (file.getBoolean("bedrock.modules.prefix.enable", false)) {
	        isBedrock = e.getName().startsWith(file.getString("bedrock.modules.prefix.value", "."));
	        if (isBedrock) return true;
	    }
	    if (file.getBoolean("bedrock.modules.uuid.enable", false)) {
	        isBedrock = e.getUniqueId().toString().startsWith("000000");
	        if (isBedrock) return true;
	    }
	    if(floodgate == true && file.getBoolean("bedrock.modules.floodgate.enable", true)) {
	    	try { isBedrock = Floodgate.INSTANCE.isBedrockUser(e); if (isBedrock) return true; } catch (Exception ignored) {}
	    }
	    return isBedrock;
	}
	
	public String getDevice(Player e) {
		String de = "Java";
		String p = "bedrock.modules.floodgate";
		if(floodgate == true && (file.getBoolean(p+".device", true) && file.getBoolean(".enable", true))) {
			try { de = Floodgate.INSTANCE.getDevice(e); return de; } catch (Exception ignored) {}
		}
		return de;
	}

	private Boolean cF() {
		return Bukkit.getPluginManager().isPluginEnabled("Floodgate");
	}
}