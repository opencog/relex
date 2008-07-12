#! /usr/bin/env perl
#
# Very simple script to scrub wikipedia xml dumps

$have_text = 0;
while (<>)
{
	if (/<text /) { $have_text = 1; }
	if (/<\/text>/) { $have_text = 0; }

	# ignore everything that isn't in a text section.
	if (0 == $have_text) { next; }

	# remove the text xml
	s/<text xml:space="preserve">//;

	# remove triple and double quotes (wiki bold, italic)
	s/\'\'\'//g;
	s/\'\'//g;

	# ignore everything of the form [[en:title]] 
	if (/^\[\[\w[\w-]+?:.+?\]\]$/) { next; }
	if (/^\{\{\w+\}\}$/) { next; }

	# Ignore headers
	if (/^==.+==$/) { next; }
	
	# remove quotes
	s/&quot;//g;

	# kill wikilinks of the form [[the real link|The Stand-In Text]]
	#s/\[\[[\w ]+?\|(.+?)\]\]/$1/g;

	#s/\[\[([\w ']+?)\]\]/$1/g;

	# kill weblinks  i.e. [http:blah.com/whjaterver A Cool Site]
	s/\[\S+ (.+?)\]/$1/g;

	# kill bullets
	s/^\* //;

	chop;
	print "its >>$_<<\n";
}
