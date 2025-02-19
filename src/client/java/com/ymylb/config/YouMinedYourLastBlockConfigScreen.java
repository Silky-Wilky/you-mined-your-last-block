package com.ymylb.config;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;

import net.minecraft.text.Text;

/**
 * Configuration screen for the "You Mined Your Last Block" mod.
 * Provides a GUI for users to modify mod settings.
 */
@Environment(EnvType.CLIENT)
public class YouMinedYourLastBlockConfigScreen extends Screen {

	private final Screen parent;
	private static YouMinedYourLastBlockConfig INSTANCE = YouMinedYourLastBlockConfig.getInstance();

	// Widgets for configuration
	private ButtonWidget toggleButton;
	private TextFieldWidget integerField;

	/**
	 * Constructs the configuration screen.
	 *
	 * @param parent The parent screen.
	 */
	public YouMinedYourLastBlockConfigScreen(Screen parent) {
		super(Text.of("You Mined Your Last Block Config"));
		this.parent = parent;
	}

	/**
	 * Initializes the configuration screen components.
	 */
	@Override
	protected void init() {
		super.init();

		// Toggle Button for modEnabled
		toggleButton = ButtonWidget.builder(Text.of(INSTANCE.getModEnabled() ? "Mod Enabled: True" : "Mod Enabled: False"),
			(button) -> {
				INSTANCE.setModEnabled(!INSTANCE.getModEnabled());
				button.setMessage(Text.of(INSTANCE.getModEnabled() ? "Mod Enabled: True" : "Mod Enabled: False"));
			})
			.dimensions(this.width / 2 - 100, this.height / 2 - 50, 200, 20)
			.build();
		this.addDrawableChild(toggleButton);

		// Integer Field (TextField) for blockLimit
		integerField = new TextFieldWidget(this.textRenderer,
				this.width / 2 + 20,
				this.height / 2,
				80,
				20,
				Text.of("Integer: " + INSTANCE.getBlockLimit()));
		integerField.setText(String.valueOf(INSTANCE.getBlockLimit()));  // Set current integer value as text
		integerField.setChangedListener(text -> {
			try {
				int num = Integer.parseInt(text);
				if (num > 0) {
					INSTANCE.setBlockLimit(num);
				} else {
					INSTANCE.setBlockLimit(1);
					integerField.setText("1");
				}
			} catch (NumberFormatException e) {
				System.err.println(e.getMessage());
			}
		});
		this.addDrawableChild(integerField);
	}

	/**
	 * Renders the configuration screen.
	 *
	 * @param context The drawing context.
	 * @param mouseX  Mouse X position.
	 * @param mouseY  Mouse Y position.
	 * @param delta   The frame delta time.
	 */
	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {
		super.render(context, mouseX, mouseY, delta);
		context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 20, 16777215);
		context.drawCenteredTextWithShadow(this.textRenderer, Text.of("Random Block Limit:"), this.width / 2 - 52,this.height / 2 + 6, 16777215);
	}

	/**
	 * Closes the configuration screen and returns to the parent screen.
	 */
	@Override
	public void close() {
		// Save the updated config values
		MinecraftClient.getInstance().setScreen(this.parent);
	}
}