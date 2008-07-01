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

package relex.corpus;

import gate.Annotation;
import gate.AnnotationSet;
import gate.Corpus;
import gate.Document;
import gate.Factory;
import gate.FeatureMap;
import gate.Gate;
import gate.ProcessingResource;
import gate.creole.SerialAnalyserController;
import gate.creole.ANNIEConstants;
import gate.util.GateException;

import java.io.File;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import relex.entity.EntityInfo;
import relex.entity.EntityMaintainer;
import relex.entity.EntityType;

/**
 * Refactored version of taca/GateEntityDetector
 * Eliminates dependence on HG, TextDocument, Danalyzer.
 * Goes directly over to EntityMaintainer, skipping 
 * the various intermediate steps.
 */
public class GateEntityDetector extends EntityMaintainerFactory
{
	public static final int DEBUG=0;
	Boolean initialized = false;

	/**
	 * The default GATE installation directory. It's not necessary to
	 * change this value; if you put GATE somewhere else define it as
	 * a Java system property (e.g. java -Dgate.home=c:/tools/gate) or
	 * in a environment variable (e.g. GATE_HOME="c:/Program
	 * Files/GATE-4.0").
	 */
	public final static String DEFAULT_GATE_HOME = "/opt/GATE-4.0";

	// XXX Do we really need to have the POSTagger loaded to do this?
	// XXX Do we really need to have the SentenceSplitter loaded ?? 
	private static final String[] PR_NAMES =
	{
		"gate.creole.annotdelete.AnnotationDeletePR",
		"gate.creole.tokeniser.DefaultTokeniser",
		// "gate.creole.splitter.SentenceSplitter",
		"gate.creole.gazetteer.DefaultGazetteer",
		// "gate.creole.POSTagger",
		"gate.creole.ANNIETransducer",
		"gate.creole.orthomatcher.OrthoMatcher"
	};

	private static String gateHome = null;

	/** The Corpus Pipeline application to contain ANNIE */
	private SerialAnalyserController annieController;
	private Corpus corpus = null;
	private Document doc = null;
	private Map<String, Map<Object, Object>> annieParams = new HashMap<String, Map<Object, Object>>();
	
	/**
	 * Initialise the ANNIE system. This creates a "corpus pipeline"
	 * application that can be used to run sets of documents through
	 * the extraction system.
	 */
	private void initAnnie() throws GateException
	{
		// Create a serial analyser controller to run ANNIE with.
		annieController = (SerialAnalyserController) Factory.createResource(
		                   "gate.creole.SerialAnalyserController", 
		                   Factory.newFeatureMap(), Factory.newFeatureMap(),
		                   "ANNIE_" + Gate.genSym());

		// Load each PR as defined in ANNIEConstants
		for (int i = 0; i < PR_NAMES.length; i++) {
			if (DEBUG>0) System.out.println("About to create "+ PR_NAMES[i]);
			FeatureMap params = Factory.newFeatureMap();
			Map<Object, Object> configuredParams = annieParams.get(PR_NAMES[i]);
			if (configuredParams != null)
				params.putAll(configuredParams);
			ProcessingResource pr = (ProcessingResource) Factory.createResource(PR_NAMES[i], params);
			
			if (pr instanceof gate.creole.tokeniser.DefaultTokeniser) {
				System.out.println("Changing parameter of "+pr.getClass().getName());
			}
			// add the PR to the pipeline controller
			annieController.add(pr);
		}
	}

	public String getGateHome()
	{
		return gateHome;
	}

	public void setGateHome(String gh)
	{
		gateHome = gh;
	}

	public GateEntityDetector()
	{
		initialized = false;
	}

	public GateEntityDetector(Map<String, Map<Object, Object>> params)
	{
		this();
		annieParams.putAll(params);
	}
	
	/**
	 * Return a map of name/value parameters used to initialize Annie.
	 * If you wish to add custom configuration parameters to GATE, put
	 * them in this time after construction time, but before any other
	 * processing. The keys to this map are classname of processing
	 * resources (e.g. "gate.creole.gazetteer.DefaultGazetteer") and
	 * the values are maps representing configuration parameters
	 * (GATE's FeatureMap) for each processing resource.
	 */
	public Map<String, Map<Object, Object>> getAnnieParams()
	{
		return annieParams;
	}
	
	public synchronized void initialize()
	{
		if (initialized) return;
		
		//	Initialise the GATE library
		try
		{
			if ((gateHome == null) || (gateHome.length() == 0))
				gateHome = System.getProperty("gate.home");
			if ((gateHome == null) || (gateHome.length() == 0))
				gateHome = System.getenv().get("GATE_HOME");
			if ((gateHome == null) || (gateHome.length() == 0))
				gateHome = DEFAULT_GATE_HOME;
			System.setProperty("gate.home", gateHome);
			File fGateHome = new File(gateHome);
			if (!fGateHome.exists())
			{
				System.err.println("gate.home is not specified.. use -Dgate.home to specify the value");
				throw new RuntimeException("GATE Home '" + fGateHome.getAbsolutePath() + "' is invalid.");
			}
			if (DEBUG>0) System.out.println("Initializing GATE...");
			Gate.init();
			if (DEBUG>0) System.out.println("GATE initialized.");
			if (DEBUG>0) System.out.println("About to register ANNIE directories...");

			Gate.getCreoleRegister().registerDirectories(
					new File(new File(fGateHome, "plugins"), "ANNIE").toURI().toURL());

			if (DEBUG>0) System.out.println("Annie plugins registered. Initializing ANNIE...");
			initAnnie();
			if (DEBUG>0) System.out.println("ANNIE initialized.");
		} catch(GateException e) {
			throw new RuntimeException(e.getMessage());
		} catch(MalformedURLException m) {
			throw new RuntimeException(m.getMessage());
		}
		if (DEBUG>0)
		{
			System.out.println("...GATE initialised");
			Package p = annieController.getClass().getPackage();
			System.out.println("GATE Specification Version : " 
			                   + p.getSpecificationVersion());
			System.out.println("GATE Implementation Version : " 
			                   + p.getImplementationVersion());
		}

		initialized = true;
	}

	@SuppressWarnings("unchecked") // effing Gate spews a warning.
	public AnnotationSet getAnnotations(String documentText)
		throws GateException
	{
		if (DEBUG>0) System.out.println("Original text is:\n"+documentText);
		initialize();
		
		// Ugly hack; since Annie thinks "Don" is a person's name.
		String fixed = documentText.replaceAll("Don't", "Do not").replaceAll("don't", "do not");
		if (DEBUG>0) System.out.println("Fixed text is :\n"+fixed);
		doc =  Factory.newDocument(fixed);
		corpus = (Corpus) Factory.createResource("gate.corpora.CorpusImpl");
		corpus.add(doc);
		annieController.setCorpus(corpus);
		annieController.execute();
		return doc.getAnnotations();
	}

	public void releaseResources()
		throws GateException
	{
		if (!initialized)
		{
			return;
		}
		Factory.deleteResource(doc);
		Factory.deleteResource(corpus);
		annieController.setCorpus(null);
	}

	public EntityMaintainer makeEntityMaintainer(String sentence)
	{
		initialize();
		EntityMaintainer em = null;
		try
		{
			AnnotationSet annoset = getAnnotations(sentence);
			em = findEntitiesInText(annoset, sentence);
			releaseResources();
		}
		catch(GateException e)
		{
			if (DEBUG>0) System.err.println(e.getMessage());
		}
		return em;
	}

	private EntityMaintainer findEntitiesInText(AnnotationSet annoset, String sentence)
	{
		try {
			ArrayList<EntityInfo> eInfos = new ArrayList<EntityInfo>();
	
			for (Iterator<Annotation> it = annoset.iterator(); it.hasNext();)
			{
				Annotation a = it.next();
				int start = a.getStartNode().getOffset().intValue();
				int end = a.getEndNode().getOffset().intValue();
	
				EntityInfo ei = null;
	
				String atype = a.getType();
				if (a.getType().equals(ANNIEConstants.SPACE_TOKEN_ANNOTATION_TYPE)) continue;
				if (a.getType().equals(ANNIEConstants.TOKEN_ANNOTATION_TYPE)) continue;
				
				if (DEBUG>0) System.out.println("Found a " + atype + " entity " + a.toString());
	
				if(a.getType().equals(ANNIEConstants.PERSON_ANNOTATION_TYPE))
				{
					EntityInfo pei = new EntityInfo(sentence, start, end, EntityType.PERSON);
					String gender = (String) a.getFeatures().get(ANNIEConstants.PERSON_GENDER_FEATURE_NAME);
					if (gender!=null) pei.setNodeProperty(EntityInfo.GENDER, gender.charAt(0) == 'm' ? "masculine" : "feminine");
					ei = pei;
				}
				else if(atype.equals(ANNIEConstants.ORGANIZATION_ANNOTATION_TYPE))
				{
					ei = new EntityInfo(sentence, start, end, EntityType.ORGANIZATION);
				}
				else if(atype.equals(ANNIEConstants.LOCATION_ANNOTATION_TYPE))
				{
					ei = new EntityInfo(sentence, start, end, EntityType.LOCATION);
				}
				else if(atype.equals(ANNIEConstants.MONEY_ANNOTATION_TYPE))
				{
					ei = new EntityInfo(sentence, start, end, EntityType.LOCATION);
				}
				else if(atype.equals(ANNIEConstants.DATE_ANNOTATION_TYPE))
				{
					ei = new EntityInfo(sentence, start, end, EntityType.DATE);
				}
	
				if (ei != null)
				{
					// Insert in proper order.
					for (int i = 0; i < eInfos.size(); i++)
					{
						if (eInfos.get(i).getFirstCharIndex() > ei.getFirstCharIndex())
						{
							eInfos.add(i, ei);
							ei = null;
							break;
						}
					}
	
					// Append at end, if not inserted in middle.
					if (ei != null) eInfos.add(ei);
				}
			}
			return new EntityMaintainer(sentence, eInfos);
		}
		catch (RuntimeException e)
		{
			e.printStackTrace();
			throw e;
		}
	}
}
