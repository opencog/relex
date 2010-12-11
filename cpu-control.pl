#! /usr/bin/env perl

# The max allowed CPU usage, 1 to 100
$max_cpu = 80;

# on a 16-cpu system, one job is about 1/16 = 6.25% of cpu.
$hysteresis = 7;

while(1)
{
	# Get a 2-second average of cpu usage. A 1-sec avg is too noisy!
	($j, $k, $l, $vmstat) = `vmstat 2 2`;

	# trim leading whitespace
	$vmstat =~ s/^\s+//;

	# replace many spaces by just one
	$vmstat =~ s/\s+/ /g;

	# the different fields returned by vmstat
	($r, $b, $swpd, $free, $buff, $cache, $si, $so, $bi, $bo, $in, $cs, $us, $sy, $id, $wa) =  split(/ /, $vmstat);

	# Look at the list of jobs.
	@jobs = `ps aux |grep linas | grep java |grep -v grep`;

	foreach $job (@jobs)
	{
		# trim leading whitespace
		# replace many spaces by just one
		$job =~ s/^\s+//;
		$job =~ s/\s+/ /g;

		($user, $pid, $cpu, $mem, $vsz, $rss, $tty, $stat, $start, $time, $command) = split(/ /, $job);

		if ($stat eq "Tl")
		{
			# if we're below the allowed max, then restart a job
			if ($us < $max_cpu-$hysteresis)
			{
				print "cpu usage=$us < max-cpu=$max_cpu -- starting job $pid\n";
				`kill -CONT $pid`;
				last;
			}
			next;
		}
		else
		{
			# if we're above the allowed max, then halt a job
			if ($us > $max_cpu)
			{
				print "cpu usage=$us > max-cpu=$max_cpu -- stopping job $pid\n";
				`kill -STOP $pid`;
				last;
			}
			next;
		}
	}

	sleep 1;
}
