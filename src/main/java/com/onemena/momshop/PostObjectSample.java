/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.onemena.momshop;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.activation.MimetypesFileTypeMap;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;
import org.nutz.lang.Streams;

import com.onemena.momshop.module.ImageMakerModule;
import com.xice7.image.filter.PointFilter;
import com.xice7.image.kit.ImageKit;

/**
 * This sample demonstrates how to post object under specfied bucket from Aliyun
 * OSS using the OSS SDK for Java.
 */
public class PostObjectSample {
	static class MomShopColorFilter extends PointFilter {

		private final int min = 62;
		private final int max = 245;
		private final float p = 255.0f / (max - min);

		@Override
		public int filterRGB(int x, int y, int rgb) {
			return ((255 & 0xFF) << 24) | ((trans((rgb >> 16) & 0xff) & 0xFF) << 16) | ((trans((rgb >> 8) & 0xff) & 0xFF) << 8) | ((trans(rgb & 0xff) & 0xFF) << 0);
		}

		private int trans(int i) {
			i -= min;
			i = i < 0 ? 0 : i;
			i = (int) (i * p + 0.5);
			return i > 255 ? 255 : i;
		}

	}
	// The local file path to upload.
	private String localFilePath = "/Users/kouxian/Desktop/WechatIMG1364.jpeg";
	private static String endpoint = "http://oss-eu-central-1.aliyuncs.com";
	private static String accessKeyId = "LTAIhPVy1OEfeAYW";
	private static String accessKeySecret = "3p8G8COzewDxYFvPbfmdNExvTC2cIq";
	private static String bucketName = "momshop-test";
	// The key name for the file to upload.
	private String key = "file";
	private final static MomShopColorFilter filter = new MomShopColorFilter();
	
	private void PostObject() throws Exception {
		String urlStr = endpoint.replace("http://", "http://" + bucketName + ".");

		// form fields
		Map<String, String> formFields = new LinkedHashMap<String, String>();
		// key
		formFields.put("key", this.key);
		// Content-Disposition
		formFields.put("Content-Disposition", "attachment;filename=" + localFilePath);
		// OSSAccessKeyId
		formFields.put("OSSAccessKeyId", accessKeyId);
		// policy
		byte[] bytes = Streams.readBytesAndClose(Streams.fileIn("/Users/kouxian/Desktop/WechatIMG1364.jpeg"));
		ImageKit imageKit = ImageKit.read(bytes);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		imageKit.doFilter(filter).transferTo(out, imageKit.getFormat());
		ImageKit kit = ImageKit.read(out.toByteArray());
		
		String policy = "{\"image\": \""+kit.toBase64()+"\"}";
		String encodePolicy = new String(Base64.encodeBase64(policy.getBytes()));
		formFields.put("policy", encodePolicy);
		// Signature
		String signaturecom = computeSignature(accessKeySecret, encodePolicy);
		formFields.put("Signature", signaturecom);

		String ret = formUpload(urlStr, formFields, localFilePath);

		System.out.println("Post Object [" + this.key + "] to bucket [" + bucketName + "]");
		System.out.println("post reponse:" + ret);
	}

	private static String computeSignature(String accessKeySecret, String encodePolicy) throws UnsupportedEncodingException, NoSuchAlgorithmException, InvalidKeyException {
		// convert to UTF-8
		byte[] key = accessKeySecret.getBytes("UTF-8");
		byte[] data = encodePolicy.getBytes("UTF-8");

		// hmac-sha1
		Mac mac = Mac.getInstance("HmacSHA1");
		mac.init(new SecretKeySpec(key, "HmacSHA1"));
		byte[] sha = mac.doFinal(data);

		// base64
		return new String(Base64.encodeBase64(sha));
	}

	private static String formUpload(String urlStr, Map<String, String> formFields, String localFile) throws Exception {
		String res = "";
		HttpURLConnection conn = null;
		String boundary = "9431149156168";

		try {
			URL url = new URL(urlStr);
			conn = (HttpURLConnection) url.openConnection();
			conn.setConnectTimeout(5000);
			conn.setReadTimeout(30000);
			conn.setDoOutput(true);
			conn.setDoInput(true);
			conn.setRequestMethod("POST");
			conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows; U; Windows NT 6.1; zh-CN; rv:1.9.2.6)");
			conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
			OutputStream out = new DataOutputStream(conn.getOutputStream());

			// text
			if (formFields != null) {
				StringBuffer strBuf = new StringBuffer();
				Iterator<Entry<String, String>> iter = formFields.entrySet().iterator();
				int i = 0;

				while (iter.hasNext()) {
					Entry<String, String> entry = iter.next();
					String inputName = entry.getKey();
					String inputValue = entry.getValue();

					if (inputValue == null) {
						continue;
					}

					if (i == 0) {
						strBuf.append("--").append(boundary).append("\r\n");
						strBuf.append("Content-Disposition: form-data; name=\"" + inputName + "\"\r\n\r\n");
						strBuf.append(inputValue);
					} else {
						strBuf.append("\r\n").append("--").append(boundary).append("\r\n");
						strBuf.append("Content-Disposition: form-data; name=\"" + inputName + "\"\r\n\r\n");
						strBuf.append(inputValue);
					}

					i++;
				}
				out.write(strBuf.toString().getBytes());
			}

			// file
			File file = new File(localFile);
			String filename = file.getName();
			String contentType = new MimetypesFileTypeMap().getContentType(file);
			if (contentType == null || contentType.equals("")) {
				contentType = "application/octet-stream";
			}

			StringBuffer strBuf = new StringBuffer();
			strBuf.append("\r\n").append("--").append(boundary).append("\r\n");
			strBuf.append("Content-Disposition: form-data; name=\"file\"; " + "filename=\"" + filename + "\"\r\n");
			strBuf.append("Content-Type: " + contentType + "\r\n\r\n");

			out.write(strBuf.toString().getBytes());

			DataInputStream in = new DataInputStream(new FileInputStream(file));
			int bytes = 0;
			byte[] bufferOut = new byte[1024];
			while ((bytes = in.read(bufferOut)) != -1) {
				out.write(bufferOut, 0, bytes);
			}
			in.close();

			byte[] endData = ("\r\n--" + boundary + "--\r\n").getBytes();
			out.write(endData);
			out.flush();
			out.close();

			// Gets the file data
			strBuf = new StringBuffer();
			BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			String line = null;
			while ((line = reader.readLine()) != null) {
				strBuf.append(line).append("\n");
			}
			res = strBuf.toString();
			reader.close();
			reader = null;
		} catch (Exception e) {
			System.err.println("Send post request exception: " + e);
			throw e;
		} finally {
			if (conn != null) {
				conn.disconnect();
				conn = null;
			}
		}

		return res;
	}

	public static void main(String[] args) throws Exception {
		//PostObjectSample ossPostObject = new PostObjectSample();
		//ossPostObject.PostObject();
		byte[] bytes = Streams.readBytesAndClose(Streams.fileIn("/Users/kouxian/Desktop/WechatIMG1364.jpeg"));
		ImageKit imageKit = ImageKit.read(bytes);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		imageKit.doFilter(filter).transferTo(out, imageKit.getFormat());
		ImageKit kit = ImageKit.read(out.toByteArray());
		
		System.out.println(kit.toBase64());
	}

}
