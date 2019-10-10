package com.stal111.forbidden_arcanus.entity.projectile;

import com.stal111.forbidden_arcanus.entity.ModEntities;
import com.stal111.forbidden_arcanus.init.ModParticles;
import com.stal111.forbidden_arcanus.item.ModItems;

import com.stal111.forbidden_arcanus.util.ModTags;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.IRendersAsItem;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileItemEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.IPacket;
import net.minecraft.particles.IParticleData;
import net.minecraft.particles.ItemParticleData;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.tags.Tag;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.common.Tags;
import net.minecraftforge.fml.network.FMLPlayMessages.SpawnEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.network.NetworkHooks;

import java.util.List;
import java.util.Random;

@OnlyIn(value = Dist.CLIENT, _interface = IRendersAsItem.class)
public class SeedBulletEntity extends ProjectileItemEntity {

	@SuppressWarnings("unchecked")
	public SeedBulletEntity(World world) {
		super((EntityType<? extends ProjectileItemEntity>) ModEntities.seed_bullet, world);
	}

	@SuppressWarnings("unchecked")
	public SeedBulletEntity(World world, double x, double y, double z) {
		super((EntityType<? extends ProjectileItemEntity>) ModEntities.seed_bullet, x, y, z, world);
	}

	@SuppressWarnings("unchecked")
	public SeedBulletEntity(World world, LivingEntity thrower) {
		super((EntityType<? extends ProjectileItemEntity>) ModEntities.seed_bullet, thrower, world);
	}

	@SuppressWarnings("unchecked")
	public SeedBulletEntity(SpawnEntity packet, World world) {
		super((EntityType<? extends ProjectileItemEntity>) ModEntities.seed_bullet, world);
	}

	@Override
	protected void onImpact(RayTraceResult result) {
		if (result.getType() == RayTraceResult.Type.ENTITY) {
			Entity entity = ((EntityRayTraceResult)result).getEntity();
			entity.attackEntityFrom(DamageSource.causeIndirectMagicDamage(this, this.getThrower()), 0);

		}
		if (!this.world.isRemote) {
			this.world.addEntity(new ItemEntity(this.world, this.posX, this.posY, this.posZ, new ItemStack(this.getRandomSeed())));
			this.world.setEntityState(this, (byte)3);
			this.remove();
		}
	}

	private Item getRandomSeed() {
		return ModTags.Items.SEEDS.getRandomElement(new Random());
	}

	@OnlyIn(Dist.CLIENT)
	private IParticleData func_213887_n() {
		ItemStack lvt_1_1_ = this.func_213882_k();
		return (lvt_1_1_.isEmpty() ? ModParticles.item_seed_bullet : new ItemParticleData(ParticleTypes.ITEM, lvt_1_1_));
	}

	@Override
	public void handleStatusUpdate(byte p_70103_1_) {
		if (p_70103_1_ == 3) {
			IParticleData lvt_2_1_ = this.func_213887_n();

			for(int lvt_3_1_ = 0; lvt_3_1_ < 8; ++lvt_3_1_) {
				this.world.addParticle(lvt_2_1_, this.posX, this.posY, this.posZ, 0.0D, 0.0D, 0.0D);
			}
		}
	}

	@Override
	public void tick() {
		super.tick();
		Vec3d vec3d = this.getMotion();
		if (!this.isInWater()) {
			this.world.addParticle(ParticleTypes.END_ROD, this.posX - vec3d.x * 0.25D, (this.posY - vec3d.y * 0.25D) + 0.2D, this.posZ - vec3d.z * 0.25D, vec3d.x, vec3d.y, vec3d.z);
		}
	}

	@Override
	protected Item func_213885_i() {
		return ModItems.seed_bullet;
	}

	@Override
	public IPacket<?> createSpawnPacket() {
		return NetworkHooks.getEntitySpawningPacket(this);
	}
}
