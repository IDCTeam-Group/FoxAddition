# FoxAddition - Folia Compatible Fork
An anti-cheat plugin designed to prevent specific types of hacks. This fork adds full Folia compatibility, MiniMessage support, and critical bug fixes.

## ✨ New in v1.2.3-folia-fix

### 🔧 Fixed
- **Folia Compatibility** - All 12 packet event types now use region scheduler (fixes "PositionEvent may only be triggered synchronously" spam)
- **ClassCastException Crash** - Fixed `getPing()` method passing UUID instead of Player object (fixes crashes on death/actions)
- **Floodgate Compatibility** - Removed deprecated `LINUX` enum
- **Bukkit/Spigot/Paper Fallback** - Automatic server detection for compatibility with both Folia and traditional Paper/Spigot
- **Plugin Loading** - Added `folia-supported: true` to plugin.yml

### ✨ Added
- **MiniMessage Support** - Dual format support with automatic detection (`&a` legacy codes and `<green>` modern tags with hex colors)
- **Gradle Build System** - Complete build setup with all dependencies

### 📦 Installation
1. Download `FoxAddition.jar` from [Releases](https://github.com/swift-dart/FoxAddition/releases)
2. Place in `plugins/` folder
3. Requires: PacketEvents 2.11.0+

### ✅ Tested On
- ✅ Folia 1.21.8

---

## FEATURES
- **Folia Support:** Full compatibility with Folia's multi-threaded regionized server architecture
- **Support for most versions:** FoxAddition aims to support Spigot servers (and forks) from older versions like 1.7.2 to the latest versions of Minecraft
- **MiniMessage & Legacy Colors:** Use modern `<green>`, `<#85bb65>` hex colors, or classic `&a` codes
- **Extensive and customizable configuration:** Create your configuration freely, edit values, and disable checks in specific worlds
- **Support for Bedrock players:** Compatible with GeyserMC. Works with Floodgate to enable or disable checks for specific editions
- **Logging system:** Log all alerts in the 'logs' folder within the plugin folder as txt files
- **Compatibility with various plugins:** Works with ExecutableItems, DiscordSRV, PlaceholderAPI, ProtocolLib, VeinMiner

**Original Plugin**:
- [SpigotMC.org](https://www.spigotmc.org/resources/111260/)
- [Modrinth.com](https://modrinth.com/plugin/foxaddition)

**This Fork**:
- [GitHub Repository](https://github.com/swift-dart/FoxAddition)
- [Report Issues](https://github.com/swift-dart/FoxAddition/issues)
