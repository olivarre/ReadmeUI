import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Roberto Enrique Olivares
 * 
 *	Modular readme template and configuration file parser utility program. 
 *	Allows general hotfix or fixpack readme templates to be kept separate of individual hotfix configuration details.
 *	Can quickly generate a final output readme based on templates and configuration files. 
 *
 *	Inputs:
 *	
 *		Configuration file (typically .txt)
 *			Contains lines and paragraphs that begin with "## SECTION-NAME ##" that are called section tokens
 *			All text after the section delimiter (and before the next or EOF) is stored under that section name.
 *
 *		Template file (typically .txt)
 *			Contains text with interspersed "## SECTION-NAME ##" tokens
 *
 *	Output:
 *
 *		Reads a configuration file with section tokens and matching text to create a set of <section-name to text blob> mappings.
 *		Reads a template file that contains section tokens that will be replaced.
 *		Generates and output file that is the template file with section tokens replaced with the appropriate blobs from the configuration file.
 *		Can also generate HTML versions of the output text file.		
 *	
 *	See ReadmeUI.java for the actual SWT-based Java UI.  The UI relies on this module as its parser.
 */
public class ReadmeGenerator {

	public static String NEWLINE = "\n";
	public static Charset ENCODING = Charset.defaultCharset();
	public static String html = 	"<meta http-equiv=\"refresh\" content=\"1;\">\n" +
									"<pre style=\"white-space:pre-wrap;\">\n" +
									"STUFF\n" +
									"</pre>";
	
	public static boolean silentMode = false;
	
	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {

		System.out.println("===================== Readme File Template Processor ====================");
		System.out.println("");
		
		/*
		println(replace("abcde", "a", "123"));
		println(replace("abcde", "c", "123"));
		println(replace("abcde", "e", "123"));
		println(replace("abcde", "f", "123"));

		println(replaceAll("aabcde", "a", "123"));
		println(replaceAll("abccde", "c", "123"));
		println(replaceAll("abcdee", "e", "123"));
		println(replaceAll("xbxdx", "x", "123"));
		println(replaceAll("abcde", "f", "123"));
		*/ 

		// Print help
		if (args.length == 0 || args[0].startsWith("-?")) {
			System.out.println("Arguments:\n" +
					"  -dir=\"...\"          If provided, is prepended to the template, config, and out paths.\n" +
					"  -template=\"...\"     Path to template file.\n" +
					"  -config=\"...\"       Path to config file.\n" +
					"  -out=\"...\"          Path to output file.\n" +
					"  -html=\"...\"         Path to HTML viewer file.\n" +
					"  -silent                 Minimizes output to screen.\n" +
					"  -refresh=\"...\"      Continually process the files every X milliseconds until keypress.\n" +
					"\n" +
					"Note: Lines from the config file are automatically wrapped for at 80 characters by the processor.\n" +
					"\n" +
					"Roberto E Olivares\n");
			return;
		}
		
		// Inputs to program
		String readme_template_path = "";
		String readme_config_path = "";
		String readme_path = "";
		String readme_html_path = "";
		String directory = "";
		int tabSize = 4;
		int refresh = 0;
		boolean htmlPathSupplied = false;

		// Scan command line arguments
		String TEMPLATE_FILE = "-template=";
		String CONFIG_FILE = "-config=";
		String OUTPUT_FILE = "-out=";
		String DIRECTORY = "-dir=";
		String HTML = "-html=";
		String TAB = "-tab=";
		String REFRESH = "-refresh=";
		String SILENT = "-silent";
		for (String arg : args) {
			if (arg.startsWith(TEMPLATE_FILE)) {
				readme_template_path = arg.substring(TEMPLATE_FILE.length());
			} else if (arg.startsWith(CONFIG_FILE)) {
				readme_config_path = arg.substring(CONFIG_FILE.length());
			} else if (arg.startsWith(OUTPUT_FILE)) {
				readme_path = arg.substring(OUTPUT_FILE.length());
			} else if (arg.startsWith(DIRECTORY)) {
				directory = arg.substring(DIRECTORY.length()) + "\\";
			} else if (arg.startsWith(HTML)) {
				readme_html_path = arg.substring(HTML.length());
				htmlPathSupplied = true;
			} else if (arg.startsWith(TAB)) {
				tabSize = Integer.parseInt(arg.substring(TAB.length()));
			} else if (arg.startsWith(REFRESH)) {
				refresh = Integer.parseInt(arg.substring(REFRESH.length()));
			} else if (arg.startsWith(SILENT)) {
				silentMode = true;
			}
		}

		// Apply parameters
		readme_template_path = directory + readme_template_path;
		readme_config_path = directory + readme_config_path;
		readme_path = directory + readme_path;
		readme_html_path = htmlPathSupplied ? (directory + readme_html_path) : "";
		
		// Display parameters
		System.out.println("Readme template path:  " + readme_template_path);
		System.out.println("Readme config path:    " + readme_config_path);
		System.out.println("Readme output path:    " + readme_path);		
		System.out.println("HTML output path:      "  + readme_html_path);
		
		if (refresh > 0) {
			// (REO) Allow the refresh=XXX to set refresh in millis and keep processing every few seconds. 
			System.out.println("\nPress any key to safely exit parsing loop. Processing files every " + refresh + " milliseconds...\n");
			boolean keepGoing = true;
			while (keepGoing) {
				try { 
					processFiles(readme_template_path, readme_config_path, readme_path, readme_html_path, tabSize);
				} catch (Exception e) {
				}
				Thread.sleep(refresh);
				if (System.in.available() > 0) break;
			}
		} else { 
			// (REO) Just process file once.
			processFiles(readme_template_path, readme_config_path, readme_path, readme_html_path, tabSize);
		}
	}
	
	public static void processFiles(String readme_template_path, String readme_config_path, String readme_path, String readme_html_path, int tabSize) throws Exception {
		
		// Load template
		println("");
		println("Processing template file ...");
		println("readme_template_path: " + readme_template_path);
		println("readme_config_path:   " + readme_config_path);
		println("readme_path:          " + readme_path);
		println("readme_html_path:     "+  readme_html_path);

		println("");
		println("Loading template...");
		List<String> 	readme_lines = readTextFileLines(readme_template_path);
		String 			readme_template = join(readme_lines);
		String 			result = readme_template;

		// Load config / template variable replacements
		println("");
		println("Loading config...");
		List<String> config_lines = readTextFileLines(readme_config_path);
		
		// Process tokens / values
		HashMap<String, String> token_map = new HashMap<String, String>(10); 
		String token = "";
		String token_value = "";
		int maxTokenKeyLength = 10;
		for (int i = 0; i < config_lines.size(); i++) 
		{
			String line = config_lines.get(i);
			
			if (i >= 150)
				line += ""; // Add a breakpoint here to debug a line
			
			if (line.startsWith("##")) { 
				// New token - apply the previous token
				if (!token.equals("")) {
					maxTokenKeyLength = token.length() > maxTokenKeyLength ? token.length() : maxTokenKeyLength;
					token_map.put(token, token_value);
				}

				// Get new token name
				int token_end = line.indexOf("##", 2) + 2; 
				token = line.substring(0, token_end);
				
				// Get new token value
				String token_value_unwrapped = line.replaceAll("##[^#]*##\\s?", "");
				
				// Wrap it at 80 chars
				token_value = token_value_unwrapped;
				// token_value = applyMaxWidth(token_value_unwrapped, 80);
				
/*				boolean wrapped = false;
				if (!token_value.equals(token_value_unwrapped))
					println(" Had to wrap value at 80 characters for token : " + token);
					*/
				
			} else {
				// Add any additional lines to token value
				String wrappedLine = line;
				// String wrappedLine = applyMaxWidth(line, 80);

				token_value += (token_value.equals("") ? "" : NEWLINE) + wrappedLine;
			}
		}
		if (!token.equals(""))
			token_map.put(token, token_value);

		
		// Remove comments
		result = result.replaceAll("%%[^%]*%%\n?", "");
			
		// Replace variables
		println("");
		println("Applying token replacements...\n");
		String arrow = "....................................................................................................";
		for (String token_key : token_map.keySet()) 
		{
			token_value = token_map.get(token_key);
			int arrowSize = maxTokenKeyLength + 5 - token_key.length();
			println("'" + token_key + "' " + arrow.substring(0, arrowSize) + " '" + token_value + "'");
			//String token_pattern = Pattern.quote(token_key);
			//String result2 = result.replaceAll(token_pattern, token_value);
			result = replaceAll(result, token_key, token_value);
		}

		// Remove tabs
		if (tabSize > 0) {
			println("\nSubstituting tab characters with " + tabSize + " space characters...");
			String spaces = "                                          ";
			String tabInSpaces = spaces.substring(0, tabSize);
			result = result.replaceAll("\t", tabInSpaces);
		}
		
		// Wrap lines
		int wrapAt = 80;
		println("\nWrapping lines at " + wrapAt + " characters...");
		String[] resultLines = result.split("\n");
		String wrappedResult = "";
		for (String line : resultLines) {
			String wrappedLine = applyMaxWidth(line, wrapAt);
			wrappedResult += (wrappedResult.equals("") ? "" : NEWLINE) + wrappedLine;
		}
		result = wrappedResult;
		
		// Write output
		println("");
		println("********************************************************************************");
		println("Writing readme...");
		println("********************************************************************************");
		writeTextFile(readme_path, result);
		println(result);
		
		// Write optional HTML output
		if (!readme_html_path.equals("")) {
			String htmlText = html.replace("STUFF", result);
			writeTextFile(readme_html_path, htmlText);
		}
		
	}
	
	public static void println(String s) {
		if (silentMode)
			return;
		else
			System.out.println(s);
	}
	
	/**
	 * @param text
	 * @param width
	 * @return Returns a version of [text] that has an enforced column width of [width], with attempt to preserve indentation, etc.
	 */
	public static String applyMaxWidth(String text, int width) {
		boolean bContainsTabs = text.lastIndexOf('\t') > -1;
		String[] lines = text.split("\\n");
		String result = "";
		// Go through each original line
		for (int i = 0; i < lines.length; i++) {
			String line = lines[i];
			int len = line.length();
			// If this line is longer than width...
			if (len > width )
			{
				// Compute the amount of indentation/padding to use for each subline
				String padding = leftWhitespace(line);
				int paddingLen = padding.length();
				int workingWidth = width - paddingLen;
				
				// Compute the number of characters to pull into this line based on where the last convenient space character is in it
				int charactersPulled = lastSpaceIndexWithinWidth(line, width) + 1;
				
				// Now continually chop down the line given the computed indentation
				String newval = line.substring(0, charactersPulled) + NEWLINE;
				String buffer = text.substring(charactersPulled);
				
				// Check if this line is a list element of some sort and add the additional indent into the padding computations for its child lines
				int listStart = padding.length();
				char firstCharAfterIndent = line.charAt(listStart);
				boolean bIsList = firstCharAfterIndent == '-' || firstCharAfterIndent == '*';
				String listPrefix = bIsList ? "" + firstCharAfterIndent + " ": "";

				// TODO: Should also check for numbered, lettered lists with a regex... 
				if (bIsList) {
					String listElementRegEx = "[ ]*([-]+[ ]?|[*]+[ ]?|[0-9]+\\.[ ]?|[A-Za-z]+[\\.\\)][ ]+?).*"; //|[*]+[ ]?|[0-9]+\\.[ ]+|[0-9]+\\)[ ]+";
					Pattern listElementPattern = Pattern.compile(listElementRegEx, Pattern.CASE_INSENSITIVE);
					Matcher matcher = listElementPattern.matcher(line);
					boolean bMatched = matcher.matches();
					String sMatch = bMatched ? matcher.group() : null;
							
					String additionalPadding = "                     ".substring(0, listPrefix.length());
					padding += additionalPadding;
					paddingLen = padding.length();
					workingWidth = width - paddingLen;
				}
				
				// Now continually chop down the line given the computed indentation
				while (buffer.length() > workingWidth) {
					charactersPulled = lastSpaceIndexWithinWidth(buffer, workingWidth) + 1;
					charactersPulled = charactersPulled == 0 ? workingWidth : charactersPulled; 
					newval += padding + buffer.substring(0, charactersPulled) + NEWLINE; // Preserve indentation when wrapping
					buffer = buffer.substring(charactersPulled);
				}
				newval += padding + buffer;
				lines[i] = newval;
			}			
		}
		List<String> list = Arrays.asList(lines);
		result = join(list);
		return result;
	}

	public static int lastSpaceIndexWithinWidth(String text, int maxWidth) {
		int lastSpaceIndex = text.substring(0, maxWidth).lastIndexOf(' ');
		if (lastSpaceIndex == -1)
			return maxWidth - 1; // REO - no spaces on this line... just cut it at the boundary
		String textBeforeLastSpace = text.substring(0, lastSpaceIndex);
		if (textBeforeLastSpace.trim().equals(""))
			return maxWidth - 1;	// (REO) The only spacing was the indentation... so just sharply cut the line at the boundary.
		else 
			return lastSpaceIndex; 
	}
	
	public static List<String> readTextFileLines(String aFileName) throws IOException {
		File file = new File(aFileName);
		Scanner scanner = new Scanner(file);
		String text = scanner.useDelimiter("\\Z").next();
		scanner.close();
		text.replaceAll("\r\n", "\n");
		text.replaceAll("\n", "\r\n");
		String[] lines = text.split("\r\n");
		List<String> result = Arrays.asList(lines);
		return result;
		// JDK 1.7
		//Path path = Paths.get(aFileName);
	    //return Files.readAllLines(path, ENCODING);
	  }
	
	public static void writeTextFileLines(String aFileName, List<String> aLines) throws IOException {
        
		String text = join(aLines); /*
        for (String line : aLines)
        	text += line + "\n";
		text = text.substring(0, text.length() - 1);
		*/
        writeTextFile(aFileName, text);
		/* 1.7		
		    Path path = Paths.get(aFileName);
		    FileWriter fw = new FileWriter(path.toFile());
		    BufferedWriter writer = new BufferedWriter(fw);
		      for(String line : aLines){
		        writer.write(line);
		        writer.newLine();
		      }
		      writer.close();
		      fw.close();
         */
   }

	public static void writeTextFile(String aFileName, String text) throws IOException {
        FileOutputStream fos = new FileOutputStream(aFileName);
        Writer out = new OutputStreamWriter(fos, "UTF8");
        out.write(text);
        out.close();
        /* 1.7		
		Path path = Paths.get(aFileName);
	    FileWriter fw = new FileWriter(path.toFile());
	    BufferedWriter writer = new BufferedWriter(fw);
	    writer.write(text);
		writer.close();
		fw.close();
		*/
	  }
	
	public static String join(List<String>lines) {
		StringBuffer result = new StringBuffer();
		if (lines.size() == 1)
			return lines.get(0);
		
		for (int i = 0; i < lines.size(); i++) {
			String line = lines.get(i);
			result.append(line);
			if (i < lines.size() - 1)
				 result.append(NEWLINE);
		}
		return result.toString();
	}
	
	public static String replace(String text, String pattern, String replacement) {
		int i =  text.indexOf(pattern);
		if (i < 0)
			return text;
		else {
			String a = text.substring(0, i);
			String b = replacement;
			String c = text.substring(i + pattern.length());
			String result = a + b + c;
			return result;
		}
		
	}
	
	public static String replaceAll(String text, String pattern, String replacement) {
		String before = "";
		String after = text; 
		for (; !before.equals(after); ) {
			before = after;
			after = replace(before, pattern, replacement);
		}
		return after;
	}
	
	public static String leftWhitespace(String line) {
		String result = "";
		for (int i = 0; i < line.length(); i++) {
			if (line.charAt(i) == ' ' || line.charAt(i) == '\t')
				result += line.charAt(i);
			else
				break;
		}
		return result;
	}
}
