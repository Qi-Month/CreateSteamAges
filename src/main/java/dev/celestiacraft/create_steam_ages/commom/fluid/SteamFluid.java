package dev.celestiacraft.create_steam_ages.commom.fluid;

import com.mojang.blaze3d.shaders.FogShape;
import com.mojang.blaze3d.systems.RenderSystem;
import com.tterrag.registrate.builders.FluidBuilder;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.material.FluidState;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidType;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Pure Forge fluid type, does not extend Create's TintedFluidType.
 * tint is ARGB; alpha must be 0xFF (opaque), otherwise the translucent
 * fluid render layer would multiply the texture alpha to 0.
 */
public class SteamFluid extends FluidType {
	private Vector3f fogColor;
	private Supplier<Float> fogDistance;
	private final ResourceLocation stillTexture;
	private final ResourceLocation flowingTexture;
	private final int tintColor;

	public SteamFluid(Properties properties, ResourceLocation stillTexture, ResourceLocation flowingTexture, int tintColor) {
		super(properties);
		this.stillTexture = stillTexture;
		this.flowingTexture = flowingTexture;
		this.tintColor = tintColor;
	}

	public static FluidBuilder.FluidTypeFactory create(int fogColor, Supplier<Float> fogDistance, int tintColor) {
		return (properties, still, flowing) -> {
			SteamFluid fluidType = new SteamFluid(properties, still, flowing, tintColor);
			fluidType.fogColor = new Vector3f(
					((fogColor >> 16) & 0xFF) / 255.0f,
					((fogColor >> 8) & 0xFF) / 255.0f,
					(fogColor & 0xFF) / 255.0f);
			fluidType.fogDistance = fogDistance;
			return fluidType;
		};
	}

	@Override
	public void initializeClient(Consumer<IClientFluidTypeExtensions> consumer) {
		consumer.accept(new IClientFluidTypeExtensions() {
			@Override
			public ResourceLocation getStillTexture() {
				return stillTexture;
			}

			@Override
			public ResourceLocation getFlowingTexture() {
				return flowingTexture;
			}

			@Override
			public int getTintColor(FluidStack stack) {
				return tintColor;
			}

			@Override
			public int getTintColor(FluidState state, BlockAndTintGetter getter, BlockPos pos) {
				return tintColor;
			}

			@Override
			public @NotNull Vector3f modifyFogColor(
					Camera camera,
					float partialTick,
					ClientLevel level,
					int renderDistance,
					float darkenWorldAmount,
					Vector3f fluidFogColor
			) {
				return fogColor == null ? fluidFogColor : fogColor;
			}

			@Override
			public void modifyFogRender(
					Camera camera, FogRenderer.FogMode mode,
					float renderDistance, float partialTick,
					float nearDistance,
					float farDistance,
					FogShape shape
			) {
				float modifier = fogDistance.get();
				if (modifier != 1.0f) {
					RenderSystem.setShaderFogShape(FogShape.CYLINDER);
					RenderSystem.setShaderFogStart(-8);
					RenderSystem.setShaderFogEnd(96.0f * modifier);
				}
			}
		});
	}
}