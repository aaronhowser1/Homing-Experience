package dev.aaronhowser.homingexperience.entity

import dev.aaronhowser.homingexperience.HomingExperience
import dev.aaronhowser.homingexperience.util.ModScheduler
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

    init {
        allHomingOrbs.add(this)

        targetPlayer = getNearestPlayer()

        experienceOrbEntity.apply {
            isNoGravity = true
            noPhysics = true
        }

        tick()
    }

    private fun getNearestPlayer(): Player? {
        return experienceOrbEntity.level.getNearestPlayer(experienceOrbEntity, 10.0)
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