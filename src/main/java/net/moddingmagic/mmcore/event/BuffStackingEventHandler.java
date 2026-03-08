package net.moddingmagic.mmcore.event;

import io.redspace.ironsspellbooks.api.events.SpellPreCastEvent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.moddingmagic.mmcore.buffstacking.BuffStackingManager;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;
import net.neoforged.fml.common.EventBusSubscriber;

@EventBusSubscriber
public class BuffStackingEventHandler {

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onEffectApplicable(MobEffectEvent.Applicable event) {
        LivingEntity entity = event.getEntity();
        if (entity.level().isClientSide()) return;

        BuffStackingManager.RuleResult result =
                BuffStackingManager.evaluateEffect(entity, event.getEffectInstance());

        switch (result.action()) {
            case CANCEL  -> event.setResult(MobEffectEvent.Applicable.Result.DO_NOT_APPLY);
            case REPLACE -> {
                entity.removeEffect(result.effectToRemove().getEffect());
            }
            case ALLOW   -> {}
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onSpellPreCast(SpellPreCastEvent event) {
        LivingEntity caster = event.getEntity();
        if (caster.level().isClientSide()) return;

        BuffStackingManager.RuleResult result =
                BuffStackingManager.evaluateSpell(caster, ResourceLocation.parse(event.getSpellId()));

        switch (result.action()) {
            case CANCEL  -> event.setCanceled(true);
            case REPLACE -> {
                // Remove the conflicting effect and let the spell cast normally.
                caster.removeEffect(result.effectToRemove().getEffect());
            }
            case ALLOW   -> {}
        }
    }
}
