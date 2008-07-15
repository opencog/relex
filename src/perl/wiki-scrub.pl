#! /usr/bin/env perl
#
# Ad-hoc script to scrub wikipedia xml dumps, outputing only valid
# english-language sentences.  This  script removes wiki markup, URL's
# tables, images, & etc.

# Need to specify the binmodes, in order for \w to match utf8 chars
use utf8;
binmode STDIN, ':encoding(UTF-8)'; 
binmode STDOUT, ':encoding(UTF-8)';

$have_text = 0;
$have_infobox = 0;
$have_table = 0;
while (<>)
{
	if (/<text /) { $have_text = 1; }
	if (/<\/text>/) { $have_text = 0; }

	chop;

	# remove the text xml
	s/.*<text xml:space="preserve">//;

	# kill redirect pages
	if (/#REDIRECT/) { $have_text = 0; next; }
	if (/#redirect/) { $have_text = 0; next; }

	# kill photo galleries
	if (/&lt;gallery&gt;/) { $have_text = 0; }
	if (/&lt;\/gallery&gt;/) { $have_text = 1; next; }

	# kill tables. These start with {| and end with |}
	# tables may be nested.
	if (/^:*\{\|/) { $have_text = 0; $have_table++; }
	if (/^\|\}/) {
		$have_table --;
		if (0 == $have_table) { $have_text = 1; }
		next;
	}
	if ($have_table) { next; }

	# kill infoxes. These may have embedded templates.
	# for the case of {{cite}}, this is too greedy, as it kills too much
	# text -- there may be valid stuff before and after ... 
	if (/\s*\{\{\s*(Infobox|infobox|Taxobox|taxobox|US state|navbox|yearbox|centurybox|cite|Football|ExamplesSidebar)/) { $have_text = 0; $have_infobox = 1; next;}
	if ($have_infobox && /\{\{/) { $have_infobox++; }
	if ($have_infobox && /\}\}/) { 
		$have_infobox--; 
		if (0 == $have_infobox) {$have_text = 1;}
		next;
	}
	if ($have_infobox) { next; }

	# remove single-line math markup. Don't be greedy(?)!
	# Do this before multi-line math markup.
	s/&lt;math&gt;.+?&lt;\/math&gt;//g;

	# kill multi-line math markup
	if (/&lt;math&gt;/) { $have_text = 0; }
	if (/&lt;\/math&gt;/) { $have_text = 1; next; }

	# ignore everything that isn't in a text section.
	if (0 == $have_text) { next; }

	# remove bogus markup
	s/&lt;nowiki&gt;//g;
	s/&lt;\/nowiki&gt;//g;

	# remove triple and double quotes (wiki bold, italic)
	s/\'\'\'//g;
	s/\'\'//g;

	# remove refs, assumed to sit on one line. Don't be greedy(?)!
	s/&lt;ref.+?&lt;\/ref&gt;//g;

	# remove stuff that's commented out. Don't be greedy(?)!
	s/&lt;!--.+?--&gt;//g;

	# Ignore everything of the form ^[[en:title]] (these are tranlsated
	# pages) These sometimes have {{Link FA|en}} after them.
	if (/^\[\[\w[\w-]+?:.+?\]\]( \{\{Link FA\|\w+\}\})*$/) { next; }

	# Ignore templates e.g. {{template gorp}}
	# These may sit alone, or be in a bullted list.
	s/\{\{.+?\}\}//g;

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
	s/&lt;\/i&gt;//g;
	s/&lt;b&gt;//g;
	s/&lt;\/b&gt;//g;
	s/&lt;tt&gt;//g;
	s/&lt;\/tt&gt;//g;
	s/&lt;div .+?&gt;//g;
	s/&lt;\/div&gt;//g;
	s/&lt;br&gt;//g;
	s/&lt;br clear.*?&gt;//g;
	s/&lt;includeonly&gt;//g;
	s/&lt;\/includeonly&gt;//g;
	s/&lt;noinclude&gt;//g;
	s/&lt;\/noinclude&gt;//g;
	s/__NOTOC__//g;

	# restore ordinary markup
	s/&amp;/&/g;
	s/&ndash;/-/g;
	# s/&minus;/-/g;
	s/&lt;/</g;
	s/&gt;/>/g;
	s/&deg;/°/g;

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
