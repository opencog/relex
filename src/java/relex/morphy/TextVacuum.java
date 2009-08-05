package relex.morphy;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.ArrayList;

/**
 * Text Vacuum is a general purpose utility class for retrieving text output
 * from other processes. It prevents buffer-overloading errors, which result in
 * missed data, by allocating a separate thread whose sole job is to read the
 * output of the process as fast as it is produced.
 *
 * KNOWN PROBLEM: Text Vaccum uses readLine, which means it assumes that the
 * watched process generates newline characters often enough to prevent
 * buffer-overflowing.
 *
 * Text Vacuum should be used as follows:
 * Process x = ....;
 * TextVacuum vac = new TextVacuum(x);
 * vac.start();
 * x.waitFor();
 * vac.join();
 */
public class TextVacuum extends Thread {
	private BufferedReader br;

	private ArrayList<String> lines;

	private Process generator;

	boolean processDone;

	private boolean readingDone;

	public TextVacuum(Process p) {
		br = new BufferedReader(new InputStreamReader(p.getInputStream()));
		lines = new ArrayList<String>();
		generator = p;
		processDone = false;
		readingDone = false;
	}

	public void run() {
		// THIS IS CAUSING DISTINCT RESULTS!!!
		// Thread pm = new ProcessMonitor(generator);
		while (!readingDone) {
			/**/
			try {
				generator.exitValue();
				processDone = true; // thread has terminated
			} catch (IllegalThreadStateException e) {
				// thread has not yet terminated
			}
			try {
				if (br.ready()) {
					String line = br.readLine();
					lines.add(line);
				} else if (processDone) {
					// only finish if br is not ready
					// AND the thread has terminated
					readingDone = true;
				}
			} catch (Exception e) {
				System.err.println("Error reading external stream");
				e.printStackTrace();
			}
		}
	}

	public Iterator<String> iterator() {
		return lines.iterator();
	}

	class ProcessMonitor extends Thread {

		Process p;

		public ProcessMonitor(Process p) {
			this.p = p;
			start();
		}

		public void run() {
			while (!processDone) {
				try {
					p.waitFor();
					processDone = true;
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}

}
