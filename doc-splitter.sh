#!/bin/bash
#
# doc-splitter.sh: A simple command-line utility to reformat
# a free-form text into sentences, one per line.
#

CLASSPATH='-classpath ./target/classes:./target/lib/*'

# Read a sentence from stdin:
java $CLASSPATH relex.corpus.DocSplitterTool
