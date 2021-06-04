package demo;

import java.util.List;
import java.util.ArrayList;

public class Generator
{
  private InputDocument inputDoc;

  // compression ratio for the text summary
  // i.e., here compression is 1/3 of the original text
  public double compressRatio = 0.3;

  public String OrgFilePath="";

  private int toIndex;

  public void loadFile(String inputFile){
    inputDoc = new InputDocument();
    inputDoc.loadFile(inputFile);
    inputDoc.OrgFilePath=OrgFilePath;
  }

  public void setKeywords(String[] keywords){
    
    List<String> processedTermList = new ArrayList<String>();
    TermPreprocessor tp = new TermPreprocessor();
    
    String resultTerm = null;
    for(String term : keywords){
      resultTerm = tp.preprocess(term);

      if(resultTerm !=null)
        processedTermList.add(resultTerm);
    }

    processedTermList.toArray(new String[processedTermList.size()]);

  }

  public List<String> generateSummary(){
    List<String> significantSentences = generateSignificantSentences();
    toIndex = (int)(compressRatio*(significantSentences.size()-1));
    return significantSentences.subList(0, toIndex);
  }

  public List<String> generateSignificantSentences()
  {
    Integer[] SentencesOrder = calcAllSentenceScores();
    String[] allSentences=inputDoc.GetOrgSentences().split("\n");
    List<String> significantSentenceOrder= new  ArrayList<String>();
    // ***TO WRITE REST OF CODE
    for (Integer element : SentencesOrder) 
    { 
      significantSentenceOrder.add(allSentences[element]);
    }
    return significantSentenceOrder;
  }

  public String[] getAllSentences(){
    return inputDoc.getAllSentences();
  }

  public Integer[] calcAllSentenceScores()
  {
    int k;
     String[] sentences=getAllSentences()[0].split("\n");
     String[][] words=new String[sentences.length][];
    
    for(int i=0; i<sentences.length; ++i)
    {
        String x[]=sentences[i].split(" ");
        words[i]=new String[x.length]; 
        for(int j=0; j<x.length; ++j)
        { 
          words[i][j]=x[j].toLowerCase().trim();
         
        }
      }
    
    //words 2-D array has words of each sentence stored in it.
    Double similarity[][]=new Double[words.length][words.length];
    double rank[]=new double[words.length];
    double sum=0, avg=0;
    for(int i=0; i<words.length; ++i)
      {   rank[i]=0.0;
          for(int j=0; j<words.length; ++j)
          {   similarity[i][j]=0.0;
              double y = (((double)words[i].length+(double)words[j].length)/2);
              
              for(int c1=0; c1<words[i].length; ++c1)
                {for(int c2=0; c2<words[j].length; ++c2)
                  {   if((words[i][c1]).equals(words[j][c2]))
                         similarity[i][j]+=(1/y); 
                  }
                }
          sum+=similarity[i][j];
          rank[i]+=similarity[i][j];
          }
      }
    
      sum=0;
      for(int i=0; i<words.length; ++i)
      {
          sum+=rank[i];
      }
      
      System.out.println();
      Integer num[]=new Integer[words.length];
      int pos=0;
      k=0;
      double max=rank[0];
      for(int i=0; i<words.length; ++i)
        {
          pos=0;  max=rank[0];
          for(int j=0; j<words.length; ++j)
          {    
              if(max<rank[j])
              { max=rank[j];
                pos=j;
              }
          }
          num[k]=pos;
          k++;
          rank[pos]=0.0;
          for(int j=0; j<words.length; ++j)
          {
            rank[j]-=similarity[j][pos];
            rank[j]=(rank[j]>=1)? rank[j]:0;
          }
          }
          return num;
    }
  
}

