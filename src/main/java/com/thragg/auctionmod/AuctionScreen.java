package com.thragg.auctionmod;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

public class AuctionScreen extends Screen {
    private TextFieldWidget searchField;
    private TextFieldWidget priceField;
    private TextFieldWidget durationField;
    private String selectedItem = "";
    private final List<String> filteredItems = new ArrayList<>();
    private final List<String> allItems = new ArrayList<>();

    private static final String[] HUGO_SMP_CUSTOM_ITEMS = {
        "Hugo SMP Stern",
        "Casino Chip (Gold)",
        "Casino Chip (Diamant)",
        "Hugo Axt (Legendary)",
        "Hugo Schwert",
        "Server Event Token",
        "Opfer-Herz",
        "Speed Boots Deluxe"
    };

    public AuctionScreen() {
        super(Text.literal("Hugo SMP Auktionshaus"));
    }

    @Override
    protected void init() {
        super.init();

        allItems.clear();
        for (Item item : Registries.ITEM) {
            allItems.add(item.getName().getString());
        }

        for (String hugoItem : HUGO_SMP_CUSTOM_ITEMS) {
            allItems.add("[Hugo] " + hugoItem);
        }

        if (client != null && client.player != null) {
            ItemStack held = client.player.getMainHandStack();
            if (!held.isEmpty()) {
                String heldName = held.getName().getString();
                if (!allItems.contains(heldName)) {
                    allItems.add(0, heldName);
                }
                selectedItem = heldName;
            }
        }

        int centerX = width / 2;
        int centerY = height / 2;

        searchField = new TextFieldWidget(textRenderer, centerX - 120, centerY - 65, 240, 20, Text.literal("Item Suchen..."));
        searchField.setChangedListener(this::updateSearch);
        if (!selectedItem.isEmpty()) {
            searchField.setText(selectedItem);
        }
        addSelectableChild(searchField);

        priceField = new TextFieldWidget(textRenderer, centerX - 120, centerY + 25, 115, 20, Text.literal("Mindestpreis"));
        priceField.setText("500");
        addSelectableChild(priceField);

        durationField = new TextFieldWidget(textRenderer, centerX + 5, centerY + 25, 115, 20, Text.literal("Zeit (Sekunden)"));
        durationField.setText("60");
        addSelectableChild(durationField);

        addDrawableChild(ButtonWidget.builder(Text.literal("Auktion Starten"), b -> {
            String itemToSell = searchField.getText().trim();
            if (itemToSell.isEmpty()) return;

            int price = 500;
            try {
                price = Integer.parseInt(priceField.getText().trim());
            } catch (Exception ignored) {}

            int duration = 60;
            try {
                duration = Integer.parseInt(durationField.getText().trim());
                if (duration < 5) duration = 5;
            } catch (Exception ignored) {}

            AuctionManager.startAuction(itemToSell, price, duration);
            close();
        }).dimensions(centerX - 120, centerY + 55, 240, 20).build());

        if (AuctionManager.isActive()) {
            addDrawableChild(ButtonWidget.builder(Text.literal("Auktion Abbrechen"), b -> {
                AuctionManager.cancelAuction();
                close();
            }).dimensions(centerX - 120, centerY + 80, 240, 20).build());
        }

        updateSearch(searchField.getText());
    }

    private void updateSearch(String query) {
        filteredItems.clear();
        String lower = query.toLowerCase().trim();

        if (!lower.isEmpty()) {
            filteredItems.add(query);
        }

        for (String item : allItems) {
            if (item.toLowerCase().contains(lower) && !item.equalsIgnoreCase(query)) {
                filteredItems.add(item);
                if (filteredItems.size() >= 5) break;
            }
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackground(context, mouseX, mouseY, delta);

        int centerX = width / 2;
        int centerY = height / 2;

        context.fill(centerX - 135, centerY - 95, centerX + 135, centerY + 105, 0xD0101010);
        context.drawBorder(centerX - 135, centerY - 95, 270, 200, 0xFFFFAA00);

        context.drawCenteredTextWithShadow(textRenderer, "§6§lHUGO SMP AUKTIONSHAUS", centerX, centerY - 85, 0xFFFFFF);

        context.drawTextWithShadow(textRenderer, "Item (Suche oder Hand-Item):", centerX - 120, centerY - 77, 0xAAAAAA);
        context.drawTextWithShadow(textRenderer, "Mindestpreis ($):", centerX - 120, centerY + 13, 0xAAAAAA);
        context.drawTextWithShadow(textRenderer, "Zeit (Sekunden):", centerX + 5, centerY + 13, 0xAAAAAA);

        searchField.render(context, mouseX, mouseY, delta);
        priceField.render(context, mouseX, mouseY, delta);
        durationField.render(context, mouseX, mouseY, delta);

        int listY = centerY - 40;
        for (int i = 0; i < Math.min(3, filteredItems.size()); i++) {
            String item = filteredItems.get(i);
            boolean hover = mouseX >= centerX - 120 && mouseX <= centerX + 120 && mouseY >= listY && mouseY <= listY + 12;
            int color = hover ? 0xFFFF55 : 0xDDDDDD;
            context.drawTextWithShadow(textRenderer, "• " + item, centerX - 115, listY + 2, color);
            listY += 13;
        }

        if (AuctionManager.isActive()) {
            context.drawCenteredTextWithShadow(textRenderer, "§aAuktion laeuft: §f" + AuctionManager.getSecondsLeft() + "s uebrig", centerX, centerY - 15, 0x55FF55);
            context.drawCenteredTextWithShadow(textRenderer, "§7Gebot: §e" + AuctionManager.getHighestBid() + "$ §7von §b" + AuctionManager.getHighestBidder(), centerX, centerY - 3, 0xFFFFFF);
        }

        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int centerX = width / 2;
        int centerY = height / 2;
        int listY = centerY - 40;

        for (int i = 0; i < Math.min(3, filteredItems.size()); i++) {
            if (mouseX >= centerX - 120 && mouseX <= centerX + 120 && mouseY >= listY && mouseY <= listY + 12) {
                searchField.setText(filteredItems.get(i));
                return true;
            }
            listY += 13;
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }
}
