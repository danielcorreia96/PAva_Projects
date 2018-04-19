package ist.meic.pa.GenericFunctions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public final class CombinationsHelper {
    private CombinationsHelper() {
        // This an utility class, which means it should never be instantiated
    }

    private static List<List<Class>> getNextCombinations(List<List<Class>> current_combs, List<Class> tmp) {
        List<List<Class>> next_combs = new ArrayList<>();

        for (List<Class> current_comb : current_combs) {
            // 1. For each current combination
            for (Class aClass : tmp) {
                // 2. Make a new list containing the current combination + an element from tmp and add it to the result
                List<Class> comb = new ArrayList<>(current_comb);
                comb.add(aClass);
                next_combs.add(comb);
            }
        }
        return next_combs;
    }

    private static List<List<Class>> getSuperCombinations(Object[] params) {
        // 1. Convert params Object[] to list of Classes
        List<Class> param_classes = Arrays.stream(params).map(Object::getClass).collect(Collectors.toList());

        // 2. Build list of sublists where each sublist contains the whole class tree for a parameter until Object.class
        // Example: two parameters: (1) String and (2) Integer
        //  returns [ [java.lang.String, interface Comparable, ... , Object], [Integer, ... , Number, ..., Object] ]
        List<List<Class>> params_all_supers = new ArrayList<>();
        for (Class param_class : param_classes) {
            List<Class> class_tree = new ArrayList<>();
            class_tree.add(param_class);
            class_tree.addAll(Arrays.asList(param_class.getInterfaces()));

            while (!param_class.equals(Object.class)) {
                param_class = param_class.getSuperclass();
                class_tree.add(param_class);
                class_tree.addAll(Arrays.asList(param_class.getInterfaces()));
            }
            params_all_supers.add(class_tree);
        }

        // 3. Generate combinations of sublists
        List<List<Class>> combinations = new ArrayList<>();
        for (List<Class> params_all_super : params_all_supers) {
            if (combinations.isEmpty()) {
                // Initial case: spread the first param sublist into multiple sublists
                combinations = params_all_super.stream().map(aClass -> new ArrayList<>(Collections.singleton(aClass))).collect(Collectors.toList());
            }
            else {
                combinations = getNextCombinations(combinations, params_all_super);
            }
        }
        return combinations;
    }

    public static List<List<String>> getParamsClassNames(Object[] params) {
        return getSuperCombinations(params).stream()
                .map(combination -> combination.stream().map(Class::getName).collect(Collectors.toList()))
                .collect(Collectors.toList());
    }
}
