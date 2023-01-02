package dev.notalpha.dashloader.client.sprite;

import dev.notalpha.dashloader.api.DashObject;
import dev.notalpha.dashloader.misc.UnsafeHelper;
import dev.notalpha.dashloader.mixin.accessor.SpriteAccessor;
import dev.notalpha.dashloader.registry.RegistryReader;
import dev.notalpha.dashloader.registry.RegistryWriter;
import net.minecraft.client.texture.Sprite;

public class DashSprite implements DashObject<Sprite> {
	public final int atlasId;
	public final DashSpriteContents contents;

	public final int x;
	public final int y;
	public final float uMin;
	public final float uMax;
	public final float vMin;
	public final float vMax;

	public DashSprite(int atlasId, DashSpriteContents contents, int x, int y, float uMin, float uMax, float vMin, float vMax) {
		this.atlasId = atlasId;
		this.contents = contents;
		this.x = x;
		this.y = y;
		this.uMin = uMin;
		this.uMax = uMax;
		this.vMin = vMin;
		this.vMax = vMax;
	}

	public DashSprite(Sprite sprite, RegistryWriter writer) {
		this.atlasId = writer.add(sprite.getAtlasId());
		this.contents = new DashSpriteContents(sprite.getContents(), writer);

		this.x = sprite.getX();
		this.y = sprite.getY();
		this.uMin = sprite.getMinU();
		this.uMax = sprite.getMaxU();
		this.vMin = sprite.getMinV();
		this.vMax = sprite.getMaxV();
	}

	@Override
	public Sprite export(final RegistryReader registry) {
		final Sprite out = UnsafeHelper.allocateInstance(Sprite.class);
		final SpriteAccessor spriteAccessor = ((SpriteAccessor) out);

		spriteAccessor.setAtlasId(registry.get(this.atlasId));
		spriteAccessor.setContents(this.contents.export(registry));

		spriteAccessor.setX(this.x);
		spriteAccessor.setY(this.y);
		spriteAccessor.setUMin(this.uMin);
		spriteAccessor.setUMax(this.uMax);
		spriteAccessor.setVMin(this.vMin);
		spriteAccessor.setVMax(this.vMax);
		return out;
	}
}