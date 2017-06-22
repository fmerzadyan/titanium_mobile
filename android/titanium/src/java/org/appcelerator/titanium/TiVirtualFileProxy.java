package org.appcelerator.titanium;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.frankify.f;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.DriveApi.DriveContentsResult;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.Metadata;
import com.google.android.gms.drive.MetadataChangeSet;
import com.google.android.gms.drive.query.Filters;
import com.google.android.gms.drive.query.Query;
import com.google.android.gms.drive.query.SearchableField;
import org.appcelerator.kroll.KrollProxy;
import org.appcelerator.kroll.annotations.Kroll;
import org.appcelerator.titanium.io.TiFile;
import org.appcelerator.titanium.io.TiFileFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

@Kroll.proxy
public class TiVirtualFileProxy extends KrollProxy implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
	private static final String TAG = "TiVirtualFileProxy";
	private GoogleApiClient mGoogleApiClient;
	// Arbitrary unique identifier for verifying request identity used in <same package>.TiBaseActivity.
	static final int RESOLUTION_REQUEST_CODE = 101;
	private TiFile mTiFile;

	public TiVirtualFileProxy() {
		initGoogleApiClient();
	}

	private Context getApplicationContext() {
		return TiApplication.getInstance().getApplicationContext();
	}

	private Activity getCurrentActivity() {
		return TiApplication.getInstance().getCurrentActivity();
	}
	
	private GoogleApiClient getGoogleApiClient() {
		if (mGoogleApiClient == null) {
			initGoogleApiClient();
		}
		return mGoogleApiClient;
	}

	public boolean isConnectingOrConnected() {
		return getGoogleApiClient().isConnecting() || getGoogleApiClient().isConnected();
	}

	private void initGoogleApiClient() {
		mGoogleApiClient = new GoogleApiClient.Builder(getApplicationContext())
			.addApi(Drive.API)
			.addScope(Drive.SCOPE_FILE)
			.addScope(Drive.SCOPE_APPFOLDER)
			.addConnectionCallbacks(this)
			.addOnConnectionFailedListener(this)
			.build();
		mGoogleApiClient.connect();
	}

	// FTODO Get folders location e.g. RootFolder/First/Second/File.extension. Create "First" folder in root then "Second" folder in "First" folder
	// FTODO then finally create "File.extension". The "/" separates the folders so they are delimiters.
	public void createFile(final String filename, final String mimeType) {
		String[] folders = filename.split("/");
		f.log("folder:", folders);
		
		Drive.DriveApi.newDriveContents(getGoogleApiClient()).setResultCallback(new ResultCallback<DriveContentsResult>() {
			@Override
			public void onResult(@NonNull DriveContentsResult driveContentsResult) {
				f.log("newDriveContents#onResult");
				if (!driveContentsResult.getStatus().isSuccess()) {
					f.log("driveContentsResult.getStatus.isSuccess");
					return;
				}
				f.log("driveContentsResult.getStatus.isSuccess");
				final DriveContents driveContents = driveContentsResult.getDriveContents();
				
				new Thread(new Runnable() {
					@Override
					public void run() {
						f.log();
						OutputStream outputStream = driveContents.getOutputStream();
						Writer writer = new OutputStreamWriter(outputStream);
						// FTODO: Remove fileContents
						String fileContent = "Lorem ipsum dolor sit amet, consectetur adipisicing elit, sed do eiusmod\n" +
								"tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam,\n" +
								"quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo\n" +
								"consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse\n" +
								"cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non\n" +
								"proident, sunt in culpa qui officia deserunt mollit anim id est laborum.";
						fileContent = "Lorem...";
						try {
							writer.write(fileContent);
						} catch (Exception e) {
							f.threwException(e);
						}
						
						MetadataChangeSet metadataChangeSet = new MetadataChangeSet.Builder()
							.setTitle(filename)
							.setMimeType(mimeType)
							.build();
						
						Drive.DriveApi.getRootFolder(getGoogleApiClient())
							.createFile(getGoogleApiClient(), metadataChangeSet, driveContents)
							// FTODO: Move callbacks to outside Thread object to prevent Thread objects from being kept alive unnecessarily
							.setResultCallback(new ResultCallback<DriveFolder.DriveFileResult>() {
							@Override
							public void onResult(@NonNull DriveFolder.DriveFileResult driveFileResult) {
								if (!driveFileResult.getStatus().isSuccess()) {
									f.log("File creation unsuccessful");
									return;
								}
								f.log("File creation successful");
							}
						});
					}
				}).start();
			}
		});
	}
	
	public void deleteFile(final String filename, final String mimeType) {
		Query query;
		if (mimeType == null) {
			query = new Query.Builder()
					.addFilter(Filters.eq(SearchableField.TITLE, filename))
					.build();
		} else {
			query = new Query.Builder()
					.addFilter(Filters.eq(SearchableField.TITLE, filename))
					.addFilter(Filters.eq(SearchableField.MIME_TYPE, mimeType))
					.build();
		}
		
		Drive.DriveApi.query(getGoogleApiClient(), query).setResultCallback(new ResultCallback<DriveApi.MetadataBufferResult>() {
			@Override
			public void onResult(@NonNull DriveApi.MetadataBufferResult metadataBufferResult) {
				if (!metadataBufferResult.getStatus().isSuccess()) {
					f.log("!metadataBufferResult.getStatus.isSuccess");
					return;
				}
				f.log("metadataBufferResult.getStatus.isSuccess");
				int count = metadataBufferResult.getMetadataBuffer().getCount();
				f.log("count: " + count);
				// FTODO: Remove count =1;
				count = 1;
				if (count == 0) {
					
				} else if (count == 1) {
					Metadata metadata = metadataBufferResult.getMetadataBuffer().get(0);
					DriveFile driveFile = metadata.getDriveId().asDriveFile();
					driveFile.delete(getGoogleApiClient()).setResultCallback(new ResultCallback<Status>() {
						@Override
						public void onResult(@NonNull Status status) {
							f.log("status.isSuccess: " + status.isSuccess());
						}
					});
				} else if (count >= 2) {
					
				} else {
					
				}
			}
		});
	}

	public void retrieveFile(final String filename, final String mimeType) {
		// FTODO support complex queries?
		Query query;
		if (mimeType == null) {
			query = new Query.Builder()
				.addFilter(Filters.eq(SearchableField.TITLE, filename))
				.build();
		} else {
			query = new Query.Builder()
				.addFilter(Filters.eq(SearchableField.TITLE, filename))
				.addFilter(Filters.eq(SearchableField.MIME_TYPE, mimeType))
				.build();
		}
		final DriveId[] driveId = new DriveId[1];
		Drive.DriveApi.query(getGoogleApiClient(), query).setResultCallback(new ResultCallback<DriveApi.MetadataBufferResult>() {
			@Override
			public void onResult(@NonNull DriveApi.MetadataBufferResult metadataBufferResult) {
				if (metadataBufferResult.getStatus().isSuccess()) {
					f.log("metadataBufferResult is success");
					int count = metadataBufferResult.getMetadataBuffer().getCount();
					f.log("count: " + count);
					// FTODO: Remove count = 1. Also multiple records logic block.
					count = 1;
					if (count == 0) {
						// No records
					} else if (count == 1) {
						// One unique record
						Metadata metadata = metadataBufferResult.getMetadataBuffer().get(0);
						driveId[0] = metadata.getDriveId();
						f.log("file size (long): " + metadata.getFileSize());
						DataCollection dataCollection = new DataCollection(driveId[0], metadata.getFileSize());
						new RetrieveDriveFileContentsAsyncTask().execute(dataCollection);
					} else if (count >= 2) {
						// Multiple records matching pattern
						f.log("Multiple files. Not supported yet.");
					} else {
						// Negative
					}
				} else {
					f.log("metadataBufferResult is not success");
				}
			}
		});
	}

	@Override
	public void onConnected(@Nullable Bundle bundle) {
		
	}

	@Override
	public void onConnectionSuspended(int i) {
		
	}

	@Override
	public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
		if (connectionResult.hasResolution()) {
			try {
				connectionResult.startResolutionForResult(getCurrentActivity(), RESOLUTION_REQUEST_CODE);
			} catch (Exception e) {
				f.threwException(e);
			}
			return;
		}
		
		// There is no resolution for connectionResult.getErrorCode.
	}

	final private class RetrieveDriveFileContentsAsyncTask extends AsyncTask<DataCollection, Boolean, String> {
		@Override
		protected String doInBackground(DataCollection... dataCollections) {
			f.log();
			String contents;
			
			DriveFile driveFile = dataCollections[0].getDriveId().asDriveFile();
			f.nullCheck("driveFile", driveFile);
			
			// DriveFile.MODE_READ_ONLY only returns InputStream.
			DriveContentsResult driveContentsResult = 
					driveFile.open(getGoogleApiClient(), DriveFile.MODE_READ_ONLY, null).await();
			if (!driveContentsResult.getStatus().isSuccess()) {
				f.log("File retrieval unsuccessful");
				return null;
			}
			DriveContents driveContents = driveContentsResult.getDriveContents();
			
			// driveContents.getInputStream can only be called once per Contents instance so keep as variable.
			InputStream inputStream = driveContents.getInputStream();

			//FTODO: Remove readFileContents. Only used for testing to see contents.
			// Contents is passed to onPostExecute.
			contents = readFileContents(inputStream);
			

			
			// mTiFile = new TiFile(new File(driveContents.getDriveId().getResourceId()), TiFileFactory.getDataDirectory(true).getPath(), false);
			// File outputFile = mTiFile.getNativeFile();
			String path = TiFileFactory.getDataDirectory(true).getPath() + "/" + driveContents.getDriveId().getResourceId();
			File outputFile = new File(path);
			f.log("outputFile path: " + outputFile.getPath());
			
			try {
				OutputStream outputStream = new FileOutputStream(outputFile);
				int bytesRead;
				byte[] dataBytes;
				long byteArraySize = dataCollections[0].getFileSize();
				f.log(byteArraySize + "");
				// Casting long to int cause an issue if the number is too large to put into an int type.
				if (byteArraySize >= Integer.MAX_VALUE) {
					f.log("byteArraySize is too large: " + byteArraySize);
					return null;
				}
				dataBytes = new byte[(int) byteArraySize];
				try {
					while ((bytesRead = inputStream.read(dataBytes, 0, dataBytes.length)) != -1) {
						outputStream.write(dataBytes, 0, bytesRead);
					}
				} catch (IOException e) {
					f.threwException(e);
				} finally {
					try {
						inputStream.close();
					} catch (IOException e) {
						// ignore
					}
					try {
						outputStream.close();
					} catch (IOException e) {
						// ignore
					}
				}
				mTiFile = new TiFile(outputFile, TiFileFactory.getDataDirectory(true).getPath(), true);
				f.log("mTiFile exists: " + mTiFile.exists() + " path: " + mTiFile.getNativeFile().getPath());
			} catch (FileNotFoundException e) {
				f.threwException(e);
			}
			// FTODO File does exist but you still need to test if contents are written to local file.
			f.log("outputFile exists: " + outputFile.exists());

			driveContents.discard(getGoogleApiClient());
			return contents;
		}

		@Override
		protected void onPostExecute(String result) {
			if (result == null) {
				f.log("Error while reading from file");
				return;
			}
			f.log("File Contents: " + result);
		}
	}

	/**
	 * Data collection used to pass collection of arguments into RetrieveDriveFileContentsAsyncTask.execute(). Holds the drive id and file size.
	 */
	private class DataCollection {
		private DriveId mDriveId;
		private long mFileSize;
		DataCollection(DriveId driveId, long fileSize) {
			mDriveId = driveId;
			mFileSize = fileSize;
		}
		
		DriveId getDriveId() {
			return mDriveId;
		}
		
		long getFileSize() {
			return mFileSize;
		}
	}
	
	// FTODO: Remove readFileContents. Only used for testing to see contents.
	private String readFileContents(InputStream inputStream) {
		BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
		StringBuilder builder = new StringBuilder();
		String line;
		try {
			while ((line = reader.readLine()) != null) {
				builder.append(line);
			}
			return builder.toString();
		} catch (IOException e) {
			f.threwException(e);
		}
		return null;
	}
}