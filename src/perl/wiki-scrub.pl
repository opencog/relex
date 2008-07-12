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

	# remove triple and double quotes (wiki bold, italic)
	s/\'\'\'//g;
	s/\'\'//g;

	# ignore everything of the form [[en:title]] 
	if (/^\[\[\w[\w-]+?:.+?\]\]$/) { next; }
	if (/^\{\{\w+\}\}$/) { next; }

	# Ignore headers
	if (/^==.+==$/) { next; }
	

	chop;
	print "its >>$_<<\n";
}
