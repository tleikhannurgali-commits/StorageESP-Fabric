package com.example.storageesp;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.gizmos.GizmoStyle;
import net.minecraft.gizmos.Gizmos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.entity.BarrelBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.DispenserBlockEntity;
import net.minecraft.world.level.block.entity.EnderChestBlockEntity;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.minecraft.world.level.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.world.level.block.entity.TrappedChestBlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.AABB;
import org.lwjgl.glfw.GLFW;

/** Fabric port of StorageESP. No Forge dependencies. */
public final class StorageEspClient implements ClientModInitializer {
    private static final int RANGE_CHUNKS = 6;
    private static final float LINE_WIDTH = 2.5f;

    private static KeyMapping menu;
    private static KeyMapping storage;
    private static KeyMapping players;
    private static KeyMapping invis;
    private static KeyMapping warden;
    private static KeyMapping mark;

    private static KeyMapping key(String name, int code) {
        return KeyBindingHelper.registerKeyBinding(new KeyMapping(
            "key.storageesp." + name, InputConstants.Type.KEYSYM, code, KeyMapping.Category.MISC));
    }

    @Override
    public void onInitializeClient() {
        Features.load();
        menu = key("menu", GLFW.GLFW_KEY_RIGHT_CONTROL);
        storage = key("storage", GLFW.GLFW_KEY_H);
        players = key("players", GLFW.GLFW_KEY_J);
        invis = key("invis", GLFW.GLFW_KEY_K);
        warden = key("warden", GLFW.GLFW_KEY_O);
        mark = key("mark", GLFW.GLFW_KEY_M);
        ClientTickEvents.END_CLIENT_TICK.register(StorageEspClient::tick);
        HudElementRegistry.attachElementAfter(VanillaHudElements.HOTBAR,
            Identifier.fromNamespaceAndPath("storageesp", "radar"), RadarHud::onRender);
        ItemTooltipCallback.EVENT.register((stack, context, flag, lines) -> PriceTags.onTooltip(stack, lines));
    }

    private static void tick(Minecraft mc) {
        ClientLevel level = mc.level;
        if (level == null || mc.player == null) return;
        while (menu.consumeClick()) {
            if (mc.screen == null) mc.setScreen(new ModScreen());
        }
        while (storage.consumeClick()) toggle(mc, Features.STORAGE);
        while (players.consumeClick()) toggle(mc, Features.PLAYERS);
        while (invis.consumeClick()) toggle(mc, Features.ANTI_INVIS);
        while (warden.consumeClick()) toggle(mc, Features.WARDEN);
        while (mark.consumeClick()) MarksTracker.addMark(mc);

        if (Features.ANTI_INVIS.on()) revealInvisiblePlayers(level);
        if (Features.PLAYERS.on()) drawPlayers(mc, level);
        if (Features.LOOT.on()) LootTracker.tick(mc, level);
        if (Features.STORAGE.on()) drawStorage(mc, level);
        if (Features.WARDEN.on()) WardenTracker.draw(mc, level);
        if (Features.MARKS.on()) MarksTracker.tick(mc, level);
        if (Features.VOID_MAP.on()) VoidMapScanner.tick(mc, level);
        if (Features.TRACES.on()) TracesTracker.draw();
    }

    private static void toggle(Minecraft mc, Features f) {
        f.toggle();
        Features.save();
        mc.player.displayClientMessage(Component.literal(f.label + ": " + (f.on() ? "ON" : "OFF")), true);
    }

    private static void revealInvisiblePlayers(ClientLevel level) {
        for (AbstractClientPlayer p : level.players()) if (p.isInvisible()) p.setInvisible(false);
    }

    private static void drawPlayers(Minecraft mc, ClientLevel level) {
        for (AbstractClientPlayer p : level.players()) {
            if (p == mc.player) continue;
            int color = p.isAlliedTo(mc.player) ? 0xFF55FF55 : 0xFFFF3333;
            Gizmos.cuboid(p.getBoundingBox(), GizmoStyle.stroke(color, LINE_WIDTH)).setAlwaysOnTop();
        }
    }

    private static void drawStorage(Minecraft mc, ClientLevel level) {
        int pcx = mc.player.blockPosition().getX() >> 4;
        int pcz = mc.player.blockPosition().getZ() >> 4;
        boolean lootOn = Features.LOOT.on();
        for (int cx = pcx - RANGE_CHUNKS; cx <= pcx + RANGE_CHUNKS; cx++) {
            for (int cz = pcz - RANGE_CHUNKS; cz <= pcz + RANGE_CHUNKS; cz++) {
                LevelChunk chunk = level.getChunkSource().getChunkNow(cx, cz);
                if (chunk == null) continue;
                for (BlockEntity be : chunk.getBlockEntities().values()) {
                    int color = colorFor(be);
                    if (color == 0) continue;
                    if (lootOn && LootTracker.isLooted(be.getBlockPos())) color = 0xFF777777;
                    Gizmos.cuboid(new AABB(be.getBlockPos()).inflate(0.002),
                        GizmoStyle.stroke(color, LINE_WIDTH)).setAlwaysOnTop();
                }
            }
        }
    }

    private static int colorFor(BlockEntity be) {
        if (be instanceof TrappedChestBlockEntity)    return 0xFFFF2626;
        if (be instanceof ChestBlockEntity)           return 0xFFFFA619;
        if (be instanceof EnderChestBlockEntity)      return 0xFFB333FF;
        if (be instanceof BarrelBlockEntity)          return 0xFF996633;
        if (be instanceof ShulkerBoxBlockEntity shulker) {
            DyeColor dye = shulker.getColor();
            return dye != null ? (0xFF000000 | dye.getTextureDiffuseColor()) : 0xFF9966B3;
        }
        if (be instanceof HopperBlockEntity)          return 0xFF999999;
        if (be instanceof DispenserBlockEntity)       return 0xFF4DCCFF;
        if (be instanceof AbstractFurnaceBlockEntity) return 0xFFE6E64D;
        return 0;
    }
}
