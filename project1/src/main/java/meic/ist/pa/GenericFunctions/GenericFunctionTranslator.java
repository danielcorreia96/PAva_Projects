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
        insaneMapSrc.append("public static final java.util.Hashtable pleasework = new java.util.Hashtable();");
        CtField insanemap = CtField.make(insaneMapSrc.toString(), ctClass);
        ctClass.addField(insanemap);

        // newMethod = buildHashTableInit
        CtMethod buildhtinit = CtNewMethod.make("public static void buildHashTableInit(Class clazz){ " +
//                "   System.out.println(\"initialize hash table for class: \" + clazz.getName());" +
                "   Method[] methods = clazz.getDeclaredMethods();" +
//                "   System.out.println(java.util.Arrays.toString(methods));" +
                "   java.util.Hashtable methods_sigs = new java.util.Hashtable();" +
                "   for (int i = 0; i < methods.length; i++){" +
                "       java.util.Hashtable params_returns;" +
                "       String methodname = methods[i].getName();" +
                "       Class[] params_types = methods[i].getParameterTypes();" +
                "       java.util.List params_names = new java.util.ArrayList();" +
                "       for (int j = 0; j < params_types.length; j++) { " +
                "           params_names.add(params_types[j].getName());" +
                "       }" +
                "       String params_id = String.join(\"#\",params_names);" +
                "       Class ret_type = methods[i].getReturnType();" +
                "       if (methods_sigs.containsKey(methodname)){" +
                "           params_returns = (java.util.Hashtable) methods_sigs.get(methodname);" +
                "           params_returns.put(params_id, ret_type.getName());" +
                "       }" +
                "       else {" +
                "           params_returns = new java.util.Hashtable();" +
                "           params_returns.put(params_id, ret_type.getName());" +
                "           methods_sigs.put(methodname, params_returns);" +
                "       }" +
                "   }" +
                "   pleasework.put(clazz, methods_sigs);" +
                "}", ctClass);
        ctClass.addMethod(buildhtinit);

        // newMethod = getMethodBySuperclasses
        CtMethod getSuperMethod = CtNewMethod.make(
                "public static Method getMethodBySuperclasses(Class clazz, String name, java.util.Hashtable paramsMap, java.util.List param_classes){ " +
//                        "System.out.println();" +
//                        "System.out.println(\"down the rabbit hole we go...\");" +
                        "Method result_method;" +
                        "int params_index = 0;" +
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
//                        "System.out.println(params_all_supers);" +
                        "java.util.List combinations = new java.util.ArrayList();" +
                        "if (param_classes.size() == 1) {" +
                        "   combinations = params_all_supers;" +
                        "}" +
                        "if (param_classes.size() == 2){" +
//                        "   System.out.println(\"handle two params case\");" +
                        "   java.util.List first_param_supers = params_all_supers.get(0);" +
                        "   java.util.List second_param_supers = params_all_supers.get(1);" +
                        "   for (int i = 0; i < first_param_supers.size(); i++) {" +
                        "       for (int j = 0; j < second_param_supers.size(); j++) {" +
                        "           java.util.List tmp = new java.util.ArrayList();" +
                        "           tmp.add(first_param_supers.get(i));" +
                        "           tmp.add(second_param_supers.get(j));" +
                        "           combinations.add(tmp);" +
                        "       }" +
                        "   }" +
                        "}" +
//                        "System.out.println(combinations);" +
                        "java.util.List param_classnames = new java.util.ArrayList();" +
                        "for (int i = 0; i < combinations.size(); i++) { " +
                        "   java.util.List tmp_comb_names = new java.util.ArrayList();" +
                        "   java.util.List iter_comb = combinations.get(i);" +
                        "   for (int j = 0; j < iter_comb.size(); j++) {" +
                        "       tmp_comb_names.add(((Class)iter_comb.get(j)).getName());" +
                        "   }" +
                        "   param_classnames.add(tmp_comb_names);" +
                        "}" +
//                        "System.out.println(param_classnames);" +
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
        StringBuilder newMethodSrc = new StringBuilder();
        newMethodSrc.append("public static Object invokeSpecific(Class clazz, String name, Object[] args) {")
//                .append("System.out.println(\"Args: \" + clazz.getName() + \" --- \" + name);")
//                .append("System.out.print(\"Args: \" + clazz.getName() + \" --- \" + name);")
//                .append("System.out.println(\" ## \" + java.util.Arrays.toString(args));")
//                .append("System.out.println(pleasework.get(clazz));")
                .append("if (pleasework.get(clazz) == null) { buildHashTableInit(clazz); }")
//                .append("System.out.println(pleasework.get(clazz));")
                .append("java.util.Hashtable methodsMap = ((java.util.Hashtable)pleasework.get(clazz));")
                .append("java.util.Hashtable paramsMap = ((java.util.Hashtable)methodsMap.get(name));")
                .append("java.util.List param_classes = new java.util.ArrayList();")
                .append("java.util.List param_classnames = new java.util.ArrayList();")
                .append("for (int i = 0; i < args.length; i++) { ")
                .append("   param_classes.add(args[i].getClass());")
                .append("   param_classnames.add(args[i].getClass().getName());")
//                .append("   System.out.println(\"loop -> \" + param_classes);")
                .append("}")
//                .append("   System.out.println(param_classes);")
                .append("String param_key = String.join(\"#\", param_classnames);")
                .append("Class[] classes_array = (Class[]) param_classes.toArray(new Class[param_classes.size()]);")
//                .append("System.out.println(\"testing key -> \" + param_key);")
                .append("if (paramsMap.get(param_key) != null) { ")
//                .append("   System.out.println(\"invoke naive method\");")
//                .append("   System.out.println(param_classes);")
                .append("   Method naive_method = clazz.getDeclaredMethod(name, classes_array);")
                .append("   naive_method.setAccessible(true);")
                .append("   return  naive_method.invoke(null,args);")
                .append("}")
                .append("Method superMethod = getMethodBySuperclasses(clazz, name, paramsMap, param_classes);")
                .append("superMethod.setAccessible(true);")
                .append("return superMethod.invoke(null, args);")
                .append("}");

        CtMethod newMethod = CtNewMethod.make(newMethodSrc.toString(), ctClass);
        ctClass.addMethod(newMethod);

        // replace methods calls declared by some GenericFunction annotated element
        for (CtMethod ctMethod : ctClass.getMethods()) {
            ctMethod.instrument(
                    new ExprEditor() {
                        public void edit(MethodCall m)
                                throws CannotCompileException
                        {
                            try {
                                if (m.getMethod().getDeclaringClass().hasAnnotation(GenericFunction.class)) {
                                    m.replace("{ $_ = ($r) invokeSpecific($class, \"" + m.getMethodName() + "\", ($args)); }");
                                }
                            } catch (NotFoundException e) {
                                e.printStackTrace();
                            }
                        }
                    });
        }
        mainClass = ctClass.getName();
        ctClass.debugWriteFile();
    }

    void doThings(CtClass ctClass) throws CannotCompileException {
        // replace recursive method calls inside a GenericFunction annotated element
        for (CtMethod ctMethod : ctClass.getMethods()) {
            ctMethod.instrument(
                    new ExprEditor() {
                        public void edit(MethodCall m)
                                throws CannotCompileException
                        {
                            try {
                                if (m.getMethod().getDeclaringClass().hasAnnotation(GenericFunction.class)) {
                                    m.replace("{ $_ = ($r) " + mainClass + ".invokeSpecific($class, \"" + m.getMethodName() + "\", ($args)); }");
                                }
                            } catch (NotFoundException e) {
                                e.printStackTrace();
                            }
                        }
                    });
        }
        ctClass.debugWriteFile();
    }

}
