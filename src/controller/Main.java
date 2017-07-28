package controller;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main{
	
	private static final Logger logger =  LoggerFactory.getLogger(Main.class);
	private static Properties prop = new Properties();
	private static InputStream input = null;
	
	public static void main(String[] args) throws IOException, InterruptedException {	
		if(args.length == 0){
			logger.error("Arguments required.");
			return;
		}
		
		try{
			input = new FileInputStream(Utilities.getAppConfigArg(args));
			prop.load(input);
			
			for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
		        if ("Nimbus".equals(info.getName())) {
		            UIManager.setLookAndFeel(info.getClassName());
		            break;
		        }
		    }
		}
		catch(FileNotFoundException e){
			logger.info("File not found: {}", Utilities.getStackTrace(e));
		}
		catch(Exception e){
			logger.info("Could not load UIManager: {}", Utilities.getStackTrace(e));
		}
		
		
		if(args[0].equals("master")){
			//String logConfig = prop.getProperty("masterLogConfig").trim();
			//PropertyConfigurator.configure(logConfig);
			
			SwingUtilities.invokeLater(new Runnable(){
				public void run() {
					MasterManagerController.getInstance().start();
				}
			});
		}
		else if(args[0].equals("cluster")){
			//String logConfig = prop.getProperty("clusterLogConfig").trim();			
			//PropertyConfigurator.configure(logConfig);
			
			SwingUtilities.invokeLater(new Runnable(){				
				public void run() {
					ClusterManagerController.getInstance().start();
				}
			});
		}
		else{
			System.out.println("Invalid command.");
		}

	}	
	
}
