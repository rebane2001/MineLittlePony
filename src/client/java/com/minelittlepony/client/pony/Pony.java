package com.minelittlepony.client.pony;

import com.google.common.base.MoreObjects;
import com.minelittlepony.MineLittlePony;
import com.minelittlepony.client.PonyRenderManager;
import com.minelittlepony.client.ducks.IRenderPony;
import com.minelittlepony.client.transform.PonyTransformation;
import com.minelittlepony.hdskins.resources.texture.DynamicTextureImage;
import com.minelittlepony.hdskins.resources.texture.IBufferedTexture;
import com.minelittlepony.hdskins.util.ProfileTextureUtil;
import com.minelittlepony.pony.IPony;
import com.minelittlepony.pony.IPonyData;
import com.minelittlepony.pony.meta.Race;
import com.minelittlepony.pony.meta.Size;
import com.minelittlepony.util.chron.Touchable;

import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.ITextureObject;
import net.minecraft.client.renderer.texture.MissingTextureSprite;
import net.minecraft.client.renderer.texture.NativeImage;
import net.minecraft.resources.IResource;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

@Immutable
public class Pony extends Touchable<Pony> implements IPony {

    private static final AtomicInteger ponyCount = new AtomicInteger();

    private final int ponyId = ponyCount.getAndIncrement();

    private final ResourceLocation texture;
    private final IPonyData metadata;

    public Pony(ResourceLocation resource) {
        texture = resource;
        metadata = checkSkin(texture);
    }

    private IPonyData checkSkin(ResourceLocation resource) {
        IPonyData data = checkPonyMeta(resource);
        if (data != null) {
            return data;
        }

        NativeImage ponyTexture = getBufferedImage(resource);

        if (ponyTexture == null) {
            ponyTexture = ProfileTextureUtil.getDynamicBufferedImage(16, 16, MissingTextureSprite.getDynamicTexture());

            Minecraft.getInstance().getTextureManager().loadTexture(resource, new DynamicTextureImage(ponyTexture));
        }

        return checkSkin(ponyTexture);
    }

    @Nullable
    private IPonyData checkPonyMeta(ResourceLocation resource) {
        try {
            IResource res = Minecraft.getInstance().getResourceManager().getResource(resource);

            if (res.hasMetadata()) {
                PonyData data = res.getMetadata(PonyData.SERIALISER);

                if (data != null) {
                    return data;
                }
            }
        } catch (FileNotFoundException e) {
            // Ignore uploaded texture
        } catch (IOException e) {
            MineLittlePony.logger.warn("Unable to read {} metadata", resource, e);
        }

        return null;
    }

    @Nullable
    public static NativeImage getBufferedImage(@Nonnull ResourceLocation resource) {
        try {
            IResource skin = Minecraft.getInstance().getResourceManager().getResource(resource);
            NativeImage skinImage = NativeImage.read(skin.getInputStream());
            MineLittlePony.logger.debug("Obtained skin from resource location {}", resource);

            return skinImage;
        } catch (IOException ignored) {
        }

        ITextureObject texture = Minecraft.getInstance().getTextureManager().getTexture(resource);

        if (texture instanceof IBufferedTexture) {
            return ((IBufferedTexture) texture).getBufferedImage();
        }

        return null;
    }

    private IPonyData checkSkin(NativeImage bufferedimage) {
        MineLittlePony.logger.debug("\tStart skin check for pony #{} with image {}.", ponyId, bufferedimage);
        return PonyData.parse(bufferedimage);
    }

    @Override
    public boolean isPerformingRainboom(EntityLivingBase entity) {
        double zMotion = Math.sqrt(entity.motionX * entity.motionX + entity.motionZ * entity.motionZ);

        return (isFlying(entity) && canFly()) || entity.isElytraFlying() & zMotion > 0.4F;
    }

    @Override
    public boolean isCrouching(EntityLivingBase entity) {

        boolean isSneak = entity.isSneaking();
        boolean isFlying = isFlying(entity);
        boolean isSwimming = isSwimming(entity);

        return !isPerformingRainboom(entity) && !isSwimming && isSneak && !isFlying;
    }

    @Override
    public boolean isFlying(EntityLivingBase entity) {
        return !(entity.onGround
                || entity.getRidingEntity() != null
                || (entity.isOnLadder() && !(entity instanceof EntityPlayer && ((EntityPlayer)entity).abilities.isFlying))
                || entity.isInWater()
                || entity.isPlayerSleeping());
    }

    @Override
    public boolean isSwimming(EntityLivingBase entity) {
        return isFullySubmerged(entity) && !(entity.onGround || entity.isOnLadder());
    }

    @Override
    public boolean isPartiallySubmerged(EntityLivingBase entity) {
        return entity.isInWater()
                || entity.getEntityWorld().getBlockState(new BlockPos(entity.posX, entity.posY, entity.posZ)).getMaterial() == Material.WATER;
    }

    @Override
    public boolean isFullySubmerged(EntityLivingBase entity) {
        return entity.isInWater()
                && entity.getEntityWorld().getBlockState(new BlockPos(getVisualEyePosition(entity))).getMaterial() == Material.WATER;
    }

    protected Vec3d getVisualEyePosition(EntityLivingBase entity) {
        Size size = entity.isChild() ? Size.FOAL : metadata.getSize();

        return new Vec3d(entity.posX, entity.posY + (double) entity.getEyeHeight() * size.getScaleFactor(), entity.posZ);
    }

    @Override
    public boolean isWearingHeadgear(EntityLivingBase entity) {
        ItemStack stack = entity.getItemStackFromSlot(EntityEquipmentSlot.HEAD);

        if (stack.isEmpty()) {
            return false;
        }

        Item item = stack.getItem();

        return !(item instanceof ItemArmor) || ((ItemArmor) item).getEquipmentSlot() != EntityEquipmentSlot.HEAD;
    }

    @Override
    public Race getRace(boolean ignorePony) {
        return metadata.getRace().getEffectiveRace(ignorePony);
    }

    @Override
    public ResourceLocation getTexture() {
        return texture;
    }

    @Override
    public IPonyData getMetadata() {
        return metadata;
    }

    @Override
    public boolean isRidingInteractive(EntityLivingBase entity) {
        return PonyRenderManager.getInstance().getPonyRenderer(entity.getRidingEntity()) != null;
    }

    @Override
    public IPony getMountedPony(EntityLivingBase entity) {
        Entity mount = entity.getRidingEntity();

        IRenderPony<EntityLivingBase> render = PonyRenderManager.getInstance().getPonyRenderer(mount);

        return render == null ? null : render.getEntityPony((EntityLivingBase)mount);
    }

    @Override
    public Vec3d getAbsoluteRidingOffset(EntityLivingBase entity) {
        IPony ridingPony = getMountedPony(entity);



        if (ridingPony != null) {
            EntityLivingBase ridee = (EntityLivingBase)entity.getRidingEntity();

            Vec3d offset = PonyTransformation.forSize(ridingPony.getMetadata().getSize()).getRiderOffset();
            float scale = ridingPony.getMetadata().getSize().getScaleFactor();

            return ridingPony.getAbsoluteRidingOffset(ridee)
                    .add(0, offset.y - ridee.height * 1/scale, 0);
        }

        return entity.getPositionVector();
    }

    @Override
    public AxisAlignedBB getComputedBoundingBox(EntityLivingBase entity) {
        float scale = getMetadata().getSize().getScaleFactor() + 0.1F;

        Vec3d pos = getAbsoluteRidingOffset(entity);

        float width = entity.width * scale;

        return new AxisAlignedBB(
                - width, (entity.height * scale), -width,
                  width, 0,                        width).offset(pos);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("texture", texture)
                .add("metadata", metadata)
                .toString();
    }
}