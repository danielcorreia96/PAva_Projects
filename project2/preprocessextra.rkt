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
; Code Style based on Racket Docs: https://docs.racket-lang.org/style/index.html

; Namespace reference necessary for @MetaToken implementation
(define ns (variable-reference->namespace (#%variable-reference)))

; Hash Table to store active-token -> activation function assocations
(define active-tokens (make-hash))

; Associates an active token with an activation function
(define (add-active-token token function)
  (hash-set! active-tokens token function))

; Macro to add active token to the pre-processor
(define-syntax-rule (def-active-token token str function)
  (add-active-token token (lambda str function)))

; Processes a string by recursively applying active token rules
;   until no more active tokens are found in the string.
(define (process-string str)
  (let ([token-pair (find-active-token str)])
    (if token-pair
      (process-string (activate-token str (car token-pair) (cdr token-pair)))
      str)))

; Searches the string for an active token match.
; On success, returns a pair with the matched token and activation function
; Otherwise returns false
(define (find-active-token str)
  (for/first
    ([(token token-function) (in-hash active-tokens)]
      #:when (regexp-match? token str))
      (cons token token-function)))

; Applies the activation function of a given token to a string
(define (activate-token str token token-function)
  (match (car (regexp-match-positions token str))
    ((cons start end)
      (~a (substring str 0 start) (token-function (substring str end))))))

; 2.1. Local Type Inference
(def-active-token #px"\\bvar " (str)
  (regexp-replace #px"(\\s*.+?\\s*=\\s*new\\s+)(.+?)\\(" str "\\2 \\1\\2("))

; 2.2 String Interpolation
(def-active-token "#" (str)
  (regexp-replace* #rx"#{(.*?)}" str "\" + (\\1) + \""))

; 2.3 Type Aliases
(def-active-token #px"\\balias\\s+" (str)
  (let* ([alias-name (car (regexp-match #px".+?(?=\\s*=)" str))]
         [name-regex (pregexp (~a "\\b" alias-name "\\b"))]
         [type-regex (string-trim (car (regexp-match #px"(?<==).+?(?=;)" str)))])

    ; Handle user mistake: alias definition after usage
    ; Solution: Define active token for this alias
    (def-active-token name-regex (in) (string-append type-regex in))

    ; Clear alias definition from the string
    (set! str (regexp-replace #px".+?=.+?;" str ""))

    ; Replace all occurences of this alias in the string after the alias definition
    (regexp-replace* name-regex str type-regex)))

; --------------------------------------------------------
; Extra Features Implementation
; --------------------------------------------------------

; Extra 1 - @MetaToken
; Definition of an active token that allows the definition 
;  of active tokens in the file that is going to be processed
(define (meta-token-handler str)
  ; Execute MetaToken function
  (let ([meta_handler (car (regexp-match #px"(?<=[{]).+?(?=[}])"str))])
    (eval (with-input-from-string meta_handler read) ns))

  ; Remove MetaToken definition after usage
  (match (car (regexp-match-positions #px"[{].+[}]\n\\s+"str))
    ((cons start end) (substring str end))))

(add-active-token "@MetaToken" meta-token-handler)

; Extra 2 - @GenAccess
; Definition of active token for generation of getters and setters for Java.
(def-active-token "@GenAccess"  (str)
  (let ([getter_regex "\\1 \\2;\npublic \\1 get_\\2() { return this.\\2; }"]
        [setter_regex "public void set_\\2(\\1 \\2) { this.\\2 = \\2; }\n"])
    (regexp-replace #px"\\s+([^{]+?)\\s+([^{]+?)\\s*;" str 
      (~a getter_regex setter_regex #:separator "\n"))))

; Extra 3 - @DataClass
; Definition of active token for generation of getters and setters for a Java class.
; Inspired by Python's PEP-557: Data Classes
(def-active-token "@DataClass" (str)
  (let ([getter_regex "\\1\\2 \\3; \\1public \\2 get_\\3() { return this.\\3; } "]
        [setter_regex "\\1public void set_\\3(\\2 \\3) { this.\\3 = \\3; }\n"])
    (regexp-replace* #px"(\\s+)([^{]+?)\\s+([^{]+?)\\s*;" str 
      (~a getter_regex setter_regex))))

; Extra 4 - special-switch
; Switch Expressions syntax sugar
; Inspired by Java's JEP 325: Switch Expressions
(define (special-switch-handler str)
  (let* ( [switch_args (car (regexp-match #px"(?<=\\().+?(?=\\)\\s*[{])" str))]
          [switch_var (string-trim (car (string-split switch_args ",")))]
          [switch_arg (string-trim (cadr (string-split switch_args ",")))]
          [var_replace (~a "\\1\\2:\n\\1\t" switch_var " =\\3;\n\\1\tbreak;")])
    
    ; Replace tuple of 2 args with single arg (the expression to be compared in the switch)
    (set! str (regexp-replace #px"(?<=\\().+?(?=\\)\\s*[{])" str switch_arg))

    ; Handle switch case cascades 
    (let loop ()
      (when (regexp-match? #px"(?<=\n)(\\s*)(\\bcase\\s+.+?)\\s*,\\s*(.+)" str)
        (set! str 
          (regexp-replace* #px"(?<=\n)(\\s*)(\\bcase\\s+.+?)\\s*,\\s*(.+)" str "\\1\\2 :\n\\1case \\3"))
        (loop)))

    ; Replace special case (and default) syntax with old syntax
    (set! str (regexp-replace* #px"(?<=\n)(\\s*)(\\bcase\\s+.+?\\s+)->(\\s+.+?\\s*);" str var_replace))
    (set! str (regexp-replace #px"(?<=\n)(\\s*)(\\bdefault\\s+)->(\\s+.+?\\s*);" str var_replace))

    ; Recover old switch keyword at start
    (set! str (string-append "switch" str))

    ; Handle user mistake: missing semicolon to close the switch statement
    (set! str (regexp-replace #px"switch\\s+\\(.+\\)\\s*[{].+[}](?!;)" str "\\0;"))

    str))

(add-active-token "special-switch" special-switch-handler)
