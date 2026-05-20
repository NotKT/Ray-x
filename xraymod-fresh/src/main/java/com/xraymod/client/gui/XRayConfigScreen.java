package com.xraymod.client.gui;

import com.xraymod.client.XRayState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.EntryListWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

public class XRayConfigScreen extends Screen {

    private final Screen parent;
    private TextFieldWidget addField;
    private BlockList blockList;

    public XRayConfigScreen(Screen parent) {
        super(Text.literal("XRay Whitelist Config"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        blockList = new BlockList(client, width, height - 80, 40, 20);
        addDrawableChild(blockList);

        addField = new TextFieldWidget(
            textRenderer, width / 2 - 150, 8, 240, 18,
            Text.literal("Block ID"));
        addField.setPlaceholder(Text.literal("minecraft:diamond_ore"));
        addDrawableChild(addField);
        setInitialFocus(addField);

        addDrawableChild(ButtonWidget.builder(Text.literal("Add"), btn -> {
            String id = addField.getText().trim().toLowerCase();
            if (!id.isEmpty()) {
                XRayState.config.addBlock(id);
                addField.setText("");
                blockList.refresh();
            }
        }).dimensions(width / 2 + 95, 7, 55, 20).build());

        addDrawableChild(ButtonWidget.builder(Text.literal("Reset Defaults"), btn -> {
            XRayState.config.resetToDefaults();
            blockList.refresh();
        }).dimensions(width / 2 - 150, height - 28, 120, 20).build());

        addDrawableChild(ButtonWidget.builder(Text.literal("Done"), btn -> {
            assert client != null;
            client.setScreen(parent);
        }).dimensions(width / 2 + 35, height - 28, 60, 20).build());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(textRenderer,
            this.title, width / 2, height - 52, 0xFFFFFF);
    }

    @Override
    public boolean shouldPause() { return false; }

    // Inner list widget
    class BlockList extends EntryListWidget<BlockList.BlockEntry> {

        public BlockList(MinecraftClient client, int width, int height, int top, int itemHeight) {
            super(client, width, height, top, itemHeight);
            refresh();
        }

        public void refresh() {
            clearEntries();
            List<String> blocks = new ArrayList<>(XRayState.config.getVisibleBlocks());
            blocks.sort(String::compareTo);
            for (String block : blocks) {
                addEntry(new BlockEntry(block));
            }
        }

        @Override
        public int getRowWidth() { return width - 60; }

        @Override
        protected int getScrollbarX() { return width - 10; }

        class BlockEntry extends EntryListWidget.Entry<BlockEntry> {
            private final String blockId;
            private final ButtonWidget removeBtn;

            BlockEntry(String blockId) {
                this.blockId = blockId;
                this.removeBtn = ButtonWidget.builder(Text.literal("X"), btn -> {
                    XRayState.config.removeBlock(blockId);
                    refresh();
                }).dimensions(0, 0, 20, 18).build();
            }

            @Override
            public void render(DrawContext context, int index, int y, int x,
                               int entryWidth, int entryHeight, int mouseX, int mouseY,
                               boolean hovered, float tickDelta) {
                context.drawTextWithShadow(textRenderer,
                    Text.literal(blockId), x + 4, y + 4, 0xFFFFFF);
                removeBtn.setX(x + entryWidth - 22);
                removeBtn.setY(y + 1);
                removeBtn.render(context, mouseX, mouseY, tickDelta);
            }

            @Override
            public boolean mouseClicked(double mouseX, double mouseY, int button) {
                return removeBtn.mouseClicked(mouseX, mouseY, button);
            }

            @Override
            public Text getNarration() {
                return Text.literal(blockId);
            }
        }
    }
}
