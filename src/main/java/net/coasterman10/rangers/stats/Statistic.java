package net.coasterman10.rangers.stats;

import net.minecraft.util.com.google.common.collect.BiMap;
import net.minecraft.util.com.google.common.collect.HashBiMap;

public class Statistic {
    private BiMap<String, Object> compound = HashBiMap.create();

    public BiMap<String, Object> getMapCompound(){
        return HashBiMap.create(compound);
    }

    public Statistic set(String key, Object value){
        compound.put(key, value);
        return this;
    }

    public Object get(String key){
        return compound.get(key);
    }

}
