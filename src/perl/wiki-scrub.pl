#! /usr/bin/env perl
#
# Very simple script to scrub wikipedia xml dumps

$have_text = 0;
while (<>)
{
	if (/<text /) { $have_text = 1; }
	if (/<\/text>/) { $have_text = 0; }
	if (/&lt;gallery&gt;/) { $have_text = 0; }
	if (/&lt;\/gallery&gt;/) { $have_text = 1; }

	# ignore everything that isn't in a text section.
	if (0 == $have_text) { next; }

	# remove the text xml
	s/<text xml:space="preserve">//;

	# remove triple and double quotes (wiki bold, italic)
	s/\'\'\'//g;
	s/\'\'//g;

	# Ignore everything of the form [[en:title]] 
	if (/^\[\[\w[\w-]+?:.+?\]\]$/) { next; }

	# Ignore templates i.e. {{template gorp}}
	if (/^\s*\{\{\w+?\}\}$/) { next; }

	# Ignore headers
	if (/^==.+==$/) { next; }
	
	# remove quotes
	s/&quot;//g;

	# Kill image tags of the form [[Image:Chemin.png|thumb|300px|blah]]
	s/\[\[Image:.+?\]\]//g;

	# kill wikilinks of the form [[the real link|The Stand-In Text]]
	s/\[\[[:\-\w ]+?\|(.+?)\]\]/$1/g;

	# Kill ordinary links -- [[Stuf more stuff]]
	s/\[\[([\w ']+?)\]\]/$1/g;

	# kill weblinks  i.e. [http:blah.com/whjaterver A Cool Site]
	s/\[\S+ (.+?)\]/$1/g;

	# kill bullets
	s/^\*\*\*//;
	s/^\*\*//;
	s/^\*//;

	chop;
	print "its >>$_<<\n";
}
