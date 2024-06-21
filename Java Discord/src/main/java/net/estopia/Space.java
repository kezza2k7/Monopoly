package net.estopia;
public class Space {
    private final int position;
    private final Property property;
    private final String type;
    private final int cost;

    public Space(int position, Property property, String extra, Integer coste) {
        this.position = position;
        this.property = property;
        this.type = extra;
        this.cost = coste == null ? 0 : coste;
    }

    public int getPosition() {
        return position;
    }

    public Property getProperty() {
        return property;
    }

    public String getType() {
        return type;
    }

    public int getCost() {
        return cost;
    }
}
