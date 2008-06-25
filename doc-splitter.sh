#!/bin/bash
#
# doc-splitter.sh: A simple command-line utility to reformat
# a free-form text into sentences, one per line.
#

CLASSPATH="-classpath \
bin:\
/usr/local/share/java/opennlp-tools-1.3.0.jar:\
/usr/local/share/java/maxent-2.4.0.jar:\
"

# Read a sentence from stdin:
java $CLASSPATH relex.corpus.DocSplitterTool

