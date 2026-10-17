package zoruafan.foxaddition.checks.badpackets;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import zoruafan.foxaddition.FoxAdditionAPI;
import zoruafan.foxaddition.utils.FoxPlayer;

public class BadPacketsD extends FoxPlayer implements Listener {
	FoxAdditionAPI api = FoxAdditionAPI.INSTANCE;
	JavaPlugin plugin = api.getPlugin();
	String p = "badpackets.modules.packets.d";
	private final Map<Player, Integer> last = new ConcurrentHashMap<>();
	
	public BadPacketsD() {}
	
	// Runs on the netty thread from FoxPacketListener, before the server handles the packet, so the
	// return value can still cancel it. Flags and Bukkit calls are moved to the player's thread.
	public boolean onPacket(Player e, int slot) {
		Integer previous = last.put(e, slot);
		if (previous != null && slot == previous && !iAD(e, "badpackets", p, true)) {
        	flagLater(false, e, "Sending held item packet with the same slot than latest.", "BadPackets (Packets) [D]", "[slot:"+slot+"] [isBedrock:"+api.getGeyser().iB(e)+"]", api.getFiles().getAC().getInt(p+".vls", 4), "badpackets");
        	return api.getFiles().getAC().getBoolean(p+".cancel", true);
		}
		return false;
	}
	
	@Override
	protected void forget(Player e) {
		super.forget(e);
		last.remove(e);
	}
}
