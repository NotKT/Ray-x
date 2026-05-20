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
    private TextFieldWidget addField;
    private int scrollOffset = 0;
    private static final int ROW_HEIGHT = 22;
    private static final int LIST_TOP = 50;
    private static final int LIST_BOTTOM_MARGIN = 50;

    public XRayConfigScreen(Screen parent) {
        super(Text.literal("XRay Whitelist Config"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        addField = new TextFieldWidget(
            textRenderer, width / 2 - 150, 10, 240, 18,
            Text.literal("Block ID"));
        addField.setPlaceholder(Text.literal("minecraft:diamond_ore"));
        addDrawableChild(addField);
        setInitialFocus(addField);

        addDrawableChild(ButtonWidget.builder(Text.literal("Add"), btn -> {
            String id = addField.getText().trim().toLowerCase();
            if (!id.isEmpty()) {
                XRayState.config.addBlock(id);
                addField.setText("");
            }
        }).dimensions(width / 2 + 95, 9, 55, 20).build());

        addDrawableChild(ButtonWidget.builder(Text.literal("Reset Defaults"), btn -> {
            XRayState.config.resetToDefaults();
            scrollOffset = 0;
        }).dimensions(width / 2 - 60, height - 30, 120, 20).build());

        addDrawableChild(ButtonWidget.builder(Text.literal("Done"), btn -> {
            assert client != null;
            client.setScreen(parent);
        }).dimensions(width / 2 + 65, height - 30, 60, 20).build());
    }

    private List<String> getSortedBlocks() {
        List<String> blocks = new ArrayList<>(XRayState.config.getVisibleBlocks());
        blocks.sort(String::compareTo);
        return blocks;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        context.fill(0, 0, width, height, 0xCC000000);
        context.fill(0, LIST_TOP - 5, width, LIST_TOP - 4, 0x88FFFFFF);
        context.fill(0, height - LIST_BOTTOM_MARGIN + 5, width, height - LIST_BOTTOM_MARGIN + 6, 0x88FFFFFF);

        List<String> blocks = getSortedBlocks();
        int listHeight = height - LIST_TOP - LIST_BOTTOM_MARGIN;
        int visibleRows = listHeight / ROW_HEIGHT;
        int maxScroll = Math.max(0, blocks.size() - visibleRows);
        scrollOffset = Math.min(scrollOffset, maxScroll);

        for (int i = 0; i < visibleRows && (i + scrollOffset) < blocks.size(); i++) {
            String blockId = blocks.get(i + scrollOffset);
            int y = LIST_TOP + i * ROW_HEIGHT;

            if (i % 2 == 0) {
                context.fill(0, y, width, y + ROW_HEIGHT, 0x22FFFFFF);
            }

            context.fill(5, y + 3, 15, y + 13, 0xFFFFAA00);
            context.drawText(textRenderer, blockId, 18, y + 6, 0xFFFFFF00, true);

            context.fill(width - 35, y + 2, width - 5, y + ROW_HEIGHT - 2, 0xFF992222);
            context.drawText(textRenderer, "X", width - 24, y + 6, 0xFFFFFFFF, true);
        }

        super.render(context, mouseX, mouseY, delta);
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        List<String> blocks = getSortedBlocks();
        int listHeight = height - LIST_TOP - LIST_BOTTOM_MARGIN;
        int visibleRows = listHeight / ROW_HEIGHT;

        for (int i = 0; i < visibleRows && (i + scrollOffset) < blocks.size(); i++) {
            int y = LIST_TOP + i * ROW_HEIGHT;
            if (mouseX >= width - 35 && mouseY >= y && mouseY <= y + ROW_HEIGHT) {
                XRayState.config.removeBlock(blocks.get(i + scrollOffset));
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY,
                                  double horizontalAmount, double verticalAmount) {
        List<String> blocks = getSortedBlocks();
        int listHeight = height - LIST_TOP - LIST_BOTTOM_MARGIN;
        int visibleRows = listHeight / ROW_HEIGHT;
        int maxScroll = Math.max(0, blocks.size() - visibleRows);
        scrollOffset = Math.max(0, Math.min(maxScroll, scrollOffset - (int) verticalAmount));
        return true;
    }

    @Override
    public boolean shouldPause() { return false; }
}
