package net.estopia;
import java.io.SyncFailedException;
import java.util.ArrayList;
import java.util.List;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import org.json.JSONArray;
import org.json.JSONObject;

public class Board {
    private List<Space> spaces;

    public Board(String filename) {
        this.spaces = new ArrayList<>();

        File file = new File(filename);
        Scanner scanner = null;
        try {
            scanner = new Scanner(file);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        StringBuilder jsonIn = new StringBuilder();
        while (scanner.hasNextLine()) {
            jsonIn.append(scanner.nextLine());
        }
        scanner.close();

        JSONObject jsonObject = new JSONObject(jsonIn.toString());
        JSONArray spacesArray = jsonObject.getJSONArray("prop");
        for (int i = 0; i < spacesArray.length(); i++) {
            JSONObject spaceObject = spacesArray.getJSONObject(i);
            if(spaceObject.get("type").equals("property") || spaceObject.get("type").equals("railroad") || spaceObject.get("type").equals("utility")) {
                boolean isRailRoad = spaceObject.get("type").equals("railroad");
                boolean isUtility = spaceObject.get("type").equals("utility");

                List<Integer> rentList = new ArrayList<>();

                String name = spaceObject.getString("name");
                int cost = spaceObject.getInt("cost");
                int house = 0;
                String color = "";

                if(!isRailRoad && !isUtility) {
                    house = spaceObject.getInt("house");
                    color = spaceObject.getString("color");

                    JSONArray rentArray = spaceObject.getJSONArray("rent");
                    for (int j = 0; j < rentArray.length(); j++) {
                        rentList.add(rentArray.getInt(j));
                    }
                }

                Property property = new Property(name, cost, rentList, house, color, isRailRoad, isUtility);
                spaces.add(new Space(i, property, spaceObject.getString("type"), null));
            } else {
                if(spaceObject.get("type").equals("tax")) {
                    spaces.add(new Space(i, null, spaceObject.getString("type"), spaceObject.getInt("cost")));
                } else {
                    spaces.add(new Space(i, null, spaceObject.getString("type"), null));
                }
            }}
    }

    public Space getSpaceAt(int position) {
        return spaces.get(position);
    }

    public List<Space> getSpaces(Player player) {
        List<Space> spaces = new ArrayList<>();
        for (Space space : this.spaces) {
            if (space.getProperty() != null && space.getProperty().getOwner() == player) {
                spaces.add(space);
            }
        }
        return spaces;
    }

    public Property getProperty(String propertyName) {
        for (Space space : spaces) {
            if (space.getProperty() != null && space.getProperty().getName().equalsIgnoreCase(propertyName)) {
                return space.getProperty();
            }
        }
        return null;
    }
}