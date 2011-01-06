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
$max_cpu = 93;

# Number of cpus in the system.
$nr_cpus = 16;

# On a 16-cpu system, one job is about 1/16 = 6.25% of cpu.
$hysteresis = 100/$nr_cpus +1;

while(1)
{
	# Get a 3-second average of cpu usage. A 1-sec avg is too noisy!
	($discard_a, $discard_b, $discard_c, $vmstat) = `vmstat 3 2`;

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

		# Some typical job status: Tl TNl   N for Nice l for multi-threaded (as java will be)
		if (($stat eq "TNl") || ($stat eq "Tl"))
		{
			# if we're below the allowed max, then restart a job
			if ($tot_usage < $max_cpu-$hysteresis)
			{
				($sec,$min,$hour,$mday,$mon,$year,$wday, $yday,$isdst) = localtime(time);
				printf "%4d-%02d-%02d %02d:%02d:%02d ",
					$year+1900,$mon+1,$mday,$hour,$min,$sec;
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
				`kill -STOP $pid`;
				($sec,$min,$hour,$mday,$mon,$year,$wday, $yday,$isdst) = localtime(time);
				printf "%4d-%02d-%02d %02d:%02d:%02d ",
					$year+1900,$mon+1,$mday,$hour,$min,$sec;
				print "curr cpu usage=$tot_usage > allowed max=$max_cpu -- stopping job $pid\n";
				last;
			}
			next;
		}
	}
}
