package de.rohrdantz.topic;



import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TreeSet;
import java.util.logging.Handler;

import cc.mallet.pipe.CharSequence2TokenSequence;
import cc.mallet.pipe.Pipe;
import cc.mallet.pipe.SerialPipes;
import cc.mallet.pipe.TokenSequence2FeatureSequence;
import cc.mallet.pipe.TokenSequenceLowercase;
import cc.mallet.pipe.TokenSequenceRemoveStopwords;
import cc.mallet.pipe.iterator.ArrayIterator;
import cc.mallet.topics.ParallelTopicModel;
import cc.mallet.topics.TopicAssignment;
import cc.mallet.types.Alphabet;
import cc.mallet.types.IDSorter;
import cc.mallet.types.InstanceList;

/**
 * Based on Christian Rohrdantz's class LDA.java
 * This implementation can deal with input files in different subfolders
 * of the input path. The subfolders are treated as different subclasses /
 * subcollections. This is not taken into account in the topic modeling
 * process itself but it is possible to add a column in the output file 
 * that contains the class labels.  
 * 
 * @author Daniela Oelke
 * @created 10.01.2013
 */
public class MalletLDA {
 
	private int maxCount = 15; 				//maximum number of words per topic that should be written to console / file
	private boolean keepOldModel = false; 	//if set true, the model of the last run is used (for repeatability)
	private int numTopics = 30; 			//number of topics to be determined
	private int numIterations = 1000; 
	private int optimizeInterval = 10; 
	private int beta = 100; 
	private Handler loggingHandler = null;
	
	
	private List<String> textList = new ArrayList<String>();	
	private HashMap<String, String> classDocMapping = new HashMap<String, String>(); 
	private HashMap<Integer, String> idDocMapping = new HashMap<Integer, String>(); 
		
	
	
	public int getNumIterations() {
		return numIterations;
	}


	public void setNumIterations(int numIterations) {
		this.numIterations = numIterations;
	}


	public void extractTopics(String inputPath, String writePathDocTopic, String writePathTopicTerm, String writePathTopicTermMatrix, 
								int numTopics, int maxCount) throws IOException {
		
		this.maxCount = maxCount; 
		this.numTopics = numTopics; 
		
		try {
			File dir = new File(inputPath);
			browseDirectory(dir);			
			ParallelTopicModel model = getOrCreateModel();
			printTopics(model, writePathDocTopic, writePathTopicTerm, writePathTopicTermMatrix);
		} catch (Exception e) {
			e.printStackTrace();
		} 
		
	}
	

	private void browseDirectory(File dir) {
		for(File file : dir.listFiles()){
			if(file.isFile() ) {
				if (!file.getName().startsWith(".")){
					String fileName = dir.getName() + "_" + file.getName();
					getTexts(file, fileName);
					this.classDocMapping.put(fileName,dir.getName());
				}
			} else {
				browseDirectory(file);
			}
		}
	}

	
	private void getTexts(File file, String fileName) {		
		try {			
			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file),"UTF8"));
			String s = "";
			String line = reader.readLine();
			while(line != null){
				s += line;
				line = reader.readLine();
			}
			this.idDocMapping.put(textList.size(), fileName); 
			textList.add(s);			
			reader.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
	
	


	private ParallelTopicModel createNewModel() throws IOException {

		InstanceList instanceList = createInstanceList(textList);
		//int numTopics = instanceList.size() / 2;
		ParallelTopicModel model = new ParallelTopicModel(numTopics);
		System.out.println(" NUMBER OF TOPICS "+numTopics);
		model.addInstances(instanceList);
		
		
		//model.beta = this.beta;
		model.setNumIterations(this.numIterations);
		model.setOptimizeInterval(this.optimizeInterval);
		model.setNumThreads(4);
		if (loggingHandler!=null) model.logger.addHandler(loggingHandler);
		
		model.estimate();
		
		
		System.out.println("Model log likelihood: " + model.modelLogLikelihood());	

		return model;
	}

	
	private InstanceList createInstanceList(List<String> texts) throws IOException {
		ArrayList<Pipe> pipes = new ArrayList<Pipe>();
		//pipes.add(new CharSequence2TokenSequence());
		pipes.add(new CharSequence2TokenSequence("([a-zA-Z0-9]|[-]|[~]|[_]|[.]|[/]|ä|ü|ö|Ä|Ö|Ü|ß|<|>|)+"));
		pipes.add(new TokenSequenceLowercase());
		//pipes.add(new TokenSequenceRemoveStopwords());
		pipes.add(new TokenSequence2FeatureSequence());
		InstanceList instanceList = new InstanceList(new SerialPipes(pipes));
		instanceList.addThruPipe(new ArrayIterator(texts));
		return instanceList;		        				
	}
	

	private ParallelTopicModel getOrCreateModel() throws Exception {
		return getOrCreateModel("model");
	}

	
	private ParallelTopicModel getOrCreateModel(String directoryPath) throws Exception {
		File directory = new File(directoryPath);
		if (!directory.exists()) {
			directory.mkdir();
		}
		File file = new File(directory, "mallet-lda.model");
		ParallelTopicModel model = null;
		if (!file.exists() || !keepOldModel) {
			model = createNewModel();
			model.write(file);
		} else {
			model = ParallelTopicModel.read(file);
		}
		return model;
	}
	
	

	
	public void printTopics(ParallelTopicModel model, String writePathDocTopic, String writePathTopicTerm, String writePathTopicTermMatrix) throws Exception {
		ArrayList<String> topicKeys = new ArrayList<String>();  
		
		BufferedWriter writerDocTopic = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(writePathDocTopic), "UTF8"));
		BufferedWriter writerTopicTerm = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(writePathTopicTerm), "UTF8"));
		File file = new File(writePathTopicTerm); 
		String path = file.getName().substring(0, file.getName().length()-4) + "-T" + String.valueOf(maxCount) + ".txt";
		String parentPath = new File(writePathTopicTerm).getParentFile().getAbsolutePath();
		BufferedWriter writerTopicTermShort = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(parentPath,path))));
		BufferedWriter writerTopicTermMatrix = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(writePathTopicTermMatrix), "UTF8")); 
		
		/* Write header */
		writerDocTopic.write("Class,Document"); 
		for(int j = 0; j < model.numTopics; j++) {
			writerDocTopic.write(",T" + j);
		}		
		writerDocTopic.newLine();
		
		/* Write document-topic probabilities to file */
		for(int i=0;i<this.textList.size(); i++){
			double[] topicProbs = model.getTopicProbabilities(i);

			//writerDocTopic.write(i  + ",");
			String docName = this.idDocMapping.get(i); 
			writerDocTopic.write(this.classDocMapping.get(docName) + ",");
			writerDocTopic.write(docName);
			for(int j=0; j < topicProbs.length; j++){
				writerDocTopic.write("," + topicProbs[j]);

			}
			writerDocTopic.newLine(); 
		}

		
		/* Write topic-term probabilities to file */
//		Alphabet alphabet = model.getAlphabet();
//		for (int i = 0; i < model.getSortedWords().size(); i++) {
//			writerTopicTermMatrix.write("TOPIC " + i + ": ");
//			/**topic for the label*/
//			TreeSet<IDSorter> set = model.getSortedWords().get(i); 
//			for (IDSorter s : set) {				
//				 							
//			}
//			writerTopicTerm.newLine(); 
//			writerTopicTermShort.newLine(); 
//		}
//		
		
		/* Write topic term associations */
		Alphabet alphabet = model.getAlphabet();
		for (int i = 0; i < model.getSortedWords().size(); i++) {
			writerTopicTerm.write("TOPIC " + i + ": ");
			writerTopicTermShort.write("TOPIC " + i + ": "); 
			writerTopicTermMatrix.write("TOPIC " + i + ": ");
			/**topic for the label*/
			String tmpTopic = "";
			int count = 0; 
			TreeSet<IDSorter> set = model.getSortedWords().get(i); 
			for (IDSorter s : set) {				
				if(count <= maxCount) {
					writerTopicTermShort.write(alphabet.lookupObject(s.getID()) + ", " ); 					
				}
				count++;
				writerTopicTerm.write(alphabet.lookupObject(s.getID()) + ", "); 			
				writerTopicTermMatrix.write(alphabet.lookupObject(s.getID()) + " (" + s.getWeight() + "), ");
				/**add to topic label*/
				tmpTopic += alphabet.lookupObject(s.getID()) + "\t";
			}
			topicKeys.add(tmpTopic);
			writerTopicTerm.newLine(); 
			writerTopicTermShort.newLine(); 
			writerTopicTermMatrix.newLine();
		}
		
		writerTopicTermMatrix.close();
		writerDocTopic.close();
		writerTopicTerm.close();
		writerTopicTermShort.close();

	}



	public void setLDAParameters(boolean keepOldModel, int numIterations, int optimizeInterval, int beta) {
		this.keepOldModel = keepOldModel; 
		this.numIterations = numIterations;
		this.optimizeInterval = optimizeInterval; 
		this.beta = beta; 
	}
	
	
	public void extractTopicsConveniently(String parentFolder, String inputPath, String writePathDocTopic, String writePathTopicTerm, String writePathTopicTermMatrix, 
			int numTopics, int maxCount) throws IOException {
		System.out.println(new File(parentFolder, writePathDocTopic).getAbsolutePath());
		this.extractTopics(inputPath,
						   new File(parentFolder, writePathDocTopic).getAbsolutePath(), 
						   new File(parentFolder, writePathTopicTerm).getAbsolutePath(), 
						   new File(parentFolder, writePathTopicTermMatrix).getAbsolutePath(), 
						   numTopics, maxCount);
	}



	public static void main(String[] args) throws Exception {
//		String inputPath = "/home/oelke/Dokumente/Daten/Diskriminierende Themen/Personen/Isenberg-Deussen/TXT_dr_lem_SWR";
//		String writePathDocTopic = "/home/oelke/Dokumente/Daten/Diskriminierende Themen/Personen/Isenberg-Deussen/LDA/PI-TI-OD_DocTopics_40.csv";
//		String writePathTopicTerm = "/home/oelke/Dokumente/Daten/Diskriminierende Themen/Personen/Isenberg-Deussen/LDA/PI-TI-OD_TopicTerms_40.txt";
//		String writePathTopicTermMatrix = "/home/oelke/Dokumente/Daten/Diskriminierende Themen/Personen/Isenberg-Deussen/LDA/PI-TI-OD_TopicTerms-weighted_40.txt";
		
		String parentPath = "/home/oelke/Dokumente/Daten/Diskriminierende Themen/Personen/Frank-Gurevych-Mihalcea/LDA";
		String inputPath = "/home/oelke/Dokumente/Daten/Diskriminierende Themen/Personen/Frank-Gurevych-Mihalcea/TXT_dr_cl_lem_SWR";
		String writePathDocTopic = "IG-AF-RM_DocTopics_40.csv";
		String writePathTopicTerm = "IG-AF-RM_TopicTerms_40.txt";
		String writePathTopicTermMatrix = "IG-AF-RM_TopicTerms-weighted_40.txt";
		
		int maxCount = 15;  //used for small file
		int numTopics = 40; 		
		int numIterations = 2000; 
		
		MalletLDA algo = new MalletLDA();
		algo.numIterations = numIterations;
		algo.extractTopicsConveniently(parentPath, inputPath, writePathDocTopic, writePathTopicTerm, writePathTopicTermMatrix, numTopics, maxCount);
		
	}


	public Handler getLoggingHandler() {
		return loggingHandler;
	}


	public void setLoggingHandler(Handler loggingHandler) {
		this.loggingHandler = loggingHandler;
	}
	
	
}
