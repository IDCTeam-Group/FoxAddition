package zoruafan.foxaddition.checks.badpackets;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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
	private final Map<Player, Boolean> swing = new ConcurrentHashMap<>();
	boolean placed = false;
	
	public BadPacketsH() {}

	@EventHandler
	public void onAnimation(AnimationEvent ev) {
		swing.put(ev.getPlayer(), false);
	}
	
	@EventHandler
	public void onInteractEntity(InteractEntityEvent ev) {
		Player e = ev.getPlayer();
		if(!(ev.getAction() == InteractAction.ATTACK) || iAD(e, "badpackets", p, true)) { return; }
		final boolean swung = swing.getOrDefault(e, false);
		if(swung) {
			flag(false, e, "Trying to send an invalid swing order when attack.", "BadPackets (Packets) [H]", "[swing:"+swung+"/false] [isBedrock:"+api.getGeyser().iB(e)+"]", api.getFiles().getAC().getInt(p+".vls", 2), "badpackets");
			if(api.getFiles().getAC().getBoolean(p+".cancel", true)) { ev.setCancelled(true); } 
		}
		swing.put(e, true);
	}
	
	@Override
	protected void forget(Player e) {
		super.forget(e);
		swing.remove(e);
	}
}
