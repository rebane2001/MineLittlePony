package com.minelittlepony.client.render.entity.npc;

import com.minelittlepony.client.model.ModelType;
import com.minelittlepony.client.model.entity.VillagerPonyModel;

import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.passive.VillagerEntity;

public class VillagerPonyRenderer extends AbstractNpcRenderer<VillagerEntity, VillagerPonyModel<VillagerEntity>> {

    private static final String TYPE = "villager";
    private static final TextureSupplier<String> FORMATTER = TextureSupplier.formatted("minelittlepony", "textures/entity/villager/%s.png");

    public VillagerPonyRenderer(EntityRenderDispatcher manager) {
        super(manager, ModelType.VILLAGER, TYPE, FORMATTER);
    }

    @Override
    public void scale(VillagerEntity villager, MatrixStack stack, float ticks) {
        super.scale(villager, stack, ticks);
        stack.scale(BASE_MODEL_SCALE, BASE_MODEL_SCALE, BASE_MODEL_SCALE);
    }

}
