package platinpython.rgbblocks.item;

import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.fml.ModList;
import platinpython.rgbblocks.RGBBlocks;
import platinpython.rgbblocks.block.entity.RGBBlockEntity;
import platinpython.rgbblocks.util.Color;
import platinpython.rgbblocks.util.compat.framedblocks.RGBBlocksFramedBlocks;
import platinpython.rgbblocks.util.registries.DataComponentRegistry;

import java.util.List;

public class PaintBucketItem extends Item {
    public PaintBucketItem() {
        super(
            new Properties().durability(500)
                .setNoRepair()
                .component(DataComponentRegistry.COLOR, -1)
                .component(DataComponentRegistry.RGB_SELECTED, true)
        );
    }

    @Override
    public void verifyComponentsAfterLoad(ItemStack stack) {
        super.verifyComponentsAfterLoad(stack);
        if (stack.has(DataComponents.CUSTOM_DATA)) {
            stack.update(DataComponents.CUSTOM_DATA, CustomData.EMPTY, customData -> customData.update(tag -> {
                if (tag.contains("color")) {
                    stack.set(DataComponentRegistry.COLOR, Color.sanitizeRGB(tag.getInt("color")));
                    tag.remove("color");
                }
                if (tag.contains("isRGBSelected")) {
                    stack.set(DataComponentRegistry.RGB_SELECTED, tag.getBoolean("isRGBSelected"));
                    tag.remove("isRGBSelected");
                }
            }));
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flagIn) {
        Color color = new Color(stack.getOrDefault(DataComponentRegistry.COLOR, Color.DEFAULT_RGB));
        tooltip.add(Component.literal("#" + Color.toHexString(color.getRGB())));
        tooltip.add(Component.translatable("tooltip.rgbblocks.paint_bucket.copy"));
        if (flagIn.isAdvanced()) {
            MutableComponent red = Component.translatable("gui.rgbblocks.red").append(": " + color.getRed());
            MutableComponent green = Component.translatable("gui.rgbblocks.green").append(": " + color.getGreen());
            MutableComponent blue = Component.translatable("gui.rgbblocks.blue").append(": " + color.getBlue());
            tooltip.add(red.append(", ").append(green).append(", ").append(blue));
            float[] hsb = Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue());
            MutableComponent hue =
                Component.translatable("gui.rgbblocks.hue").append(": " + Math.round(hsb[0] * Color.MAX_VALUE_HUE));
            MutableComponent saturation = Component.translatable("gui.rgbblocks.saturation")
                .append(": " + Math.round(hsb[1] * Color.MAX_VALUE_SB));
            MutableComponent brightness = Component.translatable("gui.rgbblocks.brightness")
                .append(": " + Math.round(hsb[2] * Color.MAX_VALUE_SB));
            tooltip.add(hue.append("°, ").append(saturation).append("%, ").append(brightness).append("%"));
        }
    }

    @Override
    public int getMaxDamage(ItemStack stack) {
        return super.getMaxDamage(stack);
    }

    @Override
    public void setDamage(ItemStack stack, int damage) {
        super.setDamage(stack, damage);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player playerIn, InteractionHand handIn) {
        if (handIn == InteractionHand.MAIN_HAND && playerIn.isShiftKeyDown()) {
            if (level.isClientSide) {
                openColorSelectScreen(
                    playerIn.getMainHandItem().getOrDefault(DataComponentRegistry.COLOR, Color.DEFAULT_RGB),
                    playerIn.getMainHandItem().getOrDefault(DataComponentRegistry.RGB_SELECTED, true)
                );
                return new InteractionResultHolder<>(InteractionResult.SUCCESS, playerIn.getMainHandItem());
            }
        }
        return new InteractionResultHolder<>(InteractionResult.PASS, playerIn.getItemInHand(handIn));
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        BlockEntity blockEntity = context.getLevel().getBlockEntity(context.getClickedPos());
        if (blockEntity instanceof RGBBlockEntity rgbBlockEntity) {
            Player player = context.getPlayer();
            if (player != null && player.isShiftKeyDown()) {
                context.getItemInHand().set(DataComponentRegistry.COLOR, rgbBlockEntity.getColor());
            } else {
                int color = Color
                    .sanitizeRGB(context.getItemInHand().getOrDefault(DataComponentRegistry.COLOR, Color.DEFAULT_RGB));
                if (player != null && !player.isCreative() && color != rgbBlockEntity.getColor()) {
                    if (context.getItemInHand().getDamageValue() == context.getItemInHand().getMaxDamage() - 1) {
                        player.setItemInHand(context.getHand(), new ItemStack(Items.BUCKET));
                    } else {
                        context.getItemInHand().hurtAndBreak(1, player, LivingEntity.getSlotForHand(context.getHand()));
                    }
                }
                rgbBlockEntity.setColorAndSync(color);
            }
            return InteractionResult.SUCCESS;
        }
        if (ModList.get().isLoaded("framedblocks")) {
            return RGBBlocksFramedBlocks.handlePaintBucketInteraction(context);
        }
        return InteractionResult.PASS;
    }

    @Override
    public boolean isBookEnchantable(ItemStack stack, ItemStack book) {
        return false;
    }

    private static void openColorSelectScreen(int color, boolean isRGBSelected) {
        try {
            Class<?> clientUtils = Class.forName("platinpython.rgbblocks.util.ClientUtils");
            clientUtils.getMethod("openColorSelectScreen", int.class, boolean.class)
                .invoke(null, Color.sanitizeRGB(color), isRGBSelected);
        } catch (ReflectiveOperationException e) {
            RGBBlocks.LOGGER.error("Failed to open RGB Blocks paint bucket color selector", e);
        }
    }
}
