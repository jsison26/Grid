package controller;

import java.io.File;
import java.io.FileFilter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Path;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class Utilities {
	
	private static final Logger logger = LoggerFactory.getLogger(Utilities.class);
	
	public static boolean tryParseInt(String value) {  
		try {  
			Integer.parseInt(value);  
		    return true;  
		} 
		catch (NumberFormatException e) {  
		    return false;  
		}
	}
	
	public static String getStackTrace(final Throwable throwable) {
	     final StringWriter sw = new StringWriter();
	     final PrintWriter pw = new PrintWriter(sw, true);
	     throwable.printStackTrace(pw);
	     return sw.getBuffer().toString();
	}
	
	public static File[] listFilesMatching(File root, String regex) {
		if(!root.isDirectory()) {
	        throw new IllegalArgumentException(root+" is no directory.");
	    }
	    
		final Pattern p = Pattern.compile(regex); // careful: could also throw an exception!
	    return root.listFiles(new FileFilter(){
	        @Override
	        public boolean accept(File file) {
	            return p.matcher(file.getName()).matches();
	        }
	    });
	}
	
	public static String getAppConfigArg(String[] args){
		for(String arg: args){
			if(arg.startsWith("-config="))
				return arg.substring(arg.indexOf('=')+1);
		}
		
		return "app.properties";
	}
	
	public static void deleteFiles(Path directory, String fileRegex) {
		File[] files = listFilesMatching(new File(directory.toString()), fileRegex);
		logger.info("files count={}", files.length);
		for(File file: files) {
			file.delete();
			logger.info("{} deleted.", file.getAbsolutePath());
		}
	}
}
