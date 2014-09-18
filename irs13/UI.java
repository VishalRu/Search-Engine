package edu.asu.irs13;

//import AutoSuggestor;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import javax.swing.DefaultListCellRenderer;
//import javax.print.DocFlavor;
//import javax.print.DocFlavor.URL;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.WordUtils;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.store.FSDirectory;
//import org.eclipse.ui;

public class UI {
	private static JFrame jFrame=new JFrame("My Search Engine"); 
	private JTextField searchField = new JTextField(30);
	final SearchBoxModel sbm;
	JComboBox searchbox = new JComboBox();
	private HashMap<String,Double> idf_val=new HashMap<String,Double>();
	ArrayList<String> words = new ArrayList<>();
	scalarCluster tempObj=new scalarCluster();
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					UI window = new UI();
					//	window.frame.setVisible(true);
					window.jFrame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	public UI() throws Exception {
		SearchFiles TFobj=new SearchFiles();
		TFobj.geneateMagnitudeOfDoc();
		tempObj.getTFdocs();
		this.idf_val=TFobj.getIdfMap(); 
		Set<String> IDFkeyset=this.idf_val.keySet();
		int i=0;
		for(String word: IDFkeyset)
		{
			if(i++>10000) break;
			if((StringUtils.isAlpha(word) && word.length()>3) && !StringUtils.isNumeric(word)) 
					words.add(word);
		}
		 sbm = new SearchBoxModel(searchbox,idf_val);
		initialize();
	}

	public void createSnipetGeneration()
	{
		JButton TfSnipbutton = new JButton();
		TfSnipbutton.setText("TF/IDF (with snipets)");
		 
	    ActionListener TfbuttonHandlerSnipet = new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				try {
					
					IndexReader r = IndexReader.open(FSDirectory.open(new File("index")));
			//		String q = searchField.getText();
					String q=sbm.getValCombo();
					
					if(!idf_val.containsKey(q))
					{
						System.out.println("QUERY word not present in corpus");
						Set<String> IDFkeyset=idf_val.keySet();
						String suggestedKeyword="";
						int temp=StringUtils.getLevenshteinDistance(q, "carl");
						for(String word: IDFkeyset)
						{
							int LevenDistance=StringUtils.getLevenshteinDistance(q, word);
							if(LevenDistance<temp)
							{
								suggestedKeyword=word;
								temp=LevenDistance;
							}
						}
						JLabel suggestion=new JLabel("Did you mean?  "+ suggestedKeyword );
						System.out.println(suggestedKeyword);
						//suggestion.setBackground(Color.blue);
						suggestion.setForeground(Color.blue);
						jFrame.add(suggestion);
						jFrame.repaint();
						jFrame.revalidate();
						
					}
					else
					{					
					snipetGeneration tempObj =new snipetGeneration();
				//	tempObj.getTFdocs(q);
				//	tempObj.SnipetHandler();
					tempObj.getSnipet(q);
					HashMap<Integer,String> docLineMap=new HashMap<Integer,String>();
					HashMap<Integer,String> docTitleMap=new HashMap<Integer,String>();
					docLineMap=	tempObj.getDocLineMaping();
					docTitleMap=tempObj.getDocTitleMap();
					ArrayList<Integer> topTFdocs=new ArrayList<Integer>(tempObj.getTFdocs());						
					 final DefaultListModel snipetModel = new DefaultListModel();
					// snipetModel.addElement("Results are");
					 snipetModel.addElement("Please single click on the the URL to go to that page"); 
				//	 snipetModel.addElement("\r\n");
					
			
					 for(int i=0;i<topTFdocs.size();i++)
					 {
						 int doc=topTFdocs.get(i);
						 String d_url = r.document(doc).getFieldable("path").stringValue().replace("%%", "/");
						if (docTitleMap.get(doc)==null) continue;
						 snipetModel.addElement("<html><u><h3 style='color:66CC66;' >"+docTitleMap.get(doc)+"</h3></u> </html>");
						//snipetModel.addElement("<html><u><h5 style='color:66CC66;' >"+d_url+"</h5></u> </html>");
						 snipetModel.addElement(d_url);
						System.out.println(WordUtils.wrap(docLineMap.get(doc),80));
						snipetModel.addElement(WordUtils.wrap(docLineMap.get(doc), 80));
					//	snipetModel.addElement(docLineMap.get(doc));
						 
					 }
					
				 final JList	snipetListBox = new JList( snipetModel );
					JScrollPane scrollPane = new JScrollPane(snipetListBox);
						
					jFrame.getContentPane().add(scrollPane);
			
				snipetListBox.setVisibleRowCount(10);
				
					
					
					MouseListener snipetMouseListener = new MouseAdapter() {
				     public void mouseClicked(MouseEvent e) {
				         if (e.getClickCount() == 1) {
				             int index = snipetListBox.locationToIndex(e.getPoint());
				             String url= snipetModel.getElementAt(index).toString();
				            
				                 try {
								Desktop.getDesktop().browse(new URL("http://"+url).toURI());
							} catch (IOException
									| URISyntaxException e1) {
								
								e1.printStackTrace();
							}
				                
				          }
				     }
				 };
				 snipetListBox.setBackground(Color.white);
				 snipetListBox.setForeground(Color.blue);
				 snipetListBox.addMouseListener(snipetMouseListener); 
			
				 jFrame.add(scrollPane);
				jFrame.add(snipetListBox);
				jFrame.repaint();
				jFrame.revalidate();	
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
			
		  };
		  
		  
		  TfSnipbutton.addActionListener(TfbuttonHandlerSnipet);
		 jFrame.add(TfSnipbutton);
	}
	
	public void createClusterDocs()
	{
			JButton Tfbutton = new JButton();
			Tfbutton.setText("TF/IDF (with clustering)");
			 
		    ActionListener TfbuttonHandler = new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent arg0) {
					try {
						
						IndexReader r = IndexReader.open(FSDirectory.open(new File("index")));
					//	String q = searchField.getText();
						String q=sbm.getValCombo();
						KmeansCluster tempObject=new KmeansCluster(3,10);
						tempObject.getTFdocs(q);
						tempObject.setdocVectoMap();
						tempObject.iterate(10);
						tempObject.printClusterMap();
						HashMap<Integer,Integer> docMap=new HashMap<Integer,Integer>(tempObject.getDocClusterMaping());
						HashMap<Integer,String> clusterTermMap=new HashMap<Integer,String>(tempObject.getClusterSummaryMap());						
						 final DefaultListModel clusterZeromodel = new DefaultListModel();
						 final DefaultListModel clusterOnemodel = new DefaultListModel();
						 final DefaultListModel clusterTwomodel = new DefaultListModel();
						 clusterZeromodel.addElement("Cluster 01");
						 clusterZeromodel.addElement("Please single click on the the URL to go to that page"); 
						 String summary=clusterTermMap.get(0);
						 clusterZeromodel.addElement("current cluster talks about "+summary);
						 clusterZeromodel.addElement("\r\n");
						 
						 clusterOnemodel.addElement("Cluster 02");
						 clusterOnemodel.addElement("Please single click on the the URL to go to that page");
						 String summary1=clusterTermMap.get(1);
						 clusterOnemodel.addElement("current cluster talks about "+summary1);
						 clusterOnemodel.addElement("\r\n");
						 
						 clusterTwomodel.addElement("Cluster 03");
						 clusterTwomodel.addElement("Please single click on the the URL to go to that page");
						 String summary2=clusterTermMap.get(2);
						 clusterTwomodel.addElement("current cluster talks about "+summary2);
						 clusterTwomodel.addElement("\r\n");
						 

							for(Map.Entry<Integer,Integer> e : docMap.entrySet())
							{
								int clusterId=e.getValue();
								int docId=e.getKey();
								String d_url = r.document(docId).getFieldable("path").stringValue().replace("%%", "/");
								if(clusterId==0)
									clusterZeromodel.addElement(d_url);
								else if(clusterId==1)
									clusterOnemodel.addElement(d_url);
								else
									clusterTwomodel.addElement(d_url);
							}
						
						final JList	ClusterZeroListBox = new JList( clusterZeromodel );
						JScrollPane scrollPane = new JScrollPane(ClusterZeroListBox);
						final JList	ClusterOneListBox = new JList( clusterOnemodel );
						JScrollPane scrollPaneOne = new JScrollPane(ClusterOneListBox);
						final JList	ClusterTwoListBox = new JList( clusterTwomodel );
						JScrollPane scrollPaneTwo = new JScrollPane(ClusterTwoListBox);
						
						
						MouseListener ClusterMouseListener = new MouseAdapter() {
					     public void mouseClicked(MouseEvent e) {
					         if (e.getClickCount() == 1) {
					             int index = ClusterZeroListBox.locationToIndex(e.getPoint());
					             String url= clusterZeromodel.getElementAt(index).toString();
					            
					                 try {
									Desktop.getDesktop().browse(new URL("http://"+url).toURI());
								} catch (IOException
										| URISyntaxException e1) {
									// TODO Auto-generated catch block
									e1.printStackTrace();
								}
					                  //  System.out.println("Double clicked on Item " + index);
					          }
					     }
					 };
					 
					 MouseListener ClusterOneMouseListener = new MouseAdapter() {
					     public void mouseClicked(MouseEvent e) {
					         if (e.getClickCount() == 1) {
					             int index = ClusterOneListBox.locationToIndex(e.getPoint());
					             String url= clusterOnemodel.getElementAt(index).toString();
					            
					                 try {
									Desktop.getDesktop().browse(new URL("http://"+url).toURI());
								} catch (IOException
										| URISyntaxException e1) {
									// TODO Auto-generated catch block
									e1.printStackTrace();
								}
					                  //  System.out.println("Double clicked on Item " + index);
					          }
					     }
					 };
					 
					 
					 MouseListener ClusterTwoMouseListener = new MouseAdapter() {
					     public void mouseClicked(MouseEvent e) {
					         if (e.getClickCount() == 1) {
					             int index = ClusterTwoListBox.locationToIndex(e.getPoint());
					             String url= clusterTwomodel.getElementAt(index).toString();
					            
					                 try {
									Desktop.getDesktop().browse(new URL("http://"+url).toURI());
								} catch (IOException
										| URISyntaxException e1) {
									// TODO Auto-generated catch block
									e1.printStackTrace();
								}
					                  //  System.out.println("Double clicked on Item " + index);
					          }
					     }
					 };
					 ClusterZeroListBox.setBackground(Color.white);
					 ClusterZeroListBox.setForeground(Color.blue);
					 ClusterOneListBox.setBackground(Color.white);
					 ClusterOneListBox.setForeground(Color.blue);
					 ClusterTwoListBox.setBackground(Color.white);
					 ClusterTwoListBox.setForeground(Color.blue);
					 
					
					 
					ClusterZeroListBox.addMouseListener(ClusterMouseListener); 
					ClusterOneListBox.addMouseListener(ClusterOneMouseListener);
					ClusterTwoListBox.addMouseListener(ClusterTwoMouseListener);
									
					jFrame.repaint();
					jFrame.add(ClusterZeroListBox);
					jFrame.add(ClusterOneListBox);
					jFrame.add(ClusterTwoListBox);
					jFrame.revalidate();	
					
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
				}
				
			  };
			  Tfbutton.addActionListener(TfbuttonHandler);
			 jFrame.add(Tfbutton);
		   
	}
	

	private void initialize() throws Exception {
		jFrame = new JFrame("Search UI");
		jFrame.setSize(1200, 1000); 
		JLabel jlab=new JLabel("Please enter your Query");
		jFrame.add(jlab);
		//jFrame. setContentPane(new JLabel(new ImageIcon("/Users/vishalruhela/Desktop/Downloads_New/field_with_moons-wallpaper-1920x1080.jpg")));
			
		searchField.setText("");
		
		//JComboBox searchbox = new JComboBox();
		searchbox.setBounds(0, 0, 10, 5);
		searchbox.setEditable(true);
	//	final SearchBoxModel sbm = new SearchBoxModel(searchbox,idf_val);
		searchbox.setModel(sbm);
		searchbox.addItemListener(sbm);
		jFrame.add(searchbox);
		
	      //   jFrame.add(searchField);
	    
	    JButton jButton = new JButton();
	    jButton.setText("TF IDF Search");
		 
	    /*
	    ActionListener searchHandler = new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				try {
					
					IndexReader r = IndexReader.open(FSDirectory.open(new File("index")));
				//	String q = searchField.getText();
					String q=sbm.getValCombo();
					
					if(!idf_val.containsKey(q))
					{
						System.out.println("QUERY word not present in corpus");
						Set<String> IDFkeyset=idf_val.keySet();
						String suggestedKeyword="";
						int temp=StringUtils.getLevenshteinDistance(q, "carl");
						for(String word: IDFkeyset)
						{
							int LevenDistance=StringUtils.getLevenshteinDistance(q, word);
							if(LevenDistance<temp)
							{
								suggestedKeyword=word;
								temp=LevenDistance;
							}
						}
						JLabel suggestion=new JLabel("Did you mean?  "+ suggestedKeyword );
						System.out.println(suggestedKeyword);
						//suggestion.setBackground(Color.blue);
						suggestion.setForeground(Color.blue);
						jFrame.add(suggestion);
						jFrame.repaint();
						jFrame.revalidate();
						
					}
					else
					{
					SearchFiles searchObj=new SearchFiles();
					searchObj.geneateMagnitudeOfDoc();
					searchObj.tf_idf_search(q);
					ArrayList<Integer> TFdocs=new ArrayList<Integer>(searchObj.getTopDocOfTFIDF());
									
					 final DefaultListModel model = new DefaultListModel();
					model.addElement("TF IDF Docs");
					model.addElement("Please single click on the the URL to go to that page");
					model.addElement("\r\n");
					for(Integer doc :TFdocs )
					{
						String d_url = r.document(doc).getFieldable("path").stringValue().replace("%%", "/");
						model.addElement(d_url);
					}
							
					final JList	listbox = new JList( model );
					JScrollPane scrollPane = new JScrollPane(listbox);
					MouseListener mouseListener = new MouseAdapter() {
				     public void mouseClicked(MouseEvent e) {
				         if (e.getClickCount() == 1) {
				             int index = listbox.locationToIndex(e.getPoint());
				             String url= model.getElementAt(index).toString();
				            
				                 try {
								Desktop.getDesktop().browse(new URL("http://"+url).toURI());
							} catch (IOException
									| URISyntaxException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}
				                  //  System.out.println("Double clicked on Item " + index);
				          }
				     }
					
				 };
			
				listbox.setForeground(Color.blue);
				listbox.setBackground(Color.white);  
				 listbox.addMouseListener(mouseListener);
						listbox.setAlignmentX(50);;
						listbox.setAlignmentY(20);
		
				jFrame.repaint();
				jFrame.add(listbox);
				jFrame.revalidate();
				
				} 
				}
								
				catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
			
		  };
	  
		 jButton.addActionListener(searchHandler);	
	    jFrame.add(jButton);
			*/    
	    JButton HubAuthButton = new JButton();
	    HubAuthButton.setText("HubAuth Search");
	   
	    
	    ActionListener hubAuthHandler = new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				try {
				
					IndexReader r = IndexReader.open(FSDirectory.open(new File("index")));
					HUBS_Auth hubObj=new HUBS_Auth();
				//	String q = searchField.getText();
					String q=sbm.getValCombo();
					hubObj.generateBaseSet(q);
					hubObj.setAdjcacencyMat();
					hubObj.findAuthAndHub();
					hubObj.showTopTenAuths();
					hubObj.showTopTenHubs();
					ArrayList<Integer> AuthVector=new ArrayList<Integer>(hubObj.getTopAuth());
					ArrayList<Integer> hubVector=new ArrayList<Integer>(hubObj.getTopHub());
					
					 final DefaultListModel modelAuth = new DefaultListModel();
					 modelAuth.addElement("Top Auth Docs");
					 modelAuth.addElement("Please single click on the the URL to go to that page");
					 modelAuth.addElement("\r\n");
						for(Integer doc :AuthVector )
						{
							String d_url = r.document(doc).getFieldable("path").stringValue().replace("%%", "/");
						//	modelAuth.addElement("Doc ID :" + doc+ " URL is "+d_url );
							modelAuth.addElement(d_url);
						}
						
					final JList 	listboxAuth = new JList( modelAuth );
					MouseListener mouseListenerAuth = new MouseAdapter() {
					     public void mouseClicked(MouseEvent e1) {
					         if (e1.getClickCount() == 1) {
					             int index = listboxAuth.locationToIndex(e1.getPoint());
					             String url= modelAuth.getElementAt(index).toString();
					            
					                 try {
									Desktop.getDesktop().browse(new URL("http://"+url).toURI());
								} catch (IOException
										| URISyntaxException e11) {
									// TODO Auto-generated catch block
									e11.printStackTrace();
								}
					                  //  System.out.println("Double clicked on Item " + index);
					          }
					     }
					 };
					 listboxAuth.setBackground(Color.white);
					 listboxAuth.setForeground(Color.blue);
					  listboxAuth.addMouseListener(mouseListenerAuth);
					  listboxAuth.setLayout(new BorderLayout());
					jFrame.add(listboxAuth);
						
					final DefaultListModel modelHub = new DefaultListModel();
					modelHub.addElement("Top Hub Docs");
					modelHub.addElement("\r\n");
						for(Integer doc :hubVector )
						{
							String d_url = r.document(doc).getFieldable("path").stringValue().replace("%%", "/");
							modelHub.addElement(d_url );
						}
						final JList 	listboxHub = new JList( modelHub );
						MouseListener mouseListenerHub = new MouseAdapter() {
						     public void mouseClicked(MouseEvent e1) {
						         if (e1.getClickCount() == 1) {
						             int index = listboxHub.locationToIndex(e1.getPoint());
						             String url= modelHub.getElementAt(index).toString();
						            
						                 try {
										Desktop.getDesktop().browse(new URL("http://"+url).toURI());
									} catch (IOException
											| URISyntaxException e11) {
										// TODO Auto-generated catch block
										e11.printStackTrace();
									}
						                  //  System.out.println("Double clicked on Item " + index);
						          }
						     }
						 };
						 listboxHub.setBackground(Color.white);
						 listboxHub.setForeground(Color.blue);
						 listboxHub.setAlignmentX(50);;
						 listboxHub.setAlignmentY(20);
						 listboxAuth.setAlignmentX(100);;
						 listboxAuth.setAlignmentY(20);
						 listboxHub.addMouseListener(mouseListenerHub);
						 jFrame.repaint();	 
					jFrame.add(listboxHub);
				jFrame.revalidate();
					
				}
				catch(Exception e )
				{
					
				}
			}
};
		HubAuthButton.addActionListener(hubAuthHandler);
		jFrame.add(HubAuthButton);
		
	    JButton prButton = new JButton();
	    prButton.setText("Page Rank Search");
		 
	    ActionListener prHandler = new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				try {
					
					IndexReader r = IndexReader.open(FSDirectory.open(new File("index")));
				//	String q = searchField.getText();
					String q=sbm.getValCombo();
					PR pageRankObj=new PR();
					
					ArrayList<Integer> PRdocs=new ArrayList<Integer>( pageRankObj.getPageRankDocs(q));
					
					 final DefaultListModel PRmodel = new DefaultListModel();
					 PRmodel.addElement("Page Rank Documents");
					 PRmodel.addElement("Please single click on the the URL to go to that page");
					 PRmodel.addElement("\r\n");
					for(Integer doc :PRdocs )
					{
						String d_url = r.document(doc).getFieldable("path").stringValue().replace("%%", "/");
						PRmodel.addElement(d_url);
					}
					
					final JList	pageRankListbox = new JList( PRmodel );
					JScrollPane scrollPane = new JScrollPane(pageRankListbox);
					MouseListener PRmouseListener = new MouseAdapter() {
				     public void mouseClicked(MouseEvent e) {
				         if (e.getClickCount() == 1) {
				             int index = pageRankListbox.locationToIndex(e.getPoint());
				             String url= PRmodel.getElementAt(index).toString();
				            
				                 try {
								Desktop.getDesktop().browse(new URL("http://"+url).toURI());
							} catch (IOException
									| URISyntaxException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}
				                  //  System.out.println("Double clicked on Item " + index);
				          }
				     }
				 };
				 pageRankListbox.setBackground(Color.white);
				 pageRankListbox.setForeground(Color.blue);
				 pageRankListbox.addMouseListener(PRmouseListener);
						
				scrollPane.setViewportView(pageRankListbox);
		
				
				jFrame.repaint();
				jFrame.add(pageRankListbox);
			jFrame.revalidate();	
				
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
			
		  };
		  
		  prButton.addActionListener(prHandler);	
	    jFrame.add(prButton);
	
	    createClusterDocs();
	    createSnipetGeneration();
	 //   jFrame.add(newTextField);
	    jFrame.setVisible(true);
	jFrame.setLayout(new FlowLayout()); 
	//jFrame.setContentPane(newPane);
//	jFrame.pack();
		jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

}



