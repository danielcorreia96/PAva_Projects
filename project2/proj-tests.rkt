#lang racket
(require rackunit "preprocess.rkt")
(require rackunit/text-ui)

(define file-tests
  (test-suite "Tests for preprocess.rkt"
    (test-equal? "Basic one var test"
      (process-string
        "var x = new HashMap<String,Integer>();"
      )
        "HashMap<String,Integer> x = new HashMap<String,Integer>();"
    )

    (test-equal? "Multiline one var test"
      (process-string
        "var 
        x 
        = 
        new 
        HashMap<String,Integer>(

        );"
      )
        "HashMap<String,Integer> 
        x 
        = 
        new 
        HashMap<String,Integer>(

        );"
    )

    (test-equal? "Repeated vars test"
      (process-string
        "var x = new HashMap<String,Integer>();
        var x = new HashMap<String,Integer>();   
        var x = new HashMap<String,Integer>();"
      )
        "HashMap<String,Integer> x = new HashMap<String,Integer>();
        HashMap<String,Integer> x = new HashMap<String,Integer>();   
        HashMap<String,Integer> x = new HashMap<String,Integer>();"
    )

    (test-equal? "String interpolation test"
      (process-string 
        "static void foo(int a, int b, int c) {
        String str = #\"First #{a}, then #{a+b}, finally #{b*c}.\";
        System.out.println(str);
        }"
      )
        "static void foo(int a, int b, int c) {
        String str = \"First \" + (a) + \", then \" + (a+b) + \", finally \" + (b*c) + \".\";
        System.out.println(str);
        }"
    )

    (test-equal? "Nested String interpolation test"
      (process-string 
        "static void foo(int a, int b, int c) {
        String str = #\"First #{a}, then #{a+b}, finally #{b*c} and also #{\"start\" + #\"#{a+b+c} million batatas\"}.\";
        System.out.println(str);
        }"
      )
        "static void foo(int a, int b, int c) {
        String str = \"First \" + (a) + \", then \" + (a+b) + \", finally \" + (b*c) + \" and also \" + (\"start\" + \"\" + (a+b+c) + \" million batatas\") + \".\";
        System.out.println(str);
        }"
    )

    (test-equal? "Only single alias test"
      (process-string 
        "alias Cache = ConcurrentSkipListMap<String,List<Map<String,Object>  >  >        ;
        public static Cache mergeCaches(
          Cache a, 
          Cache b) {
        Cache temp = new Cache();
        }"
      )
        "
        public static ConcurrentSkipListMap<String,List<Map<String,Object>  >  > mergeCaches(
          ConcurrentSkipListMap<String,List<Map<String,Object>  >  > a, 
          ConcurrentSkipListMap<String,List<Map<String,Object>  >  > b) {
        ConcurrentSkipListMap<String,List<Map<String,Object>  >  > temp = new ConcurrentSkipListMap<String,List<Map<String,Object>  >  >();
        }"
    )    

    (test-equal? "Single alias + var test"
      (process-string 
        "alias Cache = ConcurrentSkipListMap<String,List<Map<String,Object>>>;
        public static Cache mergeCaches(
          Cache a, 
          Cache b) {
        var temp = new Cache();
        }"
      )
        "
        public static ConcurrentSkipListMap<String,List<Map<String,Object>>> mergeCaches(
          ConcurrentSkipListMap<String,List<Map<String,Object>>> a, 
          ConcurrentSkipListMap<String,List<Map<String,Object>>> b) {
        ConcurrentSkipListMap<String,List<Map<String,Object>>> temp = new ConcurrentSkipListMap<String,List<Map<String,Object>>>();
        }"
    )  
  )
)

(run-tests file-tests 'verbose)