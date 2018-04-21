package ist.meic.pa.GenericFunctionsExtended;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public final class AroundMethodHelper {
    private AroundMethodHelper(){
        // This is an utility class, which means it should never be instantiated.
        // Thus, the class is final and has an empty private constructor.
    }

    private static List<String> usedAroundMethods = new ArrayList<>();
    private static HashMap<Class, HashMap<String, HashMap<String, HashMap>>> gFunAroundMap = new HashMap<>();

    public static void initializeAroundMethodMap(Class clazz) {
        gFunAroundMap.put(clazz, GenericFunctionDispatcher.getInitMapForAnnotation(clazz, AroundMethod.class));
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

    public static Object callNextMethod(Class clazz, String name, Object[] args) throws NoSuchMethodException {
        Object[] actual_args = (Object[]) args[0];
        HashMap<String, HashMap> aroundParamsMap = gFunAroundMap.get(clazz).get(name);
        HashMap aroundMethodMap = getAroundMethodBySuperClasses(aroundParamsMap, args);

        if (aroundMethodMap != null && !aroundMethodMap.isEmpty()) {
            // if i found an around method, then i remove it from used list before going recursively with invokeGenericFunction
            // this is necessary since the getAroundMethod call will be done again in the recursive call in tryInvokeAroundMethod
            usedAroundMethods.remove(usedAroundMethods.size()-1);
            Object result = GenericFunctionDispatcher.invokeGenericFunction(clazz, name, actual_args);
            usedAroundMethods.remove(usedAroundMethods.size()-1);
            return result;
        }
        else {
            Object result = GenericFunctionDispatcher.invokeApplicableMethods(clazz, name, actual_args);
            usedAroundMethods.remove(usedAroundMethods.size()-1);
            return result;
        }

    }

    public static Object tryInvokeAroundMethod(Class clazz, String name, Object[] args) throws NoSuchMethodException {
        HashMap<String, HashMap> aroundParamsMap = gFunAroundMap.get(clazz).get(name);
        if (aroundParamsMap != null) {
            HashMap aroundMethodMap = getAroundMethodBySuperClasses(aroundParamsMap, args);
            if (aroundMethodMap != null){
                Method aroundMethod = (Method) aroundMethodMap.get("method");
                return GenericFunctionDispatcher.invokeMethod(aroundMethod, args);
            }
        }
        return GenericFunctionDispatcher.invokeApplicableMethods(clazz, name, args);
    }
}
