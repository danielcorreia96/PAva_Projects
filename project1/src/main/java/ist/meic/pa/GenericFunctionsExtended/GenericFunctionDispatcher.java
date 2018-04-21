package ist.meic.pa.GenericFunctionsExtended;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import ist.meic.pa.GenericFunctions.AfterMethod;
import ist.meic.pa.GenericFunctions.BeforeMethod;
import ist.meic.pa.GenericFunctions.CombinationsHelper;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public final class GenericFunctionDispatcher {
    private GenericFunctionDispatcher(){
        // This is an utility class, which means it should never be instantiated.
        // Thus, the class is final and has an empty private constructor.
    }

    private static HashMap<Class, HashMap<String, HashMap<String, HashMap>>> gFunPrimaryMap = new HashMap<>();
    private static HashMap<Class, HashMap<String, HashMap<String, HashMap>>> gFunBeforeMap = new HashMap<>();
    private static HashMap<Class, HashMap<String, HashMap<String, HashMap>>> gFunAfterMap = new HashMap<>();

    private static String buildParamsId(List<String> params){
        // Encapsulate parameters id building logic
        return String.join("#", params);
    }

    public static Object invokeMethod(Method method, Object[] args)  {
        // Encapsulate mandatory try/catch when invoking a method with reflection
        try {
            return method.invoke(null, args);
        }
        catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static HashMap<String, HashMap<String, HashMap>> getInitMapForAnnotation(Class clazz, Class<? extends Annotation> annotationClass){
        HashMap<String, HashMap<String, HashMap>> clazz_methods = new HashMap<>();

        for (Method method : clazz.getDeclaredMethods()) {
            if (annotationClass == null && method.getAnnotations().length != 0) {
                // annotationClass == null -> primary methods
                // this if case represents "looking for primary methods, skip any auxiliary method"
                continue;
            }
            if (annotationClass == null || method.isAnnotationPresent(annotationClass)){
                method.setAccessible(true);
                List<String> paramsClassNames = Lists.transform(Arrays.asList(method.getParameterTypes()), Class::getName);
                String paramsId = buildParamsId(paramsClassNames);
                String methodName = method.getName();

                HashMap<String, Object> methodCache = Maps.newHashMap(ImmutableMap.of("cached", Maps.newHashMap(), "method", method));
                if (clazz_methods.containsKey(methodName)){
                    clazz_methods.get(methodName).put(paramsId, methodCache);
                }
                else {
                    clazz_methods.put(methodName, Maps.newHashMap(ImmutableMap.of(paramsId, methodCache)));
                }
            }
        }
        return clazz_methods;
    }

    protected static List<String> getParamsIdCombinations(Object[] params, boolean leastToMostSpecific) {
        // Get all possible id combinations for the input parameters
        List<String> paramsIdCombinations = CombinationsHelper.getSuperCombinations(params).stream()
                .map(combination -> Lists.transform(combination, Class::getName))
                .map(GenericFunctionDispatcher::buildParamsId)
                .collect(Collectors.toList());
        // Reverse order of precedence list if we want least to most specific ordering (e.g. after methods)
        return leastToMostSpecific ? Lists.reverse(paramsIdCombinations) : paramsIdCombinations;
    }

    private static HashMap getPrimaryMethod(HashMap<String, HashMap> primaryParamsMap, Object[] params) throws NoSuchMethodException {
        // Find most specific primary method according to input parameters types and available primary methods
        return getParamsIdCombinations(params, false).stream()
                .filter(primaryParamsMap::containsKey)
                .findFirst().map(primaryParamsMap::get)
                .orElseThrow(() -> new NoSuchMethodException("Unable to find a primary method during multiple dispatch"));
    }

    private static List<Method> invokeApplicableMethodsFromMap(HashMap<String, HashMap> paramsMap, boolean leastToMostSpecific, Object[] args) {
        List<Method> applicableMethods = getParamsIdCombinations(args, leastToMostSpecific).stream()
                .filter(paramsMap::containsKey).map(paramsMap::get)
                .map(validMap -> (Method) validMap.get("method"))
                .collect(Collectors.toList());

        applicableMethods.forEach(method -> invokeMethod(method, args));
        return applicableMethods;
    }

    public static Object invokeGenericFunction(Class clazz, String name, Object[] args) throws NoSuchMethodException {
        if (!gFunPrimaryMap.containsKey(clazz)) {
            gFunPrimaryMap.put(clazz, getInitMapForAnnotation(clazz, null));
            gFunBeforeMap.put(clazz, getInitMapForAnnotation(clazz, BeforeMethod.class));
            gFunAfterMap.put(clazz, getInitMapForAnnotation(clazz, AfterMethod.class));
            AroundMethodHelper.initializeAroundMethodMap(clazz);
        }
        return AroundMethodHelper.tryInvokeAroundMethod(clazz, name, args);
    }

    public static Object invokeApplicableMethods(Class clazz, String name, Object[] args) throws NoSuchMethodException {
        HashMap primaryMethodMap = getPrimaryMethod(gFunPrimaryMap.get(clazz).get(name), args);
        HashMap<String, Object> cachedMethodMap = (HashMap<String, Object>) primaryMethodMap.get("cached");
        if (cachedMethodMap.isEmpty()){
            // Call applicable before methods (order: most specific to least specific)
            if (gFunBeforeMap.get(clazz).get(name) != null){
                List<Method> beforeMethods = invokeApplicableMethodsFromMap(gFunBeforeMap.get(clazz).get(name), false, args);
                cachedMethodMap.put("before", beforeMethods);
            }

            // Call most specific primary method
            Method primaryMethod = (Method) getPrimaryMethod(gFunPrimaryMap.get(clazz).get(name), args).get("method");
            Object invocation_result = invokeMethod(primaryMethod, args);
            cachedMethodMap.put("base", primaryMethod);

            // Call applicable after methods (order: least specific to most specific)
            if (gFunAfterMap.get(clazz).get(name) != null) {
                List<Method> afterMethods = invokeApplicableMethodsFromMap(gFunAfterMap.get(clazz).get(name), true, args);
                cachedMethodMap.put("after", afterMethods);
            }
            return invocation_result;
        }
        else {
//            System.out.println("--> using cached effective method :)");
            List<Method> beforeMethods = (List<Method>) cachedMethodMap.get("before");
            Optional.ofNullable(beforeMethods).ifPresent(methods -> methods.forEach(method -> invokeMethod(method, args)));

            Method baseMethod = (Method) cachedMethodMap.get("base");
            Object base_result = invokeMethod(baseMethod, args);

            List<Method> afterMethods = (List<Method>) cachedMethodMap.get("after");
            Optional.ofNullable(afterMethods).ifPresent(methods -> methods.forEach(method -> invokeMethod(method, args)));

            return base_result;
        }
    }
}
