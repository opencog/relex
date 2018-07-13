#!/bin/bash
#
# batch-wiki.sh: Example batch processing script.
# This script is tailored for processing wikipedia articles.
# The artciles are assumed to have been stripped of html
# markup already.
#
# This script outputs the so-called "compact format" which
# captures the full range of Link Grammar and RelEx output in a format
# that can be easily post-processed by other systems (typically by
# using regex's). The src/perl/cff-to-opencog.pl perl script provides
# an example of post-processing: it converts this output format into
# OpenCog hypergraphs.
#
# The idea behind the batch processing is that it is costly to parse
# large quantities of text: thus, it is convenient to parse the text
# once, save the results, and then perform post-processing at liesure,
# as needed.  Thus, the form of post-processing can be changed at will,
# without requiring texts to be re-processed over and over again.
#

export LANG=en_US.UTF-8

VM_OPTS="-Xmx1024m"

RELEX_OPTS="\
	-Djava.library.path=/usr/local/lib:/usr/local/lib/jni \
	-DEnglishModelFilename=data/opennlp/models-1.5/en-sent.bin \
	"


# 	-Drelex.algpath=data/relex-semantic.algs \
# 	-Dwordnet.configfile=data/wordnet/file_properties.xml \
#

CLASSPATH="-classpath \
/usr/local/share/java/relex.jar:\
/usr/local/share/java/opennlp-tools-1.5.0.jar:\
/usr/local/share/java/maxent-3.0.0.jar:\
/usr/local/share/java/trove.jar:\
/usr/local/share/java/jwnl.jar:\
/usr/local/share/java/commons-logging.jar:\
/usr/local/java/slf4j-api.jar:\
/usr/local/java/logback-core.jar:\
/usr/local/java/logback-classic.jar:\
/usr/local/share/java/linkgrammar.jar:\
/usr/share/java/linkgrammar.jar:\
/usr/share/java/gnu-getopt.jar:\
"

# IFS=$(echo -en "\n\b")

lettre=S
filepat=Sa*

FILES=enwiki-20080524-alpha/$lettre/$filepat

for fpath in $FILES
do
	f=${fpath##*/}
	echo "Processing \"${f}\""
	url="http://en.wikipedia.org/wiki/${f}"
	
	echo "url $url"
	cat "${fpath}" | \
	nice java $VM_OPTS $RELEX_OPTS $CLASSPATH relex.WebFormat  -g -n 20 \
	--url "${url}" > "parsed/$lettre/${f}.xml" 2> "err/$lettre/${f}"

	mv "enwiki-20080524-alpha/$lettre/${f}" done/$lettre
done
