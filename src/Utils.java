import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Widget;

/**
 * 
 */

/**
 * @author rolivares
 *
 */
public class Utils {

	Shell shell = null;

	public Utils(Shell shell) {
		this.shell = shell;
	}

	/****************************************************************************************************************************
		Utility Functions 
	 ****************************************************************************************************************************/

	/** (REO) Returns the value of a public member variable using reflection
	 * @param obj
	 * @param fieldName
	 * @return
	 */
	public Object getField(Object obj, String fieldName) {
		try {
			Class<?> objClass = obj.getClass();
			Field[] fields = objClass.getFields();
			for(Field field : fields) {
				String name = field.getName();
				Object value;
				value = field.get(obj);
				if (name.equals(fieldName))
					return value;
			}			
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	public static String path(String ... segments) {
		StringBuffer result = new StringBuffer(512);
		for (String segment : segments) {
			result.append(segment).append("\\");
		}
		result.deleteCharAt(result.length() - 1);
		return result.toString();
	}

	public static String linkify(String x) {
		String y = "<a href=\"\">" + x + "</a>";
		return y;
	}

	public static String delinkify(String x) {
		String y = x.replaceAll("<a href=\"\">", "");
		y = y.replaceAll("</a>", "");		
		return y;
	}

	public static void spawn(String cmd) throws IOException {
		Runtime.getRuntime().exec("cmd /k start " + cmd);
	}

	public static String exec(String cmd, String args) {
		String result = "";
		try {
			result  += cmd + " " + args;
			String cmdName = getFileName(cmd);
			String workingDir = getFileDirectory(cmd);
			File workingDirFile = new File(workingDir);
			String finalCmd = String.format("CMD /C %s %s", cmdName, args);

			Process execProcess = Runtime.getRuntime().exec(finalCmd, null, workingDirFile);

			// Get input streams connected to output of process
			BufferedReader stdInput = new BufferedReader(new InputStreamReader(execProcess.getInputStream()));
			BufferedReader stdError = new BufferedReader(new InputStreamReader(execProcess.getErrorStream()));

			// Read command standard output
			String s;
			while ((s = stdInput.readLine()) != null) {
				result += "\n" + s;
			}

			// Read command errors
			while ((s = stdError.readLine()) != null) {
				result += "\n" + s;
			}

		} catch (Exception e) {
			result += e.toString();
			e.printStackTrace(System.err);
		}

		return result;
	}

	/**
	 * @param cmd		Example: "dir" or "mkdir"
	 * @throws IOException
	 */
	public static String exec(String cmd) {
		String result = "";
		try {
			// Run "netsh" Windows command
			result  += //"================================================================\n"
					cmd;
			//+  "================================================================\n";
			Process execProcess = Runtime.getRuntime().exec("CMD /C " + cmd);

			// Get input streams connected to output of process
			BufferedReader stdInput = new BufferedReader(new InputStreamReader(execProcess.getInputStream()));
			BufferedReader stdError = new BufferedReader(new InputStreamReader(execProcess.getErrorStream()));

			// Read command standard output
			String s;
			while ((s = stdInput.readLine()) != null) {
				result += "\n" + s;
			}

			// Read command errors
			while ((s = stdError.readLine()) != null) {
				result += "\n" + s;
			}
		} catch (Exception e) {
			result += e.toString();
			e.printStackTrace(System.err);
		}
		return result;
	}

	public static String readFile(String path) throws Exception {
		File file = new File(path);
		if (!file.exists())
			throw new FileNotFoundException("File not found: " + path);
		else if (file.length() == 0)
			return "";
		Scanner scanner = new Scanner(file);
		String text = scanner.useDelimiter("\\Z").next();
		scanner.close();
		return text;
	}

	public static void writeFile(String path, String text) throws IOException {
		PrintWriter out = new PrintWriter(new FileWriter(new File(path)));  
		out.print(text);  
		out.close();  
	}

	public static void dispose(Control parent) {
		if (parent instanceof Composite) {
			Control[] children = ((Composite)parent).getChildren();
			for (Control child : children) {
				dispose(child);
			}
		}
		//parent.setVisible(false);
		if (!parent.isDisposed())
			parent.dispose();
	}

	public static String getFileName(String path) {
		String result = path == null ? null : path.replaceAll(".*\\\\", "");
		return result;
	}

	public static String getFileDirectory(String path) {
		if (path == null)
			return null;
		int li = path.lastIndexOf("\\");
		if (li == -1)
			return path;
		else 
			return path.substring(0, li);
	}

	public static String getFinalPath(String defaultFolder, String filePath) {
		String parts[] = getPathParts(defaultFolder, filePath);
		String result = parts[2];
		return result;
	}

	public static String[] getPathParts(String defaultFolder, String filePath) {
		String result[] = {defaultFolder, filePath, defaultFolder + "\\" + filePath};
		if (filePath.contains(":")) {
			// Full path specified in filePath, use that.
			result[0] = filePath.substring(0, filePath.lastIndexOf("\\")); 
			result[1] = filePath.substring(filePath.lastIndexOf("\\") + 1); 
			result[2] = result[0] + "\\" + result[1]; 
		} else {
			// Otherwise, use the path from defaultFolder
		}
		try {
			result[2] = result[2].replaceAll("\\\\\\\\", "/");
			result[2] = result[2].replaceAll("/", "\\\\");
		} catch(Exception e) {}
		result[0] = getFileDirectory(result[2]);
		result[1] = getFileName(result[2]);
		return result;
	}

	public static String fileExistsIn(String filename, String[] dirs) {
		for (String dir : dirs) {
			if (fileExists(dir + "\\" + filename)) 
				return dir + "\\" + filename;
		}
		return null;
	}

	public static String filesAllExistIn(String[] filenames, String[] dirs) {
		for (String dir : dirs) {
			boolean allExist = true;
			for (String filename : filenames) {
				if (!fileExists(dir + "\\" + filename))
					allExist = false;
			}
			if (allExist)
				return dir;
		}
		return null;
	}
	
	public static boolean fileExists(String x) {
		if (x == null)
			return false;
		File f = new File(x);
		return f.exists();
	}
	
	public static boolean directoryExists(String x) {
		if (x == null)
			return false;
		File f = new File(x);
		return f.exists() && f.isDirectory();
	}

	public static int getEquivalentFirstLine(String oldLines[], int oldTopLineIndex, String[] newLines) {
		int newTopLineIndex = 0;
		String oldTopLine = oldLines[oldTopLineIndex];
		if (newLines[oldTopLineIndex].equals(oldTopLine)) 
			// Looks like the top line number didnt change ... easy case!
			newTopLineIndex = oldTopLineIndex;
		else
			// TODO Use the first matching line instead ... this may screw up unless we check other lines around it
			for (int i = 0; i < newLines.length; i++) {
				if (newLines[i].equals(oldTopLine)) {
					newTopLineIndex = i;
					break;
				}
			}
		// If we didnt find a match, we'll return zero...
		return newTopLineIndex;		
	}

	public String getFilenameFromDialog(String title, String startDirectory, String defaultFilename) {

		FileDialog dialog = new FileDialog (shell, SWT.SAVE);
		String [] filterNames;
		String [] filterExtensions;
		String filterPath = "/";
		String platform = SWT.getPlatform();
		if (platform.equals("win32") || platform.equals("wpf")) {
			filterNames = new String [] {"All Files (*.*)"};
			filterExtensions = new String [] {"*.*;*.txt", "*.*"};
			filterPath = startDirectory;
		} else {
			filterNames = new String [] {"All Files (*)"};
			filterExtensions = new String [] {"*.*;*.txt", "*"};
			filterPath = "/";
		}
		dialog.setFilterNames (filterNames);
		dialog.setFilterExtensions (filterExtensions);
		dialog.setFilterPath (filterPath);
		dialog.setFileName (defaultFilename);
		dialog.setText(title);
		String filenameResult = dialog.open();
		filenameResult = filenameResult == null ? "" : filenameResult; 
		return filenameResult;
	}

	public String getDirectoryFromDialog(String title, String startDirectory) {
		return getDirectoryFromDialog(title, startDirectory, shell);
	}
	
	public static String getDirectoryFromDialog(String title, String startDirectory, Shell shell) {
		DirectoryDialog dialog = new DirectoryDialog (shell, SWT.SAVE);
		
		dialog.setFilterPath (startDirectory);
		dialog.setText(title);
		String filenameResult = dialog.open();
		filenameResult = filenameResult == null ? "" : filenameResult; 
		return filenameResult;
	}

	public static String[] getDirectoryListing(String path, boolean bArrayIfDoesntExist) {
		File folder = new File(path);
		if (!folder.exists())
			return bArrayIfDoesntExist ? new String[0] : null;
			
		String[] names = folder.list();
		names = names == null ? new String[0] : names;
		if (names != null && names.length > 0)
			Arrays.sort(names);
		return names;
	}

	public static String[] getDirectoryListingSortedByLastModifiedTime(String path, boolean bArrayIfDoesntExist) {
		int i = 0;
		File folder = new File(path);
		if (!folder.exists())
			return bArrayIfDoesntExist ? new String[0] : null;
			
		// List the files in the directory
		File[] files = folder.listFiles();
		ArrayList<String> names = new ArrayList<String>(files.length);
		int stampLength = 15 + 1;
		for (File f : files) {
			names.add(String.format("%015d@%s", f.lastModified(), f.getName()));
		}
		
		// Sort by timestamp
		Collections.sort(names, Collections.reverseOrder());
		
		// Remove the prefixed long timestamp
		String[] results = new String[files.length];
		for (i = 0; i < results.length; i++)
			results[i] = names.get(i).substring(stampLength);
		
		return results;
	}
	
	public static String[] getDirectoryListing(String path) {
		return getDirectoryListing(path, false);
	}

	public static void showMessageBox(final String title, final int swtIcon, final String message) {
		Display.getDefault().asyncExec(new Runnable() {
		    public void run() {
				MessageBox messageBox = new MessageBox(Display.getDefault().getActiveShell(), swtIcon | SWT.RESIZE);
				messageBox.setText(title);
				messageBox.setMessage(message);
				messageBox.open();
		    }
		});
	}

	public static void showErrorDialog(Throwable exception) {
		showErrorDialog("Program Exception Encountered", exception.getMessage(), exception);
	}
	
	public static void showErrorDialog(final String title, final String message, final Throwable exception) {
		Display.getDefault().asyncExec(new Runnable() {
		    public void run() {
				MultiStatus exceptionStatus = createMultiStatus(exception);
		    	ErrorDialog.openError(Display.getDefault().getActiveShell(), title, message, exceptionStatus);
		    }
		});
	}
	
	public static MultiStatus createMultiStatus(Throwable t) {

		String pluginId = "ReadmeUI";
        List<Status> childStatuses = new ArrayList<Status>();
        StackTraceElement[] stackTraces = t.getStackTrace();

        for (StackTraceElement stackTrace : stackTraces) {
        	String messageLine = stackTrace.toString();
            Status status = new Status(IStatus.ERROR, pluginId, 0, messageLine, null);
            childStatuses.add(status);
        }

        MultiStatus ms = new MultiStatus(
        		pluginId, 
        		IStatus.ERROR, 
        		childStatuses.toArray(new Status[] {}),
                t.toString(), 
                t);
        return ms;		
	}
	
	/**
	 * @param title
	 * @param swtIcon
	 * @param message
	 * @return SWT.YES or SWT.NO
	 */
	public static int showMessageBoxYesNo(String title, String message) {
		MessageBox messageBox = new MessageBox(Display.getDefault().getActiveShell(), SWT.ICON_QUESTION | SWT.YES | SWT.NO);
		messageBox.setText(title);
		messageBox.setMessage(message);
		return messageBox.open();
	}		

	public static void setFileReadOnlyFlag(String path, boolean flagValue) {
		File file = new File(path);
		file.setWritable(!flagValue);
	}

	public static void writeObjectToFile(Object o, String path) throws Exception {
		FileOutputStream fileOut = new FileOutputStream(new File(path));
		ObjectOutputStream oos = new ObjectOutputStream(fileOut);
		oos.writeObject(o);
		oos.close();
	}

	@SuppressWarnings("unchecked")
	public static <T> T readObjectFromFile(String path) throws Exception {
		FileInputStream fileIn = new FileInputStream(new File(path));
		ObjectInputStream ois = new ObjectInputStream(fileIn);
		T result = (T) ois.readObject();
		ois.close();
		return result;
	}


	public static String deIndentLines(String text) {
		String[] lines = text.split("\n");
		String result = "";
		for (String line : lines) {
			if (line.startsWith("\t"))
				line = line.substring(1);
			else if (line.startsWith("    "))
				line = line.substring(4);
			else if (line.startsWith(" "))
				line = line.trim();

			result += ( result.equals("") ? "" : "\n") + line;
		}
		return result;
	}

	protected static String deleteFile(String zipFilePath) {
		String cmd = "del \"" + zipFilePath + "\"";
		return exec(cmd);
	}

	protected static String copyFile(String src, String dst) {
		String cmd = "copy /y \"" + src + "\" \"" + dst + "\"";
		return exec(cmd);
	}

	public static String getSection(String text, String section) {
		String regexStr =  "##" + section + "##([^#]*)";
		Pattern p = Pattern.compile(regexStr);
		Matcher m = p.matcher(text);
		boolean b = m.find();
		String result = b == false ? null : m.group(1);
		return result;
	}

	/**
	 * @param widget
	 * @param section	Ex: "Customer Company Name"
	 * @param text
	 * @return
	 */
	public static String setSectionParagraph(StyledText widget, String sectionHeader, String newText, boolean appendNewline) {
		if (sectionHeader == null || sectionHeader.length() == 0)
			return widget.getText();
		String text = widget.getText();
		String regex = Pattern.quote("##" + sectionHeader + "##") + "([^#]*)";
		String replaceWith = "##" + sectionHeader + "##\r\n" + newText + (appendNewline ? "\r\n" : "");
		replaceWith = Matcher.quoteReplacement(replaceWith);
		text = text.replaceFirst(regex, replaceWith);
		widget.setText(text);
		return text; 
	}

	/**
	 * @param widget
	 * @param section	Ex: "Customer Company Name"
	 * @param text
	 * @return
	 */
	public static String setSectionLine(StyledText widget, String sectionHeader, String newText) {
		if (sectionHeader == null || sectionHeader.length() == 0)
			return widget.getText();
		String text = widget.getText();
		String regex = "##" + sectionHeader + "##([^#]*)";
		String replaceWith = "##" + sectionHeader + "## " + newText + "\n";
		text = text.replaceFirst(regex, replaceWith);
		widget.setText(text);
		return text; 
	}

	public static String indentLines(String text) {
		String newText = "\t" + text.replaceAll("\n", "\n\t");
		if (newText.endsWith("\n\t"))
			newText = newText.substring(0, newText.length() - 1);
		return newText;
	}

	public static int indexOf(Widget widget, Object[] widgets) {
		for (int i = 0; i < widgets.length; i++) {
			if (widgets[i] == widget)
				return i;
		}
		return -1;
	}

	public static File createNewFile(String path) throws IOException {
		File file = new File(path);
		boolean created = file.createNewFile();
		if (!created)
			writeFile(path, "");
		return file;
	}

	public static String ensureDirectoryExists(String path)  {
		try {
			Files.createDirectories(Paths.get(path));
			return "Ensured directory exists: " + path;
		} catch (IOException e) {
			return "Could not create/check directory : " + path;
		}
	}

	public static void copyFileToClipboard(String outputPath) {

		File file = new File(outputPath);
		List<File> listOfFiles = new ArrayList<File>();
		listOfFiles.add(file);

		FileTransferable fileListObject = new FileTransferable(listOfFiles);
		ClipboardOwner clipboardOwner = new ClipboardOwner() {
			@Override
			public void lostOwnership(Clipboard clipboard, Transferable contents) {
				System.out.println("ReadmeUI lost ownership of clipboard contents. Copied output file no longer on clipboard.");
			}
		};

		Toolkit.getDefaultToolkit().getSystemClipboard().setContents(fileListObject, clipboardOwner); 
	}

	public static class FileTransferable implements Transferable {

		private List<File> listOfFiles;

		public FileTransferable(List<File> listOfFiles) {
			this.listOfFiles = listOfFiles;
		}

		@Override
		public DataFlavor[] getTransferDataFlavors() {
			return new DataFlavor[]{DataFlavor.javaFileListFlavor};
		}

		@Override
		public boolean isDataFlavorSupported(DataFlavor flavor) {
			return DataFlavor.javaFileListFlavor.equals(flavor);
		}

		@Override
		public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
			return listOfFiles;
		}
	}

	public static String createTemporaryFile(String fileName) {
		try {
			String tempDir = System.getProperty("java.io.tmpdir");
			String path = tempDir + "\\" + fileName;
			ReadmeUI.log("Creating temporary file:  " + path);
			createNewFile(path);
			return path;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		} 
	}


	public static String copyDirectory(String srcDir, String dstDir) {
		String cmd = String.format("xcopy /Y /F /E \"%s\" \"%s\"", srcDir, dstDir + "\\");
		return exec(cmd);
	}

	public static String copyDirectoryIfNotTheSame(String srcDir, String dstDir) {
		if (srcDir.equalsIgnoreCase(dstDir)) {
			return String.format("Not Copying Directory:  '%s' and '%s' are the same.", srcDir, dstDir);
		} else {
			return copyDirectory(srcDir, dstDir);
		}
	}

	public static String getJarUpdateCommand(String jarPath, String filesRootDir, String filesSubDir) {
		String result = String.format("uvf \"%s\" -C \"%s\" %s\\", jarPath, filesRootDir, filesSubDir);;
		return result;
	}

	public static String deleteDirectoryIfItExists(String dir) {
		if (fileExists(dir))
			return exec(String.format("rmdir /s /q \"%s\"", dir));
		else
			return String.format("Not deleteing directory '%s':  it doesn't exist", dir);
	}

	public String verifyFileExistsOrPrompt(String filePath, String prompt, boolean throwExceptionOnCancel) throws Exception {
		return verifyFileExistsOrPrompt(filePath, prompt, throwExceptionOnCancel, shell);
	}

	public static String verifyFileExistsOrPrompt(String filePath, String prompt, boolean throwExceptionOnCancel, Shell shell) throws Exception {
		if (!fileExists(filePath)) {
			 String newPath = getDirectoryFromDialog(prompt, filePath, shell);
			 if (newPath == null) {
				 if (throwExceptionOnCancel) 
					 throw new Exception("File does not exist:  " + filePath);
			 }
			 return newPath;
		}
		return filePath;
	}
	
	public static void spawnExplorerForDirectory(String dir) throws Exception {
		// Spawn browser to look at the local hotfix directory
		Utils.spawn("explorer \"" + dir + "\"");
	}

	public static List<String> getJarFilePathsFromSectionText(String jarFilesList) {
		if (jarFilesList != null) {
			String[] files = jarFilesList.split("\n");
			List<String> retval = new ArrayList<String>(Arrays.asList(files));
			return retval;
		} else {
			return new ArrayList<String>(0);
		}
	}

	public static String cleanNewLinesAndTabsFromFileString(String files) {
		String result = files.trim().replace("\t", ""); 	// remove tabs
		result = result.replace("\r", "");					// remove carriage returns
		return result;
	}

	public static long getFileSize(String filePath) {
		return new File(filePath).length();
	}

	public static String join(String[] strings, String sep) {
		StringBuffer b = new StringBuffer();
		String last = strings.length > 0 ? strings[strings.length - 1] : null;
		for (String s : strings)
			b.append(s).append(sep != last ? sep : "");
		return b.toString();
	}

	public static void runInUI(Runnable codeSnippet) {
		try {
			Display.getDefault().asyncExec(codeSnippet);
		} catch(Throwable t) {
			showErrorDialog(t);
		}
	}
	
	public static void removeAllChildren(Composite composite) {
		for (Control child : composite.getChildren()) {
			child.dispose();
	    }		
	}

	private static HashMap<String, Object> lastValueMap = new HashMap<String, Object>();
	
	/**
	 * @param key			The key to the category of strings to check in cache - e.g. "Configuration" or "Templates"
	 * @param latestValue	The latest set of strings for the category
	 * @return				True if any of the strings have changed across the previous call
	 */
	public static boolean differsFromLast(String key, String[] latestValue) {
		boolean keyExisted = lastValueMap.containsKey(key);
		
		String[] lastValue = (String[])lastValueMap.get(key);
		lastValueMap.put(key, latestValue);
		
		boolean bothNull = latestValue == null && lastValue == null;
		boolean oneNull = (latestValue == null && lastValue != null) || (latestValue != null && lastValue == null);
		
		if (!keyExisted)
			return true;
		if (bothNull)
			return false;
		if (oneNull)
			return true;
		
		boolean lengthMismatch = lastValue.length != latestValue.length;
		if (lengthMismatch) 
			return true;
		
		for (int i = 0; i < latestValue.length; i++) {
			boolean bStringsDiffer = !("" + lastValue[i]).equals(latestValue[i]);
			if (bStringsDiffer)
				return true;
		}
		
		return false;
	}

}
