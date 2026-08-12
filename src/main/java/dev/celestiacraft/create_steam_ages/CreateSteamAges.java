package dev.celestiacraft.create_steam_ages;

import com.simibubi.create.foundation.data.CreateRegistrate;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(CreateSteamAges.MODID)
public class CreateSteamAges {
	public static final String MODID = "create_steam_ages";
	public static final String NAME = "Create: \"Steam\" Ages";
	public static final Logger LOGGER = LogManager.getLogger(NAME);
	public static final CreateRegistrate REGISTRATE = CreateRegistrate.create(MODID);

	public static ResourceLocation loadResource(String path) {
		return ResourceLocation.fromNamespaceAndPath(MODID, path);
	}

	public CreateSteamAges(FMLJavaModLoadingContext context) {
		IEventBus bus = context.getModEventBus();

		REGISTRATE.registerEventListeners(bus);
	}
}