package dev.notalpha.dashloader.mixin.option.cache.model;

import dev.notalpha.dashloader.Cache;
import dev.notalpha.dashloader.client.model.ModelModule;
import dev.notalpha.dashloader.mixin.accessor.AndMultipartModelSelectorAccessor;
import dev.notalpha.dashloader.mixin.accessor.MultipartModelComponentAccessor;
import dev.notalpha.dashloader.mixin.accessor.OrMultipartModelSelectorAccessor;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.model.*;
import net.minecraft.client.render.model.json.AndMultipartModelSelector;
import net.minecraft.client.render.model.json.MultipartModelComponent;
import net.minecraft.client.render.model.json.MultipartModelSelector;
import net.minecraft.client.render.model.json.OrMultipartModelSelector;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.state.StateManager;
import net.minecraft.util.Identifier;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

@Mixin(MultipartUnbakedModel.class)
public class MultipartUnbakedModelMixin {
	@Shadow
	@Final
	private List<MultipartModelComponent> components;

	@Shadow
	@Final
	private StateManager<Block, BlockState> stateFactory;

	@Inject(
			method = "bake",
			at = @At(value = "RETURN"),
			locals = LocalCapture.CAPTURE_FAILSOFT,
			cancellable = true
	)
	private void addPredicateInfo(Baker baker, Function<SpriteIdentifier, Sprite> textureGetter, ModelBakeSettings rotationContainer, Identifier modelId, CallbackInfoReturnable<@Nullable BakedModel> cir, MultipartBakedModel.Builder builder) {
		ModelModule.MULTIPART_PREDICATES.visit(Cache.Status.SAVE, map -> {
			var bakedModel = (MultipartBakedModel) builder.build();
			var outSelectors = new ArrayList<MultipartModelSelector>();
			this.components.forEach(multipartModelComponent -> outSelectors.add(((MultipartModelComponentAccessor) multipartModelComponent).getSelector()));
			map.put(bakedModel, Pair.of(outSelectors, this.stateFactory));
			cir.setReturnValue(bakedModel);
		});
	}

}
