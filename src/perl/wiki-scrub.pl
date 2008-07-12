#! /usr/bin/env perl

$have_text = 0;
while (<>)
{
	if (/<text /) { $have_text = 1; }
	if (/<\/text>/) { $have_text = 0; }

	# ignore everything that isn't in a text section.
	if (0 == $have_text) { next; }

	# ignore everything of the form [[en:title]] 
	if (/\[\[??\:*\]\]/) { next; }

	print "its $_";
}
