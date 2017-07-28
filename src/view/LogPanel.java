package view;

import java.awt.BorderLayout;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.text.DefaultCaret;

import model.TextAreaAppender;


public class LogPanel extends JPanel {
	private static final long serialVersionUID = 689026761319439703L;
	private JTextArea textArea;
	
	public LogPanel(){
		textArea = new JTextArea();
		textArea.setEditable(false);
		
		DefaultCaret caret = (DefaultCaret)textArea.getCaret();
		caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
		
		new TextAreaAppender().setTextArea(textArea);
		this.setLayout(new BorderLayout());
		this.add(new JScrollPane(textArea), BorderLayout.CENTER);
	}
	
	public void log(String logLine){
		textArea.append(logLine);
	}
}
