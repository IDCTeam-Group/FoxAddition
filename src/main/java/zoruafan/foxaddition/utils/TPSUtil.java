package zoruafan.foxaddition.utils;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import io.github.retrooper.packetevents.util.folia.FoliaScheduler;

public class TPSUtil {
    public static double[] getRecentTPS() {
        // Paper: Server#getTPS(). Folia has no global TPS, see getTPS(Player).
        try {
            double[] tps = (double[]) Bukkit.getServer().getClass().getMethod("getTPS").invoke(Bukkit.getServer());
            if (tps != null && tps.length > 0) return clamp(tps);
        } catch (Throwable ignored) {}
        // Spigot: MinecraftServer.recentTps. Copied, so the server's own array is never changed.
        try {
            Object minecraftServer = Bukkit.getServer().getClass().getDeclaredMethod("getServer").invoke(Bukkit.getServer());
            double[] recentTps = (double[]) minecraftServer.getClass().getField("recentTps").get(minecraftServer);
            return clamp(recentTps);
        } catch (Throwable e) {
            return new double[]{-1.0, -1.0, -1.0};
        }
    }
    
    // On Folia every region ticks on its own, so the player's region is what matters.
    public static double getTPS(Player player) {
        if (player != null && FoliaScheduler.isFolia()) {
            try {
                double[] tps = (double[]) Bukkit.getServer().getClass().getMethod("getRegionTPS", Location.class).invoke(Bukkit.getServer(), player.getLocation());
                if (tps != null && tps.length > 0) return Math.min(tps[0], 20.0);
            } catch (Throwable ignored) {}
        }
        return getRecentTPS()[0];
    }
    
    private static double[] clamp(double[] tps) {
        double[] copy = tps.clone();
        for (int i = 0; i < copy.length; i++) copy[i] = Math.min(copy[i], 20.0);
        return copy;
    }
}