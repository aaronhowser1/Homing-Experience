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

    private var targetPlayer: Player? = null
        set(value) {
            if (value != field) {
                field = value
                HomingExperience.LOGGER.debug("New target player: $value")
            }
        }

    private val hasTarget: Boolean
        get() = targetPlayer != null

    init {
        allHomingOrbs.add(this)

        HomingExperience.LOGGER.debug("New homing orb spawned")
        targetPlayer = getNearestPlayer()

        experienceOrbEntity.apply {
            isNoGravity = hasTarget
            noPhysics = hasTarget

            setGlowingTag(hasTarget)
        }

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
            val dX = target.x - x
            val dY = target.y - y
            val dZ = target.z - z

            val distance = distanceTo(target)

            if (distance < 1) {
                targetPlayer?.takeXpDelay = 0
                return
            }

            val speed = 0.5f
            val motionX = dX / distance * speed
            val motionY = dY / distance * speed
            val motionZ = dZ / distance * speed

            setDeltaMovement(motionX, motionY, motionZ)
        }
    }

    private fun removeHoming() {
        HomingExperience.LOGGER.debug("Removing homing orb")
        allHomingOrbs.remove(this)
    }
}