package edu.asu.irs13;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import javax.swing.AbstractListModel;
import javax.swing.ComboBoxEditor;
import javax.swing.ComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;

import org.apache.commons.lang3.StringUtils;

public class SearchBoxModel extends AbstractListModel
                implements ComboBoxModel, KeyListener, ItemListener
{
    ArrayList<String> db = new ArrayList<String>();
    ArrayList<String> data = new ArrayList<String>();
    String selection;
    JComboBox cb;
    ComboBoxEditor cbe;
    int currPos = 0;
    SearchFiles TFobj;
    scalarCluster tempObj=new scalarCluster();
    public SearchBoxModel(JComboBox jcb,HashMap<String,Double> IDF) throws Exception
    {
    	 TFobj=new SearchFiles();
		TFobj.geneateMagnitudeOfDoc();
		tempObj.getTFdocs();
        cb = jcb;
        cbe = jcb.getEditor();

        cbe.getEditorComponent().addKeyListener(this);

        
    	Set<String> IDFkeyset=IDF.keySet();
		int i=0;
	
		for(String word: IDFkeyset)
		{
			if(StringUtils.isAlpha(word) && word.length()>2)
				db.add(word);				
		}
        
    }

    public void updateModel(String in)
    {
        data.clear();
//lets find any items which start with the string the user typed, and add it to the popup list
//here you would usually get your items from a database, or some other storage...
        for(String s:db)
            if(s.startsWith(in))
                data.add(s);

        super.fireContentsChanged(this, 0, data.size());

//this is a hack to get around redraw problems when changing the list length of the displayed popups
        cb.hidePopup();
        cb.showPopup();
        if(data.size() != 0)
            cb.setSelectedIndex(0);
    }
    public void updateModel(String in, ArrayList<String> closeWords)
    {
        data.clear();

        for(String s:closeWords)
            if(s.startsWith(in))
                data.add(s);

        super.fireContentsChanged(this, 0, data.size());


        cb.hidePopup();
        cb.showPopup();
        if(data.size() != 0)
            cb.setSelectedIndex(0);
    }

    public int getSize(){return data.size();}
    public Object getElementAt(int index){return data.get(index);}
    public void setSelectedItem(Object anItem)
                                 {selection = (String) anItem;}
    public Object getSelectedItem(){return selection;}
    public void keyTyped(KeyEvent e){}
    public void keyPressed(KeyEvent e){
    	int id = e.getID();
    	/*
            char c = e.getKeyChar();
            if(c==' '){
            	System.out.println("CAPTURED SPACE");
            	String temp=cbe.getItem().toString();            		        		
        	
            	try {
					tempObj.setQueryVector(temp);
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
					try {
						tempObj.ComputeAssociationCluster();
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				String anotherWord=tempObj.getSecondHighestTerm(temp);
				System.out.println("another word is "+ anotherWord);
				 	temp= temp + " "  + anotherWord;
				System.out.println("complete word is "+ temp);
				cbe.setItem(temp);
           // 	updateModel(temp);
            }
    	*/
    	
    }

    public void keyReleased(KeyEvent e)
    {
        String str = cbe.getItem().toString();
        JTextField jtf = (JTextField)cbe.getEditorComponent();
        currPos = jtf.getCaretPosition();

        if(e.getKeyChar() == KeyEvent.CHAR_UNDEFINED)
        {
            if(e.getKeyCode() != KeyEvent.VK_ENTER )
            {
                cbe.setItem(str);
                jtf.setCaretPosition(currPos);
            }
        }
        else if(e.getKeyCode() == KeyEvent.VK_ENTER)
            cb.setSelectedIndex(cb.getSelectedIndex());
        else
        {
            updateModel(cb.getEditor().getItem().toString());
            cbe.setItem(str);
            jtf.setCaretPosition(currPos);
        }
    }

    public String getValCombo()
    {
    	return cbe.getItem().toString();
    }
    public void itemStateChanged(ItemEvent e)
    {
        cbe.setItem(e.getItem().toString());
        cb.setSelectedItem(e.getItem());
    }

}
