package dev.celestiacraft.create_steam_ages.mixin;

import com.simibubi.create.content.fluids.tank.BoilerData;
import com.simibubi.create.content.fluids.tank.FluidTankBlockEntity;
import dev.celestiacraft.create_steam_ages.boiler.SteamBoilerData;
import dev.celestiacraft.create_steam_ages.boiler.SteamBoilerFluidHandler;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 使 Create 锅炉能够使用蒸汽运行:
 * <ul>
 * <li>锅炉的流体处理器除了原版的水之外, 还接受蒸汽(以及高温蒸汽)流体, </li>
 * <li>蒸汽输入会为锅炉提供“水”供应量(与水相同的消耗计算方式: 每级 10 mB/t),
 * 因此无需额外提供水, </li>
 * <li>在持续输入蒸汽时, 锅炉的热量等级会被强制设为 9(普通蒸汽)或 18(高温蒸汽),
 * 而不是读取锅炉下方的热源, 因此无需烈焰人燃烧室 / 营火, </li>
 * <li>护目镜面板会在原版信息的基础上显示蒸汽输入速率。</li>
 * </ul>
 * <p>
 * 所有目标均为 Create 自身的成员, 这些成员在重新混淆(reobfuscation)后仍会保留其名称,
 * 因此必须使用 {@code remap = false}(Create 是第三方模组, 不属于 Minecraft / Forge
 * 映射表的一部分)。
 */
@Mixin(BoilerData.class)
public class BoilerDataMixin implements SteamBoilerData {
	/**
	 * 0 = 没有蒸汽
	 * 9 = 普通蒸汽
	 * 18 = 高温蒸汽
	 */
	@Unique
	private int csa$steamHeatLevel;
	@Unique
	private int csa$steamGathered;

	/**
	 * 蒸汽输入速率(mB/t), 以与原版水供应相同的方式进行采样
	 */
	@Unique
	private float csa$steamSupply;
	@Unique
	private float[] csa$steamSupplyOverTime = new float[10];
	@Unique
	private int csa$steamSupplyIndex;

	@Shadow(remap = false)
	public boolean needsHeatLevelUpdate;

	@Shadow(remap = false)
	public boolean passiveHeat;

	@Shadow(remap = false)
	public int activeHeat;

	@Shadow(remap = false)
	private int maxHeatForWater;

	@Shadow(remap = false)
	private int maxHeatForSize;

	@Final
	@Shadow(remap = false)
	private static int waterSupplyPerLevel;

	@Final
	@Shadow(remap = false)
	static int SAMPLE_RATE;

	/**
	 * 将锅炉流体处理器替换为一个同时接受蒸汽的处理器
	 *
	 * @author
	 * @reason
	 * @return
	 */
	@Overwrite(remap = false)
	public BoilerData.BoilerFluidHandler createHandler() {
		return new SteamBoilerFluidHandler((BoilerData) (Object) this);
	}

	/**
	 * 在原版底部热量扫描完成后, 只要当前有蒸汽供应, 就使用蒸汽热量覆盖原版热量
	 *
	 * @param entity
	 * @param returnable
	 */
	@Inject(method = "updateTemperature", at = @At("RETURN"), remap = false, cancellable = true)
	private void csa$applySteamHeat(FluidTankBlockEntity entity, CallbackInfoReturnable<Boolean> returnable) {
		if (csa$steamHeatLevel <= 0) {
			return;
		}

		int prevActive = activeHeat;
		boolean prevPassive = passiveHeat;
		activeHeat = csa$steamHeatLevel;
		passiveHeat = false;

		if (prevActive != activeHeat || prevPassive != passiveHeat) {
			returnable.setReturnValue(true);
		}
	}

	/**
	 * 在每个供应量采样点, 以与原版水供应相同的方式采样蒸汽输入速率
	 * 如果在上一个采样窗口内没有输入蒸汽, 则降低蒸汽热量, 随后重置计数器
	 *
	 * @param entity
	 * @param info
	 */
	@Inject(
			method = "tick",
			at = @At(
					value = "FIELD",
					target = "Lcom/simibubi/create/content/fluids/tank/BoilerData;gatheredSupply:I",
					opcode = Opcodes.PUTFIELD
			),
			remap = false
	)
	private void csa$sampleSteamSupply(FluidTankBlockEntity entity, CallbackInfo info) {
		if (csa$steamGathered == 0 && csa$steamHeatLevel != 0) {
			csa$steamHeatLevel = 0;
			needsHeatLevelUpdate = true;
		}

		csa$steamSupplyOverTime[csa$steamSupplyIndex] = csa$steamGathered / (float) SAMPLE_RATE;
		csa$steamSupply = Math.max(csa$steamSupply, csa$steamSupplyOverTime[csa$steamSupplyIndex]);
		csa$steamSupplyIndex = (csa$steamSupplyIndex + 1) % csa$steamSupplyOverTime.length;

		if (csa$steamSupplyIndex == 0) {
			csa$steamSupply = 0;

			for (float sample : csa$steamSupplyOverTime) {
				csa$steamSupply = Math.max(sample, csa$steamSupply);
			}
		}

		csa$steamGathered = 0;
	}

//	/**
//	 * 当蒸汽驱动锅炉时, 将蒸汽输入速率添加到护目镜面板中
//	 * 布局与原版水输入速率保持一致
//	 */
//	@Inject(method = "addToGoggleTooltip", at = @At("RETURN"), remap = false)
//	private void csa$addSteamTooltip(
//			List<Component> tooltip,
//			boolean isPlayerSneaking,
//			int boilerSize,
//			CallbackInfoReturnable<Boolean> returnable
//	) {
//		if (csa$steamHeatLevel <= 0) {
//			return;
//		}
//
//		// 蒸汽模式: 清掉原版锅炉信息, 只显示蒸汽相关数据
//		tooltip.clear();
//
//		CreateLang.builder()
//				.add(Component.translatable("create_steam_ages.boiler.steam_input_rate")
//						.withStyle(ChatFormatting.GRAY))
//				.forGoggles(tooltip);
//		CreateLang.number(csa$steamSupply)
//				.style(ChatFormatting.BLUE)
//				.add(CreateLang.translate("generic.unit.millibuckets"))
//				.add(CreateLang.text(" / ")
//						.style(ChatFormatting.GRAY))
//				.add(CreateLang.translate("boiler.per_tick",
//								CreateLang.number(waterSupplyPerLevel)
//										.add(CreateLang.translate("generic.unit.millibuckets")))
//						.style(ChatFormatting.DARK_GRAY))
//				.forGoggles(tooltip, 1);
//
//		int actualLevel = Math.min(csa$steamHeatLevel, Math.min(maxHeatForWater, maxHeatForSize));
//		CreateLang.builder()
//				.add(Component.translatable("create_steam_ages.boiler.steam_level")
//						.withStyle(ChatFormatting.GRAY))
//				.forGoggles(tooltip);
//		CreateLang.number(actualLevel)
//				.style(ChatFormatting.AQUA)
//				.add(CreateLang.text(" / ")
//						.style(ChatFormatting.GRAY))
//				.add(CreateLang.number(18)
//						.style(ChatFormatting.DARK_GRAY))
//				.forGoggles(tooltip, 1);
//	}
//
//	/**
//	 * 将蒸汽信息同步到客户端, 供护目镜面板显示(护目镜面板在客户端渲染)
//	 *
//	 * @param returnable
//	 */
//	@Inject(method = "write", at = @At("RETURN"), remap = false)
//	private void csa$writeSteam(CallbackInfoReturnable<CompoundTag> returnable) {
//		CompoundTag nbt = returnable.getReturnValue();
//		nbt.putInt("CSA_SteamHeat", csa$steamHeatLevel);
//		nbt.putFloat("CSA_SteamSupply", csa$steamSupply);
//	}
//
//	@Inject(method = "read", at = @At("RETURN"), remap = false)
//	private void csa$readSteam(CompoundTag nbt, int boilerSize, CallbackInfo info) {
//		csa$steamHeatLevel = nbt.getInt("CSA_SteamHeat");
//		csa$steamSupply = nbt.getFloat("CSA_SteamSupply");
//	}

	@Override
	public void csa$notifySteamInput(int level, int amount) {
		if (level > csa$steamHeatLevel) {
			csa$steamHeatLevel = level;
			needsHeatLevelUpdate = true;
		}
		csa$steamGathered += amount;
	}
}