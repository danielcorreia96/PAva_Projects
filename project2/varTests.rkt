#lang racket
(require "preprocess.rkt")
(define (run-example name input)
    (displayln (string-join (list 
        name "================Input================" input
        "================Output================" (process-string input)
        "*************************************************************"
    ) "\n"))
)

(run-example "Basic one var test" #<<END
    var x = new HashMap<String,Integer>();
END
)

(run-example "Multiline one var test" #<<END
    var 
    x 
    = 
    new 
    HashMap<String,Integer>(

    );
END
)

(run-example "Repeated vars test" #<<END
    var x = new HashMap<String,Integer>();
    var x = new HashMap<String,Integer>();   
    var x = new HashMap<String,Integer>();
END
)

(run-example "Name conflicts var test" #<<END
    var varyy = new HashMap<String,Integer>();
    var xxvar = new HashMap<String,Object>();
    var xvary = new HashMap<Object,Integer>();
END
)