package zoruafan.foxaddition.utils.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import io.github.retrooper.packetevents.util.folia.FoliaScheduler;

public class SteerVehicleEvent extends Event implements Cancellable {
	private static final HandlerList handlers = new HandlerList();
	Player player;
	private float forward;
	private float sideway;
	private byte flags;
	private boolean cancelled;
	  
	public SteerVehicleEvent(Player player, float forward, float sideway, byte flags) {
		super(!FoliaScheduler.isFolia());
		
		this.player = player;
        this.forward = forward;
        this.sideway = sideway;
        this.flags = flags;
	}

	public Player getPlayer() {
		return this.player;
	}
	
    public float getForward() {
        return this.forward;
    }
    
    public float getSideway() {
        return this.sideway;
    }
    
    public byte getFlags() {
        return this.flags;
    }

    public void setCancelled(boolean value) {
    	cancelled = value;
    }
    
	public boolean isCancelled() { 
		return cancelled;
	}
	
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
