package dev.notalpha.dashloader.client.model.predicates;

import dev.notalpha.dashloader.api.DashObject;
import dev.notalpha.dashloader.mixin.accessor.AndMultipartModelSelectorAccessor;
import dev.notalpha.dashloader.mixin.accessor.OrMultipartModelSelectorAccessor;
import dev.notalpha.dashloader.registry.RegistryReader;
import dev.notalpha.dashloader.registry.RegistryWriter;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.model.json.AndMultipartModelSelector;
import net.minecraft.client.render.model.json.MultipartModelSelector;
import net.minecraft.client.render.model.json.OrMultipartModelSelector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;


public final class DashOrPredicate implements DashObject<OrMultipartModelSelector> {
	public final int[] selectors;

	public DashOrPredicate(int[] selectors) {
		this.selectors = selectors;
	}

	public DashOrPredicate(OrMultipartModelSelector selector, RegistryWriter writer) {
		OrMultipartModelSelectorAccessor access = ((OrMultipartModelSelectorAccessor) selector);

		Iterable<? extends MultipartModelSelector> accessSelectors = access.getSelectors();
		int count = 0;
		for (MultipartModelSelector ignored : accessSelectors) {
			count += 1;
		}
		this.selectors = new int[count];

		int i = 0;
		for (MultipartModelSelector accessSelector : accessSelectors) {
			this.selectors[i++] = writer.add(accessSelector);
		}
	}

	@Override
	public OrMultipartModelSelector export(RegistryReader handler) {
		final List<MultipartModelSelector> selectors = new ArrayList<>(this.selectors.length);
		for (int accessSelector : this.selectors) {
			selectors.add(handler.get(accessSelector));
		}

		return new OrMultipartModelSelector(selectors);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		DashOrPredicate that = (DashOrPredicate) o;

		return Arrays.equals(selectors, that.selectors);
	}

	@Override
	public int hashCode() {
		return Arrays.hashCode(selectors);
	}
}
