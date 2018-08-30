import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;



public class MutedCircleChecker extends JPanel {
	static JTextField pathField = new JTextField(20);
	static JTextField sampleField = new JTextField(20);
	static JTextArea outputArea = new JTextArea(12,20);
	JButton check = new JButton();
	JButton browse = new JButton();
	public MutedCircleChecker() {
		BoxLayout experimentLayout = new BoxLayout(this, BoxLayout.Y_AXIS);
		Box buttons = new Box(0);
		check.addActionListener(new CheckListener());
		this.setLayout(experimentLayout);
		check.setText("Check .osu");
		browse.setText("Browse");
		browse.setLocation(0, 0);
		browse.addActionListener(new OpenListener());
		pathField.setEditable(false);
		outputArea.setEditable(false);
		outputArea.setBackground(Color.LIGHT_GRAY);
		buttons.add(browse);
		buttons.add(check);
		this.add(buttons);
		sampleField.setText("Silent Addition");
		this.add(pathField);
		this.add(outputArea);	
		this.add(sampleField);
	}

	public static void main (String args[]) {
		JFrame gridFrame = new JFrame ("Silent Circle Checker");
		gridFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		gridFrame.getContentPane().add(new MutedCircleChecker());
		gridFrame.setVisible(true);	   
		gridFrame.setPreferredSize(new Dimension(400, 300));
		gridFrame.pack();
		
		//allows dropping file instead of using browse
		DropTarget d = new DropTarget() {
			private static final long serialVersionUID = 6511786488901217011L;

			public synchronized void drop(DropTargetDropEvent evt) {
				try {
					evt.acceptDrop(DnDConstants.ACTION_COPY);
					List<File> droppedFiles = (List<File>) evt.getTransferable()
							.getTransferData(DataFlavor.javaFileListFlavor);
						pathField.setText(droppedFiles.get(0).getPath());
					


				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		};
		outputArea.setDropTarget(d);

	}
	

	
	public static class OpenListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent arg0) {
			
			 JFileChooser fileChooser = new JFileChooser();

		     fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		 
		     fileChooser.setAcceptAllFileFilterUsed(false);
		 
		     int rVal = fileChooser.showOpenDialog(null);
		     if (rVal == JFileChooser.APPROVE_OPTION) {
		    	 pathField.setText(fileChooser.getSelectedFile().toString());
		     }
				 
		}
		
	}
	
	public static class CheckListener implements ActionListener {
		//Sampleset (1) Normal, (2) Soft, (3) Drum
		//Addition (0) Default, (1) Custom, (2+) other custom
		@Override
		public void actionPerformed(ActionEvent arg0) {
			

			Writer writer = null;
			int silentSample = 0;
			int silentAddition = 0;
			ArrayList<TimingPoint> timingPointList = new ArrayList<TimingPoint>();
			ArrayList<Integer> circleOffsetList = new ArrayList<Integer>();
			ArrayList<Integer> silentCircleList = new ArrayList<Integer>();
			ArrayList<Integer> sliderOffsetList = new ArrayList<Integer>();
			ArrayList<Integer> silentSliderList = new ArrayList<Integer>();
			File fileName = new File(pathField.getText());
		    String line = "";
		    int sectionCount = 1;
		    int currentLine = 0;
		    try {
			    if (sampleField.getText().length() > 0 && !sampleField.getText().equals("Silent Addition")) {
			    	String sampleAddition = sampleField.getText();
			    	
			    	if (sampleAddition.substring(0, 1).equalsIgnoreCase("S")) {
			    		silentSample = 2;
			    	} else if (sampleAddition.substring(0, 1).equalsIgnoreCase("N")) {
			    		silentSample = 1;
			    	} else {
			    		silentSample = 3;
			    	}
			    	
			    	silentAddition = Integer.parseInt(sampleAddition.substring(1));
			    }
			    
		    } catch (Exception e) {
		    	outputArea.append("Error reading sample and addition");
		    }
		    
		    try {	
		    	
		        FileReader fileReader = new FileReader(fileName);
		        BufferedReader bufferedReader = new BufferedReader(fileReader);
		  
			        while(((line = bufferedReader.readLine()) != null)) {
			        	if (line.equals("")) {
			        		sectionCount+=1;
			        		continue;
			        	}	        
			        	if (line.contains("[")) {
			        		continue;
			        	}        	
			        	if (sectionCount == 7) {
			        		int timingOffset = Integer.parseInt(line.split(",")[0]);
			        		int volume = Integer.parseInt(line.split(",")[5]);		        		
			        		if (Integer.parseInt(line.split(",")[3]) == silentSample && Integer.parseInt(line.split(",")[4]) == silentAddition) {
			        			volume = 5;
			        		}			        		    		
			        		timingPointList.add(new TimingPoint(timingOffset, volume));			        		
			        	}
			        	
			        	if (sectionCount == 10) {
			        		if (!line.contains("|")) {      
				        			if (Integer.parseInt(line.split(":")[3]) != 0) {
				        				if (Integer.parseInt(line.split(":")[3]) < 6) {
					        				int circleOffset = Integer.parseInt(line.split(",")[2]);
				        					outputArea.append("Silent circle at " + circleOffset + "\n");
				        				}
				        			} else {
					        			int circleOffset = Integer.parseInt(line.split(",")[2]);
						        		circleOffsetList.add(circleOffset);
				        			}
			        			
			        		} else {
				        		int sliderOffset = Integer.parseInt(line.split(",")[2]);
					        	sliderOffsetList.add(sliderOffset);  						        			
			        		}
			        	}
			        }

		
		        bufferedReader.close();
		        
		        int currentPointOffset = 0;
		        int nextPointOffset = 0;
		        
		        	for (int x = 0; x < timingPointList.size(); x++) {
		        		
		        		if (x != timingPointList.size() - 1) {
		        			currentPointOffset = timingPointList.get(x).offset;
		        			nextPointOffset = timingPointList.get(x+1).offset;
		        		} else {
		        			currentPointOffset = timingPointList.get(x).offset;
		        			nextPointOffset = Integer.MAX_VALUE;   			
		        		}   	        		
		        		timingPointList.get(x).ending = nextPointOffset;
		        	}
		        	  	
		      
		        	
		        for (int circleOffset : circleOffsetList) {	
		        	
			        for (TimingPoint timingPoint : timingPointList) {        	  
			        	
			        	if (timingPoint.volume<6) {
			        		if (circleOffset >= timingPoint.offset && circleOffset < timingPoint.ending) {
			        			silentCircleList.add(circleOffset);
			        		}
			        	}
			        	
			        }
		        
		        }
		        for (int sliderOffset : sliderOffsetList) {	
		        	
			        for (TimingPoint timingPoint : timingPointList) {        	  
			        	
			        	if (timingPoint.volume<6) {
			        		if (sliderOffset >= timingPoint.offset && sliderOffset < timingPoint.ending) {
			        			silentSliderList.add(sliderOffset);
			        		}
			        	}
			        	
			        }
		        
		        }
		        for (int circleOffset : silentCircleList) {	
		        	long minutes = TimeUnit.MILLISECONDS.toMinutes(circleOffset);
		        	circleOffset -= (minutes*60000);
		        	long seconds = TimeUnit.MILLISECONDS.toSeconds(circleOffset);
		        	circleOffset -= (seconds*1000);	
		        	outputArea.append("Silent circle at " + minutes  + ":" + seconds + ":" + circleOffset + "\n");
		        }		        
		        for (int sliderOffset : silentSliderList) {	
		        	long minutes = TimeUnit.MILLISECONDS.toMinutes(sliderOffset);
		        	sliderOffset -= (minutes*60000);
		        	long seconds = TimeUnit.MILLISECONDS.toSeconds(sliderOffset);
		        	sliderOffset -= (seconds*1000);
		        	outputArea.append("Silent slider at " + minutes  + ":" + seconds + ":" + sliderOffset + "\n");
		        }
		        
		        if (silentCircleList.size() == 0 && silentSliderList.size() == 0) {
		        	outputArea.append("No Silent Circles found");
		        }
		        
		        
		    } catch(FileNotFoundException ex1) {
		        
		    	outputArea.append("Error with file/calculation" + "\n");
		        
		        
		        
		    }
		   
		    catch(IOException ex) {
		        
		    	outputArea.append("File can't be read" + "\n");
		        
		    }
			
			
			
			
			
		}
    
	}
	
	
}
