/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client/).
 * Copyright (c) 2021 Meteor Development.
 */

package meteordevelopment.meteorclient.systems.config;

import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.renderer.Fonts;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.System;
import meteordevelopment.meteorclient.systems.Systems;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;

import java.util.ArrayList;
import java.util.List;

import static meteordevelopment.meteorclient.MeteorClient.mc;

public class Config extends System<Config> {
    public final Settings settings = new Settings();

    private final SettingGroup sgVisual = settings.createGroup("视觉的");
    private final SettingGroup sgChat = settings.createGroup("聊天");
    private final SettingGroup sgMisc = settings.createGroup("杂项");

    // Visual

    public final Setting<Boolean> customFont = sgVisual.add(new BoolSetting.Builder()
        .name("自定义字体")
        .description("使用自定义字体.")
        .defaultValue(true)
        .build()
    );

    public final Setting<String> font = sgVisual.add(new ProvidedStringSetting.Builder()
        .name("字体")
        .description("Custom font to use (picked from .minecraft/" + MeteorClient.MOD_ID + "/fonts folder).")
        .visible(customFont::get)
        .supplier(Fonts::getAvailableFonts)
        .defaultValue(Fonts.DEFAULT_FONT)
        .onChanged(s -> Fonts.load())
        .build()
    );

    public final Setting<Double> rainbowSpeed = sgVisual.add(new DoubleSetting.Builder()
        .name("彩虹速度")
        .description("全球彩虹速度.")
        .defaultValue(0.5)
        .range(0, 10)
        .sliderMax(5)
        .build()
    );

    public final Setting<Boolean> titleScreenCredits = sgVisual.add(new BoolSetting.Builder()
        .name("标题屏幕学分")
        .description("在标题屏幕上显示 Meteor 学分")
        .defaultValue(true)
        .build()
    );

    public final Setting<Boolean> titleScreenSplashes = sgVisual.add(new BoolSetting.Builder()
        .name("标题屏幕飞溅")
        .description("在标题屏幕上显示流星启动文本")
        .defaultValue(true)
        .build()
    );

    public final Setting<Boolean> customWindowTitle = sgVisual.add(new BoolSetting.Builder()
        .name("自定义窗口标题")
        .description("在窗口标题中显示自定义文本.")
        .defaultValue(false)
        .onModuleActivated(setting -> mc.updateWindowTitle())
        .onChanged(value -> mc.updateWindowTitle())
        .build()
    );

    public final Setting<String> customWindowTitleText = sgVisual.add(new StringSetting.Builder()
        .name("窗口标题文本")
        .description("它在窗口标题中显示的文本.")
        .visible(customWindowTitle::get)
        .defaultValue("Minecraft {mc_version} - Meteor Client {version}")
        .onChanged(value -> mc.updateWindowTitle())
        .build()
    );

    // Chat

    public final Setting<String> prefix = sgChat.add(new StringSetting.Builder()
        .name("字首")
        .description("字首.")
        .defaultValue(".")
        .build()
    );

    public final Setting<Boolean> chatFeedback = sgChat.add(new BoolSetting.Builder()
        .name("聊天反馈")
        .description("当流星执行某些操作时发送聊天反馈.")
        .defaultValue(true)
        .build()
    );

    public final Setting<Boolean> deleteChatFeedback = sgChat.add(new BoolSetting.Builder()
        .name("删除聊天反馈")
        .description("删除以前匹配的聊天反馈以保持聊天清晰.")
        .visible(chatFeedback::get)
        .defaultValue(true)
        .build()
    );

    // Misc

    public final Setting<Integer> rotationHoldTicks = sgMisc.add(new IntSetting.Builder()
        .name("旋转保持")
        .description("在不发送任何数据包时保持长时间以保持服务器端轮换.")
        .defaultValue(4)
        .build()
    );

    public final Setting<Boolean> useTeamColor = sgMisc.add(new BoolSetting.Builder()
        .name("使用团队颜色")
        .description("使用玩家的团队颜色来渲染诸如 esp 和跟踪器之类的东西.")
        .defaultValue(true)
        .build()
    );

    public List<String> dontShowAgainPrompts = new ArrayList<>();

    public Config() {
        super("config");
    }

    public static Config get() {
        return Systems.get(Config.class);
    }

    @Override
    public NbtCompound toTag() {
        NbtCompound tag = new NbtCompound();

        tag.putString("version", MeteorClient.VERSION.toString());
        tag.put("settings", settings.toTag());
        tag.put("dontShowAgainPrompts", listToTag(dontShowAgainPrompts));

        return tag;
    }

    @Override
    public Config fromTag(NbtCompound tag) {
        if (tag.contains("settings")) settings.fromTag(tag.getCompound("settings"));
        if (tag.contains("don'tShowAgainPrompts")) dontShowAgainPrompts = listFromTag(tag, "dontShowAgainPrompts");

        return this;
    }

    private NbtList listToTag(List<String> list) {
        NbtList nbt = new NbtList();
        for (String item : list) nbt.add(NbtString.of(item));
        return nbt;
    }

    private List<String> listFromTag(NbtCompound tag, String key) {
        List<String> list = new ArrayList<>();
        for (NbtElement item : tag.getList(key, 8)) list.add(item.asString());
        return list;
    }
}
