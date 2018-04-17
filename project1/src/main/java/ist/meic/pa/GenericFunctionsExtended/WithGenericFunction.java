package ist.meic.pa.GenericFunctionsExtended;

import javassist.ClassPool;
import javassist.Loader;
import javassist.Translator;

public class WithGenericFunction {
    public static void main(String[] args) throws Throwable {
        if (args.length < 1){
            System.err.println("No arguments!!!");
        }
        else {
            Translator translator = new GenericFunctionTranslator();
            ClassPool pool = ClassPool.getDefault();
            pool.importPackage("java.lang.reflect");
            Loader classLoader = new Loader();
            classLoader.addTranslator(pool, translator);

            String[] restArgs = new String[args.length - 1];
            System.arraycopy(args, 1, restArgs, 0, restArgs.length);
            classLoader.run(args[0], restArgs);
        }
    }

    public static void callNextMethod(Object... args){
        // Do nothing. This method is used as a marker in @AroundMethods to replace
        // with a javassist method that handles call-next-method functionality
    }
}
