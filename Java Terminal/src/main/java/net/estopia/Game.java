package net.estopia;
import java.util.List;
import java.util.ArrayList;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class Game {
    private final List<Player> players;
    private final Board board;
    private int currentPlayerIndex;
    private final Dice dice;
    private  boolean started = false;

    public Game() {
        players = new ArrayList<>();
        board = new Board("src/main/resources/data.json");
        currentPlayerIndex = 0;
        dice = new Dice();
    }

    public void start() {
        started = true;
    }

    public  boolean isStarted() {
        return started;
    }

    public void addPlayer(Player player) {
        players.add(player);
    }

    public String nextTurn(MessageReceivedEvent scanner, GameEventHandler Handler) {
        Player currentPlayer = players.get(currentPlayerIndex);
        if(currentPlayer.IsInJail()) {
            if(currentPlayer.GetIntGetOutJailCard() > 0) {
                scanner.getChannel().sendMessage("Player " + currentPlayer.getName() + " is in jail for " + currentPlayer.GetJailTurns() + " more turns, Would you like to pay $50 to get out of jail(p) or Use your get out of Jail Card(c) or none(n)? (p/c/n)").queue();
            } else {
                scanner.getChannel().sendMessage("Player " + currentPlayer.getName() + " is in jail for " + currentPlayer.GetJailTurns() + " more turns, Would you like to pay $50 to get out of jail? (y/n)").queue();
            }
            Handler.addUserState(currentPlayer.getDiscordId(), "waitingForJailResponse");
            return "no";
        }

        int oldPos = currentPlayer.getPosition();
        int diceValue = dice.roll();
        int newPos = currentPlayer.move(diceValue);
        Space space = board.getSpaceAt(currentPlayer.getPosition());
        if (newPos < oldPos) { // Check if the player has passed "Go"
            scanner.getChannel().sendMessage(("Player " + currentPlayer.getName() + " passed Go! Collect £200.")).queue();
            currentPlayer.receiveMoney(200);
        }
        scanner.getChannel().sendMessage("Player " + currentPlayer.getName() + " rolled " + diceValue + " and landed on space " + space.getPosition() + " (" + space.getType() + ")").queue();

        if(space.getType().equalsIgnoreCase("property") || space.getType().equalsIgnoreCase("railroad") || space.getType().equalsIgnoreCase("utility")) {
            Property property = space.getProperty();
            if(property.getOwner() == null) {
                if(currentPlayer.getMoney() >= property.getCost()) {
                    scanner.getChannel().sendMessage("Do you want to buy " + property.getName() + " for £" + property.getCost() + "? You have £" + currentPlayer.getMoney() + " (y/n)").queue();
                    Handler.addUserState(currentPlayer.getDiscordId(), "waitingForBuyPropertyResponse");
                    return "no";
                } else {
                    scanner.getChannel().sendMessage("You do not have enough money to buy " + property.getName() + " for £" + property.getCost() + ".").queue();
                }
            } else if(property.getOwner() != currentPlayer) {
                int rent;
                if(property.isUtility()){
                    rent = property.getRentForUtility(diceValue);
                } else {
                    rent = property.getRent();
                }

                scanner.getChannel().sendMessage("You owe " + property.getOwner().getName() + " £" + rent + " in rent.").queue();

                boolean complete = currentPlayer.payRent(property, rent, scanner);
                if (complete) {
                    scanner.getChannel().sendMessage("You paid " + property.getOwner().getName() + " £" + rent + " in rent. You have £" + currentPlayer.getMoney() + " remaining.").queue();
                } else {
                    scanner.getChannel().sendMessage("You went bankrupt.").queue();
                    players.remove(currentPlayerIndex);
                    currentPlayerIndex = currentPlayerIndex % players.size();
                    return "go";
                }
            }
        } else if (space.getType().equalsIgnoreCase("tax")) {
            scanner.getChannel().sendMessage("You owe the bank £" + space.getCost() + " in taxes.").queue();
            boolean complete = currentPlayer.payMoney(space.getCost(), scanner);
            if (complete) {
                scanner.getChannel().sendMessage("You paid the bank £" + space.getCost() + " in tax. You have £" + currentPlayer.getMoney() + " remaining.").queue();
                return "yes";
            }
            scanner.getChannel().sendMessage("You went bankrupt.").queue();
            players.remove(currentPlayerIndex);
            currentPlayerIndex = currentPlayerIndex % players.size();
            return "go";
        } else if (space.getType().equalsIgnoreCase("go-to-jail")) {
            scanner.getChannel().sendMessage("You are in jail. You cannot move for 3 turns.").queue();
            currentPlayer.goToJail();
            return "yes";
        } else if (space.getType().equalsIgnoreCase("chance")){
            scanner.getChannel().sendMessage("You landed on a chance space. You will be moved to a random space.").queue();
            int randomSpace = (int) (Math.random() * 40);
            currentPlayer.setPosition(randomSpace);
            return "yes";
        } else if (space.getType().equalsIgnoreCase("community-chest")){
            scanner.getChannel().sendMessage("You landed on a community chest space. You will be moved to a random space.").queue();
            int randomSpace = (int) (Math.random() * 40);
            currentPlayer.setPosition(randomSpace);
            return "yes";
        }

        return "yes";
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