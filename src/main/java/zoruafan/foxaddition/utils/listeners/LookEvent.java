package zoruafan.foxaddition.utils.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class LookEvent extends Event implements Cancellable {
	private static final HandlerList handlers = new HandlerList();
	Player player;
	private float yaw;
	private float pitch;
	private boolean cancelled;
	  
	public LookEvent(Player player, float yaw, float pitch) {
		// Always fired from the player's tick thread (see FoxPacketListener#runSync); an async flag makes Paper reject the event.
		super(false);
		
		this.player = player;
        this.yaw = yaw;
        this.pitch = pitch;
	}

	public Player getPlayer() {
		return this.player;
	}
	
	public float getYaw() {
		return this.yaw;
	}
	
    public float getPitch() {
        return this.pitch;
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