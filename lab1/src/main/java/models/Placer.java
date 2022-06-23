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
                    if (country.isCompleted()) {
                        results.putIfAbsent(country.name, i);
                    }
                    else {
                        isCompleted = false;
                    }
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
                    Map<String, Long> sharedCoins = new HashMap<>();
                    for (String name: names) {
                        coins.put(name, 0L);
                        sharedCoins.put(name, 0L);
                    }
                    City city = new City(country.name, cords, coins, sharedCoins);
                    if(territory.containsKey(cords))
                        throw new IllegalArgumentException("Coordinates of " + country.name + " and "
                                + territory.get(cords).country + " intersect with each others");
                    else territory.put(cords, city);
                    country.localCities.add(city);
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

            List<Pair<Integer,Integer>> potentialNeighbours = new ArrayList<>();
            Collections.addAll(potentialNeighbours,
                    new Pair<>(x + 1, y),
                    new Pair<>(x - 1, y),
                    new Pair<>(x, y + 1),
                    new Pair<>(x, y - 1)
            );

            potentialNeighbours.stream()
                    .filter(territory::containsKey)
                    .map(territory::get)
                    .forEach(city.neighbours::add);
        }
        if(countries.size()>1){
            for (Country country:countries) {
                if(country.localCities.stream().map(x->x.neighbours).flatMap(Collection::stream).allMatch(x-> x.country.equals(country.name))){
                    throw new IllegalArgumentException("Country " + country.name + " is not connected with others");
                }
            }
        }
    }

    void spreadMoney(){
        for (Map.Entry<Pair<Integer, Integer>,City> entry:territory.entrySet()) {
            City currentCity = entry.getValue();
            for (City neighbour: currentCity.neighbours) {
                for (String name :names) {
                    Long sharedValue = currentCity.coins.get(name)/ Constants.REPRESENTATIVE_PORTION_PER_COIN;
                    neighbour.sharedCoins.put(name, neighbour.sharedCoins.get(name) + sharedValue);
                    currentCity.sharedCoins.put(name, currentCity.sharedCoins.get(name) - sharedValue);
                }
            }
        }
        for (Map.Entry<Pair<Integer, Integer>,City> entry:territory.entrySet()) {
            City currentCity = entry.getValue();
            for (String name :names) {
                currentCity.coins.put(name, currentCity.coins.get(name) + currentCity.sharedCoins.get(name));
                currentCity.sharedCoins.put(name, 0L);
            }
        }
    }

}
