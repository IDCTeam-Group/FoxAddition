package zoruafan.foxaddition.checks.badpackets;

import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import zoruafan.foxaddition.FoxAdditionAPI;
import zoruafan.foxaddition.utils.FoxPlayer;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientEntityAction.Action;

public class BadPacketsF extends FoxPlayer implements Listener {
	FoxAdditionAPI api = FoxAdditionAPI.INSTANCE;
	JavaPlugin plugin = api.getPlugin();
	String p = "badpackets.modules.packets.f";
	
	public BadPacketsF() {}

	// Runs on the netty thread from FoxPacketListener, before the server handles the packet, so the
	// return value can still cancel it. Flags and Bukkit calls are moved to the player's thread.
	public boolean onPacket(Player e, int entityID, Action action, int jumpBoost) {
		if((jumpBoost < 0 || jumpBoost > 100 || (action != Action.START_JUMPING_WITH_HORSE && jumpBoost != 0)) && !iAD(e, "badpackets", p, true)) {
        	flagLater(false, e, "", "BadPackets (Packets) [F]", "[jumpBoost:"+jumpBoost+"/100] [entityID:"+entityID+"] [isBedrock:"+api.getGeyser().iB(e)+"]", api.getFiles().getAC().getInt(p+".vls", 3), "badpackets");
        	if(api.getFiles().getAC().getBoolean(p+".eject", true)) { runLater(e, () -> { try { e.eject(); } catch(Exception ig) {} }); }
        	return api.getFiles().getAC().getBoolean(p+".cancel", true);
		}
		return false;
	}
}
