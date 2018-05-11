#lang racket
(require "../preprocessextra.rkt")

(define (run-example name input)
    (displayln (string-join (list 
        name "================Input================" input
        "================Output================" (process-string input)
        "*************************************************************"
    ) "\n"))
)

(run-example "Newline MetaToken Example" 
#<<END
    @MetaToken{
      (def-active-token ";;" (str)
        (match (regexp-match-positions "\n" str)
          ((list (cons start end)) (string-trim (substring str end)))
          (else "")
        )
      )
    }
    //Another great idea from our beloved client
    ;;This is stupid but it’s what the client wants
    for(int i = 0; i < MAX_SIZE; i++) {
    ;;Lets do it again
    //Another great idea from our beloved client
    for(int i = 0; i < MAX_SIZE; i++) {
END
)