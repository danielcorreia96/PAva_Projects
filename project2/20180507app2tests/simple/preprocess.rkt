#lang racket
(provide add-active-token def-active-token process-string)
(define active-tokens (make-hash))

; add-active-token
; Associates an active token with a function
(define (add-active-token token function)
  (hash-set! active-tokens token function)
)

; def-active-token macro
(define-syntax-rule (def-active-token token str function)
  (add-active-token token (lambda str function))
)

; process-string
; receives a string and applies an active token rule if available
(define (process-string input)
  (if (not (find-match input)) input
    (let* ([matched (find-match input)]
           [matched_call (cadr matched)])
      (match (caar matched) ((cons start end)
        (process-string (string-append (substring input 0 start) (matched_call (substring input end))))
  ))))
)

; find-match
; Tries to find a match in the given string for the existing active tokens
; If there is a match, returns a list with the positions matched and the function associated with the token
; Otherwise, returns false
(define (find-match string)
  (let/ec return
    (for/list ([key (hash-keys active-tokens)])
      (when (regexp-match? key string)
        (return (list (regexp-match-positions key string) (hash-ref active-tokens key)))
    ))
    #f
  )
)

; 2.1. Local Type Inference
; Example: var variable_name = new var_type(...)
; Regex groups mapping
; 1 -> " variable_name = new "
; 2 -> "var_type"
(def-active-token #px"\\bvar " (str)
  (regexp-replace #px"(\\s*.+?\\s*=\\s*new\\s+)(.+?)\\(" str "\\2 \\1\\2(")
)

; 2.2 String Interpolation
(def-active-token "#" (str)
  (regexp-replace* #rx"#{([^}]*)}" str "\" + (\\1) + \"")
)

; 2.3 Type Aliases
(def-active-token #px"\\balias\\b" (str)
  (let* ([alias_name (car (regexp-match #px"(?<=\\s).+?(?=\\s*=)" str))]        ; match alias name
         [alias_type (car (regexp-match-positions #px"(?<==).+?(?=;)" str))]    ; match positions of alias type
         [name_token  (pregexp (string-append "\\b" alias_name "\\b"))]         ; build alias name token
         [type_start (car alias_type)]
         [type_end (cdr alias_type)]
         [type_regex (string-trim (substring str type_start type_end))]         ; type regex for replacement
         [str_noalias (substring str (+ type_end 1))])                          ; string without alias definition


    ; Replace all occurences of this alias in the string after the definition
    (regexp-replace* name_token str_noalias type_regex)
  )
)