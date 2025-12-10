package zoruafan.foxaddition.utils;

import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.TimeZone;
import java.util.stream.Stream;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import zoruafan.foxaddition.FoxAdditionAPI;

@SuppressWarnings("unused")
public class LogManager
{
	FoxAdditionAPI api = FoxAdditionAPI.INSTANCE;
	private JavaPlugin plugin = api.getPlugin();
	private File dir = new File(plugin.getDataFolder(), "logs");
	private FileConfiguration file = api.getFiles().getST();
    private DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private String timeFormat = file.getString("logs.timeformat");
    private DateTimeFormatter tsFormat = DateTimeFormatter.ofPattern(timeFormat).withZone(TimeZone.getDefault().toZoneId());
    private File currentLogFile;
    private int cleanupDays;
    
    public LogManager() {
    	if(!file.getBoolean("logs.enable")) return;
        if (!dir.exists()) dir.mkdirs();
        currentLogFile = getCurrentLogFile();
        String timezone = file.getString("logs.timezone");
        if (timezone.equalsIgnoreCase("auto")) {
        	final TimeZone systemTimezone = TimeZone.getDefault();
        	tsFormat = DateTimeFormatter.ofPattern(timeFormat).withZone(systemTimezone.toZoneId());
        } else {
        	final TimeZone customTimezone = TimeZone.getTimeZone(timezone);
        	tsFormat = DateTimeFormatter.ofPattern(timeFormat).withZone(customTimezone.toZoneId());
        }
        cleanupDays = file.getInt("logs.cleanup.days", 7);
        if (file.getBoolean("logs.cleanup.enable")) {
           	plugin.getLogger().info("[LOGS] The cleanup of old logs started...");
            cOLF();
            plugin.getLogger().info("[LOGS] The cleanup of old logs finished.");
        }
    }
    
    public void log(final String message) {
    	if(!file.getBoolean("logs.enable")) return;
        final File logFile = getCurrentLogFile();
        try (FileWriter writer = new FileWriter(logFile, true)) {
            final String timestamp = tsFormat.format(LocalDateTime.now());
            writer.write("[" + timestamp + "] " + message + "\n");
        } catch (Exception e) {}
    }
    
    private File getCurrentLogFile() {
        final String currentDateString = format.format(LocalDateTime.now());
        final File logFile = new File(dir, String.valueOf(currentDateString) + ".txt");
        if (!logFile.exists()) {
            try { logFile.createNewFile(); }
            catch (Exception e) {}
        }
        return logFile;
    }
    
    private boolean isLogFile(Path path) { return Files.isRegularFile(path) && path.getFileName().toString().endsWith(".txt"); }
    private void cOLF() { try (Stream<Path> logFiles = Files.list(dir.toPath())) { logFiles.filter(this::isLogFile).forEach(this::dOLF); } catch (Exception e) {} }
    private void dOLF(Path path) {
        try {
            BasicFileAttributes attributes = Files.readAttributes(path, BasicFileAttributes.class);
            FileTime creationTime = attributes.creationTime();
            LocalDateTime logFileCreationTime = LocalDateTime.ofInstant(creationTime.toInstant(), ZoneId.systemDefault());
            if (logFileCreationTime.isBefore(LocalDateTime.now().minusDays(cleanupDays))) Files.delete(path);
        } catch (Exception e) {}
    }
}