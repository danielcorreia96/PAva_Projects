#lang racket
(require "preprocess.rkt")
(displayln "Basic one var test")
(displayln "====Input====")
(displayln 
#<<END
    var x = new HashMap<String,Integer>();
END
)
(displayln "====Output====")
(displayln (process-string
#<<END
    var x = new HashMap<String,Integer>();
END
))
(displayln "\n*************************************************************\n")


(displayln "Multiline one var test")
(displayln "====Input====")
(displayln 
#<<END
    var 
    x 
    = 
    new 
    HashMap<String,Integer>(

    );
END
)
(displayln "====Output====")
(displayln (process-string
#<<END
    var 
    x 
    = 
    new 
    HashMap<String,Integer>(

    );
END
))
(displayln "\n*************************************************************\n")


(displayln "Repeated vars test")
(displayln "====Input====")
(displayln 
#<<END
    var x = new HashMap<String,Integer>();
    var x = new HashMap<String,Integer>();   
    var x = new HashMap<String,Integer>();
END
)
(displayln "====Output====")
(displayln (process-string
#<<END
    var x = new HashMap<String,Integer>();
    var x = new HashMap<String,Integer>();   
    var x = new HashMap<String,Integer>();
END
))
(displayln "\n*************************************************************\n")

(displayln "Name conflicts var test")
(displayln "====Input====")
(displayln 
#<<END
    var varyy = new HashMap<String,Integer>();
    var xxvar = new HashMap<String,Object>();
    var xvary = new HashMap<Object,Integer>();
END
)
(displayln "====Output====")
(displayln (process-string
#<<END
    var varyy = new HashMap<String,Integer>();
    var xxvar = new HashMap<String,Object>();
    var xvary = new HashMap<Object,Integer>();
END
))
(displayln "\n*************************************************************\n")

(displayln "String interpolation test")
(displayln "====Input====")
(displayln
#<<END
    static void foo(int a, int b, int c) {
    String str = #"First #{a}, then #{a+b}, finally #{b*c}.";
    System.out.println(str);
    }
END
)
(displayln "====Output====")
(displayln (process-string 
#<<END
    static void foo(int a, int b, int c) {
    String str = #"First #{a}, then #{a+b}, finally #{b*c}.";
    System.out.println(str);
    }
END
))
(displayln "\n*************************************************************\n")


(displayln "Nested String interpolation test")
(displayln "====Input====")
(displayln
#<<END
    static void foo(int a, int b, int c) {
    String str = #"First #{a}, then #{a+b}, finally #{b*c} and also #{"start" + #"#{a+b+c} million batatas"}.";
    System.out.println(str);
    }
END
)
(displayln "====Output====")
(displayln (process-string 
#<<END
    static void foo(int a, int b, int c) {
    String str = #"First #{a}, then #{a+b}, finally #{b*c} and also #{"start" + #"#{a+b+c} million batatas"}.";
    System.out.println(str);
    }
END
))
(displayln "\n*************************************************************\n")


(displayln "Only single alias test")
(displayln "====Input====")
(displayln 
#<<END
    alias Cache = ConcurrentSkipListMap<String,List<Map<String,Object>  >  >        ;
    public static Cache mergeCaches(
        Cache a, 
        Cache b) {
    Cache temp = new Cache();
    }
END
)
(displayln "====Output====")
(displayln (process-string 
#<<END
    alias Cache = ConcurrentSkipListMap<String,List<Map<String,Object>  >  >        ;
    public static Cache mergeCaches(
        Cache a, 
        Cache b) {
    Cache temp = new Cache();
    }
END
))
(displayln "\n*************************************************************\n")


(displayln "Single alias + var test")
(displayln "====Input====")
(displayln
#<<END
    alias Cache = ConcurrentSkipListMap<String,List<Map<String,Object>>>;
    public static Cache mergeCaches(
        Cache a, 
        Cache b) {
    var temp = new Cache();
    }
END
)
(displayln "====Output====")
(displayln (process-string 
#<<END
    alias Cache = ConcurrentSkipListMap<String,List<Map<String,Object>>>;
    public static Cache mergeCaches(
        Cache a, 
        Cache b) {
    var temp = new Cache();
    }
END
))
(displayln "\n*************************************************************\n")
