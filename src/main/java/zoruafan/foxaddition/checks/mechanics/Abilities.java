package zoruafan.foxaddition.checks.mechanics;

import java.util.HashMap;
import java.util.Map;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import io.github.retrooper.packetevents.util.folia.FoliaScheduler;
import zoruafan.foxaddition.FoxAdditionAPI;
import zoruafan.foxaddition.utils.FoxPlayer;
import zoruafan.foxaddition.utils.listeners.AbilitiesEvent;

public class Abilities extends FoxPlayer implements Listener {
	FoxAdditionAPI api = FoxAdditionAPI.INSTANCE;
	JavaPlugin plugin = api.getPlugin();
	String p = "mechanics.modules.abilities";
	private final Map<Player, Integer> pC = new HashMap<>();
	
	public Abilities() {
		FoliaScheduler.getGlobalRegionScheduler().runAtFixedRate(plugin, (ignored) -> { for (Map.Entry<Player, Integer> entry : pC.entrySet()) entry.setValue(0); }, 20, 20);
	}

	@SuppressWarnings("deprecation")
	@EventHandler
	public void onAbilitiest(AbilitiesEvent ev) {
		if (ev.isCancelled()) return;
		Player e = ev.getPlayer();
        if (!pC.containsKey(e)) pC.put(e, 0);
        int count = pC.get(e);
        pC.put(e, count+1);
        int maxium = api.getFiles().getAC().getInt(p+".amount.max", 5);
        boolean onground = true;
        if(api.getFiles().getAC().getBoolean(p+".onground", false)) { onground = e.isOnGround(); }
        boolean allowedFlight = ev.getFlightAllowed().orElse(false);
        boolean flying = ev.isFlying().orElse(false);
        boolean creativeMode = ev.getCreativeMode().orElse(false);
        boolean godMode = ev.getGodMode().orElse(false);
        
        if(!iAD(e, "mechanics", p+".amount", true) && (count >= maxium)) {
        	ev.setCancelled(true);
        	flag(true, e, "Exceeds maxium amount of packets!", "Mechanics [Abilities]", "[packets:"+count+"/"+maxium+"] [allowedFlight:"+allowedFlight+"] [flying:"+flying+"] [creativeMode:"+creativeMode+"] [godMode:"+godMode+"] [gamemode:"+e.getGameMode()+"] [isBedrock:"+api.getGeyser().iB(e)+"]", api.getFiles().getAC().getInt(p+".amount.vls", 1), "mechanics");
        	if(api.getFiles().getAC().getBoolean(p+".amount.kick", false)) packetExceeded(e);
        	return;
        }
        
    	boolean de = true;
    	boolean iv = false;
    	if(api.getFiles().getAC().getBoolean(p+".checks.abilities.ignore_velocity", true)) { if(e.getVelocity().getX() > 0.0 || e.getVelocity().getY() > 0.0 || e.getVelocity().getZ() > 0.0) { de = false; } }
    	if(de) { iv = true; }
        
        if(allowedFlight && (!flying && !creativeMode && !godMode) && !iv && onground && !iAD(e, "mechanics", p+".checks.abilities", true)) {
        	ev.setCancelled(true);
        	flag(false, e, "Sended allowedFlight value without another value! (Probably Vanilla Fly)", "Mechanics [Abilities]", "[packets:"+count+"/"+maxium+"] [allowedFlight:"+allowedFlight+"] [flying:"+flying+"] [creativeMode:"+creativeMode+"] [godMode:"+godMode+"] [gamemode:"+e.getGameMode()+"] [isBedrock:"+api.getGeyser().iB(e)+"]", api.getFiles().getAC().getInt(p+".checks.abilities.vls", 4), "mechanics");
        	if(api.getFiles().getAC().getBoolean(p+".checks.abilities.kick", false)) timeOut(e);
        	return;
        } else if((creativeMode && e.getGameMode() != GameMode.CREATIVE) && !iAD(e, "mechanics", p+".checks.creativemode", true)) {
        	ev.setCancelled(true);
        	flag(false, e, "Sended creativeMode value without creative!", "Mechanics [Abilities]", "[packets:"+count+"/"+maxium+"] [allowedFlight:"+allowedFlight+"] [flying:"+flying+"] [creativeMode:"+creativeMode+"] [godMode:"+godMode+"] [gamemode:"+e.getGameMode()+"] [isBedrock:"+api.getGeyser().iB(e)+"]", api.getFiles().getAC().getInt(p+".checks.creativemode.vls", 2), "mechanics");
        	if(api.getFiles().getAC().getBoolean(p+".checks.creativemode.kick", false)) timeOut(e);
        	return;
        }
	}
}
