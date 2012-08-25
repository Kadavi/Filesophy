package com.filesophy.src;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class UploadController {
	@RequestMapping(value = "/upload", method = RequestMethod.GET)
	public @ResponseBody 
	void onUploadGet(HttpServletRequest request, HttpServletResponse response) {
		try {
			// Load headers that help determine whether or not to accept request
			HashMap<String, Object> headers = FilesophyUtil.extractHeadersFrom(request, "GET");		
			String filename = headers.get("resumableFilename").toString();
			String chunk = headers.get("resumableChunkNumber").toString();
			
			// 'No thanks' if file part already exists on disk
			if (new File(filename + "-" + chunk).exists()) {
				response.setStatus(HttpServletResponse.SC_OK);
			} else {
				// Send the part because it is a missing piece
				response.setStatus(HttpServletResponse.SC_NOT_FOUND);
			}
			
			response.flushBuffer();
		} catch (Exception e) {
			// On error, try sending again
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
			e.printStackTrace();
		}
	}
	
	@RequestMapping(value = "/upload", method = RequestMethod.POST)
	public @ResponseBody
	void onUploadPost(HttpServletRequest request, HttpServletResponse response) {
		try {
			// Load 'headers' up with useful hash keys and values
			final HashMap<String, Object> headers = FilesophyUtil.extractHeadersFrom(request, "POST");
			
			// An 'iter' holds most of the multipart file info
			Iterator iter = (Iterator) headers.get("multipartFileIterator");
			while (iter.hasNext()) {
				// Each 'item' is either metadata or the actual bytes of a file part
				FileItem item = (FileItem) iter.next();
				if (item.isFormField()) {
					// Capture important values into hash if it contains metadata
					if (item.getFieldName().equals("resumableFilename")) {
						headers.put("resumableFilename", item.getString());
					} else if (item.getFieldName().equals("resumableChunkNumber")) {
						headers.put("resumableChunkNumber", item.getString());
					} else if (item.getFieldName().equals("resumableTotalSize")) {
						headers.put("resumableTotalSize", item.getString());
					} else if (item.getFieldName().equals("resumableChunkSize")) {
						headers.put("resumableChunkSize", item.getString());
					}
				} else {
					// File processing begins in here
					String filename = headers.get("resumableFilename").toString();
					String chunk = headers.get("resumableChunkNumber").toString();
					String totalSize = headers.get("resumableTotalSize").toString();
					String chunkSize = headers.get("resumableChunkSize").toString();
					headers.put("resumableTotalChunks", (Long.valueOf(totalSize) 
							/ Long.valueOf(chunkSize)) == 0 ? 1 : (Long.valueOf(totalSize) 
							/ Long.valueOf(chunkSize)));
					int totalChunks = Integer.valueOf(headers.get("resumableTotalChunks").toString());
					
					// The previous variables prepare us for the upcoming file writing
					File uploadedFile = new File(FilesophyUtil.TEMPDIR + filename + "-" + chunk);
					FileOutputStream output = new FileOutputStream(uploadedFile, false);
					
					// Make a file with a name of 'name.xxx-#' (ie. 'My Video.avi-3')
					if ((new File(filename + "-" + chunk).exists()) == false) {
						output.write(item.get());
					}
					
					// Now count how many parts have been written
					File[] files = new File(FilesophyUtil.TEMPDIR).listFiles(new FilenameFilter() {
						public boolean accept(File dir, String name) {
							return name.startsWith(headers.get("resumableFilename").toString());
						}
					});
					
					// If all parts exist then we are ready to snap them together
					if (files.length == totalChunks) {
						FilesophyUtil.combineFileParts(headers);
					}
				}
			}
			
			// Happy ending, ready for next file chunk
			response.setStatus(HttpServletResponse.SC_OK);
			response.flushBuffer();
		} catch (Exception e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			e.printStackTrace();
		}
	}
	
	/*
	 *   [Big Thanks Shoutout] Method provided by :
	 *   http://www.java-forums.org/blogs/servlet/668-how-write-servlet-sends-file-user-download.html
	 */
	@RequestMapping(value = "/download", method = RequestMethod.GET)
	public void download(HttpServletRequest request,
			HttpServletResponse response, @RequestParam String id)
			throws ServletException, IOException {
		String targetToDL = FilesophyUtil.TEMPDIR + id;
		File file = new File(targetToDL);
		int length = 0;
		ServletOutputStream outStream = response.getOutputStream();
		ServletContext context = request.getServletContext();
		String mimetype = context.getMimeType(targetToDL);
		if (mimetype == null) {
			mimetype = "application/octet-stream";
		}
		response.setContentType(mimetype);
		response.setContentLength((int) file.length());
		String fileName = (new File(targetToDL)).getName();
		response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
		byte[] byteBuffer = new byte[FilesophyUtil.BUFSIZE];
		DataInputStream in = new DataInputStream(new FileInputStream(file));
		while ((in != null) && ((length = in.read(byteBuffer)) != -1)) {
			outStream.write(byteBuffer, 0, length);
		}
		in.close();
		outStream.close();
	}
	
	// Roughly catches java.lang.Exception and mapped from web.xml
	@RequestMapping(value = "/exception")
	public @ResponseBody 
	void onServletException(HttpServletRequest request, HttpServletResponse response) {
		Throwable throwable = (Throwable) request.getAttribute(RequestDispatcher.ERROR_EXCEPTION);
		FilesophyUtil.logger.debug("An exception was caught: " + throwable.getCause());
	}
	
	@RequestMapping(value = "/faq", method = RequestMethod.GET)
	public String faq(HttpServletRequest request,
			HttpServletResponse response, ModelMap map) {
		return "faq";
	}
	
	// A debug page that shows files currently inside FilesophyUtil.TEMPDIR and their sizes
	@RequestMapping(value = "/debug", method = RequestMethod.GET)
	public String testy(HttpServletRequest request,
			HttpServletResponse response, ModelMap map) {
		File file = new File(FilesophyUtil.TEMPDIR);
		File[] files = file.listFiles();
		map.addAttribute("fileList", files);
		return "debug";
	}
}