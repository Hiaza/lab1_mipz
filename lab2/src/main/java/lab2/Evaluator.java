package lab2;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import org.javatuples.Pair;
import org.javatuples.Triplet;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class Evaluator {
    public static List<Node> getTheCombinedNodeListFromAllLib(String pathToLib) throws IOException {
        Path dir = Paths.get(pathToLib);
        List<Node> nodes = new ArrayList<>();
        Files.walk(dir).forEach(path -> {
            try {
                AddFilesChildren(path.toFile(), nodes);
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
        });
        return nodes;
    }

    static void AddFilesChildren(File file, List<Node> nodes) throws FileNotFoundException {
        if (!file.isDirectory()) {
            if (file.getAbsolutePath().endsWith(".java")){
                CompilationUnit cu = StaticJavaParser.parse(file);
                nodes.addAll(cu.getChildNodes());
            }
        }
    }
    public static List<TreeNode<Node>> constructTheTree(List<Node> temp){
        List<TreeNode<Node>> resultList = new ArrayList<>();
        Map<Node, String> map = new HashMap<>();
        List<Node> list = new ArrayList<>(temp);
        for (Node x: temp) {
            if (x.getClass()==com.github.javaparser.ast.body.ClassOrInterfaceDeclaration.class) {
                if (!((ClassOrInterfaceDeclaration)x).getExtendedTypes().isEmpty()){
                    map.put(x, ((ClassOrInterfaceDeclaration)x).getExtendedTypes().get(0).getName().getIdentifier());
                    list.remove(x);
                }
            }
        }
        for (Node x: list) {
            if (x.getClass()==com.github.javaparser.ast.body.ClassOrInterfaceDeclaration.class) {
                List<TreeNode<Node>> children = new ArrayList<>();
                TreeNode<Node> root = new TreeNode<>(x);
                children.add(root);
                while (!children.isEmpty()) {
                    TreeNode<Node> curr = children.get(0);
                    map.entrySet().stream().filter(entry -> entry.getValue().equals(((ClassOrInterfaceDeclaration) curr.data).getName().getIdentifier())).map(Map.Entry::getKey).forEach(child -> {
                        children.add(curr.addChild(child));
                    });
                    children.remove(curr);
                }
                resultList.add(root);
            }
        }
        return resultList;
    }

    static HashMap<String, Pair<Integer,Integer>> getAllParametersForSpecificClass(List<TreeNode<Node>> resultList, String name){
        TreeNode<Node> res = null;
        for (TreeNode<Node> node: resultList) {
            res = node.findTreeNode(((treeData) -> ((ClassOrInterfaceDeclaration)treeData)
                    .getName().getIdentifier().equals(name) ? 0 : 1));
            if (res != null) break;
        }
        if(res == null) throw new IllegalArgumentException("No such class");
        Triplet<HashMap<Pair<String, NodeList>, MethodDeclaration>,
                HashMap<String, FieldDeclaration>,
                Integer> pair = getInheritedMethodsAndFields(res, resultList);

        HashMap<Pair<String,NodeList>, MethodDeclaration> InheritedMethods = pair.getValue0();
        HashMap<Pair<String,NodeList>, MethodDeclaration> ImplementedMethods = new HashMap<>();
        HashMap<Pair<String,NodeList>, MethodDeclaration> privateMethods = new HashMap<>();

        ClassOrInterfaceDeclaration cd = (ClassOrInterfaceDeclaration) res.data;
        for (MethodDeclaration method:cd.getMethods()) {
            if (!method.getModifiers().isEmpty() && (
                    method.getModifiers().get(0).getKeyword().asString().equals("abstract")
                            || method.getModifiers().get(0).getKeyword().asString().equals("public"))){
                ImplementedMethods.putIfAbsent(new Pair<>(method.getNameAsString(),method.getTypeParameters()),method);
            } else if (!method.getModifiers().isEmpty()) {
                privateMethods.putIfAbsent(new Pair<>(method.getNameAsString(),method.getTypeParameters()),method);
            }
        }
        HashMap<String, FieldDeclaration> allInheritedFields = pair.getValue1();
        HashMap<String, FieldDeclaration> allPublicFields = new HashMap<>();
        HashMap<String, FieldDeclaration> allPrivateFields = new HashMap<>();

        for (FieldDeclaration field:cd.getFields()) {
            if (!field.getModifiers().isEmpty() && (
                    field.getModifiers().get(0).getKeyword().asString().equals("abstract")
                            || field.getModifiers().get(0).getKeyword().asString().equals("public"))){
                allPublicFields.putIfAbsent(field.getVariables().get(0).getNameAsString(),field);
            } else allPrivateFields.putIfAbsent(field.getVariables().get(0).getNameAsString(),field);
        }

        int numOfInheritedAndOverriddenMethods = (int)InheritedMethods.keySet().stream().filter(ImplementedMethods::containsKey).count();
        int numOfNotOverriddenMethods = InheritedMethods.size() - numOfInheritedAndOverriddenMethods;
        int numOfImplementedMethods = ImplementedMethods.size();
        int numOfPrivateMethods = privateMethods.size();

        int numOfInheritedAndOverriddenFields = (int)allInheritedFields.keySet().stream().filter(allPublicFields::containsKey).count();
        int numOfNotOverriddenFields = allInheritedFields.size() - numOfInheritedAndOverriddenFields;
        int numOfCreatedFields = allPublicFields.size();
        int numOfPrivateFields = allPrivateFields.size();

        HashMap<String, Pair<Integer,Integer>> results = new HashMap<>();


        Pair<Integer,Integer> DTI = new Pair<>(pair.getValue2(), res.children.size());
        Pair<Integer,Integer> MHF = new Pair<>(numOfPrivateMethods, numOfImplementedMethods + numOfPrivateMethods);
        Pair<Integer,Integer> AHF = new Pair<>(numOfPrivateFields, numOfCreatedFields + numOfPrivateFields);
        Pair<Integer,Integer> MIF = new Pair<>(numOfNotOverriddenMethods, numOfImplementedMethods + numOfNotOverriddenMethods + numOfPrivateMethods);
        Pair<Integer,Integer> AIF = new Pair<>(numOfNotOverriddenFields, numOfCreatedFields + numOfNotOverriddenFields + numOfPrivateFields);
        Pair<Integer,Integer> POF = new Pair<>(numOfNotOverriddenMethods, (numOfImplementedMethods + numOfPrivateMethods) * res.children.size());

        results.put("DTI", DTI);
        results.put("MHF", MHF);
        results.put("AHF", AHF);
        results.put("MIF", MIF);
        results.put("AIF", AIF);
        results.put("POF", POF);

        return results;
    }

    static Triplet<HashMap<Pair<String,NodeList>, MethodDeclaration>, HashMap<String, FieldDeclaration>, Integer> getInheritedMethodsAndFields(TreeNode<Node> res, List<TreeNode<Node>> resultList){
        HashMap<Pair<String,NodeList>, MethodDeclaration> InheritedMethods = new HashMap<>();
        HashMap<String, FieldDeclaration> allInheritedFields = new HashMap<>();

        if(!((ClassOrInterfaceDeclaration)res.data).getImplementedTypes().isEmpty()){
            for (Node intf:((ClassOrInterfaceDeclaration)res.data).getImplementedTypes()) {
                String intfName = ((ClassOrInterfaceType)intf).getName().getIdentifier();
                fillMapsFromInterfaces(resultList, InheritedMethods, allInheritedFields, intfName);
            }
        }
        TreeNode<Node> temp = res.parent;
        int DTI = 1;
        while (temp != null){
            ClassOrInterfaceDeclaration cd = (ClassOrInterfaceDeclaration) temp.data;
            for (MethodDeclaration method:cd.getMethods()) {
                if (!method.getModifiers().isEmpty() && (
                        method.getModifiers().get(0).getKeyword().asString().equals("abstract")
                                || method.getModifiers().get(0).getKeyword().asString().equals("public"))){
                    InheritedMethods.putIfAbsent(new Pair<>(method.getNameAsString(),method.getTypeParameters()),method);
                }
            }
            for (FieldDeclaration field:cd.getFields()) {
                if (!field.getModifiers().isEmpty() && (
                        field.getModifiers().get(0).getKeyword().asString().equals("public"))){
                    allInheritedFields.putIfAbsent(field.getVariables().get(0).getNameAsString(),field);
                }
            }
            for (ClassOrInterfaceType intf:((ClassOrInterfaceDeclaration)temp.data).getImplementedTypes()) {
                String intfName = intf.getName().getIdentifier();
                fillMapsFromInterfaces(resultList, InheritedMethods, allInheritedFields, intfName);
            }
            temp = temp.parent;
            DTI++;
        }
        return new Triplet<>(InheritedMethods, allInheritedFields, DTI);
    }

    private static void fillMapsFromInterfaces(List<TreeNode<Node>> resultList, HashMap<Pair<String, NodeList>, MethodDeclaration> inheritedMethods, HashMap<String, FieldDeclaration> allInheritedFields, String intfName) {
        ClassOrInterfaceDeclaration cd_intf = (ClassOrInterfaceDeclaration)resultList.stream()
                .filter(nodeTreeNode -> ((ClassOrInterfaceDeclaration)nodeTreeNode.data)
                        .getName()
                        .getIdentifier()
                        .equals(intfName))
                .findFirst().get().data;
        for (MethodDeclaration method:cd_intf.getMethods()) {
            inheritedMethods.putIfAbsent(new Pair<>(method.getNameAsString(),method.getTypeParameters()),method);
        }
        for (FieldDeclaration field:cd_intf.getFields()) {
            allInheritedFields.putIfAbsent(field.getVariables().get(0).getNameAsString(),field);
        }
    }

    public static void calculateForSingleClass(List<TreeNode<Node>> resultList, String name){
        HashMap<String, Pair<Integer,Integer>> results = getAllParametersForSpecificClass(resultList,name);
        System.out.println("For single class: " + name);
        System.out.println("DTI: " + results.get("DTI").getValue0());
        System.out.println("Children: " + results.get("DTI").getValue1());
        printFactors(results);
    }

    public static void calculateForTheWholeLib(List<TreeNode<Node>> resultList, String path) {
        HashMap<String, Pair<Integer,Integer>> results = new HashMap<>();
        for (TreeNode<Node> node: resultList) {
            Queue<TreeNode<Node>> traversalList = new LinkedList<>();
            traversalList.add(node);
            while (!traversalList.isEmpty()) {
                TreeNode<Node> temp = traversalList.poll();
                traversalList.addAll(temp.children);
                String className = ((ClassOrInterfaceDeclaration) temp.data).getName().getIdentifier();
                HashMap<String, Pair<Integer, Integer>> class_results = getAllParametersForSpecificClass(resultList, className);
                updateResults(results, class_results);
            }
        }
        System.out.println("For the whole lib: " + path);
        System.out.println("max DTI: " + results.get("DTI").getValue0());
        System.out.println("max children: " + results.get("DTI").getValue1());
        printFactors(results);
    }

    private static void printFactors(HashMap<String, Pair<Integer, Integer>> results) {
        System.out.println("MHF: " + (results.get("MHF").getValue1() != 0
                ? results.get("MHF").getValue0() * 100f / results.get("MHF").getValue1()
                : "can't be calculated"));
        System.out.println("AHF: " + (results.get("AHF").getValue1() != 0
                ? results.get("AHF").getValue0() * 100f / results.get("AHF").getValue1()
                : "can't be calculated"));
        System.out.println("MIF: " + (results.get("MIF").getValue1() != 0
                ? results.get("MIF").getValue0() * 100f / results.get("MIF").getValue1()
                : "can't be calculated"));
        System.out.println("AIF: " + (results.get("AIF").getValue1() != 0
                ? results.get("AIF").getValue0() * 100f / results.get("AIF").getValue1()
                : "can't be calculated"));
        System.out.println("POF: " + (results.get("POF").getValue1() != 0
                ? results.get("POF").getValue0() * 100f / results.get("POF").getValue1()
                : "can't be calculated"));
    }

    static void updateResults(HashMap<String, Pair<Integer,Integer>> storage, HashMap<String, Pair<Integer,Integer>> newInfo){
        if(!storage.containsKey("DTI")) {
            storage.putAll(newInfo);
            return;
        }
        for (Map.Entry<String, Pair<Integer,Integer>> entry:newInfo.entrySet()) {
            if(entry.getKey().equals("DTI")) {
                Pair<Integer, Integer> storedPair = storage.get("DTI");
                Pair<Integer, Integer> newPair = new Pair<>(
                        storedPair.getValue0() > entry.getValue().getValue0() ? storedPair.getValue0() : entry.getValue().getValue0(),
                        storedPair.getValue1() > entry.getValue().getValue1() ? storedPair.getValue1() : entry.getValue().getValue1()
                );
                storage.replace(entry.getKey(),newPair);
            }
            else {
                Pair<Integer, Integer> storedPair = storage.get(entry.getKey());
                Pair<Integer, Integer> newPair = new Pair<>(
                        storedPair.getValue0() + entry.getValue().getValue0(),
                        storedPair.getValue1() + entry.getValue().getValue1()
                );
                storage.replace(entry.getKey(), newPair);
            }
        }
    }
}
