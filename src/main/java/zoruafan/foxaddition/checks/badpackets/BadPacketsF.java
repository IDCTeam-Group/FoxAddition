package zoruafan.foxaddition.checks.badpackets;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import zoruafan.foxaddition.FoxAdditionAPI;
import zoruafan.foxaddition.utils.FoxPlayer;
import zoruafan.foxaddition.utils.listeners.EntityActionEvent;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientEntityAction.Action;

public class BadPacketsF extends FoxPlayer implements Listener {
	FoxAdditionAPI api = FoxAdditionAPI.INSTANCE;
	JavaPlugin plugin = api.getPlugin();
	String p = "badpackets.modules.packets.f";
	
	public BadPacketsF() {}

	@EventHandler
	public void onEntityAction(EntityActionEvent ev) {
		if (ev.isCancelled()) return;
		Player e = ev.getPlayer();
		
		int jumpBoost = ev.getJumpBoost();
		int entityID = ev.getEntityId();
		Action action = ev.getAction();
		if((jumpBoost < 0 || jumpBoost > 100 || (action != Action.START_JUMPING_WITH_HORSE && jumpBoost != 0)) && !iAD(e, "badpackets", p+".enable", true)) {
        	flag(false, e, "", "BadPackets (Packets) [F]", "[jumpBoost:"+jumpBoost+"/100] [entityID:"+entityID+"] [isBedrock:"+api.getGeyser().iB(e)+"]", api.getFiles().getAC().getInt(p+".vls", 3), "badpackets");
        	if(api.getFiles().getAC().getBoolean(p+".cancel", true)) { ev.setCancelled(true); }
        	try { if(api.getFiles().getAC().getBoolean(p+".eject", true)) { e.eject(); } } catch(Exception ig) {}
		}
	}
}
