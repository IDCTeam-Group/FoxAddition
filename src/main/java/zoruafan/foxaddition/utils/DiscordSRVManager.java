package zoruafan.foxaddition.utils;

import github.scarsz.discordsrv.DiscordSRV;
import github.scarsz.discordsrv.dependencies.jda.api.EmbedBuilder;
import github.scarsz.discordsrv.dependencies.jda.api.entities.Message;
import github.scarsz.discordsrv.dependencies.jda.api.entities.MessageEmbed;
import github.scarsz.discordsrv.dependencies.jda.api.entities.TextChannel;
import github.scarsz.discordsrv.dependencies.jda.api.requests.restaction.MessageAction;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import zoruafan.foxaddition.FoxAdditionAPI;

public class DiscordSRVManager {
	FoxAdditionAPI api = FoxAdditionAPI.INSTANCE;
	private final JavaPlugin plugin = api.getPlugin();
	private final Map<Player, Long> cooldowns = new ConcurrentHashMap<>();
	private final long cooldownTime = 1000L;
	private static final FilesManager file = FoxAdditionAPI.INSTANCE.getFiles();
	private static final String p = "hooks.discordsrv";
  
	public DiscordSRVManager() {}
  
	public void sendMessageToDiscord(String type, Player e, int vls, String detailed) {
		if (!file.getST().getBoolean(p+".enable", false) || !plugin.getServer().getPluginManager().isPluginEnabled("DiscordSRV")) { return; }
		if (this.hasCooldown(e)) { return; } 
		DiscordSRV discordSRV = DiscordSRV.getPlugin();
		String channel = file.getST().getString(p+".channel");
		boolean showTitle = file.getST().getBoolean(p+".show.title");
		boolean showUUID = file.getST().getBoolean(p+".show.uuid");
		boolean showInformation = file.getST().getBoolean(p+".show.information");
		boolean showLocation = file.getST().getBoolean(p+".show.location");
		String authorLang = file.getST().getString(p+".messages.author");
    	String playerLang = file.getST().getString(p+".messages.player");
    	String uuidLang = file.getST().getString(p+".messages.uuid");
    	String checkLang = file.getST().getString(p+".messages.check");
    	String vlsLang = file.getST().getString(p+".messages.vls");
    	String locationLang = file.getST().getString(p+".messages.location");
    	String detailedLang = file.getST().getString(p+".messages.information");
    	String contentLang = file.getST().getString(p+".messages.content");
    	String playerName = e.getName();
    	String vlsString = String.valueOf(vls);
    	String skinUrl = "https://api.creepernation.net/avatar/"+e.getUniqueId().toString();
    	EmbedBuilder embedBuilder = new EmbedBuilder();
    	embedBuilder.setAuthor(authorLang);
    	if (showTitle) { embedBuilder.setTitle("FoxAddition", "https://www.spigotmc.org/resources/111260/"); }
    	embedBuilder.addField(playerLang, "`"+playerName+"`", true);
    	if (showUUID) {
    		UUID playerUUID = e.getUniqueId();
    		String uuidString = String.valueOf(playerUUID);
    		embedBuilder.addField(uuidLang, "`"+uuidString+"`", true);
    	} 
    	embedBuilder.addField(checkLang, "`"+type+"`", true);
    	embedBuilder.addField(vlsLang, "`"+vlsString+"`", true);
    	if (showLocation) {
    		Location playerPosition = e.getLocation();
    		World playerWorld_U = e.getWorld();
    		String playerWorld = playerWorld_U.getName();
    		double x = playerPosition.getX();
    		double y = playerPosition.getY();
    		double z = playerPosition.getZ();
    		String locX = String.format("%.1f", new Object[] { x });
    		String locY = String.format("%.1f", new Object[] { y });
    		String locZ = String.format("%.1f", new Object[] { z });
    		embedBuilder.addField(locationLang, String.valueOf(String.valueOf(playerWorld)) + ": `" + locX + "`, `" + locY + "`, `" + locZ + "`", false);
    	} 
    	if (showInformation) { embedBuilder.addField(detailedLang, detailed, false); }
    	embedBuilder.setColor(5793266);
    	embedBuilder.setThumbnail(skinUrl);
    	MessageEmbed embed = embedBuilder.build();
    	TextChannel textChannel = discordSRV.getJda().getTextChannelById(channel);
    	if (textChannel != null) {
    		try {
    			Set<Message.MentionType> allowedMentions = Arrays.<Message.MentionType>asList(new Message.MentionType[] { Message.MentionType.USER, Message.MentionType.CHANNEL, Message.MentionType.EMOTE, Message.MentionType.ROLE }).stream().collect(Collectors.toSet());
    			textChannel.sendMessageEmbeds(embed).content(contentLang).allowedMentions(allowedMentions).queue();
    		} catch (Exception ig) {
    			textChannel.sendMessageEmbeds(embed).queue();
    		} 
    	} else { this.plugin.getLogger().warning("[DISCORDSRV] Channel with ID "+channel+" not found."); } 
    	this.setCooldown(e);
	}
  
	private boolean hasCooldown(Player e) { return (this.cooldowns.containsKey(e) && System.currentTimeMillis() < this.cooldowns.get(e)); }
	public void forget(Player e) { this.cooldowns.remove(e); }
	private void setCooldown(Player e) { this.cooldowns.put(e, System.currentTimeMillis() + this.cooldownTime); }
}
