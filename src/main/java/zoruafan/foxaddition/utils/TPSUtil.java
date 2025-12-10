package zoruafan.foxaddition.utils;

import org.bukkit.Bukkit;

public class TPSUtil {
    public static double[] getRecentTPS() {
        try {
            Object minecraftServer = Bukkit.getServer().getClass().getDeclaredMethod("getServer").invoke(Bukkit.getServer());
            double[] recentTps = (double[]) minecraftServer.getClass().getField("recentTps").get(minecraftServer);
            for (int i = 0; i < recentTps.length; i++) {
                recentTps[i] = Math.min(recentTps[i], 20.0);
            }
            return recentTps;
        } catch (Exception e) {
            return new double[]{-1.0, -1.0, -1.0};
        }
    }
}