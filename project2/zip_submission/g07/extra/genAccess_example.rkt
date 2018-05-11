#lang racket
(require "../preprocessextra.rkt")
(define (run-example name input)
    (displayln (string-join (list 
        name "================Input================" input
        "================Output================" (process-string input)
        "*************************************************************"
    ) "\n"))
)
(run-example "Basic @GenAccess Example"
#<<END
  public class Foo {
    @GenAccess int batatas;
    String cenas;
    @GenAccess List<Integer> bars;
    @GenAccess Object[] objects;
  }
END
)
