package models;

import org.javatuples.Pair;
import java.util.LinkedList;
import java.util.List;

public class Country{
    String name;
    List<City> localCities;
    Pair<Integer, Integer> lowLeftPoint;
    Pair<Integer, Integer> highRightPoint;
    public Country(String countryLine) {
        String[] args = countryLine.split(" ");
        if (args.length != 5) {
            throw new IllegalArgumentException("Insufficient number of parameters in country line");
        }
        if (args[0].length() > Constants.MAX_NAME_LENGTH) {
            throw new IllegalArgumentException("Too long country name: limit 25 characters");
        }
        int xl = Integer.parseInt(args[1]);
        int yl = Integer.parseInt(args[2]);
        int xh = Integer.parseInt(args[3]);
        int yh = Integer.parseInt(args[4]);
        if ( xl < Constants.MIN_X_VALUE || xh > Constants.MAX_X_VALUE || xl > xh) {
            throw new IllegalArgumentException(
                    String.format("x coordinates have to satisfy the following bounds: %d < xl < xh < %d",
                    Constants.MIN_X_VALUE, Constants.MAX_X_VALUE)
            );
        }
        if ( yl < Constants.MIN_Y_VALUE || yh > Constants.MAX_Y_VALUE || yl > yh) {
            throw new IllegalArgumentException(
                    String.format("y coordinates have to satisfy the following bounds: %d < yl < yh < %d",
                            Constants.MIN_X_VALUE, Constants.MAX_X_VALUE)
            );
        }
        this.name = args[0];
        this.lowLeftPoint = new Pair<>(xl, yl);
        this.highRightPoint = new Pair<>(xh, yh);
        this.localCities = new LinkedList<>();
    }
    boolean isCompleted(){
        return localCities.stream().allMatch(City::isCompleted);
    }
}
