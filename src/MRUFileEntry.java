import java.io.Serializable;

/** Represents an entry (serialized to the preferences) in the Most Recently Used files list in the File menu.
 * @author rolivares
 */
public class MRUFileEntry implements Serializable {

	private static final long serialVersionUID = -2945041940060861153L;
	
	public String configFilePath = "";
	public String templateFilePath = "";
	public String outputFilePath = "";

	public boolean equals(MRUFileEntry x) {
		boolean result = configFilePath.equals(x.configFilePath) && templateFilePath.equals(x.templateFilePath) && outputFilePath.equals(x.outputFilePath);
		return result;
	}
}
