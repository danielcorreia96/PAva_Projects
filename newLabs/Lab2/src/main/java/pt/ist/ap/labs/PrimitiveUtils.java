package pt.ist.ap.labs;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

public class PrimitiveUtils {
    private static final Map<String, Class<?>> PRIMITIVES_MAP = new HashMap<>(){{
        put("int", Integer.class);
        put("double", Double.class);
        put("float", Float.class);
        put("char", Character.class);
        put("boolean", Boolean.class);
        put("byte", Byte.class);
        put("long", Long.class);
        put("short", Short.class);
        put("void", Void.class);
    }};
    private static final Map<String, Class<?>> PRIMITIVES_CLASSES = new HashMap<>(){{
        put("int", int.class);
        put("double", double.class);
        put("float", float.class);
        put("char", char.class);
        put("boolean", boolean.class);
        put("byte", byte.class);
        put("long", long.class);
        put("short", short.class);
        put("void", void.class);
    }};

    static Class getTypeFromArg(String arg){
        if (PRIMITIVES_CLASSES.containsKey(arg)){
            return PRIMITIVES_CLASSES.get(arg);
        }
        else{
            try {
                return Class.forName(arg);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    static Object getParamFromArgsPair(String type, String arg){
        try {
            if (PRIMITIVES_MAP.containsKey(type)){
                return PRIMITIVES_MAP.get(type).getMethod("valueOf",String.class).invoke(PRIMITIVES_MAP.get(type), arg);
            }
            else {
                return Class.forName(type).getConstructor(String.class).newInstance(arg);
            }
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException | InstantiationException e) {
            e.printStackTrace();
        }
        return null;
    }
}
