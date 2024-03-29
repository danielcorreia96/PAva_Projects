package ist.meic.pa.GenericFunctionsExtended;

import ist.meic.pa.GenericFunctions.GenericFunction;
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

    void replaceGenericFunctionCalls(CtClass ctClass) throws CannotCompileException {
        // replace recursive method calls inside a GenericFunction annotated element
        for (CtMethod ctMethod : ctClass.getDeclaredMethods()) {
            ctMethod.instrument(new ExprEditor() {
                public void edit(MethodCall m) throws CannotCompileException {
                    try {
                        if (ctMethod.hasAnnotation(AroundMethod.class) && m.getMethodName().equals("callNextMethod")){
                            m.replace(String.format("{ $_ = ($r) AroundMethodHelper.callNextMethod(%s.class, \"%s\", ($args)); }", ctClass.getName(), ctMethod.getName()));
                        }
                        else if (m.getMethod().getDeclaringClass().hasAnnotation(GenericFunction.class)) {
                            m.replace(String.format("{ $_ = ($r) GenericFunctionDispatcher.invokeGenericFunction($class, \"%s\", ($args)); }", m.getMethodName()));
                        }
                    } catch (NotFoundException e) { e.printStackTrace(); }
                }
            });
        }
    }
}
