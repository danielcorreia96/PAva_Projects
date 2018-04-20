package ist.meic.pa.GenericFunctions;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 *
 * The CombinationsHelper class is an utility class responsible for handling parameters types combinations logic
 *  necessary to support multiple dispatch features.
 *
 * Core Features
 *  1. For a set of parameters, generate a list of combinations using each parameter's type class and interface hierarchy
 *
 */
public final class CombinationsHelper {
    private CombinationsHelper() {
        // This is an utility class, which means it should never be instantiated.
        // Thus, the class is final and has an empty private constructor.
    }

    private static List<List<Class>> getNextCombinations(List<List<Class>> currentCombinations, List<Class> nextParamClasses) {
        // Build the next combinations using each one of the current combinations + each class from the next parameter class tree.
        List<List<Class>> nextCombinations = new ArrayList<>();
        for (List<Class> currentCombination : currentCombinations) {
            for (Class paramClass : nextParamClasses) {
                List<Class> combination = Lists.newArrayList(currentCombination);
                combination.add(paramClass);
                nextCombinations.add(combination);
            }
        }
        return nextCombinations;
    }

    public static List<List<Class>> getSuperCombinations(Object[] params) {
        List<Class> paramsClasses = Lists.transform(Arrays.asList(params), Object::getClass);

        // Build list of sublists where each sublist contains the whole class+interface tree for a parameter until Object.class
        // Example: two parameters String and Integer
        //          returns [ [java.lang.String, interface Comparable, ... , Object], [Integer, ... , Number, ..., Object] ]
        List<List<Class>> paramsClassTrees = new ArrayList<>();
        for (Class paramClass : paramsClasses) {
            List<Class> classTree = Lists.newArrayList(Lists.asList(paramClass, paramClass.getInterfaces()));
            while (!paramClass.equals(Object.class)) {
                paramClass = paramClass.getSuperclass();
                classTree.addAll(Lists.asList(paramClass, paramClass.getInterfaces()));
            }
            paramsClassTrees.add(classTree);
        }

        // Generate combinations of sublists
        // Initial state: spread the first param sublist into multiple sublists (skip it in the for loop)
        List<List<Class>> combinations = Lists.partition(paramsClassTrees.get(0), 1);
        for (List<Class> paramClassTree : Iterables.skip(paramsClassTrees,1)) {
            combinations = getNextCombinations(combinations, paramClassTree);
        }
        return combinations;
    }

}
