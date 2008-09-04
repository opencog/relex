set GATE_HOME=C:/Progra~1/GATE-4.0
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
%GATE_HOME%\bin\gate.jar;^
%GATE_HOME%\lib\jdom.jar;^
%GATE_HOME%\lib\xercesImpl.jar;^
%GATE_HOME%\lib\jasper-compiler-jdt.jar;^
%GATE_HOME%\lib\nekohtml-0.9.5.jar;^
%GATE_HOME%\lib\ontotext.jar;^
%GATE_HOME%\lib\stax-api-1.0.1.jar;^
%GATE_HOME%\lib\PDFBox-0.7.2.jar ^
relex.RelationExtractor -n 4 -l -t -f -g ^
-s "Alice wrote a book about dinosaurs for the University of California in Berkeley."
