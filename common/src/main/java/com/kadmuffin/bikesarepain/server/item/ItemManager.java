package com.kadmuffin.bikesarepain.server.item;

import com.kadmuffin.bikesarepain.BikesArePain;
import com.kadmuffin.bikesarepain.client.entity.BicycleRenderer;
import com.kadmuffin.bikesarepain.client.helper.Utils;
import com.kadmuffin.bikesarepain.client.item.BaseItemRenderer;
import com.kadmuffin.bikesarepain.server.entity.Bicycle;
import com.kadmuffin.bikesarepain.server.entity.EntityManager;
import com.mojang.datafixers.types.Func;
import com.mojang.serialization.Codec;
import dev.architectury.registry.CreativeTabRegistry;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.*;
import software.bernie.geckolib.util.Color;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class ItemManager {
    public static final List<Integer> bicycleColors = List.of(
            14269837,
            14269837,
            14269837,
            14269837);



    private static int getBicycleItemColor(ItemStack item, int index) {
        if (index < 0 || index >= 4) {
            return 0;
        }
        return Utils.completeRest(item.getComponents().getOrDefault(BICYCLE_COLORS.get(), bicycleColors), bicycleColors).get(index);
    }

    private static int getBicyclePartColor(ItemStack item, int index) {
        if (index < 0 || index >= 4) {
            return 0;
        }

        return item.getComponents().getOrDefault(COLOR.get(), bicycleColors.get(index));
    }

    public static final RegistrySupplier<CreativeModeTab> BIKES_MOD_TAB = BikesArePain.TABS.register("bikes_mod_tab", () ->
            CreativeTabRegistry.create(
                    Component.literal("Bikes Are Pain"),
                    () -> new ItemStack(ItemManager.BICYCLE_ITEM.get())
            )
    );
    public static final RegistrySupplier<DataComponentType<Integer>> COLOR = BikesArePain.DATA_COMPONENTS.register(
            ResourceLocation.fromNamespaceAndPath(BikesArePain.MOD_ID, "color"),
            () -> DataComponentType.<Integer>builder().persistent(Codec.INT).build()
    );
    public static final RegistrySupplier<DataComponentType<List<Integer>>> BICYCLE_COLORS = BikesArePain.DATA_COMPONENTS.register(
            ResourceLocation.fromNamespaceAndPath(BikesArePain.MOD_ID, "colors"),
            () -> DataComponentType.<List<Integer>>builder().persistent(Codec.INT.listOf()).build()
    );

    public static final Map<String, Function<ItemStack, Integer>> bonesToColorBicycleItem = Utils.createBonesToColorMap(
            Map.of(
                    List.of("hexadecagon"), (item) -> getBicycleItemColor(item, 0),
                    List.of("hexadecagon3"), (item) -> getBicycleItemColor(item, 1),
                    List.of("hexadecagon2", "Support", "ClickyThing", "ItemInventory", "Chest", "BladesF", "BladesB", "Cover", "Union", "SteeringFixed", "WoodThing",
                            "Cover2", "RodT", "RodD2", "BarT2", "RodD1", "hexadecagon4", "WheelStuff"), (item) -> getBicycleItemColor(item, 2),
                    List.of("Cap1", "Cap2"), (item) -> getBicycleItemColor(item, 3)
            )
    );

    public static final RegistrySupplier<DataComponentType<Boolean>> HAS_DISPLAY = BikesArePain.DATA_COMPONENTS.register(
            ResourceLocation.fromNamespaceAndPath(BikesArePain.MOD_ID, "has_display"),
            () -> DataComponentType.<Boolean>builder().persistent(Codec.BOOL).build()
    );
    public static final RegistrySupplier<DataComponentType<Boolean>> SADDLED = BikesArePain.DATA_COMPONENTS.register(
            ResourceLocation.fromNamespaceAndPath(BikesArePain.MOD_ID, "saddled"),
            () -> DataComponentType.<Boolean>builder().persistent(Codec.BOOL).build()
    );
    public static final RegistrySupplier<DataComponentType<Boolean>> HEALTH_AFFECTS_SPEED = BikesArePain.DATA_COMPONENTS.register(
            ResourceLocation.fromNamespaceAndPath(BikesArePain.MOD_ID, "health_affect_speed"),
            () -> DataComponentType.<Boolean>builder().persistent(Codec.BOOL).build()
    );
    public static final RegistrySupplier<DataComponentType<Boolean>> SAVE_TIME = BikesArePain.DATA_COMPONENTS.register(
            ResourceLocation.fromNamespaceAndPath(BikesArePain.MOD_ID, "save_time"),
            () -> DataComponentType.<Boolean>builder().persistent(Codec.BOOL).build()
    );
    public static final RegistrySupplier<DataComponentType<Boolean>> SAVE_DISTANCE = BikesArePain.DATA_COMPONENTS.register(
            ResourceLocation.fromNamespaceAndPath(BikesArePain.MOD_ID, "save_distance"),
            () -> DataComponentType.<Boolean>builder().persistent(Codec.BOOL).build()
    );
    public static final RegistrySupplier<DataComponentType<Float>> DISTANCE_MOVED = BikesArePain.DATA_COMPONENTS.register(
            ResourceLocation.fromNamespaceAndPath(BikesArePain.MOD_ID, "distance_moved"),
            () -> DataComponentType.<Float>builder().persistent(Codec.FLOAT).build()
    );
    public static final RegistrySupplier<DataComponentType<Integer>> TICKS_MOVED = BikesArePain.DATA_COMPONENTS.register(
            ResourceLocation.fromNamespaceAndPath(BikesArePain.MOD_ID, "ticks_ridden"),
            () -> DataComponentType.<Integer>builder().persistent(Codec.INT).build()
    );
    public static final RegistrySupplier<DataComponentType<Boolean>> HAS_BALLOON = BikesArePain.DATA_COMPONENTS.register(
            ResourceLocation.fromNamespaceAndPath(BikesArePain.MOD_ID, "has_balloon"),
            () -> DataComponentType.<Boolean>builder().persistent(Codec.BOOL).build()
    );

    public static final RegistrySupplier<Item> BICYCLE_ITEM = BikesArePain.ITEMS.register("bicycle", () ->
            new BicycleItem(EntityManager.BICYCLE.get(),
                    ResourceLocation.fromNamespaceAndPath(BikesArePain.MOD_ID, "bicycle"),
                    bonesToColorBicycleItem,
                    List.of(),
                    new BikeItem.Properties()
                            .stacksTo(1)
                            .rarity(Rarity.UNCOMMON)
                            .durability(100)
                            .arch$tab(ItemManager.BIKES_MOD_TAB)
                            .component(SADDLED.get(), false)
                            .component(SAVE_TIME.get(), true)
                            .component(SAVE_DISTANCE.get(), true)
                            .component(DISTANCE_MOVED.get(), 0.0F)
                            .component(TICKS_MOVED.get(), 0)
                            .component(HEALTH_AFFECTS_SPEED.get(), true)
                            .component(HAS_BALLOON.get(), false)
                            .component(HAS_DISPLAY.get(), false)
                            // Where [front_wheel, rear_wheel, frame, gearbox]
                            .component(BICYCLE_COLORS.get(), bicycleColors)
            )
    );

    public static final RegistrySupplier<Item> NUT_ITEM = BikesArePain.ITEMS.register("nut", () ->
            new BaseItem(
                    ResourceLocation.fromNamespaceAndPath(BikesArePain.MOD_ID, "nut"),
                    Map.of(),
                    List.of(),
                    new Item.Properties()
                    .stacksTo(64)
                    .rarity(Rarity.COMMON)
                    .arch$tab(ItemManager.BIKES_MOD_TAB)
            )
    );

    public static final RegistrySupplier<Item> WRENCH_ITEM = BikesArePain.ITEMS.register("wrench", () ->
            new BaseItem(
                    ResourceLocation.fromNamespaceAndPath(BikesArePain.MOD_ID, "wrench"),
                    Map.of(),
                    List.of(),
                    new Item.Properties()
                    .stacksTo(1)
                    .rarity(Rarity.UNCOMMON)
                    .arch$tab(ItemManager.BIKES_MOD_TAB)
                    .durability(100)
            )
    );

    public static final RegistrySupplier<Item> PEDOMETER_ITEM = BikesArePain.ITEMS.register("pedometer", () ->
            new BaseItem(
                    ResourceLocation.fromNamespaceAndPath(BikesArePain.MOD_ID, "pedometer"),
                    Map.of(),
                    List.of(),
                    new Item.Properties()
                    .stacksTo(1)
                    .rarity(Rarity.UNCOMMON)
                    .arch$tab(ItemManager.BIKES_MOD_TAB)
            )
    );

    public static final RegistrySupplier<Item> FRAME_ITEM = BikesArePain.ITEMS.register("bicycle_frame", () ->
            new BaseItem(
                    ResourceLocation.fromNamespaceAndPath(BikesArePain.MOD_ID, "bicycle_frame"),
                    Map.of("*", (item) -> getBicyclePartColor(item, 3)),
                    List.of(),
                    new Item.Properties()
                            .stacksTo(1)
                            .rarity(Rarity.UNCOMMON)
                            .arch$tab(ItemManager.BIKES_MOD_TAB)
                            .component(COLOR.get(), Color.HSBtoARGB(34,33,17))
            )
    );

    public static final RegistrySupplier<Item> GEARBOX_ITEM = BikesArePain.ITEMS.register("bicycle_gearbox", () ->
            new BaseItem(
                    ResourceLocation.fromNamespaceAndPath(BikesArePain.MOD_ID, "bicycle_gearbox"),
                    Map.of("Cap1", (item) -> getBicyclePartColor(item, 2),
                            "Cap2", (item) -> getBicyclePartColor(item, 2)
                    ),
                    List.of(),
                    new Item.Properties()
                            .stacksTo(1)
                            .rarity(Rarity.UNCOMMON)
                            .arch$tab(ItemManager.BIKES_MOD_TAB)
                            .component(COLOR.get(), Color.HSBtoARGB(34,33,17))
            )
    );

    public static final RegistrySupplier<Item> HANDLEBAR_ITEM = BikesArePain.ITEMS.register("bicycle_handlebar", () ->
            new BaseItem(
                    ResourceLocation.fromNamespaceAndPath(BikesArePain.MOD_ID, "bicycle_handlebar"),
                    Map.of(),
                    List.of(),
                    new Item.Properties()
                            .stacksTo(1)
                            .rarity(Rarity.UNCOMMON)
                            .arch$tab(ItemManager.BIKES_MOD_TAB)
            )
    );

    public static final RegistrySupplier<Item> WHEEL_ITEM = BikesArePain.ITEMS.register("bicycle_wheel", () ->
            new BaseItem(
                    ResourceLocation.fromNamespaceAndPath(BikesArePain.MOD_ID, "bicycle_wheel"),
                    Map.of("hexadecagon", (item) -> getBicyclePartColor(item, 0),
                            "*", (item) -> bicycleColors.get(1)),
                    List.of(),
                    new Item.Properties()
                            .stacksTo(2)
                            .rarity(Rarity.UNCOMMON)
                            .arch$tab(ItemManager.BIKES_MOD_TAB)
                            .component(COLOR.get(), bicycleColors.getFirst())
            )
    );


    public static void init() {
        BikesArePain.TABS.register();
        BikesArePain.DATA_COMPONENTS.register();
        BikesArePain.ITEMS.register();
    }

}
