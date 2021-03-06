package com.multi.aiservice;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.UUID;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

@Service
public class OCRService {
	public String clovaOCRService(String filePathName) {
		
		String result = "";
		
		String apiURL = "";
		String secretKey = "";
		String imageFile = filePathName;		

		try {
			URL url = new URL(apiURL);
			HttpURLConnection con = (HttpURLConnection)url.openConnection();
			con.setUseCaches(false);
			con.setDoInput(true);
			con.setDoOutput(true);
			con.setReadTimeout(30000);
			con.setRequestMethod("POST");
			String boundary = "----" + UUID.randomUUID().toString().replaceAll("-", "");
			con.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
			con.setRequestProperty("X-OCR-SECRET", secretKey);

			JSONObject json = new JSONObject();
			json.put("version", "V2");
			json.put("requestId", UUID.randomUUID().toString());
			json.put("timestamp", System.currentTimeMillis());
			JSONObject image = new JSONObject();
			image.put("format", "jpg");
			image.put("name", "demo");
			JSONArray images = new JSONArray();
			images.put(image);
			json.put("images", images);
			String postParams = json.toString();

			con.connect();
			DataOutputStream wr = new DataOutputStream(con.getOutputStream());
			long start = System.currentTimeMillis();
			File file = new File(imageFile);
			writeMultiPart(wr, postParams, file, boundary);
			wr.close();

			int responseCode = con.getResponseCode();
			BufferedReader br;
			if (responseCode == 200) {
				br = new BufferedReader(new InputStreamReader(con.getInputStream()));
			} else {
				br = new BufferedReader(new InputStreamReader(con.getErrorStream()));
			}
			String inputLine;
			StringBuffer response = new StringBuffer();
			while ((inputLine = br.readLine()) != null) {
				response.append(inputLine);
			}
			br.close();

			System.out.println(response);  //JSON ????????? ????????? ??????			
			System.out.println(response.toString()); 
			
			result = jsonToString(response.toString()); // ?????? ??????
			System.out.println(result);
		} catch (Exception e) {
			System.out.println(e);
		}
		
		return result;
	}
	
	private static void writeMultiPart(OutputStream out, String jsonMessage, File file, String boundary) throws
	IOException {
	StringBuilder sb = new StringBuilder();
	sb.append("--").append(boundary).append("\r\n");
	sb.append("Content-Disposition:form-data; name=\"message\"\r\n\r\n");
	sb.append(jsonMessage);
	sb.append("\r\n");

	out.write(sb.toString().getBytes("UTF-8"));
	out.flush();

	if (file != null && file.isFile()) {
		out.write(("--" + boundary + "\r\n").getBytes("UTF-8"));
		StringBuilder fileString = new StringBuilder();
		fileString
			.append("Content-Disposition:form-data; name=\"file\"; filename=");
		fileString.append("\"" + file.getName() + "\"\r\n");
		fileString.append("Content-Type: application/octet-stream\r\n\r\n");
		out.write(fileString.toString().getBytes("UTF-8"));
		out.flush();

		try (FileInputStream fis = new FileInputStream(file)) {
			byte[] buffer = new byte[8192];
			int count;
			while ((count = fis.read(buffer)) != -1) {
				out.write(buffer, 0, count);
			}
			out.write("\r\n".getBytes());
		}

		out.write(("--" + boundary + "--\r\n").getBytes("UTF-8"));
	}
	out.flush();
 }
	
	// ??????????????? ????????? ?????? 
	// JSON ????????? ??????????????? ???????????? ???????????? ????????? ?????? : inferText ??? ??????
	public String jsonToString(String jsonStr) {
		String resultText = "";
		
		// ????????? ???????????? : images , fields, inferText??? ???
		JSONObject jsonObj = new JSONObject(jsonStr);
		
		//jsonObj?????? images  ?????? : ????????? 
		JSONArray imgArray = (JSONArray)jsonObj.get("images");
		//???????????? ????????? 1????????? ???????????? index??? 0?????? ??????
		JSONObject imgObj =  (JSONObject)imgArray.get(0);
		
		// fields ?????? : ?????????
		JSONArray fieldsArray = (JSONArray) imgObj.get("fields");
		
		if(fieldsArray != null) {
			for(int i=0; i<fieldsArray.length(); i++) {  //size()??? ????????? length() (org.json.JSONArray??????)
				JSONObject tempObj = (JSONObject)fieldsArray.get(i);
				String str = (String)tempObj.get("inferText");
				resultText += str + " ";
			}
		}else {
			System.out.println("??????");
		}		
		return resultText;
	}
	
}







