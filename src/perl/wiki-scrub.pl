#! /usr/bin/env perl
#
#  wiki-scrub.pl
#
# Ad-hoc script to scrub wikipedia xml dumps, outputing only valid
# english-language sentences.  This  script removes wiki markup, URL's
# tables, images, & etc.  It currently seems to be pretty darned
# bullet-proof, although it might handle multi-line refs incorrectly.
#
# The output is in the form of one big single file; it should be
# straight-forward to modify this script to dump output into individual
# files.
#
# Copyright (c) 2008 Linas Vepstas <linas@linas.org>
#

# Need to specify the binmodes, in order for \w to match utf8 chars
use utf8;
binmode STDIN, ':encoding(UTF-8)'; 
binmode STDOUT, ':encoding(UTF-8)';

$have_text = 0;
$have_infobox = 0;
$have_table = 0;
$have_ptable = 0;
$have_cmnt = 0;
$notfirst = 0;
while (<>)
{
	if (/<text xml:space/) { $have_text = 1; }

	# End of a wiki page.
	# If there are any badly-formed tables, etc. then reset the state
	# variables as we exit from a wiki page.
	if (/<\/text>/) {
		$have_text = 0;
		$have_infobox = 0;
		$have_table = 0;
		$have_ptable = 0;
		$have_cmnt = 0;
		$notfirst = 0;
	}

	chop;

	# remove the text xml
	s/.*<text xml:space="preserve">//;

	# kill redirect pages
	if (/#REDIRECT/) { $have_text = 0; next; }
	if (/#redirect/) { $have_text = 0; next; }

	# Remove stuff that's commented out. Don't be greedy(?)!
	# Do this before most other processing.
	s/&lt;!--.+?--&gt;//g;
	if (/&lt;!--/) { $have_text = 0; next; }
	if (/--&gt;/) { $have_text = 1; next; }

	# kill photo galleries
	if (/&lt;gallery&gt;/) { $have_text = 0; }
	if (/&lt;gallery .+?&gt;/) { $have_text = 0; }
	if (/&lt;\/gallery&gt;/) { $have_text = 1; next; }

	# kill tables. These start with {| and end with |}
	# tables may be nested.
	if (/^:*\{\|/) { $have_text = 0; $have_table++; }
	if ($have_table && /^\|\}\s*/) {
		$have_table --;
		if (0 == $have_table) { $have_text = 1; }
		next;
	}
	if ($have_table) { next; }

	if (/&lt;table/) { $have_text = 0; $have_ptable++; }
	if (/&lt;\/table/) {
		$have_ptable --;
		if (0 == $have_ptable) { $have_text = 1; }
		next;
	}
	if ($have_ptable) { next; }

	# Ignore single-line templates e.g. {{template gorp}}
	# Do this before processing multi-line templates
	s/\{\{.+?\}\}//g;

	# kill infoxes and other multi-line templates. These may have
	# embedded templates.
	# Don't be greedy -- some of these, like {{cite}}, have valid text
	# both before and after.
	if ($have_infobox && /\}\}/) { 
		$have_infobox--; 
		if (0 == $have_infobox) {
			s/.*\}\}//;
		}
	}
	if (/\{\{/) {
		if ($have_infobox) {
			$have_infobox++;
		} else {
			$have_infobox = 1;
			$notfirst = 0;
			s/\{\{.+$//;
		}
	}
	if ($have_infobox) { if ($notfirst) {next;} $notfirst = 1; }

	# remove single-line math markup. Don't be greedy(?)!
	# Do this before multi-line math markup.
	s/&lt;math&gt;.+?&lt;\/math&gt;//g;

	# kill multi-line math markup
	if (/&lt;math&gt;/) { $have_text = 0; }
	if (/&lt;\/math&gt;/) { $have_text = 1; next; }

	# ignore everything that isn't in a text section.
	if (0 == $have_text) { next; }

	# remove triple and double quotes (wiki bold, italic)
	s/\'\'\'//g;
	s/\'\'//g;

	# remove refs, assumed to sit on one line. Don't be greedy(?)!
	s/&lt;ref.+?&lt;\/ref&gt;//g;

	# multi-line refs seem to only have {{cite}} inside of them.
	# The below seems to work, but should probably be convertedto work 
	# like multi-line templates.
	s/&lt;ref&gt;//g;
	s/&lt;\/ref&gt;//g;
	s/&lt;ref name.+?&gt;//g;

	# Ignore everything of the form ^[[en:title]] (these are tranlsated
	# pages) These sometimes have {{Link FA|en}} after them.
	if (/^\[\[\w[\w-]+?:.+?\]\]( \{\{Link FA\|\w+\}\})*$/) { next; }

	# Ignore headers
	if (/^==.+==\s*$/) { next; }
	
	# remove quotes
	s/&quot;//g;

	# Kill image tags of the form [[Image:Chemin.png|thumb|300px|blah]]
	s/\[\[Image:.+?\]\]//g;

	# kill wikilinks of the form [[the real link#ugh|The Stand-In Text]]
	# also [[Wikipedia:special/blah|The Stand-In Text]]
	s/\[\[[#:,\.\/\-\w '\(\)]+?\|(.+?)\]\]/$1/g;

	# Kill ordinary links -- [[Stuf more stuff]]
	s/\[\[([:,\.\/\-\w '\(\)]+?)\]\]/$1/g;

	# kill weblinks  i.e. [http:blah.com/whjaterver A Cool Site]
	s/\[\S+ (.+?)\]/$1/g;

	# ignore misc html markup
	s/&lt;references\s*\/&gt;//g;
	s/&lt;i&gt;//g;
	s/&lt;i .+?&gt;//g;
	s/&lt;\/i&gt;//g;
	s/&lt;p&gt;//g;
	s/&lt;p .+?&gt;//g;
	s/&lt;\/p&gt;//g;
	s/&lt;b&gt;//g;
	s/&lt;b .+?&gt;//g;
	s/&lt;\/b&gt;//g;
	s/&lt;s&gt;//g;
	s/&lt;\/s&gt;//g;
	s/&lt;u&gt;//g;
	s/&lt;\/u&gt;//g;
	s/&lt;em&gt;//g;
	s/&lt;\/em&gt;//g;
	s/&lt;tt&gt;//g;
	s/&lt;\/tt&gt;//g;
	s/&lt;pre&gt;//g;
	s/&lt;pre .+?&gt;//g;
	s/&lt;\/pre&gt;//g;
	s/&lt;big&gt;//g;
	s/&lt;\/big&gt;//g;
	s/&lt;small&gt;//g;
	s/&lt;\/small&gt;//g;
	s/&lt;center&gt;//g;
	s/&lt;Center&gt;//g;
	s/&lt;\/center&gt;//g;
	s/&lt;inputbox&gt;//g;
	s/&lt;\/inputbox&gt;//g;
	s/&lt;charinsert&gt;//g;
	s/&lt;\/charinsert&gt;//g;
	s/&lt;timeline&gt;//g;
	s/&lt;\/timeline&gt;//g;
	s/&lt;cite&gt;//g;
	s/&lt;cite .+?&gt;//g;
	s/&lt;\/cite&gt;//g;
	s/&lt;Cite&gt;//g;
	s/&lt;Cite .+?&gt;//g;
	s/&lt;\/Cite&gt;//g;
	s/&lt;blockquote&gt;//g;
	s/&lt;\/blockquote&gt;//g;
	s/&lt;div .+?&gt;//g;
	s/&lt;\/div&gt;//g;
	s/&lt;font .+?&gt;//g;
	s/&lt;\/font&gt;//g;
	s/&lt;FONT .+?&gt;//g;
	s/&lt;\/FONT&gt;//g;
	s/&lt;span .+?&gt;//g;
	s/&lt;\/span&gt;//g;
	s/&lt;br&gt;//g;
	s/&lt;BR&gt;//g;
	s/&lt;\/br&gt;//g;
	s/&lt;br\/&gt;//g;
	s/&lt;br \/&gt;//g;
	s/&lt;br .*?&gt;//g;
	s/&lt;hr&gt;//g;
	s/&lt;hr .+?&gt;//g;
	s/&lt;includeonly&gt;//g;
	s/&lt;\/includeonly&gt;//g;
	s/&lt;noinclude&gt;//g;
	s/&lt;\/noinclude&gt;//g;
	s/&lt;Typo .+?\/&gt;//g;
	s/&lt;nowiki&gt;//g;
	s/&lt;nowiki \/&gt;//g;
	s/&lt;\/nowiki&gt;//g;
	s/__NOTOC__//g;
	s/&lt;ul&gt;//g;
	s/&lt;\/ul&gt;//g;
	s/&lt;li&gt;//g;
	s/&lt;li .?&gt;//g;
	s/&lt;\/li&gt;//g;
	s/&lt;tr&gt;//g;
	s/&lt;tr .+?&gt;//g;
	s/&lt;\/tr&gt;//g;
	s/&lt;td&gt;//g;
	s/&lt;td .+?&gt;//g;
	s/&lt;\/td&gt;//g;
	s/&lt;\/td .+?&gt;//g;

	# restore ordinary markup
	s/&amp;/&/g;
	s/&ndash;/-/g;
	# s/&minus;/-/g;
	s/&lt;/</g;
	s/&gt;/>/g;
	s/&deg;/°/g;
	s/&bull;/•/g;
	s/&nbsp;/ /g;

	# Make sure bulleted lists have a period at the end of them.
	# But do try to avoid double-periods.
	if (/^\*/ && !/\.$/) { $_ = $_ . "."; }
	if (/^#/ && !/\.$/) { $_ = $_ . "."; }
	if (/^:/ && !/\.$/) { $_ = $_ . "."; }

	# kill bullets
	s/^\*\*\*//;
	s/^\*\*//;
	s/^\*//;
	s/^#//;
	s/^:::::://;
	s/^::::://;
	s/^:::://;
	s/^::://;
	s/^:://;
	s/^://;

	print "$_\n";
}
