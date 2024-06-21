package net.estopia;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import static java.lang.Math.abs;

public class Player {
    private final String name;
    private int position;
    private int money;
    private int jailTurns;
    private  int getOutJailCard;
    private List<Property> properties;

    public Player(String name) {
        this.properties = new ArrayList<>();
        this.name = name;
        this.position = 0;
        this.money = 1500; // starting money
        this.jailTurns = 0;
    }

    public void goToJail() {
        this.jailTurns = 3;
        this.position = 10;
    }

    public void payJail() {
        this.jailTurns = 0;
    }

    public void jailTurn() {
        this.jailTurns--;
    }

    public int GetJailTurns() {
        return this.jailTurns;
    }

    public void UseGetOutJailCard() {
        this.getOutJailCard--;
    }

    public void GetGetOutJailCard() {
        this.getOutJailCard++;
    }

    public int GetIntGetOutJailCard() {
        return this.getOutJailCard;
    }

    public boolean IsInJail() {
        return this.jailTurns > 0;
    }

    public int move(int spaces) {
        position = (position + spaces) % 40; // assuming a standard Monopoly board with 40 spaces
        return position;
    }

    public String getName() {
        return name;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public void buyProperty(Property property) {
        if (money >= property.getCost()) {
            money -= property.getCost();
            property.setOwner(this);
            properties.add(property); // add the property to the player's list
        }
    }

    public boolean payRent(Property property, int rent) {
        if (money >= rent) {
            money -= rent;
            property.getOwner().receiveMoney(rent);
        } else {
            int money = goBankrupt(rent);
            property.getOwner().receiveMoney(money);

            return money >= 0;
        }
        return true;
    }

    public void receiveMoney(int amount) {
        money += amount;
    }

    public boolean payMoney(int amount) {
        if (money >= amount) {
            money -= amount;
        } else {
            int money = goBankrupt(amount);

            return money >= 0;
        }
        return true;
    }

    public  int getMoney() {
        return money;
    }

    public int goBankrupt(int moneydue) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("You owe £" + moneydue + " but only have £" + money + ". You need to sell some properties.");

        while (!properties.isEmpty()) {
            for (Property property : new ArrayList<>(properties)) { // Create a new list to avoid ConcurrentModificationException
                int propertymoney = property.getCost() + (property.getHouse() * 50);
                System.out.println("Do you want to sell " + property.getName() + " for £" + propertymoney + "? (y/n)");
                String response = scanner.nextLine();
                if (response.equalsIgnoreCase("y")) {
                    property.setOwner(null);
                    property.setLevel(0);
                    System.out.println(name + " sold " + property.getName() + " for £" + propertymoney);
                    money = money + propertymoney;
                    properties.remove(property); // Remove the property from the player's list
                    if(money >= moneydue) {
                        return money;
                    }
                }
            }
        }
        int by = abs(money - moneydue);
        System.out.println(name + " has gone bankrupt! by £" + by);
        return money - moneydue;
    }

    public List<Property> getProperties() {
        return properties;
    }

    public int getPosition() {
        return position;
    }
}