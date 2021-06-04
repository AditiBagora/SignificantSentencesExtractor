
package demo;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.util.List;

import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.ling.TaggedWord;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;


public class Demo 
{
  static String inputFileName="src/input/inputTech.txt";
  static String SummaryFilePath="src/output/Summary.txt";
  static double Compression_ratio=1;
  
  public static void main(String[] args) throws Exception 
  {
     DemoForm form=new DemoForm();
   
  }

  public  static class DemoForm extends JFrame implements ActionListener
  {
  
    JPanel panel,panel1,panel2;
    JLabel input_label, compression_ratio_label,inputFileLabel;
    static JLabel message;
    JTextField compression_ratio;
    JButton submit, cancel, browserJButton;
   
    DemoForm() 
    {
       
       input_label = new JLabel();
       inputFileLabel = new JLabel();
       input_label.setText("Choose Input File :");
       compression_ratio_label = new JLabel();
       compression_ratio_label.setText("Compression Ratio :");
       compression_ratio = new JTextField();
       submit = new JButton("Generate");
       browserJButton =new JButton("Browse");
       panel = new JPanel();
       panel2 = new JPanel();
       panel1 = new JPanel(new GridLayout(2,3));
       panel1.add(input_label);
       panel1.add(inputFileLabel);
       panel1.add(browserJButton);
       panel1.add(compression_ratio_label);
       panel1.add(compression_ratio);
       message = new JLabel();
       panel1.add(submit);
       panel2.add(message);
       panel.add(panel1);
       panel.add(panel2);
       setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
       // Adding the listeners to components..
       submit.addActionListener(this);
       browserJButton.addActionListener(this);
       add(panel, BorderLayout.CENTER);
       setTitle("Significant Sentence Extractor");
       setSize(700,150);
       setVisible(true);
    }

    @Override
    public  void actionPerformed(ActionEvent ae) 
    {
      if(((JButton)ae.getSource()).getText()=="Browse")
      {
       JFileChooser chooser = new JFileChooser();
       FileNameExtensionFilter filter = new FileNameExtensionFilter(
              "Text Files", "txt",".doc",".docx");
       chooser.setFileFilter(filter);
       int returnVal = chooser.showOpenDialog(null);
       if(returnVal == JFileChooser.APPROVE_OPTION) 
       {
         inputFileName=chooser.getSelectedFile().getAbsolutePath();
         inputFileLabel.setText(chooser.getSelectedFile().getName());
       }
      }
      else
      {
        Compression_ratio=Double.parseDouble(compression_ratio.getText());
        if(Compression_ratio==0)
         return;

         GenerateSignificantSentences();
         message.setText("Significant sentences genertaed at "+ FileSystems.getDefault().getPath(SummaryFilePath).normalize().toAbsolutePath().toString());
      }

    }
    public static void GenerateSignificantSentences()
    {

      try
      {
         List<List<HasWord>> sentences;
         String outputFileName="src/output/temp.txt";
         String TaggerPath="tagger//english-bidirectional-distsim.tagger";
         BufferedWriter outputWriter = new BufferedWriter(new FileWriter(outputFileName));
         BufferedWriter outputSummaryWriter = new BufferedWriter(new FileWriter(SummaryFilePath));
         MaxentTagger tagger = new MaxentTagger(TaggerPath);
         sentences = MaxentTagger.tokenizeText(new BufferedReader(new FileReader(inputFileName)));
         for (List<HasWord> sentence : sentences) 
         {
              List<TaggedWord> tSentence = tagger.tagSentence(sentence);
              Stemmer sentenceStemmer=new Stemmer();
              for(TaggedWord s:tSentence)
              {
                if((s.toString()).endsWith("/NNP")||(s.toString()).endsWith("/NN")||
                   (s.toString()).endsWith("/VBN")||(s.toString()).endsWith("/VBG"))
                    {
                         int np=(s.toString()).indexOf("/NNP");  int n=(s.toString()).indexOf("/NN");
                         int vb=(s.toString()).indexOf("/VBN");  int vg=(s.toString()).indexOf("/VBG");
                         if(np!=-1)
                         { 
                            String wordlist[]=(s.toString()).split("/NNP");
                            WriteOutputFile(" "+wordlist[0],outputWriter);
                         }
                         else if(n!=-1)
                         { 
                           String wordlist[]=(s.toString()).split("/NN");
                           WriteOutputFile(" "+wordlist[0],outputWriter);
                         }
                        else if(vb!=-1)
                        { 
                          String wordlist[]=(s.toString()).split("/VBN");
                          StemandWrite(wordlist[0],sentenceStemmer,outputWriter);
                        }
                       else if(vg!=-1)
                        { 
                          String wordlist[]=(s.toString()).split("/VBG");
                          StemandWrite(wordlist[0],sentenceStemmer,outputWriter);
                        }
           }
         }
         outputWriter.newLine();
        }
      outputWriter.close();
  
     Generator generator = new Generator();
     generator.OrgFilePath=inputFileName;
     generator.loadFile(outputFileName);
     generator.setKeywords(GetKeyWords(outputFileName));
     generator.compressRatio=Compression_ratio;
     for(String sentence:generator.generateSummary())
      {  
         WriteOutputFile(" "+sentence,outputSummaryWriter);
         outputSummaryWriter.newLine();
      }
      outputSummaryWriter.close();
    }
    catch(Exception ex)
     {
       message.setText("IO Exception "+ ex.getMessage());
     }
       
    }
    public static void StemandWrite(String sentence,Stemmer sentenceStemmer,BufferedWriter outputWriter)
  {
    try {

            sentenceStemmer.add(sentence.toCharArray(), sentence.length());
            sentenceStemmer.stem();
            String words=sentenceStemmer.toString();
            WriteOutputFile(" "+words,outputWriter);
        }
    catch(Exception e)
        {
              System.out.println("Exception"+ e.getMessage());
        }
      
  }
    public static void WriteOutputFile(String buffer,BufferedWriter outputWriter)
  {
    try {
           outputWriter.write(buffer);   
        }
    catch (IOException e)
        {
          System.out.println("Exception "+ e.getMessage());
        }
  }
    public static String[] GetKeyWords(String FilePath) 
    {
      int k; String[] KeyWords=null;
      File file = new File(FilePath);
      try
      {
         FileReader fr = new FileReader(file); 
        
         String fileContents = "";
         while((k =  fr.read())!=-1)
         {
            char ch = (char)k;
            fileContents = fileContents + ch; 
         }
         fr.close();
      fileContents=fileContents.replaceAll("\n", " ");
      KeyWords= fileContents.split(" ");
      return KeyWords;
     }
     catch(Exception ex)
     {
       KeyWords=new String[1];
       KeyWords[0]="";
       return KeyWords;
     }
    }
  }

}


