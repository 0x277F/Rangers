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

    public SpawnVector(Location loc) {
        this(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
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
    
    public void round() {
        x = round(x, 0.5);
        y = round(y, 0.5);
        z = round(z, 0.5);
        yaw = round(yaw, 45F);
        pitch = round(pitch, 45F);
    }

    @Override
    public Location toLocation(World world) {
        return super.toLocation(world, yaw, pitch);
    }

    @Override
    public SpawnVector subtract(Vector vec) {
        return (SpawnVector) super.subtract(vec);
    }

    public Location addTo(Location loc) {
        loc = loc.clone();
        loc.add(this);
        loc.setYaw(yaw);
        loc.setPitch(pitch);
        return loc;
    }

    private static double round(double n, double mult) {
        return mult * (Math.round(n / mult));
    }

    private static float round(float n, float mult) {
        return mult * (Math.round(n / mult));
    }
}
