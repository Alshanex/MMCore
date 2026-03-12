package net.moddingmagic.mmcore.event;

import io.redspace.ironsspellbooks.api.events.SpellCooldownAddedEvent;
import io.redspace.ironsspellbooks.api.events.SpellDamageEvent;
import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import io.redspace.ironsspellbooks.api.spells.CastSource;
import io.redspace.ironsspellbooks.api.spells.SchoolType;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.config.ServerConfigs;
import io.redspace.ironsspellbooks.damage.SpellDamageSource;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.moddingmagic.mmcore.formulas.FormulaEvaluator;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

@EventBusSubscriber
public class FormulaEventHandler {

    // Cooldown Reduction

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onCooldownPre(SpellCooldownAddedEvent.Pre event) {
        if(!(event.getEntity() instanceof ServerPlayer player)){
            return;
        }

        // Raw COOLDOWN_REDUCTION attribute value (Iron's baseline = 1.0).
        double rawCdr = player.getAttributeValue(AttributeRegistry.COOLDOWN_REDUCTION);

        // Recompute the multiplier with our configurable formula.
        double multiplier = FormulaEvaluator.cooldownMultiplier(rawCdr);

        // Reapply any item modifier Iron would have used (sword penalty etc.).
        int baseCooldown = event.getSpell().getSpellCooldown();

        // Item/cast-source modifier.
        float itemModifier = getItemModifier(event.getCastSource());

        int newCooldown = (int) (baseCooldown * multiplier * itemModifier);

        // Guard: never set a negative or zero cooldown unintentionally.
        if (newCooldown < 0) newCooldown = 0;

        event.setEffectiveCooldown(newCooldown);
    }


    // Spell Resist

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onSpellDamage(SpellDamageEvent event) {
        LivingEntity target = event.getEntity();
        if (!(event.getSpellDamageSource() instanceof SpellDamageSource spellSource)) return;

        SchoolType school = spellSource.spell().getSchoolType();

        // ── Compute Iron's resist (what it will multiply by after this event) ──
        double baseResist = target.getAttributeValue(AttributeRegistry.SPELL_RESIST);
        double schoolResist = (school != null)
                ? school.getResistanceFor(target)
                : 1.0;
        double combined = schoolResist * baseResist;

        double ironResistMultiplier = 2.0 - Utils.softCapFormula(combined);    // Iron's formula
        double ourResistMultiplier  = FormulaEvaluator.resistMultiplier(combined); // ours

        // ── Adjust amount so net result uses ourResistMultiplier ──
        if (Math.abs(ironResistMultiplier) < 1e-9) return; // safety: avoid divide by zero

        float originalAmount = event.getAmount();
        float adjustedAmount = (float) (originalAmount * (ourResistMultiplier / ironResistMultiplier));
        event.setAmount(adjustedAmount);
    }


    /**
     * Returns the item/cast-source cooldown modifier ISS would apply.
     */
    private static float getItemModifier(CastSource castSource) {
        if (castSource == CastSource.SWORD) {
            return ServerConfigs.SWORDS_CD_MULTIPLIER.get().floatValue();
        }
        return 1.0f;
    }
}
