package net.moddingmagic.mmcore.mixin;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.TooltipFlag;
import net.moddingmagic.mmcore.effect_categories.EffectCategory;
import net.moddingmagic.mmcore.effect_categories.EffectCategoryManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

// We target JEED's custom tooltip generator
@Mixin(targets = "net.mehvahdjukaar.jeed.common.EffectRenderer")
public class JeedEffectRendererMixin {

    // Inject exactly when JEED is done building its tooltip list, right before it returns it
    @Inject(method = "getTooltipsWithDescription", at = @At("RETURN"), remap = false)
    private static void injectMMCoreCategories(MobEffectInstance effectInstance, TooltipFlag tooltipFlag, boolean reactsToShift, boolean showDuration, CallbackInfoReturnable<List<Component>> cir) {
        if (effectInstance == null) return;

        // Get the list JEED just created
        List<Component> tooltip = cir.getReturnValue();

        List<EffectCategory> categories = EffectCategoryManager.INSTANCE.getCategoriesForEffect(effectInstance.getEffect());
        if (categories.isEmpty()) return;

        int insertIndex = -1;

        // Find JEED's Category line
        for (int i = 0; i < tooltip.size(); i++) {
            Component comp = tooltip.get(i);
            if (comp.getContents() instanceof TranslatableContents translatable) {
                String key = translatable.getKey();
                if (key.equals("jeed.tooltip.beneficial") ||
                        key.equals("jeed.tooltip.neutral") ||
                        key.equals("jeed.tooltip.harmful")) {
                    insertIndex = i + 1;
                    break;
                }
            }
        }

        // Add our MMCore categories
        for (EffectCategory cat : categories) {
            int rgb = cat.getColor() & 0x00FFFFFF;
            Component myCatComponent = Component.literal(cat.getName()).withStyle(Style.EMPTY.withColor(rgb));

            if (insertIndex != -1) {
                tooltip.add(insertIndex, myCatComponent);
                insertIndex++;
            } else {
                tooltip.add(myCatComponent);
            }
        }
    }
}
