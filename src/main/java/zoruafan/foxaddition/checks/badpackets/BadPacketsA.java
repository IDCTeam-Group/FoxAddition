package zoruafan.foxaddition.checks.badpackets;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import com.github.retrooper.packetevents.protocol.player.DiggingAction;
import com.github.retrooper.packetevents.protocol.world.BlockFace;
import zoruafan.foxaddition.FoxAdditionAPI;
import zoruafan.foxaddition.utils.FoxPlayer;
import zoruafan.foxaddition.utils.listeners.BlockDigEvent;

public class BadPacketsA extends FoxPlayer implements Listener {
	FoxAdditionAPI api = FoxAdditionAPI.INSTANCE;
	JavaPlugin plugin = api.getPlugin();
	String p = "badpackets.modules.packets.a";
	
	public BadPacketsA() {}
	
	@EventHandler
	public void onBlockDig(BlockDigEvent ev) {
		if (ev.isCancelled()) return;
		Player e = ev.getPlayer();
		if(ev.getAction() != DiggingAction.RELEASE_USE_ITEM) { return; }
		BlockFace face = ev.getFace();
		boolean invalid = false;
        String i = e.getInventory().getItemInHand().getType().name().toLowerCase();
        if(i.equals("crossbow") || i.equals("bow")) return;
		
        switch (face) {
	        case UP:
	        case DOWN:
	        case EAST:
	        case WEST: {
	            invalid = true;
	            break;
	        }
	        default:
	        	break;
        }
        if(invalid && !iAD(e, "badpackets", p+".enable", true)) {
        	flag(false, e, "Player sended BlockDig with invalid directions.", "BadPackets (Packets) [A]", "[face:"+face+"] [isBedrock:"+api.getGeyser().iB(e)+"]", api.getFiles().getAC().getInt(p+".vls", 5), "badpackets");
        	if(api.getFiles().getAC().getBoolean(p+".cancel", true)) { ev.setCancelled(true); }
        	return;
        }
	}

}
