package zoruafan.foxaddition.utils.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;

import com.github.retrooper.packetevents.event.PacketReceiveEvent;

import io.github.retrooper.packetevents.util.folia.FoliaScheduler;
import zoruafan.foxaddition.utils.PositionType;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;

public class PositionEvent extends Event implements Cancellable {
	private static final HandlerList handlers = new HandlerList();
	double x;
	double y;
	double z;
	float yaw;
	float pitch;
	boolean onGround;
	Player player;
	boolean cancelled;
	boolean flying;
	PacketReceiveEvent packet;
	PositionType type;
	  
	public PositionEvent(Player player, double x, double y, double z, float yaw, float pitch, boolean onGround, boolean flying, PacketReceiveEvent ev, PositionType type) {
		super(!FoliaScheduler.isFolia());
		
		this.player = player;
	    this.x = x;
	    this.y = y;
	    this.z = z;
	    this.yaw = yaw;
	    this.pitch = pitch;
	    this.onGround = onGround;
	    this.flying = flying;
	    this.packet = ev;
	    this.type = type;
	}
	  
	public double getX() {
		return this.x;
	}
	  
	public double getY() {
		return this.y;
	}
	  
	public double getZ() {
	    return this.z;
	}
	  
	public float getYaw() {
	    return this.yaw;
	}
	  
	public float getPitch() {
	    return this.pitch;
	}
	  	
	public boolean isOnGround() {
	    return this.onGround;
	}
	
	public boolean isFlying() {
		return this.flying;
	}
	
	public PacketReceiveEvent getPacket() {
		return this.packet;
	}
	
	public PositionType getType() {
		return this.type;
	}

	public Player getPlayer() {
		return this.player;
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
