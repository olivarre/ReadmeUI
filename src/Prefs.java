import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;

import org.eclipse.swt.SWT;

/** 
 * The preferences object. 
 * Serializes/deserializes to hashmap. 
 * Methods directly read and write to the UI.
 * 
 * Adding a new Preference:
 * 
 * 1. Create a widget to hold the preference in the UI. (see ReadmeUI.createPreferencesExpando())
 * 2. Add getXXX and setXXX methods for the preference's value to this class (to get/set the value of each preference from/to the UI widget)
 * 		When Prefs.getXXX is called for a preference, it gets the value from the UI widget.
 * 		When Prefs.setXXX is called for a preference, it sets the value on the UI widget.
 * 3. When Prefs.load() is called after the UI is created, it will call the setXXX methods to pull values from the blob and set them on the UI.
 * 4. When Prefs.save() is called, it gets every getXXX method defined on this object and writes it to the blob.
 * 
 * @author rolivares
 */
public class Prefs {

	// Annotation class to avoid serialization
	@Target(value = ElementType.METHOD)
	@Retention(value = RetentionPolicy.RUNTIME)
	public @interface DontSerialize {}; 
	
	// Used to serialize this class
	public static HashMap <String, Object> hashmap = new HashMap<String, Object>();

	// List of most recently used files
	static ArrayList<MRUFileEntry> mrus = null;
	
	// Path to jar.exe
	private static String jarExePath = null;
	private static String sevenZipExePath = null;
	
	// ***************************** GET METHODS ***************************** 
	public static boolean getGenerateOnSave() {
		return ReadmeUI.generateOnSave.getSelection();
	}

	public static boolean getSortConfigFilesByModifiedTime() {
		return ReadmeUI.sortConfigFilesByModifiedTime.getSelection();
	}
	
	public static String getDirectory() {
		return Utils.delinkify(ReadmeUI.linkDirectory.getText()); 
	}
	
	public static String getHotfixDirectory() {
		return Utils.delinkify(ReadmeUI.linkHotfixDirectory.getText());
	}

	public static String getConfigurationPath() {
		return ReadmeUI.labelConfiguration.getText();
	}

	public static String getTemplatePath() {
		return ReadmeUI.labelTemplate.getText();
	}

	public static String getOutputPath() {
		return ReadmeUI.labelOutput.getText();
	} 

	public static String getOutputFilename() {
		return Utils.getFileName(getOutputPath());
	}

	public static String getConfigurationFileName() {
		return Utils.getFileName(getConfigurationPath());
	}

	public static String getTemplateFileName() {
		return Utils.getFileName(getTemplatePath());
	}

	public static String getSevenZipExePath() {
		return sevenZipExePath != null ? sevenZipExePath : "C:\\Tools\\bin\\7z.exe";
	}

	public static String getJarExePath() {
		return jarExePath != null ? jarExePath : System.getProperty("java.home") + "\\..\\bin\\jar.exe";
	}
	
	public static ArrayList<MRUFileEntry> getMRUs() {
		return mrus;
	}

	// ***************************** SET METHODS ***************************** 
	
	public static void setGenerateOnSave(boolean newval) {
		ReadmeUI.generateOnSave.setSelection(newval);
	}
	
	public static void setSortConfigFilesByModifiedTime(boolean newval) {
		ReadmeUI.sortConfigFilesByModifiedTime.setSelection(newval);
	}
	
	public static void setDirectory(String newval) {
		ReadmeUI.linkDirectory.setText(Utils.linkify(newval));
	}

	public static void setHotfixDirectory(String newval) {
		newval = (newval == null || newval.equals("")) ? getDirectory() : newval;
		String link = Utils.linkify(newval);
		ReadmeUI.linkHotfixDirectory.setText(link);
	}
		
	public static void setConfigurationPath(String newval) {
		ReadmeUI.labelConfiguration.setText(newval);
		ReadmeUI.setSelectedFileLink(ReadmeUI.expandConfiguration, Utils.getFileName(newval));
	}

	public static void setTemplatePath(String newval) {
		ReadmeUI.labelTemplate.setText(newval);
		ReadmeUI.setSelectedFileLink(ReadmeUI.expandTemplate, Utils.getFileName(newval));
	}

	public static void setOutputPath(String newval) {
		ReadmeUI.labelOutput.setText(newval);
		ReadmeUI.setSelectedFileLink(ReadmeUI.expandOutput, Utils.getFileName(newval));
	}

	public static void setSevenZipExePath(String sevenZipExePath) {
		Prefs.sevenZipExePath = sevenZipExePath;
	}

	public static void setJarExePath(String jarExePath) {
		Prefs.jarExePath = jarExePath;
	}
	
	public static void setMRUs(ArrayList<MRUFileEntry> vals) {
		mrus = vals;
		assert(mrus != null);
		
		// Assign MRU information to menus
		for (int i = 0; i < ReadmeUI.mruMenuCount; i++) {
			String path = i < mrus.size() ? mrus.get(i).configFilePath : "";
			boolean bEmpty = path.equals("");
			String label =  bEmpty ? "(empty)" : Utils.getFileName(path);
			String text = "&" + (i + 1) + " : " + label;
			ReadmeUI.menuMRUFile[i].setText(text + "\tCTRL+" + (i + 1));
			ReadmeUI.menuMRUFile[i].setEnabled(!bEmpty);
			ReadmeUI.menuMRUFile[i].setAccelerator(SWT.MOD1 + ('1' + (char)i));
		}
	}
	
	public static void initMRUs() {
		mrus = new ArrayList<MRUFileEntry>(ReadmeUI.mruMenuCount);
		for (int i = 1; i <= ReadmeUI.mruMenuCount; i++)
			mrus.add(new MRUFileEntry());
		setMRUs(mrus);
	}
		
	// ***************************** SERIALIZATION METHODS ***************************** 
	
	@SuppressWarnings("rawtypes")
	public static void save() {
		try{
			// sneakily fill hashmap with all the property values from our preferences object using reflection API
			hashmap.clear();
			Class noparams[] = {};
			for (Method method : Prefs.class.getDeclaredMethods()) {
				boolean bSerialize = method.getAnnotation(DontSerialize.class) == null;
				if (method.getName().startsWith("get") && bSerialize) {
					Object result = method.invoke((Object)ReadmeUI.prefs, noparams);
					writeProperty(method.getName(), method.getReturnType().getSimpleName(), result);
				}
			}
			
			// Set write permissions on preferences file in case we've downloaded it from source control as readonly
			if (Utils.fileExists(ReadmeUI.preferencesFilePath))
				Utils.setFileReadOnlyFlag(ReadmeUI.preferencesFilePath, false); 
			
			// Serialize hashmap to preferences file
			Utils.writeObjectToFile(hashmap, ReadmeUI.preferencesFilePath);
			
		}catch(Exception ex){
			ReadmeUI.showErrorDialog("Error writing preferences file:\n" + ReadmeUI.preferencesFilePath, ex);
		}
	}
	
	public static boolean load(String preferencesFilePath) {
		String propertyName = "";
		try{
			// Load preference hashmap from file
			hashmap = Utils.readObjectFromFile(ReadmeUI.preferencesFilePath);

			// Set preferences on this object
			for (Method method : Prefs.class.getDeclaredMethods()) {
				boolean bSerialize = method.getAnnotation(DontSerialize.class) == null;
				if (method.getName().startsWith("set") && bSerialize) {
					propertyName = method.getName().replaceFirst("set", "get");
					Object result = readProperty(propertyName);
					if (result != null)
						method.invoke((Object)ReadmeUI.prefs, result);
				}
			}
			
			return true;
			
		} catch(Exception e){
			ReadmeUI.showErrorDialog("Exception reading/setting property:  " + propertyName + " from:\n" + ReadmeUI.preferencesFilePath, e);
			return false;
		}		
	}

	public static void writeProperty(String x, String t, Object y) {
		hashmap.put(x, y);
	}

	public static Object readProperty(String x) {
		return hashmap.get(x);
	}
}