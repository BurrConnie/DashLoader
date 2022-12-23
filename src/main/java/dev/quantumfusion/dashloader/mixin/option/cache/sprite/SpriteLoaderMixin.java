package dev.quantumfusion.dashloader.mixin.option.cache.sprite;

import dev.quantumfusion.dashloader.DashDataManager;
import net.minecraft.client.texture.SpriteLoader;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import static dev.quantumfusion.dashloader.DashLoader.DL;

@Mixin(SpriteLoader.class)
public final class SpriteLoaderMixin {

	@Inject(
			method = "method_47661",
			at = @At(value = "RETURN"),
			cancellable = true
	)
	private void dashloaderWrite(ResourceManager resourceManager, Identifier identifier, int i, Executor executor, CallbackInfoReturnable<CompletableFuture<SpriteLoader.StitchResult>> cir) {
		if (DL.isWrite()) {
			DashDataManager.DashWriteContextData data = DL.getData().getWriteContextData();
			CompletableFuture<SpriteLoader.StitchResult> ret = cir.getReturnValue();
			cir.setReturnValue(ret.thenApply(stitchResult -> {
				data.stitchResults.put(identifier, stitchResult);
				return stitchResult;
			}));
		}
	}

	@Inject(
			method = "method_47661",
			at = @At(value = "HEAD"),
			cancellable = true
	)
	private void dashloaderRead(ResourceManager resourceManager, Identifier identifier, int m, Executor executor, CallbackInfoReturnable<CompletableFuture<SpriteLoader.StitchResult>> cir) {
		if (DL.isRead()) {
			DashDataManager.DashReadContextData data = DL.getData().getReadContextData();
			SpriteLoader.StitchResult cached = data.stitchResults.get(identifier);
			if (cached != null) {
				// Correct the executor
				CompletableFuture<Void> completableFuture = m > 0 ? CompletableFuture.runAsync(() -> cached.regions().values().forEach(sprite -> sprite.getContents().generateMipmaps(m)), executor) : CompletableFuture.completedFuture(null);
				cir.setReturnValue(CompletableFuture.completedFuture(new SpriteLoader.StitchResult(
						cached.width(),
						cached.height(),
						cached.mipLevel(),
						cached.missing(),
						cached.regions(),
						completableFuture
				)));
				cir.cancel();
			}
		}
	}
}