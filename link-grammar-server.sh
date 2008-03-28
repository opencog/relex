#!/bin/bash
VM_OPTS="-Xmx1024m -Djava.library.path=/usr/local/lib"
RELEX_OPTS="-Drelex.algpath=data/relex-semantic-algs.txt"
CLASSPATH="-classpath bin:/usr/local/share/java/link-grammar-4.3.4.jar"

java $VM_OPTS $RELEX_OPTS $CLASSPATH relex.parser.LinkParserServer $1
