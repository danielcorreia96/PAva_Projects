#lang racket
(require "preprocess.rkt")

(def-active-token ";;" (str)
  (match (regexp-match-positions "\n" str)
    ((list (cons start end)) (string-trim (substring str end)))
    (else "")
  )
)
  
(displayln (process-string 
#<<END
  //Another great idea from our beloved client
  ;;This is stupid but it’s what the client wants
  for(int i = 0; i < MAX_SIZE; i++) {
  ;;Lets do it again
  //Another great idea from our beloved client
  ;;This is stupid but it’s what the client wants
  for(int i = 0; i < MAX_SIZE; i++) {
END
))