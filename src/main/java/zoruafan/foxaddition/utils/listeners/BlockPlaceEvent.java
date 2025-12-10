package zoruafan.foxaddition.utils.listeners;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import com.github.retrooper.packetevents.protocol.player.InteractionHand;
import com.github.retrooper.packetevents.protocol.world.BlockFace;
import com.github.retrooper.packetevents.util.Vector3i;
import io.github.retrooper.packetevents.util.folia.FoliaScheduler;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;

public class BlockPlaceEvent extends Event implements Cancellable {
	private static final HandlerList handlers = new HandlerList();
	Player player;
	Vector3i blockPosition;
	BlockFace face;
	Block block;
	long sequence;
	InteractionHand hand;
	private boolean cancelled;
	  
	public BlockPlaceEvent(Player player, Vector3i blockPosition, BlockFace face, long sequence, InteractionHand hand) {
		super(!FoliaScheduler.isFolia());
		
		this.player = player;
		this.blockPosition = blockPosition;
        this.face = face;
        this.sequence = sequence;
        this.hand = hand;
	}

	public Player getPlayer() {
		return this.player;
	}
	
    public Vector3i getBlockPosition() {
        return blockPosition;
    }
    
    public BlockFace getFace() {
        return face;
    }
    
    public Block getBlock() {
        return block = player.getWorld().getBlockAt(blockPosition.getX(), blockPosition.getY(), blockPosition.getZ());
    }
    
    public long getSequence() {
        return sequence;
    }

    public InteractionHand hand() {
        return hand;
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