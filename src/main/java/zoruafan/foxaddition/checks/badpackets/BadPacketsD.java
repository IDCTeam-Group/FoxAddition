package zoruafan.foxaddition.checks.badpackets;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import zoruafan.foxaddition.FoxAdditionAPI;
import zoruafan.foxaddition.utils.FoxPlayer;
import zoruafan.foxaddition.utils.listeners.HeldItemEvent;

public class BadPacketsD extends FoxPlayer implements Listener {
	FoxAdditionAPI api = FoxAdditionAPI.INSTANCE;
	JavaPlugin plugin = api.getPlugin();
	String p = "badpackets.modules.packets.d";
	private int last = -1;
	
	public BadPacketsD() {}
	
	@EventHandler
	public void onHeldItem(HeldItemEvent ev) {
		if (ev.isCancelled()) return;
		Player e = ev.getPlayer();
		int slot = ev.getSlot();
		
		if (slot == last && !iAD(e, "badpackets", p+".enable", true)) {
        	flag(false, e, "Sending held item packet with the same slot than latest.", "BadPackets (Packets) [D]", "[slot:"+slot+"] [isBedrock:"+api.getGeyser().iB(e)+"]", api.getFiles().getAC().getInt(p+".vls", 4), "badpackets");
        	if(api.getFiles().getAC().getBoolean(p+".cancel", true)) { ev.setCancelled(true); }
        	this.last = slot;
        	return;
		}
		this.last = slot;
	}
}