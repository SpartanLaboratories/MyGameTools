package com.spartanlabs.gaming.gameobjects

/**
 * A participant in the game that owns a roster of [Alive] actors - the units or characters
 * under one person's or one AI's control.
 *
 * This is a game-domain owner and is unrelated to the networking-level "player" tracked by
 * [com.spartanlabs.gaming.networking.GameServer], which only keys connections by name.
 *
 * @param name a label identifying the player; not required to be unique
 */
class Player(val name: String) {

    /** The [Alive] actors this player owns, in the order they were acquired. */
    val ownedAlives: ArrayList<Alive> = ArrayList()

    /** The owned actors that are still alive (see [Alive.isAlive]). */
    val livingAlives: List<Alive> get() = ownedAlives.filter { it.isAlive }

    init {
        log.debug("Created player '{}'", name)
    }

    /**
     * Adds [alive] to this player's roster. A duplicate is ignored, so the same actor is
     * never owned twice.
     *
     * @param alive the actor to take ownership of
     * @return `true` if [alive] was added, `false` if this player already owned it
     */
    fun own(alive: Alive): Boolean {
        if (alive in ownedAlives) {
            log.debug("Player '{}' already owns that Alive; ignoring", name)
            return false
        }
        ownedAlives.add(alive)
        log.debug("Player '{}' now owns {} Alive(s)", name, ownedAlives.size)
        return true
    }

    /**
     * Removes [alive] from this player's roster.
     *
     * @param alive the actor to release
     * @return `true` if [alive] was owned and has been removed, `false` if it was not owned
     */
    fun disown(alive: Alive): Boolean {
        val removed = ownedAlives.remove(alive)
        if (removed) log.debug("Player '{}' released an Alive; {} left", name, ownedAlives.size)
        return removed
    }

    /** `true` when [alive] is on this player's roster. */
    fun owns(alive: Alive): Boolean = alive in ownedAlives
}
