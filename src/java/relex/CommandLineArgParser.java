package relex;
/*
 * Copyright 2008 Novamente LLC
 *
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
 */

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

/**
 * A utility class for reading operating system command lines. Imagine a
 * program which can be called like this: ProgramName [-f filename] [-o
 * readfile] [-x] [-y] This could be easily parsed by putting "-f" and "-o"
 * in argOpts and putting "-x" and "-y" in argFlags @param args the
 * operating system command line, as passed to main() @param argOpts a list
 * of option strings which should be followed by values @param argFlags a
 * list of flag strings which are not followed by values @return a map in
 * which any present options are mapped to their values and any present
 * flags are mapped to empty string.
 *
 * XXX This should be replaced by GNU getopt
 */
public class CommandLineArgParser {

    public static Map<String,String> parse(String[] args, HashSet<String> argOpts, HashSet<String> argFlags) {
        Map<String, String> map = new HashMap<String, String>();
        int i = 0;
        while (i < args.length) {
            if (argFlags != null) {
                Iterator<String> fI = argFlags.iterator();
                while (fI.hasNext()) {
                    String flag = fI.next();
                    if (flag.equals(args[i]))
                        map.put(flag, "");
                }
            }
            if (argOpts != null) {
                Iterator<String> oI = argOpts.iterator();
                while (oI.hasNext()) {
                    String opt = oI.next();
                    if (opt.equals(args[i])) {
                        map.put(opt, args[i + 1]);
                        ++i;
                    }
                }
            }
            ++i; // increment i
        }
        return map;
    }

    private static boolean test() {
        HashSet<String> opts = new HashSet<String>();
        HashSet<String> flags = new HashSet<String>();
        String[] args = new String[7];

        args[0] = new String("-a");
        args[1] = new String("aArg");
        args[2] = new String("-b");
        args[3] = new String("bArg");
        args[4] = new String("-c");
        args[5] = new String("cArg");
        args[6] = new String("-x");

        opts.add("-a");
        opts.add("-b");
        opts.add("-c");
        flags.add("-x");

        Map<String,String> argMap = parse(args, opts, flags);

        boolean success = true;
        for (int i = 0; i < 3; i++) {
            if (!(argMap.get(args[2 * i])).equals(args[2 * i + 1])) {
                success = false;
                System.out.println("Test Failure.");
            }
        }
        if (!argMap.get("-x").equals("")) {
            success = false;
            System.out.println("Test Failure.");
        }
        if (success)
            System.out.println("Test Success.");
        return success;

    }

    public static void main(String[] args) {
        if (test())
            ;
    }
}
