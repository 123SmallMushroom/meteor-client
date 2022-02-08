/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client/).
 * Copyright (c) 2021 Meteor Development.
 */

package meteordevelopment.meteorclient.systems.commands.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import meteordevelopment.meteorclient.systems.commands.Command;
import meteordevelopment.meteorclient.systems.commands.arguments.ProfileArgumentType;
import meteordevelopment.meteorclient.systems.profiles.Profile;
import meteordevelopment.meteorclient.systems.profiles.Profiles;
import net.minecraft.command.CommandSource;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;

public class ProfilesCommand extends Command {

    public ProfilesCommand() {
        super("profiles", "加载和保存配置文件.");
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.then(literal("load").then(argument("profile", ProfileArgumentType.profile()).executes(context -> {
            Profile profile = ProfileArgumentType.getProfile(context, "profile");

            if (profile != null) {
                profile.load();
                info("Loaded profile (highlight)%s(default).", profile.name);
            }

            return SINGLE_SUCCESS;
        })));

        builder.then(literal("save").then(argument("profile", ProfileArgumentType.profile()).executes(context -> {
            Profile profile = ProfileArgumentType.getProfile(context, "profile");

            if (profile != null) {
                profile.save();
                info("Saved profile (highlight)%s(default).", profile.name);
            }

            return SINGLE_SUCCESS;
        })));

        builder.then(literal("delete").then(argument("profile", ProfileArgumentType.profile()).executes(context -> {
            Profile profile = ProfileArgumentType.getProfile(context, "profile");

            if (profile != null) {
                Profiles.get().remove(profile);
                info("Deleted profile (highlight)%s(default).", profile.name);
            }

            return SINGLE_SUCCESS;
        })));
    }
}
