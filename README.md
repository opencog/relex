
                 RelEx Semantic Relation Extractor
                 =================================
                    Version 1.6.0  XXX 2014


RelEx is a dependency parser for the English language.  It extracts
dependency relations from Link Grammar, and adds some shallow semantic
analysis.  The primary use of RelEx is as a language input front-end
to the OpenCog artificial general intelligence system.

There are multiple inter-related parts to RelEx. The core component
extracts the dependency relationships. An experimental module provides
some simple anaphora resolution suggestions.  Output is provided in
various formats, including one format suitable for later batch
post-processing, another format suitable for input to OpenCog, and an
W3C OWL format. There are also a small assortment of perl scripts for
cleaning up web and wiki pages, &c.

The main RelEx website is at 

    http://opencog.org/wiki/RelEx

It provides an overview of the project, as well as detailed documentation.

The source code management system is at

   http://github.com/opencog/relex

Source tarballs may be downloaded from either of two locations:

   https://launchpad.net/relex/+download
   http://www.abisource.com/downloads/link-grammar/relex/

Build and install of the core package is discussed below.


Dependencies
-------------

### Installing on Ubuntu/Debian

An installation script for Ubuntu/Debian is provided in the [install-scripts]
(https://github.com/opencog/relex/tree/master/install-scripts) directory.

### Install and run via Docker

This distrubution includes a Dockerfile for easy use with Docker
(http://www.docker.io).  To use this, simply say:

   $ docker build -t test/relex .
   $ docker run -i -t -p :3333 -w /home/Downloads/relex mine/relex /bin/sh plain-text-server.sh

or

   $ docker run -i -t -p :4444 -w /home/Downloads/relex mine/relex /bin/sh opencog-server.sh
   $ docker run -i -t -p :9000 -w /home/Downloads/relex mine/relex /bin/sh link-grammar-server.sh


### Installing on all other systems
 
For other systems, follow the instructions below.
To build and use RelEx, the following packages are required to be 
installed:

 - libgetopt-java (GNU getopt)
 - Link Parser
 - WordNet 3.0
 - JWNL Java wordnet library
 - OpenNLP tools (optional)
 - W3C OWL (optional)


Pre-requisite dependencies
--------------------------
The following packages are required pre-requisites for building RelEx.

- Link Grammar Parser
	Compile and install the Link Grammar Parser. This parser is
	described at 
	
	http://abisource.com/projects/link-grammar/
	
	and sources	are available for download at
	
	> http://www.abisource.com/projects/link-grammar/#download

	Link-grammar version 5.0.0 or later is needed to obtain a variety
	of required fixes.

	The Link Grammar Parser is the underlying engine, providing
	the core sentence parsing ability.

	If the parser is not installed in the default location,
	be sure to modify `-Djava.library.path` appropriately in 
	`relation-extractor.sh` and other shell scripts.

- GNU getopt
	This is a standard command-line option parsing library. 
	For Ubuntu, install the `libgetopt-java` package.

- Wordnet
	Wordnet is used by RelEx to provide basic English morphology
	analysis, such as singular versions of (plural) nouns, base forms
	(lemmas) of adjectives, adverbs and infinitive forms of verbs.

	Download, unpack and install WordNet 3.0.  The install directory
	needs to be specified in `data/wordnet/file_properties.xml`, with
	the `name="dictionary_path"` property in this file.

	Some typical install locations are:
	
	- `/opt/WordNet-3.0/data` for RedHat and SuSE
	- `/usr/share/wordnet` for Ubuntu and Debian
	- `C:\Program Files\WordNet\3.0\data` for Windows

	The `relex/Morphy/Morphy.java` class provides a simple, easy-to-use
	wrapper around wordnet, providing the needed word morphology info.

- didion.jwnl
	The didion JWNL is the "Java WordNet Library", and provides the
	Java programming API to access the wordnet data files.
	Its home page is at 
	
	> http://sourceforge.net/projects/jwordnet
	
	and can be downloaded from
	
	> http://sourceforge.net/project/showfiles.php?group_id=33824	

	Verify that the final installed location of `jwnl.jar` is correctly
	specified in the `build.xml` file. Note that GATE also provides a
	`jwnl.jar`, but the GATE version of `jwnl.jar` is not compatible
	(welcome to java DLL hell).

	When copying `jwnl.jar`: verify the file permisions! Be sure to issue
	the following command: `chmod 644 jwnl.jar`, as otherwise, you'll
	get strange "java cannot unzip jar" error messages.

- Apache commons logging
	The JWNL package requires that the Apache commons logging
	jar file be installed. In Debian/Ubuntu, this is supplied by
	the `libcommons-logging-java` package. In RedHat/CentOS systems,
	the package name is `jakarta-commons-logging`.


Optional packages
-----------------
The following packages are optional. If they are found, then
additional parts of RelEx will be built, enabling additional 
function.

- OpenNLP
	RelEx uses OpenNLP for sentence detection, giving RelEx the ability
	to find sentence boundaries in free text. If OpenNLP is not found, 
	then the less accurate `java.text.BreakIterator` class is used.

	The OpenNLP home page is at 
	
	     http://opennlp.sourceforge.net/

	Download and install OpenNLP tools, and verify that the 
	installed files are correctly identified in both `build.xml`
	and in `relation-extractor.sh`.

	OpenNLP also requires the installation of maxent from
	
	   http://maxent.sourceforge.net/  

	You'll need `maxent-3.0.0.jar` and `opennlp-tools-1.5.0.jar`.
	
	The OpenNLP package is used solely in corpus/DocSplitter.java,
	which provides a simple, easy-to-use wrapper for splitting a
	document into sentences. Replace this file if an alternate
	sentence detector is desired.

- Trove
	Some users may require the GNU Trove to enable OpenNLP, although
	this depends on the JDK installed.  GNU Trove is an implementation
	of the java.util class hierarchy, which may or may not be included
	in the installed JDK.  If needed, download trove from:

	   http://trove4j.sourceforge.net/

	Since trove is optimized, using it may improve performance and/or
	decrease memory usage, as compared to the standard Sun JDK
	implementation of the java.util hierarchy.

	***IMPORTANT*** OpenNLP expects Gnu Trove version 1.0, and will not
	work with version 2.0 !!

- `xercesImpl.jar`
	Older versions of the OpenNLP package require that the Xerces2
	XML parser package be installed. In Debian/Ubunutu, this is supplied
	by the `libxerces2-java` package.


Building
--------
After the above are installed, the relex java code can be built.
The build system uses `ant`, and the ant build specifications
are in `build.xml`. When running `ant` for the first time it will download the
[Aether antlib](https://www.eclipse.org/aether/). Afterwards, simply saying `ant` at the command line
should be enough to build. Saying `ant run` will run a basic
demo of the system. The `ant test` command will run several tests
verifying both regular parsing, and the Stanford-parser compatibility
mode.


Using RelEx
-----------
It is assumed that RelEx will be used in one of two different ways.
These are in a "batch processing" mode, and a "custom Java development"
mode.

In the "batch processing mode", RelEx is run once over a large text,
and its output is saved to a file.  This output can then be 
post-processed at a later time, to extract desired info. The goal here
is to avoid the heavy CPU overhead of re-parsing a large text over and
over.  Example post-processing scripts are included (described below).

In the "custom Java development" mode, it is assumed that a capable
Java programmer can write new code to interface RelEx to meet their needs.
A good place to start is to review the workings of the output code in
`src/java/relex/output/*.java`.

The standard RelEx demo output is NOT SUITABLE for post-processing. It
is meant to be a human-readable example of what the system generates;
it does not include all required output. For example, if the same word
appears in a sentence twice, the demo output will not distinguish between
these two words.

This release of RelEx includes an experimental Stanford-parser 
compatibility mode.  In this mode, RelEx will generate the same 
dependency relations as the Stanford parser. This mode is technically
interesting for comparing output; RelEx is more than three time faster 
than the lexicalized (factored) Stanford parser, although it is slower
than the PCFG parser. This is described in greater detail in the file
`README-Stanford`.

This release of RelEx includes an optional Penn Treebank style part of
speech tagger.  The tagger is experimental, and has not been evaluated
for accuracy. It is probable that the accuracy is low, primarily because
it has not been well tested. Because the tagging is based on the syntactic
parse, in principle the accuracy could be very high, once fully debugged.


Running RelEx
-------------
Several example unix shell scripts and MS Windows batch files are
included to show sample usage. These files (`*.sh` in unix, or `*.bat`,
in Windows) define the required system properties, classpath and JVM
options.

If there are any ClassNotFound exceptions, please verify the paths
and values in these files.


### `relation-extractor.sh`

The primary usage example is the `relation-extractor.sh` file.
Running this will display:

 - The link parser output.
 - The detected persons, organizations and locations.
 - The dependency relations found. 
 - Anaphora resolutions.
 - Parse ranking info.
 - (Optionally) Stanford and Penn Treebank output.

Output is controlled by command-line flags that are set in the shell
script.  The `-h` flag will print a list of all of the available
command-line options.


### `batch-process.sh`

The `batch-process.sh` script is an example batch processing script.
This script outputs the so-called "compact (cff) format" which captures
the full range of Link Grammar and RelEx output in a format that can be
easily post-processed by other systems (typically by using regex's).

The idea behind the batch processing is that it is costly to parse
large quantities of text: thus, it is convenient to parse the text
once, save the results, and then perform post-processing at leisure,
as needed.  Thus, the form of post-processing can be changed at will,
without requiring texts to be re-processed over and over again.


### `src/perl/cff-to-opencog.pl`

This perl script provides an example of post-processing: it converts
the "cff" batch output format into OpenCog hypergraphs, which can
then be processed by OpenCog.


### `opencog-server.sh`

This script starts a relex server that  listens for plain-text input
(English sentences) on port 4444. It then parses the text, and returns
opencog output on the same socket.  This server is meant to serve the
OpenCog chatbot directly; it is not intended for general, manual use.


### `doc-splitter.sh`

The `doc-splitter.sh` file is a simple command-line utility to reformat
a free-form text into sentences, one per line.


### `src/perl/wiki-scrub.pl`

Ad-hoc script to scrub Wikipedia xml dumps, outputting only valid
English-language sentences.  This  script removes wiki markup, URL's
tables, images, & etc.  It currently seems to be pretty darned
bullet-proof, although it might handle multi-line refs incorrectly.


Using RelEx in custom code
--------------------------
The primary output of RelEx is the set of semantic relationships of a
sentence. To obtain the list of these relationships, make a copy of
`src/java/relex/output/SimpleView.java`, and customize it to provide 
the relationships that you wish, in the format that you wish.

The class `src/java/relex/RelationExtractor.java` should be considered 
to be a large example program illustrating all of the various features
of RelEx.  For custom applications, this class should be copied and
modified as desired to fit the application.


Speed test results
------------------
Performance comparison of RelEx-1.2.0 vs. Stanford-1.6.1, run 11 Oct 2009.
Test corpus: first 150 sentences (including preface boilerplate) from 
Project Gutenberg "Pride and Prejudice".  Due to differences in sentence
detection, Stanford and RelEx disagree on the sentence count. Due to 
differences in counting punctuation, the splitting of possessives and 
contractions, the two disagree on the word count as well.

Since these tests were run, the performance of link-grammar has been
improved by a factor of 2x-3x. This update should have a significant
effect on relex speeds.

The unix command `wc` counts 2609 words in 148 sentences, for 
2609/148 = 17.6 words/sent.
 
### Stanford, w/ englishFactored.ser.gz , w/unix `time` command:

      real	10m4.882s
      user	10m1.974s
      sys	0m4.208s

   Actual: 2609/605= 4.31 words/sec

### Stanford, w/ englishPCFG.ser.gz , w/unix `time` command:

      real	2m21.690s
      user	2m23.165s
      sys	0m1.056s

   Actual: 2609/143 = 18.24 words/sec

### Stanford, w/ wsjFactored.ser.gz , w/unix `time` command:

      real	10m5.972s
      user	10m3.802s
      sys	0m4.516s

### Stanford, w/ wsjPCFG.ser.gz , w/unix `time` command:

      real	2m11.154s
      user	2m14.312s
      sys	0m1.144s

   Actual: 2609/134 = 19.47 words/sec

### RelEx, w/unix `time` command:

      real	2m59.739s
      user	2m36.342s
      sys	0m22.137s

   Actual: 2609/180 = 14.50 words/sec

      Ratio: Stanford-englishFactored/RelEx = 605sec/180sc = 3.36x (faster)
      Ratio: Stanford-wsjFactored/RelEx = 606sec/180sec = 3.36x (faster)
      Ratio: Stanford-englishPCFG/RelEx = 143sec/180sec = 0.79x (slower)
      Ratio: Stanford-wsjPCFG/RelEx = 134sec/180sec = 0.74x (slower)


TODO
----

### TODO - LinkGraphGenerator

This graph visualization class is not currently used. It should be
wired up and turned on.


### TODO - OpenNLP

A new version of OpenNLP is available. It is not backwards-compatible
with the OLD API. Need to port relex to the new API.


### TODO - Comparatives

RelEx is pretty broken when it comes to handling comparative sentences.
This needs fixing.

### TODO - Java Install

The Java install dependencies would be much easier to deal with if
there was a centralized repository from which one could easily obtain
the needed jar files. That is, something analogous to apt-get or CPAN.
The closest such thing for Java is `maven`; however, none of the jar
files required by relex have been checked into maven. Thus, a to-do:
get all of the jar files submitted to maven.


### TODO - Wordnet Install

Windows users consistently have trouble installing Wordnet correctly.
In particular, dictionary location appears to be totally random. Try
to find some work-around for this.


### TODO - polywords, lexical units, collocations, idioms. 

Would be nice to identify: "By the way" as a polyword.
"Break a leg" as an idiom.


Bugs
----
Sentence splitter:
The sentence splitter fails to split the following:

"In such cases, a woman has not often much beauty to think of."  "But,
my dear, you must indeed go and see Mr. Bingley when he comes into the neighbourhood."

todo

 - write paper on question normalization including %atLocation
 - write paper on wsd by pos-lookup
 - write paper on relex overview
