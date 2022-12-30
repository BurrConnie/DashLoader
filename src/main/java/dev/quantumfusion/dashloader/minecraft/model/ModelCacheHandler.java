package dev.quantumfusion.dashloader.minecraft.model;

import dev.quantumfusion.dashloader.DashLoader;
import dev.quantumfusion.dashloader.api.DashCacheHandler;
import dev.quantumfusion.dashloader.api.option.Option;
import dev.quantumfusion.dashloader.config.ConfigHandler;
import dev.quantumfusion.dashloader.io.data.collection.IntIntList;
import dev.quantumfusion.dashloader.minecraft.model.fallback.DashMissingDashModel;
import dev.quantumfusion.dashloader.registry.RegistryFactory;
import dev.quantumfusion.dashloader.registry.RegistryReader;
import dev.quantumfusion.dashloader.thread.ThreadHandler;
import dev.quantumfusion.dashloader.util.OptionData;
import dev.quantumfusion.taski.builtin.StepTask;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.block.BlockModels;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.json.MultipartModelSelector;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.registry.Registries;
import net.minecraft.state.StateManager;
import net.minecraft.util.Identifier;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ModelCacheHandler implements DashCacheHandler<ModelCacheHandler.Data> {
	public static final OptionData<HashMap<Identifier, BakedModel>> MODELS = new OptionData<>();
	public static final OptionData<HashMap<BakedModel, DashMissingDashModel>> MISSING_WRITE = new OptionData<>();
	public static final OptionData<HashMap<BlockState, Identifier>> MISSING_READ = new OptionData<>();
	public static final OptionData<HashMap<BakedModel, Pair<List<MultipartModelSelector>, StateManager<Block, BlockState>>>> MULTIPART_PREDICATES = new OptionData<>( DashLoader.Status.SAVE);
	public static final OptionData<HashMap<MultipartModelSelector, StateManager<Block, BlockState>>> STATE_MANAGERS = new OptionData<>(DashLoader.Status.SAVE);

	@Override
	public void prepareForSave() {
		MODELS.set(DashLoader.Status.SAVE, new HashMap<>());
		MISSING_WRITE.set(DashLoader.Status.SAVE, new HashMap<>());
		MISSING_READ.set(DashLoader.Status.SAVE, new HashMap<>());
		MULTIPART_PREDICATES.set(DashLoader.Status.SAVE, new HashMap<>());
		STATE_MANAGERS.set(DashLoader.Status.SAVE, new HashMap<>());
	}

	@Override
	public Data saveMappings(RegistryFactory writer, StepTask task) {
		var missingModelsWrite = MISSING_WRITE.get(DashLoader.Status.SAVE);
		var models = MODELS.get(DashLoader.Status.SAVE);

		if (missingModelsWrite == null || models == null) {
			return null;
		} else  {
			var outModels  = new IntIntList(new ArrayList<>(models.size()));
			task.doForEach(models, (identifier, bakedModel) -> {
				if (bakedModel != null) {
					final int add = writer.add(bakedModel);
					if (!missingModelsWrite.containsKey(bakedModel)) {
						outModels.put(writer.add(identifier), add);
					}
				}
			});

			return new Data(outModels);
		}
	}

	@Override
	public void loadMappings(Data mappings, RegistryReader reader, StepTask task) {
		final HashMap<Identifier, BakedModel> out = new HashMap<>(mappings.models.list().size());
		mappings.models.forEach((key, value) -> out.put(reader.get(key), reader.get(value)));

		var missingModelsRead = new HashMap<BlockState, Identifier>();
		var tasks = new ArrayList<Runnable>();
		DashLoader.LOG.info("Scanning Blocks");
		for (Block block : Registries.BLOCK) {
			tasks.add(() -> block.getStateManager().getStates().forEach((blockState) -> {
				final ModelIdentifier modelId = BlockModels.getModelId(blockState);
				if (!out.containsKey(modelId)) {
					missingModelsRead.put(blockState, modelId);
				}
			}));
		}

		DashLoader.LOG.info("Verifying {} BlockStates", tasks.size());
		ThreadHandler.INSTANCE.parallelRunnable(tasks);
		DashLoader.LOG.info("Found {} Missing BlockState Models", missingModelsRead.size());

		MISSING_READ.set(DashLoader.Status.LOAD, missingModelsRead);
		MODELS.set(DashLoader.Status.LOAD, out);
	}

	@Override
	public Class<Data> getDataClass() {
		return Data.class;
	}

	@Override
	public float taskWeight() {
		return 1000;
	}

	@Override
	public boolean isActive() {
		return ConfigHandler.optionActive(Option.CACHE_MODEL_LOADER);
	}

	public static final class Data {
		public final IntIntList models; // identifier to model list

		public Data(IntIntList models) {
			this.models = models;
		}
	}
}