package zoruafan.foxaddition.utils;

import org.bukkit.entity.Player;
import org.geysermc.floodgate.api.FloodgateApi;

public enum Floodgate {
	INSTANCE;
	
	public boolean isBedrockUser(Player e) {
		try {
			FloodgateApi api = FloodgateApi.getInstance();
			return api.isFloodgatePlayer(e.getUniqueId());
		} catch (Exception ignored) {
			return false;
		}
	}
	
	@SuppressWarnings("deprecation")
	public String getDevice(Player e) {
		if(!isBedrockUser(e)) { return "Java"; }
		else {
			FloodgateApi api = FloodgateApi.getInstance();
			 api.isFloodgatePlayer(e.getUniqueId());
            switch (api.getPlayer(e.getUniqueId()).getDeviceOs()) {
            	case GOOGLE:
            		return "Android";
            	case IOS:
            		return "iOS";
            	case OSX:
            		return "OSX";
            	case AMAZON:
            		return "Amazon";
            	case GEARVR:
            		return "GearVR";
            	case HOLOLENS:
            		return "HoloLens";
            	case UWP:
            		return "UWP";
            	case WIN32:
            		return "Win32";
            	case DEDICATED:
            		return "Dedicated";
            	case PS4:
            		return "PS4";
            	case TVOS:
            		return "TVOS";
            	case WINDOWS_PHONE:
            		return "WindowsPhone";
            	case NX:
            		return "NX";
            	case XBOX:
            		return "Xbox";
            	default:
            		return "Unknown";
            }
		}
	}
}