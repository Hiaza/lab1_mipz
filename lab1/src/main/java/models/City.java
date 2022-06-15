package models;

import org.javatuples.Pair;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class City {
    List<City> neighbours;
    String country;
    Map<String, Long> coins;
    Map<String, Long> shared_coins;
    Pair<Integer, Integer> cords;


    public City(String country, Pair<Integer, Integer> cords, Map<String, Long> coins, Map<String, Long> shared_coins) {
        this.country = country;
        this.neighbours = new LinkedList<>();
        this.cords = cords;
        this.coins = coins;
        this.shared_coins = shared_coins;
        this.coins.put(this.country, Constants.INIT_COINS);
    }

    boolean isCompleted(){
        boolean result = true;
        for (Map.Entry<String, Long> entry: coins.entrySet()) {
            if (entry.getValue() == 0) {
                result = false;
                break;
            }
        }
        return result;
    }
}
