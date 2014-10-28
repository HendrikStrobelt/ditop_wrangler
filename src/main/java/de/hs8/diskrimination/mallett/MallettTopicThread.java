package de.hs8.diskrimination.mallett;

import java.awt.EventQueue;
import java.io.File;
import java.io.IOException;

import de.rohrdantz.topic.MalletLDA;

public class MallettTopicThread implements Runnable {

	File inputFile;
	String tmpSubName;
	int topicNumber;
	int numIterations;
	ThreadCallBack callbacker;

	public MallettTopicThread(final File inputFile, final String tmpSubName,
			final int topicNumber, final int numIterations,
			final ThreadCallBack callbacker) {
		super();
		this.inputFile = inputFile;
		this.tmpSubName = tmpSubName;
		this.topicNumber = topicNumber;
		this.numIterations = numIterations;
		this.callbacker = callbacker;
	}

	@Override
	public void run() {
		tmpSubName = tmpSubName + "_" + topicNumber;
		final String parentPath = "datasets";

		final File tmp = new File(parentPath + File.separator + tmpSubName);
		if (!(tmp.exists() && tmp.isDirectory())) {
			tmp.mkdirs();
		} else {
			final File[] listFiles = tmp.listFiles();
			for (final File file : listFiles) {
				file.delete();
			}

		}

		final String inputPath = inputFile.getAbsolutePath();
		final String writePathDocTopic = tmpSubName + "/topics.csv";
		final String writePathTopicTerm = tmpSubName + "/topicTerm.txt";
		final String writePathTopicTermMatrix = tmpSubName
				+ "/topicTermMatrix.txt";

		final int maxCount = 15; // used for small file

		try {
			final MalletLDA algo = new MalletLDA();
			algo.setNumIterations(numIterations);
			// algo.setLoggingHandler(instance);
			algo.extractTopicsConveniently(parentPath, inputPath,
					writePathDocTopic, writePathTopicTerm,
					writePathTopicTermMatrix, topicNumber, maxCount);
		} catch (final IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		EventQueue.invokeLater(new Runnable() {

			@Override
			public void run() {
				callbacker.threadDone();

			}
		});

	}

}
