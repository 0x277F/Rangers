package net.coasterman10.rangers.boss;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashSet;
import java.util.Random;

import me.confuser.barapi.BarAPI;
import net.minecraft.server.v1_7_R3.Entity;
import net.minecraft.server.v1_7_R3.EntityIronGolem;
import net.minecraft.server.v1_7_R3.EntityPlayer;
import net.minecraft.server.v1_7_R3.GenericAttributes;
import net.minecraft.server.v1_7_R3.PathfinderGoalFloat;
import net.minecraft.server.v1_7_R3.PathfinderGoalHurtByTarget;
import net.minecraft.server.v1_7_R3.PathfinderGoalLookAtPlayer;
import net.minecraft.server.v1_7_R3.PathfinderGoalMeleeAttack;
import net.minecraft.server.v1_7_R3.PathfinderGoalMoveThroughVillage;
import net.minecraft.server.v1_7_R3.PathfinderGoalMoveTowardsRestriction;
import net.minecraft.server.v1_7_R3.PathfinderGoalNearestAttackableTarget;
import net.minecraft.server.v1_7_R3.PathfinderGoalRandomLookaround;
import net.minecraft.server.v1_7_R3.PathfinderGoalRandomStroll;
import net.minecraft.server.v1_7_R3.PathfinderGoalSelector;
import net.minecraft.server.v1_7_R3.World;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_7_R3.entity.CraftIronGolem;
import org.bukkit.craftbukkit.v1_7_R3.util.UnsafeList;
import org.bukkit.entity.Damageable;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

public class EntityGolemBoss extends EntityIronGolem {

    public int tick = Integer.MAX_VALUE;
    private Random random;

    public BossFight match;

    public EntityGolemBoss(World world) {
        super(world);
        try {// Clear original pathfinder goals
            Field bField = PathfinderGoalSelector.class.getDeclaredField("b");
            bField.setAccessible(true);
            Field cField = PathfinderGoalSelector.class.getDeclaredField("c");
            cField.setAccessible(true);
            bField.set(goalSelector, new UnsafeList<PathfinderGoalSelector>());
            bField.set(targetSelector, new UnsafeList<PathfinderGoalSelector>());
            cField.set(goalSelector, new UnsafeList<PathfinderGoalSelector>());
            cField.set(targetSelector, new UnsafeList<PathfinderGoalSelector>());
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
        }

        this.goalSelector.a(0, new PathfinderGoalFloat(this));
        this.goalSelector.a(2, new PathfinderGoalMeleeAttack(this, EntityPlayer.class, 1.0D, false));
        this.goalSelector.a(5, new PathfinderGoalMoveTowardsRestriction(this, 1.0D));
        this.goalSelector.a(6, new PathfinderGoalMoveThroughVillage(this, 1.0D, false));
        this.goalSelector.a(7, new PathfinderGoalRandomStroll(this, 1.0D));
        this.goalSelector.a(8, new PathfinderGoalLookAtPlayer(this, EntityPlayer.class, 8.0F));
        this.goalSelector.a(8, new PathfinderGoalRandomLookaround(this));
        this.targetSelector.a(1, new PathfinderGoalHurtByTarget(this, true));
        this.targetSelector.a(2, new PathfinderGoalNearestAttackableTarget(this, EntityPlayer.class, 0, false));
        this.setCustomName("Kalkara");
        getBukkitEntity().addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, Integer.MAX_VALUE, 1, true), true);
        random = new Random();
    }

    public EntityGolemBoss(World world, BossFight fight) {
        this(world);
        this.match = fight;
    }

    @Override
    public void aC() {
        super.aC();
        this.getAttributeInstance(GenericAttributes.e).setValue(15.0D);// 15 Attack Damage
        this.getAttributeInstance(GenericAttributes.d).setValue(0.700000000417D);// Speed - Not sure exactly what this
                                                                                 // will do.
        this.getAttributeInstance(GenericAttributes.b).setValue(10.0D);// Will target players within 10 blocks
        this.getAttributeInstance(GenericAttributes.a).setValue(40.0D);// 40 health.
        this.getAttributeInstance(GenericAttributes.c).setValue(100.0D);// Knockback resistance. Not sure if this works.
    }

    @Override
    public boolean n(Entity entity) {// Called whenever this entity damages another entity
        boolean flag = super.n(entity);
        if (tick == Integer.MAX_VALUE)// First attack after spawn
            tick = 0;
        if (random.nextInt(3) == 0) {// 1 out of 4 chance
            entity.setOnFire(100);// 5 seconds of fire
        }
        return flag;
    }

    @Override
    public void e() {// This is the onLivingUpdate() method
        super.e();
        tick++;

        if (tick == 400) {// Twenty Seconds
            this.launch();
        }
        if (tick == 415) {
            this.smash();
        }
        if (tick == 515) {
            this.fireBreath();
            tick = 0;
        }
        BarAPI.setHealth(this.match.player, this.getHealth());
    }
    
    @Override
    public CraftIronGolem getBukkitEntity() {
        return (CraftIronGolem) this.bukkitEntity;
    }

    public void launch() {
        org.bukkit.entity.Entity e = getBukkitEntity();
        Vector vec = e.getLocation().getDirection();
        vec.add(new Vector(0.0D, 5.0D, 0.0D));
        vec.multiply(2);// Speed
        e.setVelocity(vec);
        e.setFallDistance(-100.0F);
    }

    public void smash() {
        org.bukkit.entity.Entity e = getBukkitEntity();
        Location l = e.getLocation();
        double y = l.getY();
        for (double x = l.getX() - 10; x <= l.getX() + 10; x++) {
            for (double z = l.getZ() - 10; z <= l.getZ() + 10; z++) {
                e.getWorld().playEffect(new Location(e.getWorld(), x, y, z), Effect.STEP_SOUND,
                        e.getWorld().getBlockAt(new Location(e.getWorld(), x, y, z)).getType());
            }
        }
        for (org.bukkit.entity.Entity entity : e.getNearbyEntities(10, 10, 10)) {
            if (entity instanceof Damageable && entity != this) {
                ((Damageable) entity).damage(15, e);
            }
        }
    }

    public void fireBreath() {
        Collection<Location> blocks = new HashSet<>();
        org.bukkit.entity.Entity e = getBukkitEntity();
        // TODO calculate the blocks that need particles
        for (Location l : blocks) {
            e.getWorld().playEffect(l, Effect.MOBSPAWNER_FLAMES, 1, 1);
            for (org.bukkit.entity.Entity be : e.getWorld().getEntities()) {
                if (be == this.getGoalTarget()) {
                    be.setFireTicks(1000);
                }
            }
        }
    }
}
