package zoruafan.foxaddition.utils.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientEntityAction.Action;
import io.github.retrooper.packetevents.util.folia.FoliaScheduler;

public class EntityActionEvent extends Event implements Cancellable {
	private static final HandlerList handlers = new HandlerList();
	Player player;
    private int entityID;
    private Action action;
    private int jumpBoost;
	private boolean cancelled;
	  
	public EntityActionEvent(Player player, int entityID, Action action, int jumpBoost) {
		super(!FoliaScheduler.isFolia());
		
		this.player = player;
        this.entityID = entityID;
        this.action = action;
        this.jumpBoost = jumpBoost;
	}

	public Player getPlayer() {
		return this.player;
	}
	
    public int getEntityId() {
        return this.entityID;
    }
    
    public Action getAction() {
        return this.action;
    }
    
    public int getJumpBoost() {
        return this.jumpBoost;
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