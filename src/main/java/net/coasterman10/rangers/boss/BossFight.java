package net.coasterman10.rangers.boss;

import me.confuser.barapi.BarAPI;
import net.minecraft.server.v1_7_R3.World;

import org.bukkit.Location;
import org.bukkit.entity.Player;

//TODO
public final class BossFight {
    public EntityGolemBoss boss;
    public Player player;
    public Location bossSpawn, playerSpawn;
    public Phase current;

    public BossFight(Location bossSpawn, Location playerSpawn, Player player){
        this.bossSpawn = bossSpawn;
        this.playerSpawn = playerSpawn;
        this.player = player;
        boss = new EntityGolemBoss((World) bossSpawn.getWorld(), this);
        current = Phase.SETUP;
        this.setup();
    }

    public void setup(){
        EntityTypes.spawnEntity(boss, bossSpawn);
        BarAPI.setHealth(player, 100.0F);
        BarAPI.setMessage(player, "Kalkara");
        this.proceed();
    }
    public void start(){

        this.proceed();
    }
    public void finish(){

    }
    public static enum Phase { SETUP, FIGHT, FINISH }
    public void proceed(){
        if(this.current != Phase.FINISH)
            this.current = Phase.values()[this.current.ordinal()+1];
        switch(current){
            case SETUP:
                setup();
                break;
            case FIGHT:
                start();
                break;
            case FINISH:
                finish();
                break;
        }
    }
}
