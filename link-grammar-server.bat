set DISCO_HOME=C:/disco
java ^
-Xmx1024m ^
-Djava.library.path=%DISCO_HOME%/native/windows ^
-Drelex.linkparserpath=%DISCO_HOME%/data/linkparser ^
-Dwordnet.configfile=data/wordnet/file_properties-win32.xml ^
-Drelex.algpath=data/relex-semantic-algs.txt ^
-classpath target\classes;target\lib\* ^
relex.parser.LinkParserServer %1
