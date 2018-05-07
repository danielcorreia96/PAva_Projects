#lang racket
(require "preprocess.rkt")

(define tests_dirs (list "mixed-tokens" "string-interpolation" "type-inference" "type-alias"))
;;; (define tests_dirs (list "string-interpolation" "type-inference" "type-alias"))
;;; (define tests_dirs (list "type-inference"))
;;; (define tests_dirs (list "mixed-tokens"))

(for/list ([tests_type tests_dirs])
    (println tests_type)
    (for/list ([test (directory-list (string->path tests_type))])
        (when (regexp-match? #px"(.+).in" (path->string test))
            (let* ( [input_name (path->string test)]
                    [test_name (car (regexp-match #px"(.+)(?=.in)" input_name))]
                    [output_name (regexp-replace #px"(.+).in" input_name "\\1.out")]
                    [myout_name (regexp-replace #px"(.+).in" input_name "\\1.myout")]                    
                    [input_string (file->string (build-path (current-directory) tests_type input_name))]
                    [output_string (file->string (build-path (current-directory) tests_type output_name))]
                    [myout_file (build-path (current-directory) tests_type myout_name)]
                    [myoutput (process-string input_string)])
                (display (string-append "Test " test_name " --> "))
                (println (string=? output_string myoutput))
                (when (not (string=? output_string myoutput))
                    (displayln input_string)
                    (displayln output_string)
                    (displayln myoutput)
                    (displayln "")        
                )
                (display-to-file (process-string input_string) myout_file 	#:exists 'replace)
            )
        )
    )
)
(displayln "Finished running tests")