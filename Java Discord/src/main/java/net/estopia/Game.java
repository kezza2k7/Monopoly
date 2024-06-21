package net.estopia;
import java.util.List;
import java.util.ArrayList;
import java.util.Scanner;

public class Game {
    private final List<Player> players;
    private final Board board;
    private int currentPlayerIndex;
    private final Dice dice;

    public Game() {
        players = new ArrayList<>();
        board = new Board("src/main/resources/data.json");
        currentPlayerIndex = 0;
        dice = new Dice();
    }

    public void addPlayer(Player player) {
        players.add(player);
    }

    public boolean nextTurn(Scanner scanner) {
        Player currentPlayer = players.get(currentPlayerIndex);
        if(currentPlayer.IsInJail()) {
            if(currentPlayer.GetIntGetOutJailCard() > 0) {
                System.out.println("Player " + currentPlayer.getName() + " is in jail for " + currentPlayer.GetJailTurns() + " more turns, Would you like to pay $50 to get out of jail(p) or Use your get out of Jail Card(c) or none(n)? (p/c/n)");
                String response = scanner.nextLine();
                if(response.equalsIgnoreCase("yes")) {
                    currentPlayer.payMoney(50);
                    currentPlayer.payJail();
                } else {
                    currentPlayer.jailTurn();
                    return true;
                }
            } else {
                System.out.println("Player " + currentPlayer.getName() + " is in jail for " + currentPlayer.GetJailTurns() + " more turns, Would you like to pay $50 to get out of jail? (y/n)");
                String response = scanner.nextLine();
                if(response.equalsIgnoreCase("y")) {
                    currentPlayer.payMoney(50);
                    currentPlayer.payJail();
                } else {
                    currentPlayer.jailTurn();
                    return true;
                }
            }
        }
        int oldPos = currentPlayer.getPosition();
        int diceValue = dice.roll();
        int newPos = currentPlayer.move(diceValue);
        Space space = board.getSpaceAt(newPos);
        if (newPos < oldPos) { // Check if the player has passed "Go"
            System.out.println("Player " + currentPlayer.getName() + " passed Go! Collect £200.");
            currentPlayer.receiveMoney(200);
        }

        System.out.println("Player " + currentPlayer.getName() + " rolled " + diceValue + " and landed on space " + space.getPosition() + " (" + space.getType() + ")");
        if(space.getType().equalsIgnoreCase("property") || space.getType().equalsIgnoreCase("railroad") || space.getType().equalsIgnoreCase("utility")) {
            Property property = space.getProperty();
            if(property.getOwner() == null) {
                if(currentPlayer.getMoney() >= property.getCost()) {
                    System.out.println("Do you want to buy " + property.getName() + " for £" + property.getCost() + "? You have £" + currentPlayer.getMoney() + " (y/n)");
                    String response = scanner.nextLine();
                    if (response.equalsIgnoreCase("y")) {
                        currentPlayer.buyProperty(property);
                    }
                } else {
                    System.out.println("You do not have enough money to buy " + property.getName() + " for £" + property.getCost() + ".");
                }
            } else if(property.getOwner() != currentPlayer) {
                int rent;
                if(property.isUtility()){
                    rent = property.getRentForUtility(diceValue);
                } else {
                    rent = property.getRent();
                }

                System.out.println("You owe " + property.getOwner().getName() + " £" + rent + " in rent.");

                boolean complete = currentPlayer.payRent(property, rent);
                if (complete) {
                    System.out.println("You paid " + property.getOwner().getName() + " £" + rent + " in rent. You have £" + currentPlayer.getMoney() + " remaining.");
                } else {
                    System.out.println("You went bankrupt.");
                    players.remove(currentPlayerIndex);
                    currentPlayerIndex = currentPlayerIndex % players.size();
                    return false;
                }
            }
        } else if (space.getType().equalsIgnoreCase("tax")) {
            System.out.println("You owe the bank £" + space.getCost() + " in taxes.");
            boolean complete = currentPlayer.payMoney(space.getCost());
            if (complete) {
                System.out.println("You paid the bank £" + space.getCost() + " in tax. You have £" + currentPlayer.getMoney() + " remaining.");
            } else {
                System.out.println("You went bankrupt.");
                players.remove(currentPlayerIndex);
                currentPlayerIndex = currentPlayerIndex % players.size();
                return false;
            }
        } else if (space.getType().equalsIgnoreCase("go-to-jail")) {
            System.out.println("You are in jail. You cannot move for 3 turns.");
            currentPlayer.goToJail();
        } else if (space.getType().equalsIgnoreCase("chance")){
            System.out.println("You landed on a chance space. You will be moved to a random space.");
            int randomSpace = (int) (Math.random() * 40);
            currentPlayer.setPosition(randomSpace);
        } else if (space.getType().equalsIgnoreCase("community-chest")){
            System.out.println("You landed on a community chest space. You will be moved to a random space.");
            int randomSpace = (int) (Math.random() * 40);
            currentPlayer.setPosition(randomSpace);
        }

        System.out.println();
        return true;
    }

    public void nextPlayer() {
        currentPlayerIndex = (currentPlayerIndex + 1) % players.size();
    }
    public Player getCurrentPlayer() {
        return players.get(currentPlayerIndex);
    }

    public Board getBoard() {
        return board;
    }

    public Player getPlayer(int index) {
        return players.get(index);
    }

    public int getPlayersSize() {
        return players.size();
    }
}