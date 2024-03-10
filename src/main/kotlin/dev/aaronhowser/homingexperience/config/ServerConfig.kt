package dev.aaronhowser.homingexperience.config

import net.minecraftforge.common.ForgeConfigSpec
import net.minecraftforge.common.ForgeConfigSpec.ConfigValue

object ServerConfig {

    private val BUILDER = ForgeConfigSpec.Builder()
    val SPEC: ForgeConfigSpec

    private val homingRadius: ConfigValue<Double>
    private val maxSpeed: ConfigValue<Double>
    private val acceleration: ConfigValue<Double>

    val HOMING_RADIUS: Float
        get() = homingRadius.get().toFloat()

    val MAX_SPEED: Float
        get() = maxSpeed.get().toFloat()

    val ACCELERATION: Float
        get() = acceleration.get().toFloat()

    init {
        BUILDER.push("Server")

        homingRadius = BUILDER
            .comment("The radius in which the experience orbs will home in on the player")
            .defineInRange("homingRadius", 50.0, 0.1, 1000.0)

        maxSpeed = BUILDER
            .comment("The maximum speed the experience orbs will travel at")
            .defineInRange("maxSpeed", 0.5, 0.1, 10.0)

        acceleration = BUILDER
            .comment("The acceleration of the experience orbs")
            .defineInRange("acceleration", 0.01, 0.0, 1.0)

        BUILDER.pop()

        SPEC = BUILDER.build()
    }

}