package ist.meic.pa.GenericFunctions;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.NotFoundException;
import javassist.Translator;
import javassist.expr.ExprEditor;
import javassist.expr.MethodCall;

public class GenericFunctionTranslator implements Translator{

    @Override
    public void start(ClassPool pool) throws NotFoundException, CannotCompileException {
        // Do nothing
    }

    @Override
    public void onLoad(ClassPool pool, String classname) throws NotFoundException, CannotCompileException {
        CtClass ctClass = pool.get(classname);
        replaceGenericFunctionCalls(ctClass);
    }

    private void replaceGenericFunctionCalls(CtClass ctClass) throws CannotCompileException {
        // GenericFunction method calls in any class by the GenericFunctionDispatcher delegator function
        for (CtMethod ctMethod : ctClass.getDeclaredMethods()) {
            ctMethod.instrument(new ExprEditor() {
                public void edit(MethodCall m) throws CannotCompileException {
                    try {
                        if (m.getMethod().getDeclaringClass().hasAnnotation(GenericFunction.class)){
                            m.replace(String.format("{ $_ = ($r) GenericFunctionDispatcher.invokeGenericFunction($class, \"%s\", ($args)); }", m.getMethodName()));
                        }
                    } catch (NotFoundException e) { e.printStackTrace(); }
                }
            });
        }
    }
}
