package zoruafan.foxaddition.utils.listeners;

import org.bukkit.Location;
import org.bukkit.util.Vector;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;

import com.github.retrooper.packetevents.event.PacketReceiveEvent;

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
	Location previous;
	Vector previousVelocity;
	boolean afterTeleport;
	long receivedAt;
	  
	public PositionEvent(Player player, double x, double y, double z, float yaw, float pitch, boolean onGround, boolean flying, PacketReceiveEvent ev, PositionType type) {
		this(player, x, y, z, yaw, pitch, onGround, flying, ev, type, null, null);
	}
	
	public PositionEvent(Player player, double x, double y, double z, float yaw, float pitch, boolean onGround, boolean flying, PacketReceiveEvent ev, PositionType type, Location previous, Vector previousVelocity) {
		this(player, x, y, z, yaw, pitch, onGround, flying, ev, type, previous, previousVelocity, false, System.currentTimeMillis());
	}
	
	// previous/previousVelocity: the player's location and velocity when the packet arrived, before the server applied the move.
	// afterTeleport: the packet may carry the jump of a teleport. receivedAt: when the packet arrived.
	public PositionEvent(Player player, double x, double y, double z, float yaw, float pitch, boolean onGround, boolean flying, PacketReceiveEvent ev, PositionType type, Location previous, Vector previousVelocity, boolean afterTeleport, long receivedAt) {
		// Always fired from the player's tick thread (see FoxPacketListener#runSync); an async flag makes Paper reject the event.
		super(false);
		
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
	    this.previous = previous;
	    this.previousVelocity = previousVelocity;
	    this.afterTeleport = afterTeleport;
	    this.receivedAt = receivedAt;
	}
	
	public Location getPreviousLocation() {
		return this.previous;
	}
	
	public Vector getPreviousVelocity() {
		return this.previousVelocity;
	}
	
	public boolean isAfterTeleport() {
		return this.afterTeleport;
	}
	
	public long getReceivedAt() {
		return this.receivedAt;
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
