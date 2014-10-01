package net.coasterman10.rangers.boss;

import net.minecraft.server.v1_7_R3.Entity;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_7_R3.CraftWorld;

import java.util.Map;

public enum EntityTypes //Taken from the Spigot forums
{
    GOLEM_BOSS("Kalkara", 54, EntityGolemBoss.class); //You can add as many as you want.

    private EntityTypes(String name, int id, Class<? extends Entity> custom)
    {
        addToMaps(custom, name, id);
    }

    public static void spawnEntity(Entity entity, Location loc)
    {
        entity.setLocation(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
        ((CraftWorld)loc.getWorld()).getHandle().addEntity(entity);
    }

    private static void addToMaps(Class clazz, String name, int id)
    {
        ((Map)NMSReflectionUtil.getPrivateField("d", net.minecraft.server.v1_7_R3.EntityTypes.class, null)).put(clazz, name);
        ((Map)NMSReflectionUtil.getPrivateField("f", net.minecraft.server.v1_7_R3.EntityTypes.class, null)).put(clazz, Integer.valueOf(id));
    }
}
