#lang racket
(require "preprocess.rkt")
(define (run-example name input)
    (displayln (string-join (list 
        name "================Input================" input
        "================Output================" (process-string input)
        "*************************************************************"
    ) "\n"))
)

(run-example "Only single alias test" #<<END
    alias Cache = ConcurrentSkipListMap<String,List<Map<String,Object>  >  >        ;
    public static Cache mergeCaches(Cache a, Cache b) {
        Cache temp = new Cache();
    }
END
)

(run-example "Single alias + var test" #<<END
    alias Cache = ConcurrentSkipListMap<String,List<Map<String,Object>>>;
    public static Cache mergeCaches(Cache a, Cache b) {
        var temp = new Cache();
    }
END
)

(run-example "Name conflicts alias" #<<END
    alias   xxalias = ConcurrentSkipListMap<String,List<Map<String,Object>>>;
    alias 
    xaliasy = ConcurrentSkipListMap<String,List<Map<Integer,Object>>>;
    alias   
      aliasyy = ConcurrentSkipListMap<String,List<Map<String,Integer>>>;
    public static xxalias aliasmergealiasxalias(
        aliasyy a, 
        xaliasy b) {
    xxalias temp = new xxalias();
    }
END
)