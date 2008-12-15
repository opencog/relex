#!/bin/bash
#
# multi-page.sh: example of parsing multiple texts at once.
# This is a kludgy utility for parsing several files in parallel
#

# the files to be parsed
filelist=../wiki/simplewiki-20080629-stripped/*

# the maximum number of concurrent parses.
# (set this to the number of CPU cores on your system).
maxjobs=4;

export LANG=en_US.UTF-8

VM_OPTS="-Xmx1024m"

RELEX_OPTS="\
	-Drelex.algpath=data/relex-semantic-algs.txt \
	-Dwordnet.configfile=data/wordnet/file_properties.xml \
	-Djava.library.path=../../lib \
	-Dgate.home=../../share/java \
	-Dgate.plugins.home=../../share/java \
	-Dgate.site.config=../../share/java \
	"


CLASSPATH="-classpath \
bin:\
../../share/java/opennlp-tools-1.3.0.jar:\
../../share/java/maxent-2.4.0.jar:\
../../share/java/trove.jar:\
../../share/java/jwnl.jar:\
../../share/java/commons-logging.jar:\
../../share/java/gnu-getopt.jar:\
../../share/java/link-grammar-4.4.1.jar:\
../../share/java/xercesImpl.jar:\
../../share/java/gate.jar:\
../../share/java/jdom.jar:\
../../share/java/jasper-compiler-jdt.jar:\
../../share/java/nekohtml-0.9.5.jar:\
../../share/java/ontotext.jar:\
../../share/java/jaxp-1.3.jar:\
"

function parseit {
	fn="`basename "$1"`";
	in="$1"
	url="http://simple.wikipedia.org/wiki/$fn"
	out="../wiki/parsed/$fn.xml"
	err="../wiki/err/err-$fn"
	# echo $in $url $out $err
	echo $url
	cat "$in" | nice java $VM_OPTS $RELEX_OPTS $CLASSPATH relex.WebFormat -g -n 4 \
 	       --url "$url" > "$out" 2>"$err" &
}

jobsrunning=0;

echo $jobsrunning

for filename in $filelist;
do
	if [ $jobsrunning -lt $maxjobs ] ;
	then
		# sleep 2 &
		let jobsrunning=jobsrunning+1
		parseit "$filename"
	else
		wait
		let jobsrunning=0
	fi
done


