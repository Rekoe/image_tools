package com.onemena.momshop.module;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.lang.Streams;
import org.nutz.lang.util.NutMap;
import org.nutz.mvc.annotation.At;
import org.nutz.mvc.annotation.Ok;
import org.nutz.mvc.annotation.POST;

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
	@POST
	public NutMap makeColor(InputStream ins) {
		byte[] bytes = Streams.readBytesAndClose(ins);
		ImageKit imageKit = ImageKit.read(bytes);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		imageKit.doFilter(filter).transferTo(out, imageKit.getFormat());
		ImageKit kit = ImageKit.read(out.toByteArray());
		return NutMap.NEW().addv("code", 1).addv("message", "ok").addv("data", kit.toBase64());
	}
	
	public static void main(String[] args) {
		byte[] bytes = Streams.readBytesAndClose(Streams.fileIn("/Users/kouxian/Desktop/WechatIMG1364.jpeg"));
		ImageKit imageKit = ImageKit.read(bytes);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		imageKit.doFilter(filter).transferTo(out, imageKit.getFormat());
		ImageKit kit = ImageKit.read(out.toByteArray());
		System.out.println("转换为web base64:" + kit.getFormat().getB64Prefix() +  kit.toBase64());
		ImageKit.read(kit.toBase64()).transferTo("/Users/kouxian/Desktop/3.jpeg",imageKit.getFormat());;
	}
}
