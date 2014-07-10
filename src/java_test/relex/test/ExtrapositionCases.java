package relex.test;

import static junitparams.JUnitParamsRunner.$;

public class ExtrapositionCases {
	
	public static Object[] provideGeneralExtraposition() {
		return $(
			$("The woman who lives next door is a registered nurse.",
                    "_obj(be, nurse)\n" +
                    "_subj(be, woman)\n" +
                    "_amod(nurse, registered)\n" +
                    "_advmod(live, next_door)\n" +
                    "_subj(live, woman)\n" +
                    "who(woman, live)\n"),
            $("A player who is injured has to leave the field.",
                    "_to-do(have, leave)\n" +
                    "_subj(have, player)\n" +
                    "_obj(leave, field)\n" +
                    "_predadj(player, injured)\n" +
                    "who(player, injured)\n"),
            $("Pizza, which most people love, is not very healthy.",
                    "_advmod(very, not)\n" +
                    "_advmod(healthy, very)\n" +
                    "_obj(love, Pizza)\n" +
                    "_quantity(people, most)\n" +
                    "which(Pizza, love)\n" +
                    "_subj(love, people)\n" +
                    "_predadj(Pizza, healthy)\n"),
            $("The restaurant which belongs to my aunt is very famous.",
                    "_advmod(famous, very)\n" +
                    "to(belong, aunt)\n" +
                    "_subj(belong, restaurant)\n" +
                    "_poss(aunt, me)\n" +
                    "which(restaurant, belong)\n" +
                    "_predadj(restaurant, famous)\n"),
            $("The books which I read in the library were written by Charles Dickens.",
                    "_obj(write, book)\n" +
                    "by(write, Charles_Dickens)\n" +
                    "_obj(read, book)\n" +
                    "in(read, library)\n" +
                    "_subj(read, I)\n" +
                    "which(book, read)\n"),
            $("This is the book whose author I met in a library.",
                   "_obj(be, book)\n" +
                   "_subj(be, this)\n" +
                   "_obj(meet, author)\n" +
                   "in(meet, library)\n" +
                   "_subj(meet, I)\n" +
                   "whose(book, author)\n"),
            $("The book that Jack lent me is very boring.",
                   "_advmod(boring, very)\n" +
                   "_iobj(lend, book)\n" +
                   "_obj(lend, me)\n" +
                   "_subj(lend, Jack)\n" +
                   "that_adj(book, lend)\n" +
                   "_predadj(book, boring)\n"),
            $("They ate a special curry which was recommended by the restaurant’s owner.",
                   "_obj(eat, curry)\n" +
                   "_subj(eat, they)\n" +
                   "_obj(recommend, curry)\n" +
                   "by(recommend, owner)\n" +
                   "_poss(owner, restaurant)\n" +
                   "which(curry, recommend)\n" +
                   "_amod(curry, special)\n"),
            $("The dog who Jack said chased me was black.",
                   "_obj(chase, me)\n" +
                   "_subj(chase, dog)\n" +
                   "_subj(say, Jack)\n" +
                   "_predadj(dog, black)\n" +
                   "who(dog, chase)\n"),
            $("Jack, who hosted the party, is my cousin.",
                   "_obj(be, cousin)\n" +
                   "_subj(be, Jack)\n" +
                   "_poss(cousin, me)\n" +
                   "_obj(host, party)\n" +
                   "_subj(host, Jack)\n" +
                   "who(Jack, host)\n"),
            $("Jack, whose name is in that book, is the student near the window.",
                   "near(be, window)\n" +
                   "_obj(be, student)\n" +
                   "_subj(be, Jack)\n" +
                   "_pobj(in, book)\n" +
                   "_psubj(in, name)\n" +
                   "_det(book, that)\n" +
                   "whose(Jack, name)\n"),
            $("Jack stopped the police car that was driving fast.",
                   "_obj(stop, car)\n" +
                   "_subj(stop, Jack)\n" +
                   "_advmod(drive, fast)\n" +
                   "_subj(drive, car)\n" +
                   "that_adj(car, drive)\n" +
                   "_nn(car, police)\n"),
            $("Just before the crossroads, the car was stopped by a traffic sign that stood on the street.",
                   "_obj(stop, car)\n" +
                   "by(stop, sign)\n" +
                   "_advmod(stop, just)\n" +
                   "on(stand, street)\n" +
                   "_subj(stand, sign)\n" +
                   "that_adj(sign, stand)\n" +
                   "_nn(sign, traffic)\n" +
                   "before(just, crossroads)\n") );
	}
}