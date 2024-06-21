package net.estopia;
import java.util.List;
import java.util.Scanner;

public class Main {
    private static Game game;
    private static Scanner scanner;

    public static void main(String[] args) {
        game = new Game();
        scanner = new Scanner(System.in);

        createPlayers();
        playGame();
    }

    private static void createPlayers() {
        game.addPlayer(new Player("Jay"));
        game.addPlayer(new Player("Jayden"));
    }

    public static void UpgradeProp() {
        // Get the spaces that the player owns
        List<Space> spaces = game.getBoard().getSpaces(game.getCurrentPlayer());

        // Check if the player has any properties to upgrade
        if(spaces.isEmpty()) {
            System.out.println("You don't have any properties to upgrade");
            return;
        }

        System.out.println("Which property do you want to upgrade?");
        System.out.println("Available properties: ");

        // Format the Properies to be displayed
        for (Space space : spaces) {
            if(space.getProperty() == null) {
                continue;
            }
            if(space.getProperty().isTrainStation()){
                System.out.println(space.getProperty().getName() + " Current Rent Price - £" + space.getProperty().getRent() + " You cannot upgrade this as it is a train station.");
                continue;
            }
            if(!space.getProperty().gotColorSet(space.getProperty().getColor(), game.getCurrentPlayer())) {
                System.out.println(space.getProperty().getName() + " Current Level - " + space.getProperty().getLevel() + " Current Rent Price - £" + space.getProperty().getRent() + " You cannot upgrade this as you do not have all the properties of this color set.");
                continue;
            }

            System.out.println(space.getProperty().getName() + " Current Level - " + space.getProperty().getLevel() + " Current Rent Price - £" + space.getProperty().getRent() + " Upgrade Cost - £" + space.getProperty().getHouse() + " Upgraded Rent Price - £" + space.getProperty().getRent(space.getProperty().getLevel() + 1));
        }

        // Ask what property they want to upgrade
        String propertyName = scanner.nextLine();
        Property property = game.getBoard().getProperty(propertyName);

        // Check that the property exists
        if(property == null) {
            System.out.println("Property not found");
            return;
        }

        if(property.isTrainStation()) {
            System.out.println("You cannot upgrade this as it is a train station.");
            return;
        }

        if(property.isUtility()){
            System.out.println("You cannot upgrade this as it is a utility.");
            return;
        }

        // Check that it is still upgradable
        if(property.getLevel() > 4) {
            System.out.println("Property is already at max level");
            return;
        }

        // Check if the player has enough money to upgrade the property
        if(game.getCurrentPlayer().getMoney() < property.getHouse()) {
            System.out.println("Not enough money");
            return;
        }

        if(!property.gotColorSet(property.getColor(), game.getCurrentPlayer())) {
            System.out.println("You do not have all the properties of this color set.");
            return;
        }

        game.getCurrentPlayer().payMoney(property.getHouse());
        game.getBoard().getProperty(propertyName).upgrade();
        System.out.println("Property upgraded to level " + game.getBoard().getProperty(propertyName).getLevel());
    }
    public static void playGame() {
        String response;

        do {
            boolean complete = game.nextTurn(scanner);

            response = "s";

            // If the person goes bankrupt then don't ask them if they want to upgrade
            if(!complete) {
                if(game.getPlayersSize() == 1) {
                    Player winner = game.getPlayer(0);
                    System.out.println("Player " + winner.getName() + " has won the game, with £" + winner.getMoney() + " remaining!");
                    break;
                }
                continue;
            }

            System.out.println("Do you want to Upgrade your property's(p) or stop your turn (s)? (p/s)");

            response = scanner.nextLine();
            if(response.equalsIgnoreCase("p")) {
                UpgradeProp();
                response = "s";
            }
            game.nextPlayer();
        } while (response.equalsIgnoreCase("s"));
    }
}

