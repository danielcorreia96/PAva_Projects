package ist.meic.pa.GenericFunctionsExtended;

import ist.meic.pa.GenericFunctions.AfterMethod;
import ist.meic.pa.GenericFunctions.BeforeMethod;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public final class GenericFunctionDispatcher {
    private GenericFunctionDispatcher(){
        // This an utility class, which means it should never be instantiated
    }

    private static HashMap<Class, HashMap<String, HashMap<String, HashMap>>> gFunBaseMap = new HashMap<>();
    private static HashMap<Class, HashMap<String, HashMap<String, HashMap>>> gFunBeforeMap = new HashMap<>();
    private static HashMap<Class, HashMap<String, HashMap<String, HashMap>>> gFunAfterMap = new HashMap<>();
    private static HashMap<Class, HashMap<String, HashMap<String, HashMap>>> gFunAroundMap = new HashMap<>();
    private static List<String> usedAroundMethods = new ArrayList<>();

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

    private static HashMap getMethodBySuperclasses(HashMap<String, HashMap> paramsMap, Object[] params) throws NoSuchMethodException {
        List<List<String>> paramsClassNames = getParamsClassNames(params);
        return paramsMap.get(paramsClassNames.stream()
                .map(strings -> String.join("#", strings)).filter(paramsMap::containsKey).findFirst()
                .orElseThrow(() -> new NoSuchMethodException("Unable to find a primary method during multiple dispatch")));
    }

    private static List<Method> invokeAllValidMethodsFromMap(HashMap<String, HashMap> paramsMap, boolean leastToMostSpecific, Object[] args) throws InvocationTargetException, IllegalAccessException {
        List<Method> invokedMethods = new ArrayList<>();
        if (paramsMap == null) {
            return invokedMethods;
        }

        List<List<String>> param_classnames = getParamsClassNames(args);
        if (leastToMostSpecific) {
            Collections.reverse(param_classnames);
        }
        for (List<String> paramClasses : param_classnames) {
            String joined = String.join("#", paramClasses);
            if (paramsMap.containsKey(joined)){
                Method method = (Method) paramsMap.get(joined).get("method");
                method.invoke(null, args);
                invokedMethods.add(method);
            }
        }
        return invokedMethods;
    }

    private static HashMap getAroundMethodBySuperClasses(Class clazz, String name, HashMap<String, HashMap> paramsMap, Object[] params) {
        List<List<String>> paramsClassNames = getParamsClassNames(params);
        for (List<String> param : paramsClassNames) {
            String joined = String.join("#", param);
            if (paramsMap.containsKey(joined) && !usedAroundMethods.contains(joined)){
                HashMap methodcacheMap = paramsMap.get(joined);
                usedAroundMethods.add(joined);
                return methodcacheMap;
            }
        }
        return null;
    }

    public static Object invokeSpecific(Class clazz, String name, Object[] args) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        if (!gFunBaseMap.containsKey(clazz)) {
            gFunBaseMap.put(clazz, getInitTableForAnnotation(clazz, null));
            gFunBeforeMap.put(clazz, getInitTableForAnnotation(clazz, BeforeMethod.class));
            gFunAfterMap.put(clazz, getInitTableForAnnotation(clazz, AfterMethod.class));
            gFunAroundMap.put(clazz, getInitTableForAnnotation(clazz, AroundMethod.class));
        }
        HashMap<String, HashMap> paramsMap = gFunBaseMap.get(clazz).get(name);
        HashMap superMethodMap = getMethodBySuperclasses(paramsMap, args);
        HashMap<String, Object> cachedMethodMap = (HashMap<String, Object>) superMethodMap.get("cached");

        HashMap<String, HashMap> aroundParamsMap = gFunAroundMap.get(clazz).get(name);
        if (aroundParamsMap != null) {
            HashMap aroundMethodMap = getAroundMethodBySuperClasses(clazz, name, aroundParamsMap, args);
            if (aroundMethodMap != null && aroundMethodMap.get("method") != null){
                Method aroundMethod = (Method) aroundMethodMap.get("method");
                return aroundMethod.invoke(null, args);
            }
        }

        if (cachedMethodMap.isEmpty()){
//            System.err.println("going the long way...");
            List<Method> beforeMethods = invokeAllValidMethodsFromMap(gFunBeforeMap.get(clazz).get(name), false, args);
            Method superMethod = (Method) superMethodMap.get("method");
            Object invocation_result = superMethod.invoke(null, args);
            List<Method> afterMethods = invokeAllValidMethodsFromMap(gFunAfterMap.get(clazz).get(name), true, args);

            cachedMethodMap.put("before", beforeMethods);
            cachedMethodMap.put("base", superMethod);
            cachedMethodMap.put("after", afterMethods);
            return invocation_result;
        }
        else {
//            System.out.println("using cached effective method");
            List<Method> beforeMethods = (List<Method>) cachedMethodMap.get("before");
            for (Method beforeMethod : beforeMethods) {
                beforeMethod.invoke(null, args);
            }

            Method baseMethod = (Method) cachedMethodMap.get("base");
            Object base_result = baseMethod.invoke(null, args);

            List<Method> afterMethods = (List<Method>) cachedMethodMap.get("after");
            for (Method afterMethod : afterMethods) {
                afterMethod.invoke(null, args);
            }
            return base_result;
        }
    }

    public static Object callNextMethod(Class clazz, String name, Object[] args) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        Object[] actual_args = (Object[]) args[0];
        HashMap<String, HashMap> aroundParamsMap = gFunAroundMap.get(clazz).get(name);
        HashMap aroundMethodMap = getAroundMethodBySuperClasses(clazz, name, aroundParamsMap, args);

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

    private static Object invokeApplicableMethods(Class clazz, String name, Object[] args) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        HashMap<String, HashMap> paramsMap = gFunBaseMap.get(clazz).get(name);
        HashMap superMethodMap = getMethodBySuperclasses(paramsMap, args);
        HashMap<String, Object> cachedMethodMap = (HashMap<String, Object>) superMethodMap.get("cached");
        if (cachedMethodMap.isEmpty()){
//            System.err.println("going the long way...");
            List<Method> beforeMethods = invokeAllValidMethodsFromMap(gFunBeforeMap.get(clazz).get(name), false, args);
            Method superMethod = (Method) superMethodMap.get("method");
            Object invocation_result = superMethod.invoke(null, args);
            List<Method> afterMethods = invokeAllValidMethodsFromMap(gFunAfterMap.get(clazz).get(name), true, args);

            cachedMethodMap.put("before", beforeMethods);
            cachedMethodMap.put("base", superMethod);
            cachedMethodMap.put("after", afterMethods);
            return invocation_result;
        }
        else {
//            System.out.println("using cached effective method");
            List<Method> beforeMethods = (List<Method>) cachedMethodMap.get("before");
            for (Method beforeMethod : beforeMethods) {
                beforeMethod.invoke(null, args);
            }

            Method baseMethod = (Method) cachedMethodMap.get("base");
            Object base_result = baseMethod.invoke(null, args);

            List<Method> afterMethods = (List<Method>) cachedMethodMap.get("after");
            for (Method afterMethod : afterMethods) {
                afterMethod.invoke(null, args);
            }
            return base_result;
        }
    }
}
