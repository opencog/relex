#!/usr/bin/python
"""
This simple script checks that frames are not disjunct and that variables
are accounted for.  It could be improved, but it might be useful as a sanity
check and as a basis of a more complete testing regime.

Takes the mapping_rules.txt file as an argument and inserts a warning comment
in front of each suspicious rule. 
"""

import sys
import re
import pdb

def main(argv=None):
    if argv is None:
        argv=sys.argv
    if len(argv) != 2:
        print "Usage: " + argv[0] + " [name of mapping rules file]"
        return 1

    f = open(argv[1], 'r')
    rule_re = re.compile("# IF (.+) THEN (.+)$")
    counter = 0
    warning_count = 0
    for line in f:
        counter+=1
        matches = rule_re.match(line)
        if matches is None:
            print line.strip()
            continue
        lhs,rhs = matches.groups()
        var_dict = {}
        prefix = "line " + str(counter) + ": "
        result = ""
        try:
            for x in get_vars(lhs):
                if not x[1].isupper():
                    var_dict[x] = 1
        except TypeError, e:
            result += "LHS has no vars; "
        try:
            for y in set(get_vars(rhs)):
                if not y[1].isupper():
                    if y not in var_dict:
                        result += "missing RHS var " + y + " from LHS; "
                    else:
                        var_dict[y] += 1
        except TypeError, e:
            result += "RHS has no vars; "
        #for x in var_dict:
            #if not x[1].isupper():
                #if var_dict[x] != 2:
                    #result += "missing LHS var " + x + " from RHS; "
        if has_loose_dollar_sign(line):
            result += "has loose dollar sign; "
        if has_disjunct_frames(rhs):
            result += "==has disjunct frames==!"
        if len(result) > 0:
            warning_count += 1
            #print prefix + line + "warning: " + result + "\n"
            print ";; warning: " + result
        print line.strip()
    #print "Warnings: " + str(warning_count)

def has_loose_dollar_sign(clause):
    var_re = re.compile("(\$ [\w]+)")
    matches = var_re.findall(clause)
    if matches:
        return True
    return False

def has_disjunct_frames(clause):
    var_re = re.compile("\((\$?[\w]+),(\$?[\w]+)\)")
    matches = var_re.findall(clause)
    if matches:
        frame_tag = None
        for m in matches:
            if frame_tag is None:
                frame_tag = m[0]
            else:
                if frame_tag != m[0]:
                    return True
    return False

def get_vars(clause):
    #tokens = clause.split(' ')
    #for t in tokens:
        # skip and, not, or
        #if lx in ['^','||','NOT']: continue
    var_re = re.compile("[^=](\$[\w]+)")
    matches = var_re.findall(" " + clause)
    return matches

if __name__ == "__main__":
    sys.exit(main())
