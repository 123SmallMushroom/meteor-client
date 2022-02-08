/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client/).
 * Copyright (c) 2021 Meteor Development.
 */

package meteordevelopment.meteorclient.systems.hud.modules;

import meteordevelopment.meteorclient.systems.hud.HUD;
import meteordevelopment.meteorclient.utils.Utils;

public class SpeedHud extends DoubleTextHudElement {
    public SpeedHud(HUD hud) {
        super(hud, "速度", "显示你的水平速度.", "速度: ");
    }

    @Override
    protected String getRight() {
        if (isInEditor()) return "0";

        return String.format("%.1f", Utils.getPlayerSpeed());
    }
}
