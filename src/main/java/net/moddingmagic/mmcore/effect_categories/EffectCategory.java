package net.moddingmagic.mmcore.effect_categories;

public class EffectCategory {

    private final String tag;

    private final String name;

    private final int color;

    public EffectCategory(String tag, String name, int color) {
        this.tag = tag;
        this.name = name;
        this.color = color;
    }

    public String getTag() {
        return tag;
    }

    public String getName() {
        return name;
    }

    /**
     * Returns the ARGB color integer for use with Component styling.
     * Alpha is always 0xFF (fully opaque).
     */
    public int getColor() {
        return color;
    }

    /**
     * Parses a hex color string into an ARGB int.
     * Accepts "#RRGGBB" or "RRGGBB" (case-insensitive).
     * Returns white (0xFFFFFFFF) if parsing fails.
     */
    public static int parseColor(String hex) {
        try {
            String cleaned = hex.startsWith("#") ? hex.substring(1) : hex;
            int rgb = Integer.parseUnsignedInt(cleaned, 16);
            // Ensure full alpha
            return 0xFF000000 | rgb;
        } catch (NumberFormatException e) {
            return 0xFFFFFFFF;
        }
    }
}
