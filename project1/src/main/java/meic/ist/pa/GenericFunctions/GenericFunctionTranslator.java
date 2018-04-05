package meic.ist.pa.GenericFunctions;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtField;
import javassist.CtMethod;
import javassist.CtNewMethod;
import javassist.NotFoundException;
import javassist.Translator;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class GenericFunctionTranslator implements Translator{
    @Override
    public void start(ClassPool pool) throws NotFoundException, CannotCompileException {
        // Do nothing
    }

    @Override
    public void onLoad(ClassPool pool, String classname) throws NotFoundException, CannotCompileException {
        System.out.printf("doing onLoad method in translator for classname argument: %s%n", classname);
        CtClass ctClass = pool.get(classname);
        try {
            if (ctClass.getAnnotation(GenericFunction.class) != null) {
                System.out.printf("Found a generic function annotation at ctclass %s%n", ctClass.getName());
                doThings(ctClass);
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    void doThings(CtClass ctClass) throws CannotCompileException, NotFoundException {
        CtMethod[] methods = ctClass.getMethods();

        List<CtMethod> methodList = Arrays.stream(methods).filter(ctMethod -> ctMethod.getName().equals("bine")).collect(Collectors.toList());
//        List<CtMethod> methodList = Arrays.stream(methods).filter(ctMethod -> ctMethod.getName().equals("mix")).collect(Collectors.toList());
        StringBuilder sb2 = new StringBuilder();

        for (CtMethod ctMethod : methodList) {
            List<String> collect = Arrays.stream(ctMethod.getParameterTypes()).map(CtClass::getSimpleName).collect(Collectors.toList());
            if (!collect.stream().allMatch(s -> s.equals("Object"))) {
                List<String> types = Arrays.stream(ctMethod.getParameterTypes()).map(CtClass::getName).collect(Collectors.toList());
                String joined = String.join("#", collect);
                StringBuilder test = new StringBuilder();
                for (int i = 0; i < collect.size(); i++) {
                    test.append(String.format("if ($%d.getClass().getSimpleName().equals(\"%s\")) {\n", i+1, collect.get(i)));
                }
                appendReturnCall(ctMethod, types, test);
                for (int i = types.size() - 1; i > 0; i--) {
                    if (i != 0){
                        test.append("}"); // close previous if unless "root"
                    }
                    test.append("int i = 0;\n");
                    test.append(String.format("for (Class tmp = $%d.getClass(); i != 1; tmp = tmp.getClass().getSuperclass()) {", i+1));
//                    test.append("System.out.println(tmp.getSimpleName());");
                    test.append(String.format("if (tmp.getSimpleName().equals(\"%s\")) {\n", collect.get(i)));
                    appendReturnCall(ctMethod, types, test);
                    test.append("}"); // close return if inside for loop
                    test.append("if (tmp.getSimpleName().equals(\"Object\")) { i += 1; }");
                    test.append("}"); // close for loop
                    if (i != 0){
                        test.append("}"); // close parent if unless "root"
                    }
                }
//                System.out.println("test nested ifs: \n" + test.toString());
                sb2.append(test.toString());
            }
        }


        System.out.println("string for logic block");
        System.out.println(sb2.toString());

        for (CtMethod method : methodList) {
            if (Arrays.stream(method.getParameterTypes()).allMatch(ctClass1 -> ctClass1.getSimpleName().equals("Object"))){
//            if (Arrays.stream(method.getParameterTypes()).allMatch(ctClass1 -> ctClass1.getSimpleName().equals("Color"))){
                StringBuilder body = new StringBuilder();
//                body.append("System.out.println(\"Args: \" + $1 + \" , \" + $2);");
//                body.append("System.out.println(java.util.Arrays.toString($args));");
//                body.append("String joined = $1.getClass().getSimpleName() + \"#\" + $2.getClass().getSimpleName();");
//                body.append("System.out.println(\"TypesJoined: \" + joined); ");
                body.append(sb2.toString());
//                body.append("System.out.println(\"Do default implementation...\");");
                method.insertBefore(body.toString());
            }
        }
        ctClass.debugWriteFile();
    }

    private void appendReturnCall(CtMethod ctMethod, List<String> types, StringBuilder test) {
        test.append(String.format("return %s(", ctMethod.getName()));
        for (int j = 0; j < types.size(); j++) {
            test.append(String.format("(%s)$%d", types.get(j), j+1));
            if (j+1 < types.size()){
                test.append(", ");
            }
        }
        test.append(");");
    }

}
