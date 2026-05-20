package com.xraymod.client.gui;

import com.xraymod.client.XRayState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ButtonWidget;package com.xraymod.client.gui;

import com.xraymod.client.XRayState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
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

    @
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
    }

    @Override
    public boolean shouldPause() { return false; }

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

        @Override
        public void appendClickableNarrations(NarrationMessageBuilder builder) {}

        class BlockEntry extends EntryListWidget.Entry<BlockEntry> {

            private final String blockId;

            BlockEntry(String blockId) {
                this.blockId = blockId;
            }

            @Override
            public void render(DrawContext context, int index, boolean hovered, float tickProgress) {
                int x = getRowLeft();
                int y = getRowTop(index);
                context.drawTextWithShadow(textRenderer,
                    Text.literal(blockId), x + 4, y + 4, 0xFFFFFF);

                // Red remove button area
                context.fill(x + getRowWidth() - 22, y,
                    x + getRowWidth(), y + 18, 0xAACC3333);
                context.drawTextWithShadow(textRenderer,
                    Text.literal("X"), x + getRowWidth() - 14, y + 4, 0xFFFFFF);
            }

            @Override
            public boolean mouseClicked(double mouseX, double mouseY, int button) {
                int x = getRowLeft();
                int y = getRowTop(BlockList.this.children().indexOf(this));
                if (mouseX >= x + getRowWidth() - 22 && mouseX <= x + getRowWidth()
                    && mouseY >= y && mouseY <= y + 18) {
                    XRayState.config.removeBlock(blockId);
                    refresh();
                    return true;
                }
                return false;
            }

            @Override
            public Text getNarration() {
                return Text.literal(blockId);
            }
        }
    }
}
