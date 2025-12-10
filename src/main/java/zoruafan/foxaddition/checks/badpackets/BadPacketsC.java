package zoruafan.foxaddition.checks.badpackets;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import zoruafan.foxaddition.FoxAdditionAPI;
import zoruafan.foxaddition.utils.FoxPlayer;
import zoruafan.foxaddition.utils.listeners.SteerVehicleEvent;

public class BadPacketsC extends FoxPlayer implements Listener {
	FoxAdditionAPI api = FoxAdditionAPI.INSTANCE;
	JavaPlugin plugin = api.getPlugin();
	String p = "badpackets.modules.packets.c";
	
	public BadPacketsC() {}

	@EventHandler
	public void onSteerVehicle(SteerVehicleEvent ev) {
		if (ev.isCancelled()) return;
		Player e = ev.getPlayer();
		float forward = Math.abs(ev.getForward());
		float sideway = Math.abs(ev.getSideway());
		float max_forward = (float) api.getFiles().getAC().getDouble(p+".forward");
		float max_sideway = (float) api.getFiles().getAC().getDouble(p+".sideway");
		
		boolean invalid = false;
		String invalid_reason = "";
		
		if (forward > max_forward) { invalid = true; invalid_reason = "Sending SteerVehicle packet with an invalid forward movement value."; }
		else if (sideway > max_sideway) { invalid = true; invalid_reason = "Sending SteerVehicle packet with an invalid sideway movement value."; }
		
		if (invalid && !iAD(e, "badpackets", p+".enable", true)) {
        	flag(false, e, invalid_reason, "BadPackets (Packets) [C]", "[forward:"+forward+"/"+max_forward+"] [sideway:"+sideway+"/"+max_sideway+"] [isBedrock:"+api.getGeyser().iB(e)+"]", api.getFiles().getAC().getInt(p+".vls", 3), "badpackets");
        	if(api.getFiles().getAC().getBoolean(p+".cancel", true)) { ev.setCancelled(true); }
        	try { if(api.getFiles().getAC().getBoolean(p+".eject", true)) { e.eject(); } } catch(Exception ig) {}
        	return;
		}
	}
}
