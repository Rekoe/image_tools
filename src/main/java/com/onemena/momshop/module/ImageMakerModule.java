package com.onemena.momshop.module;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.lang.Files;
import org.nutz.lang.Streams;
import org.nutz.lang.util.NutMap;
import org.nutz.mvc.annotation.At;
import org.nutz.mvc.annotation.Ok;

import com.aliyun.oss.ClientException;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClient;
import com.aliyun.oss.OSSException;
import com.aliyun.oss.model.Callback;
import com.aliyun.oss.model.Callback.CalbackBodyType;
import com.aliyun.oss.model.PutObjectRequest;
import com.aliyun.oss.model.PutObjectResult;
import com.xice7.image.filter.PointFilter;
import com.xice7.image.kit.ImageKit;

@IocBean
@Ok("json")
public class ImageMakerModule {

	private final static MomShopColorFilter filter = new MomShopColorFilter();

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

	@At
	// @POST
	public NutMap makeColor(InputStream ins) {

		byte[] bytes = Streams.readBytesAndClose(ins);
		ImageKit imageKit = ImageKit.read(bytes);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		imageKit.doFilter(filter).transferTo(out, imageKit.getFormat());
		ImageKit kit = ImageKit.read(out.toByteArray());

		System.out.println(new String(Streams.readBytesAndClose(ins)));
		return NutMap.NEW().addv("code", 1).addv("message", "ok");// .addv("data", kit.toBase64());
	}

	private static String endpoint = "http://oss-eu-central-1.aliyuncs.com";
	private static String accessKeyId = "LTAIhPVy1OEfeAYW";
	private static String accessKeySecret = "3p8G8COzewDxYFvPbfmdNExvTC2cIq";
	private static String bucketName = "momshop-test";

	// The key name for the file to upload.
	public static void main1(String[] args) throws InvalidKeyException, UnsupportedEncodingException, NoSuchAlgorithmException {
		byte[] bytes = Streams.readBytesAndClose(Streams.fileIn("/Users/kouxian/Desktop/WechatIMG1364.jpeg"));

		ImageKit imageKit = ImageKit.read(bytes);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		imageKit.doFilter(filter).transferTo(out, imageKit.getFormat());
		ImageKit kit = ImageKit.read(out.toByteArray());
		System.out.println("转换为web base64:" + kit.getFormat().getB64Prefix() + kit.toBase64());
		ImageKit.read(kit.toBase64()).transferTo("/Users/kouxian/Desktop/3.jpeg", imageKit.getFormat());

	}

	public static void main2(String[] args) throws Exception {
		OSS ossClient = new OSSClient(endpoint, accessKeyId, accessKeySecret);
		ossClient.putObject(bucketName, "file", Streams.fileIn("/Users/kouxian/Desktop/WechatIMG1364.jpeg"));
	}

	// private static final String callbackUrl =
	// "http://apitest.momshop.cc/api/upload_oss_callback";
	private static final String callbackUrl = "http://rekoe.ngrok.wendal.cn/makeColor";

	public static void main(String[] args) throws IOException {
		OSS ossClient = new OSSClient(endpoint, accessKeyId, accessKeySecret);
		try {
			String content = "Hello OSS";
			PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, "key", new ByteArrayInputStream(content.getBytes()));

			Callback callback = new Callback();
			callback.setCallbackUrl(callbackUrl);
			// callback.setCallbackHost("apitest.momshop.cc");
			callback.setCallbackBody("{\\\"bucket\\\":${bucket},\\\"object\\\":${object}," + "\\\"mimeType\\\":${mimeType},\\\"size\\\":${size}," + "\\\"my_var1\\\":${x:var1},\\\"my_var2\\\":${x:var2}}");
			callback.setCalbackBodyType(CalbackBodyType.JSON);
			callback.addCallbackVar("x:var1", "value1");
			callback.addCallbackVar("x:var2", "222");
			putObjectRequest.setCallback(callback);
			putObjectRequest.setFile(Files.findFile("/Users/kouxian/Desktop/WechatIMG1364.jpeg"));
			// putObjectRequest.setInputStream(Streams.fileIn("/Users/kouxian/Desktop/WechatIMG1364.jpeg"));
			PutObjectResult putObjectResult = ossClient.putObject(putObjectRequest);
			byte[] buffer = new byte[1024];
			putObjectResult.getResponse().getContent().read(buffer);
			putObjectResult.getResponse().getContent().close();

		} catch (OSSException oe) {
			System.out.println("Caught an OSSException, which means your request made it to OSS, " + "but was rejected with an error response for some reason.");
			System.out.println("Error Message: " + oe.getErrorCode());
			System.out.println("Error Code:       " + oe.getErrorCode());
			System.out.println("Request ID:      " + oe.getRequestId());
			System.out.println("Host ID:           " + oe.getHostId());
		} catch (ClientException ce) {
			System.out.println("Caught an ClientException, which means the client encountered " + "a serious internal problem while trying to communicate with OSS, " + "such as not being able to access the network.");
			System.out.println("Error Message: " + ce.getMessage());
		} finally {
			ossClient.shutdown();
		}
	}
}
