package net.coasterman10.rangers.boss;

import me.confuser.barapi.BarAPI;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public final class BossFight {
    public EntityGolemBoss boss;
    public Player player;
    public Location bossSpawn, playerSpawn;

    public BossFight(Location bossSpawn, Location playerSpawn, Player player){
        this.bossSpawn = bossSpawn;
        this.playerSpawn = playerSpawn;
        this.player = player;
        boss = new EntityGolemBoss((net.minecraft.server.v1_7_R3.World) bossSpawn.getWorld(), this);
    }

    public void setup(){
        EntityTypes.spawnEntity(boss, bossSpawn);
        BarAPI.setHealth(player, 100.0F);
        BarAPI.setMessage(player, "Kalkara");
    }
    public void start(){

    }
    public void finish(){

    }
}
