package view;

import java.util.EventListener;

import model.Job;


public interface JobListener extends EventListener {
	public void jobEventOccurred(Job job);
}
