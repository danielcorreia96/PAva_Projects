#lang racket
(require "preprocess.rkt")

(println "Basic one var test")
(process-string "var x = new HashMap<String,Integer>();")

(println "Repeated vars test")
(process-string "var x = new HashMap<String,Integer>();     
var x = new HashMap<String,Integer>();   
var x = new HashMap<String,Integer>();")