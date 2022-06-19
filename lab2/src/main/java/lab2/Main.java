package lab2;

import com.github.javaparser.ast.Node;
import java.io.IOException;
import java.util.*;
import static lab2.Evaluator.*;

public class Main {
    private static final String FILE_PATH = "/home/artem/projects/lab1_mipz/lab2/src/main/java/";
    private static final String CLASS_NAME = "Test";

    public static void main(String[] args) throws IOException {
        List<TreeNode<Node>> tree = constructTheTree(getTheCombinedNodeListFromAllLib(FILE_PATH));
        try {
            calculateForSingleClass(tree, CLASS_NAME);
        }catch (IllegalArgumentException x){
            System.out.println("Class not found within directory: " + CLASS_NAME);
        }
        System.out.println("-----------------------------------");
        calculateForTheWholeLib(tree, FILE_PATH);
    }


}
