package zoruafan.foxaddition.utils.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import io.github.retrooper.packetevents.util.folia.FoliaScheduler;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;

public class KeepAliveEvent extends Event implements Cancellable {
	private static final HandlerList handlers = new HandlerList();
	Player player;
	private long id;
	private boolean cancelled;
	  
	public KeepAliveEvent(Player player, long id) {
		super(!FoliaScheduler.isFolia());
		
		this.player = player;
        this.id = id;
	}

	public Player getPlayer() {
		return this.player;
	}
	
    public long getID() {
        return id;
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