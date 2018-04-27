#lang racket
(provide add-active-token def-active-token process-string)
(define active-tokens (list))

; add-active-token
; Associates an active token with a function
(define (add-active-token token function)
  (set! active-tokens (append active-tokens (list (list token function))))
  )

; def-active-token macro
(define-syntax-rule (def-active-token token str function)
  (add-active-token token (lambda str function))
  )

; process-string
; receives a string and applies an active token rule if available
(define (process-string input)
  (define rmatch (find-match input))
  (define result "")
  (let/ec return
    (if (not rmatch)
        ((set! result input) (return result))
        (match (car rmatch)
          ((list (cons start end))
           (set! result (string-append result 
                                        (process-string (string-append (substring input 0 start)
                                                         ((cadr rmatch) (substring input end))))))
           ))
        )
    )
  result
  )

; find-match
; Tries to find a match in the given string for the active tokens
; If there is a match, returns a list with the positions matched and the function associated with the token
; Otherwise, returns false
(define (find-match string)
  (let/ec return
    (for/list ([elt (in-list active-tokens)])
      (when (regexp-match-positions (first elt) string)
        (return (list (regexp-match-positions (first elt) string) (second elt)))
        )
      )
     #f
    )
  )

; 2.1. Local Type Inference
(define (var-handler str)
  (string-append
   (car(string-split ; split by the left parentese
        (cadr(string-split ; split by space to separate new from constructor
              (car (regexp-match #rx"new .*(.*)" str)) ; match "new constructor(...);" call
              " "))
        "("))
   str)
  )

(add-active-token "var" var-handler)

; 2.3 Type Aliases
(define alias-map (make-hash)) 
(define (alias-handler str)
  (let* ([eq_split (string-split str "=")]
         [eq_split_tail (string-join (list-tail eq_split 2)#:before-first "=")]
         [end_split (string-split (cadr eq_split) ";")]
         [end_split_tail (string-join (list-tail end_split 1))]
         [alias_key (pregexp (string-append "\\b" (string-trim (car eq_split)) "\\b"))]
         [alias_type (string-trim (car end_split))])
    (hash-set! alias-map alias_key alias_type)
    (def-active-token alias_key (str)
      (string-append (hash-ref alias-map alias_key) str)
     )
    (string-append end_split_tail eq_split_tail)
    )
  )

(add-active-token "alias" alias-handler)

;(process-string "
;alias Cache = ConcurrentSkipListMap<String,List<Map<String,Object>>>;
;public static Cache mergeCaches(Cache a, Cache b) {
;Cache temp = new Cache();
;}")

(process-string "
alias Cache = ConcurrentSkipListMap<String,List<Map<String,Object>>>;
public static Cache mergeCaches(Cache a, Cache b) {
var temp = new Cache();
}")
