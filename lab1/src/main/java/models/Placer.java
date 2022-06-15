package models;

import org.javatuples.Pair;

import java.util.*;
import java.util.stream.Collectors;

public class Placer {
    private final List<Country> countries;
    private final List<String> names;
    private final Map<Pair<Integer,Integer>,City> territory = new HashMap<>();

    public Placer(List<Country> countries) {
        this.countries = countries;
        this.names = countries.stream().map(country -> country.name).collect(Collectors.toList());
        init();
    }
    void init(){
        fillTerritory();
        fillNeighbours();
    }

    public String compute(){
        Map<String,Integer> results = new TreeMap<>();

        if(names.size() == 1){
            results.put(names.get(0), 0);
        } else{
            boolean isCompleted = false;
            int i = 1;
            while(!isCompleted){
                spreadMoney();
                isCompleted = true;
                for (Country country: countries) {
                    boolean flag = country.isCompleted();
                    if (flag) {
                        results.putIfAbsent(country.name, i);
                    }
                    isCompleted = flag && isCompleted;
                }
                i++;
            }
        }
        return printResult(entriesSortedByValuesAndKeys(results));
    }

    private String printResult(SortedSet<Map.Entry<String, Integer>> set){
        StringBuilder result = new StringBuilder();
        for(Map.Entry<String, Integer> record:set) {
           result.append(record.getKey()).append(" ").append(record.getValue());
           result.append("\n");
        }
        result.deleteCharAt(result.length()-1);
        return result.toString();
    }

    static <String,Integer extends Comparable<? super Integer>> SortedSet<Map.Entry<String, Integer>> entriesSortedByValuesAndKeys(Map<String,Integer> map) {
        SortedSet<Map.Entry<String, Integer>> sortedEntries = new TreeSet<>(
                Map.Entry.<String, Integer>comparingByValue().thenComparing(e -> e.getKey().toString())
        );
        sortedEntries.addAll(map.entrySet());
        return sortedEntries;
    }

    void fillTerritory(){
        for (Country country: countries) {
            int xl = country.lowLeftPoint.getValue0();
            int yl = country.lowLeftPoint.getValue1();
            int xh = country.highRightPoint.getValue0();
            int yh = country.highRightPoint.getValue1();

            for (; xl <= xh; xl++){
                for (int j = yl; j <= yh; j++){
                    Pair<Integer, Integer> cords = new Pair<>(xl,j);
                    Map<String, Long> coins = new HashMap<>();
                    Map<String, Long> shared_coins = new HashMap<>();
                    for (String name: names) {
                        coins.put(name, 0L);
                        shared_coins.put(name, 0L);
                    }
                    City city = new City(country.name, cords, coins, shared_coins);
                    country.local_cities.add(city);
                    territory.put(cords, city);
                }
            }
        }
    }
    void fillNeighbours(){
        for (Map.Entry<Pair<Integer, Integer>,City> entry:territory.entrySet()) {
            Pair<Integer, Integer> pair = entry.getKey();
            City city = entry.getValue();
            int x = pair.getValue0();
            int y = pair.getValue1();
            for (int i = -1; i <= 1; i+=2){
                Pair<Integer, Integer> cords = new Pair<>(x + i, y);
                if (territory.containsKey(cords)){
                    city.neighbours.add(territory.get(cords));
                }
                cords = new Pair<>(x, y + i);
                if (territory.containsKey(cords)){
                    city.neighbours.add(territory.get(cords));
                }
            }
        }
    }

    void spreadMoney(){
        for (Map.Entry<Pair<Integer, Integer>,City> entry:territory.entrySet()) {
            City current_city = entry.getValue();
            for (City neighbour: current_city.neighbours) {
                for (String name :names) {
                    Long sharedValue = current_city.coins.get(name)/ Constants.REPRESENTATIVE_PORTION_PER_COIN;
                    neighbour.shared_coins.put(name, neighbour.shared_coins.get(name) + sharedValue);
                    current_city.shared_coins.put(name, current_city.shared_coins.get(name) - sharedValue);
                }
            }
        }
        for (Map.Entry<Pair<Integer, Integer>,City> entry:territory.entrySet()) {
            City current_city = entry.getValue();
            for (String name :names) {
                current_city.coins.put(name, current_city.coins.get(name) + current_city.shared_coins.get(name));
                current_city.shared_coins.put(name, 0L);
            }
        }
    }

}
