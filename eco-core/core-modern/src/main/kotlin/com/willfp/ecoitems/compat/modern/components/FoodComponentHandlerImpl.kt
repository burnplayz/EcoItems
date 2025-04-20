package com.willfp.ecoitems.compat.modern.components

import com.willfp.eco.core.config.interfaces.Config
import com.willfp.ecoitems.items.components.FoodComponentHandler
import de.tr7zw.nbtapi.NBT
import de.tr7zw.nbtapi.NBTItem
import io.papermc.paper.datacomponent.item.FoodProperties
import org.bukkit.Bukkit
import org.bukkit.NamespacedKey
import org.bukkit.Registry
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffect

@Suppress("UnstableApiUsage")
class FoodComponentHandlerImpl : FoodComponentHandler() {


    override fun apply(item: ItemStack, config: Config) {
        if (isServerVersionAtLeast1_21_3()) {
            applyModern(item, config)
        } else {
            applyOld(item, config)
        }
    }

    fun applyModern(item: ItemStack, config: Config) {
        val meta = item.itemMeta ?: return


        val nutrition = config.getInt("nutrition")
        val saturation = config.getDouble("saturation").toFloat()
        var canAlwaysEat = false
        if (config.has("can-always-eat")) {
            canAlwaysEat = config.getBool("can-always-eat")


            var consumeSeconds = config.getDouble("eat-seconds").toFloat()

            val effects = mutableListOf<Pair<PotionEffect, Float>>()

            for (effectConfig in config.getSubsections("effects")) {
                val effect = Registry.POTION_EFFECT_TYPE.get(
                    NamespacedKey.minecraft(effectConfig.getString("effect").lowercase())
                ) ?: continue

                val amplifier = effectConfig.getInt("level") - 1
                val duration = effectConfig.getInt("duration")

                val ambient = effectConfig.getBoolOrNull("ambient") ?: true
                val particles = effectConfig.getBoolOrNull("particles") ?: true
                val icon = effectConfig.getBoolOrNull("icon") ?: true

                val probability = (effectConfig.getDoubleOrNull("probability") ?: 100.0).toFloat() / 100f

                effects.add(Pair(PotionEffect(effect, duration, amplifier, ambient, particles, icon), probability))

            }
            createCustomFoodItem(item, nutrition, saturation, canAlwaysEat, consumeSeconds, effects)

        }
    }

    private fun createCustomFoodItem(
        item: ItemStack,
        nutrition: Int,
        saturation: Float,
        canAlwaysEat: Boolean,
        consumeSeconds: Float,
        effectEntries: List<Pair<PotionEffect, Float>>
    ) {
        NBT.modifyComponents(item) { components ->
            // Food component
            val food = components.getOrCreateCompound("minecraft:food").apply {
                setInteger("nutrition", nutrition)
                setFloat("saturation", saturation)
                setByte("can_always_eat", if (canAlwaysEat) 1.toByte() else 0.toByte())
            }

            // Consumable component
            val consumable = components.getOrCreateCompound("minecraft:consumable").apply {
                setFloat("consume_seconds", consumeSeconds)

                // Effects
                getCompoundList("on_consume_effects").apply {
                    clear()
                    effectEntries.forEach { (effect, chance) ->
                        addCompound().apply {
                            setString("type", "minecraft:apply_effects")
                            getCompoundList("effects").addCompound().apply {
                                setString("id", effect.type.key.toString())
                                setInteger("duration", effect.duration)
                                setByte("amplifier", effect.amplifier.toByte())
                                setByte("show_icon", 1.toByte())
                            }
                        }
                    }
                }
            }
        }
    }


        fun isServerVersionAtLeast1_21_3(): Boolean {
            val serverVersion = Bukkit.getVersion()

            // Extract version parts (e.g., "1.21.3" from "git-Paper-1.21.3-R0.1-SNAPSHOT")
            val versionMatch = Regex("(?<=MC: ?|\\s)\\d+\\.\\d+(?:\\.\\d+)?").find(serverVersion)
            val versionString = versionMatch?.value ?: return false

            // Split into major, minor, patch (e.g., [1, 21, 3])
            val parts = versionString.split('.').map { it.toIntOrNull() ?: 0 }

            // Compare versions
            return when {
                parts.size < 2 -> false // Invalid format
                parts[0] > 1 -> true   // 2.x.x, 3.x.x, etc. (future versions)
                parts[0] == 1 && parts[1] > 21 -> true // 1.22+, 1.23+, etc.
                parts[0] == 1 && parts[1] == 21 && parts.getOrNull(2) ?: 0 >= 3 -> true // 1.21.3+
                else -> false // Older than 1.21.3
            }
        }

        fun applyOld(item: ItemStack, config: Config) {
            val meta = item.itemMeta ?: return
            val food = meta.food
            food.nutrition = config.getInt("nutrition")
            food.saturation = config.getDouble("saturation").toFloat()
            //food.eatSeconds = config.getDouble("eat-seconds").toFloat()

            if (config.has("can-always-eat")) {
                food.setCanAlwaysEat(config.getBool("can-always-eat"))
            }

            //food.effects = mutableListOf()

            for (effectConfig in config.getSubsections("effects")) {
                val effect = Registry.POTION_EFFECT_TYPE.get(
                    NamespacedKey.minecraft(effectConfig.getString("effect").lowercase())
                ) ?: continue

                val amplifier = effectConfig.getInt("level") - 1
                val duration = effectConfig.getInt("duration")

                val ambient = effectConfig.getBoolOrNull("ambient") ?: true
                val particles = effectConfig.getBoolOrNull("particles") ?: true
                val icon = effectConfig.getBoolOrNull("icon") ?: true

                val probability = (effectConfig.getDoubleOrNull("probability") ?: 100.0).toFloat() / 100f

                //food.addEffect(
                //    PotionEffect(
                //        effect,
                //        duration,
                //        amplifier,
                //        ambient,
                //        particles,
                //        icon
                //    ),
                //    probability
                //)
            }

            meta.setFood(food)
            item.itemMeta = meta
        }
    }

