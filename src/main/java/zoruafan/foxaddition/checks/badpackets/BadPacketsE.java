package zoruafan.foxaddition.checks.badpackets;

import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import zoruafan.foxaddition.FoxAdditionAPI;
import zoruafan.foxaddition.utils.FoxPlayer;

public class BadPacketsE extends FoxPlayer implements Listener {
	FoxAdditionAPI api = FoxAdditionAPI.INSTANCE;
	JavaPlugin plugin = api.getPlugin();
	String p = "badpackets.modules.packets.e";
	
	public BadPacketsE() {}
	
	// Runs on the netty thread from FoxPacketListener, before the server handles the packet, so the
	// return value can still cancel it. Flags and Bukkit calls are moved to the player's thread.
	public boolean onPacket(Player e, int slot) {
		if ((slot > 8 || slot < 0) && !iAD(e, "badpackets", p, true)) {
        	flagLater(false, e, "Sending held item packet with an invalid slot ID.", "BadPackets (Packets) [E]", "[slot:"+slot+"] [isBedrock:"+api.getGeyser().iB(e)+"]", api.getFiles().getAC().getInt(p+".vls", 4), "badpackets");
        	return true;
		}
		return false;
	}
}
