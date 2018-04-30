#lang racket
(define ns (variable-reference->namespace (#%variable-reference)))
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
  (if (not (find-match input))
    input
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
      (when (regexp-match-positions key string)
        (return (list (regexp-match-positions key string) (hash-ref active-tokens key)))
      )
    )
    #f
  )
)

; 2.1. Local Type Inference
(define (var-handler str)
  (regexp-replace #px"(\\s+.+\\s*=\\s*new\\s+)(.+)(\\()" str "\\2\\1\\2\\3")
)

(add-active-token "var" var-handler)

; 2.2 String Interpolation
(define (str-intstart-handler str)
  (regexp-replace* #rx"#{([^}]*)}" str "\" + (\\1) + \"")
)

(add-active-token "#" str-intstart-handler)


; 2.3 Type Aliases
(define alias-map (make-hash)) 
(define (alias-handler str)
  (let* ([alias_name (string-trim (car (regexp-match #px".+?(?=\\s*=)" str)))]
        [alias_key (pregexp (string-append "\\b" alias_name "\\b"))]
        [alias_type (car (regexp-match-positions #px"(?<==)[^;]+(?=;)" str))])
    (match alias_type
      ((cons start end)
        (def-active-token alias_key (in) (string-append (string-trim (substring str start end)) in))
        (substring str (+ end 1))
  )))
)

(add-active-token "alias" alias-handler)

; Extra 1
; Definition of an active token that allows the definition 
;  of active tokens in the file that is going to be processed
(define (meta-token-handler input)
  ; Execute MetaToken function
  (let ([meta_handler (car (regexp-match #px"(?<=[{]).+?(?=[}])" input))])
    (eval (with-input-from-string meta_handler read) ns)
  )
  ; Remove MetaToken definition after usage
  (match (regexp-match-positions #px"[{].+[}]" input)
    ((list (cons start end))
      (substring input end)
    )
  )
)

(add-active-token "@MetaToken" meta-token-handler)
(println (process-string "
  @MetaToken{
    (def-active-token \";;\" (str)
      (match (regexp-match-positions \"\n\" str)
        ((list (cons start end)) (substring str end))
        (else \"\")
      )
    )
  }
  //Another great idea from our beloved client
  ;;This is stupid but it’s what the client wants
  for(int i = 0; i < MAX_SIZE; i++) {
  ;;Lets do it again
  //Another great idea from our beloved client
  "
))