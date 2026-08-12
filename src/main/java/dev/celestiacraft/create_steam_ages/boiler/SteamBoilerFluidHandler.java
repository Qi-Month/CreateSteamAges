package dev.celestiacraft.create_steam_ages.boiler;

import com.simibubi.create.content.fluids.tank.BoilerData;

import dev.celestiacraft.create_steam_ages.tags.CSAFluidTags;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;

/**
 * 锅炉输入处理器, 除了接受水(原版行为)外, 还接受 {@code forge:steam} / {@code forge:high_temperature_steam} 流体
 * <p>
 * 蒸汽的消耗方式与水完全相同：每个通过 {@link #fill} 接受的 mB 都会计入锅炉的采样供应量
 * <p>
 * 因此水供应所决定的等级上限(每级每 tick 为 {@code ceil(supply) / 10} mB)保持不变
 * <p>
 * 高温蒸汽会额外将锅炉的等级提升至 18, 普通蒸汽则提升至 9
 */
public class SteamBoilerFluidHandler extends BoilerData.BoilerFluidHandler {
	private final BoilerData owner;

	public SteamBoilerFluidHandler(BoilerData owner) {
		owner.super();
		this.owner = owner;
	}

	@Override
	public boolean isFluidValid(int tank, FluidStack stack) {
		return super.isFluidValid(tank, stack) || isSteam(stack);
	}

	@Override
	public int fill(FluidStack resource, FluidAction action) {
		int filled = super.fill(resource, action);
		if (filled > 0 && action.execute()
				&& isSteam(resource)
				&& owner instanceof SteamBoilerData steamBoiler)
			steamBoiler.csa$notifySteamInput(steamHeatLevel(resource), filled);
		return filled;
	}

	private static boolean isSteam(FluidStack stack) {
		Fluid fluid = stack.getFluid();

		return fluid.is(CSAFluidTags.STEAM)
				|| fluid.is(CSAFluidTags.HIGH_TEMPERATURE_STEAM);
	}

	private static int steamHeatLevel(FluidStack stack) {
		Fluid fluid = stack.getFluid();

		return fluid.is(CSAFluidTags.HIGH_TEMPERATURE_STEAM) ? 18 : 9;
	}
}