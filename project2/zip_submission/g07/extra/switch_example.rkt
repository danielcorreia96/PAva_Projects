#lang racket
(require "../preprocessextra.rkt")
(define (run-example name input)
    (displayln (string-join (list 
        name "================Input================" input
        "================Output================" (process-string input)
        "*************************************************************"
    ) "\n"))
)
(run-example "Basic special-switch Example"
#<<END
T result;
special-switch (result, arg) {
    case L1 -> e1;
    case L2 -> e2;
    default -> e3;
};
END
)

(run-example "Cascade special-switch Example"
#<<END
T result;
special-switch (result, arg) {
    case L1_A, L1_B, L1_C -> e1;
    case L2 -> e2;
    default -> e3;
};
END
)
