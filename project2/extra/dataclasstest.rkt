#lang racket
(require "preprocessextra.rkt")
(displayln "====Input====")
(displayln 
#<<END
    @DataClass
    public class Foo {
        int batatas;
        String cenas;
        List<Integer> bars;
        Object[] objects;
    }
END
)

(displayln "====Output====")
(displayln (process-string 
#<<END
    @DataClass
    public class Foo {
        int batatas;
        String cenas;
        List<Integer> bars;
        Object[] objects;
    }
END
))
