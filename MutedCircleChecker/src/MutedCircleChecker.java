import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;



public class MutedCircleChecker extends JPanel {
	static JTextField pathField = new JTextField(20);
	static JTextArea outputArea = new JTextArea(12,20);
	JButton check = new JButton();
	JButton browse = new JButton();
	public MutedCircleChecker() {
		check.addActionListener(new CheckListener());
		check.setText("Check .osu");
		browse.setText("Browse");
		browse.addActionListener(new OpenListener());
		pathField.setEditable(false);
		outputArea.setEditable(false);
		outputArea.setBackground(Color.LIGHT_GRAY);
		this.add(pathField);
		this.add(browse);
		this.add(check);
		this.add(outputArea);	
	}

	public static void main (String args[]) {
		JFrame gridFrame = new JFrame ("Silent Circle Checker");
		gridFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		gridFrame.getContentPane().add(new MutedCircleChecker());
		gridFrame.pack();
		gridFrame.setVisible(true);	    
		
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

		@Override
		public void actionPerformed(ActionEvent arg0) {
			

			Writer writer = null;
			
			ArrayList<TimingPoint> timingPointList = new ArrayList<TimingPoint>();
			ArrayList<Integer> circleOffsetList = new ArrayList<Integer>();
			ArrayList<Integer> silentCircleList = new ArrayList<Integer>();
			File fileName = new File(pathField.getText());
		    String line = "";
		    int sectionCount = 1;
		    int currentLine = 0;
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
		        for (int circleOffset : silentCircleList) {	
		        	outputArea.append("Silent circle at " + circleOffset + "\n");
		        }
		        
		        if (silentCircleList.size()==0) {
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

