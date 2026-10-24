package com.whistles.item;

import com.whistles.ability.WhistleChannel;
import com.whistles.registry.ModMobEffects;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * The Frequency Whistle. Used like a goat horn (same arm animation), but once started it is forced to last 5 s,
 * emitting a Stasis shockwave every second. See {@link WhistleChannel} for the actual ability.
 */
public class FrequencyWhistleItem extends Item {
    /** Practically endless: the real 5 s duration is enforced server-side by WhistleChannel. */
    private static final int USE_DURATION = 72000;

    public FrequencyWhistleItem(Properties properties) {
        super(properties);
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.TOOT_HORN; // placeholder: the goat horn animation
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return USE_DURATION;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (player.getCooldowns().isOnCooldown(this) || player.hasEffect(ModMobEffects.STASIS)) {
            return InteractionResultHolder.fail(stack);
        }

        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
            if (WhistleChannel.isChanneling(serverPlayer)) {
                return InteractionResultHolder.fail(stack);
            }
            WhistleChannel.start(serverPlayer, hand);
        }

        return ItemUtils.startUsingInstantly(level, player, hand);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.whistles.frequency_whistle.1").withStyle(ChatFormatting.AQUA));
        tooltip.add(Component.translatable("tooltip.whistles.frequency_whistle.2").withStyle(ChatFormatting.GRAY));
    }
}