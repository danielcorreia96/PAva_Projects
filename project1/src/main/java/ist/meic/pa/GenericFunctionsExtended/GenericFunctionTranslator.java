package ist.meic.pa.GenericFunctionsExtended;

import ist.meic.pa.GenericFunctions.GenericFunction;
import javassist.*;
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
        if (ctClass.hasAnnotation(GenericFunction.class) || Arrays.stream(ctClass.getMethods()).map(CtMethod::getName).anyMatch(name -> name.equals("main"))) {
            doThings(ctClass);
        }
    }

    void doThings(CtClass ctClass) throws CannotCompileException {
        // replace recursive method calls inside a GenericFunction annotated element
        for (CtMethod ctMethod : ctClass.getMethods()) {
            ctMethod.instrument(new ExprEditor() {
                public void edit(MethodCall m) throws CannotCompileException {
                    try {
                        if (ctMethod.hasAnnotation(AroundMethod.class) && m.getMethodName().equals("callNextMethod")){
                            m.replace(String.format("{ $_ = ($r) GenericFunctionDispatcher.callNextMethod(%s.class, \"%s\", ($args)); }", ctClass.getName(), ctMethod.getName()));
                        }
                        else if (m.getMethod().getDeclaringClass().hasAnnotation(GenericFunction.class)) {
                            m.replace(String.format("{ $_ = ($r) GenericFunctionDispatcher.invokeSpecific($class, \"%s\", ($args)); }", m.getMethodName()));
                        }
                    } catch (NotFoundException e) { e.printStackTrace(); }
                }
            });
        }
        ctClass.debugWriteFile();
    }

}
