#lang racket
(require "preprocess.rkt")
(require racket/format)

; This script assumes it is in the main directory where preprocess.rkt is
; and that the course tests structure is given by the unzip result
(define main_test_dir "20180507app2tests/simple/")
(current-directory main_test_dir)
(define tests_dirs (list "mixed-tokens" "string-interpolation" "type-inference" "type-alias"))

(define tests_results (list))
(for ([tests_type tests_dirs])
    (displayln tests_type)
    (let ([total_tests 0] [passed_tests 0])
        (for ([test (directory-list (string->path tests_type))])
            (when (regexp-match? #px"(.+).in" (path->string test))
                (let* ( [input_name (path->string test)]
                        [test_name (car (regexp-match #px"(.+)(?=.in)" input_name))]
                        [output_name (regexp-replace #px"(.+).in" input_name "\\1.out")]
                        [myout_name (regexp-replace #px"(.+).in" input_name "\\1.myout")]
                        [input_string (file->string (build-path (current-directory) tests_type input_name))]
                        [output_string (file->string (build-path (current-directory) tests_type output_name))]
                        [myout_file (build-path (current-directory) tests_type myout_name)]
                        [myoutput (process-string input_string)]
                        [test_passed (string=? output_string myoutput)])
                    (set! total_tests (+ total_tests 1))
                    (displayln (~a "Test " test_name " --> " test_passed))
                    (display-to-file myoutput myout_file #:exists 'replace)
                    (if test_passed
                        (set! passed_tests (+ passed_tests 1))
                        (displayln (~a input_string output_string myoutput "" #:separator "\n"))
                        ;;; For debugging, you can try to use ~v option to show \n\t\r and spaces
                        ;;; (displayln (~v "Input" input_string "Expected" output_string "Obtained" myoutput #:separator "\n"))
                    )
        )))
        (set! tests_results (append tests_results (list (list tests_type (list passed_tests total_tests)))))
    )
)

(displayln (~a "===========================" "Tests Results" #:separator "\n"))
(for-each (lambda (test_group) (displayln (~a (car test_group) " : " (caadr test_group) " of " (cadadr test_group))))
    tests_results)