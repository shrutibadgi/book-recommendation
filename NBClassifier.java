package com.kpt;

import java.io.*;
import java.util.*;

/***
   *  NBClassifier for BOOKSPREE
   **/

class NBClassifier{
      
	   ArrayList<String> bookAuthor = new ArrayList<String>();
	   ArrayList<ArrayList<String>> bookAuthorList = new ArrayList<ArrayList<String>>();
	   ArrayList<String> trainingDocs = new ArrayList<String>();
	   ArrayList<String> trainingLabels = new ArrayList<String>();
	   int[] classCounts; //number of docs per class
	   String[] classStrings; //concatenated string for a given class
	   int[] classTokenCounts; //total number of tokens per class
	   HashMap<String,Double>[] condProb;
	   HashSet<String> vocabulary; //entire vocabuary
	   ArrayList<String> stopList = new ArrayList<String>(); //stop words list
	   
	   ArrayList<String> trainDocs = new ArrayList<String>();
	   ArrayList<String> trainLabels =new ArrayList<String>();
	   int numClasses = 6;
	   
	   ArrayList<String> testDocs = new ArrayList<String>();
	   ArrayList<String> testLabels =new ArrayList<String>();
	   double totalTest = 0.0;
	   ArrayList<String> newTestDocs = new ArrayList<String>();
		Double thresholdvalue = 1.0;
	   
	   ArrayList<String> stemmedList;
	   ArrayList<ArrayList<String>> topwords =  new ArrayList<ArrayList<String>>();
	   ArrayList<String> preLabel = new ArrayList<String>();
	   
	      
	   public NBClassifier()
	   {
	      
	      readTrainData();
	      
	      classCounts = new int[numClasses];
	      classStrings = new String[numClasses];
	      classTokenCounts = new int[numClasses];
	      condProb = new HashMap[numClasses];
	      vocabulary = new HashSet<String>();
	      
	      //Reading stop word file
	      //stopFileRead();
	      
	      //collecting labels and all docs under each label
	      for(int i=0;i<trainDocs.size();i++){
	         
	         String label = trainLabels.get(i);
	         String doc = trainDocs.get(i);
	         ArrayList<String> authList = new ArrayList<String>();
	         //testing
	      //         System.out.println("doc: "+trainLabels.get(i)+"--"+trainDocs.get(i));
	         
	         if(trainingLabels.contains(label)){
	            int index = trainingLabels.indexOf(label);
	            String inListDoc = trainingDocs.get(index)+" "+doc;
	            trainingDocs.set(index, inListDoc);
	            authList = bookAuthorList.get(index);
	            authList.add(bookAuthor.get(i));
	            bookAuthorList.set(index, authList);
	            int count = classCounts[index];
	            classCounts[index] = count+1;
	         }
	         else{           
	            trainingLabels.add(label);
	            trainingDocs.add(doc); 
	            authList.add(bookAuthor.get(i));
	            bookAuthorList.add(authList);
	            int index = trainingLabels.size()-1;
	            classCounts[index] = 1;        
	         }
	      } 
	      
//	       for(int p=0;p<trainingLabels.size();p++){
//	          System.out.println("- "+trainingLabels.get(p));
//	       }
	      System.out.println("bookauthLen "+bookAuthorList.size());
	      //initilization
	      for(int i=0;i<numClasses;i++){
	         classStrings[i] = "";
	         condProb[i] = new HashMap<String,Double>();
	      }
	      
	      //assigning classStrings from trainingDocs
	      for(int i=0;i<trainingLabels.size();i++){
	         classStrings[i] = trainingDocs.get(i) + " ";
	      }
	      
	      //tokenizing from the collection of documents for each class
	      for(int i=0;i<numClasses;i++){
	         String[] tokens = classStrings[i].split(" ");
	         
	         ArrayList<String> tokenized = new ArrayList<String>();
	         
	      	//collecting the tokens after removing stop words
	         for(String token:tokens){
	           // if(searchStopword(token)==-1){
	               tokenized.add(token);
	            //}
	         }
	         //stemingFunction(tokenized);
	         //tokenized = stemmedList;
	         
	         //calculating number of tokens per class
	         classTokenCounts[i] = tokenized.size();
	         
	         //calculating conditional probability
	         for(int j=0;j<tokenized.size();j++){
	            String token = tokenized.get(j);
	            if(condProb[i].containsKey(token)){
	               double count = condProb[i].get(token);
	               condProb[i].put(token, count+1);
	            }
	            else
	               condProb[i].put(token, 1.0);
	            vocabulary.add(token);  //add to vocabulary of whole dataset
	         }
	      }      
	      
	   	//computing the class conditional probability
	      Double dummy = 0.0;
	      for(int i=0;i<numClasses;i++){
	         //writeFile(trainLabels.get(i), dummy);
	         Iterator<Map.Entry<String, Double>> iterator = condProb[i].entrySet().iterator();
	         int vSize = vocabulary.size();
	         while(iterator.hasNext())
	         {
	            Map.Entry<String, Double> entry = iterator.next();
	            String token = entry.getKey();
	            Double count = entry.getValue();
	            //if(count>thresholdvalue){
	               count = (count+1)/(classTokenCounts[i]+vSize);
	               condProb[i].put(token, count);
	            //}
	           // writeFile(token, count);
	         }
	         //System.out.println(condProb[i]);
	      }
	      readTopWords();
	   }
	   
	   //method to stem the tokens
	   private void stemingFunction(ArrayList<String> tkstm){
	      
	      stemmedList = new ArrayList<String>();
	      for(int i=0; i<tkstm.size(); i++){
	         Stemmer st = new Stemmer();
	         String toStem = tkstm.get(i);
	   		st.add(toStem.toCharArray(),toStem.length());
	   		st.stem();
	         String stemString = st.toString();
	         stemmedList.add(stemString);
	      }
	   }
	   
	   //method to find class word distribution
	   private void writeFile(String data, String c){
	   
	      try{
	         BufferedWriter bfw = new BufferedWriter(new FileWriter(c, true));
	         bfw.write(data);
	         bfw.flush();
	         bfw.close();
	      } 
	      catch (Exception e) {
	         e.printStackTrace();
	      }
	   }
		
	   //classifier for testData
	   public String classify(String doc){
	      String label = "";
	      //System.out.println("doc="+doc);
	      int vSize = vocabulary.size();
	      double[] score = new double[numClasses];
	      for(int i=0;i<score.length;i++){
	         score[i] = Math.log(classCounts[i]*1.0/trainDocs.size());
	      }
	      String[] tokens = doc.split(" ");
	      for(int i=0;i<numClasses;i++){
	         int c = 0;
	         for(String token: tokens){
	            if(condProb[i].containsKey(token)){
	               for(int n=0;n<preLabel.size();n++){
	                  if(topwords.get(i).contains(token)){
	                     c++;
	                  }
	               }
	               if(c>0){
	                  score[i] += Math.log(condProb[i].get(token)+c);
	               }else{
	                  score[i] += Math.log(condProb[i].get(token));
	               }
	            }
	            else
	               score[i] += Math.log(1.0/(classTokenCounts[i]+vSize));
	            
	         }
	      }
	      double maxScore = score[0];
	      label = trainingLabels.get(0);
	      for(int i=0;i<score.length;i++){
	         //System.out.println("Score - -> "+score[i]);
	         if(score[i]>maxScore){
	            label= trainingLabels.get(i);
	            maxScore = score[i];
	         }
	      }
	      return label;
	   }
		
	    //reading stop words
	   private void stopFileRead(){
	      
	      String stopListFile = "C:/Program Files (x86)/eclipse/workSpace/bookspreeWebApp/src/com/kpt/stopwords.txt";
	      try{        
	         int i = 0;
	         String line = null;
	         String allLines = new String();
	         String[] stopListArray;
	         BufferedReader br = new BufferedReader(new FileReader(stopListFile));			
	         while((line=br.readLine())!=null){
	            allLines = line.toLowerCase(); 
	            stopList.add(allLines);
	         }
	      }
	      catch(IOException ioe){
	         ioe.printStackTrace();
	      }
	   }
	   
	   //Remove Stop word method
	   public int searchStopword(String key){
	      int lo = 0;
	      int hi = (stopList.size())-1;
	      while(lo<=hi)
	      {
	      	//Key is in a[lo..hi] or not present
	         int mid = lo + (hi-lo)/2;
	         int result = key.compareTo(stopList.get(mid));
	         if(result <0) hi = mid - 1;
	         else if(result >0) lo = mid+1;
	         else 
	            return mid;
	      }
	      return -1;
	   }
	   
	   //reading data from topwords file
	   private void readTopWords(){
	     
	      String csvFile = "C:/Program Files (x86)/eclipse/workSpace/bookspreeWebApp/src/com/kpt/topwords.csv";
	      BufferedReader br = null;
	      String line = "";
	      String cvsSplitBy = ",";
	      ArrayList<String> list = new ArrayList<String>();
	   
	      try {
	         br = new BufferedReader(new FileReader(csvFile));
	         while ((line = br.readLine()) != null) {
	            String[] item = line.split(cvsSplitBy);
	            list.add(item[1]);
	            preLabel.add(item[0]);
	         }
	         for(int l=0; l<list.size(); l++){
	            String[] item = list.get(l).split(" ");
	            ArrayList<String> temp = new ArrayList<String>();
	            for(String t:item){
	               temp.add(t);
	            }
	            topwords.add(temp);
	         }
	      } 
	      catch (FileNotFoundException e) {
	         e.printStackTrace();
	      } 
	      catch (IOException e) {
	         e.printStackTrace();
	      } 
	      finally {
	         if (br != null) {
	            try {
	               br.close();
	            } 
	            catch (IOException e) {
	               e.printStackTrace();
	            }
	         }
	      }
	      // System.out.println("top words completed");
//	       for(int m=0; m<preLabel.size(); m++){
//	             System.out.print(""+preLabel.get(m)+": ");
//	             ArrayList<String> newT = new ArrayList<String>();
//	             newT = topwords.get(m);
//	             for(int n=0; n<newT.size(); n++){
//	                System.out.println(""+newT.get(n));
//	             }
//	        }
	   }

	   
	   //reading data from input file
	   private void readTrainData(){
	     
	      String csvFile = "C:/Program Files (x86)/eclipse/workSpace/bookspreeWebApp/src/com/kpt/updatedFinal.csv";
	      BufferedReader br = null;
	      String line = "";
	      String cvsSplitBy = ",";
	   
	      try {
	         br = new BufferedReader(new FileReader(csvFile));
	         while ((line = br.readLine()) != null) {
	            // use comma as separator
	            String[] item = line.split(cvsSplitBy);
	            trainDocs.add(item[0]);
	            trainLabels.add(item[1]);
	            bookAuthor.add(item[2]+" - "+item[3]);
	         }
	      } 
	      catch (FileNotFoundException e) {
	         e.printStackTrace();
	      } 
	      catch (IOException e) {
	         e.printStackTrace();
	      } 
	      finally {
	         if (br != null) {
	            try {
	               br.close();
	            } 
	            catch (IOException e) {
	               e.printStackTrace();
	            }
	         }
	      }
	      System.out.println("readTrainData completed");
	   }
	   
	   
	   //reading test data from input file
	   private void readTestData(){
	     
	      String csvFile = "C:/Program Files (x86)/eclipse/workSpace/bookspreeWebApp/src/com/kpt/testData.csv";
	      BufferedReader br = null;
	      String line = "";
	      String cvsSplitBy = ",";
	   
	      try {
	         br = new BufferedReader(new FileReader(csvFile));
	         while ((line = br.readLine()) != null) {
	            // use comma as separator
	            String[] item = line.split(cvsSplitBy);
	            testDocs.add(item[0]);
	            testLabels.add(item[1]);
	         }
	      } 
	      catch (FileNotFoundException e) {
	         e.printStackTrace();
	      } 
	      catch (IOException e) {
	         e.printStackTrace();
	      } 
	      finally {
	         if (br != null) {
	            try {
	               br.close();
	            } 
	            catch (IOException e) {
	               e.printStackTrace();
	            }
	         }
	      }
	      totalTest = testDocs.size();
	      System.out.println("readTestData completed. TestDocSize:"+totalTest);
	   }
	   
	   //collecting the tokens from test docs after removing stop words
	   private void removeStopTest(){
	      for(int i =0; i<totalTest; i++){
	         String[] tokens = testDocs.get(i).split(" ");
	         for(String token:tokens){
	            if(searchStopword(token)==-1){
	               newTestDocs.add(token);
	            }
	         }
	      }
	      testDocs = newTestDocs;
	   }
	   
	   private double compareData(){
	      int correct = 0;
	      double accuracy = 0.0;
	      for(int i =0; i<totalTest; i++){
	         //System.out.println("doc"+i+"--");
	         String label = classify(testDocs.get(i));
	         if(label.equals(testLabels.get(i))){
	            correct++;
	         }
	      }
	      System.out.println("correct: "+correct);
	      accuracy = correct/totalTest;
	      return accuracy;
	   }
}