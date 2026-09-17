package platinpython.rgbblocks.dispenser;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import platinpython.rgbblocks.block.entity.RGBBlockEntity;
import platinpython.rgbblocks.util.Color;
import platinpython.rgbblocks.util.registries.DataComponentRegistry;

public class DispensePaintBucketBehaviour extends DefaultDispenseItemBehavior {
    @SuppressWarnings("resource")
    @Override
    protected ItemStack execute(BlockSource source, ItemStack itemStack) {
        Direction dispenserFacing = source.state().getValue(DispenserBlock.FACING);
        BlockPos blockPos = source.pos().relative(dispenserFacing);
        BlockEntity blockEntity = source.level().getBlockEntity(blockPos);
        if (blockEntity instanceof RGBBlockEntity rgbBlockEntity) {
            int color = Color.sanitizeRGB(itemStack.getOrDefault(DataComponentRegistry.COLOR, Color.DEFAULT_RGB));
            boolean broke = false;
            if (color != rgbBlockEntity.getColor()) {
                if (itemStack.getDamageValue() == itemStack.getMaxDamage() - 1) {
                    broke = true;
                } else {
                    itemStack.hurtAndBreak(1, source.level(), null, item -> {});
                }
            }
            rgbBlockEntity.setColorAndSync(color);
            return broke ? new ItemStack(Items.BUCKET) : itemStack;
        } else {
            return super.execute(source, itemStack);
        }
    }
}
