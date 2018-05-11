#lang racket
(require "preprocess.rkt")
(define (run-example name input)
    (displayln (string-join (list 
        name "================Input================" input
        "================Output================" (process-string input)
        "*************************************************************"
    ) "\n"))
)

(run-example "String interpolation test" #<<END
    static void foo(int a, int b, int c) {
    String str = #"First #{a}, then #{a+b}, finally #{b*c}.";
    System.out.println(str);
    }
END
)

(run-example "Nested String interpolation test" #<<END
    static void foo(int a, int b, int c) {
    String str = #"First #{a}, then #{a+b}, finally #{b*c} and also #{"start" + #"#{a+b+c} million batatas"}.";
    System.out.println(str);
    }
END
)

(run-example "var/alias conflicts interpolation test" 
#<<END
    static void foo(int a, int b, int c) {
    String str = #"First #{a}, then #{a+b}, finally #{b*c}.";
    System.out.println(str);
    }
    // #{not string interpolation}
END
)

(run-example "String interpolation test" #<<END
    static void foo(int a, int b, int c) {
    String str = #"First \\" \\\\";
    System.out.println(str);
    }
END
)


(run-example "String interpolation test" #<<END
    static void foo(int a, int b, int c) {
    String str = #"First #{a}, then #{a+b}, finally #{b*c}.";
    String str = #"batatas";
    System.out.println(str);
    }
END
)
