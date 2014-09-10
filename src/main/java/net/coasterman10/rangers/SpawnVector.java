package net.coasterman10.rangers;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.util.Vector;

public class SpawnVector extends Vector {
    private float yaw;
    private float pitch;
    
    public SpawnVector() {
        super();
    }
    
    public SpawnVector(double x, double y, double z) {
        super(x, y, z);
    }
    
    public SpawnVector(double x, double y, double z, float yaw, float pitch) {
        super(x, y, z);
        this.yaw = yaw;
        this.pitch = pitch;
    }
    
    public SpawnVector setYaw(float yaw) {
        this.yaw = yaw;
        return this;
    }
    
    public float getYaw() {
        return yaw;
    }
    
    public SpawnVector setPitch(float pitch) {
        this.pitch = pitch;
        return this;
    }
    
    public float getPitch() {
        return pitch;
    }
    
    @Override
    public Location toLocation(World world) {
        return super.toLocation(world, yaw, pitch);
    }
    
    public Location addTo(Location loc) {
        loc = loc.clone();
        loc.add(this);
        loc.setYaw(yaw);
        loc.setPitch(pitch);
        return loc;
    }
}
