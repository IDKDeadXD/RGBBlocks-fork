package platinpython.rgbblocks.client.colorhandlers;

import net.minecraft.client.color.item.ItemColor;
import net.minecraft.world.item.ItemStack;
import platinpython.rgbblocks.item.RGBBlockItem;
import platinpython.rgbblocks.util.Color;
import platinpython.rgbblocks.util.registries.DataComponentRegistry;

public class RGBBlockItemColor implements ItemColor {
    public int getColor(ItemStack stack, int tintIndex) {
        if (stack.getItem() instanceof RGBBlockItem) {
            return Color.sanitizeRGB(stack.getOrDefault(DataComponentRegistry.COLOR, Color.DEFAULT_RGB));
        }
        return Color.DEFAULT_RGB;
    }
}
