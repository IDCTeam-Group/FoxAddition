package zoruafan.foxaddition.utils.listeners;

import org.bukkit.event.HandlerList;

import io.github.retrooper.packetevents.util.folia.FoliaScheduler;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import java.util.Optional;

public class AbilitiesEvent extends Event implements Cancellable {
	private static final HandlerList handlers = new HandlerList();
	Player player;
	private Optional<Boolean> flying;
    private Optional<Boolean> godMode;
    private Optional<Boolean> flightAllowed;
    private Optional<Boolean> creativeMode;
    private Optional<Float> flySpeed;
    private Optional<Float> walkSpeed;
	boolean cancelled;
	public boolean disable = false;
	  
	public AbilitiesEvent(Player player, Optional<Boolean> flying2, Optional<Boolean> godMode, Optional<Boolean> flightAllowed, Optional<Boolean> creativeMode, Optional<Float> flySpeed, Optional<Float> walkSpeed) {
		super(!FoliaScheduler.isFolia());
		
		this.player = player;
        this.flying = flying2;
        this.godMode = godMode;
        this.flightAllowed = flightAllowed;
        this.creativeMode = creativeMode;
        this.flySpeed = flySpeed;
        this.walkSpeed = walkSpeed;
	}

	public Player getPlayer() {
		return this.player;
	}
	
    public Optional<Boolean> isFlying() {
        return flying;
    }

    public Optional<Boolean> getGodMode() {
        return godMode;
    }

    public Optional<Boolean> getFlightAllowed() {
        return flightAllowed;
    }

    public Optional<Boolean> getCreativeMode() {
        return creativeMode;
    }

    public Optional<Float> getFlySpeed() {
        return flySpeed;
    }

    public Optional<Float> getWalkSpeed() {
        return walkSpeed;
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
