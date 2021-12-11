package com.stal111.forbidden_arcanus.item;

import com.stal111.forbidden_arcanus.init.ModBlocks;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BoneMealItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.InteractionResult;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;
import net.valhelsia.valhelsia_core.common.util.ItemStackUtils;

import javax.annotation.Nonnull;

/**
 * Arcane Bone Meal Item <br>
 * Forbidden Arcanus - com.stal111.forbidden_arcanus.item.ArcaneBoneMealItem
 *
 * @author stal111
 * @version 2.0.0
 * @since 2021-06-12
 */
public class ArcaneBoneMealItem extends BoneMealItem {

    public ArcaneBoneMealItem(Properties builder) {
        super(builder);
    }

    @Nonnull
    @Override
    public InteractionResult useOn(@Nonnull UseOnContext context) {
        Level world = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockPos offsetPos = pos.relative(context.getClickedFace());
        BlockState state = world.getBlockState(pos);
        Player player = context.getPlayer();

        if (state.getBlock() == Blocks.FARMLAND) {
            world.setBlockAndUpdate(pos, ModBlocks.MAGICAL_FARMLAND.get().defaultBlockState().setValue(BlockStateProperties.MOISTURE, state.getValue(BlockStateProperties.MOISTURE)));
            world.levelEvent(player, 2001, pos, Block.getId(state));

            ItemStackUtils.shrinkStack(player, context.getItemInHand());

            return InteractionResult.sidedSuccess(world.isClientSide);
        } else if (ArcaneBoneMealItem.applyBoneMeal(context.getItemInHand(), world, pos, player)) {
            if (!world.isClientSide) {
                world.levelEvent(2005, pos, 0);
            }

            return InteractionResult.sidedSuccess(world.isClientSide);
        } else {
            boolean flag = state.isFaceSturdy(world, pos, context.getClickedFace());

            if (flag && growWaterPlant(context.getItemInHand(), world, offsetPos, context.getClickedFace())) {
                if (!world.isClientSide) {
                    world.levelEvent(2005, offsetPos, 0);
                }

                return InteractionResult.sidedSuccess(world.isClientSide);
            } else {
                return InteractionResult.PASS;
            }
        }
    }

    public static boolean applyBoneMeal(ItemStack stack, Level world, BlockPos pos, Player player) {
        int hook = net.minecraftforge.event.ForgeEventFactory.onApplyBonemeal(player, world, pos, world.getBlockState(pos), stack);
        if (hook != 0) {
            return hook > 0;
        }

        if (canGrow(world, pos)) {
            grow(world, pos);

            stack.shrink(1);

            return true;
        }

        return false;
    }

    private static boolean canGrow(Level world, BlockPos pos) {
        BlockState state = world.getBlockState(pos);
        if (state.getBlock() instanceof BonemealableBlock) {
            return ((BonemealableBlock) state.getBlock()).isValidBonemealTarget(world, pos, state, world.isClientSide());
        }
        return false;
    }

    private static void grow(Level world, BlockPos pos) {
        if (world.isClientSide()) {
            return;
        }
        for (int i = 0; i < 1000; i++) {
            if (canGrow(world, pos) && !world.isClientSide()) {
                ((BonemealableBlock) world.getBlockState(pos).getBlock()).performBonemeal((ServerLevel) world, world.random, pos, world.getBlockState(pos));
            } else {
                return;
            }
        }
    }
}
