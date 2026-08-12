package dev.celestiacraft.create_steam_ages.tags;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;

public class CSAFluidTags {
	public static TagKey<Fluid>
			STEAM,
			HIGH_TEMPERATURE_STEAM;

	static {
		STEAM = create("forge", "steam");
		HIGH_TEMPERATURE_STEAM = create("forge", "high_temperature_steam");
	}

	private static TagKey<Fluid> create(String namespace, String name) {
		return FluidTags.create(ResourceLocation.fromNamespaceAndPath(namespace, name));
	}
}