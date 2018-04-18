package ist.meic.pa.GenericFunctions;

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
            pool.importPackage("ist.meic.pa.GenericFunctions");
            Loader classLoader = new Loader();
            classLoader.addTranslator(pool, translator);

            String[] restArgs = new String[args.length - 1];
            System.arraycopy(args, 1, restArgs, 0, restArgs.length);
            classLoader.run(args[0], restArgs);
        }
    }
}
