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
    (let ([rmatch (find-match input)])
      (match (car rmatch)
        ((list (cons start end))
          (process-string (string-append (substring input 0 start) ((cadr rmatch) (substring input end))))
  ))))
)

; find-match
; Tries to find a match in the given string for the active tokens
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
(def-active-token #px"\\bvar\\b" (str) 
  (regexp-replace #px"(\\s+.+\\s*=\\s*new\\s+)(.+)(\\()" str "\\2\\1\\2\\3")
)

; 2.2 String Interpolation
(def-active-token "#" (str)
  (regexp-replace* #rx"#{([^}]*)}" str "\" + (\\1) + \"")
)

; 2.3 Type Aliases
(def-active-token #px"\\balias\\b" (str)
  (let* ([alias_name (string-trim (car (regexp-match #px".+?(?=\\s*=)" str)))]
        [alias_key (pregexp (string-append "\\b" alias_name "\\b"))]
        [alias_type (car (regexp-match-positions #px"(?<==).+?(?=;)" str))])
    (match alias_type
      ((cons start end)
        (def-active-token alias_key (in) (string-append (string-trim (substring str start end)) in))
        (regexp-replace* alias_key (string-trim (substring str (+ end 1))) (string-trim (substring str start end)))
  )))
)