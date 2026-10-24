package com.whistles;

/**
 * Central place to tune The Frequency Whistle. All durations are in game ticks (20 ticks = 1 second).
 */
public final class WhistleConfig {
    /** The whistle is forced to be used for exactly this long: 5 s. */
    public static final int CHANNEL_TICKS = 100;

    /** A shockwave is emitted every second while channeling (at 1 s, 2 s, 3 s and 4 s). */
    public static final int PULSE_INTERVAL_TICKS = 20;

    /** Radius (blocks) in which entities receive Stasis. */
    public static final double RADIUS = 8.0;

    /** Stasis added to every entity in range by each shockwave: 3 s. */
    public static final int STASIS_PER_PULSE_TICKS = 60;

    /** Cooldown applied when the channel ends: 42.5 s. */
    public static final int COOLDOWN_TICKS = 850;

    /** If true, the player blowing the whistle also gets Stasis from their own shockwaves. */
    public static final boolean AFFECTS_CASTER = false;

    /** Players within this distance (blocks) of the whistle see the shockwaves / camera effect. */
    public static final double VISUAL_RANGE = 48.0;

    private WhistleConfig() {}
}
