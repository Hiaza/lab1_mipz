package models;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Parser {
    public static List<List<Country>> parseFile(String fileName) throws IllegalArgumentException, IOException {
        Objects.requireNonNull(fileName);
        List<List<Country>> tasks = new LinkedList<>();
        try (Stream<String> streamLines = Files.lines(Paths.get(fileName))) {
            List<String> lines = streamLines.collect(Collectors.toList());
            int lineIndex = 0;
            while (lineIndex < lines.size() - 2) {
                String currLine = lines.get(lineIndex);
                int countryNum = Integer.parseInt(currLine);
                if (countryNum < Constants.MIN_NUMBER_OF_COUNTRIES || countryNum > Constants.MAX_NUMBER_OF_COUNTRIES)
                    throw new IllegalArgumentException(
                            String.format("Invalid number of countries! Needs to be between %d and %d",
                                    Constants.MIN_NUMBER_OF_COUNTRIES,
                                    Constants.MAX_NUMBER_OF_COUNTRIES
                            ));
                lineIndex++;
                List<Country> countries = new LinkedList<>();
                for (int countryLineIndex = lineIndex; countryLineIndex < countryNum + lineIndex; countryLineIndex++) {
                    countries.add(new Country(lines.get(countryLineIndex)));
                }
                lineIndex += countryNum;
                tasks.add(countries);
            }
            if (!Objects.equals(lines.get(lines.size() - 1), "0")) {
                throw new Error("Input file must end with '0' line");
            }
        }

        return tasks;
    }
}