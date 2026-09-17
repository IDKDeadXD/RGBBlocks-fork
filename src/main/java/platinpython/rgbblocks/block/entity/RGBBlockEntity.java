package platinpython.rgbblocks.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NumericTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import platinpython.rgbblocks.util.Color;
import platinpython.rgbblocks.util.registries.BlockEntityRegistry;
import platinpython.rgbblocks.util.registries.DataComponentRegistry;

public class RGBBlockEntity extends BlockEntity {
    private int color = Color.DEFAULT_RGB;
    private MapColor mapColor = Color.getNearestMapColor(Color.DEFAULT_RGB);

    public RGBBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntityRegistry.RGB.get(), pos, state);
    }

    public boolean setColor(int color) {
        int sanitizedColor = Color.sanitizeRGB(color);
        if (this.color == sanitizedColor) {
            return false;
        }
        this.color = sanitizedColor;
        this.mapColor = Color.getNearestMapColor(this.color);
        setChanged();
        return true;
    }

    public void setColorAndSync(int color) {
        if (setColor(color) && this.level != null) {
            this.level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    public int getColor() {
        return color;
    }

    public MapColor getMapColor() {
        return mapColor;
    }

    @Override
    protected void applyImplicitComponents(DataComponentInput componentInput) {
        super.applyImplicitComponents(componentInput);
        setColor(componentInput.getOrDefault(DataComponentRegistry.COLOR, Color.DEFAULT_RGB));
    }

    @Override
    protected void collectImplicitComponents(DataComponentMap.Builder builder) {
        super.collectImplicitComponents(builder);
        builder.set(DataComponentRegistry.COLOR, Color.sanitizeRGB(this.color));
    }

    @Override
    public void saveAdditional(CompoundTag compound, HolderLookup.Provider provider) {
        super.saveAdditional(compound, provider);
        compound.putInt("color", getColor());
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.loadAdditional(tag, provider);
        setColor(readColor(tag));
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider provider) {
        CompoundTag tag = super.getUpdateTag(provider);
        tag.putInt("color", Color.sanitizeRGB(color));
        return tag;
    }

    @Override
    public void handleUpdateTag(CompoundTag tag, HolderLookup.Provider provider) {
        super.handleUpdateTag(tag, provider);
        setColor(readColor(tag));
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket packet, HolderLookup.Provider provider) {
        CompoundTag tag = packet.getTag();
        int updatedColor = tag != null ? readColor(tag) : Color.DEFAULT_RGB;
        if (setColor(updatedColor) && this.level != null) {
            this.level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    private static int readColor(CompoundTag tag) {
        if (tag.contains("color", Tag.TAG_ANY_NUMERIC)) {
            return Color.sanitizeRGB(tag.getInt("color"));
        }
        if (tag.contains("color", Tag.TAG_STRING)) {
            return Color.parseHexRGB(tag.getString("color")).orElse(Color.DEFAULT_RGB);
        }
        if (tag.contains("hex", Tag.TAG_STRING)) {
            return Color.parseHexRGB(tag.getString("hex")).orElse(Color.DEFAULT_RGB);
        }
        if (tag.contains("rgb", Tag.TAG_INT_ARRAY)) {
            int[] rgb = tag.getIntArray("rgb");
            if (rgb.length >= 3) {
                return Color.fromRGBComponents(rgb[0], rgb[1], rgb[2]);
            }
        }
        Tag rgbTag = tag.get("rgb");
        if (rgbTag instanceof ListTag rgb && rgb.size() >= 3 && rgb.get(0) instanceof NumericTag red
            && rgb.get(1) instanceof NumericTag green && rgb.get(2) instanceof NumericTag blue) {
            return Color.fromRGBComponents(red.getAsInt(), green.getAsInt(), blue.getAsInt());
        }
        if (tag.contains("red", Tag.TAG_ANY_NUMERIC) && tag.contains("green", Tag.TAG_ANY_NUMERIC)
            && tag.contains("blue", Tag.TAG_ANY_NUMERIC)) {
            return Color.fromRGBComponents(tag.getInt("red"), tag.getInt("green"), tag.getInt("blue"));
        }
        return Color.DEFAULT_RGB;
    }
}
