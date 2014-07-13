/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Copyright (c) 2008-2014 Linas Vepstas <linasvepstas@gmail.com>,
 * Hendy Irawan <ceefour666@gmail.com>
 */
package relex;

import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.MessageToMessageDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;

import javax.annotation.Nullable;

import org.linkgrammar.LinkGrammar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import relex.corpus.DocSplitter;
import relex.corpus.DocSplitterFactory;
import relex.output.LogicView;
import relex.output.OpenCogScheme;
import relex.output.SimpleView;
import ch.qos.logback.classic.Level;

import com.beust.jcommander.IStringConverter;
import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;

/**
 * Multi-threaded <a href="https://en.wikipedia.org/wiki/Non-blocking_I/O_(Java)">NIO socket-based</a> parse server using
 * <a href="http://netty.io/">Netty</a>.
 * It will listen for plain-text input sentences on port {@code 4444}, and will
 * generate OpenCog output.
 *
 * <p>It is intended that this server be used by OpenCog agents to process
 * text; the text is sent from OpenCog to this server, and the returned
 * parses are then further processed by OpenCog.
 * 
 * <p>Summary of features:
 * <ol>
 * 	<li>Multi-threaded. TODO: {@link RelationExtractor} and/or {@link LinkGrammar} isn't thread safe (see {@code README.md} for details).</li>
 * 	<li>Faster and better CPU utilization. (however {@link RelationExtractor} is {@code synchronized} due to above issue) 
 * 		Hendy's naive 1000 sentence-test finished 22.2s, it's ~60% faster than 35.6s using Java5 {@link Server}.</li>
 * 	<li>Input is decoded using {@link StandardCharsets#UTF_8} encoding, as well as output to same channel or to Cog server.</li>
 * 	<li>Logs via <a href="www.slf4j.org/">SLF4J API</a> to <a href="logback.qos.ch/">Logback</a>. Setting {@link #verbose} simply
 * 		{@link ch.qos.logback.classic.Logger#setLevel(Level)} to {@link Level#DEBUG}. Logs client's address & port for easier
 * 		diagnostics when many concurrent clients.</li>
 *  <li>Can be embedded, construct with {@link #Server2(int, URI, int, boolean, boolean, boolean, boolean, boolean, String)} then
 *  	call {@link #run()}.</li>
 * 	<li>No {@link System#gc()} call. If still leaks, then the cause is probably in other parts of RelEx.</li>
 * 	<li><a href="http://zeroturnaround.com/software/jrebel/">JRebel</a> (optional) can be used for rapid development
 * 		at runtime without restarting server.</li>
 * 	<li>Not that big ~380 LoC, still in the range of Java5 {@link Server}'s.</li>
 * 	<li>Clear separation of responsibility, should be doable to refactor this to implement {@link PlainTextServer} if desired.</li>
 * </ol>
 * 
 * <p>Build with Maven to use this, it's ignored when building in Ant.
 * Available shell scripts:
 * 
 * <ol>
 * 	<li>{@code target/relex/bin/relexd}, which runs {@code java relex.Server2 ...}
 * 	<li>{@code target/relex/bin/relexd-relex}, which runs {@code java relex.Server2 --relex --anaphora ...}
 * 	<li>{@code target/relex/bin/relexd-link}, which runs {@code relex.Server2 --link --relex --anaphora --verbose ...}
 * 	<li>{@code target/relex/bin/relexd-logic}, which runs {@code java relex.Server2 --logic ...}
 * </ol>
 * 
 * <p>Use {@literal --help} to show the list of accepted arguments.
 * 
 * <p>I hope the class structure is easy to learn:
 * <ol>
 * 	<li>{@link SentenceDecoder} decodes lines to sentences using {@link DocSplitter} for {@link #free_text} mode.</li>
 * 	<li>{@link SentenceInputHandler} calls {@link #processSentence(Object, String, PrintWriter)} then sends output
 * 		to Cog server or same channel.</li>
 * 	<li>{@link #parseArgs(String[])} parses command line arguments using {@link JCommander} (similar to {@code getopt} but better API).</li>
 * 	<li>{@link #run()} configures {@link RelationExtractor}, {@link OpenCogScheme}, and {@link LogicView} then
 * 		{@link #startServer()}.</li>
 * 	<li>{@link #startServer()} starts a Netty client (if OpenCog {@link #host} is given) then starts the RelEx Netty server.</li>
 * 	<li>{@link #processSentence(Object, String, PrintWriter)} passes the sentence to {@link RelationExtractor}
 * 		then prints the output to provided {@link PrintWriter}.</li>
 * </ol>
 * 
 * <p>Please let Hendy know if you find bugs.
 * @author linas, ceefour
 */
public class Server2 {

	public static class URIConverter implements IStringConverter<URI> {
		@Override
		public URI convert(String value) {
			return URI.create("opencog://" + value);
		}
	}
	
	/**
	 * Buffers lines into {@link DocSplitter}, then passes to the next handler only if there's a complete sentence.
	 * Used if {@link Server2#free_text} flag is set.
	 * @author ceefour
	 */
	private static class SentenceDecoder extends MessageToMessageDecoder<String> {
		private final Logger log = LoggerFactory
				.getLogger(Server2.SentenceDecoder.class);
		/**
		 * {@link DocSplitter} is thread-unsafe by design, so we create an
		 * instance per {@link SocketChannel}.
		 */
		private final DocSplitter ds = DocSplitterFactory.create();
		
		@Override
		protected void decode(ChannelHandlerContext ctx, String line,
				List<Object> out) throws Exception {
			final String trimmedLine = line.trim();
			log.trace("{}» Received line: {}", ctx.channel().remoteAddress(), trimmedLine);
			ds.addText(trimmedLine + " ");
			while (true) {
				@Nullable
				String sentence = ds.getNextSentence();
				if (sentence != null) {
					sentence = sentence.trim();
					log.trace("{}» Queueing sentence: {}", ctx.channel().remoteAddress(), sentence);
					out.add(sentence);
				} else {
					break;
				}
			}
			if (line.contains("\4")) {
				String remainder = ds.getRemainder().trim(); // get whatever unfinished sentence
				if (!remainder.isEmpty()) {
					out.add(remainder);
				}
				log.trace("{}» EOT detected, will close channel after sending response", ctx.channel().remoteAddress());
				out.add("\4"); // pass EOT to next handler
			}
		}
	}
	
	/**
	 * Processes a whole sentence, calls {@link Server2#processSentence(Object, String, PrintWriter)} then sends output
	 * to Cog server or same channel
	 * @author ceefour
	 */
	private class SentenceInputHandler extends SimpleChannelInboundHandler<String> {
		private final Logger log = LoggerFactory
				.getLogger(Server2.SentenceInputHandler.class);
		private final Optional<Channel> out;
		
		/**
		 * @param out Output channel if present, otherwise uses input channel. 
		 */
		public SentenceInputHandler(Optional<Channel> out) {
			super();
			this.out = out;
		}

		@Override
		protected void channelRead0(final ChannelHandlerContext ctx, final String msg)
				throws Exception {
			final String sentence = msg.trim();
			log.info("{}» Received sentence: {}", ctx.channel().remoteAddress(), sentence);
			StringWriter stringWriter = new StringWriter();
			PrintWriter printWriter = new PrintWriter(stringWriter);
			processSentence(ctx.channel().remoteAddress(), sentence, printWriter);
			final boolean closeChannelAfterFlush;
			if (msg.contains("\4")) {
				log.trace("{}» EOT detected, will close channel after sending response", ctx.channel().remoteAddress());
				closeChannelAfterFlush = true;
			} else {
				closeChannelAfterFlush = !free_text;
			}
			if (out.isPresent()) {
				out.get().writeAndFlush(stringWriter.toString()).addListener(new ChannelFutureListener() {
					@Override
					public void operationComplete(ChannelFuture future) throws Exception {
						log.trace("{}» Response sent to OpenCog server {} for: {}", 
								ctx.channel().remoteAddress(), out.get().remoteAddress(), sentence);
						if (closeChannelAfterFlush) {
							ctx.close();
						}
					}
				});
			} else {
				ctx.writeAndFlush(stringWriter.toString()).addListener(new ChannelFutureListener() {
					@Override
					public void operationComplete(ChannelFuture future) throws Exception {
						log.trace("{}» Response sent for: {}", ctx.channel().remoteAddress(), sentence);
						if (closeChannelAfterFlush) {
							ctx.close();
						}
					}
				});
			}
		}
	}
	
	private static final Logger log = LoggerFactory.getLogger(Server2.class);
	private static final String USAGE_STRING = "RelEx server (designed for OpenCog interaction).\n" +
			"Given a sentence, it returns a parse in OpenCog-style scheme format.\n";

	@Parameter(names={"-p", "--port"}, description="Port number to listen on.")
	private int listen_port = 4444;
	@Parameter(names="--host", description="Send output to indicated host:port (example: localhost:17001).",
			converter=URIConverter.class)
	private URI host = null;
	@Parameter(names="-n", description="Max number of parses to return.")
	private int max_parses = 1;
	@Parameter(names=" --relex", description="Output RelEx relations (default).")
	private boolean relex_on = false;
	@Parameter(names="--logic", description="Output of Relex2Logic scheme function calls and Relex relations.")
	private boolean logic_on = false;
	@Parameter(names="--link", description="Output Link Grammar Linkages.")
	private boolean link_on = false;
	@Parameter(names="--anaphora", description="Output anaphore references.")
	private boolean anaphora_on = false;
	@Parameter(names="--free-text", description="Don't assume one sentence per line; look for !?. to end sentence.")
	private boolean free_text = false;
	@Parameter(names="--lang", description="Set language.")
	private String lang = "en";

	@Parameter(names={"-h", "--help"}, description="Displays this help text.", help=true)
	private transient boolean help = false;
	@Parameter(names={"-v", "--verbose"}, description="Print parse output to server stdout.")
	private transient boolean verbose = false;
	
	// TODO: These beans are shared, however Hendy's not sure if they're thread-safe?
	private final RelationExtractor re = new RelationExtractor(false);
	private final OpenCogScheme opencog = new OpenCogScheme();
	private final LogicView logicView = new LogicView();
	
	public Server2() {
	}
	
	/**
	 * For embedding, after constructing you can call {@link #run()}.
	 */
	public Server2(int listen_port, URI host, int max_parses, boolean relex_on,
			boolean logic_on, boolean link_on, boolean anaphora_on,
			boolean free_text, String lang) {
		this.listen_port = listen_port;
		this.host = host;
		this.max_parses = max_parses;
		this.relex_on = relex_on;
		this.logic_on = logic_on;
		this.link_on = link_on;
		this.anaphora_on = anaphora_on;
		this.free_text = free_text;
		this.lang = lang;
	}

	public static void main(String[] args) {
		try {
			Server2 s = new Server2();
			if (s.parseArgs(args)) {
				s.run();
			}
		} catch (Exception e) {
			log.error("Error: " + e, e);
			System.exit(-1);
		}
	}
	
	/**
	 * Parses command line arguments using {@link JCommander} (similar to {@code getopt} but better API).
	 * @return {@code false} if should exit
	 */
	public boolean parseArgs(String[] args) {
		JCommander jc = new JCommander(this, args);
		if (verbose) { // the first arg to check because it enables DEBUG level
			final ch.qos.logback.classic.Logger logback = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
			if (logback.getLevel().isGreaterOrEqual(Level.DEBUG)) { // e.g. if root logger is TRACE, then no change 
				logback.setLevel(Level.DEBUG);
			}
			log.info("Root logger level is {}", logback.getLevel());
		}
		log.info("Version: {}", Version.getVersion());

		if (help) {
			System.err.println(USAGE_STRING);
			jc.usage();
			return false;
		}
		if (logic_on) {
			// --logic incorporates --relex and --link
			relex_on = true;
			link_on = true;
		 }
		if (!relex_on && !link_on) {
			// By default just export RelEx output.
			relex_on = true;
		}
		return true;
	}
	
	/**
	 * Configures {@link RelationExtractor}, {@link OpenCogScheme}, and {@link LogicView} then
	 * {@link #startServer()}.
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public void run() throws IOException, InterruptedException {
		log.info("Input mode: {}", free_text ? "Free text" : "One sentence per line");
		log.info("Anaphora output: {} - Link grammar output: {} - Relex2Logic output: {} - RelEx output: {}",
				anaphora_on, link_on, logic_on, relex_on);

		// After parsing the commmand arguments, set up the assorted classes.
		RelationExtractor re = new RelationExtractor();
		re.setLanguage(lang);
		re.setMaxParses(max_parses);
		if (1000 < max_parses) re.setMaxLinkages(max_parses + 100);
		re.do_apply_algs = relex_on;
		opencog.setShowAnaphora(anaphora_on);
		opencog.setShowLinkage(link_on);
		if (logic_on) {
			logicView.loadRules();
		}
		if (relex_on) {
			opencog.setShowRelex(relex_on);
		}

		startServer();
	}

	/**
	 * Starts a Netty client (if OpenCog {@link #host} is given) then starts the RelEx Netty server.
	 * @throws IOException
	 * @throws InterruptedException
	 */
	protected void startServer() throws IOException, InterruptedException {
		NioEventLoopGroup clientGroup = new NioEventLoopGroup();
		Optional<Channel> cogChannel = Optional.absent();
		try {
			if (host != null) {
				Preconditions.checkArgument(host.getHost() != null, "OpenCog server hostname must be specified");
				Preconditions.checkArgument(host.getPort() >= 1, "OpenCog server port must be specified");
				// Send output to an opencog server, instead of returning it on the input socket.
				Bootstrap cln = new Bootstrap()
					.group(clientGroup)
					.channel(NioSocketChannel.class)
					.option(ChannelOption.SO_KEEPALIVE, true)
					.handler(new ChannelInitializer<SocketChannel>() {
						@Override
						protected void initChannel(SocketChannel ch)
								throws Exception {
							ch.pipeline().addLast("outputstring", new StringEncoder(StandardCharsets.UTF_8)); // OUTPUT
							ch.pipeline() // INPUT
								.addLast("line", new LineBasedFrameDecoder(65535))
								.addLast("inputstring", new StringDecoder(StandardCharsets.UTF_8)) // Note: OpenCog sends ANSI escapes
								.addLast("cogresponse", new SimpleChannelInboundHandler<String>() {
									@Override
									protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
										log.info("{}» OpenCog: {}", ctx.channel().remoteAddress(), msg);
									}
								});
							// Assume we're talking to an opencog server.
							// Escape it into a scheme shell.
							log.info("{}» Connected to OpenCog server {}, will send 'scm hush'", ch, host);
							ch.writeAndFlush("scm hush\n");
						}
					});
				// Start the client
				ChannelFuture clnf = cln.connect(host.getHost(), host.getPort()).sync();
				cogChannel = Optional.of(clnf.channel());
			}
			
			NioEventLoopGroup bossGroup = new NioEventLoopGroup(); // event loop that handles I/O operations
			NioEventLoopGroup workerGroup = new NioEventLoopGroup();
			try {
				// RelEx server
				ServerBootstrap b = new ServerBootstrap();
				final Optional<Channel> realCogChannel = cogChannel;
				b.group(bossGroup, workerGroup) // boss accepts an incoming connection. worker handles the traffic of the accepted connection 
					.channel(NioServerSocketChannel.class) // use non-blocking IO
					.childHandler(new ChannelInitializer<SocketChannel>() {
						@Override
						protected void initChannel(SocketChannel ch) throws Exception {
							ByteBuf[] lineOrEotDelimiters = new ByteBuf[] {
				                Unpooled.wrappedBuffer(new byte[] { '\r', '\n' }),
				                Unpooled.wrappedBuffer(new byte[] { '\n' }),
				                Unpooled.wrappedBuffer(new byte[] { '\4' }), // EOT
							};
							ch.pipeline().addLast("stringEncoder", new StringEncoder(StandardCharsets.UTF_8)); // OUTPUT						
							ch.pipeline() // INPUT
								// max 65535 bytes per line
								.addLast("frameDecoder", new DelimiterBasedFrameDecoder(65535, false, lineOrEotDelimiters))
								.addLast("stringDecoder", new StringDecoder(StandardCharsets.UTF_8));
							if (free_text) {
								ch.pipeline().addLast("sentence", new SentenceDecoder());
							}
							ch.pipeline().addLast("relex", new SentenceInputHandler(realCogChannel));
						};
					})
					.option(ChannelOption.SO_BACKLOG, 128) // length of the queue of pending connections
					.option(ChannelOption.SO_KEEPALIVE, true);
				
				ChannelFuture f = b.bind(listen_port).sync(); // Bind and start accept incoming connections
				f.channel().closeFuture().sync(); // Wait for the socket to close, which will happen if you Ctrl+C
			} finally {
				workerGroup.shutdownGracefully();
				bossGroup.shutdownGracefully();
			}
		} finally {
			if (cogChannel.isPresent()) {
				log.info("{}» Disconnecting from OpenCog server {}", 
						cogChannel.get().localAddress(), cogChannel.get().remoteAddress());
				cogChannel.get().closeFuture().sync();
			}
			clientGroup.shutdownGracefully();
		}
	}
	
	/**
	 * Passes the sentence to {@link RelationExtractor} then prints the output to provided {@link PrintWriter}.
	 * @param sentence
	 * @param writer
	 * @return true if can continue loop, otherwise break
	 */
	protected boolean processSentence(Object logPrefix, String sentence, PrintWriter writer) {
		try {
			log.info("{}» sentence: \"{}\"", logPrefix, sentence);
			final Sentence sntc;
			synchronized (re) {
				sntc = re.processSentence(sentence);
			}
			if (sntc.getParses().size() == 0) {
				log.info("{}» No parses!", logPrefix);
				writer.println("; NO PARSES");

				// Only one sentence per connection in the non-free-text mode.
				if (!free_text) {
					return false;
				} else {
					return true;
				}
			}
			int np = Math.min(max_parses, sntc.getParses().size());
			int pn;
			for (pn = 0; pn < np; pn++) {
				ParsedSentence parse = sntc.getParses().get(pn);

				// Print the phrase string ... handy for debugging.
				writer.println("; " + parse.getPhraseString());

				if (log.isDebugEnabled()) {
					String fin = SimpleView.printRelationsAlt(parse);
					log.debug("{}» relationsAlt: {}", logPrefix, fin);
				}
				opencog.setParse(parse);
				writer.println(opencog);
				log.info("{}» sent parse {} of {}", logPrefix, pn + 1, np);

				if (logic_on) {
					writer.println("; ##### START OF R2L #####");
					writer.println(logicView.printRelationsNew(parse));
					log.info("{}» called relex2logic functions", logPrefix);
				}

				// This is for simplifying pre-processing of scheme string
				// before evaluating it in opencog, for Relex2Logic.
				writer.println("; ##### END OF A PARSE #####");
			}

			// Add a special tag to tell the cog server that it's
			// just received a brand new sentence. The OpenCog scheme
			// code depends on this being visible, in order to find
			// the new sentence.
			writer.println("(ListLink (stv 1 1)");
			writer.println("   (AnchorNode \"# New Parsed Sentence\")");
			writer.println("   (SentenceNode \"" + sntc.getID() + "\")");
			writer.println(")");

			writer.println("; END OF SENTENCE");
			return true;
		}
		catch (Exception e) {
			log.error("Failed to parse '" + sentence + "'", e);
			return false;
		}
	}
}
