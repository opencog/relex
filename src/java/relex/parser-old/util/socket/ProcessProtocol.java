package relex.parser_old.util.socket;

public abstract class ProcessProtocol {
	public static final String HANDSHAKE_REQUEST = "GETREADY";

	public static final String HANDSHAKE_RESPONSE = "READY";

	public static final String RESPONSE_SEPERATOR = "#";

	public abstract String processInput(String input);

	protected String makeMessage(String message) {
		return message + "\n" + RESPONSE_SEPERATOR;
	}

}
