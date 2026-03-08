package net.moddingmagic.mmcore;

import com.electronwill.nightconfig.core.Config;
import net.neoforged.neoforge.common.ModConfigSpec;
import java.util.List;

public class BuffStackingConfig {

    public static final ModConfigSpec SPEC;

    // Effect-vs-effect rules

    private static final ModConfigSpec.ConfigValue<List<? extends Config>> EFFECT_RULES;

    // Spell-vs-effect rules

    private static final ModConfigSpec.ConfigValue<List<? extends Config>> SPELL_RULES;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        // ------ Effect rules ------
        builder.comment(
                "Effect Stacking Prevention Rules",
                "",
                "Each [[effect_rules.entries]] entry defines a conflict between mob effects.",
                "  active_effects   = [\"namespace:effect\", ...]  -- effects already on the entity",
                "  incoming_effects = [\"namespace:effect\", ...]  -- effects about to be applied",
                "  replace          = true  -> remove the conflicting active effect, apply the new one",
                "                     false -> cancel the incoming effect entirely"
        ).push("effect_rules");

        EFFECT_RULES = builder.defineList(
                "entries",
                List.of(
                        makeConfig(cfg -> {
                            cfg.set("active_effects", List.of("minecraft:speed", "minecraft:haste"));
                            cfg.set("incoming_effects", List.of("minecraft:speed"));
                            cfg.set("replace", false);
                        }),
                        makeConfig(cfg -> {
                            cfg.set("active_effects", List.of("minecraft:regeneration"));
                            cfg.set("incoming_effects", List.of("minecraft:poison"));
                            cfg.set("replace", true);
                        })
                ),
                entry -> entry instanceof Config cfg
                        && cfg.contains("active_effects")
                        && cfg.contains("incoming_effects")
                        && cfg.contains("replace")
        );

        builder.pop();

        // ------ Spell rules ------
        builder.comment(
                "Spell Cast Prevention Rules",
                "",
                "Each [[spell_rules.entries]] entry cancels or allows a spell cast",
                "based on which mob effects the caster currently has.",
                "  active_effects = [\"namespace:effect\", ...]  -- effects to check on the caster",
                "  blocked_spells = [\"namespace:spell\", ...]   -- spells that are affected",
                "  replace        = true  -> remove the matching active effect, let the cast continue",
                "                   false -> cancel the cast entirely"
        ).push("spell_rules");

        SPELL_RULES = builder.defineList(
                "entries",
                List.of(
                        makeConfig(cfg -> {
                            cfg.set("active_effects", List.of("minecraft:resistance"));
                            cfg.set("blocked_spells", List.of("irons_spellbooks:fortify"));
                            cfg.set("replace", false);
                        }),
                        makeConfig(cfg -> {
                            cfg.set("active_effects", List.of("irons_spellbooks:oakskin"));
                            cfg.set("blocked_spells", List.of("irons_spellbooks:fortify"));
                            cfg.set("replace", true);
                        })
                ),
                entry -> entry instanceof Config cfg
                        && cfg.contains("active_effects")
                        && cfg.contains("blocked_spells")
                        && cfg.contains("replace")
        );

        builder.pop();
        SPEC = builder.build();
    }

    // Public accessors

    public static List<EffectRawRule> getEffectRules() {
        return EFFECT_RULES.get().stream()
                .map(BuffStackingConfig::parseEffectRule)
                .filter(r -> r != null)
                .toList();
    }

    public static List<SpellRawRule> getSpellRules() {
        return SPELL_RULES.get().stream()
                .map(BuffStackingConfig::parseSpellRule)
                .filter(r -> r != null)
                .toList();
    }

    // Parsers

    private static EffectRawRule parseEffectRule(Config cfg) {
        try {
            List<String> activeEffects = cfg.get("active_effects");
            List<String> incomingEffects = cfg.get("incoming_effects");
            boolean replace = cfg.get("replace");

            if (activeEffects == null || activeEffects.isEmpty()) {
                MMCore.LOGGER.warn("[BuffStacking] Skipping effect rule with empty 'active_effects'.");
                return null;
            }
            if (incomingEffects == null || incomingEffects.isEmpty()) {
                MMCore.LOGGER.warn("[BuffStacking] Skipping effect rule with empty 'incoming_effects'.");
                return null;
            }
            return new EffectRawRule(activeEffects, incomingEffects, replace);
        } catch (Exception e) {
            MMCore.LOGGER.warn("[BuffStacking] Skipping malformed effect rule: {}", e.getMessage());
            return null;
        }
    }

    private static SpellRawRule parseSpellRule(Config cfg) {
        try {
            List<String> activeEffects = cfg.get("active_effects");
            List<String> blockedSpells = cfg.get("blocked_spells");
            boolean replace = cfg.get("replace");

            if (activeEffects == null || activeEffects.isEmpty()) {
                MMCore.LOGGER.warn("[BuffStacking] Skipping spell rule with empty 'active_effects'.");
                return null;
            }
            if (blockedSpells == null || blockedSpells.isEmpty()) {
                MMCore.LOGGER.warn("[BuffStacking] Skipping spell rule with empty 'blocked_spells'.");
                return null;
            }
            return new SpellRawRule(activeEffects, blockedSpells, replace);
        } catch (Exception e) {
            MMCore.LOGGER.warn("[BuffStacking] Skipping malformed spell rule: {}", e.getMessage());
            return null;
        }
    }

    // Builder helper

    @FunctionalInterface
    private interface ConfigPopulator { void populate(Config cfg); }

    private static Config makeConfig(ConfigPopulator populator) {
        Config cfg = Config.inMemory();
        populator.populate(cfg);
        return cfg;
    }

    // Raw rule records

    public record EffectRawRule(
            List<String> activeEffects,
            List<String> incomingEffects,
            boolean replace
    ) {}

    public record SpellRawRule(
            List<String> activeEffects,
            List<String> blockedSpells,
            boolean replace
    ) {}
}
