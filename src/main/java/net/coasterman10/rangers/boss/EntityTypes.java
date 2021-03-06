package net.coasterman10.rangers.boss;

import java.util.Map;

import net.minecraft.server.v1_7_R3.Entity;
import net.minecraft.server.v1_7_R3.World;

import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_7_R3.CraftWorld;

// Taken from the Spigot forums
public enum EntityTypes {
    GOLEM_BOSS("Kalkara", 99, EntityGolemBoss.class); // You can add as many as you want.
    protected Class<? extends Entity> clazz;

    private EntityTypes(String name, int id, Class<? extends Entity> custom) {
        addToMaps(custom, name, id);
        clazz = custom;
    }

    public static void spawnEntity(Entity entity, Location loc) {
        entity.setLocation(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
        ((CraftWorld) loc.getWorld()).getHandle().addEntity(entity);
    }

    public static void spawnEntity(EntityTypes type, Location loc) {
        try {
            Entity e = type.clazz.getDeclaredConstructor(World.class).newInstance(
                    ((CraftWorld) loc.getWorld()).getHandle());
            EntityTypes.spawnEntity(e, loc);
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private static void addToMaps(Class<? extends Entity> clazz, String name, int id) {
        ((Map) NMSReflectionUtil.getPrivateField("d", net.minecraft.server.v1_7_R3.EntityTypes.class, null)).put(clazz,
                name);
        ((Map) NMSReflectionUtil.getPrivateField("f", net.minecraft.server.v1_7_R3.EntityTypes.class, null)).put(clazz,
                Integer.valueOf(id));
    }
}
