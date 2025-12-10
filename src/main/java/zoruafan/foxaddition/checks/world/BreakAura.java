package zoruafan.foxaddition.checks.world;

import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import com.github.retrooper.packetevents.protocol.player.DiggingAction;
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.util.Vector3d;
import zoruafan.foxaddition.FoxAdditionAPI;
import zoruafan.foxaddition.utils.FoxPlayer;
import zoruafan.foxaddition.utils.listeners.BlockDigEvent;

/* Unfinished, it's possible to use, but false flag a lot with some blocks, like slabs. */
public class BreakAura extends FoxPlayer implements Listener {
	FoxAdditionAPI api = FoxAdditionAPI.INSTANCE;
	JavaPlugin plugin = api.getPlugin();
	String p = "world.modules.breakaura";
	private int ms = 50;
	private Map<Player, Long> lBT = new HashMap<>();
	
	public BreakAura() {}
	
	@EventHandler
	public void onBlockDig(BlockDigEvent ev) {
		if (ev.isCancelled()) return;
		Player e = ev.getPlayer();
		if (ev.getAction() != DiggingAction.START_DIGGING && ev.getAction() != DiggingAction.CANCELLED_DIGGING && ev.getAction() != DiggingAction.FINISHED_DIGGING) return;
	   	if (lBT.containsKey(e)) {
	       	long lastBreakTime = lBT.get(e);
	      	long currentTime = System.currentTimeMillis();
	       	if (currentTime - lastBreakTime < ms) { startCooldown(e); ev.setCancelled(true); return; }
	       	else { lBT.remove(e); return; }
	  	}
	   	
		Block nB = ev.getBlock();
		Block tB = gTB(e, 6);
		if (tB == null) return;
		Location nBL = nB.getLocation();
		String loc = String.format("%.1f,%.1f,%.1f", nBL.getX(), nBL.getY(), nBL.getZ());
		Location tBL = tB.getLocation();
		String loc2 = String.format("%.1f,%.1f,%.1f", tBL.getX(), tBL.getY(), tBL.getZ());
		if (tB.getType().isSolid() && tB.equals(nB)) return;
		
		String block = "";
		if (nB.getType().name().toLowerCase().contains("bed") && !iAD(e, "world", p+".bed", true)) {
			e.sendMessage("aDSL:"+aDSL(e, nBL));
			block = "bed";
		}
		
		if (block.equals("bed")) {
			flag(true, e, "Destroying blocks (probably through blocks) with invalid angle in BED!", "World [BreakAura]", "[B="+nB.getType()+"] [V="+tB.getType()+"] [L1="+loc+"] [L2="+loc2+"] [isBedrock:"+api.getGeyser().iB(e)+"]", api.getFiles().getAC().getInt(p+".impossible.vls", 2), "world");
			startCooldown(e);
			ev.setCancelled(true);
			return;
		}
	}
	
    private boolean aDSL(Player e, Location l) {
        World world = l.getWorld();
        int x = l.getBlockX();
        int y = l.getBlockY();
        int z = l.getBlockZ();

        int[][] offsets = {
            {1, 0, 0}, {-1, 0, 0}, {0, 0, 1}, {0, 0, -1},
            {1, 0, 1}, {1, 0, -1}, {-1, 0, 1}, {-1, 0, -1}
        };

        for (int[] offset : offsets) {
            int checkX = x + offset[0];
            int checkZ = z + offset[2];

            if (iDS(e, world, checkX, y, checkZ)) {
                return true;
            }
        }
        return false;
    }

    private boolean iDS(Player e, World w, int x, int y, int z) {
        Block lSB = w.getBlockAt(x, y, z);
        Block uSB = w.getBlockAt(x, (int) (y + 0.5), z);
        e.sendMessage("lSB: "+lSB.getType()+" / uSB:"+uSB.getType());
        
        
        //if (isSlab(lowerSlabBlock) && isSlab(upperSlabBlock)) {
        //   Slab lowerSlab = (Slab) lowerSlabBlock.getBlockData();
        //    Slab upperSlab = (Slab) upperSlabBlock.getBlockData();
        //
        //    return lowerSlab.getType() == Slab.Type.BOTTOM && upperSlab.getType() == Slab.Type.TOP;
        //}
        return false;
    }

    @SuppressWarnings("unused")
	private boolean isMatchingSlabDirection(Player player, Block block) {
        @SuppressWarnings("deprecation")
		byte data = block.getData();
        User ue = (User) player;
        BlockFace face = block.getFace(block);
        if (face == null) return false;

        if (face == BlockFace.UP && (data & 0x8) == 0x8) {
            return true;
        } else if (face == BlockFace.DOWN && (data & 0x8) == 0x0) {
            return true;
        }
        return false;
    }
	
    public Block gTB(Player e, double maxDistance) {
        Location eyeLocation = e.getEyeLocation();
        Vector3d startPos = new Vector3d(eyeLocation.getX(), eyeLocation.getY(), eyeLocation.getZ());
        Vector direction = eyeLocation.getDirection();
        Vector3d endPos = new Vector3d(
            startPos.getX() + direction.getX() * maxDistance,
            startPos.getY() + direction.getY() * maxDistance,
            startPos.getZ() + direction.getZ() * maxDistance
        );
        return tB(e, startPos, endPos);
    }
    
    private static Block tB(Player e, Vector3d start, Vector3d end) {
        double startX = start.x;
        double startY = start.y;
        double startZ = start.z;
        double endX = end.x;
        double endY = end.y;
        double endZ = end.z;

        int floorStartX = (int) Math.floor(startX);
        int floorStartY = (int) Math.floor(startY);
        int floorStartZ = (int) Math.floor(startZ);

        double xDiff = endX - startX;
        double yDiff = endY - startY;
        double zDiff = endZ - startZ;
        double xSign = Math.signum(xDiff);
        double ySign = Math.signum(yDiff);
        double zSign = Math.signum(zDiff);

        double posXInverse = xSign == 0 ? Double.MAX_VALUE : xSign / xDiff;
        double posYInverse = ySign == 0 ? Double.MAX_VALUE : ySign / yDiff;
        double posZInverse = zSign == 0 ? Double.MAX_VALUE : zSign / zDiff;

        double nextX = posXInverse * (xSign > 0 ? 1.0 - (startX % 1) : startX % 1);
        double nextY = posYInverse * (ySign > 0 ? 1.0 - (startY % 1) : startY % 1);
        double nextZ = posZInverse * (zSign > 0 ? 1.0 - (startZ % 1) : startZ % 1);

        while (nextX <= 1.0 || nextY <= 1.0 || nextZ <= 1.0) {
            if (nextX < nextY) {
                if (nextX < nextZ) {
                    floorStartX += xSign;
                    nextX += posXInverse;
                } else {
                    floorStartZ += zSign;
                    nextZ += posZInverse;
                }
            } else if (nextY < nextZ) {
                floorStartY += ySign;
                nextY += posYInverse;
            } else {
                floorStartZ += zSign;
                nextZ += posZInverse;
            }

            Block block = e.getWorld().getBlockAt(floorStartX, floorStartY, floorStartZ);		
            if (block.getType() != Material.AIR) {
                return block;
            }
        }
        return null;
    }
    
	private void startCooldown(Player player) {
	  	lBT.put(player, System.currentTimeMillis()+ms);
	}
	   
	@EventHandler
	public void onBlockBreak(BlockBreakEvent ev) {
	  	Player e = ev.getPlayer();
	  	if(!(ev.getPlayer().getType() == EntityType.PLAYER)) return;
	   	if (lBT.containsKey(e)) {
	       	long lastBreakTime = lBT.get(e);
	      	long currentTime = System.currentTimeMillis();
	       	if (currentTime - lastBreakTime < ms) { ev.setCancelled(true); return; }
	       	else { lBT.remove(e); return; }
	  	}
	}
}