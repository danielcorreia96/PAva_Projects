#lang racket
(require "../preprocessextra.rkt")
(define (run-example name input)
    (displayln (string-join (list 
        name "================Input================" input
        "================Output================" (process-string input)
        "*************************************************************"
    ) "\n"))
)

(run-example "Basic @DataClass Example" #<<END
    @DataClass
    public class Foo {
        int batatas;
        String cenas;
        List<Integer> bars;
        Object[] objects;
    }
END
)
