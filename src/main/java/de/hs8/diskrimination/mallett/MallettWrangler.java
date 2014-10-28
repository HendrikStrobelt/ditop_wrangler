package de.hs8.diskrimination.mallett;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

public class MallettWrangler extends JDialog implements ThreadCallBack {

	private final JPanel contentPanel = new JPanel();
	private JTextField datasetLabel;
	private JLabel filenameLabel;
	private final List<Integer> noOfTopics = new ArrayList<Integer>();
	private JButton okButton;
	private int noTopicsCount;
	private File dataFile;
	private JProgressBar progressBar;
	private JButton cancelButton;
	private String configFileString;
	private Box horizontalBox_1;
	private JLabel lblSizes;
	private JTextField textField;
	private Box horizontalBox_2;
	private JLabel label;
	private JTextField textField_1;
	private int noIterations;

	/**
	 * Launch the application.
	 */
	public static void main(final String[] args) {
		try {
			final MallettWrangler dialog = new MallettWrangler();
			dialog.setVisible(true);
		} catch (final Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Create the dialog.
	 */
	public MallettWrangler() {
		setTitle("Create DiTop Topic Files");

		setModalityType(ModalityType.APPLICATION_MODAL);
		setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		setBounds(100, 100, 847, 271);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setLayout(new FlowLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		{
			final Box verticalBox = Box.createVerticalBox();
			contentPanel.add(verticalBox);
			{
				final JButton btnChooseDir = new JButton("choose Dir");
				btnChooseDir.addActionListener(new ActionListener() {

					@Override
					public void actionPerformed(final ActionEvent arg0) {
						final JFileChooser fc = new JFileChooser(new File("."));
						fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
						final int returnVal = fc.showOpenDialog(contentPanel);

						if (returnVal == JFileChooser.APPROVE_OPTION) {
							dataFile = fc.getSelectedFile();

							EventQueue.invokeLater(new Runnable() {
								@Override
								public void run() {
									filenameLabel.setText(dataFile
											.getAbsolutePath());
									datasetLabel.setText(dataFile.getName());
									okButton.setEnabled(true);

								}
							});

						} else {
							System.out
									.println("Open command cancelled by user.");
						}

					}
				});
				btnChooseDir.setAlignmentX(Component.CENTER_ALIGNMENT);
				verticalBox.add(btnChooseDir);
			}
			{
				filenameLabel = new JLabel("<dir>");
				filenameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
				verticalBox.add(filenameLabel);
			}
			{
				final Box horizontalBox = Box.createHorizontalBox();
				verticalBox.add(horizontalBox);
				{
					final JLabel lblName = new JLabel("Name:");
					horizontalBox.add(lblName);
				}
				{
					datasetLabel = new JTextField();
					horizontalBox.add(datasetLabel);
					datasetLabel.setColumns(10);
				}
			}
			{
				horizontalBox_1 = Box.createHorizontalBox();
				verticalBox.add(horizontalBox_1);
				{
					lblSizes = new JLabel("No. Topics:");
					horizontalBox_1.add(lblSizes);
				}
				{
					textField = new JTextField();
					textField.setText("40,80");
					textField.setColumns(10);
					horizontalBox_1.add(textField);
				}
			}
			{
				horizontalBox_2 = Box.createHorizontalBox();
				verticalBox.add(horizontalBox_2);
				{
					label = new JLabel("No. Iterations:");
					horizontalBox_2.add(label);
				}
				{
					textField_1 = new JTextField();
					textField_1.setText("2000");
					textField_1.setColumns(10);
					horizontalBox_2.add(textField_1);
				}
			}
			{
				progressBar = new JProgressBar();
				verticalBox.add(progressBar);
			}
		}
		{
			final JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				okButton = new JButton("Create File");
				okButton.setEnabled(false);
				okButton.addActionListener(new ActionListener() {

					@Override
					public void actionPerformed(final ActionEvent arg0) {
						final List<Integer> noTopicList = new ArrayList<Integer>();
						final String numberTopics = textField.getText();
						if (numberTopics.matches("[0-9]+(,[0-9]+)*")) {
							final String[] split = numberTopics.split(",");
							for (final String s : split) {
								try {
									final int size = Integer.parseInt(s);
									noTopicList.add(size);
								} catch (final NumberFormatException e) {

								}

							}

						} else {
							textField.setText("40,80");
							noTopicList.add(40);
							noTopicList.add(80);
						}
						;

						/*
						 * copy the sizes to a list which will be reduced step
						 * by step
						 */
						for (final int noTopic : noTopicList) {
							noOfTopics.add(noTopic);
						}
						noTopicsCount = noOfTopics.size() + 1;

						noIterations = Integer.parseInt(textField_1.getText());

						EventQueue.invokeLater(new Runnable() {

							@Override
							public void run() {
								okButton.setEnabled(false);
								cancelButton.setEnabled(false);

								configFileString = datasetLabel.getText();
								for (final Integer noTopic : noOfTopics) {
									configFileString += ";" + noTopic;
								}

								threadDone();

							}
						});

					}
				});
				//				okButton.setActionCommand("OK");
				buttonPane.add(okButton);
				getRootPane().setDefaultButton(okButton);
			}
			{
				cancelButton = new JButton("Cancel");
				cancelButton.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(final ActionEvent e) {
						dispose();
					}
				});
				cancelButton.setActionCommand("Cancel");
				buttonPane.add(cancelButton);
			}
		}
	}

	@Override
	public synchronized void threadDone() {

		if (noOfTopics.size() > 0) {
			progressBar.setValue((noTopicsCount - noOfTopics.size()) * 100
					/ noTopicsCount);

			final Integer topicCount = noOfTopics.remove(0);
			final MallettTopicThread mallettTopicThread = new MallettTopicThread(
					dataFile, datasetLabel.getText(), topicCount, noIterations,
					this);
			final Thread thread = new Thread(mallettTopicThread);
			thread.start();

		} else {
			progressBar.setValue(100);
			writeConfiguration();

			try {
				Thread.sleep(1000);
			} catch (final InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			dispose();

		}

	}

	private void writeConfiguration() {
		//		File file = new File(DiscriminativeViewer.configFile);
		//		List<String> dataSets = new ArrayList<String>();
		//		if (file.exists()){
		//			try {
		//				Scanner scanner = new Scanner(file, "UTF-8");
		//				try {
		//					while (scanner.hasNextLine()){
		//						dataSets.add(scanner.nextLine()); 
		//					}
		//				}
		//				finally{
		//					scanner.close();
		//				}
		//				
		//			} catch (FileNotFoundException e) {
		//				// TODO Auto-generated catch block
		//				e.printStackTrace();
		//			}
		//			file.delete();
		//			
		//			/*
		//			 * look for duplicate Entries
		//			 * */
		//			
		//			String signature = datasetLabel.getText()+";";
		//			int c=0;
		//			int replace= -1;
		//			for (String string : dataSets) {
		//				if (string.startsWith(signature)) replace = c; 
		//				c++;
		//			}
		//			if (replace>0){
		//				dataSets.remove(replace);
		//			}
		//			
		//		}
		//		
		//		dataSets.add(configFileString);
		//		
		//		BufferedWriter writer = null;
		//		try
		//		{
		//			writer = new BufferedWriter( new FileWriter( DiscriminativeViewer.configFile));
		//			for (String string : dataSets) {
		//				writer.write( string + "\n");	
		//			}
		//			
		//		}
		//		catch ( IOException e)
		//		{
		//		}
		//		finally
		//		{
		//			try
		//			{
		//				if ( writer != null)
		//					writer.close( );
		//			}
		//			catch ( IOException e)
		//			{
		//			}
		//	     }
	}

}
