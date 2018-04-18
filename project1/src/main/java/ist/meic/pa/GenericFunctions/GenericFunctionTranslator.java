package ist.meic.pa.GenericFunctions;

import javassist.*;
import javassist.expr.ExprEditor;
import javassist.expr.MethodCall;

import java.util.Arrays;

public class GenericFunctionTranslator implements Translator{

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

    private void doThings(CtClass ctClass) throws CannotCompileException {
        // replace recursive method calls inside a GenericFunction annotated element
        for (CtMethod ctMethod : ctClass.getDeclaredMethods()) {
            ctMethod.instrument(new ExprEditor() {
                public void edit(MethodCall m) throws CannotCompileException {
                    try {
                        if (m.getMethod().getDeclaringClass().hasAnnotation(GenericFunction.class)){
                            m.replace("{ $_ = ($r) GenericFunctionDispatcher.invokeSpecific($class, \"" + m.getMethodName() + "\", ($args)); }");
                        }
                    } catch (NotFoundException e) { e.printStackTrace(); }
                }
            });
        }
        ctClass.debugWriteFile();
    }

}
