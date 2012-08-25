package com.filesophy.src;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class FilesophyUtil {
	public static final String TEMPDIR = "/tmp/";
	//public static final String TEMPDIR = "/Users/landstar/Desktop/temp/";
	public static final int BUFSIZE = 4096;
	public static final Logger logger = LoggerFactory.getLogger(UploadController.class);
	
	public static void combineFileParts(final HashMap<String, Object> map) throws IOException {
		// Search directory for file parts matching original filename
		File[] files = new File(TEMPDIR).listFiles(new FilenameFilter() {
				public boolean accept(File dir, String name) {
				return name.startsWith(map.get("resumableFilename").toString());
			}
		});
		
		// Shove file parts into a hash along with their respective index values
		HashMap<Integer, File> refinedFileList = new HashMap<Integer, File>();
		for (File f : files) {
			int pos = f.getName().lastIndexOf("-");
			int numericSuffix = Integer.valueOf(f.getName().substring(pos+1));
			refinedFileList.put(numericSuffix, f);
		}
		
		// Call the files stored in the hash in numerical order and append it to an ultimate file
		File finalFile = new File(TEMPDIR + map.get("resumableFilename"));
		for (int i = 1; i <= Integer.valueOf(map.get("resumableTotalChunks").toString()); i++) {
			FileUtils.writeByteArrayToFile(finalFile, FileUtils.readFileToByteArray(refinedFileList.get(i)), true);
		}
	}

	public static HashMap<String, Object> extractHeadersFrom(final HttpServletRequest request, String requestType) throws FileUploadException {
		// Populate hash with HTTP header values for later use
		HashMap<String, Object> headers = new HashMap<String, Object>();
		
		// Called from controller's GET method
		if (requestType == "GET") {
			Enumeration<String> paramNames = request.getParameterNames();
			String paramKey, paramValue;
			while (paramNames.hasMoreElements()) {
				paramKey = (String) paramNames.nextElement();
				paramValue = request.getParameter(paramKey);
				headers.put(paramKey, paramValue);
			}
			
			// Calculate and add additional values; file is the actual file and 
			// resumableTotalChunks is used to determine how many file parts there are
			long totalChunks = Long.valueOf((String) headers.get("resumableTotalSize")) / Long.valueOf((String) headers.get("resumableChunkSize"));
			headers.put("file", new File(FilesophyUtil.TEMPDIR + headers.get("resumableFilename")));
			headers.put("resumableTotalChunks", totalChunks == 0 ? 1 : totalChunks);
		} else if (requestType == "POST") {
			// Prepare Apache Commons IO to process a multipart file POST
			FileItemFactory factory = new DiskFileItemFactory();
			ServletFileUpload upload = new ServletFileUpload(factory);
			List items = upload.parseRequest(request);
			Iterator iter = items.iterator();
			headers.put("multipartFileIterator", iter);
		} else {
			// Was neither a GET or POST
			throw new IllegalArgumentException("Either GET or POST must be passed as an argument to " +
					"FilesophyUtil.extractHeadersFrom(HttpServletRequest request, String requestType).");
		}
		
		return headers;
	}
}
