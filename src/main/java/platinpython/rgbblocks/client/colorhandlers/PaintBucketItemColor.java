package platinpython.rgbblocks.client.colorhandlers;

import net.minecraft.client.color.item.ItemColor;
import net.minecraft.world.item.ItemStack;
import platinpython.rgbblocks.item.PaintBucketItem;
import platinpython.rgbblocks.util.Color;
import platinpython.rgbblocks.util.registries.DataComponentRegistry;

public class PaintBucketItemColor implements ItemColor {
    @Override
    public int getColor(ItemStack stack, int tintIndex) {
        if (stack.getItem() instanceof PaintBucketItem) {
            if (tintIndex == 1) {
                return Color.sanitizeRGB(stack.getOrDefault(DataComponentRegistry.COLOR, Color.DEFAULT_RGB));
            }
        }
        return Color.DEFAULT_RGB;
    }
}
