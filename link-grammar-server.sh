#!/bin/bash

export LANG=en_US.UTF-8

VM_OPTS="-Xmx1024m -Djava.library.path=/usr/lib:/usr/local/lib"
RELEX_OPTS="-Drelex.algpath=data/relex-semantic-algs.txt"
CLASSPATH="-classpath bin:/usr/share/java/link-grammar-4.3.5.jar:/usr/local/share/java/link-grammar-4.3.5.jar"

java $VM_OPTS $RELEX_OPTS $CLASSPATH relex.parser.LinkParserServer $1
