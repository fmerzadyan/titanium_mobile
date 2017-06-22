package org.appcelerator.titanium.io;

import java.io.File;

public class TiVirtualFile {
	private File mFile;
	
	public TiVirtualFile(final File file) {
		mFile = file;
	}
	
	public TiVirtualFile() {
		
	}
	
	public File getFile() {
		return mFile;
	}
}
