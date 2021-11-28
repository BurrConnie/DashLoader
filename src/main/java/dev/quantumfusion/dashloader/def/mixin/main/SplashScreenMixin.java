package dev.quantumfusion.dashloader.def.mixin.main;

import dev.quantumfusion.dashloader.def.DashLoader;
import dev.quantumfusion.dashloader.def.client.DashCachingScreen;
import dev.quantumfusion.dashloader.def.util.TimeUtil;
import dev.quantumfusion.dashloader.def.util.mixins.MixinThings;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.SplashOverlay;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.management.ManagementFactory;


@Mixin(SplashOverlay.class)
public class SplashScreenMixin {
	@Shadow
	@Final
	private MinecraftClient client;

	@Inject(
			method = "render(Lnet/minecraft/client/util/math/MatrixStack;IIF)V",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Util;getMeasuringTimeMs()J", shift = At.Shift.BEFORE, ordinal = 1),
			cancellable = true
	)
	private void render(MatrixStack matrices, int mouseX, int mouseY, float delta, CallbackInfo ci) {
		DashLoader.LOGGER.info("</> DashLoader Profiled {}", "Times"); // ij labels plz show

		if (DashLoader.INSTANCE.getStatus() == DashLoader.Status.READ) {
			this.client.setOverlay(null);
			if (client.currentScreen != null) {
				if (this.client.currentScreen instanceof TitleScreen) {
					DashLoader.LOGGER.info("</> ==> DashLoader Export time {}", TimeUtil.getTimeString(DashLoader.EXPORT_END - DashLoader.EXPORT_START));
					this.client.currentScreen = new TitleScreen(false);
				}
				this.client.currentScreen.init(this.client, this.client.getWindow().getScaledWidth(), this.client.getWindow().getScaledHeight());
			}
		} else {
			this.client.setOverlay(null);
			final DashCachingScreen currentScreen = new DashCachingScreen(this.client.currentScreen);
			this.client.setScreen(currentScreen);
			currentScreen.start();
		}
		DashLoader.LOGGER.info("</> ==> Minecraft Reload time {}", TimeUtil.getTimeStringFromStart(DashLoader.RELOAD_START));
		DashLoader.LOGGER.info("</> ==> Minecraft Bootstrap time {}", TimeUtil.getTimeString(MixinThings.BOOTSTRAP_END - MixinThings.BOOTSTRAP_START));
		DashLoader.LOGGER.info("</> ==> Total Loading time {}", TimeUtil.getTimeString(ManagementFactory.getRuntimeMXBean().getUptime()));
		ci.cancel();
	}
}