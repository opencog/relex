set DISCO_HOME=C:/disco
java ^
-Xmx1024m ^
-Djava.library.path=%DISCO_HOME%/native/windows ^
-Drelex.linkparserpath=%DISCO_HOME%/data/linkparser ^
-Dwordnet.configfile=data/wordnet/file_properties-win32.xml ^
-Drelex.algpath=data/relex-semantic-algs.txt ^
-classpath C:\relex\bin;^
%DISCO_HOME%\lib\link-grammar.jar;^
%DISCO_HOME%\lib\gnu-getopt.jar;^
%DISCO_HOME%\lib\wordnet\jwnl.jar;^
%DISCO_HOME%\lib\wordnet\commons-logging.jar;^
%DISCO_HOME%\lib\opennlp\opennlp-tools-1.3.0.jar;^
%DISCO_HOME%\lib\opennlp\maxent-2.4.0.jar;^
%DISCO_HOME%\lib\opennlp\trove.jar;^
relex.parser.LinkParserServer %1
