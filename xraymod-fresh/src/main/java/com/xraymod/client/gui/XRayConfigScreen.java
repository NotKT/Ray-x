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
    private static final int ROW_HEIGHT = 20;
    private static final int LIST_TOP = 80;

    public XRayConfigScreen(Screen parent) {
        super(Text.literal("XRay Whitelist Config"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        addField = new TextFieldWidget(
            textRenderer, width / 2 - 150, LIST_TOP - 30, 240, 18,
            Text.literal("Block ID"));
        addField.setPlaceholder(Text.literal("minecraft:diamond_ore"));
        addDrawableChild(addField);
        setInitialFocus(addField);

        addDrawableChild(ButtonWidget.builder(Text.literal("Add"), btn -> {
            String id = addField.getText().trim().toLowerCase();
            if (!id.isEmpty()) { XRayState.config.addBlock(id); addField.setText(""); }
        }).dimensions(width / 2 + 95, LIST_TOP - 31, 55, 20).build());

        addDrawableChild(ButtonWidget.builder(Text.literal("Reset Defaults"),
            btn -> XRayState.config.resetToDefaults()
        ).dimensions(width / 2 - 150, height - 30, 120, 20).build());

        addDrawableChild(ButtonWidget.builder(Text.literal("Done"), btn -> {
            assert client != null;
            client.setScreen(parent);
        }).dimensions(width / 2 + 35, height - 30, 60, 20).build());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        context.drawCenteredTextWithShadow(textRenderer,
            Text.literal("§bXRay §fWhitelist"), width / 2, 10, 0xFFFFFF);
        context.drawCenteredTextWithShadow(textRenderer,
            Text.literal("§7Hold §eX §7to activate"), width / 2, 24, 0xAAAAAA);

        List<String> blocks = new ArrayList<>(XRayState.config.getVisibleBlocks());
        blocks.sort(String::compareTo);
        int visibleRows = (height - LIST_TOP - 50) / ROW_HEIGHT;
        int maxScroll = Math.max(0, blocks.size() - visibleRows);
        scrollOffset = Math.min(scrollOffset, maxScroll);

        for (int i = 0; i < visibleRows && (i + scrollOffset) < blocks.size(); i++) {
            String blockId = blocks.get(i + scrollOffset);
            int y = LIST_TOP + 20 + i * ROW_HEIGHT;
            if (i % 2 == 0)
                context.fill(width / 2 - 152, y - 1, width / 2 + 152, y + ROW_HEIGHT - 2, 0x22FFFFFF);
            context.drawTextWithShadow(textRenderer, Text.literal(blockId), width / 2 - 148, y + 4, 0xFFFFFF);
            context.fill(width / 2 + 110, y, width / 2 + 150, y + ROW_HEIGHT - 2, 0xAACC3333);
            context.drawCenteredTextWithShadow(textRenderer, Text.literal("X"), width / 2 + 130, y + 4, 0xFFFFFF);
        }
        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY,
                                  double horizontalAmount, double verticalAmount) {
        List<String> blocks = new ArrayList<>(XRayState.config.getVisibleBlocks());
        int visibleRows = (height - LIST_TOP - 50) / ROW_HEIGHT;
        int maxScroll = Math.max(0, blocks.size() - visibleRows);
        scrollOffset = Math.max(0, Math.min(maxScroll, scrollOffset - (int) verticalAmount));
        return true;
    }

    @Override
    public boolean shouldPause() { return false; }
}
