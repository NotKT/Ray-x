package com.xraymod.client.gui;

import com.xraymod.client.XRayState;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

public class XRayConfigScreen extends Screen {

    private final Screen parent;
    private TextFieldWidget addBlockField;
    private TextFieldWidget addEntityField;
    private TextFieldWidget rangeField;
    private int blockScrollOffset = 0;
    private int entityScrollOffset = 0;
    private boolean showingEntities = false;
    private static final int ROW_HEIGHT = 22;
    private static final int LIST_TOP = 115;
    private static final int LIST_BOTTOM_MARGIN = 50;

    public XRayConfigScreen(Screen parent) {
        super(Text.literal("KPS+ Config"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        clearChildren();

        if (!showingEntities) {
            initBlocksTab();
        } else {
            initEntitiesTab();
        }

        // Tab switcher buttons
        addDrawableChild(ButtonWidget.builder(
            Text.literal(showingEntities ? "§7Blocks" : "§aBlocks"), btn -> {
                showingEntities = false;
                init();
            }).dimensions(width / 2 - 65, height - 30, 60, 20).build());

        addDrawableChild(ButtonWidget.builder(
            Text.literal(showingEntities ? "§aEntities" : "§7Entities"), btn -> {
                showingEntities = true;
                init();
            }).dimensions(width / 2 - 1, height - 30, 65, 20).build());

        addDrawableChild(ButtonWidget.builder(Text.literal("Done"), btn -> {
            assert client != null;
            client.setScreen(parent);
        }).dimensions(width / 2 + 68, height - 30, 50, 20).build());
    }

    private void initBlocksTab() {
        // Add block field
        addBlockField = new TextFieldWidget(
            textRenderer, width / 2 - 150, 10, 200, 18,
            Text.literal("Block ID"));
        addBlockField.setPlaceholder(Text.literal("minecraft:diamond_ore"));
        addDrawableChild(addBlockField);
        setInitialFocus(addBlockField);

        addDrawableChild(ButtonWidget.builder(Text.literal("Add"), btn -> {
            String id = addBlockField.getText().trim().toLowerCase();
            if (!id.isEmpty()) {
                XRayState.config.addBlock(id);
                addBlockField.setText("");
                init();
            }
        }).dimensions(width / 2 + 55, 9, 55, 20).build());

        // Range preset buttons
        int rangeY = 60;
        int[] presets = {3, 6, 9, 12};
        for (int i = 0; i < presets.length; i++) {
            final int range = presets[i];
            addDrawableChild(ButtonWidget.builder(Text.literal(range + "c"), btn -> {
                XRayState.config.setChunkRange(range);
                init();
            }).dimensions(width / 2 - 90 + i * 45, rangeY, 40, 20).build());
        }

        rangeField = new TextFieldWidget(
            textRenderer, width / 2 + 95, rangeY, 35, 18,
            Text.literal("Range"));
        rangeField.setPlaceholder(Text.literal("6"));
        rangeField.setText(String.valueOf(XRayState.config.getChunkRange()));
        addDrawableChild(rangeField);

        addDrawableChild(ButtonWidget.builder(Text.literal("Set"), btn -> {
            try {
                int val = Integer.parseInt(rangeField.getText().trim());
                XRayState.config.setChunkRange(val);
                init();
            } catch (NumberFormatException ignored) {}
        }).dimensions(width / 2 + 133, rangeY, 30, 20).build());

        // Fullbright toggle
        boolean fb = XRayState.config.isFullbright();
        addDrawableChild(ButtonWidget.builder(
            Text.literal("Fullbright: " + (fb ? "§aON" : "§cOFF")), btn -> {
                XRayState.config.setFullbright(!XRayState.config.isFullbright());
                init();
            }).dimensions(width / 2 - 60, 88, 120, 20).build());

        // Reset defaults
        addDrawableChild(ButtonWidget.builder(Text.literal("Reset Defaults"), btn -> {
            XRayState.config.resetToDefaults();
            blockScrollOffset = 0;
            init();
        }).dimensions(width / 2 - 130, height - 30, 120, 20).build());

        // Block list X buttons
        List<String> blocks = getSortedBlocks();
        int listHeight = height - LIST_TOP - LIST_BOTTOM_MARGIN;
        int visibleRows = listHeight / ROW_HEIGHT;
        int maxScroll = Math.max(0, blocks.size() - visibleRows);
        blockScrollOffset = Math.min(blockScrollOffset, maxScroll);

        for (int i = 0; i < visibleRows && (i + blockScrollOffset) < blocks.size(); i++) {
            final String blockId = blocks.get(i + blockScrollOffset);
            int y = LIST_TOP + i * ROW_HEIGHT;
            addDrawableChild(ButtonWidget.builder(Text.literal("X"), btn -> {
                XRayState.config.removeBlock(blockId);
                init();
            }).dimensions(width - 35, y + 2, 28, 18).build());
        }
    }

    private void initEntitiesTab() {
        // Add entity field
        addEntityField = new TextFieldWidget(
            textRenderer, width / 2 - 150, 10, 200, 18,
            Text.literal("Entity ID"));
        addEntityField.setPlaceholder(Text.literal("minecraft:zombie"));
        addDrawableChild(addEntityField);
        setInitialFocus(addEntityField);

        addDrawableChild(ButtonWidget.builder(Text.literal("Exclude"), btn -> {
            String id = addEntityField.getText().trim().toLowerCase();
            if (!id.isEmpty()) {
                XRayState.config.addExcludedEntity(id);
                addEntityField.setText("");
                init();
            }
        }).dimensions(width / 2 + 55, 9, 65, 20).build());

        // Entity exclusion list X buttons
        List<String> entities = getSortedEntities();
        int listHeight = height - LIST_TOP - LIST_BOTTOM_MARGIN;
        int visibleRows = listHeight / ROW_HEIGHT;
        int maxScroll = Math.max(0, entities.size() - visibleRows);
        entityScrollOffset = Math.min(entityScrollOffset, maxScroll);

        for (int i = 0; i < visibleRows && (i + entityScrollOffset) < entities.size(); i++) {
            final String entityId = entities.get(i + entityScrollOffset);
            int y = LIST_TOP + i * ROW_HEIGHT;
            addDrawableChild(ButtonWidget.builder(Text.literal("X"), btn -> {
                XRayState.config.removeExcludedEntity(entityId);
                init();
            }).dimensions(width - 35, y + 2, 28, 18).build());
        }
    }

    private List<String> getSortedBlocks() {
        List<String> blocks = new ArrayList<>(XRayState.config.getVisibleBlocks());
        blocks.sort(String::compareTo);
        return blocks;
    }

    private List<String> getSortedEntities() {
        List<String> entities = new ArrayList<>(XRayState.config.getExcludedEntities());
        entities.sort(String::compareTo);
        return entities;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        context.fill(0, 0, width, height, 0xCC000000);
        context.fill(0, LIST_TOP - 5, width, LIST_TOP - 4, 0x88FFFFFF);
        context.fill(0, height - LIST_BOTTOM_MARGIN + 5, width,
            height - LIST_BOTTOM_MARGIN + 6, 0x88FFFFFF);

        if (!showingEntities) {
            context.drawText(textRenderer, "Add Block:", 10, 14, 0xFF00BFFF, true);
            context.drawText(textRenderer,
                "XRay Range: (current: " + XRayState.config.getChunkRange() + " chunks)",
                10, 46, 0xFFFFAA00, true);
            context.drawText(textRenderer,
                "Block Whitelist (stay visible during XRay):",
                10, LIST_TOP - 18, 0xFF00BFFF, true);

            List<String> blocks = getSortedBlocks();
            int listHeight = height - LIST_TOP - LIST_BOTTOM_MARGIN;
            int visibleRows = listHeight / ROW_HEIGHT;
            for (int i = 0; i < visibleRows && (i + blockScrollOffset) < blocks.size(); i++) {
                String blockId = blocks.get(i + blockScrollOffset);
                int y = LIST_TOP + i * ROW_HEIGHT;
                if (i % 2 == 0) context.fill(0, y, width, y + ROW_HEIGHT, 0x22FFFFFF);
                context.fill(5, y + 3, 15, y + 13, 0xFFFFAA00);
                context.drawText(textRenderer, blockId, 18, y + 6, 0xFFFFFF00, true);
            }
        } else {
            context.drawText(textRenderer, "Add Entity to exclude from glow:",
                10, 14, 0xFF00BFFF, true);
            context.drawText(textRenderer,
                "Excluded Entities (won't glow):",
                10, LIST_TOP - 18, 0xFF00BFFF, true);
            context.drawText(textRenderer,
                "Hold §eC §fto activate entity glow",
                10, 36, 0xFFAAAAAA, true);

            List<String> entities = getSortedEntities();
            int listHeight = height - LIST_TOP - LIST_BOTTOM_MARGIN;
            int visibleRows = listHeight / ROW_HEIGHT;
            for (int i = 0; i < visibleRows && (i + entityScrollOffset) < entities.size(); i++) {
                String entityId = entities.get(i + entityScrollOffset);
                int y = LIST_TOP + i * ROW_HEIGHT;
                if (i % 2 == 0) context.fill(0, y, width, y + ROW_HEIGHT, 0x22FFFFFF);
                context.fill(5, y + 3, 15, y + 13, 0xFF00FF00);
                context.drawText(textRenderer, entityId, 18, y + 6, 0xFF00FF00, true);
            }

            if (entities.isEmpty()) {
                context.drawText(textRenderer,
                    "No exclusions — all entities will glow",
                    10, LIST_TOP + 10, 0xFF888888, true);
            }
        }

        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY,
                                  double horizontalAmount, double verticalAmount) {
        if (!showingEntities) {
            List<String> blocks = getSortedBlocks();
            int listHeight = height - LIST_TOP - LIST_BOTTOM_MARGIN;
            int visibleRows = listHeight / ROW_HEIGHT;
            int maxScroll = Math.max(0, blocks.size() - visibleRows);
            blockScrollOffset = Math.max(0, Math.min(maxScroll,
                blockScrollOffset - (int) verticalAmount));
        } else {
            List<String> entities = getSortedEntities();
            int listHeight = height - LIST_TOP - LIST_BOTTOM_MARGIN;
            int visibleRows = listHeight / ROW_HEIGHT;
            int maxScroll = Math.max(0, entities.size() - visibleRows);
            entityScrollOffset = Math.max(0, Math.min(maxScroll,
                entityScrollOffset - (int) verticalAmount));
        }
        init();
        return true;
    }

    @Override
    public boolean shouldPause() { return false; }
}
