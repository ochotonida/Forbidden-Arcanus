package com.stal111.forbidden_arcanus.common.block.entity;

import com.stal111.forbidden_arcanus.core.init.ModBlockEntities;
import com.stal111.forbidden_arcanus.core.init.ModItems;
import com.stal111.forbidden_arcanus.util.ModTags;
import com.stal111.forbidden_arcanus.util.ModUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Black Hole Block Entity <br>
 * Forbidden Arcanus - com.stal111.forbidden_arcanus.common.block.entity.BlackHoleBlockEntity
 *
 * @author stal111
 * @version 1.18.2 - 2.0.0
 */
public class BlackHoleBlockEntity extends BlockEntity {

    private static final double DAMAGE_DISTANCE = 0.6D;
    private static final int PLAYER_SEARCH_DISTANCE = 6;

    private final List<ItemEntity> thrownOutItems = new ArrayList<>();

    private double stored_xp;
    public int rotation = 0;
    public int tickCounter;
    public int auraTexture = 0;

    public BlackHoleBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.BLACK_HOLE.get(), pos, state);
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, BlackHoleBlockEntity blockEntity) {
        blockEntity.rotation++;
        blockEntity.tickCounter++;

        if (blockEntity.tickCounter == 5 || blockEntity.tickCounter == 10) {
            blockEntity.auraTexture++;
        } else if (blockEntity.tickCounter == 15) {
            blockEntity.tickCounter = 0;
            blockEntity.auraTexture = 0;
        }
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, BlackHoleBlockEntity blockEntity) {
        List<Entity> entities = level.getEntities(null, new AABB(pos.getX() + 0.5 - 5, pos.getY() + 0.5 - 5, pos.getZ() + 0.5 - 5, pos.getX() + 0.5 + 5, pos.getY() + 0.5 + 5, pos.getZ() + 0.5 + 5));

        for (Entity entity : entities) {
            if (!entity.getType().is(ModTags.EntityTypes.BLACK_HOLE_AFFECTED)) {
                continue;
            }

            if (entity instanceof ItemEntity itemEntity && !blockEntity.isAffectedItem(itemEntity)) {
                continue;
            }

            double distance = entity.position().distanceTo(ModUtils.blockPosToVector(pos, 0.5));
            double movementFactor = blockEntity.getMovementFactor(distance);

            entity.push((pos.getX() + 0.5 - entity.getX()) * movementFactor, (pos.getY() + 0.5 - entity.getY() + 1.25) * movementFactor, (pos.getZ() + 0.5 - entity.getZ()) * movementFactor);

            if (distance <= DAMAGE_DISTANCE) {
                if (entity instanceof ExperienceOrb experienceOrb) {
                    blockEntity.stored_xp += experienceOrb.getValue();

                    if (blockEntity.stored_xp >= 60) {
                        blockEntity.throwOutItemStack(level, new ItemStack(ModItems.XPETRIFIED_ORB.get()), pos);
                        blockEntity.stored_xp = 0;
                    }
                    experienceOrb.kill();
                } else {
                    entity.hurt(DamageSource.MAGIC, 4);
                }
            }
        }

        blockEntity.thrownOutItems.removeIf(itemEntity -> !itemEntity.isAlive());
    }

    public boolean isAffectedItem(ItemEntity entity) {
        return !this.thrownOutItems.contains(entity) && !entity.getItem().is(ModTags.Items.BLACK_HOLE_UNAFFECTED);
    }

    private void throwOutItemStack(Level level, ItemStack stack, BlockPos pos) {
        pos = pos.offset(0.5D, 0.5D, 0.5D);

        ItemEntity item = new ItemEntity(level, pos.getX(), pos.getY(), pos.getZ(), stack);
        Player nearestPlayer = level.getNearestPlayer(pos.getX(), pos.getY(), pos.getZ(), PLAYER_SEARCH_DISTANCE, false);

        if (nearestPlayer == null) {
            this.setRandomVelocity(item, level.getRandom());
        } else {
            item.push((nearestPlayer.getX() - item.getX()) * 0.09, (nearestPlayer.getY() - item.getY() + 1.25) * 0.09, (nearestPlayer.getZ() - item.getZ()) * 0.09);
        }

        this.thrownOutItems.add(item);

        level.addFreshEntity(item);
    }

    private double getMovementFactor(double distance) {
        return distance <= 3 ? 0.035 : 0.02;
    }

    private void setRandomVelocity(ItemEntity itemEntity, Random random) {
        double x = random.nextDouble();
        double y = random.nextDouble();
        double z = random.nextDouble();

        itemEntity.setDeltaMovement(random.nextBoolean() ? x : -x, random.nextBoolean() ? y : -y, random.nextBoolean() ? z : -z);
    }

    @Override
    public void load(@Nonnull CompoundTag tag) {
        super.load(tag);
        this.stored_xp = tag.getDouble("StoredXP");
    }

    @Override
    public void saveAdditional(@Nonnull CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putDouble("StoredXP", this.stored_xp);
    }
}
