package com.ymylb.mixin.client;

import com.ymylb.YouMinedYourLastBlockClient;
import com.ymylb.config.YouMinedYourLastBlockConfig;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.*;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.screen.ScreenTexts;

import net.minecraft.text.Style;
import net.minecraft.text.Text;

import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

/**
 * A Mixin for modifying the Death Screen behavior in Minecraft.
 * This mixin customizes the Death Screen based on the mod's settings.
 */
@Environment(EnvType.CLIENT)
@Mixin(DeathScreen.class)
public class DeathScreenMixin extends Screen {

	@Shadow @Final private static Identifier DRAFT_REPORT_ICON_TEXTURE;

	private Text deathReasonMessage = Text.of("You've mined your last block");
	private Text scoreText;

	private ButtonWidget titleScreenButton;

	private boolean hardcore;
	private boolean deathByMining;
	private boolean modEnabled = YouMinedYourLastBlockConfig.INSTANCE.getModEnabled();

	private final List<ButtonWidget> buttons = new ArrayList<>();

	/**
	 * Default constructor for the DeathScreenMixin.
	 */
	public DeathScreenMixin() {
		super(Text.of(""));
	}

	/**
	 * Modifies the initialization process of the Death Screen.
	 *
	 * @param message    The death message.
	 * @param isHardcore Whether the death screen should be hardcore version
	 * @param ci         Callback information.
	 */
	@Inject(method = "<init>", at = @At("RETURN"))
	private void onInit(Text message, boolean isHardcore, CallbackInfo ci) {
		this.hardcore = checkDifficulty();
		this.deathByMining = checkTooManyBlocks();

		if (!this.deathByMining) {
			this.deathReasonMessage = message;
		}
	}

	/**
	 * Customizes the initialization process of the screen UI.
	 *
	 * @param ci Callback information.
	 */
	@Inject(method = "init", at = @At("HEAD"), cancellable = true)
	private void init(CallbackInfo ci) {
		if(modEnabled) {
			ci.cancel();

			// clear buttons on screen
			this.buttons.clear();
			this.buttons.removeAll(buttons);

			// first button (spectate or respawn)
			this.buttons.add(this.addDrawableChild(ButtonWidget.builder(this.hardcore ? Text.translatable("deathScreen.spectate") : Text.translatable("deathScreen.respawn"), (button) -> {
				this.client.player.requestRespawn();
				button.active = false;
			}).dimensions(this.width / 2 - 100, this.height / 4 + 72, 200, 20).build()));

			// title screen button
			this.titleScreenButton = this.addDrawableChild(ButtonWidget.builder(Text.translatable("deathScreen.titleScreen"), (button) -> this.client.getAbuseReportContext().tryShowDraftScreen(this.client, this, this::titleScreenWasClicked, true)).dimensions(this.width / 2 - 100, this.height / 4 + 96, 200, 20).build());
			this.buttons.add(this.titleScreenButton);

			this.setButtonsActive(false);

			this.scoreText = Text.translatable("deathScreen.score.value", Text.literal(Integer.toString(this.client.player.getScore())).formatted(Formatting.YELLOW));

		} else {
			return;
		}
	}

	/**
	 * Customizes the rendering of the Death Screen UI.
	 *
	 * @param context Render context.
	 * @param mouseX  Mouse X position.
	 * @param mouseY  Mouse Y position.
	 * @param delta   Render delta time.
	 * @param ci      Callback information.
	 */
	@Inject(method = "render", at = @At("HEAD"), cancellable = true)
	private void render(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
		if(modEnabled) {
			ci.cancel();

			// super logic
			super.render(context, mouseX, mouseY, delta);
			context.getMatrices().push();
			context.getMatrices().scale(2.0F, 2.0F, 2.0F);
			context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2 / 2, 30, 16777215);
			context.getMatrices().pop();

			// render death message
			if (this.deathReasonMessage != null) {
				context.drawCenteredTextWithShadow(this.textRenderer, this.deathReasonMessage, this.width / 2, 85, 16777215);
			}

			context.drawCenteredTextWithShadow(this.textRenderer, this.scoreText, this.width / 2, 100, 16777215);
			if (this.deathReasonMessage != null && mouseY > 85) {
				Objects.requireNonNull(this.textRenderer);
				if (mouseY < 85 + 9) {
					Style style = this.getTextComponentUnderMouse(mouseX);
					context.drawHoverEvent(this.textRenderer, style, mouseX, mouseY);
				}
			}

			// render title screen button
			if (this.titleScreenButton != null && this.client.getAbuseReportContext().hasDraft()) {
				context.drawGuiTexture(RenderLayer::getGuiTextured, this.DRAFT_REPORT_ICON_TEXTURE, this.titleScreenButton.getX() + this.titleScreenButton.getWidth() - 17, this.titleScreenButton.getY() + 3, 15, 15);
			}

		} else {
			return;
		}
	}

	/**
	 * Sets the active state for all buttons in the death screen.
	 *
	 * @param active {@code true} to enable buttons, {@code false} to disable them.
	 */
	private void setButtonsActive(boolean active) {
		ButtonWidget buttonWidget;
		for(Iterator<ButtonWidget> buttonItr = this.buttons.iterator(); buttonItr.hasNext(); buttonWidget.active = active) {
			buttonWidget = buttonItr.next();
		}
	}

	/**
	 * Handles the event when the title screen button is clicked.
	 * If the player is in hardcore mode, they will be sent to the title screen.
	 * Otherwise, a confirmation screen will be shown, allowing the player to
	 * choose between quitting or respawning.
	 */
	private void titleScreenWasClicked() {
		if (this.hardcore) {
			this.quitLevel();
		} else {
			ConfirmScreen confirmScreen = new DeathScreen.TitleScreenConfirmScreen((confirmed) -> {
				if (confirmed) {
					this.quitLevel();
				} else {
					this.client.player.requestRespawn();
					this.client.setScreen((Screen)null);
				}

			}, Text.translatable("deathScreen.quit.confirm"), ScreenTexts.EMPTY, Text.translatable("deathScreen.titleScreen"), Text.translatable("deathScreen.respawn"));
			this.client.setScreen(confirmScreen);
			confirmScreen.disableButtons(20);
		}
	}

	/**
	 * Disconnects the player from the current world and returns them to the title screen.
	 * If the world is loaded, it first disconnects from the server and then
	 * saves progress before switching to the main menu.
	 */
	private void quitLevel() {
		if (this.client.world != null) {
			this.client.world.disconnect();
		}
		this.client.disconnect(new MessageScreen(Text.translatable("menu.savingLevel")));
		this.client.setScreen(new TitleScreen());
	}

	/**
	 * Retrieves the style of the text component under the mouse cursor.
	 *
	 * @param mouseX The x-coordinate of the mouse.
	 * @return The {@link Style} of the text component under the cursor, or {@code null} if none is found.
	 */
	@Nullable
	private Style getTextComponentUnderMouse(int mouseX) {
		if (this.deathReasonMessage == null) {
			return null;
		}
		int i = this.client.textRenderer.getWidth(this.deathReasonMessage);
		int j = this.width / 2 - i / 2;
		int k = this.width / 2 + i / 2;
		if (mouseX < j || mouseX > k) {
			return null;
		}
		return this.client.textRenderer.getTextHandler().getStyleAt(this.deathReasonMessage, mouseX - j);
	}

	/**
	 * Checks if the game is in hardcore mode (true or false).
	 *
	 * @return True if hardcore, false otherwise.
	 */
	private boolean checkDifficulty() {
		return YouMinedYourLastBlockClient.playerData.getIsHardcoreDeath();
	}

	/**
	 * Checks if the player died due to mining too many blocks.
	 *
	 * @return True if death was caused by excessive mining, false otherwise.
	 */
	private boolean checkTooManyBlocks() {
		return YouMinedYourLastBlockClient.playerData.getTooManyBlocks();
	}
}