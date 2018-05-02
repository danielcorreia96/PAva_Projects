#lang racket
(require "preprocessextra.rkt")
(displayln "====Input====")
(displayln 
#<<END
  public class Foo {
    @GenAccess int batatas;
    String cenas;
    @GenAccess List<Integer> bars;
    @GenAccess Object[] objects;
  }
END
)

(displayln "====Output====")
(displayln (process-string 
#<<END
  public class Foo {
    @GenAccess int batatas;
    String cenas;
    @GenAccess List<Integer> bars;
    @GenAccess Object[] objects;
  }
END
))
