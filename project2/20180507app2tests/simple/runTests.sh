 #!bin/sh

declare -a arr=("./mixed-tokens/" "./string-interpolation/" "./type-alias/" "./type-inference/")

## now loop through the above array
for i in "${arr[@]}"
do
   for f in $i*.in; do
    # do some stuff here with "$f"
    echo $f
    content=$( cat $f )
    NEWLINE=$'\n'
    #echo "$content" > out.txt 
    command=$(echo '(require "preprocess.rkt") (displayln (process-string' "$NEWLINE"'#<<END'"$NEWLINE""$content" 'END'"$NEWLINE" '))')
    file=$( basename $f .in )
    racket -e "$command" > $i$file'.myout'
    diff $i$file'.myout' $i$file'.out' 
    #racket -e '(require "preprocess.rkt") (displayln (process-string ' \"$content\" '))'
    # remember to quote it or spaces may misbehave
    done

done

