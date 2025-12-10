package zoruafan.foxaddition.checks.badpackets;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import zoruafan.foxaddition.FoxAdditionAPI;
import zoruafan.foxaddition.utils.FoxPlayer;
import zoruafan.foxaddition.utils.listeners.HeldItemEvent;

public class BadPacketsE extends FoxPlayer implements Listener {
	FoxAdditionAPI api = FoxAdditionAPI.INSTANCE;
	JavaPlugin plugin = api.getPlugin();
	String p = "badpackets.modules.packets.e";
	
	public BadPacketsE() {}
	
	@EventHandler
	public void onHeldItem(HeldItemEvent ev) {
		if (ev.isCancelled()) return;
		Player e = ev.getPlayer();
		int slot = ev.getSlot();
		if ((slot > 8 || slot < 0) && !iAD(e, "badpackets", p+".enable", true)) {
        	flag(false, e, "Sending held item packet with an invalid slot ID.", "BadPackets (Packets) [E]", "[slot:"+slot+"] [isBedrock:"+api.getGeyser().iB(e)+"]", api.getFiles().getAC().getInt(p+".vls", 4), "badpackets");
        	ev.setCancelled(true);
        	return;
		}
	}
}