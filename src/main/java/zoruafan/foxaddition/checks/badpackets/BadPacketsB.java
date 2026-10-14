package zoruafan.foxaddition.checks.badpackets;

import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import zoruafan.foxaddition.FoxAdditionAPI;
import zoruafan.foxaddition.utils.FoxPlayer;

public class BadPacketsB extends FoxPlayer implements Listener {
	FoxAdditionAPI api = FoxAdditionAPI.INSTANCE;
	JavaPlugin plugin = api.getPlugin();
	String p = "badpackets.modules.packets.b";
	
	public BadPacketsB() {}
	
	// Runs on the netty thread from FoxPacketListener, before the server handles the packet, so the
	// return value can still cancel it. Flags and Bukkit calls are moved to the player's thread.
	public boolean onPacket(Player e, int id) {
		boolean invalid = false;
		String invalid_reason = "";
		if (id < 0) { invalid = true; invalid_reason = "Sending interaction entity id less than 0 (invalid)."; }
		else if (id == e.getEntityId()) { invalid = true; invalid_reason = "Sending self interaction entity id (invalid)."; }
		if (invalid && !iAD(e, "badpackets", p, true)) {
        	flagLater(false, e, invalid_reason, "BadPackets (Packets) [B]", "[id:"+id+"] [isBedrock:"+api.getGeyser().iB(e)+"]", api.getFiles().getAC().getInt(p+".vls", 5), "badpackets");
        	return api.getFiles().getAC().getBoolean(p+".cancel", true);
		}
		return false;
	}
}
