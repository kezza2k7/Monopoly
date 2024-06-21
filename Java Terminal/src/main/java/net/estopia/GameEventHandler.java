package net.estopia;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GameEventHandler extends ListenerAdapter {
    private static Game game = new Game();
    private static Map<String, String> userStates = new HashMap<>();

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        String message = event.getMessage().getContentRaw();
        String userId = event.getAuthor().getId();


        // Ensure bot does not respond to itself
        if (event.getAuthor().isBot()) return;

        System.out.println("Message received: " + message + " from " + event.getAuthor().getName());

        // Check if the user is in a state where they need to respond
        System.out.println(userStates);
        if (userStates.containsKey(userId)) {
            System.out.println("User is in state: " + userStates.get(userId));
            handleUserResponse(userId, message, event);
            return;
        }

        if (message.equalsIgnoreCase("!startgame")) {
            game = new Game();
            game.addPlayer(new Player(event.getAuthor().getName(), event.getAuthor().getId()));
            event.getChannel().sendMessage("Game has started, Waiting for Others to join!").queue();
        }

        if (message.equalsIgnoreCase("!start")) {
            game.nextTurn(event, this);
        }


        if(message.equalsIgnoreCase("!join")) {
            if(!game.isStarted()) {
                game.addPlayer(new Player(event.getAuthor().getName(), event.getAuthor().getId()));
                event.getChannel().sendMessage("Player " + event.getAuthor().getName() + " has joined the game!").queue();
            } else {
                event.getChannel().sendMessage("Game has already started!").queue();
            }


        }

        // Additional commands can be handled similarly
    }

    private void handleUserResponse(String userId, String message, MessageReceivedEvent event) {
        String userState = userStates.get(userId);
        System.out.println("Message with: " + userState);

        if ("waitingForUpgradeResponse".equals(userState)) {

            if ("p".equalsIgnoreCase(message)) {
                userStates.remove(userId);
                UpgradeProp(event);
                return;
            } else if ("s".equalsIgnoreCase(message)) {
                userStates.remove(userId);
                game.nextPlayer();
                String messagee = "_\nIt is now: " + game.getCurrentPlayer().getName() + "'s turn";
                event.getChannel().sendMessage(messagee).queue();
                playGame(event);
                return;
            }

            event.getChannel().sendMessage("Do you want to Upgrade your property(p) or stop your turn (s)? (p/s)").queue();

            // Remove the user's state after handling their response

        }
        if("propertyUpgrade".equals(userState)) {
            userStates.remove(userId);
            Property property = game.getBoard().getProperty(message);

            // Check that the property exists
            if(property == null) {
                event.getChannel().sendMessage("Property not found").queue();
                runbeforeend(event);
                return;
            }

            if(property.isTrainStation()) {
                event.getChannel().sendMessage("You cannot upgrade this as it is a train station.").queue();
                runbeforeend(event);
                return;
            }

            if(property.isUtility()){
                event.getChannel().sendMessage("You cannot upgrade this as it is a utility.").queue();
                runbeforeend(event);
                return;
            }

            // Check that it is still upgradable
            if(property.getLevel() > 4) {
                event.getChannel().sendMessage("Property is already at max level").queue();
                runbeforeend(event);
                return;
            }

            // Check if the player has enough money to upgrade the property
            if(game.getCurrentPlayer().getMoney() < property.getHouse()) {
                event.getChannel().sendMessage("Not enough money").queue();
                runbeforeend(event);
                return;
            }

            if(!property.gotColorSet(property.getColor(), game.getCurrentPlayer())) {
                event.getChannel().sendMessage("You do not have all the properties of this color set.").queue();
                runbeforeend(event);
                return;
            }

            game.getCurrentPlayer().payMoney(property.getHouse(), event);
            game.getBoard().getProperty(message).upgrade();
            event.getChannel().sendMessage("Property upgraded to level " + game.getBoard().getProperty(message).getLevel()).queue();
            runbeforeend(event);
        }
        if("waitingForJailResponse".equals(userState)){

            Player currentPlayer = game.getCurrentPlayer();
            if(currentPlayer.IsInJail()) {
                if(currentPlayer.GetIntGetOutJailCard() > 0) {
                    if(message.equalsIgnoreCase("p")) {
                        userStates.remove(userId);
                        currentPlayer.payMoney(50, event);
                        currentPlayer.payJail();
                        game.nextTurn(event, this);
                    } else if(message.equalsIgnoreCase("c")) {
                        userStates.remove(userId);
                        currentPlayer.UseGetOutJailCard();
                        currentPlayer.payJail();
                        game.nextTurn(event, this);
                    } else if(message.equalsIgnoreCase("n")) {
                        userStates.remove(userId);
                        currentPlayer.jailTurn();
                        game.nextTurn(event, this);
                    }
                } else {
                    if(message.equalsIgnoreCase("y")) {
                        userStates.remove(userId);
                        currentPlayer.payMoney(50, event);
                        currentPlayer.payJail();
                    } else if(message.equalsIgnoreCase("n")) {
                        userStates.remove(userId);
                        currentPlayer.jailTurn();
                    }
                    game.nextTurn(event, this);
                }
            }
        }
        if("waitingForBuyPropertyResponse".equals(userState)) {
            userStates.remove(userId);
            Player currentPlayer = game.getCurrentPlayer();
            Space space = game.getBoard().getSpaceAt(currentPlayer.getPosition());
            Property property = space.getProperty();
            if(property.getOwner() == null) {
                if(message.equalsIgnoreCase("y")) {
                    currentPlayer.buyProperty(property);
                    event.getChannel().sendMessage("You bought " + property.getName() + " for £" + property.getCost()).queue();
                } else {
                    event.getChannel().sendMessage("You did not buy " + property.getName()).queue();
                }
            }
            wannaUpgrade(event);
        }
    }

    private void playGame(MessageReceivedEvent event) {
        String complete = game.nextTurn(event, this);

        // If the person goes bankrupt then don't ask them if they want to upgrade
        if(complete.equals("go")){
            if (game.getPlayersSize() == 1) {
                Player winner = game.getPlayer(0);
                event.getChannel().sendMessage("Player " + winner.getName() + " has won the game, with £" + winner.getMoney() + " remaining!").queue();
            }

            game.nextPlayer();
            String messagee = "_\nIt is now: " + game.getCurrentPlayer().getName() + "'s turn";
            event.getChannel().sendMessage(messagee).queue();
            playGame(event);
        }
        if(complete.equals("yes")){
            wannaUpgrade(event);
        }
    }

    private void wannaUpgrade(MessageReceivedEvent event) {
        event.getChannel().sendMessage("Do you want to Upgrade your property(p) or stop your turn (s)? (p/s)").queue();
        addUserState(game.getCurrentPlayer().getDiscordId(), "waitingForUpgradeResponse");
    }



    public void UpgradeProp(MessageReceivedEvent event) {
        // Get the spaces that the player owns
        List<Space> spaces = game.getBoard().getSpaces(game.getCurrentPlayer());

        // Check if the player has any properties to upgrade
        if(spaces.isEmpty()) {
            event.getChannel().sendMessage("You don't have any properties to upgrade").queue();
            runbeforeend(event);
            return;
        }

        event.getChannel().sendMessage("Which property do you want to upgrade?\nAvailable properties: ").queue();

        // Format the Properies to be displayed
        String text = "";
        for (Space space : spaces) {
            if(space.getProperty() == null) {
                continue;
            }
            if(space.getProperty().isTrainStation()){
                text = text + space.getProperty().getName() + " Current Rent Price - £" + space.getProperty().getRent() + " You cannot upgrade this as it is a train station.\n";
                continue;
            }
            if(!space.getProperty().gotColorSet(space.getProperty().getColor(), game.getCurrentPlayer())) {
                text = text + space.getProperty().getName() + " Current Level - " + space.getProperty().getLevel() + " Current Rent Price - £" + space.getProperty().getRent() + " You cannot upgrade this as you do not have all the properties of this color set.\n";
                continue;
            }

            text = text + space.getProperty().getName() + " Current Level - " + space.getProperty().getLevel() + " Current Rent Price - £" + space.getProperty().getRent() + " Upgrade Cost - £" + space.getProperty().getHouse() + " Upgraded Rent Price - £" + space.getProperty().getRent(space.getProperty().getLevel() + 1);
        }

        event.getChannel().sendMessage(text).queue();

        // Ask what property they want to upgrade
        addUserState(game.getCurrentPlayer().getDiscordId(), "propertyUpgrade");
    }

    public static void addUserState(String userId, String state) {
        System.out.println("Adding user state: " + state + " for user: " + userId);
        userStates.put(userId, state);
    }

    public void runbeforeend(MessageReceivedEvent event) {
        game.nextPlayer();
        game.nextTurn(event, this);
    }
}