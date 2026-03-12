package net.moddingmagic.mmcore.buffstacking;

import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.moddingmagic.mmcore.config.BuffStackingConfig;
import net.moddingmagic.mmcore.MMCore;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public record SpellStackingRule(
        List<MobEffect> activeEffects,
        List<AbstractSpell> blockedSpells,
        boolean replace
) {
    public static List<SpellStackingRule> resolveAll() {
        List<SpellStackingRule> resolved = new ArrayList<>();
        for (BuffStackingConfig.SpellRawRule raw : BuffStackingConfig.getSpellRules()) {
            resolve(raw).ifPresent(resolved::add);
        }
        MMCore.LOGGER.debug("[BuffStacking] Resolved {} spell rule(s).", resolved.size());
        return resolved;
    }

    private static Optional<SpellStackingRule> resolve(BuffStackingConfig.SpellRawRule raw) {
        List<MobEffect> active  = BuffStackingRule.resolveEffects(raw.activeEffects(), "active_effects");
        List<AbstractSpell> spells = resolveSpells(raw.blockedSpells());

        if (active.isEmpty() || spells.isEmpty()) {
            MMCore.LOGGER.warn("[BuffStacking] Skipping spell rule — no valid entries resolved. Raw: {}", raw);
            return Optional.empty();
        }
        return Optional.of(new SpellStackingRule(active, spells, raw.replace()));
    }

    private static List<AbstractSpell> resolveSpells(List<String> ids) {
        List<AbstractSpell> spells = new ArrayList<>();
        for (String id : ids) {
            ResourceLocation rl = ResourceLocation.tryParse(id.trim());
            if (rl == null) {
                MMCore.LOGGER.warn("[BuffStacking] '{}' in blocked_spells is not a valid ResourceLocation.", id);
                continue;
            }
            AbstractSpell spell = SpellRegistry.getSpell(rl);
            if (spell == null || spell == SpellRegistry.none()) {
                MMCore.LOGGER.warn("[BuffStacking] Unknown spell '{}' in blocked_spells — is Iron Spells loaded and is the spell ID correct?", id);
            } else {
                spells.add(spell);
            }
        }
        return spells;
    }

    /**
     * Returns true if this rule applies: the caster has any of the listed active effects AND the spell being cast is one of the blocked spells.
     */
    public boolean matches(MobEffect activeEffect, AbstractSpell castingSpell) {
        return activeEffects.contains(activeEffect) && blockedSpells.contains(castingSpell);
    }
}
