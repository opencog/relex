package relex.test;

import static junitparams.JUnitParamsRunner.$;

public class ConjunctionCases {
	
	public static Object[] provideConjoined() {
		return $(
			//conjoined verbs
			$("Scientists make observations and ask questions.",
                     "_obj(make, observation)\n" +
                     "_obj(ask, question)\n" +
                     "_subj(make, scientist)\n" +
                     "_subj(ask, scientist)\n" +
                     "conj_and(make, ask)\n"),
            //conjoined nouns              
			$("She is a student and an employee.",
			                     "_obj(be, student)\n" +
			                     "_obj(be, employee)\n" +
			                     "_subj(be, she)\n" +
	    	                     "conj_and(student, employee)\n"),
            //conjoined adjectives
			$("I hailed a black and white taxi.",
			                     "_obj(hail, taxi)\n" +
			                     "_subj(hail, I)\n" +
			                     "_amod(taxi, black)\n" +
                                 "_amod(taxi, white)\n" +
	    	                     "conj_and(black, white)\n"),
            //conjoined adverbs
             $("She ran quickly and quietly.",
	                     "_advmod(run, quickly)\n" +
	                     "_advmod(run, quietly)\n" +
	                     "_subj(run, she)\n" +
	                     "conj_and(quickly, quietly)\n"),
            //adjectival modifiers on conjoined subject          
             $("The big truck and the little car collided.",
	                     "_amod(car, little)\n" +
	                     "_amod(truck, big)\n" +
	                     "_subj(collide, truck)\n" +
                         "_subj(collide, car)\n" +
	                     "conj_and(truck, car)\n"),
            //verbs with modifiers
            $( "We ate dinner at home and went to the movies.",
	                     "_obj(eat, dinner)\n" +
	                     "conj_and(eat, go)\n" +
	                     "at(eat, home)\n" +
                         "_subj(eat, we)\n" +
                         "to(go, movie)\n" +
	                     "_subj(go, we)\n"),
            //verb with more modifiers
            $("We ate a late dinner at home and went out to the movies afterwards.",
	                     "_obj(eat, dinner)\n" +
	                     "conj_and(eat, go_out)\n" +
	                     "at(eat, home)\n" +
                         "_subj(eat, we)\n" +
                         "to(go_out, movie)\n" +
                         "_advmod(go_out, afterwards)\n" +
                         "_subj(go_out, we)\n" +
            			 "_amod(dinner, late)\n"),
            //conjoined ditransitive verbs 
            $("She baked him a cake and sang him a song.",
	                     "_iobj(sing, him)\n" +
	                     "_obj(sing, song)\n" +
	                     "_subj(sing, she)\n" +
                         "_iobj(bake, him)\n" +
                         "_obj(bake, cake)\n" +
                         "conj_and(bake, sing)\n" +
   	                     "_subj(bake, she)\n"),
            //conjoined adverbs with modifiers
            $("she ran very quickly and extremely quietly.",
	                     "_advmod(run, quickly)\n" +
	                     "_advmod(run, quietly)\n" +
	                     "_subj(run, she)\n" +
                         "_advmod(quietly, extremely)\n" +
                         "conj_and(quickly, quietly)\n" +
  	                     "_advmod(quickly, very)\n"), 
           //conjoined adverbs with out modifiers
            $("She handled it quickly and gracefully.",
	                     "_obj(handle, quickly)\n" +
	                     "_obj(handle, gracefully)\n" +
	                     "_advmod(handle, quickly)\n" +
                         "_advmod(handle, gracefully)\n" +
                         "_subj(handle, she)\n" +
  	                     "conj_and(quickly, gracefully)\n"), 
           //modifiers on conjoined adjectives
            $("He had very long and very white hair.",
	                     "_obj(have, hair)\n" +
	                     "_subj(have, he)\n" +
	                     "_amod(hair, long)\n" +
                         "_amod(hair, white)\n" +
                         "_advmod(white, very)\n" +
                         "conj_and(long, white)\n" +
  	                     "_advmod(long, very)\n"),
           //adjectival modifiers on conjoined object
            $("The collision was between the little car and the big truck.",
	                     "_pobj(between, car)\n" +
	                     "_pobj(between, truck)\n" +
	                     "_psubj(between, collision)\n" +
                         "_amod(truck, big)\n" +
                         "_amod(car, little)\n" +
                         "conj_and(car, truck)\n"),
            //Names Modifiers  and conjunction
            $("Big Tom and Angry Sue went to the movies.",
	                     "to(go, movie)\n" +
	                     "_subj(go, Big_Tom)\n" +
	                     "_subj(go, Angry_Sue)\n" +
                         "conj_and(Big_Tom, Angry_Sue)\n") );
	}
}