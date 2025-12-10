package zoruafan.foxaddition.utils;

import net.md_5.bungee.api.chat.HoverEvent;
import zoruafan.foxaddition.FoxAdditionAPI;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

public class CommandManager implements CommandExecutor {
	FoxAdditionAPI api = FoxAdditionAPI.INSTANCE;
	private final JavaPlugin plugin = api.getPlugin();
	private final FilesManager file = api.getFiles();
    private final String version = plugin.getDescription().getVersion();

    public CommandManager() {}
    
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        final int length = args.length;
        if (!sender.hasPermission("foxac.command")) {
            final String unknownCommand = "/" + label + " " + String.join(" ", (CharSequence[])args);
            Bukkit.dispatchCommand(sender, unknownCommand);
            return false;
        }
        if (length < 1 || args[0].equalsIgnoreCase("help")) sendHelpMessage(sender);
        else if (args[0].equalsIgnoreCase("reload")) {
        	file.reload("hooks");
            file.reload("checks");
            file.reload("settings");
            file.reload("language");
            final String message = (sender instanceof Player) ? file.MN("command.reload.message", (Player)sender) : file.MN("command.reload.message", null);
            sender.sendMessage(message);
        }
        else if (args[0].equalsIgnoreCase("notify")) {
            if (length < 2) {
                final String message = (sender instanceof Player) ? file.MN("command.notify.noargs", (Player)sender) : file.MN("command.notify.noargs", null);
                sender.sendMessage(message);
                return false;
            }
            final String text = "{prefix} " + String.join(" ", (CharSequence[])args).substring(7);
            final String text2 = text.replace("{text}", text);
            final String prefix = file.getPrefix();
            final String message2 = text2.replace("{prefix}", prefix);
            final String colorMessage = ChatColor.translateAlternateColorCodes('&', message2);
            if (file.getST().getBoolean("notifies.console", true)) Bukkit.getLogger().info(ChatColor.stripColor(colorMessage));
            for (final Player notifyPlayer : Bukkit.getOnlinePlayers()) {
                if (notifyPlayer.hasPermission(file.getST().getString("notifies.permission", "foxac.notifications"))) notifyPlayer.sendMessage(colorMessage);
            }
        }
        else if (args[0].equalsIgnoreCase("verbose")) {
            if(!api.getVerbose(sender)) {
            	final String message = (sender instanceof Player) ? file.MN("command.verbose.enable", (Player)sender) : file.MN("command.verbose.enable", null);
                sender.sendMessage(message);
            } else {
            	final String message = (sender instanceof Player) ? file.MN("command.verbose.disable", (Player)sender) : file.MN("command.verbose.disable", null);
                sender.sendMessage(message);
            }
            api.toggleVerbose(sender);
        }
        else sendHelpMessage(sender);
        return true;
    }
    
    private void sendHelpMessage(final CommandSender sender) {
        sender.sendMessage(" ");
        sender.sendMessage(" §a§lFoxAddition §fby §e§lNovaCraft254 §8[v" + version + "]");
        sender.sendMessage(" §b§nwww.spigotmc.org/resources/111260/");
        sender.sendMessage(" ");
        if (sender instanceof Player) {
            String[] p1 = { "§7/foxaddition §rreload", "§7/foxaddition §rnotify", "§7/foxaddition §rverbose" };
            String[] p2 = { file.MN("help.reload", (Player)sender), file.MN("help.notify", (Player)sender), file.MN("help.verbose", (Player)sender) };

            if (p1.length == p2.length) {
                for (int i = 0; i < p1.length; i++) {
                    String command = p1[i];
                    String hoverText = p2[i];
                    String clickableText = " §8\u25aa §r" + command;
                    ComponentBuilder message = new ComponentBuilder(clickableText);
                    message.event(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, command.replaceAll("§7", "").replaceAll("§r", "")));
                    message.event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(hoverText).create()));
                    ((Player)sender).spigot().sendMessage(message.create());
                }
            }
        }
        else {
            sender.sendMessage(" §8\u25aa §7/foxaddition §rreload");
            sender.sendMessage(" §8\u25aa §7/foxaddition §rnotify");
            sender.sendMessage(" §8\u25aa §7/foxaddition §rverbose");
        }
        sender.sendMessage(" ");
    }
}
