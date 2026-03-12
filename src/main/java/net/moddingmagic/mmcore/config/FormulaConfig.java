package net.moddingmagic.mmcore.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public class FormulaConfig {
    public static final ModConfigSpec SPEC;

    // Spell Power

    /**
     * Which formula to use when computing the entity spell-power multiplier.
     *
     * LINEAR   : output = attribute_value
     * SOFTCAP  : Iron's default, output = softCap(x)  (asymptote at 2)
     * DIMINISH : output = x / (x + k)  scaled to [0, cap]
     * CUSTOM   : output = a * ln(b * x + 1) + c
     */
    public static final ModConfigSpec.EnumValue<FormulaType> SPELL_POWER_FORMULA;

    /** Only used by DIMINISH — the half-value point (default 1.0). */
    public static final ModConfigSpec.DoubleValue SPELL_POWER_DIMINISH_K;

    /** Only used by DIMINISH — the maximum value the formula can return (default 2.0). */
    public static final ModConfigSpec.DoubleValue SPELL_POWER_DIMINISH_CAP;

    /** Only used by CUSTOM — multiplier on the logarithm (default 1.0). */
    public static final ModConfigSpec.DoubleValue SPELL_POWER_CUSTOM_A;

    /** Only used by CUSTOM — scale inside the logarithm (default 1.0). */
    public static final ModConfigSpec.DoubleValue SPELL_POWER_CUSTOM_B;

    /** Only used by CUSTOM — additive constant (default 1.0). */
    public static final ModConfigSpec.DoubleValue SPELL_POWER_CUSTOM_C;


    // Cooldown Reduction

    /**
     * Which formula to use for effective cooldown.
     * The attribute value is passed in; the result replaces  2 - softCap(attr).
     * A result of 1.0 means no change; 0.5 means half cooldown; 2.0 means double.
     */
    public static final ModConfigSpec.EnumValue<FormulaType> COOLDOWN_FORMULA;
    public static final ModConfigSpec.DoubleValue COOLDOWN_DIMINISH_K;
    public static final ModConfigSpec.DoubleValue COOLDOWN_DIMINISH_CAP;
    public static final ModConfigSpec.DoubleValue COOLDOWN_CUSTOM_A;
    public static final ModConfigSpec.DoubleValue COOLDOWN_CUSTOM_B;
    public static final ModConfigSpec.DoubleValue COOLDOWN_CUSTOM_C;


    // Spell Resist

    /**
     * Which formula to use for the resist multiplier.
     * The combined resist value (school * base) is passed in.
     * The result replaces  2 - softCap(combined).
     * A result < 1.0 means the entity takes less damage; > 1.0 means more.
     */
    public static final ModConfigSpec.EnumValue<FormulaType> RESIST_FORMULA;
    public static final ModConfigSpec.DoubleValue RESIST_DIMINISH_K;
    public static final ModConfigSpec.DoubleValue RESIST_DIMINISH_CAP;
    public static final ModConfigSpec.DoubleValue RESIST_CUSTOM_A;
    public static final ModConfigSpec.DoubleValue RESIST_CUSTOM_B;
    public static final ModConfigSpec.DoubleValue RESIST_CUSTOM_C;


    static {
        ModConfigSpec.Builder b = new ModConfigSpec.Builder();

        // ---- Spell Power ----
        b.comment(
                "Spell Power formula",
                "Controls how the SPELL_POWER attribute translates into a damage multiplier.",
                "The raw attribute value (default baseline = 1.0) is fed into the chosen formula.",
                "Formula types:",
                "  LINEAR   – multiplier = attribute_value  (no cap)",
                "  SOFTCAP  – Iron's default: multiplier = softCap(x), asymptote at 2.0",
                "  DIMINISH – multiplier = cap * x / (x + k)   (smooth diminishing returns)",
                "  CUSTOM   – multiplier = a * ln(b * x + 1) + c"
        ).push("spell_power");

        SPELL_POWER_FORMULA = b.comment("Formula type for Spell Power.")
                .defineEnum("formula", FormulaType.SOFTCAP);

        SPELL_POWER_DIMINISH_K = b.comment("DIMINISH only — attribute value at which you reach half the cap.")
                .defineInRange("diminish_k", 1.0, 0.01, 1000.0);

        SPELL_POWER_DIMINISH_CAP = b.comment("DIMINISH only — maximum multiplier value.")
                .defineInRange("diminish_cap", 2.0, 0.01, 1000.0);

        SPELL_POWER_CUSTOM_A = b.comment("CUSTOM only — coefficient A in  A * ln(B * x + 1) + C.")
                .defineInRange("custom_a", 1.0, -1000.0, 1000.0);

        SPELL_POWER_CUSTOM_B = b.comment("CUSTOM only — coefficient B.")
                .defineInRange("custom_b", 1.0, 0.001, 1000.0);

        SPELL_POWER_CUSTOM_C = b.comment("CUSTOM only — additive constant C.")
                .defineInRange("custom_c", 1.0, -1000.0, 1000.0);

        b.pop();

        // ---- Cooldown Reduction ----
        b.comment(
                "Cooldown Reduction formula",
                "Controls how the COOLDOWN_REDUCTION attribute shortens spell cooldowns.",
                "The result is used as a multiplier on the base cooldown ticks.",
                "A result of 1.0 = no change, 0.5 = half duration, 2.0 = double duration.",
                "ISS default (SOFTCAP): multiplier = 2 - softCap(attr).",
                "For LINEAR/DIMINISH/CUSTOM the output is used directly as the multiplier.",
                "Formula types: LINEAR, SOFTCAP, DIMINISH, CUSTOM"
        ).push("cooldown_reduction");

        COOLDOWN_FORMULA = b.comment("Formula type for Cooldown Reduction.")
                .defineEnum("formula", FormulaType.SOFTCAP);

        COOLDOWN_DIMINISH_K   = b.comment("DIMINISH only — attribute value at which the reduction reaches half-cap.")
                .defineInRange("diminish_k",   1.0, 0.01, 1000.0);

        COOLDOWN_DIMINISH_CAP = b.comment("DIMINISH only — maximum reduction factor (subtracted from 1 to get the multiplier, so cap ≤ 1).")
                .defineInRange("diminish_cap", 1.0, 0.01, 1.0);

        COOLDOWN_CUSTOM_A = b.comment("CUSTOM only — coefficient A.")
                .defineInRange("custom_a", 1.0, -1000.0, 1000.0);

        COOLDOWN_CUSTOM_B = b.comment("CUSTOM only — coefficient B.")
                .defineInRange("custom_b", 1.0, 0.001, 1000.0);

        COOLDOWN_CUSTOM_C = b.comment("CUSTOM only — additive constant C.")
                .defineInRange("custom_c", 1.0, -1000.0, 1000.0);

        b.pop();

        // ---- Spell Resist ----
        b.comment(
                "Spell Resist formula",
                "Controls how the SPELL_RESIST (and school-specific) attribute reduces incoming spell damage.",
                "The combined resist value (schoolResist * baseResist) is fed into the formula.",
                "The output is used directly as the damage multiplier applied to incoming spell damage.",
                "A result < 1.0 means the entity takes less damage; > 1.0 means more (vulnerability).",
                "ISS default (SOFTCAP): multiplier = 2 - softCap(combined).",
                "Formula types: LINEAR, SOFTCAP, DIMINISH, CUSTOM"
        ).push("spell_resist");

        RESIST_FORMULA = b.comment("Formula type for Spell Resist.")
                .defineEnum("formula", FormulaType.SOFTCAP);

        RESIST_DIMINISH_K   = b.comment("DIMINISH only — combined-resist value at which the resistance reaches half-cap.")
                .defineInRange("diminish_k",   1.0, 0.01, 1000.0);

        RESIST_DIMINISH_CAP = b.comment("DIMINISH only — maximum damage reduction (subtracted from 1 to get multiplier, so cap ≤ 1).")
                .defineInRange("diminish_cap", 1.0, 0.01, 1.0);

        RESIST_CUSTOM_A = b.comment("CUSTOM only — coefficient A.")
                .defineInRange("custom_a", 1.0, -1000.0, 1000.0);

        RESIST_CUSTOM_B = b.comment("CUSTOM only — coefficient B.")
                .defineInRange("custom_b", 1.0, 0.001, 1000.0);

        RESIST_CUSTOM_C = b.comment("CUSTOM only — additive constant C.")
                .defineInRange("custom_c", -1000.0, -1000.0, 1000.0);

        b.pop();

        SPEC = b.build();
    }


    // Formula type enum

    public enum FormulaType {
        /**
         * output = x
         * No diminishing returns at all — pure linear scaling.
         */
        LINEAR,

        /**
         * Iron's built-in soft-cap:
         *   x <= 1.5  →  x
         *   x >  1.5  →  -0.25 / (x - 1) + 2
         * Asymptote at 2.
         */
        SOFTCAP,

        /**
         * Smooth diminishing-returns curve:
         *   output = cap * x / (x + k)
         * Approaches `cap` as x → ∞; reaches cap/2 at x = k.
         */
        DIMINISH,

        /**
         * Logarithmic custom curve:
         *   output = a * ln(b * x + 1) + c
         * Tune a, b, c freely in the config.
         */
        CUSTOM
    }
}
