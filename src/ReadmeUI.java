import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ExtendedModifyEvent;
import org.eclipse.swt.custom.ExtendedModifyListener;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.custom.VerifyKeyListener;
import org.eclipse.swt.events.ExpandEvent;
import org.eclipse.swt.events.ExpandListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.ExpandBar;
import org.eclipse.swt.widgets.ExpandItem;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;

/**
 * @author Roberto Enrique Olivares
 * 
 * ================================================================
 * DESCRIPTION
 * ================================================================
 * Simple DevOps tool to automate Fixpack and Hotfix Readme development.
 * Prevents problems with searching/replacing manually in templates handed down by legal.
 * Allows storage of configuration files for each readme seperate from the template.
 * New file generation becomes simply adding a new configuration file and applying that to a template file. 
 * 
 * ================================================================
 *  Required SWT / JFace / Eclipse JARs can be downloaded here:
 * ================================================================
 *  	http://www.java2s.com/Code/JarDownload/
 *  	These are located in the lib subdirectory.
 *  
 * ================================================================
 * Instructions:
 * ================================================================
 * - Run this from the commandline or Eclipse to get the Readme UI.
 * 
 * 		.\readmeui.cmd
 * 	
 * ================================================================
 * Enhancement Suggestions:
 * ================================================================
 *  - Have the hotfix publishing Wizard create a JAVA file with the {hotfix #, files, sizes, times of the hotfix files}, then compile it to FRXXXXX.class and have it included in the hotfix. Add diagnostic servlet code to look for these and summarize and look for hotfix class file conflicts.
 *  
 * ================================================================
 * Defects To Fix:
 * ================================================================
 *  Smooth the scrolling on the FileExpando composite via setRedraw=false/true
 *  
 *  Add File Selector and Logging selector dialog.
 *  Change directory for save as based on which type of file it is.
 *  
 *  Menu option to refresh directories
 *  Clicking on a subdirectory should drill into that directory
 *  Changing the configuration file should offer to rename the output as well
 *  Add something to keep the (xxx) at the top of the list.
 *  Right click on directories to create new file / delete file / rename file / change directory   
 *  
 *  Central exception handler on Main to print exceptions and save temporary work during crash recovery. 
 *  Have each pane scroll with touch drags on white area.
 *  Fix defaulting of save As and also output rename based on new directory structure
 *  
 *  Add a Search bar with drop downs to select files
 *  Font size should be saved to preferences
 *  Size, location, state of window, and also of panes should be restored upon open/exit
 *  Last opened files are highlighted but not opened when app is started.
 *  Need to add refresh directory lists option
 *  Need to add Save Copy... option
 *  Need to add preferences for default filenames
 *  
 * 	Bolding of selected file based on prefs load
 *  Size of directory preference hyperlink is too large
 *  RegExs for config/template/output file name matching should be editable preferences
 *  
 * ================================================================
 * Defects Fixed:
 * ================================================================
 *  7/19/13 	No link to documentation in menus.
 *  7/19/13 	Need a help page for UI
 *  8/21/13		TAB, SHIFT+TAB do not indent/unindent
 *  8/21/13		No reselection of Link control upon save as of file
 *  8/21/13		Saving file scrolls back to top of output file
 *  8/21/13		Title didnt update after save as
 *  8/22/13		Added word wrap
 *  8/22/13		CTRL+A doesnt work
 *  			Need to fix UNDO support
 *  			Added ALT+C+T+O to switch tabs
 *  2/21/14		CTRL+M maximizes the current window
 *  			CTRL+-, CTRL++ change font size
 *  6/17/14		Changed SWT JAR to 32bit and file i/o code from Java 1.7 to Java 1.6 
 *  			Fixed Font startup error
 *  			Exiting other than via file > exit doesnt save prefs.
 *  4/23/15		Added "Publish Hotfix" menu option to copy readme file and generate ZIP
 *  			Fixed: Exception in thread "main" java.lang.UnsatisfiedLinkError: Cannot load 64-bit SWT libraries on 32-bit JVM 
 *  				http://stackoverflow.com/questions/4415827/java-lang-unsatisfiedlinkerror-cannot-load-64-bit-swt-libraries-on-32-bit-jvm-i
 *  				Solution was to include the 32-bit swt.jar and remove the old one from the path.
 *  4/25/15		Added most recently used file list functionality to file menu.
 *  4/27/15		Added Toggle Action Bars to view menu.
 *  			Made default window state maximized.
 *  			Fixed multiple directory loads at beginning which was slow with remote directories.
 *  			Added runXXX methods for running code in new thread or UI thread.
 *  4/28/15		Added default hotfix publishing directory preference to make creating / selecting new folder easier.
 *  4/29/15		Fixed problem with MRU file accelerators.
 *  8/25/15		Fixed issues with saving files. Reverted back to JDK 1.6 compliance.
 *  8/??/16		Added Hotfix generation capability via menu item 
 *  1/18/17		Added more MRUs (now 9)
 *  5/1/17		When you click a config file, have it look for the matching output file
 *  			Be able to sort by date modified
 * 				Sort by date on file lists
 * 				Add dialog to see progress of build hotfix
 * 				Fix converting customer name to filename
 * 				Fix filename directory used for hotfix building ...
 *  			Add New > Hotfix & Readme ... >  Wizard for creating files, ZIP, directory, etc.
 * 				FR #, Name, 
 * 				Add local hotfix directory (Customer Data) to preferences
 * 				Add "copy file to clipboard" menu item.
 * 				Remove filtering for config_XXX
 * 				A wizard option for creating a new config file.
 *  			Multithreaded loading of directories for when path points to network drive.
 *  			Hotfix publish wizard dialog
 *  				Default folder name based on config file name
 *  				Show what's going to be created
 *  				Progress dialog on ZIP creation
 *  			New preference for default hotfix publish directory.
 *  			Progress bar for hotfix publishing.
 *  			Sorting files by last modified 
 */
@SuppressWarnings("unused")
public class ReadmeUI {

	public static Shell shell = new Shell();
	public static Display display = shell.getDisplay();
	public static Utils utils = new Utils(shell);

	public static org.eclipse.swt.graphics.Color blue;
	public static org.eclipse.swt.graphics.Color green;
	public static org.eclipse.swt.graphics.Color red;

	public static StyledText templateText;
	public static StyledText configurationText; 
	public static StyledText outputText;
	
	public static StyledText logStyledText;
	public static String logData = "";
	public static boolean bLogToConsoleAsWell = true;
	
	public static UndoRedoImpl outputTextUndo;
	public static UndoRedoImpl configurationTextUndo;
	public static UndoRedoImpl templateTextUndo;
	
	public static int editorFontSize = 8;
	public static Font editorFont = new Font(display, "Courier New", editorFontSize, SWT.NONE);
	
	public static SashForm sash;

	public static TabFolder tabFolderLeft;
	public static TabFolder tabFolderRight;
	//public static TabFolder tabFolderHidden;
	public static TabItem configurationItem;
	public static TabItem templateItem;
	public static TabItem outputItem;
	public static TabItem logItem;
	
	public static ExpandBar expandBar;
	public static ExpandItem expandActions;
	public static ExpandItem expandPreferences;
	public static ExpandItem expandOutput;
	public static ExpandItem expandConfiguration;
	public static ExpandItem expandTemplate;
	public static HashMap<String, HashMap<String, Link>> linkMaps = new HashMap<String, HashMap<String, Link>>();

	public static Font expandoFontBold = new Font(null, "Segoe UI", 9, SWT.BOLD);
	public static Font expandoFontNormal = new Font(null, "Segoe UI", 9, SWT.NORMAL);
	public static Font expandoFontSmall = new Font(null, "Segoe UI", 8, SWT.NORMAL);
	public static Font expandoFontSmallBold = new Font(null, "Segoe UI", 8, SWT.BOLD);
	
	public static Link oldTemplateLink = null;
	public static Link oldConfigurationLink = null;
	public static Link oldOutputLink = null;

	public static Link linkDirectory;
	public static Button generateOnSave;
	public static Label labelTemplate;
	public static Label labelConfiguration;
	public static Label labelOutput;
	public static Link linkHotfixDirectory;
	public static Button sortConfigFilesByModifiedTime;
	
	public static StyledText widget;
	public static boolean reselect = false;
	public static org.eclipse.swt.graphics.Point range;
	
	public static String helpURL  = "https://www.wikis.com/wikis/home?lang=en-us#!/wiki/W46ae048abb36_4c7f_8735_f88ffe101e86/page/FixPack%20Readme%20Files%20%26%20Templates";
	public static String aboutURL = "https://www.wikis.com/wikis/home?lang=en-us#!/wiki/W46ae048abb36_4c7f_8735_f88ffe101e86/page/FixPack%20Readme%20Files%20%26%20Templates";

	static int mruMenuCount = 9;
	static MenuItem[] menuMRUFile = new MenuItem[mruMenuCount];
	
	static String binDirectory = "";
	static String baseDirectory = "";
	static String defaultDirectory = "";
	static String preferencesFilePath = "";

	static String templateSubdir = "(Templates)";
	static String configSubdir = "(Configurations)";
	static String outputSubdir = "(Output)";

	static String defaultConfigFilename = "(Default Configuration).txt";
	static String defaultTemplateFilename = "(Default Template).txt";
		
	static Prefs prefs = new Prefs();
	static int preferencesColWidth1 = 100;
	static int preferencesColWidth2 = 600;
			
	public static HashMap<String, Object> lastUsed = new HashMap<String, Object>();

	// Threads for reloading directories
	public static ThreadedFileReader threadedFileReader = null;
	
	public static String hotfixPublishingDirectoryTag = "Hotfix Publishing Directory";
	public static String developerJavaFilesTag = "Developer Java File Paths";
	public static String developerLibraryFilesTag = "Developer Library Paths";
	public static String filesIncludedInThisHotfixTag = "Files Included In This Hotfix";

	// REO - New Readme Wizard class
	public static NewReadmeWizard newReadmeWizard = new NewReadmeWizard();
	private static HashMap<String, String> readmeWizardLastEnteredValues = null;
	
	// REO - The readme generator class - processes readme template + config file to generate output file 
	static ReadmeGenerator readme = new ReadmeGenerator();

	// REO - UI Main
	public static void main(String [] args) {
		
		try {
			// Locate preferences file in same dir as this .class file
			binDirectory = ReadmeUI.class.getResource(".").getPath().replaceAll("/", "\\\\").replaceAll("\\\\$","").replaceAll("%20", " ").substring(1);
			String classParentDirectory = binDirectory.replaceAll("(?i)\\\\Bin", "");;
			String[] directoriesToSearch = {classParentDirectory, System.getProperty("user.dir"), "C:\\Tools\\ReadmeUI"};
			String[] standardSubdirectories = {templateSubdir, configSubdir, outputSubdir};
			
			// Search for a preferences file and a suitable default directory (that contains the configuration/template/output subdirs)
			preferencesFilePath = Utils.fileExistsIn(".readmeui.preferences", directoriesToSearch);
			defaultDirectory = Utils.filesAllExistIn(standardSubdirectories, directoriesToSearch);

			if (defaultDirectory == null) {
				defaultDirectory = classParentDirectory;
				// Create subdirectories here
			}
			
			// Initialize and start UI
			createUI();

			// Load any saved preferences
			if (preferencesFilePath != null)
				Prefs.load(preferencesFilePath);			// from preference file
			else 
				Prefs.setDirectory(defaultDirectory);		// prefs file missing, prompt for a directory
				
			// Configure the UI based on preference data
			configureUIFromPreferences();
			
			// Transfer control to UI message loop (mediated by the display object)
			processUIEventsUntilExit();
		
			// Program finishes here when File -> Exit or Close button closes the main window (aka Shell)
			onExit();
			
		} catch (Exception e) {
			log("Initialization Error", e);
		}
	}
	
/****************************************************************************************************************************
	 UI Initialization 
****************************************************************************************************************************/
	public static void createUI() {

		// Init UI and colors
			blue = display.getSystemColor(SWT.COLOR_BLUE);
			green = display.getSystemColor(SWT.COLOR_GREEN);
			red = display.getSystemColor(SWT.COLOR_RED);

		// Dialog layout
			shell.setText("Readme Generator with SWT User Interface (ReadmeUI)");
			shell.setLayout(new FillLayout());
			shell.setSize(1000, 800);
			Rectangle clientArea = shell.getClientArea ();

		// Sash holds the panes together with sizer sliders
			sash = new SashForm (shell, SWT.HORIZONTAL);
			sash.setSashWidth(sash.getSashWidth()*3);

		// Expando bar
			createExpandoBar();
			createFileExpandos();
			expandBar.layout(true, true);

		// Tab folder
			tabFolderLeft = new TabFolder(sash, SWT.NONE);
			tabFolderRight = new TabFolder(sash, SWT.NONE);
		  //tabFolderHidden = new TabFolder(sash, SWT.NONE);
		  //tabFolderHidden.setVisible(false);
		  //tabFolderLeft.setLocation (clientArea.x, clientArea.y);

		// create the styled text widgets
			createTextEditors();

		// Tabs
		  	createTabs();
			tabFolderLeft.pack();
			tabFolderRight.pack();

		// Populate menus
		  	createMenus();
		  	//initToolbar();
		
		// Display the main window
			shell.setMaximized(true);
			shell.open();
	}
	
	public static void configureUIFromPreferences() {
		
		// Initialize threaded directory reader (does not actually start loads)
			threadedFileReader = new ThreadedFileReader(threadedFileReaderEventHandler);
		
		// Updates the file lists in our expandos (this can be slow on network drives)
			requestFileExpandoUpdate();
	
		// Initialize MRUs if none cached
			initMRUList();
	}

	private static void initMRUList() {
		if (Prefs.getMRUs() == null)
			Prefs.initMRUs();	
	}
	
	public static void createMenus() {
		Menu menuBar = new Menu(shell, SWT.BAR);
		shell.setMenuBar(menuBar);
		
		createFileMenu(menuBar);
		createViewMenu(menuBar);
		createHelpMenu(menuBar);
	}

	private static void createTabs() {
		configurationItem = new TabItem (tabFolderLeft, SWT.NONE);
		configurationItem.setText ("Configuration");
		configurationItem.setControl(configurationText);

		templateItem = new TabItem (tabFolderLeft, SWT.NONE);
		templateItem.setText ("Template");
		templateItem.setControl(templateText);

		outputItem = new TabItem (tabFolderRight, SWT.NONE);
		outputItem.setText ("Output");
		outputItem.setControl(outputText);

//		tabFolderHidden.pack();
	}

	private static void createTextEditors() {
		// Instantiate text editors
		templateText = new StyledText(tabFolderLeft, 		SWT.V_SCROLL | SWT.H_SCROLL);
		templateTextUndo = new UndoRedoImpl(templateText);

		configurationText = new StyledText(tabFolderLeft, 	SWT.V_SCROLL | SWT.H_SCROLL);
		configurationTextUndo = new UndoRedoImpl(configurationText);

		outputText = new StyledText(tabFolderRight, 		SWT.V_SCROLL | SWT.H_SCROLL);
		outputTextUndo = new UndoRedoImpl(outputText);
		
		// Add additional keystroke handlers for select all, block indent
		StyledText[] editors = {templateText, configurationText, outputText}; 
		addCommonEditorFunctionality(editors);
	}
	
	private static void addCommonEditorFunctionality(StyledText[] editors) {
		// Configure text editors with common accelerator key functionality
		for (StyledText textEditor : editors) {
			textEditor.setFont(editorFont);
			textEditor.addKeyListener(keyListener);
			textEditor.addVerifyKeyListener(verifyKeyListener);
			textEditor.addExtendedModifyListener(modifyListener);
			textEditor.addVerifyListener(verifyListener);			
			textEditor.addTraverseListener(traverseListener);
		}
	}

	private static void processUIEventsUntilExit() {
		while (!shell.isDisposed()) {
			if (!dispatchUIEvent()) 
				display.sleep();
		}
	}

	public static boolean dispatchUIEvent() {
		return display.readAndDispatch(); 
	}

	private static void onExit() {
		System.exit(0);
	}
	
/****************************************************************************************************************************
	 Menus 
****************************************************************************************************************************/	
	private static void createFileMenu(Menu menuBar) {
		// File menu
		Menu fileMenu = new Menu(shell, SWT.DROP_DOWN);
		MenuItem cascadeFileMenu = new MenuItem(menuBar, SWT.CASCADE);
		cascadeFileMenu.setText("&File");
		cascadeFileMenu.setMenu(fileMenu);

		// File Menu Child items
			createMenuItem(fileMenu, "&New Readme Wizard...\tCtrl+N", SWT.MOD1 + 'N', EventAdapters.newReadmeWizard);
			
			createMenuItem(fileMenu, "", 0, null);

			createMenuItem(fileMenu, "Ne&w File...\tCtrl+W", SWT.MOD1 + 'W', EventAdapters.fileCreateNewFile);
			
			createMenuItem(fileMenu, "&Save\tCtrl+S", SWT.MOD1 + 'S', EventAdapters.fileSave);

			createMenuItem(fileMenu, "Save &As...\tCtrl+Shift+S", SWT.MOD1 + SWT.MOD2 + 'S', EventAdapters.fileSaveAs);

			createMenuItem(fileMenu, "", 0, null);

			createMenuItem(fileMenu, "Show in &Explorer...\tALT+E", SWT.MOD3 + 'E', EventAdapters.fileShowInExplorer);

			createMenuItem(fileMenu, "", 0, null);

			createMenuItem(fileMenu, "&Open Directory...\tCtrl+O", SWT.MOD1 + 'O', EventAdapters.fileOpenDirectory);

			createMenuItem(fileMenu, "", 0, null);
			
			// Generate the processed output and display it
			createMenuItem(fileMenu, "&Generate Readme\tF5", SWT.F5, EventAdapters.generateReadmeAdapter);

			createMenuItem(fileMenu, "Copy File to Clip&board\tCTRL+B", SWT.MOD1 + 'B', EventAdapters.copyFileToClipboardAdapter);
			
			createMenuItem(fileMenu, "", 0, null);
			
			createMenuItem(fileMenu, "&Publish Hotfix...\tCTRL+H", SWT.MOD1 + 'H', EventAdapters.publishHotfixAdapter);

			createMenuItem(fileMenu, "", 0, null);

			for (int i = 0; i < mruMenuCount; i++)
				menuMRUFile[i] = createMenuItem(fileMenu, "MRU" + i + "\tCTRL+" + i, SWT.MOD1 + ('1' + (char)i), EventAdapters.openMostRecentlyUsedFile);
			
			createMenuItem(fileMenu, "", 0, null);
			
/*			createMenuItem(fileMenu, "", 0, null);
			
			createMenuItem(fileMenu, "&Preferences...", SWT.NONE, new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
				}
			});
	*/		
			createMenuItem(fileMenu, "&Exit", SWT.NONE, new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					Prefs.save();
					shell.getDisplay().dispose();
					System.exit(0);
				}
			});
			
			shell.addListener(SWT.Close, new Listener() {
			      public void handleEvent(Event event) {
						Prefs.save();
			      }
			    });
	}
	
	private static void createViewMenu(Menu menuBar) {
		// View menu
		Menu viewMenu = new Menu(shell, SWT.DROP_DOWN);
		MenuItem cascadeViewMenu = new MenuItem(menuBar, SWT.CASCADE);
		cascadeViewMenu.setText("&View");
		cascadeViewMenu.setMenu(viewMenu);
		MenuItem wordWrap = createMenuItem(viewMenu, "&Word Wrap\t CTRL+W", SWT.CHECK, SWT.CTRL | 'w', new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				super.widgetSelected(e);
				boolean wrap = ((MenuItem)e.getSource()).getSelection();
				configurationText.setWordWrap(wrap);
				outputText.setWordWrap(wrap);
				templateText.setWordWrap(wrap);
				((MenuItem)e.getSource()).setSelection(wrap);
			}
		});
		
		createMenuItem(viewMenu, "", SWT.NONE, null);
		
		MenuItem maximize = createMenuItem(viewMenu, "Toggle &Maximize\t CTRL+M", SWT.NONE, SWT.CTRL | 'm', new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				super.widgetSelected(e);
				Control[] x = {};

				if (configurationText.isFocusControl() || templateText.isFocusControl()) {
					 x = new Control[] {tabFolderRight, expandBar};

				} else if (outputText.isFocusControl()) {
					 x = new Control[] {tabFolderLeft, expandBar};
				}
				
				// Toggle visibility on the left hand sash and whichever TabContainer is not selected.
				for (Control c : x) {
					c.setVisible(!c.getVisible());
				}
				
				shell.layout(true, true);
			}
		});		
		
		MenuItem toggleActionBars = createMenuItem(viewMenu, "Toggle &Action Bars\t ALT+A", SWT.NONE, SWT.ALT | 'a', new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				super.widgetSelected(e);
				expandBar.setVisible(!expandBar.getVisible());
				shell.layout(true, true);
			}
		});		
		
		createMenuItem(viewMenu, "", SWT.NONE, null);
		
		MenuItem decreaseFontSize = createMenuItem(viewMenu, "&Decrease Font Size\tCTRL+-", SWT.CTRL | '-', new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				int newSize = getEditorFontSize() - 1;
				if (newSize >= 6)
					setEditorFontSize(newSize);
			}
		});
		MenuItem increaseFontSize = createMenuItem(viewMenu, "&Increase Font Size\tCTRL++", SWT.CTRL | '+', new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				int newSize = getEditorFontSize() + 1;
				if (newSize <= 16)
					setEditorFontSize(newSize);
			}
		});
		
		createMenuItem(viewMenu, "", SWT.NONE, null);
		
		MenuItem viewConfig = createMenuItem(viewMenu, "&Configuration\tALT+C", SWT.ALT | 'c', new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				tabFolderLeft.setSelection(configurationItem);
				configurationText.setFocus();
			}
		});
		
		MenuItem viewTemplate = createMenuItem(viewMenu, "&Template\tALT+T", SWT.ALT | 't', new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				tabFolderLeft.setSelection(templateItem);
				templateText.setFocus();
			}
		});
		
		MenuItem viewOutput = createMenuItem(viewMenu, "&Output\tALT+O", SWT.ALT | 'o', new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				tabFolderRight.setSelection(outputItem);
				outputText.setFocus();
			}
		});

		createMenuItem(viewMenu, "", SWT.NONE, null);
		
		// Log menu
		MenuItem viewLog = createMenuItem(viewMenu, "&Error Log...", SWT.NONE, new SelectionAdapter() {
			/* (non-Javadoc)
			 * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(org.eclipse.swt.events.SelectionEvent)
			 */
			@Override
			public void widgetSelected(SelectionEvent e) {
				setLogVisible(!getLogVisible());				
			}
		});	
	}

	private static void createHelpMenu(Menu menuBar) {
		// Help menu
		Menu helpMenu = new Menu(shell, SWT.DROP_DOWN);
		
		// Online Help menu
		Menu onlineHelpMenu = new Menu(shell, SWT.DROP_DOWN);
		createMenuItem(helpMenu, "Online &Help...\tF1", SWT.F1, new SelectionAdapter() {
			/* (non-Javadoc)
			 * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(org.eclipse.swt.events.SelectionEvent)
			 */
			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					Utils.spawn(helpURL);
				} catch (IOException e1) {
					showException("Error showing Online Help", e1);
				}
			}
		});
		
		createMenuItem(helpMenu, "", SWT.NONE, null);
		
		// About menu
		createMenuItem(helpMenu, "&About...", SWT.NONE, new SelectionAdapter() {
			/* (non-Javadoc)
			 * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(org.eclipse.swt.events.SelectionEvent)
			 */
			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					Utils.spawn(aboutURL);
					utils.showMessageBox("About Readme Generator", SWT.ICON_INFORMATION, "For more information, please see the following website:\n\n" + aboutURL + "\n\n Or contact Roberto Enrique Olivares");
				} catch (IOException e1) {
					showException("Error showing Online About Page", e1);
				}
			}
		});
		
		MenuItem cascadeHelpMenu = new MenuItem(menuBar, SWT.CASCADE);
		cascadeHelpMenu.setText("&Help");
		cascadeHelpMenu.setMenu(helpMenu);
	}

	public static MenuItem createMenuItem(Menu parent, String text, int accel, SelectionAdapter handler) {
		return createMenuItem(parent, text, SWT.PUSH, accel, handler);
	}

	public static MenuItem createMenuItem(Menu parent, String text, int style, int accel, SelectionAdapter handler) {
		if (text == "") {
			MenuItem menuItem = new MenuItem(parent, SWT.SEPARATOR);
			return menuItem;
		}
		MenuItem menuItem = new MenuItem(parent, style);
		menuItem.setText(text);
		menuItem.setAccelerator(accel);
		menuItem.addSelectionListener(handler);
		return menuItem;
	}

	public static void setEditorFontSize(int height) {
		editorFontSize = height;
		editorFont = new Font(null, "Courier New", height, SWT.NONE);
		outputText.setFont(editorFont);
		configurationText.setFont(editorFont);
		templateText.setFont(editorFont);
	}

	public static int getEditorFontSize() {
		return editorFontSize;
	}

/****************************************************************************************************************************
	 UI Widget Creation 
****************************************************************************************************************************/
	
	private static void createExpandoBar() {
		expandBar = new ExpandBar (sash, SWT.V_SCROLL);
		
		expandActions = createActionsExpando(expandBar, 0, "Actions");
		expandActions.setExpanded(true);
		
		expandPreferences = createPreferencesExpando(expandBar, 1, "Preferences");
		expandPreferences.setExpanded(false);
	}
	
	public static ExpandItem createActionsExpando(ExpandBar bar, int index, String title) {

		ExpandItem item = new ExpandItem (bar, SWT.NONE, index);
		Composite composite = new Composite (bar, SWT.NONE);
		GridLayout layout = new GridLayout ();
		layout.marginLeft = layout.marginTop = layout.marginRight = layout.marginBottom = 10;
		//layout.marginLeft = layout.marginTop = layout.marginRight = layout.marginBottom = 0;
		//layout.verticalSpacing = 0;
		//layout.marginTop = 0; 
		//layout.marginBottom = 0; 
		//composite.setBackground(blue);
		//layout.verticalSpacing = 10;
		composite.setLayout(layout);
		
		// Create the action links in this expando and tie them to menu event handlers 
		createActionLinks(composite);
		
		item.setText(title);
		item.setHeight(composite.computeSize(SWT.DEFAULT, SWT.DEFAULT).y);
		item.setControl(composite);
		return item;
		
	}
	
	public static void createActionLinks(Composite composite) {

		createLink(composite, Utils.linkify("New Readme Wizard... (CTRL+N)"), EventListeners.showNewReadmeWizard);

		createLink(composite, Utils.linkify("Create a New File... (CTRL+W)"), EventListeners.createNewFileListener);

		createLink(composite, Utils.linkify("Show File in Explorer... (ALT+E)"), EventAdapters.fileShowInExplorer);

		createLink(composite, Utils.linkify("Generate Readme File... (F5)"), EventAdapters.generateReadmeAdapter);
		
		createLink(composite, Utils.linkify("Copy Output File to Clipboard (CTRL+B)"), EventAdapters.copyFileToClipboardAdapter );
		
		createLink(composite, Utils.linkify("Publish Hotfix to ZIP File... (CTRL+H)"), EventAdapters.publishHotfixAdapter );
	}

	public static ExpandItem createPreferencesExpando(ExpandBar bar, int index, String title) {

		ExpandItem item = new ExpandItem (bar, SWT.NONE, index);
		Composite composite = new Composite (bar, SWT.NONE);
		GridLayout layout = new GridLayout ();
		//layout.marginLeft = layout.marginTop = layout.marginRight = layout.marginBottom = 0;
		//layout.verticalSpacing = 0;
		//layout.marginTop = 0; 
		//layout.marginBottom = 0; 
		//composite.setBackground(blue);
		//layout.verticalSpacing = 10;
		composite.setLayout(layout);
		
		// Create preferences widgets
		generateOnSave 		= createCheckPair(composite, "Regenerate readme file on every save", true);
		
		linkDirectory 		= createLinkPair(composite,  "Directory:", 		defaultDirectory, EventListeners.directoryLinkage);

		labelTemplate 		= createLabelPair(composite, "Template:", 		"");
		labelConfiguration 	= createLabelPair(composite, "Configuration:", 	"");
		labelOutput 		= createLabelPair(composite, "Output:", 		"");
		
		linkHotfixDirectory = createLinkPair(composite,  "Hotfixes:", 		defaultDirectory, EventListeners.hotfixDirectoryLinkage);
		
		sortConfigFilesByModifiedTime = createCheckPair(composite, "Sort Configuration Files by Last Modified", true);
		sortConfigFilesByModifiedTime.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				System.out.println("Before request for file expando update, Prefs.getSorted() = " + Prefs.getSortConfigFilesByModifiedTime());
				requestFileExpandoUpdate();
			}
			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
			}
		});
		
		for (Control child : composite.getChildren()) {
			child.setFont(expandoFontSmall);
		}
		
		//composite.pack();
		//composite.layout();
		
		item.setText(title);
		item.setHeight(composite.computeSize(SWT.DEFAULT, SWT.DEFAULT).y);
		item.setControl(composite);
		//item0.setImage(image);
		return item;
		
	}
	
	public static void createFileExpandos() {
		// Recreate file list controls
		// "(?i).*_Template\\.txt" "(?i)Config.*\\.txt" "(?i).*\\.txt" 		exclude: ".*(_Config|_Template).*"
		expandTemplate 			= createFileListComposite(expandBar, 2, "Template");
		expandConfiguration 	= createFileListComposite(expandBar, 3, "Configuration");
		expandOutput 			= createFileListComposite(expandBar, 4, "Output");

		linkMaps.put(expandTemplate.getText(), new HashMap<String, Link>());
		linkMaps.put(expandConfiguration.getText(), new HashMap<String, Link>());
		linkMaps.put(expandOutput.getText(), new HashMap<String, Link>());
		
		// Expand them
		expandConfiguration.setExpanded(true);
		expandTemplate.setExpanded(true);
		expandOutput.setExpanded(true);
	}

	public static void requestFileExpandoUpdate() {
		String workingDirectory = Prefs.getDirectory();
		
		// Start three threads to perform fast alphabetically sorted directory listings (for slow network shares) 
		threadedFileReader.startLoadAllDirectories(workingDirectory);
		
		// Start a thread to perform sorted directory listing
		if (Prefs.getSortConfigFilesByModifiedTime())
			threadedFileReader.startModifiedTimesCalculatorThread(threadedFileReader.KEY_CONFIGS_SORTED_BY_MODIFIED_TIME, workingDirectory + "\\" + configSubdir);
		
		// possibly clear controls and add a loading label??
		
		// We update the actual FileExpando control as the threadedFileReader fires its completed events
	}

	private static void updateSelectedFileLinks() {
		setSelectedFileLink(expandConfiguration,	Prefs.getConfigurationFileName());
		setSelectedFileLink(expandTemplate, 		Prefs.getTemplateFileName());
		setSelectedFileLink(expandOutput, 			Prefs.getOutputFilename());
	}

	public static Link createLink(Composite parent, String text, final SelectionAdapter listener) {
		final Link link = createLink(parent, text, new Listener() {
			@Override
			public void handleEvent(Event arg0) {
				listener.widgetSelected(new SelectionEvent(arg0));
			}
		}); 
		return link;
	}

	public static Link createLink(Composite parent, String text, Listener listener) {
		Link link = new Link(parent, SWT.PUSH);
		link.setText(text);
		//link.setForeground(new Color());
		link.addListener(SWT.Selection, listener);
		return link;

	}

	public static Label createLabelPair(Composite parent, String text, String value) {
		
		// Composite layout
		Composite pairComposite = new Composite(parent, SWT.NONE);
		//pairComposite.setBackground(red);
		GridLayout pairLayout = new GridLayout(2, false);
		//layout.verticalSpacing = 2;
		pairLayout.marginLeft = 0;
		pairLayout.marginRight = 0;
		pairLayout.marginTop = 0;
		pairLayout.marginBottom = 0;
		pairLayout.verticalSpacing = 0;
		pairLayout.horizontalSpacing = 0;
		pairComposite.setLayout(pairLayout);
		
		// Left label
		Label label1 = new Label(pairComposite, SWT.NONE);
		GridData gridData = new GridData();
		gridData.horizontalAlignment = GridData.BEGINNING;
		gridData.grabExcessHorizontalSpace = false;
		gridData.grabExcessVerticalSpace = true;
		gridData.widthHint = preferencesColWidth1;
		gridData.horizontalIndent = 0;
		gridData.verticalIndent = 0;
		//gridData.heightHint = 9;
		label1.setLayoutData(gridData);
		label1.setText(text);
		label1.setFont(expandoFontSmall);

		Label label2 = new Label(pairComposite, SWT.NONE);
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = false;
		gridData.horizontalIndent = 0;
		gridData.widthHint = preferencesColWidth2;
		gridData.verticalIndent = 0;
		//gridData.heightHint = 9;
		label2.setLayoutData(gridData);
		label2.setText(value);
		label2.setFont(expandoFontSmall);
		
		//pairComposite.pack();
		pairComposite.layout();
		//composite.pack();
		//composite.setHeight(composite.computeSize(SWT.DEFAULT, SWT.DEFAULT).y);
		return label2;
	}

	public static Link createLinkPair(Composite parent, String text, String value, SelectionListener onSelectionListener) {
		
		// Composite layout
		Composite pairComposite = new Composite(parent, SWT.NONE);
		//pairComposite.setBackground(red);
		GridLayout pairLayout = new GridLayout(2, false);
		//layout.verticalSpacing = 2;
		pairLayout.marginLeft = 0;
		pairLayout.marginRight = 0;
		pairLayout.marginTop = 0;
		pairLayout.marginBottom = 0;
		pairLayout.verticalSpacing = 0;
		pairLayout.horizontalSpacing = 0;
		pairComposite.setLayout(pairLayout);
		
		// Left label
		Label label1 = new Label(pairComposite, SWT.NONE);
		GridData gridData = new GridData();
		gridData.horizontalAlignment = GridData.BEGINNING;
		gridData.grabExcessHorizontalSpace = false;
		gridData.grabExcessVerticalSpace = true;
		gridData.widthHint = preferencesColWidth1;
		gridData.horizontalIndent = 0;
		gridData.verticalIndent = 0;
		//gridData.heightHint = 9;
		label1.setLayoutData(gridData);
		label1.setText(text);
		label1.setFont(expandoFontSmall);

		Link widget = new Link(pairComposite, SWT.PUSH);
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = false;
		gridData.horizontalIndent = 0;
		gridData.verticalIndent = 0;
		gridData.widthHint = preferencesColWidth2;
		//gridData.heightHint = 9;
		widget.setLayoutData(gridData);
		widget.setText(value); //"<a href=\"wee\">" + value + "</a>");
		//widget.setFont(expandoFontSmall);
		widget.addSelectionListener(onSelectionListener);
		
		//pairComposite.pack();
		pairComposite.layout();
		//composite.pack();
		//composite.setHeight(composite.computeSize(SWT.DEFAULT, SWT.DEFAULT).y);
		return widget;
	}
	
	public static Text createTextPair(Composite parent, String text, String value) {
		
		// Composite layout
		Composite pairComposite = new Composite(parent, SWT.NONE);
		//pairComposite.setBackground(red);
		GridLayout pairLayout = new GridLayout(2, false);
		//layout.verticalSpacing = 2;
		pairLayout.marginLeft = 0;
		pairLayout.marginRight = 0;
		pairLayout.marginTop = 0;
		pairLayout.marginBottom = 0;
		pairLayout.verticalSpacing = 0;
		pairLayout.horizontalSpacing = 0;
		pairComposite.setLayout(pairLayout);
		
		// Left label
		Label label1 = new Label(pairComposite, SWT.NONE);
		GridData gridData = new GridData();
		gridData.horizontalAlignment = GridData.BEGINNING;
		gridData.grabExcessHorizontalSpace = false;
		gridData.grabExcessVerticalSpace = true;
		gridData.widthHint = preferencesColWidth1;
		gridData.horizontalIndent = 0;
		gridData.verticalIndent = 0;
		//gridData.heightHint = 9;
		label1.setLayoutData(gridData);
		label1.setText(text);
		label1.setFont(expandoFontSmall);

		Text textbox = new Text(pairComposite, SWT.NONE);
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = false;
		gridData.horizontalIndent = 0;
		gridData.verticalIndent = 0;
		gridData.widthHint = preferencesColWidth2;
		//gridData.heightHint = 9;
		textbox.setLayoutData(gridData);
		textbox.setText(value);
		textbox.setFont(expandoFontSmall);
		
		//pairComposite.pack();
		pairComposite.layout();
		//composite.pack();
		//composite.setHeight(composite.computeSize(SWT.DEFAULT, SWT.DEFAULT).y);
		return textbox;
	}	

	
	public static Button createCheckPair(Composite parent, String text, boolean value) {
		
		// Composite layout
		Composite pairComposite = new Composite(parent, SWT.NONE);
		//pairComposite.setBackground(red);
		GridLayout pairLayout = new GridLayout(2, false);
		//layout.verticalSpacing = 2;
		pairLayout.marginLeft = 0;
		pairLayout.marginRight = 0;
		pairLayout.marginTop = 0;
		pairLayout.marginBottom = 0;
		pairLayout.verticalSpacing = 0;
		pairLayout.horizontalSpacing = 0;
		pairComposite.setLayout(pairLayout);
		
		// Left widget
		Button widget = new Button(pairComposite, SWT.CHECK);
		GridData gridData = new GridData();
		gridData.horizontalAlignment = GridData.BEGINNING;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		gridData.widthHint = preferencesColWidth1 + preferencesColWidth2;
		gridData.horizontalIndent = 0;
		gridData.verticalIndent = 0;
		//gridData.heightHint = 9;
		widget.setLayoutData(gridData);
		widget.setText(text);
		widget.setFont(expandoFontSmall);
		widget.setSelection(value);
		
		//pairComposite.pack();
		pairComposite.layout();
		//composite.pack();
		//composite.setHeight(composite.computeSize(SWT.DEFAULT, SWT.DEFAULT).y);
		return widget;
	}	
	
	public static ExpandItem createFileListComposite(ExpandBar bar, int index, String title) {
		ExpandItem expandItem = new ExpandItem (bar, SWT.NONE, index);
		Composite fileList = new Composite (bar, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.marginLeft = layout.marginTop = layout.marginRight = layout.marginBottom = 10;
		//layout.verticalSpacing = 10;
		fileList.setLayout(layout);
		expandItem.setText(title);
		expandItem.setHeight(fileList.computeSize(SWT.DEFAULT, SWT.DEFAULT).y);
		expandItem.setControl(fileList);
		//item0.setImage(image);
		expandBar.addExpandListener(EventListeners.fileListExpandListener);
		return expandItem;
		
	}
	
	public static void populateFileListComposite(ExpandItem expandItem, String directory, String[] fileNames, String includeRegEx, String excludeRegEx, boolean bFreezeRedraw) {

		String title = expandItem.getText();
		Composite fileList = (Composite)expandItem.getControl();

		if (bFreezeRedraw)
			fileList.setRedraw(false);
		
		// temporarily disable filtering (slow)
		boolean bUseInclusionExclusionRegExs = false;
		Pattern includeFilter = bUseInclusionExclusionRegExs ? Pattern.compile(includeRegEx, Pattern.CASE_INSENSITIVE) : null;
		Pattern excludeFilter = bUseInclusionExclusionRegExs ? Pattern.compile(excludeRegEx, Pattern.CASE_INSENSITIVE) : null;
		
		// REO - Clear the cached links for this view
		linkMaps.get(title).clear();
		
		// REO - Remove all child controls
		Utils.removeAllChildren(fileList);
		
		// Create links using the (optionally filtered) list of files
		for (String name : fileNames) {
			boolean bAddThisFile = true;
			
			if (bUseInclusionExclusionRegExs) {
				Matcher includeMatcher = includeFilter.matcher(name);
				Matcher excludeMatcher = excludeFilter.matcher(name);
				boolean isInclude =  includeMatcher.matches();
				boolean isExclude =  excludeMatcher.matches();
				bAddThisFile = isInclude && !isExclude;
			}
			
			if (bAddThisFile) {
				Link link = new Link(fileList, SWT.PUSH);
				link.setText(Utils.linkify(name));
				link.setFont(expandoFontNormal);
				//link.setForeground(new Color());
				link.addListener (SWT.Selection, new FileLinkListener(expandItem, title, directory + "\\" + name, link));
				linkMaps.get(title).put(name, link);
			}
		}
		
		expandItem.setHeight(fileList.computeSize(SWT.DEFAULT, SWT.DEFAULT).y);
		
		if (bFreezeRedraw)
			fileList.setRedraw(true);

		//expandItem.layout();
		//expandItem.getParent().layout();
		//expandItem.getParent().getParent().layout();
	}

// =====================================================================================================================
// EXCEPTION MANAGEMENT	
// =====================================================================================================================
	public static void showException(String title, Exception e) {
		String msg = title + "\n\n" + exceptionAsString(e);
		log(title, e);
		utils.showMessageBox("Exception Error", SWT.ICON_ERROR, msg);
	}

	public static String exceptionAsString(Exception e) {
		StringWriter sw = new StringWriter();
		e.printStackTrace(new PrintWriter(sw));
		return sw.toString();
	}
	
// =====================================================================================================================
// LOGGING 	
// =====================================================================================================================
	public static void setLogToConsoleAsWell(boolean enabled) {
		bLogToConsoleAsWell = enabled;
	}
	
	public static void log(Exception e) {
		log(null, e);
	}
	
	public static void log(String title, Exception e) {
		// print java stacktrace to console
		e.printStackTrace();
		
		// prepare stacktrace for string and textbox
		StringWriter sw = new StringWriter();
		e.printStackTrace(new PrintWriter(sw));
		String exceptionAsString = sw.toString();
		String line1 = (title == null || "".equals(title)) ? "" : title + "\n";
		
		// log to string and textbox
		final String x = line1 + exceptionAsString;
		logToStringAndTextBox(x);
	}

	public static void log(final String x) {
		if (bLogToConsoleAsWell)
			System.out.println(x);
		
		logToStringAndTextBox(x);
	}

	public static void logToStringAndTextBox(final String x) {
		Display.getDefault().asyncExec(new Runnable() {
		    public void run() {
				if (logStyledText != null) {
					logStyledText.setText(logStyledText.getText() + x + "\n\n");
				} else 
					logData = logData + x + "\n";
				}
		});		
	}
	
	public static void logContiguous(final String x) {
		Display.getDefault().asyncExec(new Runnable() {
		    public void run() {
				if (logStyledText != null) {
					logStyledText.setText(logStyledText.getText() + x);
				} else { 
					logData = logData + x + "\n";
				}
			}		
		});
	}

	public static boolean getLogVisible() {
		return true; //tabFolderRight.getVisible();
	}
	
	public static void setLogVisible(boolean isVisible) {
		try {
			if (logStyledText != null)
				return;

//			tabFolderHidden.setVisible(isVisible);
//			sash.layout();
			logStyledText = new StyledText(tabFolderRight, SWT.V_SCROLL | SWT.H_SCROLL);
			logStyledText.setText(logData);
			addCommonEditorFunctionality(new StyledText[] {logStyledText});

			logItem = new TabItem (tabFolderRight, SWT.NONE);
			logItem.setText ("Log");
			logItem.setControl(logStyledText);
		} catch (Exception e) {
			
		}
		//sash.layout(true, true);
	}

// =====================================================================================================================
// NEW README WIZARD 	
// =====================================================================================================================
	@SuppressWarnings("static-access")
	public static void showNewReadmeWizard() {
		newReadmeWizard = new NewReadmeWizard();
		try {
			if (readmeWizardLastEnteredValues == null) {
				// If the wizard hasn't failed, or if this is our first time, use the defaults.
				newReadmeWizard.setValue(NewReadmeWizard.PROJECT_FOLDER_KEY, 	prefs.getDirectory());
				newReadmeWizard.setValue(NewReadmeWizard.TEMPLATE_PATH_KEY, 	"\\" + templateSubdir + "\\" + prefs.getTemplateFileName());
				newReadmeWizard.setValue(NewReadmeWizard.COPY_CONFIG_FROM_KEY, 	"\\" + configSubdir + "\\" + defaultConfigFilename);
				
			} else {
				// Repopulate the last entered values
				newReadmeWizard.setValues(readmeWizardLastEnteredValues);
			}

			// Show the dialog
			WizardDialog dialog = new WizardDialog(shell, newReadmeWizard);
			int result = dialog.open();

			// Save last entered values for repopulating it on next show
			readmeWizardLastEnteredValues = newReadmeWizard.getValues();

			// If they didn't cancel, open/create the file paths provided.
			int cancel = 1;
			if (result != cancel) {
				prefs.setDirectory(newReadmeWizard.getValue(NewReadmeWizard.PROJECT_FOLDER_KEY));
				utils.copyFile(newReadmeWizard.getValue(NewReadmeWizard.COPY_CONFIG_FROM_KEY), newReadmeWizard.getValue(NewReadmeWizard.CONFIGURATION_PATH_KEY));
				utils.createNewFile(newReadmeWizard.getValue(NewReadmeWizard.OUTPUT_PATH_KEY));
				
				requestFileExpandoUpdate();
				
				openFile(newReadmeWizard.getValue(NewReadmeWizard.TEMPLATE_PATH_KEY), 		"Template");
				openFile(newReadmeWizard.getValue(NewReadmeWizard.OUTPUT_PATH_KEY), 		"Output");
				openFile(newReadmeWizard.getValue(NewReadmeWizard.CONFIGURATION_PATH_KEY), 	"Configuration");
				
				// Set values in the config file to what we got from the dialog....
				String cfg = configurationText.getText();
				String currentMonthYear = new SimpleDateFormat("MMMM yyyy").format(Calendar.getInstance().getTime());
				String[][] replaces = { {"##Hotfix Release Date##", 			currentMonthYear} , 
										{"##Product Names and Versions##", 		"" + newReadmeWizard.getValue(NewReadmeWizard.VERSION_KEY) } ,
										{"##Fix Request ID##", 					newReadmeWizard.getValue(NewReadmeWizard.FIX_REQUEST_NUMBER_KEY) } ,
										{"##Fix Request Title##", 				newReadmeWizard.getValue(NewReadmeWizard.FIX_REQUEST_TITLE_KEY) } ,
										{"##Customer Company Name##", 			newReadmeWizard.getValue(NewReadmeWizard.CUSTOMER_NAME_KEY) } };
				for (int i = 0; i < replaces.length; i++) {
					String section = replaces[i][0]; 
					String value = replaces[i][1]; 
					cfg = cfg.replaceAll(section + ".*", section + " " + value);
				}
				configurationText.setText(cfg);
			}
			
		} catch(Exception e) {
			showException("Error Generating Readme Files from Wizard", e);
		}
	}

	public static void promptForFileNameAndCreateNewFile() {
		try {
			String filePath = utils.getFilenameFromDialog("Specify filename for new file", Prefs.getDirectory(), "");
			if (filePath.length() == 0)
				return;
			File file = new File(filePath);
			file.createNewFile();
		} catch(Exception e) {
			showException("Error creating new file", e);
		}
	}	
	
	public static void setSelectedFileLink(ExpandItem expandItem, String linkFilename) {
		String expandItemName = expandItem.getText();
		Link lastLink = (Link)lastUsed.get(expandItemName);		
		HashMap<String, Link> linkMap = linkMaps.get(expandItemName); 
		Link link = linkMap == null ? null : linkMap.get(linkFilename);
		if (lastLink != null && !lastLink.isDisposed())
			lastLink.setFont(expandoFontNormal);
		if (link != null) {
			link.setFont(expandoFontBold);
			Rectangle rect = link.getBounds();
			rect.width +=10;
			link.setBounds(rect);
			link.getParent().layout(true, true);
		}
		lastUsed.put(expandItemName, link);
	}
	
/****************************************************************************************************************************
	 UI Event Handlers 
 ****************************************************************************************************************************/

	// Events from threaded file reader
	private static ThreadedFileReader.IThreadedFileReaderEvents threadedFileReaderEventHandler = new ThreadedFileReader.IThreadedFileReaderEvents() {
		@Override
		public void onShowWaitAgainDialog(ThreadedFileReader reader, long timeElapsed) {
			String msg = "Waited " + (int)(timeElapsed / 1000) + " seconds on the following directories to load:\n\n";
			for (String path : reader.paths.values()) {
				msg += path + "\n";
			}
			msg += "\nWait again?";
			int yesno = utils.showMessageBoxYesNo("Waiting on Directories", msg);
			reader.setEventResult(yesno == SWT.YES);
		}

		@SuppressWarnings("static-access")
		@Override
		public void onDirectoryLoaded(ThreadedFileReader reader, String key, String path, String[] fileNames) {
			
			if (reader.KEY_CONFIGURATION_FILES_SORTED_BY_NAME.equalsIgnoreCase(key)) {
				// called when configuration files are reloaded
				if (Utils.differsFromLast("Configuration Files", fileNames)) {
					populateFileListComposite(expandConfiguration, 	path, fileNames, "(?i).*", 	"None",	true); 
					setSelectedFileLink(expandConfiguration, Prefs.getConfigurationFileName());
				}
				
			} else if (reader.KEY_OUTPUT_FILES.equalsIgnoreCase(key)) {
				// called when output files are reloaded
				populateFileListComposite(expandOutput, 		path, fileNames, "(?i).*", 	"None",	true); 
				setSelectedFileLink(expandOutput, Prefs.getOutputFilename());
			
			} else if (reader.KEY_TEMPLATE_FILES.equalsIgnoreCase(key)) {
				// called when template files are reloaded
				populateFileListComposite(expandTemplate, 		path, fileNames, "(?i).*", 	"None",	true); 
				setSelectedFileLink(expandTemplate, Prefs.getTemplateFileName());
				
			} else if (reader.KEY_CONFIGS_SORTED_BY_MODIFIED_TIME.equalsIgnoreCase(key)) {
				// Called every few seconds to update list of config files
				if (Utils.differsFromLast("Configuration Files", fileNames)) {
					//System.out.println(Utils.join(fileNames, "\n"));
					populateFileListComposite(expandConfiguration, 	path, fileNames, "(?i).*", 	"None",	true); 
					setSelectedFileLink(expandConfiguration, Prefs.getConfigurationFileName());
				}
			}
		}
	};
	
	/**
	 * Updates the info on the action expand bar item
	 * @author rolivares 
	 */
	public static void updateActionInfo() {
		
		String titleInfo = "[" + Prefs.getTemplateFileName() + " + " + Prefs.getConfigurationFileName() + " = " + Prefs.getOutputFilename() + "]";
		shell.setText("Unica Readme Generator - " + titleInfo);

		/*
		labelDirectory.setText(Prefs.getDirectory());
		labelDirectory.getParent().layout();

		labelTemplate.setText(Prefs.getTemplateFileName());
		labelTemplate.getParent().layout();

		labelConfiguration.setText(Prefs.getConfigurationFileName());
		labelConfiguration.getParent().layout();

		labelOutput.setText(Prefs.getOutputFileName());	
		labelOutput.getParent().getParent().layout();
		*/
		labelOutput.getParent().getParent().layout(true, true);
	}

	/**
	 * @author rolivares
	 * Generally for menu events 
	 */
	public static class EventAdapters {

		public static SelectionAdapter copyFileToClipboardAdapter = new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				try {
					String contents = outputText.getText();
					String tempFilePath = Utils.createTemporaryFile(Prefs.getOutputFilename());
					Utils.writeFile(tempFilePath, contents);
					Utils.copyFileToClipboard(tempFilePath);
				} catch (IOException e) {
					log("Error creating temporary output file to copy to clipboard.", e);
				}
			}
		};;

		public static SelectionAdapter newReadmeWizard = new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				showNewReadmeWizard();
			}
		};

		public static SelectionAdapter openMostRecentlyUsedFile = new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				int index = Utils.indexOf(event.widget, menuMRUFile);
				if (index == -1)
					return;
				ArrayList<MRUFileEntry> mrus = Prefs.getMRUs();
				MRUFileEntry e = mrus.get(index);
				ReadmeUI.openFile(e.templateFilePath, "Template");
				ReadmeUI.openFile(e.configFilePath, "Configuration");
				ReadmeUI.openFile(e.outputFilePath, "Output");
			}
		};

		public static SelectionAdapter fileCreateNewFile = new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				promptForFileNameAndCreateNewFile();
			}
		};
		
		public static SelectionAdapter fileOpenDirectory = new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				try {
					String x = Utils.getDirectoryFromDialog("", Prefs.getDirectory(), shell);
					if (x.length() == 0 || !Utils.fileExists(x) )
						return;
					Prefs.setDirectory(x);
					requestFileExpandoUpdate();
				} catch (Exception e) {
					showException("Error opening directory", e);
				}
			}
		};

		public static SelectionAdapter fileSave = new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				try {
					
					if (configurationText.isFocusControl()) {
						if (Prefs.getConfigurationPath().length() == 0)
							Prefs.setConfigurationPath(utils.getFilenameFromDialog("", Prefs.getDirectory(), ""));
						if (Prefs.getConfigurationPath().length() == 0)
							return;
						Utils.writeFile(Prefs.getConfigurationPath(), configurationText.getText());
						if (Prefs.getGenerateOnSave())
							EventAdapters.generateReadmeAdapter.widgetSelected(null);

					} else if (templateText.isFocusControl()) {
						if (Prefs.getTemplatePath().length() == 0)
							Prefs.setTemplatePath(utils.getFilenameFromDialog("", Prefs.getDirectory(), ""));
						if (Prefs.getTemplatePath().length() == 0)
							return;
						Utils.writeFile(Prefs.getTemplatePath(), templateText.getText());
						if (Prefs.getGenerateOnSave())
							EventAdapters.generateReadmeAdapter.widgetSelected(null);

					} else if (outputText.isFocusControl()) {
						if (Prefs.getOutputPath().length() == 0)
							Prefs.setOutputPath(utils.getFilenameFromDialog("", Prefs.getDirectory(), ""));
						if (Prefs.getOutputPath().length() == 0)
							return;
						Utils.writeFile(Prefs.getOutputPath(), outputText.getText());
					}
				} catch (Exception e) {
					showException("Error saving file", e);
				}
			}
		};
		
		public static SelectionAdapter fileSaveAs = new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					String newFilename = "";

					if (configurationText.isFocusControl()) {
						newFilename = utils.getFilenameFromDialog("Save As", Prefs.getDirectory(), Prefs.getConfigurationFileName());
						if (newFilename.length() == 0)
							return;
						Prefs.setConfigurationPath(newFilename);
						Utils.writeFile(Prefs.getConfigurationPath(), configurationText.getText());
						if (Prefs.getGenerateOnSave())
							EventAdapters.generateReadmeAdapter.widgetSelected(null);
						
					} else if (templateText.isFocusControl()) {
						newFilename = utils.getFilenameFromDialog("Save As", Prefs.getDirectory(), Prefs.getTemplateFileName());
						if (newFilename.length() == 0)
							return;
						Utils.writeFile(Prefs.getTemplatePath(), templateText.getText());
						Prefs.setTemplatePath(newFilename);
						if (Prefs.getGenerateOnSave())
							EventAdapters.generateReadmeAdapter.widgetSelected(null);
						
					} else if (outputText.isFocusControl()) {
						newFilename = utils.getFilenameFromDialog("Save As", Prefs.getDirectory(), Prefs.getOutputFilename());
						if (newFilename.length() == 0)
							return;
						Prefs.setOutputPath(newFilename);
						Utils.writeFile(Prefs.getOutputPath(), outputText.getText());
					}
					
					requestFileExpandoUpdate();
					updateActionInfo();
				} catch (Exception ex) {
					showException("Error saving file as...", ex);
				}
			}
		};

		public static SelectionAdapter fileShowInExplorer = new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					String filePath = "";

					if (configurationText.isFocusControl()) {
						filePath = Prefs.getConfigurationPath();
						
					} else if (templateText.isFocusControl()) {
						filePath = Prefs.getTemplatePath();
						
					} else if (outputText.isFocusControl()) {
						filePath = Prefs.getOutputPath();
					
					} else {
						filePath = Prefs.getOutputPath();
					}
					
					// explorer /select,"%PROJECT%\bin\%CLASS%" or  
					Utils.spawn("explorer /select,\"" + filePath + "\"");
					
				} catch (Exception ex) {
					showException("Error showing file in explorer", ex);
				}
			}
		};
		
		public static SelectionAdapter generateReadmeAdapter = new SelectionAdapter() {
			@SuppressWarnings("static-access")
			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					
					// Need file inputs specified
					if (Prefs.getTemplateFileName().length() == 0 || Prefs.getConfigurationFileName().length() == 0) {
				        MessageBox messageBox = new MessageBox(shell, SWT.ICON_ERROR);
				        messageBox.setText("Generate Readme File - Error");
				        messageBox.setMessage("Valid Template, Configuration, and Output files must be selected before generation.");
				        messageBox.open();
				        return;
					}
	
					// No output file specified, guess one
					if (Prefs.getOutputFilename().length() == 0) {
						String outputPathSuggested = Prefs.getConfigurationPath().replaceAll("(?i)_CONFIG", "");
				        MessageBox messageBox = new MessageBox(shell, SWT.ICON_WARNING | SWT.OK | SWT.CANCEL );
				        messageBox.setText("Generate Readme File - Warning");
				        messageBox.setMessage(	"An output file was not selected.\n" +
				        						"\n" +
				        						"The configuration file is:\n\n" +
				        						"" + Prefs.getConfigurationPath() + "\n\n" + 
				        						"The output file has been defaulted to:\n\n" +
				        						"" + outputPathSuggested);
				        if (messageBox.open() == SWT.CANCEL)
				        	return;
				        Prefs.setOutputPath(outputPathSuggested);
				        updateActionInfo();
					}
					
					// Process readme
					log("Generating: readme.processFiles(" + Prefs.getTemplatePath() + ", " + Prefs.getConfigurationPath() + ", " + Prefs.getOutputPath() + ")");
					readme.processFiles(Prefs.getTemplatePath(), Prefs.getConfigurationPath(), Prefs.getOutputPath(), "", 4);
					String text = utils.readFile(Prefs.getOutputPath());
					
					// Try to use either the exact line index or the first match to that as new scroll target so we dont jump back to the beginning
					int newTopLineIndex = utils.getEquivalentFirstLine(outputText.getText().split("\n"), outputText.getTopIndex(), text.split("\n"));
					outputText.setText(text);
					outputText.setTopIndex(newTopLineIndex);
					
					commitOpenFilesToMRUFileList();
				} catch (Exception e1) {
					showException("Error Generating Readme File", e1);
				}
			}
		};

		/**
		 * Creates and executes a task to publish the hotfix while updating the progress dialog 
		 */
		public static SelectionAdapter publishHotfixAdapter = new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					setLogToConsoleAsWell(true);
					
					// create dialog - TODO: Add configuration pane for multiple hotfix publishing plugins
					PublishHotfixTaskSamplePlugin publishTask = new PublishHotfixTaskSamplePlugin(new Shell(), configurationText, outputText, new Object[] {
						"OutputFilename", 		Prefs.getOutputFilename(), 
						"OutputPath", 			Prefs.getOutputPath(),
						"ConfigurationPath", 	Prefs.getConfigurationPath(),
						"TemplatePath", 		Prefs.getTemplatePath(),
						"JarExePath", 			Prefs.getJarExePath(),
						"SevenZipExePath", 		Prefs.getSevenZipExePath(),
						"configurationText", 	configurationText.getText()
					});

					// show dialog
					publishTask.execute();
					
					// display failures if any
					String failures = publishTask.getFailures();
					if (failures != null)
						utils.showMessageBox("Failure(s) While Publishing Hotfix", SWT.ERROR, failures);
										
				} catch (Exception e1) {
					showException("Exception Encountered While Publishing Hotfix", e1);
					
				} finally {
					setLogToConsoleAsWell(false);
				}
			}

		};
	};
	
	/**
	 * @author rolivares
	 * Generally for widget events (Link, etc.)
	 */
	public static class EventListeners {
		
		public static ExpandListener fileListExpandListener = new ExpandListener() {
			
			@Override
			public void itemExpanded(ExpandEvent arg0) {
				ExpandBar bar = (ExpandBar)arg0.getSource();
				ExpandItem item = (ExpandItem)arg0.item;
			}
			
			@Override
			public void itemCollapsed(ExpandEvent arg0) {
			}
		};

		public static Listener showNewReadmeWizard = new Listener() {
			@Override
			public void handleEvent(Event arg0) {
				showNewReadmeWizard();
			}
		};
		
		public static Listener createNewFileListener = new Listener() {
			@Override
			public void handleEvent(Event arg0) {
				promptForFileNameAndCreateNewFile();
			}
		};
		
		public static SelectionListener directoryLinkage = new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				Link link = (Link)arg0.getSource();
				String start = Prefs.getDirectory();
				String d = Utils.getDirectoryFromDialog("Select readme repository directory", start, shell);
				if (d.length() == 0)
					return;
				Prefs.setDirectory(d);
				requestFileExpandoUpdate();
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
			}
		};

		public static SelectionListener hotfixDirectoryLinkage = new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				Link link = (Link)arg0.getSource();
				String start = Prefs.getHotfixDirectory();
				String d = Utils.getDirectoryFromDialog("Select Directory To Publish Hotfixes Into", start, shell);
				if (d.length() == 0)
					return;
				Prefs.setHotfixDirectory(d);
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
			}
		};
		
	}

/************************************************************************************************************************************
 	COMMON TEXT EDITOR KEY EVENT HANDLERS FOR ADDED FUNCTIONALITY (SELECT ALL, BLOCK INDENT)
 ************************************************************************************************************************************/
	
	public static org.eclipse.swt.events.KeyListener keyListener = new org.eclipse.swt.events.KeyListener() {
		@Override
		public void keyPressed(KeyEvent arg0) {
			try {
				widget = (StyledText) arg0.getSource();
			} catch(Exception e) {
			}
		}

		@Override
		public void keyReleased(KeyEvent arg0) {
		}
	};

	public static VerifyKeyListener verifyKeyListener = new VerifyKeyListener() {

		@Override
		public void verifyKey(VerifyEvent e) {			
			widget = (StyledText) e.getSource();
			boolean bCtrl = ((e.stateMask & SWT.CTRL) != 0);
			boolean bShift = ((e.stateMask & SWT.SHIFT) != 0);
			
			try {
				//System.out.println(e.toString());
			} catch (Exception ex) {
			}
			
            if (e.keyCode == SWT.TAB) {
            	// ========== TAB KEY - BLOCK INDENT ========== 
            	String oldText = widget.getSelectionText();
            	String newText = null;
                if ((e.stateMask & SWT.SHIFT) != 0){
					newText = Utils.deIndentLines(oldText);	// deindent lines
                } else {
    				if (widget.getSelectionCount() > 0) {
    					newText = Utils.indentLines(oldText);	// indent lines
    				}
                }
                
                if (newText != null) {
					e.doit = false;
					int delta = newText.length() - oldText.length();
					range = widget.getSelectionRange();				
					widget.replaceTextRange(range.x, range.y, newText);
					range.y += delta;
					widget.setSelectionRange(range.x, range.y);
					widget.showSelection();
                	
                }

            } else if (e.keyCode == (int)'a' && bCtrl) {
            	// ========== CTRL+A - SELECT ALL ========== 
            	widget.setSelection(0, widget.getText().length());
            	e.doit = false;
            }
		}
	};

	public static ExtendedModifyListener modifyListener = new ExtendedModifyListener() {
		
		@Override
		public void modifyText(ExtendedModifyEvent arg0) {
			if (reselect) {
				widget.setSelectionRange(range.x, range.y);
				reselect = false;
			}
		}
	};
	
	public static VerifyListener verifyListener = new VerifyListener() {
		@Override
		public void verifyText(VerifyEvent arg0) {
			if (arg0.text.equals("\t")) {
			}
			
		}
	};
	
	public static TraverseListener traverseListener = new TraverseListener() { 
		@Override
        public void keyTraversed(TraverseEvent e) { 
            if (e.detail == SWT.TRAVERSE_TAB_PREVIOUS) {
            	// http://stackoverflow.com/questions/3773123/swt-styledtext-how-to-indent-un-indent-selected-text-with-tab-or-shifttab-key
                e.doit = false; //allows verifyKey listener to fire
            }
        } 
    };	

/************************************************************************************************************************************
 ************************************************************************************************************************************/
	
	/** (REO) Opens a file in the UI. Usually called by FileLinkListener objects.
	 * 
	 * @param filePath
	 * @param key		"Configuration", "Template", "Output"
	 */
	public static void openFile(String filePath, String key) {
		try {
			String text = Utils.readFile(filePath);
			String fileName = Utils.getFileName(filePath);
			if (key.equals("Configuration")) {
					Prefs.setConfigurationPath(filePath);
					ReadmeUI.configurationText.setText(text);
					ReadmeUI.tabFolderLeft.setSelection(ReadmeUI.configurationItem);
					ReadmeUI.configurationText.setFocus();
					//configurationItem.setText("Configuration - " + fileName);
					ReadmeUI.setSelectedFileLink(ReadmeUI.expandConfiguration, fileName);
/*						link.setFont(expandoFontBold);
					if (oldConfigurationLink != null)
						oldConfigurationLink.setFont(expandoFontNormal);
					oldConfigurationLink = link;
*/				
			} else if (key.equals("Template")) {
					Prefs.setTemplatePath(filePath);
					ReadmeUI.templateText.setText(text);
					ReadmeUI.tabFolderLeft.setSelection(ReadmeUI.templateItem);
					ReadmeUI.templateText.setFocus();
					ReadmeUI.setSelectedFileLink(ReadmeUI.expandTemplate, fileName);

					//templateItem.setText("Template - " + fileName);
//					link.setFont(expandoFontBold);
//					if (oldTemplateLink != null)
//						oldTemplateLink.setFont(expandoFontNormal);
//					oldTemplateLink = link;

			} else if (key.equals("Output")) {
					Prefs.setOutputPath(filePath);
					ReadmeUI.outputText.setText(text);
					ReadmeUI.tabFolderRight.setSelection(ReadmeUI.outputItem);
					ReadmeUI.outputText.setFocus();
					//outputItem.setText("Output - " + fileName);
					ReadmeUI.setSelectedFileLink(ReadmeUI.expandOutput, fileName);
					//						link.setFont(expandoFontBold);
//					if (oldOutputLink != null)
//						oldOutputLink.setFont(expandoFontNormal);
//					oldOutputLink = link;
			}
			ReadmeUI.updateActionInfo();
		} catch(Exception e) {
			ReadmeUI.showException("Error Opening File: " + filePath,e);
		}		
	}


	/** (REO) Add entry to MRU list and update menus
	 */
	public static void commitOpenFilesToMRUFileList() {
		
		ArrayList<MRUFileEntry> mrus = Prefs.getMRUs();
		
		// New MRU entry
		MRUFileEntry e = new MRUFileEntry();
		e.configFilePath = Prefs.getConfigurationPath();
		e.templateFilePath = Prefs.getTemplatePath();
		e.outputFilePath = Prefs.getOutputPath();

		// See if it already exists
		int existingEntryIndex = -1;
		for (int i = 0; i < mrus.size(); i++) { 
			if (mrus.get(i).equals(e)) {
				existingEntryIndex = i;
				break;
			}
		}

		// Update the list
		boolean bEntryPresent = existingEntryIndex > -1;
		if (bEntryPresent && existingEntryIndex == 0) {
			// It's at the top already
			return;
			
		} else if (bEntryPresent) {
			// Move it to the top
			mrus.remove(existingEntryIndex);
			mrus.add(0, e);
			
		} else {
			// Make sure we have at least as many MRU entries as menus
			while(mrus.size() < ReadmeUI.mruMenuCount)
				mrus.add(new MRUFileEntry());

			// Push new entry onto front, pop one off end
			mrus.add(0, e);
			mrus.remove(mrus.size() - 1);
		}
		
		// Redo menus
		Prefs.setMRUs(mrus);
	}

	/** (REO) Runs a snippet of code safely in the UI thread. Use for UI control updates from another thread.
	 * 
	 * Example: runInUIThread( new Runnable() { @Override public void run() { ... } } );
	 * 
	 * @param codeSnippet
	 */
	public static void runInUIThread(Runnable codeSnippet) {
		Display.getDefault().asyncExec(codeSnippet);
	}
	
	/** (REO) Spawns a new thread and runs it.
	 * @param codeSnippet
	 * @return
	 */
	public static Thread runInNewThread(Runnable codeSnippet) {
		Thread thread = new Thread(codeSnippet);
		thread.run();
		return thread;
	}
}
