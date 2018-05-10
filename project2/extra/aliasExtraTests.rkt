#lang racket
(require "preprocessextra.rkt")
(define (run-example name input)
    (displayln (string-join (list 
        name "================Input================" input
        "================Output================" (process-string input)
        "*************************************************************"
    ) "\n"))
)

(run-example "Definition after usage alias" #<<END
    public static Cache mergeCaches(Cache a, Cache b) {
        var temp = new Cache();
    }
    alias Cache = List<Map<String,Integer>>;
END
)

(run-example "Redefinition after usage alias" 
#<<END
    alias Cache = List<Map<String,Object>>;
    public static Cache mergeCaches(Cache a, Cache b) {
        var temp = new Cache();
    }
    alias Cache = List<Map<String,Integer>>;
    public static Cache mergeCaches2(Cache a, Cache b) {
        var temp = new Cache();
    }
END
)