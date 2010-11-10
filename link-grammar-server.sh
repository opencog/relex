#!/bin/bash

export LANG=en_US.UTF-8

VM_OPTS="-Xmx1024m -Djava.library.path=/usr/lib:/usr/lib/jni:/usr/local/lib:/usr/local/lib/jni"
RELEX_OPTS="-Drelex.algpath=data/relex-semantic-algs.txt"
CLASSPATH="-classpath bin:/usr/share/java/linkgrammar.jar:/usr/local/share/java/linkgrammar.jar"

java $VM_OPTS $RELEX_OPTS $CLASSPATH relex.parser.LinkParserServer $1
