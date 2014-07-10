/*
 * This class converts the relation extracted using RelEx to OWL
 * by Rui P. Costa (adapted from SimpleView and RelationView)
 *
 * Created in 2008 by "Rui Costa" <racosta@student.dei.uc.pt>,
 *                     Rui P. Costa <b4h0pe@gmail.com>
 */
package relex.output;

import java.net.URI;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;
import org.semanticweb.owlapi.model.OWLAnnotationSubject;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChangeException;
import org.semanticweb.owlapi.model.OWLOntologyFormat;
import org.semanticweb.owlapi.model.OWLOntologyIRIMapper;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLProperty;
import org.semanticweb.owlapi.model.PrefixManager;
import org.semanticweb.owlapi.util.SimpleIRIMapper;

import relex.ParsedSentence;
import relex.feature.FeatureNode;
import relex.feature.RelationCallback;

/**
 * Implements a very simple, direct printout of the
 * RelEx feature graph in OWL.
 *
 * Copyright (c) 2008 Linas Vepstas <linas@linas.org> and Rui P. Costa <b4h0pe@gmail.com>
 */
public class OWLView
{

	private String sent;
	private OWLClass sentence;
	private OWLClass word;
	private OWLClass relex_word;
	private HashMap<String,OWLIndividual> map_owl_relexwords;
	private HashMap<String,OWLProperty> map_owl_properties;
	private HashMap<String,OWLIndividual> phr_type_map_owl;

	private OWLOntologyManager manager;
	private URI ontologyURI;
	private URI physicalURI;
	private OWLOntology ontology;
	private OWLDataFactory factory;
	private OWLIndividual sentenceInd;
	private int sentence_id;
	private OWLObjectProperty has;
	boolean viz = false;
	boolean viz_sentence = false;

	public OWLView() {
		initOntology(); //Change to the constructor in order to happen just one time
	}

	public OWLView(boolean viz, boolean viz_sentence) {
		initOntology(); //Change to the constructor in order to happen just one time

		this.viz = viz;
		this.viz_sentence = viz_sentence;
	}



	/**
	* Print out RelEx relations. All relations shown
	* in a binary form.
	*
	* Example:
	*       _subj(throw, John)
	*       _obj(throw, ball)
	*       tense(throw, past)
	*       definite-FLAG(ball, T)
	*       noun_number(ball, singular)
	*/
	public void printRelations(ParsedSentence parse, String sentence, int sentence_id, String ontologyname)
	{
		try
		{
			sent = sentence;

			//Add the sentence to Sentence Class
			this.sentence_id = sentence_id;
			sentenceInd = factory.getOWLNamedIndividual(IRI.create(ontologyURI + "#" + "sentence_" + sentence_id));
			//OWLAnnotationProperty p = new OWLAnnotationPropertyImpl(IRI.create(sentence));

			//OWLAnnotation label = factory.getOWLAnnotation(sentence);
			OWLOntologyFormat ontologyFormat = manager.getOntologyFormat(ontology);
			OWLAnnotation label = (OWLAnnotation) factory.getOWLAnnotationProperty(sentence, (PrefixManager) ontologyFormat);

			OWLClassAssertionAxiom sentClass = factory.getOWLClassAssertionAxiom(this.sentence,sentenceInd);
			OWLAnnotationAssertionAxiom labelClass = factory.getOWLAnnotationAssertionAxiom((OWLAnnotationSubject) sentClass, label);
			manager.applyChange(new AddAxiom(ontology, sentClass));
			manager.applyChange(new AddAxiom(ontology, labelClass));

			printRelations(parse, null);

		}
		catch (OWLOntologyChangeException ex)
		{
			Logger.getLogger(OWLView.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	public void initOntology()
	{
		try
		{
			//Auxiliar structures
			//map_owl_relexwords = new HashMap<String,OWLIndividual>();
			map_owl_properties = new HashMap<String,OWLProperty>();

			// OWLOntologyManager that manages a set of ontologies
			manager = OWLManager.createOWLOntologyManager();

			// URI of the ontology
			ontologyURI = URI.create("http://student.dei.uc.pt/~racosta/relex/owl_format.owl");

			// Pphysical URI
			physicalURI = URI.create("file:/media/Docs/uc/MSc-2/SW/Project/ontologies/relex2.owl");

			// Set up a mapping, which maps the ontology URI to the physical URI
			OWLOntologyIRIMapper mapper = new SimpleIRIMapper(IRI.create(ontologyURI),IRI.create(physicalURI));
			manager.addIRIMapper(mapper);

			// Now create the ontology - we use the ontology URI
			ontology = manager.createOntology(IRI.create(physicalURI));
			//Data factory, allows to manipulate ontology data
			factory = manager.getOWLDataFactory();

			sentence = factory.getOWLClass(IRI.create(ontologyURI + "#Sentence"));
			word = factory.getOWLClass(IRI.create(ontologyURI + "#Word"));
			//relex_word = factory.getOWLClass(IRI.create(ontologyURI + "#Relex_word"));

			//Generic properties for classes
			has = factory.getOWLObjectProperty(IRI.create(ontologyURI + "#relex_has"));
			//map_owl_properties.put("type",factory.getOWLObjectProperty(IRI.create(ontologyURI + "#type")));

			// word is subclass of phrase
			//OWLAxiom subclassing = factory.getOWLSubClassAxiom(word, sentence);

			// Add the axiom to the ontology
			//AddAxiom addAxiom = new AddAxiom(ontology, subclassing);
			// We now use the manager to apply the change
			//manager.applyChange(addAxiom);

			//2º Add the predefined properties

			/*map_owl_properties.put("tense",factory.getOWLObjectProperty(IRI.create(ontologyURI + * "#p_tense")));
			map_owl_properties.put("index",factory.getOWLDataProperty(IRI.create(ontologyURI + "#p_index")));
			//Add possible relex words
			map_owl_relexwords.put("masculine",factory.getOWLNamedIndividual(IRI.create(ontologyURI + "#rw_masculine")));
			map_owl_relexwords.put("feminine",factory.getOWLNamedIndividual(IRI.create(ontologyURI + "#rw_feminine")));
			map_owl_relexwords.put("person",factory.getOWLNamedIndividual(IRI.create(ontologyURI + "#rw_person")));
			map_owl_relexwords.put("neuter",factory.getOWLNamedIndividual(IRI.create(ontologyURI + "#rw_neuter")));*/

			/*OWLObjectProperty number = factory.getOWLObjectProperty(IRI.create(ontologyURI + "#number"));
			OWLObjectProperty tense = factory.getOWLObjectProperty(IRI.create(ontologyURI + "#tense"));
			OWLObjectProperty query = factory.getOWLObjectProperty(IRI.create(ontologyURI + "#query"));
			OWLObjectProperty quantification = factory.getOWLObjectProperty(IRI.create(ontologyURI + "#quantification"));*/

			// Add axioms to the ontology
			//OWLAxiom genderax = factory.getOWLObjectProperty(infinitive);

			//Phrase Individuals
			/*phr_type_map_owl = new HashMap<String,OWLIndividual>();
			phr_type_map_owl.put("Adverbial Phrase", factory.getOWLNamedIndividual(IRI.create(ontologyURI + "#rw_adjective")));
			phr_type_map_owl.put("Adverbial Phrase", factory.getOWLNamedIndividual(IRI.create(ontologyURI + "#rw_adverb")));
			phr_type_map_owl.put("Noun Phrase",      factory.getOWLNamedIndividual(IRI.create(ontologyURI + "#rw_noun")));
			phr_type_map_owl.put("Prepositional Phrase", factory.getOWLNamedIndividual(IRI.create(ontologyURI + "#rw_prepositional")));
			phr_type_map_owl.put("Particle",         factory.getOWLNamedIndividual(IRI.create(ontologyURI + "#rw_particle")));
			phr_type_map_owl.put("Quantifier Phrase", factory.getOWLNamedIndividual(IRI.create(ontologyURI + "#rw_quantifier")));
			phr_type_map_owl.put("Clause",           factory.getOWLNamedIndividual(IRI.create(ontologyURI + "#rw_clause")));
			phr_type_map_owl.put("Subordinate Clause", factory.getOWLNamedIndividual(IRI.create(ontologyURI + "#rw_subordinate")));
			phr_type_map_owl.put("Subject Inverted", factory.getOWLNamedIndividual(IRI.create(ontologyURI + "#rw_inverted")));
			phr_type_map_owl.put("Sentence",         factory.getOWLNamedIndividual(IRI.create(ontologyURI + "#rw_root")));
			phr_type_map_owl.put("Verb Phrase",      factory.getOWLNamedIndividual(IRI.create(ontologyURI + "#rw_verb")));
			phr_type_map_owl.put("Wh-Adverb Phrase", factory.getOWLNamedIndividual(IRI.create(ontologyURI + "#rw_wh-adverb")));
			phr_type_map_owl.put("Wh-Noun Phrase",   factory.getOWLNamedIndividual(IRI.create(ontologyURI + "#rw_wh-noun")));
			phr_type_map_owl.put("Wh-Prepositional Phrase", factory.getOWLNamedIndividual(IRI.create(ontologyURI + "#rw_wh-prep")));*/

			//Add all the phr_type_map_owl Individuals to the Relex_word Class
			/*Set<String> s = phr_type_map_owl.keySet();
			for (Iterator<String> it = s.iterator(); it.hasNext();)
			{
				manager.applyChange(new AddAxiom(ontology, factory.getOWLClassAssertionAxiom(phr_type_map_owl.get(it.next()), relex_word)));
			}

			//Add all the map_owl_relexwords Individuals to the Relex_word Class
			s = map_owl_relexwords.keySet();
			for (Iterator<String> it = s.iterator(); it.hasNext();)
			{
				manager.applyChange(new AddAxiom(ontology, factory.getOWLClassAssertionAxiom(map_owl_relexwords.get(it.next()), relex_word)));
			}*/

		}
		catch (OWLException e)
		{
			e.printStackTrace();
		}
	}


	public void printRelations(ParsedSentence parse, HashMap<FeatureNode,String> map)
	{
		Visit v = new Visit();
		v.id_map = map;
		v.str = "";
		parse.foreach(v);
	}

	public void saveOWL(String path)
	{
		try
		{
			physicalURI = URI.create("file:" + path);

			manager.setOntologyDocumentIRI(ontology,IRI.create(physicalURI));

			manager.saveOntology(ontology);
		}
		catch (OWLException ex)
		{
			Logger.getLogger(OWLView.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	/**
	* Print out RelEx relations, alternate format.
	* Unary relations, including booleans, doen't show
	* the attribute name.
	*
	* Example:
	*       _subj(throw, John)
	*       _obj(throw, ball)
	*       past(throw)
	*       definite(ball)
	*       singular(ball)
	*/
	public String printRelationsAlt(ParsedSentence parse)
	{
		return printRelationsAlt(parse, null);
	}

	public String printRelationsAlt(ParsedSentence parse, HashMap<FeatureNode,String> map)
	{
		Visit v = new Visit();
		v.id_map = map;
		v.unaryStyle = true;
		v.str = "";
		parse.foreach(v);

		return v.str;
	}

	private class Visit implements RelationCallback
	{
		// Map associating a feature-node to a unique ID string.
		public HashMap<FeatureNode,String> id_map = null;

		public Boolean unaryStyle = false;
		public String str;
		public Boolean BinaryHeadCB(FeatureNode node) { return false; }

		public Boolean BinaryRelationCB(String relName,
												FeatureNode srcNode,
												FeatureNode tgtNode)
		{
			String srcName = srcNode.get("name").getValue();
			FeatureNode tgt = tgtNode.get("name");
			if (tgt == null)
			{
				System.out.println("Error: No target! rel=" + relName + " and src=" + srcName);
				return false;
			}
			String tgtName = tgt.getValue();
			if (id_map != null)
			{
				srcName = id_map.get(srcNode);
				tgtName = id_map.get(tgtNode);
			}
			//Optimize using StringBuilder
			//Get the Individual type (noun, etc.) and the type_property
			System.out.println("\n\tRELATION (binary) = " + relName + "(" + srcName + ", " + tgtName + ")\n");

			//Cleaning
			if (relName.charAt(0)=='_')
				relName = relName.replaceFirst("_", "");
			if (relName.length()>1)
				if (relName.charAt(1)=='%')
					relName = relName.replaceFirst("%", "");

			if (tgtName.contains("[") || tgtName.contains("]") || srcName.contains("[") || srcName.contains("]") ||
				tgtName.equals("WORD") || srcName.equals("WORD")  || tgtName.contains("misc-") || srcName.contains("misc-")) return false;

			if (srcName.length()>0 && tgtName.length()>0)
			{
				//1º Add the first term to Word Class
				srcName=srcName.replaceAll("[\\%\\s]", "");
				//System.out.println("srcName = " + srcName);
				OWLIndividual src_word = factory.getOWLNamedIndividual(IRI.create(ontologyURI + "#" + srcName));
				OWLClassAssertionAxiom addSrcWord = factory.getOWLClassAssertionAxiom(word,src_word);
				manager.applyChange(new AddAxiom(ontology, addSrcWord));

				//2º Create the property
				relName = relName.replaceAll("[\\%\\ss]", "");
				OWLObjectProperty rel = factory.getOWLObjectProperty(IRI.create(ontologyURI + "#" + relName));

				//3º Add the second term to Word Class
				tgtName = tgtName.replaceAll("[\\%\\s]", "");
				OWLIndividual dst_word = factory.getOWLNamedIndividual(IRI.create(ontologyURI + "#" + tgtName));
				OWLClassAssertionAxiom addDstWord = factory.getOWLClassAssertionAxiom(word,dst_word );
				manager.applyChange(new AddAxiom(ontology, addDstWord));

				//4º Create axiom for the relation
				OWLObjectPropertyAssertionAxiom addrel = factory.getOWLObjectPropertyAssertionAxiom(rel,src_word, dst_word);
				manager.applyChange(new AddAxiom(ontology, addrel));

				//5º Add the words (Class) to the sentence (Class)
				OWLObjectPropertyAssertionAxiom addw1 = factory.getOWLObjectPropertyAssertionAxiom(has,sentenceInd, src_word);
				//OWLObjectPropertyAssertionAxiom addw2 = factory.getOWLObjectPropertyAssertionAxiom(sentenceInd, has,
				//dst_word);


				manager.applyChange(new AddAxiom(ontology, addw1));
				//manager.applyChange(new AddAxiom(ontology, addw2));
			}

			return false;
		}

		public Boolean UnaryRelationCB(FeatureNode srcNode, String attrName)
		{
			FeatureNode attr = srcNode.get(attrName);
			if (!attr.isValued()) return false;
			String value = attr.getValue();
			String srcName = srcNode.get("name").getValue();

			if (id_map != null)
			{
				srcName = id_map.get(srcNode);
			}
			if (unaryStyle)
			{
				if (attrName.endsWith("-FLAG"))
					value = attrName.replaceAll("-FLAG","").toLowerCase();

				if (attrName.equals("HYP"))
					value = attrName.toLowerCase();

				//Optimize using StringBuilder
				//Get the Individual type (noun, etc.) and the type_property
				System.out.println("\n\tRELATION (unary1) = " + value + "(" + srcName + ")\n");

				//1º Add the first term to Word Class
				srcName=srcName.replaceAll("\\%", "");
				OWLIndividual src_word = factory.getOWLNamedIndividual(IRI.create(ontologyURI + "#" + srcName.replaceAll(" ", "")));
				OWLClassAssertionAxiom addSrcWord = factory.getOWLClassAssertionAxiom(word,src_word );
				manager.applyChange(new AddAxiom(ontology, addSrcWord));

				//2º Create the property
				OWLObjectProperty rel = factory.getOWLObjectProperty(IRI.create(ontologyURI + "#relex_is"));

				//3º Add the second term to Word Class
				value=value.replaceAll("\\%", "");
				OWLIndividual dst_word = factory.getOWLNamedIndividual(IRI.create(ontologyURI + "#" + value.replaceAll(" ", "")));
				OWLClassAssertionAxiom addDstWord = factory.getOWLClassAssertionAxiom(word,dst_word );
				manager.applyChange(new AddAxiom(ontology, addDstWord));

				//4º Create axiom for the relation
				OWLObjectPropertyAssertionAxiom addrel = factory.getOWLObjectPropertyAssertionAxiom(rel,src_word , dst_word);
				manager.applyChange(new AddAxiom(ontology, addrel));

				//5º Add the words (Class) to the sentence (Class)
				/*OWLObjectPropertyAssertionAxiom addw1 = * factory.getOWLObjectPropertyAssertionAxiom(sentenceInd, has, src_word);
				OWLObjectPropertyAssertionAxiom addw2 = factory.getOWLObjectPropertyAssertionAxiom(sentenceInd, has, dst_word);
				manager.applyChange(new AddAxiom(ontology, addw1));
				manager.applyChange(new AddAxiom(ontology, addw2));*/
			}
			else
			{
				//Optimize using StringBuilder
				//Get the Individual type (noun, etc.) and the type_property
				System.out.println("\n\tRELATION (unary2) = " + attrName + "(" + srcName + ", " + value + ")\n");

				if (value.charAt(0)=='.')
					value = value.replaceFirst(".", "");

				if (value.contains("[") || value.contains("]") || srcName.contains("[") || srcName.contains("]") ||
						value.equals("WORD") || srcName.equals("WORD") || value.contains("misc-") || srcName.contains("misc-")) return false;

				if (srcName.length()>0 && value.length()>0)
				{
					//1º Add the first term to Word Class
					srcName=srcName.replaceAll("[\\%\\s]", "");
					OWLIndividual src_word = factory.getOWLNamedIndividual(IRI.create(ontologyURI + "#" + srcName));
					OWLClassAssertionAxiom addSrcWord = factory.getOWLClassAssertionAxiom( word,src_word);
					manager.applyChange(new AddAxiom(ontology, addSrcWord));

					//2º Create the property
					attrName=attrName.replaceAll("[\\%\\s]", "");
					OWLObjectProperty rel = factory.getOWLObjectProperty(IRI.create(ontologyURI + "#" + attrName));

					//3º Add the second term to Word Class
					value=value.replaceAll("[\\%\\s]", "");
					OWLIndividual dst_word = factory.getOWLNamedIndividual(IRI.create(ontologyURI + "#" + value));
					OWLClassAssertionAxiom addDstWord = factory.getOWLClassAssertionAxiom(word,dst_word );
					manager.applyChange(new AddAxiom(ontology, addDstWord));

					//4º Create axiom for the relation
					OWLObjectPropertyAssertionAxiom addrel = factory.getOWLObjectPropertyAssertionAxiom(rel,src_word , dst_word);
					manager.applyChange(new AddAxiom(ontology, addrel));

					//5º Add the words (Class) to the sentence (Class)
					OWLObjectPropertyAssertionAxiom addw1 = factory.getOWLObjectPropertyAssertionAxiom(has,sentenceInd , src_word);
					//OWLObjectPropertyAssertionAxiom addw2 = factory.getOWLObjectPropertyAssertionAxiom(sentenceInd, has, dst_word);

					manager.applyChange(new AddAxiom(ontology, addw1));
					//manager.applyChange(new AddAxiom(ontology, addw2));
				}

			}
			return false;
		}
	}
}

/* ============================ END OF FILE ====================== */
