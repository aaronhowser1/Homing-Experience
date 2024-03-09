package dev.aaronhowser.homingexperience.entity

import dev.aaronhowser.homingexperience.HomingExperience
import dev.aaronhowser.homingexperience.config.ServerConfig
import dev.aaronhowser.homingexperience.util.ModScheduler
import net.minecraft.util.Mth
import net.minecraft.world.entity.ExperienceOrb
import net.minecraft.world.entity.player.Player

// Not an actual entity! Just has the logic etc, for compatibility reasons with anything that needs vanilla xp orbs
class HomingExperienceEntity(
    private val experienceOrbEntity: ExperienceOrb
) {

    companion object {
        val allHomingOrbs = mutableSetOf<HomingExperienceEntity>()
    }

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

            }
        }

    private val hasTarget: Boolean
        get() = targetPlayer != null

    init {
        allHomingOrbs.add(this)

        HomingExperience.LOGGER.debug("New homing orb spawned")
        targetPlayer = getNearestPlayer()

        tick()
    }

    private fun getNearestPlayer(): Player? {

        fun isPlayerValid(player: Player): Boolean {

            if (player.isSpectator) return false
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

        ModScheduler.scheduleSynchronisedTask(1) {
            tick()
        }
    }

    private fun moveCloser() {
        experienceOrbEntity.apply {
            val target = targetPlayer ?: return
            if (target.level != level) {
                targetPlayer = null

                return
            }

            val differenceVector = target.eyePosition.subtract(position())
            val distanceSquared = differenceVector.lengthSqr()

            if (distanceSquared < 1) {
                targetPlayer?.takeXpDelay = 0
                return
            }

            val distance = Mth.sqrt(distanceSquared.toFloat())

            val speed = ServerConfig.MAX_SPEED

            val motion = differenceVector.scale(speed / distance.toDouble())

            deltaMovement = motion
        }
    }

    private fun removeHoming() {
        HomingExperience.LOGGER.debug("Removing homing orb")

        enabled = false
        allHomingOrbs.remove(this)
    }
}