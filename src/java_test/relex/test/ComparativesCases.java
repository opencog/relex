package relex.test;

import static junitparams.JUnitParamsRunner.$;

public class ComparativesCases {
	
	public static Object[] provideGeneralComparatives() {
		return $(
				$("Some people like pigs less than dogs.",
					"_advmod(like, less)\n" +
	                "_obj(like, pig)\n" +
	                "_quantity(people, some)\n" +
	                "_subj(like, people)\n" +
	                "than(pig, dog)\n"),
				$("Some people like pigs more than dogs.",
	                "_advmod(like, more)\n" +
	                "_obj(like, pig)\n" +
	                "_quantity(people, some)\n" +
	                "_subj(like, people)\n" +
	                "than(pig, dog)\n") );
	}
	
	public static Object[] provideNonEqualGradableComparatives() {
		return $(
        	//Non-equal Gradable : Two entities one feature "more/less"
            $("He is more intelligent than John.",
        	    "than(he, John)\n" +
        	    "_comparative(intelligent, he)\n" +
        	    "degree(intelligent, comparative)\n"+
        	    "_predadj(he, intelligent)\n"),
            $("He is less intelligent than John.",
        	    "than(he, John)\n" +
        	    "_comparative(intelligent, he)\n" +
        	    "degree(intelligent, comparative)\n"+
        	    "_advmod(intelligent, less)\n"+
        	    "_predadj(he, intelligent)\n"),
    	    $("He runs more quickly than John.",
        	    "_advmod(run, quickly)\n" +
        	    "_subj(run, he)\n" +
        	    "than(he, John)\n" +
        	    "_comparative(quickly, run)\n" +
        	    "degree(quickly, comparative)\n"),
    	    $("He runs less quickly than John.",
        	    "_advmod(run, quickly)\n" +
        	    "_subj(run, he)\n" +
        	    "_advmod(quickly, less)\n"+
        	    "than(he, John)\n" +
        	    "_comparative(quickly, run)\n" +
        	    "degree(quickly, comparative)\n"),
            $("He runs more quickly than John does.",
        	    "_advmod(run, quickly)\n" +
        	    "_subj(run, he)\n" +
        	    "_subj(do, John)\n"+
        	    "than(he, John)\n" +
        	    "_comparative(quickly, run)\n" +
        	    "degree(quickly, comparative)\n") );
	}
        	    
	public static Object[] provideUngrammaticalButCommonlyUsedByNonNativeEnglishSpeakersComparatives() {
		return $(
            //This sentence is ungrammatical but commonly used by non-native English speakers 
            $("He runs less quickly than John does.",
        	    "_advmod(run, quickly)\n" +
        	    "_subj(run, he)\n" +
        	    "_subj(do, John)\n"+
        	    "_advmod(quickly, less)\n"+
        	    "than(he, John)\n" +
        	    "_comparative(quickly, run)\n" +
        	    "degree(quickly, comparative)\n"),
	    	$("He runs slower than John does.",
        	    "_advmod(run, slow)\n" +
        	    "_subj(run, he)\n" +
        	    "_subj(do, John)\n"+
        	    "than(he, John)\n" +
        	    "_comparative(slow, run)\n" +
        	    "degree(slow, comparative)\n"),
            $("He runs more than John.",
        	    "_obj(run, more)\n" +
        	    "_subj(run, he)\n" +
        	    "than(he, John)\n"+
        	    "_comparative(more, run)\n"+
        	    "degree(more, comparative)\n"),
            $("He runs less than John.",
        	    "_obj(run, less)\n" +
        	    "_subj(run, he)\n" +
        	    "than(he, John)\n"+
        	    "_comparative(less, run)\n"+
        	    "degree(less, comparative)\n"),
            $("He runs faster than John.",
            	    "than(he, John)\n" +
            	    "_comparative(fast, run)\n" +
            	    "_subj(run, he)\n"+
            	    "_advmod(run, fast)\n"+
            	    "degree(fast, comparative)\n"),
            $("He runs more slowly than John.",
            	    "than(he, John)\n" +
            	    "_subj(run, he)\n" +
            	    "_comparative(slowly, run)\n"+
            	    "_advmod(run, slowly)\n"+
            	    "degree(slowly, comparative)\n"),
            $("He runs less slowly than John.",
            	    "than(he, John)\n" +
            	    "_subj(run, he)\n" +
            	    "_comparative(slowly, run)\n"+
            	    "_advmod(run, slowly)\n"+
            	    "_advmod(slowly, less)\n"+
            	    "degree(slowly, comparative)\n"),
            $("He runs more miles than John does.",
            	    "than(he, John)\n" +
            	    "_subj(run, he)\n" +
            	    "_subj(do, John)\n"+
            	    "_obj(run, mile)\n"+
            	    "_comparative(mile, run)\n"+
            	    "_quantity(mile, more)\n"+
            	    "degree(more, comparative)\n"),
            $("He runs less miles than John does.",
            	    "than(he, John)\n" +
            	    "_subj(run, he)\n" +
            	    "_subj(do, John)\n"+
            	    "_obj(run, mile)\n"+
            	    "_comparative(mile, run)\n"+
            	    "_quantity(mile, less)\n"+
            	    "degree(less, comparative)\n"),
            $("He runs many more miles than John does.",
            	    "than(he, John)\n" +
            	    "_comparative(mile, run)\n"+
            	    "_obj(run, mile)\n"+
            	    "_subj(run, he)\n" +
            	    "_subj(do, John)\n" +
            	    "_quantity(mile, many)\n"+
            	    "degree(more, comparative)\n"),
            $("He runs fewer miles than John does.",
            	    "than(he, John)\n" +
            	    "_comparative(mile, run)\n"+
            	    "_obj(run, mile)\n"+
            	    "_subj(run, he)\n" +
            	    "_subj(do, John)\n" +
            	    "_quantity(mile, fewer)\n"+
            	    "degree(fewer, comparative)\n"),
            $("He runs ten more miles than John.",
            	    "_obj(run, mile)\n"+
            	    "_subj(run, he)\n" +
            	    "_quantity(mile, more)\n"+
            	    "than(he, John)\n" +
            	    "_comparative(mile, run)\n"+
            	    "_num_quantity(miles, ten)\n" +
            	    "degree(more, comparative)\n"),
            $("He runs almost ten more miles than John does.",
            	    "_obj(run, mile)\n"+
            	    "_subj(run, he)\n"+
            	    "_comparative(mile, run)\n"+
            	    "_subj(do, John)\n"+
            	    "than(he, John)\n"+
            	    "_quantity_mod(ten, almost)\n"+
            	    "_num_quantity(miles, ten)\n"+
            	    "degree(more, comparative)\n"),
            $("He runs more often than John.",
            	    "_subj(run, he)\n"+
            	    "_comparative(often, run)\n"+
            	    "than(he, John)\n"+
            	    "degree(often, comparative)\n"),
            $("He runs less often than John.",
            	    "_subj(run, he)\n"+
            	    "_comparative(often, run)\n"+
            	    "than(he, John)\n"+
            	    "degree(often, comparative)\n"),                				
            $("He runs here more often than John.",
            	    "_advmod(run, here)\n"+
            	    "_subj(run, he)\n"+
            	    "_comparative(often, run)\n"+
            	    "than(he, John)\n"+
            	    "degree(often, comparative)\n"),
            $("He runs here less often than John.",
            	    "_advmod(run, here)\n"+
            	    "_subj(run, he)\n"+
            	    "_comparative(often, run)\n"+
            	    "than(he, John)\n"+
            	    "degree(often, comparative)\n"),
            $("He is faster than John.",
            	    "than(he, John)\n"+
            	    "_predadj(he, fast)\n"+
            	    "_comparative(fast, be)\n"+
            	    "degree(fast, comparative)\n"),
            $("He is faster than John is.",
            	    "than(he, John)\n"+
            	    "_predadj(he, fast)\n"+
            	    "_subj(be, John)\n"+
            	    "_comparative(fast, be)\n"+
            	    "degree(fast, comparative)\n"),
            $("His speed is faster than John's.",
            	    "than(speed, be)\n"+
            	    "_predadj(speed, fast)\n"+
            	    "_poss(speed, him)\n"+
            	    "_comparative(fast, be)\n"+
            	    "degree(fast, comparative)\n"),
            $("I run more than Ben.",
            	    "_subj(run, I)\n"+
            	    "_adv(run, more)\n"+
            	    "_comparative(more, run)\n"+
            	    "than(I, Ben)\n"+
            	    "degree(more, comparative)\n"),
            $("I run less than Ben.",
            	    "_subj(run, I)\n"+
            	    "_adv(run, less)\n"+
            	    "_comparative(less, run)\n"+
            	    "than(I, Ben)\n"+
            	    "degree(less, comparative)\n"),
            $("I run more miles than Ben.",
            	    "_subj(run, I)\n"+
            	    "_obj(run, mile)\n"+
            	    "_quantity(mile, more)\n"+
            	    "_comparative(mile, run)\n"+
            	    "than(I, Ben)\n"+
            	    "degree(more, comparative)\n"),
            $("I run fewer miles than Ben.",
            	    "_subj(run, I)\n"+
            	    "_obj(run, mile)\n"+
            	    "_quantity(mile, fewer)\n"+
            	    "_comparative(mile, run)\n"+
            	    "than(I, Ben)\n"+
            	    "degree(fewer, comparative)\n"),
            $("I run 10 more miles than Ben.",
            	    "_subj(run, I)\n"+
            	    "_obj(run, mile)\n"+
            	    "_num_quantity(mile, 10)\n"+
            	    "_quantity_mod(10, more)\n"+
            	    "_comparative(mile, run)\n"+
            	    "than(I, Ben)\n"+
            	    "degree(more, comparative)\n"),
            $("I run 10 fewer miles than Ben.",
            	    "_subj(run, I)\n"+
            	    "_obj(run, mile)\n"+
            	    "_num_quantity(mile, 10)\n"+
            	    "_quantity_mod(10, fewer)\n"+
            	    "_comparative(mile, run)\n"+
            	    "than(I, Ben)\n"+
            	    "degree(fewer, comparative)\n"),
            $("I run more often than Ben.",
            	    "_subj(run, I)\n"+
            	    "_advmod(run, often)\n"+
            	    "_comparative(often, run)\n"+
            	    "than(I, Ben)\n"+
            	    "degree(often, comparative)\n"+
            	    "_advmod(often, more)\n"),
            $("I run less often than Ben.",
            	    "_subj(run, I)\n"+
            	    "_advmod(run, often)\n"+
            	    "_comparative(often, run)\n"+
            	    "than(I, Ben)\n"+
            	    "degree(often, comparative)\n"+
            	    "_advmod(often, less)\n"),
            $("I run more often than Ben does.",
            	    "_subj(run, I)\n"+
            	    "_subj(do, Ben)\n"+
            	    "_advmod(run, often)\n"+
            	    "_comparative(often, run)\n"+
            	    "than(I, Ben)\n"+
            	    "degree(often, comparative)\n"+
            	    "_advmod(often, more)\n"),
            $("I run less often than Ben does.",
            	    "_subj(run, I)\n"+
            	    "_subj(do, Ben)\n"+
            	    "_advmod(run, often)\n"+
            	    "_comparative(often, run)\n"+
            	    "than(I, Ben)\n"+
            	    "degree(often, comparative)\n"+
            	    "_advmod(often, less)\n"),
            $("I run more often than Ben climbs.",
            	    "_subj(run, I)\n"+
            	    "_subj(climb, Ben)\n"+
            	    "_comparative(often, run)\n"+
            	    "than(I, Ben)\n"+
            	    "than1(run, climb)\n"+
            	    "degree(often, comparative)\n"+
            	    "_advmod(often, more)\n"),
            $("I run less often than Ben climbs.",
            	    "_subj(run, I)\n"+
            	    "_subj(climb, Ben)\n"+
            	    "_comparative(often, run)\n"+
            	    "than(I, Ben)\n"+
            	    "than1(run, climb)\n"+
            	    "degree(often, comparative)\n"+
            	    "_advmod(often, less)\n"),
            $("I run more races than Ben wins contests.",
            	    "_subj(run, I)\n"+
            	    "_obj(run, race)\n"+
            	    "_subj(win, Ben)\n"+
            	    "_obj(win, contest)\n"+
            	    "_quantity(race, more)\n"+
            	    "_comparative(race, run)\n"+
            	    "than(I, Ben)\n"+
            	    "than1(run, climb)\n"+
            	    "than2(race, contest)\n"+
            	    "degree(more, comparative)\n"),
            $("I run fewer races than Ben wins contests.",
            	    "_subj(run, I)\n"+
            	    "_obj(run, race)\n"+
            	    "_subj(win, Ben)\n"+
            	    "_obj(win, contest)\n"+
            	    "_quantity(race, fewer)\n"+
            	    "_comparative(race, run)\n"+
            	    "than(I, Ben)\n"+
            	    "than1(run, climb)\n"+
            	    "than2(race, contest)\n"+
            	    "degree(fewer, comparative)\n"),
            $("I have more chairs than Ben.",
            	    "_obj(have, chair)\n"+
            	    "_subj(have, I)\n"+
            	    "than(I, Ben)\n"+
            	    "_comparative(chair, have)\n"+
            	    "_quantity(chair, more)\n"+
            	    "_advmod(have, more)\n"+
            	    "degree(more, comparative)\n"),
            $("I have fewer chairs than Ben.",
            	    "_obj(have, chair)\n"+
            	    "_subj(have, I)\n"+
            	    "than(I, Ben)\n"+
            	    "_comparative(chair, have)\n"+
            	    "_quantity(chair, fewer)\n"+
            	    "_advmod(have, fewer)\n"+
            	    "degree(fewer, comparative)\n"),
            $("He earns much more money than I do.",
            	    "_obj(earn, money)\n"+
            	    "_subj(do, I)\n"+
            	    "_subj(earn, he)\n"+
            	    "than(he,I)\n"+
            	    "_comparative(money,earn)\n"+
            	    "_advmod(earn, much)\n"+
            	    "_quantity(money, more)\n"+
            	    "_advmod(more, much)\n"+
            	    "degree(more,comparative)\n"),
            $("He earns much less money than I do.",
            	    "_obj(earn, money)\n"+
            	    "_subj(do, I)\n"+
            	    "_subj(earn, he)\n"+
            	    "than(he, I)\n"+
            	    "_comparative(money, earn)\n"+
            	    "_advmod(earn, much)\n"+
            	    "_quantity(money, less)\n"+
            	    "_advmod(less, much)\n"+
            	    "degree(less, comparative)\n"),
            $("She comes here more often than her husband.",
            	    "_advmod(come, here)\n"+
            	    "_subj(come, she)\n"+
            	    "_poss(husband, her)\n"+
            	    "_comparative(often, come)\n"+
            	    "than(she, husband)\n"+
            	    "degree(often, comparative)\n"),
            $("She comes here less often than her husband.",
            	    "_advmod(come, here)\n"+
            	    "_subj(come, she)\n"+
            	    "_poss(husband, her)\n"+
            	    "_comparative(often, come)\n"+
            	    "than(she, husband)\n"+
            	    "degree(often, comparative)\n"),
            $("Russian grammar is more difficult than English grammar.",
            	    "_comparative(difficult, grammar)\n"+
            	    "than(grammar, grammar)\n"+
            	    "_amod(grammar, Russian)\n"+ //When link-grammar uses A, relex should use _amod it will use A instead of AN; will be  updated in next linkgrammer version
            	    "_predadj(grammar, difficult)\n"+
            	    "_amod(grammar, English)\n"+
            	    "degree(difficult, comparative)\n"),
            $("Russian grammar is less difficult than English grammar.",
            	    "_comparative(difficult, grammar)\n"+
            	    "than(grammar, grammar)\n"+
            	    "_amod(grammar, Russian)\n"+
            	    "_predadj(grammar, difficult)\n"+
            	    "_amod(grammar, English)\n"+
            	    "_advmod(difficult, less)\n"+
            	    "degree(difficult, comparative)\n"),
            $("My sister is much more intelligent than me.",
            	    "_amod(much, intelligent)\n"+
            	    "_predadj(sister, intelligent)\n"+
            	    "_poss(sister, me)\n"+
            	    "than(sister, me)\n"+
            	    "_comparative(intelligent, sister)\n"+
            	    "degree(intelligent, comparative)\n"),
            $("My sister is much less intelligent than me.",
            	    "_amod(much, intelligent)\n"+
            	    "_predadj(sister, intelligent)\n"+
            	    "_poss(sister, me)\n"+
            	    "than(sister, me)\n"+
            	    "_comparative(intelligent, sister)\n"+
            	    "_advmod(intelligent, less)\n"+
            	    "degree(intelligent, comparative)\n"),
            $("I find maths lessons more enjoyable than science lessons.",
            	    "_iobj(find, maths)\n"+
            	    "_obj(find, lesson)\n"+
            	    "_subj(find, I)\n"+
            	    "_amod(lesson, enjoyable)\n"+
            	    "_nn(lesson, science)\n"+
            	    "than(maths, science)\n"+
            	    "_comparative(enjoyable, maths)\n"+
            	    "degree(enjoyable, comparative)\n"),
            $("I find maths lessons less enjoyable than science lessons.",
            	    "_iobj(find, maths)\n"+
            	    "_obj(find, lesson)\n"+
            	    "_subj(find, I)\n"+
            	    "_amod(lesson, enjoyable)\n"+
            	    "_nn(lesson, science)\n"+
            	    "than(maths, science)\n"+
            	    "_comparative(enjoyable, maths)\n"+
            	    "_advmod(enjoyable, less)\n"+
            	    "degree(enjoyable, comparative)\n") );
	}

}