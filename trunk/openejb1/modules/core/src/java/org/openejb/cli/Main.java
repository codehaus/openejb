package org.openejb.cli;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.JarURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Properties;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.openejb.loader.SystemClassPath;

/**
 * Entry point for ALL things OpenEJB.  This will use the new service 
 * architecture explained here:
 * 
 * @link http://docs.codehaus.org/display/OPENEJB/Executables
 *
 */
public class Main {
	private static CommandFinder finder = null;
	private static String basePath = "META-INF/org.openejb.cli/";
	private static String locale = "";
	private static String descriptionBase = "description";
	
	public static void init() {
		finder = new CommandFinder(basePath);
		locale = Locale.getDefault().getLanguage();
		
		setupClasspath();
	}
	
	public static void setupClasspath() {
		ClassLoader current = Thread.currentThread().getContextClassLoader();
		URL classURL = Thread.currentThread().getContextClassLoader().getResource(basePath + "start");
        String propsString = classURL.getFile();
        URL jarURL = null;
        File jarFile = null;
        
        propsString = propsString.substring(0, propsString.indexOf("!"));
        
        try {
			jarURL = new URL(propsString);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
        jarFile = new File(jarURL.getFile());
        
        if (jarFile.getName().indexOf("openejb-core") > -1) {
        	File lib = jarFile.getParentFile();
        	File home = lib.getParentFile();
        	
        	System.setProperty("openejb.home", home.getAbsolutePath());
        }
		
		File lib = new File(System.getProperty("openejb.home") + File.separator + "lib");
		SystemClassPath systemCP = new SystemClassPath();
		
		try {
			systemCP.addJarsToPath(lib);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		ArrayList argsList = new ArrayList();
		
		for (int i = 0; i < args.length; i++) {
			if (args[i].indexOf("-D") == -1) {
				argsList.add(args[i]);
			} else {
				String prop = args[i].substring(args[i].indexOf("-D") + 2, args[i].indexOf("="));
				String val = args[i].substring(args[i].indexOf("=") + 1);
				
				System.setProperty(prop, val);
			}
		}
		
		args = (String[])argsList.toArray(new String[argsList.size()]);
		
		init();
		
		if (args.length > 0) {
			Properties props = null;
			
			if (args[0].equals("--help")) {
				System.out.println("Usage: openejb help [command]");
				
				printAvailableCommands();
			} else {
				String mainClass = null;
				Class clazz = null;
				boolean help = false;
				
				if (args[0].equals("help")) {
					if (args.length < 2) {
						printAvailableCommands();
					}
					
					try {
						props = finder.doFindCommandProperies(args[1]);
					} catch (IOException e1) {
						System.out.println("Unavailable command: " + args[1]);
						
						printAvailableCommands();
					}
					
					help = true;
				}
				
				if (props != null) {
					mainClass = props.getProperty("main.class");
				}
				
				try {
					clazz = Thread.currentThread().getContextClassLoader().loadClass(mainClass);
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}
				
				Method mainMethod = null;
				
				try {
					mainMethod = clazz.getMethod("main", new Class[]{String[].class});
				} catch (SecurityException e) {
					e.printStackTrace();
				} catch (NoSuchMethodException e) {
					e.printStackTrace();
				}
				
				argsList = new ArrayList();
				int startPoint = 1;
				
				if (help) {
					startPoint = 2;
					
					argsList.add("--help");
				}
				
				
				for (int i = startPoint; i < args.length; i++) {
					argsList.add(args[i]);
				}
				
				args = (String[])argsList.toArray(new String[argsList.size()]);
				
				try {
					mainMethod.invoke(clazz, new Object[] { args });
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					e.printStackTrace();
				}
			}
		} else {
			System.out.println("Usage: openejb command [command-options-and-arguments]\n");
			
			printAvailableCommands();
		}
	}

	private static void printAvailableCommands() {
		System.out.println("COMMANDS:");
		
		try {
			Enumeration commandHomes = finder.doFindCommands();
			
			if (commandHomes != null) {
				for (; commandHomes.hasMoreElements(); ) {
					URL cHomeURL = (URL)commandHomes.nextElement();
					JarURLConnection conn = (JarURLConnection)cHomeURL.openConnection();
			        JarFile jarfile = conn.getJarFile();
			        Enumeration commands = jarfile.entries();
			        
			        if (commands != null) {
			        	while (commands.hasMoreElements()) {
			        		JarEntry je = (JarEntry)commands.nextElement();
				        	
				        	if (je.getName().indexOf(basePath) > -1 && !je.getName().equals(basePath) && !je.getName().endsWith(".help") && !je.getName().endsWith(".examples")) {
				        		Properties props = finder.doFindCommandProperies(je.getName().substring(je.getName().lastIndexOf("/") + 1));
				        		 
								String key = locale.equals("en") ? descriptionBase : descriptionBase + "." + locale;
								
								System.out.println("\n  " + props.getProperty("name") + " - " + props.getProperty(key));
				        	}
			        	}
			        }
				}
			} else {
				System.out.println("No available commands!");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		System.out.println("\nTry 'openejb help <command>' for more information about the command.\n");
		System.out.println("OpenEJB -- EJB Container System and EJB Server.");
		System.out.println("For updates and additional information, visit");
		System.out.println("http://www.openejb.org\n");
		System.out.println("Bug Reports to <user@openejb.org>");

    System.exit(0);
	}
}
