package dev.aaronhowser.homingexperience.event

import dev.aaronhowser.homingexperience.HomingExperience
import dev.aaronhowser.homingexperience.config.ServerConfig
import dev.aaronhowser.homingexperience.entity.HomingExperienceEntity
import dev.aaronhowser.homingexperience.util.ModScheduler
import net.minecraft.world.entity.ExperienceOrb
import net.minecraftforge.event.TickEvent
import net.minecraftforge.event.TickEvent.ServerTickEvent
import net.minecraftforge.event.entity.EntityJoinLevelEvent
import net.minecraftforge.event.entity.living.LivingDeathEvent
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.common.Mod

@Mod.EventBusSubscriber(
    modid = HomingExperience.MOD_ID
)
object ModCommonEvents {

    @SubscribeEvent
    fun onExperienceOrbSpawn(event: EntityJoinLevelEvent) {
        val entity = event.entity
        val level = event.level

        if (level.isClientSide || entity !is ExperienceOrb) return

        HomingExperienceEntity(entity)
    }

    @SubscribeEvent
    fun serverTick(event: ServerTickEvent) {
        if (event.phase == TickEvent.Phase.END) ModScheduler.tick++
    }

    @SubscribeEvent
    fun onMobDeath(event: LivingDeathEvent) {
        if (!ServerConfig.ONLY_HOME_ON_DEATH) return

        val entity = event.entity
        val level = entity.level
        if (level.isClientSide) return

        HomingExperienceEntity.addRecentDeathLocation(entity.position())
    }

}