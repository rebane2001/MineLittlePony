package com.minelittlepony.client.model;

import net.minecraft.client.render.entity.model.SkullEntityModel;

import com.minelittlepony.mson.api.model.BoxBuilder;

public class DJPon3EarsModel extends SkullEntityModel {

    public DJPon3EarsModel() {
        super(24, 0, 64, 64);
        ((BoxBuilder.ContentAccessor)skull).cubes().clear();
        skull.addCuboid(-9, -13, -1, 6, 6, 1, 0);
        skull.addCuboid(3, -13, -1, 6, 6, 1, 0);
    }

    public void setVisible(boolean show) {
        skull.visible = show;
    }
}
