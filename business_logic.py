'''
Created on Nov 17, 2014

@author: matthias
'''


'''
[BE] {3} <SVO, SV> _subj(be, $subj) & _obj(be, $obj(be, $obj) => (be-inheritance-rule $subj (get-instance-name $subj word_index sentence_index) $obj (get-instance-name $obj word_index sentence_index))
'''                      

    
with open('RuleImplicationToRelEx2Logic.txt','r') as f:
    for line in f:
        words_list = line.split()

        #retrive just '_subj'
        if len(words_list) >= 4:
            if(  words_list[4].find('(') != -1 ):
                c = words_list[4].split("(")
                _subj = c[0]
                
        #retrive just '_obj'
        if len(words_list) >= 7:
            if(  words_list[7].find('(') != -1 ):
                c = words_list[7].split("(")
                _obj = c[0]
        
        if len(words_list) >= 10:
            if(  words_list[10].find('(') != -1 ):
                be_inheritance = words_list[10].replace('(', '').replace('-rule', '')
                pre_inheritance = words_list[10].replace('(', '').replace('-rule', '').replace('b', 'pr')
                #print("twelve: "+ be_inheritance)
                #print("twelve: "+ pre_inheritance)
                  
        
        if( words_list[0] == '[BE]'):
            print("(define "+ be_inheritance, "\n\t" \
                  "(BlindLink\n\t\t" \
                  "(ListLink\n\t\t\t" \
                  "(TypedVariableLink\n\t\t\t\t" \
                  "(VariableNode \"$a-parse\")\n\t\t\t\t" \
                  "(VariableNode \"ParseNode\")\n\t\t\t" \
                  ")\n\t\t\t" \
                  "(TypedVariableLink\n\t\t\t\t" \
                  "(VariableNode \"$X\")\n\t\t\t\t" \
                  "(VariableNode \"WordTnstanceNode\")\n\t\t\t" \
                  ")\n\t\t\t" \
                  "(TypedVariableLink\n\t\t\t\t" \
                  "(VariableNode \"$Y\")\n\t\t\t\t" \
                  "(VariableNode \"WordTnstanceNode\")\n\t\t\t" \
                  ")\n\t\t\t" \
                  "(TypedVariableLink\n\t\t\t\t" \
                  "(VariableNode \"$Z\")\n\t\t\t\t" \
                  "(VariableNode \"WordTnstanceNode\")\n\t\t\t" \
                  ")\n\t\t" \
                  ")\n\t\t" \
                  "(ImplicationLink\n\t\t\t" \
                  "(AndLink\n\t\t\t\t" \
                  "(WordInstanceLink\n\t\t\t\t\t" \
                  "(VariableNode \"$X\")\n\t\t\t\t\t" \
                  "(VariableNode \"$a-parse\")\n\t\t\t\t" \
                  ")\n\t\t\t\t" \
                  "(WordInstanceLink\n\t\t\t\t\t" \
                  "(VariableNode \"$Y\")\n\t\t\t\t\t" \
                  "(VariableNode \"$a-parse\")\n\t\t\t\t" \
                  ")\n\t\t\t\t" \
                  "(WordInstanceLink\n\t\t\t\t\t" \
                  "(VariableNode \"$Z\")\n\t\t\t\t\t" \
                  "(VariableNode \"$a-parse\")\n\t\t\t\t" \
                  ")\n\t\t\t\t" \
                  "(EvaluationLink\n\t\t\t\t\t" \
                  "(DefinedLinguisticRelationshipNode \""+ _subj +"\")\n\t\t\t\t\t" \
                  "(ListLink\n\t\t\t\t\t\t" \
                  "(VariableNode \"$Y\")\n\t\t\t\t\t\t" \
                  "(VariableNode \"$X\")\n\t\t\t\t\t" \
                  ")\n\t\t\t\t" \
                  ")\n\t\t\t\t" \
                  "(EvaluationLink\n\t\t\t\t\t" \
                  "(DefinedLinguisticRelationshipNode \""+ _obj +"\")\n\t\t\t\t\t" \
                  "(ListLink\n\t\t\t\t\t\t" \
                  "(VariableNode \"$Y\")\n\t\t\t\t\t\t" \
                  "(VariableNode \"$X\")\n\t\t\t\t\t" \
                  ")\n\t\t\t\t" \
                  ")\n\t\t\t\t" \
                  "(LemmaLink\n\t\t\t\t\t" \
                  "(VariableNode \"$Y\")\n\t\t\t\t\t" \
                  "(WordNode \""+ be_inheritance +"\")\n\t\t\t\t" \
                  ")\n\t\t\t" \
                  ")\n\t\t\t" \
                  "(EvaluationLink\n\t\t\t\t" \
                  "(GroundedSchemaNode \"scm: "+ pre_inheritance+"\")\n\t\t\t\t" \
                  "(ListLink\n\t\t\t\t\t" \
                  "(VariableNode \"$X\")\n\t\t\t\t\t" \
                  "(VariableNode \"$Z\")\n\t\t\t\t" \
                  ")\n\t\t\t" \
                  ")\n\t\t" \
                  ")\n\t" \
                  ")\n" \
                  ")\n" \
                  "\n" \
                  "(InheritanceLink (stv 1 .99 ) (ConceptNode \"" + words_list[0].replace(']', '').replace('[', '') + "-Rule\") (ConceptNode \"Rule\"))\n" \
                  "\n" \
                  "(ReferenceLink (stv 1 .99 ) (ConceptNode \"" + words_list[0].replace(']', '').replace('[', '') + "-Rule\") "+be_inheritance+")\n" \
                  "\n" \
                  "; This is function is not needed. It is added so as not to break the existing\n" \
                  "; r2l pipeline.\n" \
                  "(define (pre-be-inheritance-rule subj obj)\n\t" \
                  "("+be_inheritance+"-rule (word-inst-get-word-str subj) (cog-name subj)\n\t\t" \
                  "(word-inst-get-word-str obj) (cog-name obj)\n\t" \
                  ")\n" \
                  ")\n"
                  )
            
        
        



            
    