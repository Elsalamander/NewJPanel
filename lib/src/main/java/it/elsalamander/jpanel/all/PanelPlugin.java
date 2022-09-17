package it.elsalamander.jpanel.all;

import static spark.Spark.awaitStop;
import static spark.Spark.externalStaticFileLocation;
import static spark.Spark.port;
import static spark.Spark.secure;
import static spark.Spark.webSocket;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.github.jknack.handlebars.internal.Files;
import com.vaadin.sass.internal.ScssContext;
import com.vaadin.sass.internal.ScssStylesheet;
import com.vaadin.sass.internal.handler.SCSSDocumentHandlerImpl;
import com.vaadin.sass.internal.handler.SCSSErrorHandler;

import it.elsalamander.jpanel.all.Utils.Lag;
import it.elsalamander.jpanel.all.getters.FileGetter;
import it.elsalamander.jpanel.all.getters.LogoutPath;
import it.elsalamander.jpanel.all.getters.SimplePageGetter;
import it.elsalamander.jpanel.all.getters.StatsGetter;
import it.elsalamander.jpanel.all.getters.SwitchThemeGetter;
import it.elsalamander.jpanel.all.posters.ClientLoginPost;
import it.elsalamander.jpanel.all.posters.FileManager;
import it.elsalamander.jpanel.all.posters.FilePost;
import it.elsalamander.jpanel.all.posters.LoginPost;
import oshi.SystemInfo;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.software.os.OperatingSystem;

/*********************************************************************
 * Parte del plugin in comune con spigot e bungeecord
 * 
 * 
 * @author: Elsalamander
 * @data: 16 set 2022
 * @version: v3.1.4
 * 
 *********************************************************************/
public class PanelPlugin{
	
	private static PanelPlugin instance;
	private ServerInterface server;
	
	private boolean debugMode;
	private File resFolder;
	
	private SystemInfo si;
	private HardwareAbstractionLayer hal;
	private OperatingSystem sys;
	
	private int httpPort = 4567;
	private boolean useSsl = false;
	private String keystorePath = "";
	private String keystorePassword = "";
	
	private PanelSessions sessions;
	
	/**
	 * Costruttore della istanza generica
	 * @param server
	 */
	public PanelPlugin(ServerInterface server) {
		this.server = server;
		this.si = new SystemInfo();
		this.hal = si.getHardware();
		this.sys = si.getOperatingSystem();
		
		this.debugMode = false;
	}
	
	public static PanelPlugin getInstance(){
		return instance;
	}
	
	public void onLoad() {
		
	}
	
	public void onEnable(){
		// istanza Lag per i TICK
		Lag lag = Lag.getInstance();
		
		// sessione del pannello
		sessions = PanelSessions.getInstance();
		
		// istanza plugin
		instance = this;
		
		// Avvio task per i tick
		this.server.startTaskSync(lag);
		
		// ottieni configurazione config
		this.server.loadMyConfig();
		
		// cartella dove ci sono tutti i file per il sito
		resFolder = new File(new File(".").getAbsolutePath() + "/JPanel-public/");
		File verFile = new File(resFolder + "/ .resVersion");
		
		// se manca la cartella la crea
		this.createFolderIfAbsent(verFile);
		
		// estrai le risorse per il sito
		this.extractResources(this.server.getClass(), "public");
		
		// protocollo ssl
		if(useSsl){
			secure(keystorePath, keystorePassword, null, null);
		}
		
		// salva i file di configurazione
		this.server.saveMyConfig();
		
		// socket della classe "ConsoleSocket"
		webSocket("/socket", ConsoleSocket.class);
		
		// file per il socket
		externalStaticFileLocation(resFolder.getName() + "/");
		
		// Compila i file scss -> css
		this.compileScssToCss();
		
		// porta per il socket
		port(httpPort);
		
		//fine caricamento plugin
		this.server.getLogger().log(Level.INFO, "Caricamento Completato!");
	}
	
	public void onDisable(){
		awaitStop();
		sessions.destroy();
	}

	/**
	 * Crea le pagine
	 */
	public void createPage(){
		new SimplePageGetter("/", "index.hbs", this);
		new SimplePageGetter("/files", "file-manager.hbs", this);
		
		// text only paths
		new StatsGetter("/stats");
		new LoginPost("/login", this.server.getLogger());
		new LogoutPath("/logout");
		new ClientLoginPost("/auth", this.server.getLogger());
		new FilePost("/file/*");
		new SwitchThemeGetter("/switchtheme");
		new FileGetter("/file/*");
		new FileManager("/files/manager", this);
	}
	
	public void createNavigation() {
		//navigazione tra le pagine
		PanelNavigation nav = PanelNavigation.getInstance();
		nav.registerPath("/", "Home");
		nav.registerPath("/files", "Files");
	}
	
	/**
	 * Compila i file da Scss a Css
	 */
	private void compileScssToCss(){
		Path srcRoot = Paths.get(resFolder.toURI());
		
		File mainScss = srcRoot.resolve("main.scss").toFile();
		File darkScss = srcRoot.resolve("dark.scss").toFile();
		
		File mainCss = srcRoot.resolve("main.css").toFile();
		File darkCss = srcRoot.resolve("dark.css").toFile();
		
		try{
			if(!mainCss.exists() && compileScssFile(mainScss, mainCss.toString())) {
				this.server.getLogger().log(Level.SEVERE, "[JPanel] Error when compiling main.scss file, panel may not work!");
			}
			if(!darkCss.exists() && compileScssFile(darkScss, srcRoot.resolve("dark.css").toString())) {
				this.server.getLogger().log(Level.SEVERE, "[JPanel] Error when compiling dark.scss file, panel may not work!");
			}
		}catch(Exception e){
			this.server.getLogger().log(Level.SEVERE, "[JPanel] Error when compiling scss file, panel may not work!");
			e.printStackTrace();
		}
	}
	
	/**
	 * Crea la cartella che contiene tutti i file per il sito se manca
	 * 
	 * @param verFile
	 */
	private void createFolderIfAbsent(File verFile){
		try{
			String currVer = this.server.getVersion();
			if(!resFolder.exists()){
				resFolder.mkdir();
				resFolder.setWritable(true);
			}
			
			if(verFile.exists()){
				String version = Files.read(verFile);
				if(!version.equals(currVer) || debugMode){
					extractResources(this.server.getClass(), "public");
				}
			}else{
				verFile.createNewFile();
				extractResources(this.server.getClass(), "public");
			}
			
			PrintWriter out = new PrintWriter(verFile);
			out.print(currVer);
			out.flush();
			out.close();
		}catch(IOException e){
			e.printStackTrace();
		}
	}
	
	/**
	 * Compila il file scss
	 * @param input
	 * @param output
	 * @return
	 * @throws Exception
	 */
	public boolean compileScssFile(File input,String output) throws Exception{
		if(!input.canRead()){
			System.err.println(input.getCanonicalPath() + " could not be read!");
			return false;
		}
		String inputPath = input.getAbsolutePath();
		
		SCSSErrorHandler errorHandler = new SCSSErrorHandler(this.server.getLogger());
		errorHandler.setWarningsAreErrors(false);
		try{
			// Parse stylesheet
			ScssStylesheet scss = ScssStylesheet.get(inputPath, null, new SCSSDocumentHandlerImpl(), errorHandler);
			if(scss == null){
				this.server.getLogger().log(Level.SEVERE, "The scss file " + input + " could not be found.");
				return false;
			}
			
			// Compile scss -> css
			scss.compile(ScssContext.UrlMode.ABSOLUTE);
			
			// Write result
			Writer writer = createOutputWriter(output);
			scss.write(writer, true);
			writer.close();
		}catch(Exception e){
			throw e;
		}
		
		return !errorHandler.isErrorsDetected();
	}
	
	/**
	 * Crea il write del file dato il path
	 * 
	 * @param filename
	 * @return
	 * @throws IOException
	 */
	private Writer createOutputWriter(String filename) throws IOException{
		if(filename == null){
			return new OutputStreamWriter(System.out, "UTF-8");
		}else{
			File file = new File(filename);
			return new FileWriter(file);
		}
	}
	
	/**
	 * Debug
	 * 
	 * @param s
	 */
	public void debug(String s){
		if(debugMode){
			this.server.getLogger().log(Level.INFO, "[DEBUG]: " + s);
		}
	}
	
	/**
	 * Server Logger
	 * 
	 * @return
	 */
	public Logger getServerLogger(){
		return this.server.getLogger();
	}
	
	/**
	 * Estrai le risorse
	 * 
	 * @param pluginClass
	 * @param filePath
	 */
	public void extractResources(Class<? extends ServerInterface> pluginClass,String filePath){
		debug("Extracting resources from " + pluginClass.getName());
		
		try{
			debug("Destination: " + resFolder.getPath());
			
			if(!resFolder.exists()){
				resFolder.mkdir();
				resFolder.setWritable(true);
			}
			
			File jarFile = new File(
					pluginClass.getProtectionDomain().getCodeSource().getLocation().getPath().replace("%20", " "));
			
			if(jarFile.isFile()){
				JarFile jar;
				jar = new JarFile(jarFile);
				
				Enumeration<JarEntry> entries = jar.entries();
				while(entries.hasMoreElements()){
					String name = entries.nextElement().getName();
					if(name.startsWith(filePath + "/")){
						InputStream in = getResourceFromJar(name, pluginClass.getClassLoader());
						name = name.replace(filePath + "/", "");
						File outFile = new File(resFolder + "/" + name);
						if(name.endsWith("/")){
							debug("Creating folder: " + outFile.getPath());
							outFile.mkdirs();
						}else{
							if(outFile.isDirectory())
								continue;
							
							if((outFile.getName().equals("_vars.scss") || outFile.getName().equals("custom.scss"))
									&& outFile.exists()){
								continue;
							}
							
							debug("Creating file: " + outFile.getPath());
							
							if(!outFile.exists())
								outFile.createNewFile();
							OutputStream out = new FileOutputStream(outFile);
							byte[] buf = new byte[1024];
							int len;
							while((len = in.read(buf)) > 0){
								out.write(buf, 0, len);
							}
							out.close();
						}
						in.close();
					}
				}
				jar.close();
			}
		}catch(IOException e){
			this.server.getLogger().log(Level.SEVERE ,"Failed to copy files to the ./JPanel-public/ folder");
			this.server.getLogger().log(Level.SEVERE ,"Please report the following error!");
			e.printStackTrace();
		}
	}
	
	/**
	 * Estrai le risorse da un server Bukkit API
	 * 
	 * @param filename
	 * @param classLoader
	 * @return
	 */
	public InputStream getResourceFromJar(String filename,ClassLoader classLoader){
		if(filename == null){
			throw new IllegalArgumentException("Filename cannot be null");
		}
		
		try{
			URL url = classLoader.getResource(filename);
			
			if(url == null){
				return null;
			}
			
			URLConnection connection = url.openConnection();
			connection.setUseCaches(false);
			return connection.getInputStream();
		}catch(IOException ex){
			return null;
		}
	}
	
	
	
	/**
	 * PanelSession
	 * @return
	 */
	public PanelSessions getSession(){
		return this.sessions;
	}

	/**
	 * @return the si
	 */
	public SystemInfo getSystemInfo(){
		return si;
	}

	/**
	 * @return the hal
	 */
	public HardwareAbstractionLayer getHardwareInfo(){
		return hal;
	}

	/**
	 * @return the sys
	 */
	public OperatingSystem getOsInfo(){
		return sys;
	}

	/**
	 * @return the debugMode
	 */
	public boolean getDebugMode(){
		return debugMode;
	}

	/**
	 * @param debugMode the debugMode to set
	 */
	public void setDebugMode(boolean debugMode){
		this.debugMode = debugMode;
	}

	/**
	 * @return the resFolder
	 */
	public File getResFolder(){
		return resFolder;
	}

	/**
	 * @param resFolder the resFolder to set
	 */
	public void setResFolder(File resFolder){
		this.resFolder = resFolder;
	}

	/**
	 * @return the httpPort
	 */
	public int getHttpPort(){
		return httpPort;
	}

	/**
	 * @param httpPort the httpPort to set
	 */
	public void setHttpPort(int httpPort){
		this.httpPort = httpPort;
	}

	/**
	 * @return the useSsl
	 */
	public boolean isUseSsl(){
		return useSsl;
	}

	/**
	 * @param useSsl the useSsl to set
	 */
	public void setUseSsl(boolean useSsl){
		this.useSsl = useSsl;
	}

	/**
	 * @return the keystorePath
	 */
	public String getKeystorePath(){
		return keystorePath;
	}

	/**
	 * @param keystorePath the keystorePath to set
	 */
	public void setKeystorePath(String keystorePath){
		this.keystorePath = keystorePath;
	}

	/**
	 * @return the keystorePassword
	 */
	public String getKeystorePassword(){
		return keystorePassword;
	}

	/**
	 * @param keystorePassword the keystorePassword to set
	 */
	public void setKeystorePassword(String keystorePassword){
		this.keystorePassword = keystorePassword;
	}

	/**
	 * @return the sessions
	 */
	public PanelSessions getSessions(){
		return sessions;
	}

	/**
	 * @param sessions the sessions to set
	 */
	public void setSessions(PanelSessions sessions){
		this.sessions = sessions;
	}

	/**
	 * @return the server
	 */
	public ServerInterface getServer(){
		return server;
	}

	public PanelNavigation getNav(){
		return PanelNavigation.getInstance();
	}
}
