package model;

import com.google.gson.Gson;

public class TopicMessage {
	
	private String messageType;
	private String machine;
	private String targetMachine;
	private Object object;	
	
	
	public TopicMessage(){
		
	}
	
	public TopicMessage(String gsonString) {
		Gson gson = new Gson();
		setMessageType(((TopicMessage) gson.fromJson(gsonString, TopicMessage.class)).getMessageType());
		setMachine(((TopicMessage) gson.fromJson(gsonString, TopicMessage.class)).getMachine());
		setTargetMachine(((TopicMessage) gson.fromJson(gsonString, TopicMessage.class)).getTargetMachine());
		setObject(((TopicMessage) gson.fromJson(gsonString, TopicMessage.class)).getObject());
	}
		
	public void setMessageType(String messageType) {
		this.messageType = messageType;
	}
	
	public void setMachine(String machine) {
		this.machine = machine;
	}
	
	public void setTargetMachine(String targetMachine) {
		this.targetMachine = targetMachine;
	}
	
	public void setObject(Object object) {
		this.object = object;
	}
	
	public String getMessageType() {
		return messageType;
	}
	
	public String getMachine() {
		return machine;
	}
	
	public String getTargetMachine() {
		return targetMachine;
	}
	
	public Object getObject() {
		return object;
	}
	
	public String toString() {
		return new Gson().toJson(this);
	}
}
