package com.kadmuffin.bikesarepain.fabric;

import com.kadmuffin.bikesarepain.BikesArePain;
import com.kadmuffin.bikesarepain.packets.VersionCheckPacket;
import com.kadmuffin.bikesarepain.server.entity.AbstractBike;
import net.dehydration.access.ThirstManagerAccess;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerConfigurationConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerConfigurationNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.CommonColors;
import net.minecraft.world.entity.player.Player;

import static com.kadmuffin.bikesarepain.BikesArePain.*;

public final class BikesArePainFabric implements ModInitializer {
    private float thirstTick = 0;
    private float movingTicks = 0;

    @Override
    public void onInitialize() {
        // This code runs as soon as Minecraft is in a mod-load-ready state.
        // However, some things (like resources) may still be uninitialized.
        // Proceed with mild caution.

        // Run our common setup.
        BikesArePain.init();

        // Set up version checking
        ServerConfigurationConnectionEvents.CONFIGURE.register((handler, server) -> {
            if (ServerConfigurationNetworking.canSend(handler, VersionCheckPacket.S2CVersionRequest.TYPE)) {
                handler.addTask(new VersionCheckPacket.VersionCheckTask());
            } else {
                handler.disconnect(Component.literal("You need to install BikesArePain mod (").append(
                        Component.literal(String.format("v%s", VersionCheckPacket.getVersion())).withColor(CommonColors.BLUE).append(
                                Component.literal(") to join!")
                        )
                ));
            }
        });

        PayloadTypeRegistry.configurationC2S().register(
                VersionCheckPacket.C2SVersionShare.TYPE,
                StreamCodec.of((buf, obj) -> {
                    buf.writeInt(obj.major());
                    buf.writeInt(obj.minor());
                    buf.writeInt(obj.patch());
                }, buf -> new VersionCheckPacket.C2SVersionShare(buf.readInt(), buf.readInt(), buf.readInt()))
        );

        ServerConfigurationNetworking.registerGlobalReceiver(VersionCheckPacket.C2SVersionShare.TYPE, (payload, context) -> {
            boolean versionAccepted;
            try {
                versionAccepted = VersionCheckPacket.C2SVersionShare.isVersionSupported(payload);
            } catch (Exception e) {
                LOGGER.error(String.format("%s -> Server", MOD_NAME), e);
                context.networkHandler().disconnect(Component.literal(String.format(
                        "[%s] Something went wrong while parsing the version number in the server side.",
                        MOD_NAME
                )));
                context.networkHandler().completeTask(VersionCheckPacket.VersionCheckTask.TYPE);
                return;
            }

            if (versionAccepted) {
                LOGGER.info(String.format("%s version is valid.", MOD_NAME));
                context.networkHandler().completeTask(VersionCheckPacket.VersionCheckTask.TYPE);
            } else {
                LOGGER.info(String.format("%s version is not supported. Disconnecting.", MOD_NAME));
                context.networkHandler().disconnect(VersionCheckPacket.C2SVersionShare.getDisconnectMessage(payload));
            }
        });


        if (FabricLoader.getInstance().isModLoaded("dehydration")) {
            AbstractBike.addOnMoveListener((bike, speed, moving) -> {
                if (bike.getFirstPassenger() instanceof Player player) {
                    ThirstManagerAccess playerAcc = (ThirstManagerAccess) player;

                    if (moving) {
                        // Calculate bike health ratio
                        float healthRatio = bike.getHealth() / bike.getMaxHealth();

                        // Calculate reduced speed based on bike health
                        float reducedSpeed = speed * bike.getSpeedFactor(healthRatio);
                        float effortToSpeedRatio = Math.clamp(reducedSpeed / speed, 0, 1);
                        float effortPenalty = (float)Math.pow(1 - effortToSpeedRatio, 3);

                        // Increment ticks
                        thirstTick++;

                        float movementPenalty = 1.0F - (float)Math.exp(-movingTicks / 400.0);
                        movingTicks++;

                        if (thirstTick >= 20) {
                            thirstTick = 0;

                            float dehydrationAmount = getDehydrationAmount(effortPenalty, movementPenalty, healthRatio);

                            playerAcc.getThirstManager().addDehydration(dehydrationAmount);
                        }
                    } else {
                        movingTicks = Math.max(0, (int)(movingTicks * 0.5F));
                        thirstTick = 0;
                    }
                }
            });
        }
    }

    private static float getDehydrationAmount(float effortPenalty, float movementPenalty, float healthRatio) {
        float baseDehydration = 0.1F;
        float dehydrationAmount = baseDehydration +
                (effortPenalty * 1.5F) + // Increased penalty for reduced speed
                (movementPenalty * 0.5F); // Dehydration based on continuous movement

        // More dehydration when bike is close to broken
        float healthMultiplier = (float)Math.pow(1 - healthRatio, 2) * 2 + 1;
        dehydrationAmount *= healthMultiplier;
        return dehydrationAmount;
    }
}
