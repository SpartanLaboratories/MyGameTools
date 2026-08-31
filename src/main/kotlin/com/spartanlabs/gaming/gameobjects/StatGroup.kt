package com.spartanlabs.gaming.gameobjects

/**
 * A single numeric stat - health, mana, stamina - tracked as a current [value] against a
 * normal ceiling [maxValue] and a hard ceiling [cap].
 *
 * [maxValue] is what a full bar shows; [cap] is the absolute maximum a temporary buff may
 * push [value] to. [value] itself is free to sit anywhere, including at or below zero, so
 * callers can test for depletion.
 *
 * @property value the current amount
 * @property maxValue the normal ceiling; must be positive
 * @property cap the hard ceiling; must be positive
 */
data class StatGroup(
    var value: Double,
    var maxValue: Double,
    var cap: Double
) {

    init {
        require(maxValue > 0) { "maxValue must be positive but was $maxValue" }
        require(cap > 0) { "cap must be positive but was $cap" }
    }

    /** [value] as a fraction of [cap]; `1.0` at the hard ceiling. May exceed `1.0`. */
    val fractionOfCap get() = value / cap

    /** [value] as a fraction of [maxValue]; `1.0` when a normal bar is full. May exceed `1.0`. */
    val fractionOfMax get() = value / maxValue

    /** `true` once [value] has reached the hard [cap]. */
    val isCapped get() = value >= cap

    /** `true` once [value] has reached the normal [maxValue]. */
    val isMaxed get() = value >= maxValue

    /**
     * Flat adjustments to this stat, keyed by a caller-chosen source name so each can be
     * added or removed independently. Values may be negative.
     */
    val additiveMods: HashMap<String, Double> = hashMapOf()

    /** The sum of every entry in [additiveMods]; `0.0` when there are none. */
    val totalAdditiveMod get() = additiveMods.values.sum()

    /**
     * Scaling adjustments to this stat, keyed by a caller-chosen source name so each can be
     * added or removed independently. Each value is a factor, e.g. `1.1` for +10%.
     */
    val multiplicativeMods: HashMap<String, Double> = hashMapOf()

    /** The product of every entry in [multiplicativeMods]; `1.0` when there are none. */
    val totalMultiplicativeMod get() = multiplicativeMods.values.fold(1.0) { acc, mod -> acc * mod }
}
