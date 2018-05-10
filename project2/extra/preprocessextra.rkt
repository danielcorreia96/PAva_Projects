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
(def-active-token #px"\\balias " (str)
  (let* ([alias_name (string-trim (car (regexp-match #px"\\s*.+?(?=\\s*=)" str)))]        ; match alias name
         [alias_type (car (regexp-match-positions #px"(?<==).+?(?=;)" str))]    ; match positions of alias type
         [name_token  (pregexp (string-append "\\b" alias_name "\\b"))]         ; build alias name token
         [type_start (car alias_type)]
         [type_end (cdr alias_type)]
         [type_regex (string-trim (substring str type_start type_end))]         ; type regex for replacement
         [str_noalias (substring str (+ type_end 1))])                          ; string without alias definition

    ; Define active token for this alias, just in case the alias definition is after the usage
    (def-active-token name_token (in) (string-append type_regex in))

    ; Replace all occurences of this alias in the string after the definition
    (regexp-replace* name_token str_noalias type_regex)
  )
)

; Extra 1
; Definition of an active token that allows the definition 
;  of active tokens in the file that is going to be processed
(define (meta-token-handler input)
  ; Execute MetaToken function
  (let ([meta_handler (car (regexp-match #px"(?<=[{]).+?(?=[}])" input))])
    (eval (with-input-from-string meta_handler read) ns)
  )
  ; Remove MetaToken definition after usage
  (match (regexp-match-positions #px"[{].+[}]\n\\s+" input)
    ((list (cons start end))
      (substring input end)
    )
  )
)

(add-active-token "@MetaToken" meta-token-handler)

; Extra 2
; Definition of active token for generation of getters and setters for Java.
(define (gen-access-handler input)
  (regexp-replace #px"\\s+([^{]+?)\\s+([^{]+?)\\s*;" input 
    "\\1 \\2;\npublic \\1 get_\\2() { return this.\\2; }\npublic void set_\\2(\\1 \\2) { this.\\2 = \\2; }\n"
  )
)

(add-active-token "@GenAccess" gen-access-handler)

; Extra 3
; Definition of active token for generation of getters and setters for a Java data class.
(define (data-class-handler input)
  (regexp-replace* #px"(\\s+)([^{]+?)\\s+([^{]+?)\\s*;" input 
    "\\1\\2 \\3; \\1public \\2 get_\\3() { return this.\\3; } \\1public void set_\\3(\\2 \\3) { this.\\3 = \\3; }\n"
  )
)

(add-active-token "@DataClass" data-class-handler)

; Extra 4
; Switch Expressions syntax sugar
; Inspired by https://bugs.openjdk.java.net/browse/JDK-8192963
(define (special-switch-handler input)
  (let* ( [switch_args (car (regexp-match #px"(?<=\\().+?(?=\\)\\s*[{])" input))]
          [switch_var (string-trim (car (string-split switch_args ",")))]
          [switch_arg (string-trim (cadr (string-split switch_args ",")))]
          [var_replace (string-append "\\1\\2:\n\\1\t" switch_var " =\\3;\n\\1\tbreak;")])
    (set! input (regexp-replace #px"(?<=\\().+?(?=\\)\\s*[{])" input switch_arg))
    (let loop ()
      (when (regexp-match? #px"(?<=\n)(\\s*)(\\bcase\\s+.+?)\\s*,\\s*(.+)" input)
        (set! input (regexp-replace* #px"(?<=\n)(\\s*)(\\bcase\\s+.+?)\\s*,\\s*(.+)" input "\\1\\2 :\n\\1case \\3"))
        (loop)
      )
    )
    (set! input (regexp-replace* #px"(?<=\n)(\\s*)(\\bcase\\s+.+?\\s+)->(\\s+.+?\\s*);" input var_replace))
    (set! input (regexp-replace #px"(?<=\n)(\\s*)(\\bdefault\\s+)->(\\s+.+?\\s*);" input var_replace))    
    (set! input (string-append "switch" input))
    (set! input (regexp-replace #px"switch\\s+\\(.+\\)\\s*[{].+[}]" input "\\0;"))
    input
  )
)

(add-active-token "special-switch" special-switch-handler)

; Extra 5
; Support definition of multiple aliases in a single line
; TODO
