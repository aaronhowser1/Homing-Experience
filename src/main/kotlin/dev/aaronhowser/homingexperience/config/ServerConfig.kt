package dev.aaronhowser.homingexperience.config

import net.minecraftforge.common.ForgeConfigSpec
import net.minecraftforge.common.ForgeConfigSpec.ConfigValue

object ServerConfig {

    private val BUILDER = ForgeConfigSpec.Builder()
    val SPEC: ForgeConfigSpec

    private val homingRadius: ConfigValue<Double>

    val HOMING_RADIUS: Float
        get() = homingRadius.get().toFloat()

    init {
        BUILDER.push("Server")

        homingRadius = BUILDER
            .comment("The radius in which the experience orbs will home in on the player")
            .defineInRange("homingRadius", 100.0, 0.1, 1000.0)

        BUILDER.pop()

        SPEC = BUILDER.build()
    }

}