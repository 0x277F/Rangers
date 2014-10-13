package net.coasterman10.rangers.config;

import net.coasterman10.rangers.SpawnVector;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.util.Vector;

public class ConfigUtil {
    private ConfigUtil() {
    }

    public static Vector getVector(ConfigurationSection config, String path) {
        double x = config.getDouble(path(path, "x"));
        double y = config.getDouble(path(path, "y"));
        double z = config.getDouble(path(path, "z"));
        return new Vector(x, y, z);
    }

    public static void setVector(ConfigurationSection config, String path, Vector vector) {
        if (vector == null) {
            config.set(path, null);
        } else {
            ConfigurationSection section = config.getConfigurationSection(path);
            if (section == null)
                section = config.createSection(path);
            section.set("x", vector.getX());
            section.set("y", vector.getY());
            section.set("z", vector.getZ());
        }
    }

    public static SpawnVector getSpawnVector(ConfigurationSection config, String path) {
        double x = config.getDouble(path(path, "x"));
        double y = config.getDouble(path(path, "y"));
        double z = config.getDouble(path(path, "z"));
        float yaw = (float) config.getDouble(path(path, "yaw"));
        float pitch = (float) config.getDouble(path(path, "pitch"));
        return new SpawnVector(x, y, z, yaw, pitch);
    }

    public static void setSpawnVector(ConfigurationSection config, String path, SpawnVector vector) {
        if (vector == null) {
            config.set(path, null);
        } else {
            ConfigurationSection section = config.getConfigurationSection(path);
            if (section == null)
                section = config.createSection(path);
            section.set("x", vector.getX());
            section.set("y", vector.getY());
            section.set("z", vector.getZ());
            section.set("yaw", vector.getYaw());
            section.set("pitch", vector.getPitch());
        }
    }

    public static Location getLocation(ConfigurationSection config, String path) {
        String worldName = config.getString(path(path, "world"));
        if (worldName == null || worldName.isEmpty())
            return null;
        World world = Bukkit.getWorld(worldName);
        if (world == null)
            return null;
        double x = config.getDouble(path(path, "x"));
        double y = config.getDouble(path(path, "y"));
        double z = config.getDouble(path(path, "z"));
        float yaw = (float) config.getDouble(path(path, "yaw"));
        float pitch = (float) config.getDouble(path(path, "pitch"));
        return new Location(world, x, y, z, yaw, pitch);
    }

    public static void setLocation(ConfigurationSection config, String path, Location location) {
        if (location == null) {
            config.set(path, null);
        } else {
            ConfigurationSection section = config.getConfigurationSection(path);
            if (section == null)
                section = config.createSection(path);
            section.set("world", location.getWorld().getName());
            section.set("x", location.getX());
            section.set("y", location.getY());
            section.set("z", location.getZ());
            section.set("yaw", location.getYaw());
            section.set("pitch", location.getPitch());
        }
    }

    private static String path(String path, String key) {
        return (path == null ? key : path + "." + key);
    }
}
