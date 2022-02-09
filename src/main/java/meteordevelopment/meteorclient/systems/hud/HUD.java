/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client/).
 * Copyright (c) 2022 Meteor Development.
 */

package meteordevelopment.meteorclient.systems.hud;

import meteordevelopment.meteorclient.events.render.Render2DEvent;
import meteordevelopment.meteorclient.gui.screens.HudEditorScreen;
import meteordevelopment.meteorclient.gui.screens.HudElementScreen;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.System;
import meteordevelopment.meteorclient.systems.Systems;
import meteordevelopment.meteorclient.systems.hud.modules.*;
import meteordevelopment.meteorclient.utils.misc.Keybind;
import meteordevelopment.meteorclient.utils.misc.NbtUtils;
import meteordevelopment.meteorclient.utils.render.AlignmentX;
import meteordevelopment.meteorclient.utils.render.AlignmentY;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import static meteordevelopment.meteorclient.MeteorClient.mc;

public class HUD extends System<HUD> {
    public final Settings settings = new Settings();

    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgEditor = settings.createGroup("编辑");

    public boolean active;

    // General

    public final Setting<Double> scale = sgGeneral.add(new DoubleSetting.Builder()
        .name("规模")
        .description("HUD 的规模.")
        .defaultValue(1)
        .min(0.75)
        .sliderRange(0.75, 4)
        .build()
    );

    public final Setting<SettingColor> primaryColor = sgGeneral.add(new ColorSetting.Builder()
        .name("主要颜色")
        .description("文字的主要颜色.")
        .defaultValue(new SettingColor(255, 255, 255))
        .build()
    );

    public final Setting<SettingColor> secondaryColor = sgGeneral.add(new ColorSetting.Builder()
        .name("次要颜色")
        .description("文字的次要颜色.")
        .defaultValue(new SettingColor(175, 175, 175))
        .build()
    );

    private final Setting<Keybind> toggleKeybind = sgGeneral.add(new KeybindSetting.Builder()
        .name("切换键合")
        .description("用于切换 HUD 的绑定键.")
        .defaultValue(Keybind.none())
        .action(() -> active = !active)
        .build()
    );

    // Editor

    public final Setting<Integer> snappingRange = sgEditor.add(new IntSetting.Builder()
        .name("捕捉范围")
        .description("在编辑器中捕捉范围.")
        .defaultValue(6)
        .build()
    );

    private final HudRenderer RENDERER = new HudRenderer();

    public final List<HudElement> elements = new ArrayList<>();
    public final HudElementLayer topLeft, topCenter, topRight, bottomLeft, bottomCenter, bottomRight;

    public final Runnable reset = () -> {
        align();
        elements.forEach(element -> {
            element.active = element.defaultActive;
            element.settings.forEach(group -> group.forEach(Setting::reset));
        });
    };

    public HUD() {
        super("hud");

        // Top Left
        topLeft = new HudElementLayer(RENDERER, elements, AlignmentX.Left, AlignmentY.Top, 2, 2);
        topLeft.add(new LogoHud(this));
        topLeft.add(new WatermarkHud(this));
        topLeft.add(new FpsHud(this));
        topLeft.add(new PingHud(this));
        topLeft.add(new TpsHud(this));
        topLeft.add(new SpeedHud(this));
        topLeft.add(new BiomeHud(this));
        topLeft.add(new TimeHud(this));
        topLeft.add(new ServerHud(this));
        topLeft.add(new DurabilityHud(this));
        topLeft.add(new BreakingBlockHud(this));
        topLeft.add(new LookingAtHud(this));
        topLeft.add(new ModuleInfoHud(this));
        topLeft.add(new TextRadarHud(this));

        // Top Center
        topCenter = new HudElementLayer(RENDERER, elements, AlignmentX.Center, AlignmentY.Top, 0, 2);
        topCenter.add(new InventoryViewerHud(this));
        topCenter.add(new WelcomeHud(this));
        topCenter.add(new LagNotifierHud(this));

        // Top Right
        topRight = new HudElementLayer(RENDERER, elements, AlignmentX.Right, AlignmentY.Top, 2, 2);
        topRight.add(new ActiveModulesHud(this));


        // Bottom Left
        bottomLeft = new HudElementLayer(RENDERER, elements, AlignmentX.Left, AlignmentY.Bottom, 2, 2);
        bottomLeft.add(new PlayerModelHud(this));

        // Bottom Center
        bottomCenter = new HudElementLayer(RENDERER, elements, AlignmentX.Center, AlignmentY.Bottom, 48, 64);
        bottomCenter.add(new ArmorHud(this));
        bottomCenter.add(new CompassHud(this));
        bottomCenter.add(new ContainerViewerHud(this));
        bottomCenter.add(new TotemHud(this));

        // Bottom Right
        bottomRight = new HudElementLayer(RENDERER, elements, AlignmentX.Right, AlignmentY.Bottom, 2, 2);
        bottomRight.add(new PositionHud(this));
        bottomRight.add(new RotationHud(this));
        bottomRight.add(new PotionTimersHud(this));
        bottomRight.add(new HoleHud(this));
        bottomRight.add(new CombatHud(this));

        align();
    }

    public static HUD get() {
        return Systems.get(HUD.class);
    }

    private void align() {
        RENDERER.begin(scale.get(), 0, true);

        topLeft.align();
        topCenter.align();
        topRight.align();
        bottomLeft.align();
        bottomCenter.align();
        bottomRight.align();

        RENDERER.end();
    }

    @EventHandler
    public void onRender(Render2DEvent event) {
        if (isEditorScreen()) {
            render(event.tickDelta, hudElement -> true);
        }
        else if (active && !mc.options.hudHidden && !mc.options.debugEnabled) {
            render(event.tickDelta, hudElement -> hudElement.active);
        }
    }

    public void render(float delta, Predicate<HudElement> shouldRender) {
        RENDERER.begin(scale.get(), delta, false);

        for (HudElement element : elements) {
            if (shouldRender.test(element)) {
                element.update(RENDERER);
                element.render(RENDERER);
            }
        }

        RENDERER.end();
    }

    public static boolean isEditorScreen() {
        return mc.currentScreen instanceof HudEditorScreen || mc.currentScreen instanceof HudElementScreen;
    }

    @Override
    public NbtCompound toTag() {
        NbtCompound tag = new NbtCompound();

        tag.putBoolean("活动", active);
        tag.put("设置", settings.toTag());
        tag.put("元素", NbtUtils.listToTag(elements));

        return tag;
    }

    @Override
    public HUD fromTag(NbtCompound tag) {
        settings.reset();

        if (tag.contains("活动")) active = tag.getBoolean("活动");
        if (tag.contains("设置")) settings.fromTag(tag.getCompound("设置"));
        if (tag.contains("元素")) {
            NbtList elementsTag = tag.getList("元素", 10);

            for (NbtElement t : elementsTag) {
                NbtCompound elementTag = (NbtCompound) t;

                for (HudElement element : elements) {
                    if (element.name.equals(elementTag.getString("name"))) {
                        element.fromTag(elementTag);
                        break;
                    }
                }
            }
        }

        return super.fromTag(tag);
    }
}
