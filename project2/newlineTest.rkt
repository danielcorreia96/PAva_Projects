#lang racket
(require "preprocess.rkt")

(def-active-token ";;" (str)
  (match (regexp-match-positions "\n" str)
    ((list (cons start end)) (substring str end))
    (else "")))
  
(process-string "
//Another great idea from our beloved client
;;This is stupid but it’s what the client wants
for(int i = 0; i < MAX_SIZE; i++) {
;;Lets do it again
//Another great idea from our beloved client
;;This is stupid but it’s what the client wants
for(int i = 0; i < MAX_SIZE; i++) {
")