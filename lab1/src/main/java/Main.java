import models.Country;
import models.Parser;
import models.Placer;

import java.util.List;

public class Main {
    public static void main(String[] args){
        try {
            List<List<Country>> tasks = Parser.parseFile("src/main/resources/inputFile.txt");
            for (int i = 0; i < tasks.size(); i++) {
                System.out.println("Case Number " + (i + 1));
                Placer placer = new Placer(tasks.get(i));
                String result = placer.compute();
                System.out.println(result);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
