package net.moddingmagic.mmcore.mixin;

import io.redspace.ironsspellbooks.api.config.SpellConfigManager;
import io.redspace.ironsspellbooks.api.config.SpellConfigParameter;
import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.moddingmagic.mmcore.formulas.FormulaEvaluator;
import net.moddingmagic.mmcore.util.ISpellAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = AbstractSpell.class, remap = false)
public abstract class SpellPowerMixin {

    /**
     * We inject at HEAD, read the entity's attributes ourselves, compute the custom multipliers, assemble the result, and cancel the original return.
     */
    @Inject(
            method = "getSpellPower",
            at = @At("HEAD"),
            cancellable = true,
            remap = false
    )
    private void mmcore$overrideSpellPower(
            int spellLevel,
            Entity sourceEntity,
            CallbackInfoReturnable<Float> cir
    ) {
        // Cast to access protected fields via the accessor interface.
        AbstractSpell self = (AbstractSpell) (Object) this;

        // Retrieve base values via the accessor mixin.
        ISpellAccessor accessor = (ISpellAccessor) self;
        int baseSpellPower = accessor.mmcore$getBaseSpellPower();
        int spellPowerPerLevel = accessor.mmcore$getSpellPowerPerLevel();

        // Config power multiplier.
        float configPowerMultiplier = SpellConfigManager
                .getSpellConfigValue(self, SpellConfigParameter.POWER_MULTIPLIER)
                .floatValue();

        float rawBase = baseSpellPower + spellPowerPerLevel * (spellLevel - 1);

        if (!(sourceEntity instanceof LivingEntity livingEntity)) {
            // No entity -> no attribute modifier, just apply config multiplier.
            cir.setReturnValue(rawBase * configPowerMultiplier);
            return;
        }

        // Raw attribute values.
        double rawSpellPower = livingEntity.getAttributeValue(AttributeRegistry.SPELL_POWER);
        double rawSchoolPower = self.getSchoolType().getPowerFor(livingEntity);

        // Apply our configurable formulas.
        double spellPowerMultiplier  = FormulaEvaluator.spellPowerMultiplier(rawSpellPower);
        double schoolPowerMultiplier = FormulaEvaluator.spellPowerMultiplier(rawSchoolPower);

        float result = (float) (rawBase * spellPowerMultiplier * schoolPowerMultiplier * configPowerMultiplier);
        cir.setReturnValue(result);
    }
}
