package com.willfp.ecoweapons

import com.willfp.eco.core.EcoPlugin
import com.willfp.eco.core.command.impl.PluginCommand
import com.willfp.eco.core.display.DisplayModule
import com.willfp.eco.core.integrations.IntegrationLoader
import com.willfp.ecoweapons.commands.CommandEcoweapons
import com.willfp.ecoweapons.config.EcoWeaponsJson
import com.willfp.ecoweapons.display.WeaponsDisplay
import com.willfp.ecoweapons.util.DiscoverRecipeListener
import com.willfp.ecoweapons.weapons.FuelHandler
import com.willfp.ecoweapons.weapons.WeaponListener
import com.willfp.ecoweapons.weapons.WeaponUtils
import com.willfp.ecoweapons.weapons.toSingletonList
import com.willfp.libreforge.LibReforge
import org.bukkit.event.Listener

class EcoWeaponsPlugin : EcoPlugin(1241, 12134, "&#ff0000") {
    /**
     * tiers.json.
     */
    val ecoWeaponsJson: EcoWeaponsJson

    /**
     * Internal constructor called by bukkit on plugin load.
     */
    init {
        instance = this
        ecoWeaponsJson = EcoWeaponsJson(this)
        LibReforge.init(this)
        LibReforge.registerHolderProvider { WeaponUtils.getWeaponOnPlayer(it).toSingletonList() }
    }

    override fun handleEnable() {
        LibReforge.enable(this)
    }

    override fun handleDisable() {
        LibReforge.disable(this)
    }

    override fun handleReload() {
        LibReforge.reload(this)
        FuelHandler.createRunnable(this)
    }

    override fun loadListeners(): List<Listener> {
        return listOf(
            DiscoverRecipeListener(this),
            WeaponListener(this),
            FuelHandler()
        )
    }

    override fun loadIntegrationLoaders(): List<IntegrationLoader> {
        return LibReforge.getIntegrationLoaders()
    }

    override fun loadPluginCommands(): List<PluginCommand> {
        return listOf(
            CommandEcoweapons(this)
        )
    }

    override fun createDisplayModule(): DisplayModule {
        return WeaponsDisplay(this)
    }

    companion object {
        /**
         * Instance of EcoWeapons.
         */
        @JvmStatic
        lateinit var instance: EcoWeaponsPlugin
    }
}