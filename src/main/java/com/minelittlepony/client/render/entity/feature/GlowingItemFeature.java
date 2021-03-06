package com.minelittlepony.client.render.entity.feature;

import com.minelittlepony.client.model.IPonyModel;
import com.minelittlepony.client.render.IPonyRenderContext;
import com.minelittlepony.client.render.PonyRenderDispatcher;
import com.minelittlepony.model.IUnicorn;

import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Arm;

public class GlowingItemFeature<T extends LivingEntity, M extends EntityModel<T> & IPonyModel<T>> extends HeldItemFeature<T, M> {

    public GlowingItemFeature(IPonyRenderContext<T, M> context) {
        super(context);
    }

    protected boolean isUnicorn() {
        return getContextModel() instanceof IUnicorn<?> && ((IUnicorn<?>)getContextModel()).canCast();
    }

    @Override
    protected void preItemRender(T entity, ItemStack drop, ModelTransformation.Mode transform, Arm hand, MatrixStack stack) {
        if (isUnicorn()) {
            stack.translate(hand == Arm.LEFT ? -0.6F : 0, 0.5F, -0.3F);
        } else {
            super.preItemRender(entity, drop, transform, hand, stack);
        }
    }

    @Override
    protected void postItemRender(T entity, ItemStack drop, ModelTransformation.Mode transform, Arm hand, MatrixStack stack, VertexConsumerProvider renderContext) {
        if (isUnicorn()) {
            PonyRenderDispatcher.getInstance().getMagicRenderer().renderItemGlow(entity, drop, transform, hand, ((IUnicorn<?>)getContextModel()).getMagicColor(), stack, renderContext);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void renderArm(Arm arm, MatrixStack stack) {
        if (isUnicorn()) {
            ((IUnicorn<ModelPart>)getContextModel()).getUnicornArmForSide(arm).rotate(stack);
        } else {
            super.renderArm(arm, stack);
        }
    }
}
