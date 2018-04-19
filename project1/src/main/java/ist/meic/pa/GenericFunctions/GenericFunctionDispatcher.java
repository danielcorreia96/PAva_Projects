package ist.meic.pa.GenericFunctions;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

public final class GenericFunctionDispatcher {
    private GenericFunctionDispatcher() {
        // This an utility class, which means it should never be instantiated
    }

    private static HashMap<Class, HashMap<String, HashMap<String, Method>>> gFunBaseMap = new HashMap<>();
    private static HashMap<Class, HashMap<String, HashMap<String, Method>>> gFunBeforeMap = new HashMap<>();
    private static HashMap<Class, HashMap<String, HashMap<String, Method>>> gFunAfterMap = new HashMap<>();


    private static HashMap<String, HashMap<String, Method>> getInitTableForAnnotation(Class clazz, Class<? extends Annotation> annotationClass) {
        HashMap<String, HashMap<String, Method>> clazz_methods = new HashMap<>();

        for (Method method : clazz.getDeclaredMethods()) {
            if (annotationClass == null && method.getAnnotations().length != 0) {
                // annotationClass == null -> primary methods
                // this if case represents "looking for primary methods, skip any auxiliary method"
                continue;
            }
            if (annotationClass == null || method.isAnnotationPresent(annotationClass)) {
                method.setAccessible(true);
                String params_id = Arrays.stream(method.getParameterTypes())
                        .map(Class::getName).collect(Collectors.joining("#"));

                if (clazz_methods.containsKey(method.getName())) {
                    HashMap<String, Method> params_method = clazz_methods.get(method.getName());
                    params_method.put(params_id, method);
                } else {
                    HashMap<String, Method> params_method = new HashMap<>();
                    params_method.put(params_id, method);
                    clazz_methods.put(method.getName(), params_method);
                }
            }
        }
        return clazz_methods;
    }

    private static Method getMethodBySuperclasses(HashMap<String, Method> paramsMap, Object[] params) throws NoSuchMethodException {
        List<List<String>> paramsClassNames = CombinationsHelper.getParamsClassNames(params);
        return paramsMap.get(paramsClassNames.stream()
                .map(strings -> String.join("#", strings)).filter(paramsMap::containsKey).findFirst()
                .orElseThrow(() -> new NoSuchMethodException("Unable to find a primary method during multiple dispatch")));
    }

    private static void invokeAllValidMethodsFromMap(HashMap<String, Method> paramsMap, boolean leastToMostSpecific, Object[] args) throws InvocationTargetException, IllegalAccessException {
        if (paramsMap == null) {
            return;
        }
        List<List<String>> param_classnames = CombinationsHelper.getParamsClassNames(args);
        if (leastToMostSpecific) {
            Collections.reverse(param_classnames);
        }
        for (List<String> paramClasses : param_classnames) {
            String joined = String.join("#", paramClasses);
            if (paramsMap.containsKey(joined)) {
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
