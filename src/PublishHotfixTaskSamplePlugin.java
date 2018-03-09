import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.TreeMap;
import java.util.regex.Matcher;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

/**
 * @author ROBERTO.OLIVARES
 * 
 * A Sample Hotfix Publishing Plugin 
 *
 */
public class PublishHotfixTaskSamplePlugin implements IRunnableWithProgress {

	// Widgets on the main window
	Shell parentWindow = null;
	StyledText configurationText = null;
	StyledText outputText = null;

	// Progress Monitor interface
	IProgressMonitor progress = null;
	
	// Parameters from our caller
	TreeMap<String, Object> parameters = new TreeMap<String, Object>(String.CASE_INSENSITIVE_ORDER);
	
	// List of successes and failures
	private ArrayList<String> successes = new ArrayList<String>();
	private ArrayList<String> failures = new ArrayList<String>();
	
	// Flag of whether the exception we've encountered is really just a hard failure
	private boolean exitDueToFailure = false;
	
	// constructor
	public PublishHotfixTaskSamplePlugin(Shell parentShell, StyledText configTextbox, StyledText outputTextbox, Object[] params) {
		this.parentWindow = parentShell;
		configurationText = configTextbox;
		outputText = outputTextbox;
		for (int i = 0; i < params.length; i += 2)
			parameters.put((String)params[i], params[i+1]);
	}

	/** 
	 * Shows the dialog and attempts to perform the hotfix publishing to local and remote ZIP files.
	 * 
	 * @author rolivares
	 */
	public void execute() {
		try {
			// create the progress monitor dialog
			ProgressMonitorDialog pmd = new ProgressMonitorDialog(new Shell()){
				@Override
				protected void setShellStyle(int newShellStyle) {           
					super.setShellStyle(SWT.CLOSE | SWT.BORDER | SWT.TITLE);
				}  
			};
			
			// calls run() on this class
			pmd.run(true, true, this);
			
		} catch (final InvocationTargetException e) {
			MessageDialog.openError(parentWindow, "Error", e.getMessage());
			
		} catch (final InterruptedException e) {
			MessageDialog.openInformation(parentWindow, "Cancelled", e.getMessage());
		}
	}
	
	private String param(String key) {
		return (String)parameters.get(key);
	}

	// Performs the operation of publishing the hotfix (and updates the progress monitor / dialog as well)
	@Override
	public void run(IProgressMonitor progress) throws InvocationTargetException, InterruptedException {
		try {
			String hotfixName 		= param("OutputFilename").replace("Readme_", "").replace(".txt", "");
			String zipFileName 		= "Hotfix_" + hotfixName + ".zip";
			
			// Initialize progress monitor
			this.progress = progress;
			int totalProgressSteps = 8;
			progress.beginTask("Publishing " + zipFileName, totalProgressSteps);
			
			// Compute all the paths
			subtask("Preparing parameters for publishing hotfix...");
			
				String localHotfixDir 	= "C:\\Tools\\(Hotfixes)" + "\\" + hotfixName; // Prefs.getHotfixDirectory() +  
				String localZipFilePath 	= localHotfixDir + "\\" + zipFileName;
	
				String remoteHotfixDir 	= "Z:\\(Hotfixes)\\" + hotfixName; 
				String remoteZipFilePath	= remoteHotfixDir + "\\" + zipFileName; 
	
				String localHotfixLibDir = localHotfixDir + "\\lib";
				String localHotfixLibDirInteract = localHotfixLibDir + "\\interact-war\\web-inf\\lib";
				String localHotfixLibDirCampaign = localHotfixLibDir + "\\campaign-war\\web-inf\\lib";
				
				String srcReadme 		= param("OutputPath");
				String dstReadme 		= localHotfixDir + "\\" + param("OutputFilename");

				String publishParameters = String.format("%s\n%s\n%s\n%s\n%s\n%s\n%s\n%s", hotfixName, localHotfixDir, srcReadme, dstReadme, zipFileName, localZipFilePath, remoteHotfixDir, remoteZipFilePath);
				
				log(publishParameters);

				// Fail if no configuration file
				if (isNullOrSizeIsSmallerThan(param("ConfigurationText"), 10))
					fail("Configuration file text is blank or less than 10 characters.");
				
			subtask("Computing list of CLASS files to publish...");
			
				// Extract list of JAVA files and location of the eclipse bin directory from the developer section in the configuration
				HashMap<String, String> result = getJavaFilesListAndEclipseBinDirectoryFromConfigSection();
				String javaFilesList = result.get("javaFilesList");
				String jarFilesList = result.get("jarFilesList");
				String eclipseBinDirectory = result.get("eclipseBinDirectory");

				// Fail if no files to package
				if (isNullOrSizeIsSmallerThan(javaFilesList, 10))
					fail("Developer java files section is blank or less than 10 characters.");
				
				// Generate list of JAR files -- typically pulled in from a local build
				List<String> srcJarFiles = Utils.getJarFilePathsFromSectionText(jarFilesList);
	
				// Generate list of CLASS files from the list of JAVA files and the eclipse bin directory
				List<String> srcClassFiles = getClassFilePathsFromJavaFilesSectionText(javaFilesList, eclipseBinDirectory);
	
			subtask("Updating Configuration and Output files...");
				
				// Update class file list in the config file and regenerate the output file
				updateConfigAndOutputFiles(srcClassFiles);

			subtask("Downloading customer JARs from remote storage to staging directory...");
			
				// Stage all *.class hotfix files locally (pull from the remote directory)
				stageHotfixFiles(localHotfixDir, remoteHotfixDir, srcClassFiles, srcReadme, dstReadme);
	
			subtask("Copying local developer .CLASS files to staging directory...");
			
				// Stage all *.jar hotfix files locally (pull from specified directory in 
				copyDeveloperJarsToHotfixStagingDirectory(srcJarFiles, localHotfixLibDirInteract, localHotfixLibDirCampaign);

			subtask("Zipping staging directory...");
				
				// Generate the ZIP file and upload it to the remote dir
				zipHotfixDirectory(localHotfixDir, localZipFilePath);

			subtask("Copying ZIPed file to remote storage...");

				// Copy the local .ZIP file to Z:\(Hotfixes)\
				copyZipToRemoteDirectory(localZipFilePath, remoteZipFilePath);
				
			subtask("Generating report...");
			
				// Spawn browser to look at the local hotfix directory
				Utils.spawnExplorerForDirectory(localHotfixDir);
				
				// Double check everything went well
				if (Utils.fileExists(localZipFilePath))
					addSuccess(String.format(	"[LOCAL] %s exists [%d bytes]", localZipFilePath, Utils.getFileSize(localZipFilePath)));
				else
					addFail(String.format(		"[LOCAL] %s not created.", localZipFilePath));
				
				if (Utils.fileExists(remoteZipFilePath))	
					addSuccess(String.format(	"[REMOTE] %s exists [%d bytes]", remoteZipFilePath, Utils.getFileSize(remoteZipFilePath)));
				else
					addFail(String.format(		"[REMOTE] %s not created.", remoteZipFilePath));
				
				// list contents of ZIP file: modified time, path, size, 
				
								
		} catch (Exception e) {
			if (!exitDueToFailure)
				showException("Error Publishing Hotfix", e);
			
		} finally {
			progress.done();
		}
	}
	
	private boolean isNullOrSizeIsSmallerThan(String param, int i) {
		return param == null || param.length() < i;
	}

	private void updateConfigAndOutputFiles(final List<String> srcClassFiles) throws IOException, Exception {

		final Exception[] ex = new Exception[1];
		
		// Do within the UI thread to update GUI widgets
		Display.getDefault().asyncExec(new Runnable() {
		    public void run()  {
		    	try {
					// Turn list of class files into the hotfix WAR section for the admin to read
					String newFilesSectionContents = getComputedHotfixFilesSection(srcClassFiles);
			
					// Update the "Files Included In This Hotfix" section in the configuration 
					String filesIncludedInThisHotfixTag = "Files Included In This Hotfix";
					Utils.setSectionParagraph(configurationText, filesIncludedInThisHotfixTag, newFilesSectionContents, false);
					log("Computed 'New Hotfix Files' section for Config:\n" + newFilesSectionContents);
			
					// Save update configuration file
					String updatedConfigurationText = configurationText.getText();
					configurationText.setText(updatedConfigurationText);
					Utils.writeFile(param("ConfigurationPath"), updatedConfigurationText);
			
					// Regenerate the readme outputfile
					log("Generating: readme.processFiles(" + param("TemplatePath") + ", " + param("ConfigurationPath") + ", " + param("OutputPath") + ")");
					ReadmeGenerator.processFiles(param("TemplatePath"), param("ConfigurationPath"), param("OutputPath"), "", 4);
					String output = Utils.readFile(param("OutputPath"));
			
					// Try to use either the exact line index or the first match to that as new scroll target so we dont jump back to the beginning
					int newTopLineIndex = Utils.getEquivalentFirstLine(outputText.getText().split("\n"), outputText.getTopIndex(), output.split("\n"));
					outputText.setText(output);
					outputText.setTopIndex(newTopLineIndex);
					
		    	} catch (Exception e) {
		    		ex[0] = e;
		    	}
		    }
		});
		
		if (ex[0] != null)
			throw ex[0];
	}

	private void stageHotfixFiles(String localHotfixDir, String remoteHotfixDir, List<String> srcClassFiles, String srcReadme, String dstReadme) throws Exception {

		String remoteBaselineJarsDir = remoteHotfixDir + "\\customer-baseline-jars"; 

		String localComDir = localHotfixDir + "\\com";

		String localBaselineJarsDir = localHotfixDir + "\\customer-baseline-jars"; 
		String localHotfixedJarsDir = localHotfixDir + "\\final-hotfixed-jars";

		String localHotfixedCampaignJar = localHotfixedJarsDir + "\\Campaign.jar"; 
		String localHotfixedInteractJar = localHotfixedJarsDir + "\\interact.jar"; 

		String jarExe = Utils.verifyFileExistsOrPrompt(param("JarExePath"), "Please locate jar.exe", true, parentWindow);

		// Make sure we can remove all the old class files
		try {
			// Create local C:\(Hotfixes)\<hotfixname> if it doesn't already exist
			log(Utils.ensureDirectoryExists(localHotfixDir));
			log(Utils.deleteDirectoryIfItExists(localComDir));
			log(Utils.ensureDirectoryExists(localComDir));

		} catch (Exception e) {
			throw new Exception("Could not delete/remake the 'com' subdirectory of the hotfix directory:\n\n   " + localHotfixDir + "\n\nCan't copy class files.\n\nIt may still be open in a file explorer window.");
		}

		// Copy the local hotfix binaries to their new location in local C:\(hotfixes)\<hotfixname>\com\... 
		log(copyClassFilesToHotfixStagingDirectory(srcClassFiles, localHotfixDir));

		// Copy the local readme.txt file to the local hotfix directory as well
		log(Utils.copyFile(srcReadme, dstReadme));

		// Copy any other library files to be included


		// If a "/Customer-Baseline-Jars" directory exists, copy that over and apply the hotfix to it.
		if (Utils.fileExists(remoteBaselineJarsDir)) {
			log("Found remote 'customer-baseline-jars' directory... copying it down and patching it with class files to 'final-hotfixed-jars' directory");

			// Copy remote baseline to local customer-baseline-jars and local final-hotfixed-jars
			log(Utils.deleteDirectoryIfItExists(localBaselineJarsDir));
			log(Utils.copyDirectoryIfNotTheSame(remoteBaselineJarsDir, localBaselineJarsDir));

			log(Utils.deleteDirectoryIfItExists(localHotfixedJarsDir));
			log(Utils.copyDirectory(localBaselineJarsDir, localHotfixedJarsDir));

			// JAR the Interact class files into the final jar files
			if (Utils.fileExists(localHotfixedInteractJar)) {
				String jarArgs = Utils.getJarUpdateCommand(localHotfixedInteractJar, localHotfixDir, "com\\unicacorp\\interact"); 
				log(Utils.exec(jarExe, jarArgs));
			}

			// JAR the Campaign class files into the final jar files
			if (Utils.fileExists(localHotfixedCampaignJar)) {
				String jarArgs = Utils.getJarUpdateCommand(localHotfixedInteractJar, localHotfixDir, "com\\unicacorp\\Campaign"); 
				log(Utils.exec(jarExe, jarArgs));
			}
		}
	}

	private void zipHotfixDirectory(String localHotfixDir, String localZipFilePath) throws Exception {

		String zipExe = Utils.verifyFileExistsOrPrompt(param("SevenZipExePath"), "Please locate 7zip.exe", true, parentWindow);
		String zipArgs = " a -r -x!*.zip \"" + localZipFilePath + "\"" + " \"" + localHotfixDir + "\\*\"";

		// Zip local C:\(hotfixes)\<hotfixname>\*.* to C:\(hotfixes)\<hotfixname>\<hotfixname>.zip
		log(Utils.exec(zipExe, zipArgs));
	}

	private void copyZipToRemoteDirectory(String localZipFilePath, String remoteZipFilePath) {
		String remoteHotfixDir = Utils.getFileDirectory(remoteZipFilePath);
		// Copy local hotfix ZIP file to the remote hotfix directory: Z:\(hotfixes)
		log(Utils.ensureDirectoryExists(remoteHotfixDir));
		log(Utils.copyFile(localZipFilePath, remoteZipFilePath));
	}

	private void copyDeveloperJarsToHotfixStagingDirectory(List<String> srcJarFiles, String localHotfixLibDirInteract, String localHotfixLibDirCampaign) {
		if (srcJarFiles.size() > 0) {
			log(Utils.ensureDirectoryExists(localHotfixLibDirInteract));
			log(Utils.ensureDirectoryExists(localHotfixLibDirCampaign));
			for (String src : srcJarFiles) {
				boolean isInteractLib = src.contains("\\interact");
				if (isInteractLib)
					log(Utils.copyFile(src, localHotfixLibDirInteract));
				else
					log(Utils.copyFile(src, localHotfixLibDirCampaign));
			}
		}
	}

	private HashMap<String, String> getJavaFilesListAndEclipseBinDirectoryFromConfigSection() throws Exception {
		HashMap<String, String> result = new HashMap<String, String>();
		
		// Get configuration txt file
		String configText = param("configurationText");
		
		// Get list of java source files from the readme config
		String javaFilesSection = Utils.getSection(configText, ReadmeUI.developerJavaFilesTag);
		log("Java Files Section:\n" + javaFilesSection);
	
		// If no java source files provided, just exit.
		if (javaFilesSection != null && javaFilesSection.length() > 2) {
			// Convert from list of Java source files to to a list of class files to copy
			javaFilesSection = Utils.cleanNewLinesAndTabsFromFileString(javaFilesSection);					
	
			// Write the eclipse bin directory (first line) and the list of class files (following lines) to the results
			int eol = javaFilesSection.indexOf("\n");
			result.put("eclipseBinDirectory", 	javaFilesSection.substring(0, eol));
			result.put("javaFilesList", 		javaFilesSection.substring(eol + 1));
	
		} else {
			result.put("eclipseBinDirectory", null);
			result.put("javaFilesList", null);
			log("The " + ReadmeUI.developerJavaFilesTag + " section did not contain any .java source file paths.");
		}
		
		// Get list of jar libraries to include from the readme config
		String jarFilesSection =  Utils.getSection(configText, ReadmeUI.developerLibraryFilesTag);
		log("Developer Libaries Section:\n" + jarFilesSection);
		if (jarFilesSection != null) {
			jarFilesSection = Utils.cleanNewLinesAndTabsFromFileString(jarFilesSection);
		}
		
		// Write list of jar files to the result
		result.put("jarFilesList", jarFilesSection);
		
		return result;
	}
	
	private void fail(String msg) throws Exception {
		addFail(msg);
		exitDueToFailure = true;		
		throw new Exception(msg);
	}
	
	private void addSuccess(String msg) {
		successes.add(msg);
	}

	private void addFail(String msg) {
		failures.add(msg);
	}

	public String getFailures() {
		String result = "";
		for (String f : failures)
			result += f + "\n";
		return failures.size() > 0 ? result : null;
	}
	
	private void subtask(String title) {
		progress.worked(1);
		progress.subTask(title);
		log(title);
	}
	
	private void log (String s) {
		ReadmeUI.log(s);
	}

	private void showException(String title, Exception e) {
		ReadmeUI.showErrorDialog(title, e);
	}

/* ====================================================================================================================================
 * UTILITY METHODS
 * ==================================================================================================================================== */
	public static ArrayList<String> getChildClassFiles(String classPath) {
		ArrayList<String> results = new ArrayList<String>();
		String fileName = Utils.getFileName(classPath.trim());
		String folderPath = Utils.getFileDirectory(classPath.trim());
		final String filter = fileName.replace(".class", "$");
		File folder = new File(folderPath);
		if (!folder.exists())
			return results;				
		String[] names = folder.list(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.startsWith(filter);
			}
		});
		results.addAll(Arrays.asList(names));
		for (int i = 0; i < results.size(); i++) {
			results.set(i, folderPath + "\\" + results.get(i));
		}
		return results;
	}

	public static List<String> getClassFilePathsFromJavaFilesSectionText(String javaFilesSection, String eclipseBinDirectory) {
		List<String> results = new ArrayList<String>(); 
		if (javaFilesSection == null)
			return results;
		try {
			// Replace Java source file paths with Java binary file paths
			String regex = "C.*(\\\\com\\\\)"; 													// Find C:\...\com\
			String replaceWith = Matcher.quoteReplacement(eclipseBinDirectory + "\\com\\");		// Replace with C:\...\bin\com\
			String text = javaFilesSection.replaceAll(regex, replaceWith);
			String classFiles = text.replaceAll("\\.java", "\\.class");							// Turn .java into .class

			// Scan file system for child class files, returning all the suspected source class file paths.
			String[] classFilePaths = classFiles.split("\n");
			results = getAllRelatedClassFiles(classFilePaths);
		} catch (Exception e) {
		}
		return results;
	}

	public static List<String> getAllRelatedClassFiles(String[] filePaths) {
		// List of binary files included in this hotfix
		HashSet<String> classFilePaths = new HashSet<String>(Arrays.asList(filePaths));
		ArrayList<String> childClassFiles = new ArrayList<String>();
		ArrayList<String> finalPathsToUse = new ArrayList<String>();

		// See if there are any classname$1.class files for this class and add them to the child list.
		for (String classPath : classFilePaths) {
			childClassFiles.addAll(getChildClassFiles(classPath));
		}

		// Add all the children to the classFilePaths hashset to remove duplicates
		classFilePaths.addAll(childClassFiles);

		// Convert the hashset to an arraylist to sort it
		finalPathsToUse.addAll(classFilePaths);
		Collections.sort(finalPathsToUse);

		// Return the sorted arraylist of file paths
		return finalPathsToUse;
	}

	public static String getComputedHotfixFilesSection(List<String> classFiles) {

		String campaignJarClassesHdr = "    <Campaign.war>/WEB-INF/lib/<Campaign.jar>/\r\n";
		String interactJarClassesHdr = "    <InteractRT.war>/WEB-INF/lib/<Interact.jar>/\r\n";
		String campaignJarClasses = "";
		String interactJarClasses = "";

		// Add each truncated file path under Campaign.jar or Interact.jar sections
		for (String classPath : classFiles) {
			String pathInJar = classPath.replaceAll(".*com", "com").replace("\\", "/");
			boolean isInteract = !pathInJar.contains("/Campaign/");
			if (isInteract)
				interactJarClasses += String.format("        %s\r\n", pathInJar);
			else
				campaignJarClasses += String.format("        %s\r\n", pathInJar);
		}

		campaignJarClasses += "".equals(campaignJarClasses) ? "        [None]\r\n" : "";
		interactJarClasses += "".equals(interactJarClasses) ? "        [None]\r\n" : "";

		String newFilesSectionContents = String.format("%s%s\r\n%s%s", campaignJarClassesHdr, campaignJarClasses, interactJarClassesHdr, interactJarClasses); 
		return newFilesSectionContents;
	}

	public static String copyClassFilesToHotfixStagingDirectory(List<String> srcClassFiles, String hotfixDirectory) {
		String results = "";
		for (String srcClassPath : srcClassFiles) {
			// Get the realtive "com\...\xyz.class" part of the path 
			int relativePathStart = srcClassPath.indexOf("bin\\");
			String relativePath = srcClassPath.substring(relativePathStart + 4);

			// Tack it onto the hotfix staging directory to get the destination path:  
			String dstClassPath = hotfixDirectory + "\\" + relativePath;
			File dstClass = new File(dstClassPath);
			String dstClassDirectory = dstClass.getParent();

			// Ensure Z:\(hotfixes)\<hotfixname>\com\... directory exists and copy the CLASS file to it.
			// Copy the hotfix binaries to their new location in local C:\(hotfixes)\<hotfixname>\com\... 
			results += Utils.ensureDirectoryExists(dstClassDirectory) + "\n\n";
			results += Utils.copyFile(srcClassPath, dstClassPath) + "\n\n";
		}
		return results;
	}
}
