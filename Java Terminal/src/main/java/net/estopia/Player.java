package net.estopia;

import java.util.ArrayList;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import java.util.List;
import java.util.Scanner;

import static java.lang.Math.abs;

public class Player {
    private final String name;
    private int position;
    private int money;
    private int jailTurns;
    private  int getOutJailCard;
    private  String discordid;
    private List<Property> properties;

    public Player(String name, String discordid) {
        this.properties = new ArrayList<>();
        this.name = name;
        this.position = 0;
        this.money = 1500; // starting money
        this.jailTurns = 0;
        this.discordid = discordid;
    }

    public void goToJail() {
        this.jailTurns = 3;
        this.position = 10;
    }

    public String getDiscordId() {
        return discordid;
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
        this.jailTurns = 0;
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

    public boolean payRent(Property property, int rent, MessageReceivedEvent event) {
        if (money >= rent) {
            money -= rent;
            property.getOwner().receiveMoney(rent);
        } else {
            int money = goBankrupt(rent, event);
            property.getOwner().receiveMoney(money);

            return money >= 0;
        }
        return true;
    }

    public void receiveMoney(int amount) {
        money += amount;
    }

    public boolean payMoney(int amount, MessageReceivedEvent event) {
        if (money >= amount) {
            money -= amount;
        } else {
            int money = goBankrupt(amount, event);

            return money >= 0;
        }
        return true;
    }

    public  int getMoney() {
        return money;
    }

    public int goBankrupt(int moneydue, MessageReceivedEvent event) {
        event.getChannel().sendMessage("You owe £" + moneydue + " but only have £" + money + ". You need to sell some properties.").queue();

        while (!properties.isEmpty()) {
            for (Property property : new ArrayList<>(properties)) { // Create a new list to avoid ConcurrentModificationException
                int propertymoney = property.getCost() + (property.getHouse() * 50);
                property.setOwner(null);
                property.setLevel(0);
                event.getChannel().sendMessage(name + " sold " + property.getName() + " for £" + propertymoney).queue();
                money = money + propertymoney;
                properties.remove(property); // Remove the property from the player's list
                if(money >= moneydue) {
                    return money;
                }
            }
        }
        int by = abs(money - moneydue);
        event.getChannel().sendMessage(name + " has gone bankrupt! by £" + by).queue();
        return money - moneydue;
    }

    public List<Property> getProperties() {
        return properties;
    }

    public int getPosition() {
        return position;
    }
}