package ist.meic.pa.GenericFunctions;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

public final class GenericFunctionDispatcher {
    private GenericFunctionDispatcher(){
        // This an utility class, which means it should never be instantiated
    }

    private static HashMap<Class, HashMap<String, HashMap<String, Method>>> gFunBaseMap = new HashMap<>();
    private static HashMap<Class, HashMap<String, HashMap<String, Method>>> gFunBeforeMap = new HashMap<>();
    private static HashMap<Class, HashMap<String, HashMap<String, Method>>> gFunAfterMap = new HashMap<>();


    private static HashMap<String, HashMap<String, Method>> getInitTableForAnnotation(Class clazz, Class<? extends Annotation> annotationClass){
        HashMap<String, HashMap<String, Method>> clazz_methods = new HashMap<>();

        for (Method method : clazz.getDeclaredMethods()) {
            if (annotationClass == null && method.getAnnotations().length != 0) {
                // annotationClass == null -> primary methods
                // this if case represents "looking for primary methods, skip any auxiliary method"
                continue;
            }
            if (annotationClass == null || method.isAnnotationPresent(annotationClass)){
                method.setAccessible(true);
                String params_id = Arrays.stream(method.getParameterTypes())
                        .map(Class::getName).collect(Collectors.joining("#"));

                if (clazz_methods.containsKey(method.getName())){
                    HashMap<String, Method> params_method = clazz_methods.get(method.getName());
                    params_method.put(params_id,method);
                }
                else {
                    HashMap<String, Method> params_method = new HashMap<>();
                    params_method.put(params_id,method);
                    clazz_methods.put(method.getName(), params_method);
                }
            }
        }
        return clazz_methods;
    }

    private static List<List<Class>> getNextCombinations(List<List<Class>> current_combs, List<Class> tmp) {
        List<List<Class>> next_combs = new ArrayList<>();
        for (List<Class> current_comb : current_combs) {
            for (Class aClass : tmp) {
                List<Class> comb = new ArrayList<>(current_comb);
                comb.add(aClass);
                next_combs.add(comb);
            }
        }
        return next_combs;
    }

    private static List<List<Class>> getSuperCombinations(Object[] params) {
        List<Class> param_classes = Arrays.stream(params).map(Object::getClass).collect(Collectors.toList());

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

        List<List<Class>> combinations = new ArrayList<>();
        for (List<Class> params_all_super : params_all_supers) {
            if (combinations.isEmpty()) {
                combinations = params_all_super.stream().map(aClass -> new ArrayList<>(Collections.singleton(aClass))).collect(Collectors.toList());
            }
            else {
                combinations = getNextCombinations(combinations, params_all_super);
            }
        }
        return combinations;
    }

    private static List<List<String>> getParamsClassNames(Object[] params){
        return getSuperCombinations(params).stream()
                .map(combination -> combination.stream().map(Class::getName).collect(Collectors.toList()))
                .collect(Collectors.toList());
    }

    private static Method getMethodBySuperclasses(HashMap<String, Method> paramsMap, Object[] params) throws NoSuchMethodException {
        List<List<String>> paramsClassNames = getParamsClassNames(params);
        return paramsMap.get(paramsClassNames.stream()
                .map(strings -> String.join("#", strings)).filter(paramsMap::containsKey).findFirst()
                .orElseThrow(() -> new NoSuchMethodException("Unable to find a primary method during multiple dispatch")));
    }

    private static void invokeAllValidMethodsFromMap(HashMap<String, Method> paramsMap, boolean leastToMostSpecific, Object[] args) throws InvocationTargetException, IllegalAccessException {
        if (paramsMap == null) {
            return;
        }
        List<List<String>> param_classnames = getParamsClassNames(args);
        if (leastToMostSpecific) {
            Collections.reverse(param_classnames);
        }
        for (List<String> paramClasses : param_classnames) {
            String joined = String.join("#", paramClasses);
            if (paramsMap.containsKey(joined)){
                paramsMap.get(joined).invoke(null, args);
            }
        }
    }

    public static Object invokeSpecific(Class clazz, String name, Object[] args) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        if (!gFunBaseMap.containsKey(clazz)) {
            gFunBaseMap.put(clazz, getInitTableForAnnotation(clazz, null));
            gFunBeforeMap.put(clazz, getInitTableForAnnotation(clazz, BeforeMethod.class));
            gFunAfterMap.put(clazz, getInitTableForAnnotation(clazz, AfterMethod.class));
        }
        HashMap<String, Method> paramsMap = gFunBaseMap.get(clazz).get(name);
        Method superMethod = getMethodBySuperclasses(paramsMap, args);

        invokeAllValidMethodsFromMap(gFunBeforeMap.get(clazz).get(name), false, args);
        Object invocation_result = superMethod.invoke(null, args);
        invokeAllValidMethodsFromMap(gFunAfterMap.get(clazz).get(name), true, args);

        return invocation_result;
    }
}
