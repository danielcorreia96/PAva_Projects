package ist.meic.pa.GenericFunctionsExtended;

import com.google.common.collect.Lists;
import ist.meic.pa.GenericFunctions.AfterMethod;
import ist.meic.pa.GenericFunctions.BeforeMethod;
import ist.meic.pa.GenericFunctions.CombinationsHelper;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

public final class GenericFunctionDispatcher {
    private GenericFunctionDispatcher(){
        // This an utility class, which means it should never be instantiated
    }

    private static HashMap<Class, HashMap<String, HashMap<String, HashMap>>> gFunPrimaryMap = new HashMap<>();
    private static HashMap<Class, HashMap<String, HashMap<String, HashMap>>> gFunBeforeMap = new HashMap<>();
    private static HashMap<Class, HashMap<String, HashMap<String, HashMap>>> gFunAfterMap = new HashMap<>();
    private static HashMap<Class, HashMap<String, HashMap<String, HashMap>>> gFunAroundMap = new HashMap<>();
    private static List<String> usedAroundMethods = new ArrayList<>();

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

    private static HashMap<String, HashMap<String, HashMap>> getInitTableForAnnotation(Class clazz, Class<? extends Annotation> annotationClass){
        HashMap<String, HashMap<String, HashMap>> clazz_methods = new HashMap<>();

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
           
                HashMap<String, Object> methodcache = new HashMap<>();
                methodcache.put("cached", new HashMap<>());
                methodcache.put("method", method);
                if (clazz_methods.containsKey(method.getName())){
                    HashMap<String, HashMap> params_method = clazz_methods.get(method.getName());
                    params_method.put(params_id, methodcache);
                }
                else {
                    HashMap<String, HashMap> params_method = new HashMap<>();
                    params_method.put(params_id,methodcache);
                    clazz_methods.put(method.getName(), params_method);
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

    private static HashMap getPrimaryMethod(HashMap<String, HashMap> primaryParamsMap, Object[] params) throws NoSuchMethodException {
        // Find most specific primary method according to input parameters types and available primary methods
        return primaryParamsMap.get(getParamsIdCombinations(params, false).stream()
                .filter(primaryParamsMap::containsKey).findFirst()
                .orElseThrow(() -> new NoSuchMethodException("Unable to find a primary method during multiple dispatch"))
        );
    }

    private static List<Method> invokeApplicableMethodsFromMap(HashMap<String, HashMap> paramsMap, boolean leastToMostSpecific, Object[] args) {
        List<Method> applicableMethods = getParamsIdCombinations(args, leastToMostSpecific).stream()
                .map(paramsMap::get).filter(Objects::nonNull)
                .map(validMap -> (Method) validMap.get("method")).collect(Collectors.toList());

        applicableMethods.forEach(method -> invokeMethod(method, args));
        return applicableMethods;
    }

    private static HashMap getAroundMethodBySuperClasses(HashMap<String, HashMap> paramsMap, Object[] params) {
        for (String paramsId : GenericFunctionDispatcher.getParamsIdCombinations(params, false)) {
            if (paramsMap.containsKey(paramsId) && !usedAroundMethods.contains(paramsId)){
                HashMap methodCacheMap = paramsMap.get(paramsId);
                usedAroundMethods.add(paramsId);
                return methodCacheMap;
            }
        }
        return null;
    }

    public static Object invokeSpecific(Class clazz, String name, Object[] args) throws NoSuchMethodException {
        if (!gFunPrimaryMap.containsKey(clazz)) {
            gFunPrimaryMap.put(clazz, getInitTableForAnnotation(clazz, null));
            gFunBeforeMap.put(clazz, getInitTableForAnnotation(clazz, BeforeMethod.class));
            gFunAfterMap.put(clazz, getInitTableForAnnotation(clazz, AfterMethod.class));
            gFunAroundMap.put(clazz, getInitTableForAnnotation(clazz, AroundMethod.class));
        }

        HashMap<String, HashMap> aroundParamsMap = gFunAroundMap.get(clazz).get(name);
        if (aroundParamsMap != null) {
            HashMap aroundMethodMap = getAroundMethodBySuperClasses(aroundParamsMap, args);
            if (aroundMethodMap != null && aroundMethodMap.get("method") != null){
                Method aroundMethod = (Method) aroundMethodMap.get("method");
                return invokeMethod(aroundMethod, args);
            }
        }
        return invokeApplicableMethods(clazz, name, args);
    }

    public static Object callNextMethod(Class clazz, String name, Object[] args) throws NoSuchMethodException {
        Object[] actual_args = (Object[]) args[0];
        HashMap<String, HashMap> aroundParamsMap = gFunAroundMap.get(clazz).get(name);
        HashMap aroundMethodMap = getAroundMethodBySuperClasses(aroundParamsMap, args);

        if (aroundMethodMap != null && !aroundMethodMap.isEmpty()) {
            // if i found an aroundmethod, then i remove it from used before going recursively with invokeSpecific, because i'm an idiot :)
            usedAroundMethods.remove(usedAroundMethods.size()-1);
            Object result = invokeSpecific(clazz, name, actual_args);
            usedAroundMethods.remove(usedAroundMethods.size()-1);
            return result;
        }
        else {
            Object result = invokeApplicableMethods(clazz, name, actual_args);
            usedAroundMethods.remove(usedAroundMethods.size()-1);
            return result;
        }

    }

    private static Object invokeApplicableMethods(Class clazz, String name, Object[] args) throws NoSuchMethodException {
        HashMap primaryMethodMap = getPrimaryMethod(gFunPrimaryMap.get(clazz).get(name), args);
        HashMap<String, Object> cachedMethodMap = (HashMap<String, Object>) primaryMethodMap.get("cached");

        if (cachedMethodMap.isEmpty()){
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
//            System.err.println("using cached effective method");
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
