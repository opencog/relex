package relex.util.socket;

public class ProcessProtocolExample extends ProcessProtocol {

	private int getInt = 0;

	public String processInput(String theInput) {
		String theOutput = "";
		if (theInput.equals("INIT"))
			theOutput = "";
		if (theInput.equals("GET")) {
			theOutput = Integer.toString(getInt);
			++getInt;
		}
		return theOutput;
	}
}
