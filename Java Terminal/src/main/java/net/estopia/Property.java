package net.estopia;
import java.util.List;

public class Property {
    private final String name;
    private final int cost;
    private final int house;
    private final List<Integer> rent;
    private int level;
    private final String color;
    private Player owner;

    private final boolean isTrainStation;
    private final boolean isUtility;

    public Property(String name, Integer cost, List<Integer> rent, Integer house, String color, boolean isTrainStation, boolean isUtility) {
        this.name = name;
        this.cost = cost;
        this.rent = rent;
        this.house = house;
        this.owner = null;
        this.color = color;
        this.isTrainStation = isTrainStation;
        this.isUtility = isUtility;
    }

    public String getColor() {
        return color;
    }

    public boolean isTrainStation() {
        return isTrainStation;
    }

    public boolean isUtility() {
        return isUtility;
    }

    public int getRentForUtility(int DiceValue) {
        int numberOfUtility = (int) owner.getProperties().stream()
                .filter(Property::isUtility)
                .count();

        return switch (numberOfUtility) {
            case 1 -> 4 * DiceValue;
            case 2 -> 10 * DiceValue;
            default -> 0;
        };
    }

    public int getRentForTrainStation() {
        if (!isTrainStation || owner == null) {
            return 0;
        }

        int numberOfTrainStations = (int) owner.getProperties().stream()
                .filter(Property::isTrainStation)
                .count();

        return switch (numberOfTrainStations) {
            case 1 -> 25;
            case 2 -> 50;
            case 3 -> 100;
            case 4 -> 200;
            default -> 0;
        };
    }

    public boolean gotColorSet(String color, Player player) {
        int count = 0;
        for (Property property : player.getProperties()) {
            if (property.getColor().equals(color)) {
                count++;
            }
        }
        return count == 3;
    }

    public String getName() {
        return name;
    }

    public int getCost() {
        return cost;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getRent() {
        if (isTrainStation) {
            return getRentForTrainStation();
        } else {
            return rent.get(level);
        }
    }

    public int getRent(int level) {
        return rent.get(level);
    }

    public int getLevel() {
        return level;
    }

    public void upgrade() {
        if(level < 5) {
            level++;
        }
    }

    public Player getOwner() {
        return owner;
    }

    public void setOwner(Player player) {
        owner = player;
    }

    public int getHouse() {
        if(level == 4) {
            return house * 2;
        }
        return house;
    }

}
