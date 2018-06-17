package com.xice7.image.kit;

import java.awt.Color;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.imageio.stream.ImageInputStream;

import com.xice7.image.codec.Base64;
import com.xice7.image.ex.ImageParseException;
import com.xice7.image.filter.CropCircleFilter;
import com.xice7.image.filter.CropFilter;
import com.xice7.image.filter.GradientFilter;
import com.xice7.image.filter.GrayscaleFilter;
import com.xice7.image.filter.PixellateFilter;
import com.xice7.image.filter.RotateFilter;
import com.xice7.image.filter.ScaleFilter;
import com.xice7.image.filter.SepiaToneFilter;
import com.xice7.image.filter.ThresholdFilter;
import com.xice7.image.filter.ZipFilter;
import com.xice7.image.type.ImageType;

/**
 * @author mdc
 * @date 2016年4月2日
 */
public class ImageKit {

	private BufferedImage image;
	private ImageType format;

	/**
	 * 从BufferedImage中构造ImageKit
	 * @author mdc
	 * @date 2015年4月30日
	 * @param image 图片对象
	 * @param format 图片格式
	 * @return
	 * @throws IOException
	 */
	private ImageKit(BufferedImage image, ImageType format) throws ImageParseException {
		this.image = image;
		this.format = format;
	        
		try {
			image.getWidth();
		} catch (Exception e) {
			throw new ImageParseException(e.getMessage(), e);
		}
	}

	/**
	 * 根据BufferedImage构造ImageKit
	 * @author mdc
	 * @date 2016年5月15日
	 * @param image 图片对象
	 * @param format 图片格式
	 * @return
	 */
	public static ImageKit read(BufferedImage image, ImageType format) {
		return new ImageKit(image, format);
	}

	/**
	 * 从文件读取图片
	 * @author mdc
	 * @date 2015年4月30日
	 * @param input 一个图片文件
	 * @return
	 */
	public static ImageKit read(File input) {
		ImageKitUtils.ImageAndFormat imgFmt = ImageKitUtils.getImageAndFormat(input);
		return new ImageKit(imgFmt.image, imgFmt.format);
	}

	/**
	 * 从base64的字符串数据读取图片
	 * @author mdc
	 * @date 2015年4月30日
	 * @param base64 base64的字符串数据
	 * @return
	 */
	public static ImageKit read(String base64) {
		return read(Base64.decodeBase64(base64.getBytes()));
	}

	public static ImageKit read(byte[] input) {
		InputStream bytes = null;
		
		try {
			bytes = new ByteArrayInputStream(input);
			ImageKitUtils.ImageAndFormat imgFmt = ImageKitUtils.getImageAndFormat(bytes);
			return new ImageKit(imgFmt.image, imgFmt.format);
		} finally {
			IOKit.closeQuietly(bytes);
		}
	}

	/**
	 * 从InputStream读取图片,这个方法不会关闭ImageInputStream
	 * @author mdc
	 * @date 2015年4月30日
	 * @param input input
	 * @return
	 */
	public static ImageKit read(InputStream input) {
		ImageKitUtils.ImageAndFormat imgFmt = ImageKitUtils.getImageAndFormat(input);
		return new ImageKit(imgFmt.image, imgFmt.format);
	}

	/**
	 * 从URL读取图片
	 * @author mdc
	 * @date 2016年5月14日
	 * @param input
	 * @param format 图片格式
	 * @return
	 */
	public static ImageKit read(URL input, ImageType format) {

		try {
			return new ImageKit(ImageIO.read(input), format);
		} catch (IOException e) {
			throw new ImageParseException(e.getMessage(), e);
		}
	}

	/**
	 * 从ImageInputStream读取图片,这个方法不会关闭ImageInputStream
	 * @author mdc
	 * @date 2016年5月14日
	 * @param input
	 * @return
	 */
	public static ImageKit read(ImageInputStream input) {
		ImageKitUtils.ImageAndFormat imgFmt = ImageKitUtils.getImageAndFormat(input);
		return new ImageKit(imgFmt.image, imgFmt.format);
	}

	/**
	 * 创建图片
	 * @author mdc
	 * @date 2016年5月15日
	 * @param widht 宽度
	 * @param height 高度
	 * @param type 图片格式
	 * @return
	 */
	public static ImageKit create(int widht, int height, ImageType type) {
		return new ImageKit(new BufferedImage(widht, height, BufferedImage.TYPE_INT_RGB), type);
	}

	/**
	 * 转换为base64字符串
	 * @author mdc
	 * @date 2016年4月2日
	 * @return
	 */
	public String toBase64() {
		return toBase64(format);
	}

	/**
	 * 转换为byte数组
	 * @author mdc
	 * @date 2016年4月2日
	 * @return
	 */
	public byte[] toBytes() {
		return toBytes(format);
	}

	/**
	 * 转换为base64字符串
	 * @author mdc
	 * @date 2016年4月2日
	 * @param format 保存格式
	 * @return
	 */
	public String toBase64(ImageType format) {
		return new String(Base64.encodeBase64(toBytes(format)));
	}

	/**
	 * 转换为byte数组
	 * @author mdc
	 * @date 2016年4月2日
	 * @param format 保存格式
	 * @return
	 */
	public byte[] toBytes(ImageType format) {
		ByteArrayOutputStream output = null;
		
		try {
			output = new ByteArrayOutputStream();
			ImageIO.write(image, format.getType(), output);
			return output.toByteArray();

		} catch (Exception e) {
			throw new ImageParseException(e.getMessage(), e);
			
		} finally {
			IOKit.closeQuietly(output);
		}
	}

	/**
	 * 获取图片的宽度
	 * @author mdc
	 * @date 2016年4月2日
	 * @return
	 */
	public int getWidth() {
		return image.getWidth();
	}

	/**
	 * 获取图片的高度
	 * @author mdc
	 * @date 2016年4月2日
	 * @return
	 */
	public int getHeight() {
		return image.getHeight();
	}

	/**
	 * 获取图片的大小,单位byte
	 * @author mdc
	 * @date 2016年4月2日
	 * @return
	 */
	public int getSize() {
		return toBytes().length;
	}

	/**
	 * 写入到文件,会自动创建文件路径
	 * @author mdc
	 * @date 2016年4月2日
	 * @param output
	 */
	public void transferTo(String output) {
		this.transferTo(output, format);
	}

	/**
	 * 写入到文件,会自动创建文件路径
	 * @author mdc
	 * @date 2016年4月2日
	 * @param output
	 */
	public void transferTo(File output) {
		this.transferTo(output, format);
	}

	/**
	 * 写入到OutputStream
	 * @author mdc
	 * @date 2016年4月2日
	 * @param output
	 */
	public void transferTo(OutputStream output) {
		this.transferTo(output, format);
	}

	/**
	 * 写入到文件,会自动创建文件路径
	 * @author mdc
	 * @date 2016年4月2日
	 * @param output
	 * @param format 保存格式
	 */
	public void transferTo(String output, ImageType format) {
		transferTo(new File(output), format);
	}

	/**
	 * 写入到文件,会自动创建文件路径
	 * @author mdc
	 * @date 2016年4月2日
	 * @param output
	 * @param format 保存格式
	 */
	public void transferTo(File output, ImageType format) {
		if (!output.getParentFile().exists()) {
			output.getParentFile().mkdirs();
		}

		try {
			ImageIO.write(image, format.getType(), output);
		} catch (IOException e) {
			throw new ImageParseException(e.getMessage(), e);
		}
	}

	/**
	 * 写入到OutputStream
	 * @author mdc
	 * @date 2016年4月2日
	 * @param output
	 * @param format 保存格式
	 */
	public void transferTo(OutputStream output, ImageType format) {
		try {
			ImageIO.write(image, format.getType(), output);
		} catch (IOException e) {
			throw new ImageParseException(e.getMessage(), e);
		}
	}

	/**
	 * @return 获取{@link #format}
	 */
	public ImageType getFormat() {
		return format;
	}

	/**
	 * 获取得到的图片
	 * @author mdc
	 * @date 2016年4月3日
	 * @return
	 */
	public BufferedImage getImage() {
		return image;
	}

	/**
	 * 压缩图片,使用0.5压缩比率
	 * @author mdc
	 * @date 2016年4月2日
	 * @return ImageKit
	 */
	public ImageKit zip() {
		return zip(getWidth(), getHeight(), true, 0.5);
	}

	/**
	 * 压缩图片
	 * @author mdc
	 * @date 2016年4月2日
	 * @param quality 压缩比率 取值 0.0-1.0(透明图片无效)
	 * @return ImageKit
	 */
	public ImageKit zip(double quality) {
		return zip(getWidth(), getHeight(), true, quality);
	}

	/**
	 * 压缩图片,使用0.5压缩比率
	 * @author mdc
	 * @date 2016年4月2日
	 * @param proportion 是否等比缩放
	 * @return ImageKit
	 */
	public ImageKit zip(int width, int height, boolean proportion) {
		return zip(width, height, proportion, 0.5);
	}

	/**
	 * 压缩图片
	 * @author mdc
	 * @date 2016年4月2日
	 * @param width 宽度
	 * @param height 高度
	 * @param proportion 是否等比缩放
	 * @param quality 压缩比率 取值 0.0-1.0(透明图片无效,不限于png)
	 * @return ImageKit
	 */
	public ImageKit zip(int width, int height, boolean proportion, double quality) {
		int []prop = ImageKitUtils.getProportion(this.image, width, height, proportion);
		return this.doFilter(new ZipFilter(prop[0], prop[1], quality, format));
	}

	/**
	 * 图片等比缩放
	 * @author mdc
	 * @date 2016年5月14日
	 * @param width 缩放到指定宽度
	 * @param height 缩放到指定高度
	 * @return
	 */
	public ImageKit scale(int width, int height) {
		return this.scale(width, height, true);
	}

	/**
	 * 图片缩放,角度以逆时针方向为正值
	 * @author mdc
	 * @date 2016年5月14日
	 * @param width 缩放到指定宽度
	 * @param height 缩放到指定高度
	 * @param proportion 是否等比缩放
	 * @return
	 */
	public ImageKit scale(int width, int height, boolean proportion) {
		int []prop = ImageKitUtils.getProportion(this.image, width, height, proportion);
		return this.doFilter(new ScaleFilter(prop[0], prop[1]));
	}

	/**
	 * 旋转图片,角度以逆时针方向为正值,默认180度
	 * @author mdc
	 * @date 2016年5月14日
	 * @return
	 */
	public ImageKit rotate() {
		return this.doFilter(new RotateFilter());
	}

	/**
	 * 旋转图片,角度以逆时针方向为正值
	 * @author mdc
	 * @date 2016年5月14日
	 * @param angle 旋转角度
	 * @return
	 */
	public ImageKit rotate(double angle) {
		return this.doFilter(new RotateFilter(angle));
	}

	/**
	 * 旋转图片
	 * @author mdc
	 * @date 2016年5月14日
	 * @param angle 旋转角度
	 * @param resize 是否需要调整大小
	 * @return ImageKit
	 */
	public ImageKit rotate(double angle, boolean resize) {
		return this.doFilter(new RotateFilter(angle, resize));
	}

	/**
	 * 图像剪切(不检查坐标值和尺寸)
	 * @author mdc
	 * @date 2016年5月14日
	 * @param x x坐标
	 * @param y y坐标
	 * @param width 剪切宽度
	 * @param height 剪切高度
	 * @return ImageKit
	 */
	public ImageKit crop(int x, int y, int width, int height) {
		return this.doFilter(new CropFilter(x, y, width, height));
	}

	/**
	 * 图像圆形剪切,以图片的大小剪切
	 * @author mdc
	 * @date 2016年5月14日
	 * @return ImageKit
	 */
	public ImageKit cropCircle() {
		int size = getWidth() > getHeight() ? getHeight() : getWidth();
		return this.cropCircle(0, 0, size / 2);
	}

	/**
	 * 图像圆形剪切(以宽度和高度最小值为直径)
	 * @author mdc
	 * @date 2016年5月14日
	 * @param x x坐标
	 * @param y y坐标
	 * @return ImageKit
	 */
	public ImageKit cropCircle(int x, int y) {
		int width = getWidth() - x;
		int height = getHeight() - y;
		int size = width > height ? height : width;
		return this.cropCircle(x, y, size, size, size / 2);
	}

	/**
	 * 图像圆形剪切(不检查坐标值和尺寸)
	 * @author mdc
	 * @date 2016年5月14日
	 * @param x x坐标
	 * @param y y坐标
	 * @param radius 剪切半径
	 * @return ImageKit
	 */
	public ImageKit cropCircle(int x, int y, int radius) {
		this.format = ImageType.PNG;
		return this.doFilter(new CropCircleFilter(x, y, radius * 2, radius * 2, radius), ImageKitUtils.convertPng(image));
	}

	/**
	 * 图像带有弧度的剪切(不检查坐标值和尺寸)
	 * @author mdc
	 * @date 2016年5月14日
	 * @param x x坐标
	 * @param y y坐标
	 * @param width 剪切的宽度
	 * @param height 剪切的高度
	 * @param radius 剪切半径
	 * @return ImageKit
	 */
	public ImageKit cropCircle(int x, int y, int width, int height, int radius) {
		this.format = ImageType.PNG;
		return this.doFilter(new CropCircleFilter(x, y, width, height, radius), ImageKitUtils.convertPng(image));
	}

	/**
	 * 图片灰度处理,黑白效果
	 * @author mdc
	 * @date 2016年5月14日
	 * @return ImageKit
	 */
	public ImageKit grayscale() {
		return this.doFilter(new GrayscaleFilter());
	}

	/**
	 * 老照片效果
	 * @author mdc
	 * @date 2016年5月15日
	 * @return
	 */
	public ImageKit sepiaTone() {
		return this.doFilter(new SepiaToneFilter());
	}

	/**
	 * 像素效果,默认像素块大小为10x10 px
	 * @author mdc
	 * @date 2016年5月15日
	 * @return
	 */
	public ImageKit pixellate() {
		return this.pixellate(10);
	}

	/**
	 * 像素效果
	 * @author mdc
	 * @date 2016年5月15日
	 * @param size 像素块大小
	 * @return
	 */
	public ImageKit pixellate(int size) {
		return this.doFilter(new PixellateFilter(size));
	}

	/**
	 * 黑白色
	 * @author mdc
	 * @date 2016年5月22日
	 * @return
	 */
	public ImageKit gray(){
		return this.doFilter(new ThresholdFilter());
	}

	// GrayFilter
	public ImageKit gradient() {
		boolean repeat = true;
		Point p1 = new Point(getWidth() / 2, 0);
		Point p2 = new Point(getWidth() / 2, getHeight());
		Color color1 = new Color(137, 207, 226);
		Color color2 = new Color(24, 110, 167);

		return this.doFilter(new GradientFilter(p1, p2, color1.getRGB(), color2.getRGB(), repeat, GradientFilter.LINEAR, GradientFilter.INT_LINEAR));
	}
	
	/**
	 * 执行图像Filter
	 * @param filter 图像Filter, BufferedImageOp的实现
	 * @return
	 */
	public ImageKit doFilter(BufferedImageOp filter) {
		image = filter.filter(image, null);
		return this;
	}
	
	/**
	 * 执行图像Filter
	 * @param filter 图像Filter, BufferedImageOp的实现
	 * @param srcImage 处理的原始图像
	 * @return
	 */
	public ImageKit doFilter(BufferedImageOp filter, BufferedImage srcImage) {
		image = filter.filter(srcImage, null);
		return this;
	}

}
