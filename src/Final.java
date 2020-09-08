import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Map.Entry;
import java.util.TreeMap;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class Final extends Application {
	static ListView<String> listView = new ListView<>();
	static ObservableList<String> items = FXCollections.observableArrayList();
	
	@Override
	public void start(Stage primaryStage) throws IOException{
		
		TextField textPath = new TextField();
		textPath.setEditable(false);
		textPath.setMinWidth(380);
		TextField textURL = new TextField();
		textURL.setMinWidth(380);
		Label label = new Label("OR |  Enter URL");
		
		listView.setPrefHeight(400);
		listView.setPrefWidth(400);
		
		Button fileBtn = new Button("Choose a File");
		Button submit = new Button("Submit");
		
		fileBtn.setOnAction(e -> {
			FileChooser fc = new FileChooser();
			File selectedFile = fc.showOpenDialog(null);
			if (selectedFile != null) {
				String path = selectedFile.getAbsolutePath().replace('\\', '/');
				textPath.setText(path);
			} else {
				//TODO: setup a debug for wrong files
			}
		});
		
		submit.setOnAction(e -> {
			if (textURL.getText().length() > 0) { //Load from URL
				Document doc = null; 
				try {
					doc = Jsoup.connect(textURL.getText()).maxBodySize(0).timeout(0).get();
					beginList(doc);
				} catch (IOException econnect) {
					econnect.printStackTrace();
				} 
				
			} else if (textPath.getText().length() > 0) { //Load from .HTML file 
				File input = new File(textPath.getText()); 
				Document doc = null;
				try {
					doc = Jsoup.parse(input, "UTF-8");
					beginList(doc);
					
				} catch (IOException edocCreation) {
					edocCreation.printStackTrace();
				} 
		    	
			} else {
				items.add("Select a .HTML file or enter a valid URL. Then hit submit.");
				listView.setItems(items);
			}
		});
		
		FlowPane root = new FlowPane(Orientation.HORIZONTAL, 5, 5);
        root.setPadding(new Insets(5));
        root.getChildren().addAll(fileBtn, textPath, label, textURL, submit, listView);
		Scene scene = new Scene(root, 500, 500);
		primaryStage.setTitle("Text Reader");
		primaryStage.setScene(scene);
		primaryStage.show();
		
	}
    public static void main(String[] args) throws IOException {
    	launch(args);
    }
    public static void beginList(Document doc) {
    	try {
		      int totalWords = 0;
		      TreeMap<String, Integer> freqMap = generateFrequencyList(doc);
		      for (String key : freqMap.keySet()) { //loop to find how many words were in the file (used for the percentage calculation)
		    	  totalWords += freqMap.get(key);
		      }
		      items.add("The total number of words found: " + totalWords);
		      items.add("Word\t\tCount\t\tPercentage");
		      
		      for (int i = 0; i < 20; i++) {  //loop to print the top 20 words in a file
		    	  int maxValueInMap = (Collections.max(freqMap.values())); //finds the max frequency in the Treemap
		    	  String keyToRemove = ""; //declare a String for key removal after the row has been displayed
		    	  for (Entry<String, Integer> entry : freqMap.entrySet()) {  // Iterates to display a row
		              if (entry.getValue() == maxValueInMap) { //if the current max value has been found 
		            	  items.add(entry.getKey() + "\t\t\t" + maxValueInMap + "\t\t\t" + ((double)freqMap.get(entry.getKey())*100.0/(double)totalWords) + " %");
		                  keyToRemove = entry.getKey(); //sets the String for removal outside for loop
		              }
		          }
		    	  freqMap.remove(keyToRemove); //removes the key so that Collections.max() can find the 2nd max key 
		      }
		      listView.setItems(items);
	    } catch (Exception ehtml) {
	    	ehtml.printStackTrace();
	    }
    }
    public static TreeMap<String, Integer> generateFrequencyList(Document doc) throws IOException {
	    TreeMap<String, Integer> freqMap = new TreeMap<String, Integer>(); //A Treemap for word storage: ("the_word , frequency")
	    
		String [] tokens = doc.text().split("\\s+"); //split entire .HTML text into tokens in a String array based on spaces
		
		for (String token : tokens) { //iterates through the entire array of words
			token = token.replaceAll("[^a-zA-Z]", ""); //removes all punctuation
			token = token.toLowerCase(); //lowercase all tokens
			if (!freqMap.containsKey(token)) {
				freqMap.put(token, 1); //if a word is occurring for the first time it will be given a 1 frequency
			} else {
				int count = freqMap.get(token); //gets the current frequency
				freqMap.put(token, count + 1); //adds one to the current frequency
			}
		}
		return freqMap;
	  }
}