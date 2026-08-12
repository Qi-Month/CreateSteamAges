package dev.celestiacraft.create_steam_ages.boiler;

/**
 * Implemented by the BoilerData mixin so the steam fluid handler can push
 * steam input info back into the boiler without touching Create's classes.
 */
public interface SteamBoilerData {
	/**
	 * @param level  9 for normal steam, 18 for high temperature steam
	 * @param amount amount of steam fluid accepted this call (mB)
	 */
	void csa$notifySteamInput(int level, int amount);
}
