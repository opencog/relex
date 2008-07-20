#!/bin/bash
#
# relation-extractor.sh: example relationship extractor.
# Parses a simple sentence about dinosaurs.
# This provides a basic demo of the RelEx abilities.
#
# Flags:
# RelationExtractor [-s Sentence (in quotes)] [-h (show this help)] 
# [-t (show parse tree)] [-l (show parse links)] 
# [-o (show opencog XML output)] [-v verbose]
# [-n parse-number] [--maxParses N] [--maxParseSeconds N]

export LANG=en_US.UTF-8

VM_OPTS="-Xmx1024m -Djava.library.path=/usr/lib:/usr/local/lib"

# By default, these resources are read from the relex jar file.
# Alternately, they are taken from the default paths, which are the
# same as those immediate below.
# RELEX_OPTS="\
# 	-Drelex.algpath=data/relex-semantic-algs.txt \
# 	-Dwordnet.configfile=data/wordnet/file_properties.xml \
# 	"

CLASSPATH="-classpath \
bin:\
/usr/local/share/java/jwnl.jar:\
/usr/local/share/java/opennlp-tools-1.3.0.jar:\
/usr/local/share/java/maxent-2.4.0.jar:\
/usr/local/share/java/trove.jar:\
/usr/local/share/java/link-grammar-4.3.5.jar:\
/usr/share/java/link-grammar-4.3.5.jar:\
/usr/share/java/commons-logging.jar:\
/usr/share/java/gnu-getopt.jar:\
/usr/share/java/xercesImpl.jar:\
/opt/GATE-4.0/bin/gate.jar:\
/opt/GATE-4.0/lib/jdom.jar:\
/opt/GATE-4.0/lib/jasper-compiler-jdt.jar:\
/opt/GATE-4.0/lib/nekohtml-0.9.5.jar:\
/opt/GATE-4.0/lib/ontotext.jar:\
"
# wordnet doesn't work with the gate version of jwnl ...
# /opt/GATE-4.0/lib/jwnl.jar:\

# Read a sentence from stdin:
echo "Alice wrote a book about dinosaurs for the University of California in Berkeley." | \
	java $VM_OPTS $RELEX_OPTS $CLASSPATH relex.RelationExtractor -n 4 -l -t -f

# Alternately, the sentence can be specified on the command line:
# java $VM_OPTS $RELEX_OPTS $CLASSPATH relex.RelationExtractor -n 4 -l -t \
#	-s "Alice wrote a book about dinosaurs for the University of California in Berkeley."

# Alternately, a collection of sentences can be read from a file:
# cat trivial-corpus.txt | \
#	java $VM_OPTS $RELEX_OPTS $CLASSPATH relex.RelationExtractor -n 4 -l -t

# A collection of sentences can be read from a file and sent to the 
# opencog server (assumed to be at port 17001 on localhost).
# cat trivial-corpus.txt | \
#	java $VM_OPTS $RELEX_OPTS $CLASSPATH relex.RelationExtractor -o | \
#	telnet localhost 17001
