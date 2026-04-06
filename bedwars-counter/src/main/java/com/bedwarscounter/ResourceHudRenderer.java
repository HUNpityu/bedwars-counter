package com.bedwarscounter;

import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;

import java.util.LinkedHashMap;
import java.util.Map;

public class ResourceHudRenderer implements HudRenderCallback {

    // Padding and sizing
    private static final int PADDING = 8;
    private static final int ROW_HEIGHT = 18;
    private static final int ICON_SIZE = 16;
    private static final int BOX_WIDTH = 80;
    private static final int CORNER_RADIUS = 4;

    // Resource definitions: item -> display color
    private static final Map<Item, Integer> RESOURCES = new LinkedHashMap<>();

    static {
        RESOURCES.put(Items.IRON_INGOT,    0xFFCCCCCC); // light grey  – iron
        RESOURCES.put(Items.GOLD_INGOT,    0xFFFFD700); // gold yellow – gold
        RESOURCES.put(Items.DIAMOND,       0xFF00FFFF); // cyan        – diamond
        RESOURCES.put(Items.EMERALD,       0xFF00FF7F); // green       – emerald
    }

    @Override
    public void onHudRender(DrawContext context, RenderTickCounter tickCounter) {
        MinecraftClient client = MinecraftClient.getInstance();

        // Only render when in game with a player
        if (client.player == null || client.world == null) return;
        // Hide when any screen is open (inventory, etc.)
        if (client.currentScreen != null) return;

        // Count each resource in the player's inventory
        Map<Item, Integer> counts = new LinkedHashMap<>();
        for (Item item : RESOURCES.keySet()) {
            counts.put(item, 0);
        }

        for (ItemStack stack : client.player.getInventory().main) {
            if (counts.containsKey(stack.getItem())) {
                counts.merge(stack.getItem(), stack.getCount(), Integer::sum);
            }
        }

        // Filter to only show resources the player actually has
        // (comment out the next 3 lines if you want to always show all 4)
        // counts.entrySet().removeIf(e -> e.getValue() == 0);
        // if (counts.isEmpty()) return;

        int resourceCount = RESOURCES.size();
        int boxHeight = PADDING + resourceCount * ROW_HEIGHT + PADDING / 2;
        int screenWidth = context.getScaledWindowWidth();
        int screenHeight = context.getScaledWindowHeight();

        // Position: bottom-right corner
        int boxX = screenWidth - BOX_WIDTH - PADDING;
        int boxY = screenHeight - boxHeight - PADDING - 39; // 39 = hotbar height offset

        // Draw semi-transparent dark background
        context.fill(
            boxX,
            boxY,
            boxX + BOX_WIDTH,
            boxY + boxHeight,
            0xAA000000
        );

        // Draw a thin border
        drawBorder(context, boxX, boxY, BOX_WIDTH, boxHeight, 0x55FFFFFF);

        // Draw each resource row
        int y = boxY + PADDING;
        for (Map.Entry<Item, Integer> entry : RESOURCES.entrySet()) {
            Item item = entry.getKey();
            int count = counts.getOrDefault(item, 0);
            int color = RESOURCES.get(item);

            // Dim the row if the player has none of this resource
            int textColor = count > 0 ? color : 0xFF666666;

            // Draw the item icon
            context.drawItem(new ItemStack(item), boxX + PADDING / 2, y);

            // Draw the count text, right-aligned inside the box
            String countStr = String.valueOf(count);
            int textWidth = client.textRenderer.getWidth(countStr);
            int textX = boxX + BOX_WIDTH - textWidth - PADDING / 2;
            int textY = y + 4; // vertically center with 16px icon

            context.drawText(
                client.textRenderer,
                countStr,
                textX,
                textY,
                textColor,
                true // shadow
            );

            y += ROW_HEIGHT;
        }
    }

    /** Draws a 1px border rectangle */
    private void drawBorder(DrawContext ctx, int x, int y, int w, int h, int color) {
        ctx.fill(x,         y,         x + w,     y + 1,     color); // top
        ctx.fill(x,         y + h - 1, x + w,     y + h,     color); // bottom
        ctx.fill(x,         y,         x + 1,     y + h,     color); // left
        ctx.fill(x + w - 1, y,         x + w,     y + h,     color); // right
    }
}
