#! /usr/bin/env perl

# The max allowed CPU usage, 1 to 100
$max_cpu = 20;

while(1)
{
	($j, $k, $l, $vmstat) = `vmstat 1 2`;

	# trim leading whitespace
	$vmstat =~ s/^\s+//;

	# replace many spaces by just one
	$vmstat =~ s/\s+/ /g;

	# the different fields returned by vmstat
	($r, $b, $swpd, $free, $buff, $cache, $si, $so, $bi, $bo, $in, $cs, $us, $sy, $id, $wa) =  split(/ /, $vmstat);

	if ($us < $max_cpu)
	{
		sleep 1;
		next;
	}

	# If we are here, the cpu usage is too high.
	@jobs = `ps aux |grep linas | grep java |grep -v grep`;

	foreach $job (@jobs)
	{
		# trim leading whitespace
		# replace many spaces by just one
		$job =~ s/^\s+//;
		$job =~ s/\s+/ /g;

		($user, $pid, $cpu, $mem, $vsz, $rss, $tty, $stat, $start, $time, $command) = split(/ /, $job);

		print "duude $pid  $stat\n";

		if ($stat eq "Tl")
		{
			print "duude $pid is stopped: $stat\n";
			next;
		}

		`kill -STOP $pid`;
		last;
	}

	print "duuude come around agin\n";
	sleep 1;

}
