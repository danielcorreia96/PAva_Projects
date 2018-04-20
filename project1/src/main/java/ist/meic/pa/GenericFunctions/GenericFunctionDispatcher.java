package ist.meic.pa.GenericFunctions;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

/**
 *
 * The GenericFunctionDispatcher is an utility class responsible for handling the multiple dispatch logic of generic functions.
 *
 * Core Features
 *  1. Nested HashMaps containing information on available GenericFunction methods (before, primary, after).
 *      Each map has 3 levels using different keys as parameters to support several features:
 *          Level 1 (Class): support multiple GenericFunction annotated classes
 *          Level 2 (String): support multiple method names inside a GenericFunction annotated class.
 *          Level 3 (String): support multiple parameters types combinations for the same method name.
 *
 *      Note: In Level 3, the key is built by joining the String qualified names of the parameters types using a char "#" as delimiter
 *            For example, for a method with two parameters String and Integer, the key is "java.lang.String#java.lang.Integer"
 *
 *  2. A public method (invokeGenericFunction) responsible for invoking a generic function method according to CLOS.
 *      This method should be used to replace GenericFunctions method calls in a application using Javassist.
 *
 */
public final class GenericFunctionDispatcher {
    private GenericFunctionDispatcher() {
        // This is an utility class, which means it should never be instantiated.
        // Thus, the class is final and has an empty private constructor.
    }

    // Nested maps containing information on available GenericFunction methods (before, primary, after)
    private static HashMap<Class, HashMap<String, HashMap<String, Method>>> gFunPrimaryMap = new HashMap<>();
    private static HashMap<Class, HashMap<String, HashMap<String, Method>>> gFunBeforeMap = new HashMap<>();
    private static HashMap<Class, HashMap<String, HashMap<String, Method>>> gFunAfterMap = new HashMap<>();

    private static String buildParamsId(List<String> params){
        // Encapsulate parameters id building logic
        return String.join("#", params);
    }

    private static Object invokeMethod(Method method, Object[] args)  {
        // Encapsulate mandatory try/catch when invoking a method with reflection
        try {
            return method.invoke(null, args);
        }
        catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static HashMap<String, HashMap<String, Method>> getInitMapForAnnotation(Class clazz, Class<? extends Annotation> annotationClass) {
        HashMap<String, HashMap<String, Method>> clazz_methods = new HashMap<>();
        for (Method method : clazz.getDeclaredMethods()) {
            if (annotationClass == null && method.getAnnotations().length != 0) {
                // annotationClass == null -> primary methods
                // this if case represents "looking for primary methods, skip any auxiliary method (method that has annotations)"
                continue;
            }
            if (annotationClass == null || method.isAnnotationPresent(annotationClass)) {
                // All generic functions should be public according to CLOS
                method.setAccessible(true);

                // Build parameters id to add to map
                List<String> paramsClassNames = Lists.transform(Arrays.asList(method.getParameterTypes()), Class::getName);
                String paramsId = buildParamsId(paramsClassNames);

                if (clazz_methods.containsKey(method.getName())) {
                    // Method already exists -> add a new parameters types map
                    clazz_methods.get(method.getName()).put(paramsId, method);
                }
                else {
                    // Method didn't exist yet -> create map for the method and add the new parameters types map
                    clazz_methods.put(method.getName(), Maps.newHashMap(ImmutableMap.of(paramsId, method)));
                }
            }
        }
        return clazz_methods;
    }

    private static List<String> getParamsIdCombinations(Object[] params, boolean leastToMostSpecific) {
        // Get all possible id combinations for the input parameters
        List<String> paramsIdCombinations = CombinationsHelper.getSuperCombinations(params).stream()
                .map(combination -> Lists.transform(combination, Class::getName))
                .map(GenericFunctionDispatcher::buildParamsId)
                .collect(Collectors.toList());
        // Reverse order of precedence list if we want least to most specific ordering (e.g. after methods)
        return leastToMostSpecific ? Lists.reverse(paramsIdCombinations) : paramsIdCombinations;
    }

    private static Method getPrimaryMethod(HashMap<String, Method> primaryParamsMap, Object[] params) throws NoSuchMethodException {
        // Find most specific primary method according to input parameters types and available primary methods
        String primaryMethodId = getParamsIdCombinations(params, false).stream()
                .filter(primaryParamsMap::containsKey).findFirst()
                .orElseThrow(() -> new NoSuchMethodException("Unable to find a primary method during multiple dispatch"));
        return primaryParamsMap.get(primaryMethodId);
    }

    private static void invokeApplicableMethodsFromMap(HashMap<String, Method> paramsMap, boolean leastToMostSpecific, Object[] args){
        List<Method> applicableMethods = getParamsIdCombinations(args, leastToMostSpecific).stream()
                .map(paramsMap::get).filter(Objects::nonNull).collect(Collectors.toList());

        applicableMethods.forEach(method -> invokeMethod(method, args));
    }

    public static Object invokeGenericFunction(Class clazz, String name, Object[] args) throws NoSuchMethodException {
        if (!gFunPrimaryMap.containsKey(clazz)) {
            // If it's the first time the dispatcher sees this class, then initialize all methods maps for this class (before, primary, after)
            gFunPrimaryMap.put(clazz, getInitMapForAnnotation(clazz, null));
            gFunBeforeMap.put(clazz, getInitMapForAnnotation(clazz, BeforeMethod.class));
            gFunAfterMap.put(clazz, getInitMapForAnnotation(clazz, AfterMethod.class));
        }
        // Call applicable before methods (order: most specific to least specific)
        Optional.ofNullable(gFunBeforeMap.get(clazz).get(name))
                .ifPresent(beforeParamsMap -> invokeApplicableMethodsFromMap(beforeParamsMap, false, args));

        // Call most specific primary method
        Method primaryMethod = getPrimaryMethod(gFunPrimaryMap.get(clazz).get(name), args);
        Object invocation_result = invokeMethod(primaryMethod, args);

        // Call applicable after methods (order: least specific to most specific)
        Optional.ofNullable(gFunAfterMap.get(clazz).get(name))
                .ifPresent(afterParamsMap -> invokeApplicableMethodsFromMap(afterParamsMap, true, args));

        return invocation_result;
    }
}