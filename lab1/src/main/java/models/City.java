package models;

import org.javatuples.Pair;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class City {
    List<City> neighbours;
    String country;
    Map<String, Long> coins;
    Map<String, Long> sharedCoins;
    Pair<Integer, Integer> cords;


    public City(String country, Pair<Integer, Integer> cords, Map<String, Long> coins, Map<String, Long> sharedCoins) {
        this.country = country;
        this.neighbours = new LinkedList<>();
        this.cords = cords;
        this.coins = coins;
        this.sharedCoins = sharedCoins;
        this.coins.put(this.country, Constants.INIT_COINS);
    }

    boolean isCompleted(){
        return coins.values().stream().allMatch(value -> value > 0);
    }
}
