import java.util.HashMap;

import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

	class NewReadmeWizard extends Wizard {

		// Keys to values in the dialog
		public static final String FIX_REQUEST_TITLE_KEY = "Fix Request Title";
		public static final String VERSION_KEY = "Version #";
		public static final String FIX_REQUEST_NUMBER_KEY = "Fix Request #";
		public static final String CUSTOMER_NAME_KEY = "Customer Name";
		public static final String COPY_CONFIG_FROM_KEY = "Copy Config From";
		public static final String OUTPUT_PATH_KEY = "Output Path";
		public static final String OUTPUT_DIRECTORY_KEY = "Output Directory";
		public static final String CONFIGURATION_PATH_KEY = "Configuration Path";
		public static final String TEMPLATE_PATH_KEY = "Template Path";
		public static final String PROJECT_FOLDER_KEY = "Project Folder";
		
		// Maps of values and controls to keys
		public HashMap<String, Object> controls = new HashMap<String, Object>(); 
		public HashMap<String, String> values = new HashMap<String, String>(); 
		
		// The actual tab page of the dialog
		HotfixInfoPage hotfixInfoPage = new HotfixInfoPage(this);
		
		public NewReadmeWizard() {
			super();
		}

		public void addPages() {
			addPage(hotfixInfoPage);
			//addPage(new ChooseDirectoryPage(this));
			//addPage(new SummaryPage(this));
		}

		// Returns a complete suggested readme filename 
		public String getSuggestedFilenamePostfix() {
			String result = "" + 
					"" + getTextWidget(CUSTOMER_NAME_KEY).getText().toUpperCase().replace(" ", "") +
					"_FR" + getTextWidget(FIX_REQUEST_NUMBER_KEY).getText() +					
					"_v" + getTextWidget(VERSION_KEY).getText().replace(".", "") + 
					"_" + getTextWidget(FIX_REQUEST_TITLE_KEY).getText() +
					".txt";
			result = result.replace(" ", "_");
			return result; 
		}

		public void recordWidgetValues() {
			// Save basic fields
			setValue(FIX_REQUEST_NUMBER_KEY, 	getTextWidget(FIX_REQUEST_NUMBER_KEY).getText());
			setValue(FIX_REQUEST_TITLE_KEY, 	getTextWidget(FIX_REQUEST_TITLE_KEY).getText());
			setValue(CUSTOMER_NAME_KEY, 		getTextWidget(CUSTOMER_NAME_KEY).getText());
			setValue(VERSION_KEY, 				getTextWidget(VERSION_KEY).getText());

			// Save computed paths
			String root = getTextWidget(PROJECT_FOLDER_KEY).getText();
			setValue(PROJECT_FOLDER_KEY, 		root);
			setValue(TEMPLATE_PATH_KEY,  		Utils.getFinalPath(root, getTextWidget(TEMPLATE_PATH_KEY).getText()));
			setValue(CONFIGURATION_PATH_KEY,  	Utils.getFinalPath(root, getTextWidget(CONFIGURATION_PATH_KEY).getText()));
			setValue(OUTPUT_PATH_KEY,  			Utils.getFinalPath(root, getTextWidget(OUTPUT_PATH_KEY).getText()));
			setValue(COPY_CONFIG_FROM_KEY, 		Utils.getFinalPath(root, getTextWidget(COPY_CONFIG_FROM_KEY).getText()));
			setValue(OUTPUT_DIRECTORY_KEY,		Utils.getFileDirectory(getValue(OUTPUT_PATH_KEY)));
		}
		
		public boolean performFinish() {
			// The dialog is being closed, serialize text in controls to values in our hashmap.
			recordWidgetValues();
			
			// Only close the wizard if the paths for the input files exist. 
			String[] filePaths = {PROJECT_FOLDER_KEY, TEMPLATE_PATH_KEY, COPY_CONFIG_FROM_KEY, OUTPUT_DIRECTORY_KEY};
			String msg = "";
			for (String p : filePaths) {
				String path = getValue(p);
				if (!Utils.fileExists(path))
					msg += "\n" + p + " does not exist:\n" + path + "\n";
			}
			
			// Show them what's not working in an error dialog
			if (!msg.equals("")) 
				Utils.showMessageBox("Error", SWT.ERROR, "Wizard cannot complete. The following error(s) occurred:\n" + msg); 

			// Only close the wizard if no errors. They can cancel out if they wish.
			return msg.equals("");
		}

		public boolean performCancel() {
			return true;
		}

		public IWizardPage getNextPage(IWizardPage page) {
			if (page instanceof HotfixInfoPage) {
				HotfixInfoPage infoPage = (HotfixInfoPage) page;
				String SuggestedFileName = "-FR" + infoPage.requestNumber.getText() + "-" + infoPage.customerName.getText() + "-" + infoPage.productVersion.getText() + "-" + infoPage.requestTitle.getText();
				values.put("SuggestedFileName", SuggestedFileName);
			}
			IWizardPage nextPage = super.getNextPage(page);
			return nextPage;
		}

		private HotfixInfoPage getHotfixInfoPage() {
			return (HotfixInfoPage) getPage(HotfixInfoPage.PAGE_NAME);
		}
		
		public HashMap<String, String> getValues() {
			return (HashMap<String, String>) values.clone();
		}
		
		public void setValues(HashMap<String, String> map) {
			values = (HashMap<String, String>) map.clone();
		}
		
		public String getValue(String key) {
			return values.get(key);
		}
		
		public void setValue(String key, String value) {
			values.put(key, value);
		}

		public Control getControl(String label) {
			return (Control)controls.get(label);
		}
		
		/** (REO) Returns a text widget from any of the pages in the Wizard.
		 * @param label	
		 * @return 
		 */
		public Text getTextWidget(String label) {
			return (Text)controls.get(label);
		}
	}

	class HotfixInfoPage extends WizardPage {
		public static final String PAGE_NAME = "Info";
		NewReadmeWizard wiz;
		public Text requestNumber;
		public Text requestTitle;
		public Text customerName;
		public Text productVersion;
		public Text projectFolder;
		public Text template;
		public Text configPath;
		public Text outputPath;
		public Text copyConfigPath;
		
		public HotfixInfoPage(NewReadmeWizard parent) {
			super(PAGE_NAME, "New Hotfix Information", null);
			wiz = parent;
		}

		@SuppressWarnings("static-access")
		public void createControl(Composite parent) {
			Composite topLevel = new Composite(parent, SWT.NONE);
			topLevel.setLayout(new GridLayout(3, false));

			// Hotfix info textboxes
			requestNumber = createLabelTextBox(topLevel, 	wiz.FIX_REQUEST_NUMBER_KEY, "", wiz.getValue(wiz.FIX_REQUEST_NUMBER_KEY));
			requestTitle = createLabelTextBox(topLevel, 	wiz.FIX_REQUEST_TITLE_KEY, "", wiz.getValue(wiz.FIX_REQUEST_TITLE_KEY));
			customerName = createLabelTextBox(topLevel, 	wiz.CUSTOMER_NAME_KEY, "", wiz.getValue(wiz.CUSTOMER_NAME_KEY));
			productVersion = createLabelTextBox(topLevel, 	wiz.VERSION_KEY, "", wiz.getValue(wiz.VERSION_KEY));

			requestNumber.addModifyListener(modifyListener);
			requestTitle.addModifyListener(modifyListener);
			customerName.addModifyListener(modifyListener);
			productVersion.addModifyListener(modifyListener);
			
			// Input / Output path textboxes
			createLabelTextBox(topLevel, "");
			projectFolder = createLabelTextBox(topLevel, 	wiz.PROJECT_FOLDER_KEY, "disabled,button", wiz.getValue(wiz.PROJECT_FOLDER_KEY));
			template = createLabelTextBox(topLevel, 		wiz.TEMPLATE_PATH_KEY, "disabled", wiz.getValue(wiz.TEMPLATE_PATH_KEY));
			outputPath = createLabelTextBox(topLevel, 		wiz.OUTPUT_PATH_KEY, "disabled", wiz.getValue(wiz.OUTPUT_PATH_KEY));
			configPath = createLabelTextBox(topLevel, 		wiz.CONFIGURATION_PATH_KEY, "disabled", wiz.getValue(wiz.CONFIGURATION_PATH_KEY));
			
			// Change project folder button
			Button projectFolderButton = (Button)wiz.getControl("Project Folder.button");
			projectFolderButton.addSelectionListener(new SelectionListener() {
				@Override
				public void widgetSelected(SelectionEvent arg0) {
					String result = ReadmeUI.utils.getDirectoryFromDialog("Select Project Folder Path", projectFolder.getText());
					if (result != null && !result.equals("")) 
						projectFolder.setText(result);
				}
				@Override
				public void widgetDefaultSelected(SelectionEvent arg0) {}
			});
			
			// Copy Configuration From textbox and file select button
			createLabelTextBox(topLevel, "");
			copyConfigPath = createLabelTextBox(topLevel, 	wiz.COPY_CONFIG_FROM_KEY, "disabled,button", wiz.getValue(wiz.COPY_CONFIG_FROM_KEY));
			Button copyConfigPathButton = (Button)wiz.getControl("Copy Config From.button");
			copyConfigPathButton.addSelectionListener(new SelectionListener() {
				@Override
				public void widgetSelected(SelectionEvent arg0) {
					String[] parts = Utils.getPathParts(projectFolder.getText(), copyConfigPath.getText());
					String result = ReadmeUI.utils.getFilenameFromDialog("Configuration to Copy", parts[0], parts[1]);
					if (result != null && !result.equals("")) 
						copyConfigPath.setText(result);
				}
				@Override
				public void widgetDefaultSelected(SelectionEvent arg0) {}
			});
			
			setControl(topLevel);
			setPageComplete(true);
		}

		private Text createLabelTextBox(Composite parent, String label) {
			return createLabelTextBox(parent, label, "", "");
		}
		
		private Text createLabelTextBox(Composite parent, String label, String attributes) {
			return createLabelTextBox(parent, label, attributes, "");
		}
		
		private Text createLabelTextBox(Composite parent, String label, String attributes, String defaultValue) {
			boolean bIsBlank = label == null || label.equals("");
			Text text = null;
			Label l = new Label(parent, SWT.CENTER); 		// Left control
			//l.setFont(JFaceResources.getFontRegistry().getBold(JFaceResources.DEFAULT_FONT));
			if (bIsBlank) {
				l = new Label(parent, SWT.CENTER);			// Mid control
				l = new Label(parent, SWT.CENTER); 			// Right control
			} else {
				l.setText(label + "  ");
				text = new Text(parent, SWT.SINGLE);		// Mid control
				GridData gridData = new GridData();
				gridData.horizontalAlignment = GridData.FILL;
				gridData.grabExcessHorizontalSpace = true;			
				text.setLayoutData(gridData);
				text.setText(defaultValue != null ? defaultValue : "");
				text.setEditable(!attributes.contains("readonly"));
				text.setEnabled(!attributes.contains("disabled"));
				wiz.controls.put(label, text); 
				if (attributes.contains("button")) {
					Button button = new Button(parent, SWT.ARROW | SWT.DOWN);
					wiz.controls.put(label + ".button", button); 
					//button.setText("...");
				} else {
					l = new Label(parent, SWT.CENTER);			// Right control
				}
				
			}
			return text;
		}

		public boolean useDefaultDirectory() {
			return false; // button.getSelection();
		}
		
		 /* @author rolivares
		 *	Updates this wizard page based on keystrokes.
		 */
		private ModifyListener modifyListener = new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent arg0) {
				boolean bIsFixRequestField = arg0.widget.equals(requestNumber) || arg0.widget.equals(requestTitle) || arg0.widget.equals(customerName) || arg0.widget.equals(productVersion); 
				if (bIsFixRequestField) {
					String postFix = wiz.getSuggestedFilenamePostfix();
					configPath.setText("\\" + ReadmeUI.configSubdir + "\\Config_" + postFix);
					outputPath.setText("\\" + ReadmeUI.outputSubdir + "\\Readme_" + postFix);
				} else {
				
				} 
			}
		};
	}

	class SummaryPage extends WizardPage {
		public static final String PAGE_NAME = "Summary";
		NewReadmeWizard parentWizard;

		private Label textLabel;

		public SummaryPage(NewReadmeWizard parent) {
			super(PAGE_NAME, "Summary Page", null);
		}

		public void createControl(Composite parent) {
			Composite topLevel = new Composite(parent, SWT.NONE);
			topLevel.setLayout(new FillLayout());

			textLabel = new Label(topLevel, SWT.CENTER);
			textLabel.setText("");

			setControl(topLevel);
			setPageComplete(true);
		}

		public void updateText(String newText) {
			textLabel.setText(newText);
		}
	}
	
	class ChooseDirectoryPage extends WizardPage {
		public static final String PAGE_NAME = "Choose Directory";
		NewReadmeWizard parentWizard;

		private Text text;

		public ChooseDirectoryPage(NewReadmeWizard parent) {
			super(PAGE_NAME, "Choose Directory Page", null);
			parentWizard = parent;
		}

		public void createControl(Composite parent) {
			Composite topLevel = new Composite(parent, SWT.NONE);
			topLevel.setLayout(new GridLayout(2, false));

			Label l = new Label(topLevel, SWT.CENTER);
			l.setText("Enter the directory to use:");

			text = new Text(topLevel, SWT.SINGLE);
			text.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

			setControl(topLevel);
			setPageComplete(true);
		}

		public String getDirectory() {
			return text.getText();
		}
	}