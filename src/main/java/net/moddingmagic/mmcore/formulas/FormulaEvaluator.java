package net.moddingmagic.mmcore.formulas;

import io.redspace.ironsspellbooks.api.util.Utils;
import net.moddingmagic.mmcore.config.FormulaConfig;

public final class FormulaEvaluator {

    private FormulaEvaluator() {}

    /**
     * Returns the spell-power multiplier for the given raw SPELL_POWER attribute value.
     *
     * Iron's default equivalent: {@code softCap(x)}  (result ≈ x, asymptote 2).
     *
     * @param attributeValue  raw value of the SPELL_POWER attribute (baseline 1.0)
     * @return multiplier applied to (baseSpellPower + perLevel * (level-1))
     */
    public static double spellPowerMultiplier(double attributeValue) {
        return evaluate(
                attributeValue,
                FormulaConfig.SPELL_POWER_FORMULA.get(),
                FormulaConfig.SPELL_POWER_DIMINISH_K.get(),
                FormulaConfig.SPELL_POWER_DIMINISH_CAP.get(),
                FormulaConfig.SPELL_POWER_CUSTOM_A.get(),
                FormulaConfig.SPELL_POWER_CUSTOM_B.get(),
                FormulaConfig.SPELL_POWER_CUSTOM_C.get()
        );
    }

    /**
     * Returns the cooldown multiplier for the given raw COOLDOWN_REDUCTION attribute value.
     *
     * A value < 1.0 shortens cooldowns; > 1.0 lengthens them.
     * Iron's default equivalent: {@code 2 - softCap(x)}.
     *
     * @param attributeValue  raw value of the COOLDOWN_REDUCTION attribute (baseline 1.0)
     * @return multiplier applied to the base spell cooldown ticks
     */
    public static double cooldownMultiplier(double attributeValue) {
        FormulaConfig.FormulaType type = FormulaConfig.COOLDOWN_FORMULA.get();

        // SOFTCAP keeps Iron's own inversion (2 - softCap).
        if (type == FormulaConfig.FormulaType.SOFTCAP) {
            return 2.0 - Utils.softCapFormula(attributeValue);
        }

        // LINEAR, DIMINISH, CUSTOM: compute a raw value then turn it into a multiplier.
        return evaluate(
                attributeValue,
                type,
                FormulaConfig.COOLDOWN_DIMINISH_K.get(),
                FormulaConfig.COOLDOWN_DIMINISH_CAP.get(),
                FormulaConfig.COOLDOWN_CUSTOM_A.get(),
                FormulaConfig.COOLDOWN_CUSTOM_B.get(),
                FormulaConfig.COOLDOWN_CUSTOM_C.get()
        );
    }

    /**
     * Returns the spell-resist damage multiplier for the given combined resist value.
     *
     * A value < 1.0 means the entity takes less damage (resistance).
     * A value > 1.0 means more damage (vulnerability).
     * Iron's default equivalent: {@code 2 - softCap(combinedResist)}.
     *
     * @param combinedResist  schoolResist * baseResist  (baseline 1.0 = no resistance)
     * @return damage multiplier applied to incoming spell damage
     */
    public static double resistMultiplier(double combinedResist) {
        FormulaConfig.FormulaType type = FormulaConfig.RESIST_FORMULA.get();

        if (type == FormulaConfig.FormulaType.SOFTCAP) {
            return 2.0 - Utils.softCapFormula(combinedResist);
        }

        return evaluate(
                combinedResist,
                type,
                FormulaConfig.RESIST_DIMINISH_K.get(),
                FormulaConfig.RESIST_DIMINISH_CAP.get(),
                FormulaConfig.RESIST_CUSTOM_A.get(),
                FormulaConfig.RESIST_CUSTOM_B.get(),
                FormulaConfig.RESIST_CUSTOM_C.get()
        );
    }


    /**
     * Generic formula dispatcher.
     * For SOFTCAP the caller is responsible for applying the 2 - f(x) inversion if semantically needed (resist / cooldown).  Spell power just uses f(x) directly.
     */
    private static double evaluate(
            double x,
            FormulaConfig.FormulaType type,
            double diminishK,
            double diminishCap,
            double customA,
            double customB,
            double customC
    ) {
        return switch (type) {
            case LINEAR   -> x;
            case SOFTCAP  -> Utils.softCapFormula(x);
            case DIMINISH -> diminishReturns(x, diminishK, diminishCap);
            case CUSTOM   -> customLog(x, customA, customB, customC);
        };
    }

    /**
     * Smooth diminishing-returns hyperbolic curve.
     * <pre>
     *   output = cap * x / (x + k)
     * </pre>
     * Reaches cap/2 at x = k; approaches cap asymptotically.
     */
    private static double diminishReturns(double x, double k, double cap) {
        if (x <= 0.0) return 0.0;
        return cap * x / (x + k);
    }

    /**
     * Logarithmic custom curve.
     * <pre>
     *   output = a * ln(b * x + 1) + c
     * </pre>
     * At x=0: output = c (use c=1 to match baseline of 1 with no resist/power/etc).
     */
    private static double customLog(double x, double a, double b, double c) {
        double inner = b * x + 1.0;
        if (inner <= 0.0) inner = 1e-9; // guard against log(0)
        return a * Math.log(inner) + c;
    }
}
