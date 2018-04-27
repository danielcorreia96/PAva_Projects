#lang racket
(require "preprocess.rkt")

(define ns (make-base-namespace))

(def-active-token "//eval " (str)
  (call-with-input-string str
   (lambda (in)
     (string-append (~a (eval (read in) ns))
                    (port->string in))))
  )

(process-string "if (curYear > //eval (date-year (seconds->date (current-seconds)))) {")