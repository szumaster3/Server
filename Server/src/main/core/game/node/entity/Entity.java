package core.game.node.entity;

import core.game.event.*;
import core.game.interaction.DestinationFlag;
import core.game.interaction.MovementPulse;
import core.game.interaction.ScriptProcessor;
import core.game.node.Node;
import core.game.node.entity.combat.*;
import core.game.node.entity.combat.equipment.ArmourSet;
import core.game.node.entity.impl.Properties;
import core.game.node.entity.impl.*;
import core.game.node.entity.lock.ActionLocks;
import core.game.node.entity.npc.NPC;
import core.game.node.entity.player.Player;
import core.game.node.entity.player.link.TeleportManager;
import core.game.node.entity.skill.Skills;
import core.game.system.task.Pulse;
import core.game.system.timer.TimerManager;
import core.game.system.timer.TimerRegistry;
import core.game.world.GameWorld;
import core.game.world.map.Location;
import core.game.world.map.Viewport;
import core.game.world.map.path.Path;
import core.game.world.map.path.Pathfinder;
import core.game.world.map.zone.ZoneMonitor;
import core.game.world.map.zone.ZoneRestriction;
import core.game.world.update.UpdateMasks;
import core.game.world.update.flag.EFlagType;
import core.game.world.update.flag.EntityFlag;
import core.game.world.update.flag.EntityFlags;
import core.game.world.update.flag.context.Animation;
import core.game.world.update.flag.context.Graphics;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * An entity is a movable node, such as players and NPCs.
 *
 * @author Emperor
 */
public abstract class Entity extends Node {

    /**
     * The entity's properties.
     */
    private final Properties properties = new Properties(this);

    /**
     * The entity's update masks.
     */
    private final UpdateMasks updateMasks = new UpdateMasks(this);

    /**
     * The walking queue.
     */
    private final WalkingQueue walkingQueue = new WalkingQueue(this);

    /**
     * The entity's skills.
     */
    public Skills skills = new Skills(this);

    /**
     * The entity's extension classes.
     */
    private final Map<Class<?>, Object> extensions = new HashMap<Class<?>, Object>();

    /**
     * The entity's attributes.
     */
    private final GameAttributes attributes = new GameAttributes();

    /**
     * The entity's viewport.
     */
    private final Viewport viewport = new Viewport();

    /**
     * The pulse manager.
     */
    private final PulseManager pulseManager = new PulseManager(this);

    /**
     * The impact handler.
     */
    private final ImpactHandler impactHandler = new ImpactHandler(this);

    /**
     * The animator.
     */
    private final Animator animator = new Animator(this);

    /**
     * The teleporter.
     */
    private final TeleportManager teleporter = new TeleportManager(this);

    /**
     * The zone monitor.
     */
    private final ZoneMonitor zoneMonitor = new ZoneMonitor(this);


    private final ActionLocks locks = new ActionLocks();

    /**
     * The Scripts.
     */
    public ScriptProcessor scripts = new ScriptProcessor(this);

    /**
     * The Clocks.
     */
    public final int[] clocks = new int[10];

    /**
     * The Current movement.
     */
    public MovementPulse currentMovement;

    private HashMap<Class<?>, ArrayList<EventHook>> hooks = new HashMap<>();

    /**
     * The Timers.
     */
    public TimerManager timers = new TimerManager(this);

    private boolean invisible;

    /**
     * Instantiates a new Entity.
     *
     * @param name     the name
     * @param location the location
     */
    public Entity(String name, Location location) {
        super(name, location);
        super.destinationFlag = DestinationFlag.ENTITY;
    }

    /**
     * Move step.
     */
    public void moveStep() {
        if (locks.isMovementLocked()) {
            return;
        }
        Path path;
        if (!(path = Pathfinder.find(this, getLocation().transform(-1, 0, 0), false, Pathfinder.DUMB)).isSuccessful()) {
            if (!(path = Pathfinder.find(this, getLocation().transform(0, -1, 0), false, Pathfinder.DUMB)).isSuccessful()) {
                if (!(path = Pathfinder.find(this, getLocation().transform(1, 0, 0), false, Pathfinder.DUMB)).isSuccessful()) {
                    if (!(path = Pathfinder.find(this, getLocation().transform(0, 1, 0), false, Pathfinder.DUMB)).isSuccessful()) {
                        path = null;
                    }
                }
            }
        }
        if (path != null) {
            path.walk(this);
        }
    }

    /**
     * Dispatch.
     *
     * @param event the event
     */
    public void dispatch(Event event) {
        if (this.hooks.containsKey(event.getClass())) {
            ArrayList<EventHook> processList = new ArrayList(this.hooks.get(event.getClass()));
            processList.forEach((hook) -> {
                hook.process(this, event);
            });
        }
    }

    /**
     * Unhook.
     *
     * @param hook the hook
     */
    public void unhook(EventHook hook) {
        for (ArrayList<EventHook> s : hooks.values()) s.remove(hook);
    }

    /**
     * Hook.
     *
     * @param event the event
     * @param hook  the hook
     */
    public void hook(Event event, EventHook hook) {
        hook(event.getClass(), hook);
    }

    /**
     * Hook.
     *
     * @param event the event
     * @param hook  the hook
     */
    public void hook(Class<?> event, EventHook hook) {
        ArrayList<EventHook> hookList;
        if (hooks.get(event) != null) {
            hookList = hooks.get(event);
        } else {
            hookList = new ArrayList<EventHook>();
        }
        if (!hookList.contains(hook)) hookList.add(hook);
        hooks.put(event, hookList);
    }

    /**
     * Init.
     */
    public void init() {
        active = true;
        TimerRegistry.addAutoTimers(this);
    }

    /**
     * Tick.
     */
    public void tick() {
        scripts.preMovement();
        dispatch(new TickEvent(GameWorld.getTicks()));
        skills.pulse();
        Location old = location != null ? location.transform(0, 0, 0) : Location.create(0, 0, 0);
        walkingQueue.update();
        scripts.postMovement(!Objects.equals(location, old));
        timers.processTimers();
        updateMasks.prepare(this);
    }

    /**
     * Update.
     */
    public void update() {
    }

    /**
     * Reset.
     */
    public void reset() {
        updateMasks.reset();
        properties.setTeleporting(false);
    }

    /**
     * Clear.
     */
    public void clear() {
        active = false;
        viewport.remove(this);
        pulseManager.clear();
    }

    /**
     * In combat boolean.
     *
     * @return the boolean
     */
    public boolean inCombat() {
        return getAttribute("combat-time", 0L) > System.currentTimeMillis();
    }

    /**
     * Full restore.
     */
    public void fullRestore() {
        skills.restore();
        timers.removeTimer("poison");
        timers.removeTimer("poison:immunity");
        timers.removeTimer("disease");
    }

    /**
     * Commence death.
     *
     * @param killer the killer
     */
    public void commenceDeath(Entity killer) {
    }

    /**
     * Finalize death.
     *
     * @param killer the killer
     */
    public void finalizeDeath(Entity killer) {
        skills.restore();
        skills.rechargePrayerPoints();
        impactHandler.getImpactQueue().clear();
        impactHandler.setDisabledTicks(10);
        timers.onEntityDeath();
        removeAttribute("combat-time");
        face(null);
        //Check if it's a Loar shade and transform back into the shadow version.
        if (this.getId() == 1240 || this.getId() == 1241) {
            this.asNpc().transform(1240);
        }
    }

    /**
     * Update location.
     *
     * @param last the last
     */
    public void updateLocation(Location last) {

    }

    /**
     * Is ignore multi boundaries boolean.
     *
     * @param victim the victim
     * @return the boolean
     */
    public boolean isIgnoreMultiBoundaries(Entity victim) {
        if (this instanceof NPC) {
            return ((NPC) this).behavior.shouldIgnoreMultiRestrictions((NPC) this, victim);
        }
        return false;
    }

    /**
     * Should prevent stacking boolean.
     *
     * @param mover the mover
     * @return the boolean
     */
    public boolean shouldPreventStacking(Entity mover) {
        return false;
    }

    /**
     * Check impact.
     *
     * @param state the state
     */
    public void checkImpact(BattleState state) {
        getProperties().getCombatPulse().setLastReceivedAttack(GameWorld.getTicks());
        int ticks = GameWorld.getTicks() - getProperties().getCombatPulse().getLastSentAttack();
        if (ticks > 10 && this instanceof NPC && ((NPC) this).getDefinition().getConfiguration("safespot", false)) {
            Pathfinder.find(state.getAttacker(), getLocation()).walk(state.getAttacker());
            Pathfinder.find(state.getVictim(), state.getAttacker().getLocation()).walk(state.getVictim());
            if (ticks > 40) {
                state.getAttacker().moveStep();
                state.getAttacker().getProperties().getCombatPulse().stop();
                state.getVictim().moveStep();
                state.getVictim().getProperties().getCombatPulse().stop();
            }
        }
    }

    /**
     * On impact.
     *
     * @param entity the entity
     * @param state  the state
     */
    public void onImpact(final Entity entity, BattleState state) {
        if (DeathTask.isDead(this)) state.neutralizeHits();
        if (this instanceof NPC) {
            ((NPC) this).behavior.afterDamageReceived((NPC) this, entity, state);
        }
        if (properties.isRetaliating() && !properties.getCombatPulse().isAttacking() && !getLocks().isInteractionLocked() && properties.getCombatPulse().getNextAttack() < GameWorld.getTicks()) {
            if (!getWalkingQueue().hasPath() && !getPulseManager().isMovingPulse() || (this instanceof NPC)) {
                properties.getCombatPulse().attack(entity);
            }
        }
    }

    /**
     * On attack.
     *
     * @param target the target
     */
    public void onAttack(final Entity target) {

    }

    /**
     * Attack.
     *
     * @param node the node
     */
    public void attack(final Node node) {
        getProperties().getCombatPulse().attack(node);
    }

    /**
     * Can move boolean.
     *
     * @param destination the destination
     * @return the boolean
     */
    public boolean canMove(Location destination) {
        return true;
    }

    /**
     * Teleport.
     *
     * @param location the location
     */
    public void teleport(Location location) {
        getProperties().setTeleportLocation(location);
    }

    /**
     * Teleport.
     *
     * @param location the location
     * @param ticks    the ticks
     */
    public void teleport(final Location location, int ticks) {
        GameWorld.getPulser().submit(new Pulse(ticks, this) {
            @Override
            public boolean pulse() {
                teleport(location);
                return true;
            }
        });
    }

    /**
     * Lock.
     */
    public void lock() {
        locks.lock();
    }

    /**
     * Lock.
     *
     * @param time the time
     */
    public void lock(int time) {
        locks.lock(time);
    }

    /**
     * Unlock.
     */
    public void unlock() {
        locks.unlock();
    }

    /**
     * Has protection prayer boolean.
     *
     * @param style the style
     * @return the boolean
     */
    public abstract boolean hasProtectionPrayer(CombatStyle style);

    /**
     * Gets dragonfire protection.
     *
     * @param fire the fire
     * @return the dragonfire protection
     */
    public abstract int getDragonfireProtection(boolean fire);

    /**
     * Is attackable boolean.
     *
     * @param entity  the entity
     * @param style   the style
     * @param message the message
     * @return the boolean
     */
    public boolean isAttackable(Entity entity, CombatStyle style, boolean message) {
        if (DeathTask.isDead(this)) {
            return false;
        }
        if (!entity.getZoneMonitor().continueAttack(this, style, message)) {
            return false;
        }
        return true;
    }

    /**
     * Graphics boolean.
     *
     * @param graphics the graphics
     * @return the boolean
     */
    public boolean graphics(Graphics graphics) {
        return animator.graphics(graphics);
    }

    /**
     * Animate boolean.
     *
     * @param animation the animation
     * @return the boolean
     */
    public boolean animate(Animation animation) {
        return animator.animate(animation);
    }

    /**
     * Graphics boolean.
     *
     * @param graphics the graphics
     * @param delay    the delay
     * @return the boolean
     */
    public boolean graphics(final Graphics graphics, int delay) {
        GameWorld.getPulser().submit(new Pulse(delay, this) {
            @Override
            public boolean pulse() {
                graphics(graphics);
                return true;
            }
        });
        return true;
    }

    /**
     * Continue attack boolean.
     *
     * @param target  the target
     * @param style   the style
     * @param message the message
     * @return the boolean
     */
    public boolean continueAttack(Entity target, CombatStyle style, boolean message) {
        return true;
    }

    /**
     * Animate boolean.
     *
     * @param animation the animation
     * @param delay     the delay
     * @return the boolean
     */
    public boolean animate(final Animation animation, int delay) {
        GameWorld.getPulser().submit(new Pulse(delay, this) {
            @Override
            public boolean pulse() {
                animate(animation);
                return true;
            }
        });
        return true;
    }

    /**
     * Send impact.
     *
     * @param state the state
     */
    public void sendImpact(BattleState state) {
        getProperties().getCombatPulse().setLastSentAttack(GameWorld.getTicks());
    }

    /**
     * Can select target boolean.
     *
     * @param target the target
     * @return the boolean
     */
    public boolean canSelectTarget(Entity target) {
        return true;
    }

    /**
     * Visualize boolean.
     *
     * @param animation the animation
     * @param graphics  the graphics
     * @return the boolean
     */
    public boolean visualize(Animation animation, Graphics graphics) {
        return animator.animate(animation, graphics);
    }

    /**
     * Face temporary boolean.
     *
     * @param entity the entity
     * @param ticks  the ticks
     * @return the boolean
     */
    public boolean faceTemporary(Entity entity, int ticks) {
        return faceTemporary(entity, null, ticks);
    }

    /**
     * Face temporary boolean.
     *
     * @param entity the entity
     * @param reset  the reset
     * @param ticks  the ticks
     * @return the boolean
     */
    public boolean faceTemporary(Entity entity, final Entity reset, int ticks) {
        if (face(entity)) {
            GameWorld.getPulser().submit(new Pulse(ticks + 1, this) {
                @Override
                public boolean pulse() {
                    face(reset);
                    return true;
                }
            });
            return true;
        }
        return false;
    }

    /**
     * Gets formatted hit.
     *
     * @param state the state
     * @param hit   the hit
     * @return the formatted hit
     */
    public double getFormattedHit(BattleState state, int hit) {
        if (state.getAttacker() == null || state.getVictim() == null || state.getStyle() == null) {
            return hit;
        }
        Entity entity = state.getAttacker();
        Entity victim = state.getVictim();
        CombatStyle type = state.getStyle();
        if (state.getArmourEffect() != ArmourSet.VERAC && !entity.isIgnoreProtection(type) && victim.hasProtectionPrayer(type)) {
            return hit *= (entity instanceof Player && victim instanceof Player) ? 0.6 : 0;
        }
        return hit;
    }

    /**
     * Is ignore protection boolean.
     *
     * @param style the style
     * @return the boolean
     */
    public boolean isIgnoreProtection(CombatStyle style) {
        return false;
    }

    /**
     * Start death.
     *
     * @param killer the killer
     */
    public void startDeath(Entity killer) {
        if (zoneMonitor.startDeath(this, killer)) {
            DeathTask.startDeath(this, killer);
        }
    }

    public Player asPlayer() {
        return (Player) this;
    }

    /**
     * Is player boolean.
     *
     * @return the boolean
     */
    public boolean isPlayer() {
        return this instanceof Player;
    }

    /**
     * Face boolean.
     *
     * @param entity the entity
     * @return the boolean
     */
    public boolean face(Entity entity) {
        if (entity == null) {
            int ordinal = EntityFlags.getOrdinal(EFlagType.of(this), EntityFlag.FaceEntity);
            if (getUpdateMasks().unregisterSynced(ordinal)) {
                return getUpdateMasks().register(EntityFlag.FaceEntity, null);
            }
            return true;
        }
        return getUpdateMasks().register(EntityFlag.FaceEntity, entity, true);
    }

    /**
     * Face location boolean.
     *
     * @param location the location
     * @return the boolean
     */
    public boolean faceLocation(Location location) {
        if (location == null) {
            int ordinal = EntityFlags.getOrdinal(EFlagType.of(this), EntityFlag.FaceLocation);
            getUpdateMasks().unregisterSynced(ordinal);
            return true;
        }
        return getUpdateMasks().register(EntityFlag.FaceLocation, location, true);
    }

    /**
     * Send chat boolean.
     *
     * @param string the string
     * @return the boolean
     */
    public boolean sendChat(String string) {
        return getUpdateMasks().register(EntityFlag.ForceChat, string);
    }

    /**
     * Gets swing handler.
     *
     * @param swing the swing
     * @return the swing handler
     */
    public abstract CombatSwingHandler getSwingHandler(boolean swing);

    /**
     * Is poison immune boolean.
     *
     * @return the boolean
     */
    public abstract boolean isPoisonImmune();

    /**
     * Send chat.
     *
     * @param string the string
     * @param ticks  the ticks
     */
    public void sendChat(final String string, int ticks) {
        GameWorld.getPulser().submit(new Pulse(ticks, this) {
            @Override
            public boolean pulse() {
                sendChat(string);
                return true;
            }
        });
    }

    /**
     * Gets level mod.
     *
     * @param entity the entity
     * @param victim the victim
     * @return the level mod
     */
    public double getLevelMod(Entity entity, Entity victim) {
        return 0;
    }

    /**
     * Gets locks.
     *
     * @return the locks
     */
    public ActionLocks getLocks() {
        return locks;
    }

    /**
     * Gets client index.
     *
     * @return the client index
     */
    public int getClientIndex() {
        return index;
    }

    /**
     * Gets properties.
     *
     * @return the properties
     */
    public Properties getProperties() {
        return properties;
    }

    /**
     * Gets update masks.
     *
     * @return the update masks
     */
    public UpdateMasks getUpdateMasks() {
        return updateMasks;
    }

    /**
     * Add extension.
     *
     * @param clazz  the clazz
     * @param object the object
     */
    public void addExtension(Class<?> clazz, Object object) {
        extensions.put(clazz, object);
    }

    /**
     * Gets extension.
     *
     * @param <T>   the type parameter
     * @param clazz the clazz
     * @return the extension
     */
    @SuppressWarnings("unchecked")
    public <T> T getExtension(Class<?> clazz) {
        return (T) extensions.get(clazz);
    }

    /**
     * Gets extension.
     *
     * @param <T>   the type parameter
     * @param clazz the clazz
     * @param fail  the fail
     * @return the extension
     */
    @SuppressWarnings("unchecked")
    public <T> T getExtension(Class<?> clazz, T fail) {
        T extension = (T) extensions.get(clazz);
        if (extension == null) {
            return fail;
        }
        return extension;
    }

    /**
     * Remove extension.
     *
     * @param clazz the clazz
     */
    public void removeExtension(Class<?> clazz) {
        extensions.remove(clazz);
    }

    /**
     * Gets attributes.
     *
     * @return the attributes
     */
    public Map<String, Object> getAttributes() {
        return attributes.getAttributes();
    }

    /**
     * Clear attributes.
     */
    public void clearAttributes() {
        this.attributes.getAttributes().clear();
        this.attributes.getSavedAttributes().clear();
    }

    /**
     * Sets attribute.
     *
     * @param key   the key
     * @param value the value
     */
    public void setAttribute(String key, Object value) {
        attributes.setAttribute(key, value);
        dispatch(new AttributeSetEvent(this, key, value));
    }

    /**
     * Gets attribute.
     *
     * @param <T> the type parameter
     * @param key the key
     * @return the attribute
     */
    public <T> T getAttribute(String key) {
        return attributes.getAttribute(key);
    }

    /**
     * Increment attribute.
     *
     * @param key the key
     */
    public void incrementAttribute(String key) {
        incrementAttribute(key, 1);
    }

    /**
     * Increment attribute.
     *
     * @param key    the key
     * @param amount the amount
     */
    public void incrementAttribute(String key, int amount) {
        attributes.setAttribute(key, attributes.getAttribute(key, 0) + amount);
    }

    /**
     * Gets attribute.
     *
     * @param <T>    the type parameter
     * @param string the string
     * @param fail   the fail
     * @return the attribute
     */
    public <T> T getAttribute(String string, T fail) {
        return attributes.getAttribute(string, fail);
    }

    /**
     * Remove attribute.
     *
     * @param string the string
     */
    public void removeAttribute(String string) {
        attributes.removeAttribute(string);
        dispatch(new AttributeRemoveEvent(this, string));
    }

    /**
     * Gets game attributes.
     *
     * @return the game attributes
     */
    public GameAttributes getGameAttributes() {
        return attributes;
    }

    /**
     * Gets walking queue.
     *
     * @return the walking queue
     */
    public WalkingQueue getWalkingQueue() {
        return walkingQueue;
    }

    /**
     * Gets viewport.
     *
     * @return the viewport
     */
    public Viewport getViewport() {
        return viewport;
    }

    /**
     * Gets skills.
     *
     * @return the skills
     */
    public Skills getSkills() {
        return skills;
    }

    /**
     * Gets pulse manager.
     *
     * @return the pulse manager
     */
    public PulseManager getPulseManager() {
        return pulseManager;
    }

    /**
     * Gets impact handler.
     *
     * @return the impact handler
     */
    public ImpactHandler getImpactHandler() {
        return impactHandler;
    }

    /**
     * Gets animator.
     *
     * @return the animator
     */
    public Animator getAnimator() {
        return animator;
    }

    /**
     * Gets teleporter.
     *
     * @return the teleporter
     */
    public TeleportManager getTeleporter() {
        return teleporter;
    }

    /**
     * Gets zone monitor.
     *
     * @return the zone monitor
     */
    public ZoneMonitor getZoneMonitor() {
        return zoneMonitor;
    }

    /**
     * Has fire resistance boolean.
     *
     * @return the boolean
     */
    public boolean hasFireResistance() {
        return getAttribute("fire:immune", 0) >= GameWorld.getTicks();
    }

    /**
     * Is tele blocked boolean.
     *
     * @return the boolean
     */
    public boolean isTeleBlocked() {
        return timers.getTimer("teleblock") != null || getLocks().isTeleportLocked() || getZoneMonitor().isRestricted(ZoneRestriction.TELEPORT);
    }

    /**
     * Is invisible boolean.
     *
     * @return the boolean
     */
    public boolean isInvisible() {
        return invisible;
    }

    /**
     * Sets invisible.
     *
     * @param invisible the invisible
     */
    public void setInvisible(boolean invisible) {
        this.invisible = invisible;
    }

    /**
     * Gets closest occupied tile.
     *
     * @param other the other
     * @return the closest occupied tile
     */
    public Location getClosestOccupiedTile(@NotNull Location other) {
        List<Location> occupied = getOccupiedTiles();

        Location closest = location;
        if (occupied.size() > 1) {
            double lowest = 9999;
            for (Location tile : occupied) {
                double dist = tile.getDistance(other);
                if (dist < lowest) {
                    lowest = dist;
                    closest = tile;
                }
            }
        }

        return closest;
    }

    /**
     * Gets occupied tiles.
     *
     * @return the occupied tiles
     */
    public List<Location> getOccupiedTiles() {
        ArrayList<Location> occupied = new ArrayList<>();

        Location northEast = location.transform(size, size, 0);

        for (int x = location.getX(); x < northEast.getX(); x++) {
            for (int y = location.getY(); y < northEast.getY(); y++) {
                occupied.add(Location.create(x, y, location.getZ()));
            }
        }
        return occupied;
    }

    /**
     * Delayed boolean.
     *
     * @return the boolean
     */
    public boolean delayed() {
        return scripts.getDelay() > GameWorld.getTicks();
    }

    /**
     * Is teleporting boolean.
     *
     * @return the boolean
     */
    public boolean isTeleporting() {
        return getAttribute("tele-pulse", null) != null || properties.getTeleportLocation() != null;
    }
}
