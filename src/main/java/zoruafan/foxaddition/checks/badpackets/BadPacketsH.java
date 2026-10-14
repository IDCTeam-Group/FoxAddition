package zoruafan.foxaddition.checks.badpackets;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientInteractEntity.InteractAction;

import zoruafan.foxaddition.FoxAdditionAPI;
import zoruafan.foxaddition.utils.FoxPlayer;

public class BadPacketsH extends FoxPlayer implements Listener {
	FoxAdditionAPI api = FoxAdditionAPI.INSTANCE;
	JavaPlugin plugin = api.getPlugin();
	String p = "badpackets.modules.packets.h";
	private final Map<Player, Boolean> swing = new ConcurrentHashMap<>();
	
	public BadPacketsH() {}

	public void onAnimation(Player e) {
		swing.put(e, false);
	}
	
	// Runs on the netty thread from FoxPacketListener, before the server handles the packet, so the
	// return value can still cancel it. Flags and Bukkit calls are moved to the player's thread.
	public boolean onInteract(Player e, InteractAction action) {
		if(!(action == InteractAction.ATTACK) || iAD(e, "badpackets", p, true)) { return false; }
		final boolean swung = swing.getOrDefault(e, false);
		swing.put(e, true);
		if(swung) {
			flagLater(false, e, "Trying to send an invalid swing order when attack.", "BadPackets (Packets) [H]", "[swing:"+swung+"/false] [isBedrock:"+api.getGeyser().iB(e)+"]", api.getFiles().getAC().getInt(p+".vls", 2), "badpackets");
			return api.getFiles().getAC().getBoolean(p+".cancel", true);
		}
		return false;
	}
	
	@Override
	protected void forget(Player e) {
		super.forget(e);
		swing.remove(e);
	}
}
