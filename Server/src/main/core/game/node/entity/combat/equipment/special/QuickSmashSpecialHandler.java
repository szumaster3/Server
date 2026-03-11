package core.game.node.entity.combat.equipment.special;

import core.game.node.entity.Entity;
import core.game.node.entity.combat.BattleState;
import core.game.node.entity.combat.CombatStyle;
import core.game.node.entity.combat.DeathTask;
import core.game.node.entity.combat.MeleeSwingHandler;
import core.game.node.entity.impl.Animator.Priority;
import core.game.node.entity.player.Player;
import core.game.world.GameWorld;
import core.game.world.update.flag.context.Animation;
import core.game.world.update.flag.context.Graphics;
import core.plugin.Initializable;
import core.plugin.Plugin;
import core.tools.RandomFunction;
import shared.consts.Animations;
import shared.consts.Sounds;

import static core.api.ContentAPIKt.playGlobalAudio;

/**
 * Handles the granite maul special attack.
 *
 * @author Emperor
 */
@Initializable
public final class QuickSmashSpecialHandler extends MeleeSwingHandler implements Plugin<Object> {

    private static final int SPECIAL_ENERGY = 50;

    private static final Animation ANIMATION = new Animation(Animations.ATTACK_MAUL_1667, Priority.HIGH);

    private static final Graphics GRAPHICS = new Graphics(shared.consts.Graphics.GRANITE_MAUL_SPECIAL_340, 96);

    @Override
    public Object fireEvent(String identifier, Object... args) {
        switch (identifier) {
            case "instant_spec":
                return true;
        }
        return null;
    }

    @Override
    public Plugin<Object> newInstance(Object arg) throws Throwable {
        CombatStyle.MELEE.getSwingHandler().register(4153, this);
        return this;
    }

    @Override
    public int swing(Entity entity, Entity victim, BattleState state) {
        Player p = (Player) entity;
        if (victim == null) {
            victim = p.getProperties().getCombatPulse().getLastVictim();
            if (victim == null || GameWorld.getTicks() - p.getAttribute("combat-stop", -1) > 2 || !MeleeSwingHandler.Companion.canMelee(p, victim, 1)) {
                p.getPacketDispatch().sendMessage("Warning: Since the maul's special is an instant attack, it will be wasted when used on a");
                p.getPacketDispatch().sendMessage("first strike.");
                return -1;
            }
        }
        if (DeathTask.isDead(victim)) {
            return -1;
        }
        if (!p.getSettings().drainSpecial(SPECIAL_ENERGY)) {
            return -1;
        }
        visualize(entity, victim, null);
        int hit = 0;
        if (isAccurateImpact(entity, victim)) {
            hit = RandomFunction.random(calculateHit(entity, victim, 1.0) + 1);
        }
        if (victim.hasProtectionPrayer(CombatStyle.MELEE))
            hit *= (victim instanceof Player) ? 0.6 : 0;
        BattleState b = new BattleState();
        b.setEstimatedHit(victim.getImpactHandler().handleImpact(entity, hit, CombatStyle.MELEE).getAmount());
        addExperience(entity, victim, b);
        return 1;
    }

    @Override
    public void visualize(Entity entity, Entity victim, BattleState state) {
        playGlobalAudio(entity.getLocation(), Sounds.QUICKSMASH_2715);
        entity.visualize(ANIMATION, GRAPHICS);
        victim.animate(victim.getProperties().getDefenceAnimation());
    }

}
