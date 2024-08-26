package com.kadmuffin.bikesarepain.packets;

import com.kadmuffin.bikesarepain.server.GameRuleManager;
import com.kadmuffin.bikesarepain.server.entity.AbstractBike;
import com.kadmuffin.bikesarepain.server.entity.Bicycle;
import dev.architectury.networking.NetworkManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

import static com.kadmuffin.bikesarepain.BikesArePain.MOD_ID;

public class PacketManager {
    public record ArduinoData(float speed, float distanceMoved, float kcalories, float wheelCircumference, float scaleFactor) implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<ArduinoData> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(MOD_ID, "arduino_data"));
        public static final StreamCodec<RegistryFriendlyByteBuf, ArduinoData> CODEC = StreamCodec.of(
                (buf, obj) -> {
                    buf.writeFloat(obj.speed);
                    buf.writeFloat(obj.distanceMoved);
                    buf.writeFloat(obj.kcalories);
                    buf.writeFloat(obj.wheelCircumference);
                    buf.writeFloat(obj.scaleFactor);
                },
                buf -> new ArduinoData(buf.readFloat(), buf.readFloat(), buf.readFloat(), buf.readFloat(), buf.readFloat())
        );
        public static final NetworkManager.NetworkReceiver<ArduinoData> RECEIVER = (packet, context) -> {
            Player player = context.getPlayer();
            if (player != null){
                if (player.getVehicle() instanceof AbstractBike bike) {
                    System.out.println("Received data from Arduino: " + packet.speed + " " + packet.distanceMoved + " " + packet.kcalories + " " + packet.wheelCircumference);
                    // Scale factor is controlled by the player by running
                    // /bikes scale set <factor1> block is <factor2> meter
                    // The physics code for the bike work on the basis of 1 block is 1 meter
                    // This command allows players to make their bike smaller or larger,
                    // typically a bike's wheel has a radius of 0.3 meters, and that i
                    // quite small in the game, so we will allow player to scale the bike
                    // without modifying the physics code

                    // The factor value results of taking the two parameters:
                    // - <factor1>
                    // - <factor2>
                    // and calculating a ratio.

                    // Read the gamerule limit
                    float scaleFactor = packet.scaleFactor;
                    if (bike.level().getGameRules().getRule(GameRuleManager.MAX_BIKE_SCALING).get() < packet.scaleFactor) {
                        scaleFactor = bike.level().getGameRules().getRule(GameRuleManager.MAX_BIKE_SCALING).get();
                    }
                    if (bike.level().getGameRules().getRule(GameRuleManager.MIN_BIKE_SCALING).get() > packet.scaleFactor) {
                        scaleFactor = bike.level().getGameRules().getRule(GameRuleManager.MIN_BIKE_SCALING).get()/10F;
                    }

                    bike.setjCommaSpeed(packet.speed * scaleFactor);
                    bike.setDistanceTravelled(packet.distanceMoved);
                    bike.setCaloriesBurned(packet.kcalories);
                    bike.setSerialWheelRadius(packet.wheelCircumference * scaleFactor);
                    bike.setjCommaEnabled(true);
                }
            }
        };

        @Override
        public @NotNull Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

        public record RingBellPacket(boolean isPressed) implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<RingBellPacket> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(MOD_ID, "ringbell_click"));
        public static final StreamCodec<FriendlyByteBuf, RingBellPacket> CODEC = StreamCodec.of((buf, obj) -> {
            buf.writeBoolean(obj.isPressed);
        }, buf -> new RingBellPacket(buf.readBoolean()));

        public static final NetworkManager.NetworkReceiver<RingBellPacket> RECEIVER = (packet, contextSupplier) -> {
            Player player = contextSupplier.getPlayer();
            if (player != null) {
                Bicycle bike = player.getVehicle() instanceof Bicycle ? (Bicycle) player.getVehicle() : null;
                if (bike != null) {
                    if (!bike.wasRingedAlready && packet.isPressed) {
                        bike.wasRingedAlready = true;
                        bike.ringBell();
                    }

                    if (!packet.isPressed) {
                        bike.wasRingedAlready = false;
                    }
                }
            }
        };

        @Override
        public @NotNull Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public static void init() {
        NetworkManager.registerReceiver(NetworkManager.Side.C2S, RingBellPacket.TYPE, RingBellPacket.CODEC, RingBellPacket.RECEIVER);
        NetworkManager.registerReceiver(
                NetworkManager.c2s(),
                PacketManager.ArduinoData.TYPE,
                PacketManager.ArduinoData.CODEC,
                PacketManager.ArduinoData.RECEIVER);
    }
}
