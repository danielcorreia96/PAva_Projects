package meic.ist.pa.GenericFunctions;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtField;
import javassist.CtMethod;
import javassist.CtNewMethod;
import javassist.NotFoundException;
import javassist.Translator;
import javassist.expr.ExprEditor;
import javassist.expr.MethodCall;

import java.util.Arrays;

public class GenericFunctionTranslator implements Translator{
    private static String mainClass;

    @Override
    public void start(ClassPool pool) throws NotFoundException, CannotCompileException {
        // Do nothing
    }

    @Override
    public void onLoad(ClassPool pool, String classname) throws NotFoundException, CannotCompileException {
        CtClass ctClass = pool.get(classname);
        try {
            if (ctClass.getAnnotation(GenericFunction.class) != null) {
                doThings(ctClass);
            }
            else if (Arrays.stream(ctClass.getMethods()).map(CtMethod::getName).anyMatch(name -> name.equals("main"))) {
                doMainClassThings(ctClass);
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    private void doMainClassThings(CtClass ctClass) throws CannotCompileException {
        // newField - gFunBaseMap
        ctClass.addField(CtField.make("static java.util.Hashtable gFunBaseMap = new java.util.Hashtable();", ctClass));

        // newField - gFunBeforeMap
        ctClass.addField(CtField.make("static java.util.Hashtable gFunBeforeMap = new java.util.Hashtable();", ctClass));

        // newField - gFunAfterMap
        ctClass.addField(CtField.make("static java.util.Hashtable gFunAfterMap = new java.util.Hashtable();", ctClass));

        // newMethod = getInitTableForAnnotation
        CtMethod getInitTableForAnnotation = CtNewMethod.make(
                "public static java.util.Hashtable getInitTableForAnnotation(Class clazz, Class annotationClass){ " +
                        "Method[] methods = clazz.getDeclaredMethods();" +
                        "java.util.Hashtable methods_sigs = new java.util.Hashtable();" +
                        "for (int i = 0; i < methods.length; i++){" +
                        "    if (annotationClass == null || methods[i].isAnnotationPresent(annotationClass)){" +
                        "       java.util.Hashtable params_method;" +
                        "       String methodname = methods[i].getName();" +
                        "       Class[] params_types = methods[i].getParameterTypes();" +
                        "       java.util.List params_names = new java.util.ArrayList();" +
                        "       for (int j = 0; j < params_types.length; j++) { " +
                        "           params_names.add(params_types[j].getName());" +
                        "       }" +
                        "       String params_id = String.join(\"#\",params_names);" +
                        "       if (methods_sigs.containsKey(methodname)){" +
                        "           params_method = (java.util.Hashtable) methods_sigs.get(methodname);" +
                        "           params_method.put(params_id, methods[i]);" +
                        "       }" +
                        "       else {" +
                        "           params_method = new java.util.Hashtable();" +
                        "           params_method.put(params_id, methods[i]);" +
                        "           methods_sigs.put(methodname, params_method);" +
                        "       }" +
                        "   }" +
                        "}" +
                        "return methods_sigs;" +
                        "}", ctClass);
        ctClass.addMethod(getInitTableForAnnotation);

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

        // newMethod = getSuperCombinationsMethod
        CtMethod getSuperCombinationsMethod = CtNewMethod.make(
                "public static java.util.List getSuperCombinations(Object[] params) {" +
                        "   java.util.List param_classes = new java.util.ArrayList();" +
                        "   for (int i = 0; i < params.length; i++) { " +
                        "       param_classes.add(params[i].getClass());" +
                        "   }" +

                        "   java.util.List params_all_supers = new java.util.ArrayList();" +
                        "   for (int i = 0; i < param_classes.size(); i++){" +
                        "       java.util.List class_tree = new java.util.ArrayList();" +
                        "       Class orig_class = (Class)param_classes.get(i);" +
                        "       class_tree.add(orig_class);" +
                        "       class_tree.addAll(java.util.Arrays.asList(orig_class.getInterfaces()));" +
                        "       while(!orig_class.equals(Object.class)){" +
                        "           orig_class = orig_class.getSuperclass();" +
                        "           class_tree.add(orig_class);" +
                        "           class_tree.addAll(java.util.Arrays.asList(orig_class.getInterfaces()));" +
                        "       }" +
                        "       params_all_supers.add(class_tree);" +
                        "   }" +

                        "   java.util.List combinations = new java.util.ArrayList();" +
                        "   for (int i = 0; i < params_all_supers.size(); i++){" +
                        "       java.util.List tmp = params_all_supers.get(i);" +
                        "       java.util.List next_l = new java.util.ArrayList();" +
                        "       if (combinations.size() == 0){" +
                        "           for (int j = 0; j < tmp.size(); j++){" +
                        "               java.util.List el_l = new java.util.ArrayList();" +
                        "               el_l.add(tmp.get(j));" +
                        "               next_l.add(el_l);" +
                        "           }" +
                        "           combinations = next_l;" +
                        "       }" +
                        "       else {" +
                        "           combinations = getNextCombinations(combinations, tmp);" +
                        "       }" +
                        "   }" +
                        "   return combinations;" +
                        "}"
                , ctClass);
        ctClass.addMethod(getSuperCombinationsMethod);
        // newMethod = getClassNames
        CtMethod getClassNames = CtNewMethod.make(
                "public static java.util.List getClassNames(Object[] params){" +
                        "java.util.List combinations = getSuperCombinations(params);" +
                        "java.util.List classNames = new java.util.ArrayList();" +
                        "for (int i = 0; i < combinations.size(); i++) { " +
                        "   java.util.List tmp_comb_names = new java.util.ArrayList();" +
                        "   java.util.List iter_comb = combinations.get(i);" +
                        "   for (int j = 0; j < iter_comb.size(); j++) {" +
                        "       tmp_comb_names.add(((Class)iter_comb.get(j)).getName());" +
                        "   }" +
                        "   classNames.add(tmp_comb_names);" +
                        "}" +
                        "return classNames;" +
                    "}"
                , ctClass);
        ctClass.addMethod(getClassNames);

        // newMethod = getMethodBySuperclasses
        CtMethod getSuperMethod = CtNewMethod.make(
                "public static Method getMethodBySuperclasses(Class clazz, String name, java.util.Hashtable paramsMap, Object[] params){ " +
                        "java.util.List param_classnames = getClassNames(params);" +

                        "for (int i = 0; i < param_classnames.size(); i++){" +
                        "   String joined = String.join(\"#\", (java.util.List)param_classnames.get(i));" +
                        "   if (paramsMap.containsKey(joined)){" +
                        "       return (Method) paramsMap.get(joined);" +
                        "   }" +
                        "}" +

                        "return null;" +
                    "}", ctClass);
        ctClass.addMethod(getSuperMethod);

        // newMethod = invokeMethodsFromMap
        CtMethod invokeAllValidMethodsFromMap = CtNewMethod.make(
                "public static void invokeAllValidMethodsFromMap(java.util.Hashtable genericFunctionMap, Class clazz, String name, Object[] args){" +
                        "java.util.Hashtable methodsMap = ((java.util.Hashtable)genericFunctionMap.get(clazz));" +
                        "if (methodsMap != null && !methodsMap.isEmpty()) {" +
                        "   java.util.List param_classnames = getClassNames(args);" +
                        "   java.util.Hashtable paramsMap = ((java.util.Hashtable)methodsMap.get(name));" +
                        "   for (int i = 0; i < param_classnames.size(); i++){" +
                        "       String joined = String.join(\"#\", (java.util.List)param_classnames.get(i));" +
                        "       if (paramsMap.containsKey(joined)){" +
                        "           Method method = (Method) paramsMap.get(joined);" +
                        "           method.invoke(null, args);" +
                        "       }" +
                        "   }" +
                        "}" +
                    "}",
                ctClass);

        ctClass.addMethod(invokeAllValidMethodsFromMap);

        // newMethod = invokeSpecifc
        CtMethod newMethod = CtNewMethod.make(
                "public static Object invokeSpecific(Class clazz, String name, Object[] args) {" +
                        "if (gFunBaseMap.get(clazz) == null) { " +
                        "   gFunBaseMap.put(clazz, getInitTableForAnnotation(clazz, null)); " +
                        "   gFunBeforeMap.put(clazz, getInitTableForAnnotation(clazz, meic.ist.pa.GenericFunctions.BeforeMethod.class));" +
                        "   gFunAfterMap.put(clazz, getInitTableForAnnotation(clazz, meic.ist.pa.GenericFunctions.AfterMethod.class));" +
                        "}" +
                        "invokeAllValidMethodsFromMap(gFunBeforeMap, clazz, name, args);" +

                        "java.util.Hashtable methodsMap = ((java.util.Hashtable)gFunBaseMap.get(clazz));" +
                        "java.util.Hashtable paramsMap = ((java.util.Hashtable)methodsMap.get(name));" +
                        "Object invocation_result;" +
                        "Method superMethod = getMethodBySuperclasses(clazz, name, paramsMap, args);" +
                        "invocation_result = superMethod.invoke(null, args);" +

                        "invokeAllValidMethodsFromMap(gFunAfterMap, clazz, name, args);" +
                        "return invocation_result;" +
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
