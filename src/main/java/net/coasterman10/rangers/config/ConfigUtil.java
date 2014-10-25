package net.coasterman10.rangers.config;

import net.coasterman10.rangers.SpawnVector;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.util.Vector;

/**
 * Utilities to simplify the reading and writing of special values to a configuration without using messy
 * ConfigurationSerializables that tend to make the configuration look like a mess to the end-user.
 */
public class ConfigUtil {
    private ConfigUtil() {
    }

    /**
     * Returns a Vector from a path in a configuration. The default components are specified by the configuration.
     * 
     * @param config The ConfigurationSection from which to read
     * @param path The path of the map containing the vector components. Accepts null or "".
     * @return New Vector from components
     */
    public static Vector getVector(ConfigurationSection config, String path) {
        double x = config.getDouble(path(path, "x"));
        double y = config.getDouble(path(path, "y"));
        double z = config.getDouble(path(path, "z"));
        return new Vector(x, y, z);
    }

    /**
     * Sets a Vector at a path in a configuration.
     * 
     * @param config The ConfigurationSection to which to save
     * @param path The path of the map to which to save the vector components. Accepts null or "".
     * @param vector The Vector to save, or null to delete.
     */
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

    /**
     * Returns a SpawnVector from a path in a configuration. The default components are specified by the configuration.
     * 
     * @param config The ConfigurationSection from which to read
     * @param path The path of the map containing the vector components. Accepts null or "".
     * @return New SpawnVector from components
     */
    public static SpawnVector getSpawnVector(ConfigurationSection config, String path) {
        double x = config.getDouble(path(path, "x"));
        double y = config.getDouble(path(path, "y"));
        double z = config.getDouble(path(path, "z"));
        float yaw = (float) config.getDouble(path(path, "yaw"));
        float pitch = (float) config.getDouble(path(path, "pitch"));
        return new SpawnVector(x, y, z, yaw, pitch);
    }

    /**
     * Sets a SpawnVector at a path in a configuration.
     * 
     * @param config The ConfigurationSection to which to save
     * @param path The path of the map to which to save the vector components. Accepts null or "".
     * @param vector The SpawnVector to save, or null to delete.
     */
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

    /**
     * Returns a Location from a path in a configuration. The default components are specified by the configuration. If
     * the world is not specified by the configuration or does not exist, this will return null.
     * 
     * @param config The ConfigurationSection from which to read
     * @param path The path of the map containing the location world name and components. Accepts null or "".
     * @return New Location from components, or null if world is unspecified or nonexistent.
     */
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

    /**
     * Sets a Location at a path in a configuration.
     * 
     * @param config The ConfigurationSection to which to save
     * @param path The path of the map to which to save the location world name and components. Accepts null or "".
     * @param vector The Location to save, or null to delete.
     */
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

    /**
     * Returns a cleaned up path based on the base path given by the programmer and the specified key. If the programmer
     * uses null or an empty string, this will return just the key and forgo the insertion of the path separator (".").
     * 
     * @param path The base path given by the programmer
     * @param key The key within the path.
     * @return The complete path assuming the programmer entered a legal path.
     */
    private static String path(String path, String key) {
        return (path == null || path.isEmpty() ? key : path + "." + key);
    }
}
