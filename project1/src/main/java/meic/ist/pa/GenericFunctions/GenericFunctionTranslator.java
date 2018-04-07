package meic.ist.pa.GenericFunctions;

import javassist.*;
import javassist.expr.ExprEditor;
import javassist.expr.MethodCall;

import java.util.Arrays;

public class GenericFunctionTranslator implements Translator{
    static String mainClass;

    @Override
    public void start(ClassPool pool) throws NotFoundException, CannotCompileException {
        // Do nothing
    }

    @Override
    public void onLoad(ClassPool pool, String classname) throws NotFoundException, CannotCompileException {
//        System.out.printf("doing onLoad method in translator for classname argument: %s%n", classname);
        pool.importPackage("java.lang.reflect");
        CtClass ctClass = pool.get(classname);
        try {
            if (ctClass.getAnnotation(GenericFunction.class) != null) {
//                System.out.printf("Found a generic function annotation at ctclass %s%n", ctClass.getName());
                doThings(ctClass);
            }
            else {
//                System.out.println("No generic function annotation.");
                if (Arrays.stream(ctClass.getMethods()).map(CtMethod::getName).anyMatch(name -> name.equals("main"))){
//                    System.out.println("Has a main");
                    doMainClassThings(ctClass);
                }
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    private void doMainClassThings(CtClass ctClass) throws CannotCompileException {
//        System.out.println("Current class -> " + ctClass.getName());

        // newField
        StringBuilder insaneMapSrc = new StringBuilder();
        insaneMapSrc.append("public static final java.util.Hashtable genericFunctionMap = new java.util.Hashtable();");
        CtField insanemap = CtField.make(insaneMapSrc.toString(), ctClass);
        ctClass.addField(insanemap);

        // newMethod = buildHashTableInit
        CtMethod buildhtinit = CtNewMethod.make(
                "public static void buildHashTableInit(Class clazz){ " +
                        "Method[] methods = clazz.getDeclaredMethods();" +
                        "java.util.Hashtable methods_sigs = new java.util.Hashtable();" +
                        "for (int i = 0; i < methods.length; i++){" +
                        "    java.util.Hashtable params_returns;" +
                        "    String methodname = methods[i].getName();" +
                        "    Class[] params_types = methods[i].getParameterTypes();" +
                        "    java.util.List params_names = new java.util.ArrayList();" +
                        "    for (int j = 0; j < params_types.length; j++) { " +
                        "        params_names.add(params_types[j].getName());" +
                        "    }" +
                        "    String params_id = String.join(\"#\",params_names);" +
                        "    Class ret_type = methods[i].getReturnType();" +
                        "    if (methods_sigs.containsKey(methodname)){" +
                        "        params_returns = (java.util.Hashtable) methods_sigs.get(methodname);" +
                        "        params_returns.put(params_id, ret_type.getName());" +
                        "    }" +
                        "    else {" +
                        "        params_returns = new java.util.Hashtable();" +
                        "        params_returns.put(params_id, ret_type.getName());" +
                        "        methods_sigs.put(methodname, params_returns);" +
                        "    }" +
                        "}" +
                        "genericFunctionMap.put(clazz, methods_sigs);" +
                    "}", ctClass);
        ctClass.addMethod(buildhtinit);

        // newMethod = getNextCombinations
        CtMethod getNextCombsMethod = CtNewMethod.make(
                "public static java.util.List getNextCombinations(java.util.List current_combs, java.util.List tmp) {" +
                        "java.util.List next_c = new java.util.ArrayList();" +
                        "for (int j = 0; j < current_combs.size(); j++){" +
                        "   java.util.List c = (java.util.List) current_combs.get(j);" +
                        "   for (int k = 0; k < tmp.size(); k++){" +
                        "       java.util.List el_l = new java.util.ArrayList();" +
                        "       el_l.addAll(c);" +
                        "       el_l.add(tmp.get(k));" +
                        "       next_c.add(el_l);" +
                        "   }" +
                        "}" +
                        "return next_c;" +
                    "}"
                , ctClass);
        ctClass.addMethod(getNextCombsMethod);

        // newMethod = getMethodBySuperclasses
        CtMethod getSuperMethod = CtNewMethod.make(
                "public static Method getMethodBySuperclasses(Class clazz, String name, java.util.Hashtable paramsMap, java.util.List param_classes){ " +
                        "Method result_method;" +
                        "java.util.List params_all_supers = new java.util.ArrayList();" +
                        "for (int i = 0; i < param_classes.size(); i++){" +
                        "   java.util.List class_tree = new java.util.ArrayList();" +
                        "   Class orig_class = (Class)param_classes.get(i);" +
                        "   class_tree.add(orig_class);" +
                        "   while(!orig_class.equals(Object.class)){" +
                        "       orig_class = orig_class.getSuperclass();" +
                        "       class_tree.add(orig_class);" +
                        "   }" +
                        "   params_all_supers.add(class_tree);" +
                        "}" +

                        "java.util.List combinations = new java.util.ArrayList();" +
                        "for (int i = 0; i < params_all_supers.size(); i++){" +
                        "   java.util.List tmp = params_all_supers.get(i);" +
                        "   java.util.List next_l = new java.util.ArrayList();" +
                        "   if (combinations.size() == 0){" +
                        "       for (int j = 0; j < tmp.size(); j++){" +
                        "           java.util.List el_l = new java.util.ArrayList();" +
                        "           el_l.add(tmp.get(j));" +
                        "           next_l.add(el_l);" +
                        "       }" +
                        "       combinations = next_l;" +
                        "   }" +
                        "   else {" +
                        "       combinations = getNextCombinations(combinations, tmp);" +
                        "   }" +
                        "}" +

                        "java.util.List param_classnames = new java.util.ArrayList();" +
                        "for (int i = 0; i < combinations.size(); i++) { " +
                        "   java.util.List tmp_comb_names = new java.util.ArrayList();" +
                        "   java.util.List iter_comb = combinations.get(i);" +
                        "   for (int j = 0; j < iter_comb.size(); j++) {" +
                        "       tmp_comb_names.add(((Class)iter_comb.get(j)).getName());" +
                        "   }" +
                        "   param_classnames.add(tmp_comb_names);" +
                        "}" +

                        "for (int i = 0; i < combinations.size(); i++){" +
                        "   String joined = String.join(\"#\", (java.util.List)param_classnames.get(i));" +
                        "   if (paramsMap.get(joined) != null){" +
                        "       java.util.List param_classes = (java.util.List)combinations.get(i);" +
                        "       Class[] classes_array = (Class[]) param_classes.toArray(new Class[param_classes.size()]);" +
                        "       return clazz.getDeclaredMethod(name, classes_array);" +
                        "   }" +
                        "}" +
                        "return null;" +
                    "}", ctClass);
        ctClass.addMethod(getSuperMethod);


        // newMethod = invokeSpecifc
        CtMethod newMethod = CtNewMethod.make(
                "public static Object invokeSpecific(Class clazz, String name, Object[] args) {" +
                        "if (genericFunctionMap.get(clazz) == null) { " +
                        "   buildHashTableInit(clazz); " +
                        "}" +
                        "java.util.Hashtable methodsMap = ((java.util.Hashtable)genericFunctionMap.get(clazz));" +
                        "java.util.Hashtable paramsMap = ((java.util.Hashtable)methodsMap.get(name));" +
                        "java.util.List param_classes = new java.util.ArrayList();" +
                        "java.util.List param_classnames = new java.util.ArrayList();" +
                        "for (int i = 0; i < args.length; i++) { " +
                        "   param_classes.add(args[i].getClass());" +
                        "   param_classnames.add(args[i].getClass().getName());" +
                        "}" +
                        "String param_key = String.join(\"#\", param_classnames);" +
                        "Class[] classes_array = (Class[]) param_classes.toArray(new Class[param_classes.size()]);" +
                        "if (paramsMap.get(param_key) != null) { " +
                        "   Method naive_method = clazz.getDeclaredMethod(name, classes_array);" +
                        "   naive_method.setAccessible(true);" +
                        "   return  naive_method.invoke(null,args);" +
                        "}" +
                        "Method superMethod = getMethodBySuperclasses(clazz, name, paramsMap, param_classes);" +
                        "superMethod.setAccessible(true);" +
                        "return superMethod.invoke(null, args);" +
                    "}", ctClass);
        ctClass.addMethod(newMethod);

        // replace methods calls declared by some GenericFunction annotated element
        for (CtMethod ctMethod : ctClass.getMethods()) {
            ctMethod.instrument(new ExprEditor() {
                        public void edit(MethodCall m) throws CannotCompileException {
                            try {
                                if (m.getMethod().getDeclaringClass().hasAnnotation(GenericFunction.class))
                                    m.replace("{ $_ = ($r) invokeSpecific($class, \"" + m.getMethodName() + "\", ($args)); }");
                            } catch (NotFoundException e) { e.printStackTrace(); }
                        }
                    });
        }
        mainClass = ctClass.getName();
        ctClass.debugWriteFile();
    }

    void doThings(CtClass ctClass) throws CannotCompileException {
        // replace recursive method calls inside a GenericFunction annotated element
        for (CtMethod ctMethod : ctClass.getMethods()) {
            ctMethod.instrument(new ExprEditor() {
                        public void edit(MethodCall m) throws CannotCompileException {
                            try {
                                if (m.getMethod().getDeclaringClass().hasAnnotation(GenericFunction.class))
                                    m.replace("{ $_ = ($r) " + mainClass + ".invokeSpecific($class, \"" + m.getMethodName() + "\", ($args)); }");
                            } catch (NotFoundException e) { e.printStackTrace(); }
                        }
                    });
        }
        ctClass.debugWriteFile();
    }

}
