package zoruafan.foxaddition.checks.badpackets;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientInteractEntity.InteractAction;
import zoruafan.foxaddition.FoxAdditionAPI;
import zoruafan.foxaddition.utils.FoxPlayer;
import zoruafan.foxaddition.utils.listeners.AnimationEvent;
import zoruafan.foxaddition.utils.listeners.InteractEntityEvent;

public class BadPacketsH extends FoxPlayer implements Listener {
	FoxAdditionAPI api = FoxAdditionAPI.INSTANCE;
	JavaPlugin plugin = api.getPlugin();
	String p = "badpackets.modules.packets.h";
	boolean swing;
	boolean placed = false;
	
	public BadPacketsH() {}

	@EventHandler
	public void onAnimation(AnimationEvent ev) {
		swing = false;
	}
	
	@EventHandler
	public void onInteractEntity(InteractEntityEvent ev) {
		Player e = ev.getPlayer();
		if(!(ev.getAction() == InteractAction.ATTACK) || !iAD(e, "badpackets", p+".attack.enable", true)) { return; }
		if(swing) {
			flag(false, e, "Trying to send an invalid swing order when attack.", "BadPackets (Packets) [H]", "[swing:"+swing+"/false] [isBedrock:"+api.getGeyser().iB(e)+"]", api.getFiles().getAC().getInt(p+".vls", 2), "badpackets");
			if(api.getFiles().getAC().getBoolean(p+".attack.cancel", true)) { ev.setCancelled(true); } 
		}
		swing = true;
	}
}
