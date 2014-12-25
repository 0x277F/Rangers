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
        String[] coordinates = config.getString(path).split(",");
        double x = 0, y = 0, z = 0;
        if (coordinates.length == 3) {
            try {
                x = Double.parseDouble(coordinates[0]);
                y = Double.parseDouble(coordinates[1]);
                z = Double.parseDouble(coordinates[2]);
            } catch (NumberFormatException e) {
            }
        }
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
            config.set(path, vector.getX() + "," + vector.getY() + "," + vector.getZ());
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
        String[] coordinates = config.getString(path).split(",");
        double x = 0, y = 0, z = 0;
        float yaw = 0, pitch = 0;
        if (coordinates.length >= 3) {
            try {
                x = Double.parseDouble(coordinates[0]);
                y = Double.parseDouble(coordinates[1]);
                z = Double.parseDouble(coordinates[2]);
            } catch (NumberFormatException e) {
            }
            if (coordinates.length == 5) {
                try {
                    yaw = Float.parseFloat(coordinates[3]);
                    pitch = Float.parseFloat(coordinates[4]);
                } catch (NumberFormatException e) {
                }
            }
        }
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
            config.set(path, vector.getX() + "," + vector.getY() + "," + vector.getZ() + "," + vector.getYaw() + ","
                    + vector.getPitch());
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
        String[] coordinates = config.getString(path).split(",");
        World world = null;
        double x = 0, y = 0, z = 0;
        float yaw = 0, pitch = 0;
        if (coordinates.length >= 4) {
            world = Bukkit.getWorld(coordinates[0]);
            if (world == null)
                return null;
            try {
                x = Double.parseDouble(coordinates[1]);
                y = Double.parseDouble(coordinates[2]);
                z = Double.parseDouble(coordinates[3]);
            } catch (NumberFormatException e) {
            }
            if (coordinates.length == 6) {
                try {
                    yaw = Float.parseFloat(coordinates[4]);
                    pitch = Float.parseFloat(coordinates[5]);
                } catch (NumberFormatException e) {
                }
            }
        }
        if (world == null)
            return null;
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
            config.set(path, location.getWorld().getName() + "," + location.getX() + "," + location.getY() + ","
                    + location.getZ() + "," + location.getYaw() + "," + location.getPitch());
        }
    }
}
