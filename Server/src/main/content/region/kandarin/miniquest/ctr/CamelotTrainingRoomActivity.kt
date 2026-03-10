package content.region.kandarin.miniquest.ctr

import content.data.GameAttributes
import core.api.*
import core.game.activity.ActivityManager
import core.game.activity.ActivityPlugin
import core.game.node.entity.Entity
import core.game.node.entity.player.Player
import core.game.system.timer.impl.SkillRestore
import core.game.world.map.Location
import core.game.world.map.zone.ZoneBorders
import core.game.world.map.zone.ZoneRestriction
import core.plugin.ClassScanner
import core.plugin.Initializable

private val SAFE_LOCATION: Location = Location.create(2750, 3507, 2)

/**
 * Represents the Training Grounds activity.
 */
@Initializable
class CamelotTrainingRoomActivity : ActivityPlugin("Knight's training", true, false, true), MapArea {

    init {
        ActivityManager.register(this)
        ClassScanner.definePlugin(CamelotTrainingRoomNPC())
    }

    override fun death(entity: Entity, killer: Entity): Boolean {
        if (entity is Player) teleport(entity, SAFE_LOCATION)
        return entity is Player
    }

    override fun areaEnter(entity: Entity) {
        super.areaEnter(entity)
        if (entity !is Player) return

        setAttribute(entity, GameAttributes.PRAYER_LOCK, true)
        entity.hook(Event.PrayerDeactivated, SkillRestore.PrayerDeactivatedHook)
        entity.hook(Event.PrayerActivated, SkillRestore.PrayerActivatedHook)

        registerLogoutListener(entity, "Knight's training") { _ ->
            entity.unhook(SkillRestore.PrayerDeactivatedHook)
            entity.unhook(SkillRestore.PrayerActivatedHook)
            teleport(entity, SAFE_LOCATION)
        }
    }

    override fun areaLeave(entity: Entity, logout: Boolean) {
        super.areaLeave(entity, logout)
        if (entity !is Player) return

        removeAttributes(
            entity,
            GameAttributes.PRAYER_LOCK,
            GameAttributes.KW_SPAWN,
            GameAttributes.KW_TIER,
            GameAttributes.KW_BEGIN
        )

        CamelotTrainingRoomNPC().getIds().firstOrNull()?.let { id ->
            findLocalNPC(entity, id)?.let { poofClear(it) }
        }

        clearLogoutListener(entity, "Knight's training")
        entity.unhook(SkillRestore.PrayerActivatedHook)
        entity.unhook(SkillRestore.PrayerDeactivatedHook)
    }

    override fun start(player: Player?, login: Boolean, vararg args: Any?): Boolean =
        super.start(player, login, *args)

    override fun defineAreaBorders(): Array<ZoneBorders> =
        arrayOf(ZoneBorders(2752, 3502, 2764, 3513, 2, false))

    override fun newInstance(p: Player?): ActivityPlugin = CamelotTrainingRoomActivity()
    override fun getRestrictions(): Array<ZoneRestriction> = arrayOf(ZoneRestriction.CANNON, ZoneRestriction.RANDOM_EVENTS, ZoneRestriction.FOLLOWERS)
    override fun getSpawnLocation(): Location = SAFE_LOCATION
}