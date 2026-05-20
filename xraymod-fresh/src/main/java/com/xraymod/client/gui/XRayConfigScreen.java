// No @Override — just handle clicks directly without overriding broken parent method
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        List<String> blocks = new ArrayList<>(XRayState.config.getVisibleBlocks());
        blocks.sort(String::compareTo);
        int visibleRows = (height - LIST_TOP - 50) / ROW_HEIGHT;

        for (int i = 0; i < visibleRows && (i + scrollOffset) < blocks.size(); i++) {
            int y = LIST_TOP + 20 + i * ROW_HEIGHT;
            if (mouseX >= width / 2 + 110 && mouseX <= width / 2 + 150
                && mouseY >= y && mouseY <= y + ROW_HEIGHT - 2) {
                XRayState.config.removeBlock(blocks.get(i + scrollOffset));
                return true;
            }
        }
        return false;
    }
