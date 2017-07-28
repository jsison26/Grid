package model;

import javax.swing.JTextArea;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.spi.LoggingEvent;

public class TextAreaAppender extends AppenderSkeleton {

	private JTextArea textArea;
	
	public void setTextArea(JTextArea textArea) {
		this.textArea = textArea;
	}
	
	@Override
	public void close() {
		// TODO Auto-generated method stub	
	}

	@Override
	public boolean requiresLayout() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	protected void append(LoggingEvent event) {
		// TODO Auto-generated method stub
		if(textArea != null)
			textArea.append(event.getMessage().toString() + "\n");
	}

}
