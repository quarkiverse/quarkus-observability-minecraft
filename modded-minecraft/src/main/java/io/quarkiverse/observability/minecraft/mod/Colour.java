package io.quarkiverse.observability.minecraft.mod;

public class Colour {
    private final float r;
    private final float g;
    private final float b;

    public Colour(String hex) {
        if (hex == null) {
            r = 0f;
            g = 0f;
            b = 0f;
        } else {
            // Remove leading '#' if present
            if (hex.startsWith("#")) {
                hex = hex.substring(1);
            }

            // Expand 3-digit shorthand (e.g. "F80" -> "FF8800")
            if (hex.length() == 3) {
                StringBuilder sb = new StringBuilder();
                for (char c : hex.toCharArray()) {
                    sb.append(c).append(c);
                }
                hex = sb.toString();
            }

            // Ensure valid 6-digit hex
            if (hex.length() != 6) {
                throw new IllegalArgumentException("Hex colour must be 3 or 6 characters long");
            }

            try {
                int rgb = Integer.parseInt(hex, 16);
                r = ((rgb >> 16) & 0xFF) / 255.0f;
                g = ((rgb >> 8) & 0xFF) / 255.0f;
                b = (rgb & 0xFF) / 255.0f;
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid hex colour: " + hex, e);
            }
        }
    }

    public float getR() {
        return r;
    }

    public float getG() {
        return g;
    }

    public float getB() {
        return b;
    }

    @Override
    public String toString() {
        return String.format("Colour(r=%.3f, g=%.3f, b=%.3f)", r, g, b);
    }
}