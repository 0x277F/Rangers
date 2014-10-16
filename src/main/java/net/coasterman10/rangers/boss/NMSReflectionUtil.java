package net.coasterman10.rangers.boss;

import org.bukkit.Bukkit;

import java.lang.reflect.Field;
import java.util.logging.Level;

public final class NMSReflectionUtil {
    public static Object getPrivateField(String name, Class clazz, Object obj){
        Object r = null;
        try{
            Field f = clazz.getDeclaredField(name);
            f.setAccessible(true);
            return f.get(obj);
        } catch (ReflectiveOperationException e){
            Bukkit.getLogger().log(Level.SEVERE, e.getMessage());
        }
        return null;
    }
}
