package dev.celestiacraft.create_steam_ages.common.register;

import com.tterrag.registrate.util.entry.FluidEntry;
import com.tterrag.registrate.util.nullness.NonNullBiConsumer;
import dev.celestiacraft.create_steam_ages.CreateSteamAges;
import dev.celestiacraft.create_steam_ages.common.fluid.SteamFluid;
import dev.celestiacraft.create_steam_ages.tags.CSAFluidTags;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraftforge.fluids.ForgeFlowingFluid;

public class CSAFluids {
	public static final FluidEntry<ForgeFlowingFluid.Flowing> STEAM;
	public static final FluidEntry<ForgeFlowingFluid.Flowing> HIGH_TEMPERATURE_STEAM;

	static {
		STEAM = CreateSteamAges.REGISTRATE.fluid(
						"steam",
						CreateSteamAges.loadResource("fluid/steam_still"),
						CreateSteamAges.loadResource("fluid/steam_flow"),
						SteamFluid.create(0xFAFAFA, () -> {
							return 1.0f / 8.0f;
						}, 0xFFFAFAFA))
				.renderType(() -> RenderType.translucent())
				.properties((properties) -> {
					properties.density(-10)
							.viscosity(1)
							.temperature(473)
							.canPushEntity(false)
							.canConvertToSource(false)
							.canDrown(true);
				})
				.fluidProperties((properties) -> {
					properties.slopeFindDistance(3)
							.explosionResistance(100.0F);
				})
				.tag(CSAFluidTags.STEAM)
				.source(ForgeFlowingFluid.Source::new)
				.bucket()
				.tag(ItemTags.create(ResourceLocation.parse("forge:buckets/steam")))
				.tag(ItemTags.create(ResourceLocation.parse("forge:buckets")))
				.model(NonNullBiConsumer.noop())
				.build()
				.register();

		HIGH_TEMPERATURE_STEAM = CreateSteamAges.REGISTRATE.fluid(
						"high_temperature_steam",
						CreateSteamAges.loadResource("fluid/high_temperature_steam_still"),
						CreateSteamAges.loadResource("fluid/high_temperature_steam_flow"),
						SteamFluid.create(0xFAFAFA, () -> {
							return 1.0f / 8.0f;
						}, 0xFFFAFAFA))
				.renderType(() -> RenderType.translucent())
				.properties((properties) -> {
					properties.density(-10)
							.viscosity(1)
							.temperature(473)
							.canPushEntity(false)
							.canConvertToSource(false)
							.canDrown(true);
				})
				.fluidProperties((properties) -> {
					properties.slopeFindDistance(3)
							.explosionResistance(100.0F);
				})
				.tag(CSAFluidTags.HIGH_TEMPERATURE_STEAM)
				.source(ForgeFlowingFluid.Source::new)
				.bucket()
				.model(NonNullBiConsumer.noop())
				.tag(ItemTags.create(ResourceLocation.parse("forge:buckets/high_temperature_steam")))
				.tag(ItemTags.create(ResourceLocation.parse("forge:buckets")))
				.build()
				.register();
	}

	public static void register() {
	}
}