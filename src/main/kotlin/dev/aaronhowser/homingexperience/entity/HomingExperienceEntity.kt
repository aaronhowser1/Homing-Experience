package dev.aaronhowser.homingexperience.entity

import dev.aaronhowser.homingexperience.HomingExperience
import dev.aaronhowser.homingexperience.config.ServerConfig
import dev.aaronhowser.homingexperience.util.ModScheduler
import net.minecraft.util.Mth
import net.minecraft.world.entity.ExperienceOrb
import net.minecraft.world.entity.player.Player
import kotlin.random.Random

// Not an actual entity! Just has the logic etc, for compatibility reasons with anything that needs vanilla xp orbs
class HomingExperienceEntity(
    private val experienceOrbEntity: ExperienceOrb
) {

    companion object {
        var amountOrbs: Int = 0
    }

    private val currentOrb = amountOrbs++

    private var enabled = true

    private var targetPlayer: Player? = null
        set(value) {
            if (value != field) {
                field = value
                HomingExperience.LOGGER.debug("New target player: $value")

                experienceOrbEntity.apply {
                    isNoGravity = hasTarget
                    noPhysics = hasTarget

                    setGlowingTag(hasTarget)
                }

                if (value == null) speed = 0f
            }
        }

    private val hasTarget: Boolean
        get() = targetPlayer != null

    init {
        HomingExperience.LOGGER.debug("New homing orb spawned")
        targetPlayer = getNearestPlayer()

        if (targetPlayer != null) {
            experienceOrbEntity.push(
                Random.nextDouble(-0.25, 0.25),
                Random.nextDouble(0.25, 0.75),
                Random.nextDouble(-0.25, 0.25)
            )
        }

        tick()
    }

    private fun getNearestPlayer(): Player? {

        fun isPlayerValid(player: Player): Boolean {

            if (player.isSpectator) return false

            if (player == targetPlayer && experienceOrbEntity.isInWall) return true
            if (player.distanceToSqr(experienceOrbEntity) > Mth.square(ServerConfig.HOMING_RADIUS)) return false

            return experienceOrbEntity.isInWall || player.hasLineOfSight(experienceOrbEntity)

        }

        return experienceOrbEntity.level.players()
            .filter { isPlayerValid(it) }
            .minByOrNull { it.distanceToSqr(experienceOrbEntity) }
    }

    private fun tick() {

        if (!enabled) return

        experienceOrbEntity.apply {

            if (level.isClientSide) return@tick
            if (isRemoved) {
                removeHoming()
                return@tick
            }

            if (tickCount % 20 == 0) {
                targetPlayer = getNearestPlayer()
            }
        }

        if (hasTarget) moveCloser()

        ModScheduler.scheduleTaskInTicks(1) {
            tick()
        }
    }

    private var speed: Float = 0f
        set(value) {
            field = value.coerceIn(0f, ServerConfig.MAX_SPEED)
        }

    private fun accelerate() {
        speed += ServerConfig.ACCELERATION
    }

    private fun moveCloser() {
        val target = targetPlayer ?: return
        if (target.level != experienceOrbEntity.level) {
            targetPlayer = null
            return
        }

        val differenceVector = target.eyePosition.subtract(experienceOrbEntity.position())
        val distanceSquared = differenceVector.lengthSqr()

        if (distanceSquared < 1) {
            targetPlayer?.takeXpDelay = 0
            return
        }

        accelerate()

        val motion = differenceVector.normalize().scale(speed.toDouble())

        experienceOrbEntity.push(
            motion.x,
            motion.y,
            motion.z
        )

    }

    private fun removeHoming() {
        enabled = false
    }
}