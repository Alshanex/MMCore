package net.moddingmagic.mmcore.mixin;

import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import net.moddingmagic.mmcore.util.ISpellAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = AbstractSpell.class, remap = false)
public abstract class SpellAccessorMixin implements ISpellAccessor {

    @Shadow protected int baseSpellPower;
    @Shadow protected int spellPowerPerLevel;

    @Override
    public int mmcore$getBaseSpellPower() {
        return baseSpellPower;
    }

    @Override
    public int mmcore$getSpellPowerPerLevel() {
        return spellPowerPerLevel;
    }
}
