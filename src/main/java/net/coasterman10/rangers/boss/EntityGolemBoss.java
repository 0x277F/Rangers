package net.coasterman10.rangers.boss;

import net.minecraft.server.v1_7_R3.EntityIronGolem;
import net.minecraft.server.v1_7_R3.EntityLiving;
import net.minecraft.server.v1_7_R3.GenericAttributes;
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
import net.minecraft.server.v1_7_R3.PathfinderGoalFloat;
import net.minecraft.server.v1_7_R3.EntityPlayer;
import org.bukkit.craftbukkit.v1_7_R3.util.UnsafeList;
import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.lang.reflect.Field;

public class EntityGolemBoss extends EntityIronGolem {

    public EntityGolemBoss(World world) {
        super(world);
        try {//Clear original pathfinder goals
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
        ((LivingEntity)this).addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION,Integer.MAX_VALUE, 1, false));
    }

    @Override
    public void aC(){
        super.aC();
        this.getAttributeInstance(GenericAttributes.e).setValue(5.0D);//5 Attack Damage
        this.getAttributeInstance(GenericAttributes.d).setValue(0.500000000417D);//Speed - Not sure exactly what this will do.
        this.getAttributeInstance(GenericAttributes.b).setValue(10.0D);//Will target players within 10 blocks
        this.getAttributeInstance(GenericAttributes.a).setValue(40.0D);//40 health. Might change later.
        this.getAttributeInstance(GenericAttributes.c).setValue(100.0D);//Knockback resistance. Not sure if this works.
    }
}
