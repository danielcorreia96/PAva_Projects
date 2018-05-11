#lang racket
(provide add-active-token def-active-token process-string)

; This module implements a language-independent programmable pre-processor
;   using active tokens and activation functions that transform the input string.
;
; By default, it supports pre-processing for Java features such as:
;   1. Local Type Inference using a token "var"
;   2. String Interpolation using a token "#"
;   3. Type Alias using a token "alias"
;
; Code Style based on Racket Docs Style Guide

; Hash Table to store active-token -> activation function assocations
(define active-tokens (make-hash))

; Associates an active token with an activation function
(define (add-active-token token function) (hash-set! active-tokens token function))

; Macro to add active token to the pre-processor
(define-syntax-rule (def-active-token token str function)
  (add-active-token token (lambda str function)))

; Processes a string by recursively applying active token rules
;   until no more active tokens are found in the string.
(define (process-string str)
  (match (find-active-token str)
    ((cons token token-function) (process-string (activate-token str token token-function)))
    (else str)))

; Searches the string for an active token match.
; On success, returns a pair with the matched token and activation function
; Otherwise returns false
(define (find-active-token str)
  (for/first ([(token token-function) (in-hash active-tokens)] #:when (regexp-match? token str))
    (cons token token-function)))

; Applies the activation function of a given token to a string
(define (activate-token str token token-function)
  (match (car (regexp-match-positions token str))
    ((cons start end) (~a (substring str 0 start) (token-function (substring str end))))))

; 2.1. Local Type Inference
(def-active-token #px"\\bvar " (str)
  (regexp-replace #px"(\\s*.+?\\s*=\\s*new\\s+)(.+?)(\\s*)(?=\\()" str "\\2 \\1\\2\\3"))

; 2.2 String Interpolation
(def-active-token "#(?=\")" (str)
  (match (car (regexp-match-positions #rx".+\"" str)) ((cons start end)
    (~a (regexp-replace* #rx"#{(.*?)}" (substring str start end) "\" + (\\1) + \"")
        (substring str end)))))

; 2.3 Type Aliases
;  Clear alias definition from the string
;  Replace all occurences of this alias in the string after the alias definition
(def-active-token #px"\\balias\\s+" (str)
  (let* ([alias-name (car (regexp-match #px".+?(?=\\s*=)" str))]
         [name-regex (pregexp (~a "\\b" alias-name "\\b"))]
         [type-regex (string-trim (car (regexp-match #px"(?<==).+?(?=;)" str)))])
    (regexp-replace* name-regex (regexp-replace #px".+?=.+?;" str "") type-regex)))
