package io.quarkiverse.observability.minecraft.mod;

public class WuffStuff {

    private String name = "undefined";
    private String colour = "#000000";
    private Pattern pattern = Pattern.PLAIN;
    private boolean isSitting;
    private float headRollAngle = 0f;

    @Override
    public String toString() {
        return "WuffStuff{" +
                "name='" + name + '\'' +
                ", colour='" + colour + '\'' +
                ", pattern=" + pattern +
                ", isSitting=" + isSitting +
                ", headRollAngle=" + headRollAngle +
                '}';
    }

    public float getHeadRollAngle() {
        return headRollAngle;
    }

    public void setHeadRollAngle(float headRollAngle) {
        this.headRollAngle = headRollAngle;
    }

    public boolean isSitting() {
        return isSitting;
    }

    public void setSitting(boolean sitting) {
        isSitting = sitting;
    }

    public Pattern getPattern() {
        return pattern;
    }

    public void setPattern(Pattern pattern) {
        this.pattern = pattern;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getColour() {
        return colour;
    }

    public void setColour(String colour) {
        this.colour = colour;
    }

}
