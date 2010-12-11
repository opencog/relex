#! /usr/bin/env perl
#
# Job scheduling control.
#
# This perl script controls the number of running jobs, so as to keep
# the cpu usage below a certain percentage.  If the cpu usage gets too
# high, jobs are sent the -STOP signal, halting them. When the cpu load
# drops, jobs are restarted by sending them the -START signal.
#
# Right now, this script is HARD-CODED to manage relex parsing
# jobs, running in java, by user 'linas', on a 16-way CPU.
# These hard-codedd values can be easily found, and modified, below.


# The max allowed CPU usage, 1 to 99
$max_cpu = 80;

# Number of cpus in the system.
$nr_cpus = 16;

# On a 16-cpu system, one job is about 1/16 = 6.25% of cpu.
$hysteresis = 100/$nr_cpus +1;

while(1)
{
	# Get a 2-second average of cpu usage. A 1-sec avg is too noisy!
	($j, $k, $l, $vmstat) = `vmstat 2 2`;

	# trim leading whitespace
	$vmstat =~ s/^\s+//;

	# replace many spaces by just one
	$vmstat =~ s/\s+/ /g;

	# the different fields returned by vmstat
	($r, $b, $swpd, $free, $buff, $cache, $si, $so, $bi, $bo, $int, $cs, $user, $sys, $idle, $wait) =  split(/ /, $vmstat);

	$tot_usage = $user + $sys;

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
			if ($tot_usage < $max_cpu-$hysteresis)
			{
				print "curr cpu usage=$tot_usage < allowed max=$max_cpu -- starting job $pid\n";
				`kill -CONT $pid`;
				last;
			}
			next;
		}
		else
		{
			# if we're above the allowed max, then halt a job
			if ($tot_usage > $max_cpu)
			{
				print "curr cpu usage=$tot_usage > allowed max=$max_cpu -- stopping job $pid\n";
				`kill -STOP $pid`;
				last;
			}
			next;
		}
	}

	sleep 1;
}
