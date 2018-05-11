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

; Namespace reference necessary for @MetaToken implementation
(define ns (variable-reference->namespace (#%variable-reference)))

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
  (match (car (regexp-match-positions #rx".+\"" str))
    ((cons start end)
      (~a (regexp-replace* #rx"#{(.*?)}" (substring str start end) "\" + (\\1) + \"")
          (substring str end)))))

; 2.3 Type Aliases
;  Handle user mistake: alias definition after usage (define active token for this alias)
;  Clear alias definition from the string
;  Replace all occurences of this alias in the string after the alias definition
(def-active-token #px"\\balias\\s+" (str)
  (let* ([alias-name (car (regexp-match #px".+?(?=\\s*=)" str))]
         [name-regex (pregexp (~a "\\b" alias-name "\\b"))]
         [type-regex (string-trim (car (regexp-match #px"(?<==).+?(?=;)" str)))])
    (def-active-token name-regex (in) (string-append type-regex in))
    (regexp-replace* name-regex (regexp-replace #px".+?=.+?;" str "") type-regex)))

; --------------------------------------------------------
; Extra Features Implementation
; --------------------------------------------------------

; Extra 1 - @MetaToken
; Definition of an active token that allows the definition
;  of active tokens in the file that is going to be processed
;   a. Execute MetaToken function
;   b. Remove MetaToken definition after usage
(def-active-token "@MetaToken" (str)   
  (let ([meta_handler (car (regexp-match #px"(?<=[{]).+?(?=[}])"str))])
    (eval (with-input-from-string meta_handler read) ns)
    (match (car (regexp-match-positions #px"[{].+[}]\n\\s+"str))
      ((cons start end) (substring str end)))))

; Extra 2 - @GenAccess
; Definition of active token for generation of getters and setters for Java.
(def-active-token "@GenAccess"  (str)
    (regexp-replace #px"\\s+([^{]+?)\\s+([^{]+?)\\s*;" str
      (~a "\\1 \\2;\npublic \\1 get_\\2() { return this.\\2; }"
          "public void set_\\2(\\1 \\2) { this.\\2 = \\2; }\n"
          #:separator "\n")))

; Extra 3 - @DataClass
; Definition of active token for generation of getters and setters for a Java class.
; Inspired by Python's PEP-557: Data Classes
(def-active-token "@DataClass" (str)
  (regexp-replace* #px"(\\s+)([^{]+?)\\s+([^{]+?)\\s*;" str
    (~a "\\1\\2 \\3; \\1public \\2 get_\\3() { return this.\\3; } " 
        "\\1public void set_\\3(\\2 \\3) { this.\\3 = \\3; }\n")))

; Extra 4 - special-switch
; Switch Expressions syntax sugar
; Inspired by Java's JEP 325: Switch Expressions
;   a.Handle switch case cascades
;   b. Replace tuple of 2 args with single arg (the expression to be compared in the switch)
;   c. Replace special case (and default) syntax with old syntax      
(define (switch-cascade str)
  (if (regexp-match? #px"(\\s*)(\\bcase\\s+.+?)\\s*,\\s*(.+)" str)
    (switch-cascade 
      (regexp-replace #px"(\\s*)(\\bcase\\s+.+?)\\s*,\\s*(.+)" str "\\1\\2 :\\1case \\3"))
    str))
  
(define (switch-var-regex switch-expr)
  (~a "\\1\\2:\\1\t"
      (car (regexp-match #px"(?<=\\().+?(?=,.+?\\))" switch-expr))
      " =\\3;\\1\tbreak;"))

(def-active-token "special-(?=switch \\()" (str)
  (let ([switch-expr (car (regexp-match #px"switch\\s+\\(.+?,.+?\\)\\s*[{].+[}]" str))])
    (regexp-replaces (switch-cascade switch-expr)
      (list (list #px"(?<=\\()\\s*.+?,\\s*(.+?)(?=\\))" "\\1")
            (list #px"(\\s*)(\\bcase\\s+.+?\\s+)->(\\s+.+?\\s*);" (switch-var-regex switch-expr))
            (list #px"(\\s*)(\\bdefault\\s+)->(\\s+.+?\\s*);" (switch-var-regex switch-expr))))))
