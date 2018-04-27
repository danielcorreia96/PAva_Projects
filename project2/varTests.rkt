#lang racket
(require "preprocess.rkt")

(println "")
(println "Basic one var test")
(process-string "var x = new HashMap<String,Integer>();")

(println "")
(println "Repeated vars test")
(process-string "var x = new HashMap<String,Integer>();     
var x = new HashMap<String,Integer>();   
var x = new HashMap<String,Integer>();")

(println "")
(println "String interpolation test")
(process-string "static void foo(int a, int b, int c) {
String str = #\"First #{a}, then #{a+b}, finally #{b*c}.\";
System.out.println(str);
}")

(println "")
(println "Only single alias test")
(process-string "
alias Cache = ConcurrentSkipListMap<String,List<Map<String,Object>>>;
public static Cache mergeCaches(Cache a, Cache b) {
Cache temp = new Cache();
}")

(println "")
(println "Single alias + var test")
(process-string "
alias Cache = ConcurrentSkipListMap<String,List<Map<String,Object>>>;
public static Cache mergeCaches(Cache a, Cache b) {
var temp = new Cache();
}")