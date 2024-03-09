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
        val allHomingOrbs = mutableListOf<HomingExperienceEntity>()
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
            isNoGravity = !hasTarget
            noPhysics = !hasTarget

            setGlowingTag(isInWall)
        }

        tick()
    }

    private fun getNearestPlayer(): Player? {

        val nearbyPlayers = experienceOrbEntity.level.players()
            .filter {
                it.distanceToSqr(experienceOrbEntity) < Mth.square(ServerConfig.HOMING_RADIUS) &&
                        (experienceOrbEntity.isInWall || it.hasLineOfSight(experienceOrbEntity))
            }
            .sortedBy { it.distanceToSqr(experienceOrbEntity) }

        return nearbyPlayers.firstOrNull()
    }

    private fun tick() {

        experienceOrbEntity.apply {
            if (level.isClientSide) return
            if (isRemoved) {
                this@HomingExperienceEntity.remove()
                return
            }

            if (tickCount % 20 == 0) {
                targetPlayer = getNearestPlayer()
            }
        }

        ModScheduler.scheduleSynchronisedTask(1) {
            tick()
        }
    }

    private fun remove() {
        HomingExperience.LOGGER.debug("Removing homing orb")
        allHomingOrbs.remove(this)
    }
}