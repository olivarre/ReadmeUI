import java.util.HashMap;

/**
 * @author ROBERTO ENRIQUE OLIVARES
 *
 *	Loads the file paths and file information (for a possibly remote directory) on a separate (non-UI) thread.
 *	This allows the program to start and UI to be functional despite file system delays.
 *
 */
public class ThreadedFileReader {

	final HashMap<String, Thread> threads = new HashMap<String, Thread>();					// threads to wait on in waitForXXX method 
	final HashMap<String, String> paths = new HashMap<String, String>(); 					// paths we're waiting on to finish loading
	
	final public HashMap<String, String[]> results = new HashMap<String, String[]>(); 		// where we store the results of the loads

	// Event handler we're given to call back
	final ThreadedFileReader me = this;
	IThreadedFileReaderEvents eventHandler = null;
	Object eventResult = null;

	// Keys into the results hashmap for things we load
	public final static String KEY_CONFIGS_SORTED_BY_MODIFIED_TIME = "Configurations-Sorted-By-Modified-Time";
	public final static String KEY_CONFIGURATION_FILES_SORTED_BY_NAME = "Configurations";
	public final static String KEY_TEMPLATE_FILES = "Templates";
	public final static String KEY_OUTPUT_FILES = "Output";

	// Events Interface
	public interface IThreadedFileReaderEvents {
		public abstract void 		onShowWaitAgainDialog	(ThreadedFileReader reader, long timeElapsed);
		public abstract void 		onDirectoryLoaded		(ThreadedFileReader reader, String key, String path, String[] fileNames);
	}

	public ThreadedFileReader(IThreadedFileReaderEvents iThreadedFileReaderEvents) {
		eventHandler = iThreadedFileReaderEvents;
	}

	public void startLoadAllDirectories(String workingDirectory) {
		// kick off threads to load all three directories simultaneously
		startDirectoryLoadThread("Templates", 		workingDirectory + "\\" + ReadmeUI.templateSubdir);
		startDirectoryLoadThread("Configurations", 	workingDirectory + "\\" + ReadmeUI.configSubdir);
		startDirectoryLoadThread("Output", 			workingDirectory + "\\" + ReadmeUI.outputSubdir);
	}

	public void startDirectoryLoadThread(final String key, final String path) {
		Thread thread = new Thread(new Runnable() 		{ 
			@Override public void run() { 
				try {
					String[] fileNames = Utils.getDirectoryListing(path, true);
					results.put(key, fileNames);
					threads.remove(key);
					fireOnDirectoryLoaded(key, path, fileNames);
					
				} catch (Exception e) {
					e.printStackTrace();
				}
				threads.remove(key);
			} 
		});
		paths.put(key, path);																							// register this path we're looking for
		threads.put(key, thread);																						// register this thread we're using
		results.put(key, new String[0]);																				// start with an empty return value
		thread.start();																									// run this thread
	}

	public void startModifiedTimesCalculatorThread(final String key, final String path) {
		Thread thread = new Thread(new Runnable() { 																	// thread code to run continuously
			@Override public void run() { 
				// Never-terminating sorter thread that periodically puts updated <key, pathsSortedByModifiedTime[]> entry into results.
				try {
					String[] sortedFileNames = Utils.getDirectoryListingSortedByLastModifiedTime(path, true);		// list directory  
					results.put(key, sortedFileNames);																// save results
					fireOnDirectoryLoaded(key, path, sortedFileNames);												// notify event handler

				} catch (Exception e) {
					e.printStackTrace();
				}
				threads.remove(key);
			}
		});
		paths.put(key, path);																							// register this path we're looking for
		threads.put(key, thread);																						// register this thread we're using
		results.put(key, new String[0]);																				// start with an empty return value
		thread.start();																									// run this thread
	}

	// currently not used due to new event handler methodology
	/*
	private boolean waitForFileListingRefreshThreads(long maxMillis, boolean bAllowRetryDialog) {
		long timeElapsed = 0;
		long millisPerSleep = 10;
		try {
			while(true) {
				Thread.sleep(millisPerSleep);
				timeElapsed += millisPerSleep;
				boolean bDone = threads.size() == 0;
				if (bDone) {
					return true;
				}
				if (bAllowRetryDialog && timeElapsed > maxMillis) {
					fireOnShowWaitAgainDialog(timeElapsed);
					boolean bWaitAgain = getEventResult(true);
					timeElapsed = 0;
					if (bWaitAgain)
						continue;
				}
				return (false);
			}
		} catch (InterruptedException e) {
			return false;
		}
	}
	*/

	public String getPath(String key) {
		return paths.get(key);
	}

	public String[] getResults(String key) {
		return results.get(key);
	}
	
	private boolean getEventResult(boolean defaultValue) {
		return eventResult != null ? (Boolean)eventResult : defaultValue;
	}

	public void setEventResult(boolean newValue) {
		eventResult = newValue;
	}

	private void fireOnDirectoryLoaded(final String key, final String path, final String[] sortedFileNames) {
		Utils.runInUI(new Runnable() {
			@Override
			public void run() {
				eventHandler.onDirectoryLoaded(me, key, path, sortedFileNames);									// notify event handler
			}
		});
	} 
	
	private void fireOnShowWaitAgainDialog(final long timeElapsed) {
		Utils.runInUI(new Runnable() {
			@Override
			public void run() {
				eventHandler.onShowWaitAgainDialog(me, timeElapsed);
			}
		});
	}
}