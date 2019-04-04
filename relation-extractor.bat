set DISCO_HOME=C:/disco

"%JAVA_HOME%\bin\java" ^
-Xmx1024m ^
-Djava.library.path=%DISCO_HOME%/native/windows ^
-Drelex.linkparserpath=%DISCO_HOME%/data/linkparser ^
-Dwordnet.configfile=data/wordnet/file_properties-win32.xml ^
-Drelex.algpath=data/relex-semantic-algs.txt ^
-classpath target\classes;target\lib\* ^
relex.RelationExtractor -n 4 -l -t -f ^
-s "Alice wrote a book about dinosaurs for the University of California in Berkeley."
