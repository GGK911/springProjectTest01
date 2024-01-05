package com.ggk911.springtest01.util;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.io.resource.ResourceUtil;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.FontSelector;
import com.itextpdf.text.pdf.GrayColor;

import javax.imageio.ImageIO;
import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.RenderingHints;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 制章工具
 * 如修改图章大小需注意：
 * 企业参数scaleSize
 *
 * @author TangHaoKai
 * @version V1.0 2023/12/26 11:59
 **/
public class CreateSealUtil {
    // 默认印章图片像素大小 推荐3的倍数 不要小于300
    private final static int IMAGE_SIZE = 300;

    // 画笔颜色
    private final static Color COLOR = Color.RED;

    // 字体
    private final static Map<BaseFont, String> SPARE_FONT = new HashMap<>();

    static {
        GraphicsEnvironment genv = GraphicsEnvironment.getLocalGraphicsEnvironment();

        Font font = null;
        try {
            font = Font.createFont(Font.TRUETYPE_FONT, ResourceUtil.getStream("font/SIMSUN.TTF"));
        } catch (FontFormatException | IOException e) {
            e.printStackTrace();
        }
        genv.registerFont(font);
        BaseFont baseFont = FontFactory.getFont("src/main/resources/font/SIMSUN.TTF", BaseFont.IDENTITY_H, BaseFont.NOT_EMBEDDED, 12f, com.itextpdf.text.Font.NORMAL, BaseColor.BLACK).getBaseFont();
        if (font != null) {
            SPARE_FONT.put(baseFont, font.getName());
        }

        Font spareFont1 = null;
        try {
            spareFont1 = Font.createFont(Font.TRUETYPE_FONT, ResourceUtil.getStream("font/JinbiaoSong.TTF"));
        } catch (FontFormatException | IOException e) {
            e.printStackTrace();
        }
        genv.registerFont(spareFont1);
        BaseFont baseFont1 = FontFactory.getFont("src/main/resources/font/JinbiaoSong.TTF", BaseFont.IDENTITY_H, BaseFont.NOT_EMBEDDED, 12f, com.itextpdf.text.Font.NORMAL, BaseColor.BLACK).getBaseFont();
        if (spareFont1 != null) {
            SPARE_FONT.put(baseFont1, spareFont1.getName());
        }

        Font spareFont2 = null;
        try {
            spareFont2 = Font.createFont(Font.TRUETYPE_FONT, ResourceUtil.getStream("font/simsunb.ttf"));
        } catch (FontFormatException | IOException e) {
            e.printStackTrace();
        }
        genv.registerFont(spareFont2);
        BaseFont baseFont2 = FontFactory.getFont("src/main/resources/font/simsunb.ttf", BaseFont.IDENTITY_H, BaseFont.NOT_EMBEDDED, 12f, com.itextpdf.text.Font.NORMAL, BaseColor.BLACK).getBaseFont();
        if (spareFont2 != null) {
            SPARE_FONT.put(baseFont2, spareFont2.getName());
        }

    }

    /**
     * 制作方章(个人图章)
     *
     * @param name 名称
     * @return 字节
     */
    public static byte[] createSquareSeal(String name) {
        // 两字是否在左
        boolean twoLeft = true;
        // 是否保持字体宽度，不拉伸
        boolean isometricFont = true;
        int pointCount = name.codePointCount(0, name.length());
        BufferedImage bufferedImage = null;
        if (pointCount == 1) {
            bufferedImage = drawOneStringHeightImageSizeHalf(name);
        } else if (pointCount == 2) {
            bufferedImage = drawTwoHorizonString(StrUtil.subString(name, 0, 1), StrUtil.subString(name, 1, 2));
        } else if (pointCount == 3) {
            if (twoLeft) {
                bufferedImage = drawThreeHorizonTwoLeftString(StrUtil.subString(name, 1, 2), StrUtil.subString(name, 2, 3), StrUtil.subString(name, 0, 1));
            } else {
                bufferedImage = drawThreeHorizonTwoRightString(StrUtil.subString(name, 1, 2), StrUtil.subString(name, 2, 3), StrUtil.subString(name, 0, 1));
            }
        } else if (pointCount == 4) {
            bufferedImage = drawFourString(StrUtil.subString(name, 2, 3), StrUtil.subString(name, 3, 4), StrUtil.subString(name, 0, 1), StrUtil.subString(name, 1, 2));
        } else if (pointCount == 5) {
            if (twoLeft && isometricFont) {
                bufferedImage = drawFiveVerticalTwoLeftIsometricFontStringTest(StrUtil.subString(name, 2, 3), StrUtil.subString(name, 3, 4), StrUtil.subString(name, 4, 5), StrUtil.subString(name, 0, 1), StrUtil.subString(name, 1, 2));
            } else if (!twoLeft && !isometricFont) {
                bufferedImage = drawFiveVerticalTwoRightNotIsometricFontStringTest(StrUtil.subString(name, 2, 3), StrUtil.subString(name, 3, 4), StrUtil.subString(name, 4, 5), StrUtil.subString(name, 0, 1), StrUtil.subString(name, 1, 2));
            } else if (twoLeft && !isometricFont) {
                bufferedImage = drawFiveVerticalTwoLeftNotIsometricFontStringTest(StrUtil.subString(name, 2, 3), StrUtil.subString(name, 3, 4), StrUtil.subString(name, 4, 5), StrUtil.subString(name, 0, 1), StrUtil.subString(name, 1, 2));
            } else if (!twoLeft && isometricFont) {
                bufferedImage = drawFiveVerticalTwoRightIsometricFontStringTest(StrUtil.subString(name, 2, 3), StrUtil.subString(name, 3, 4), StrUtil.subString(name, 4, 5), StrUtil.subString(name, 0, 1), StrUtil.subString(name, 1, 2));
            }
        } else if (pointCount == 6) {
            if (isometricFont) {
                bufferedImage = drawSixVerticalTwoRightIsometricFontStringTest(StrUtil.subString(name, 0, 1), StrUtil.subString(name, 1, 2), StrUtil.subString(name, 2, 3), StrUtil.subString(name, 3, 4), StrUtil.subString(name, 4, 5), StrUtil.subString(name, 5, 6));
            } else {
                bufferedImage = drawSixVerticalTwoRightNotIsometricFontStringTest(StrUtil.subString(name, 0, 1), StrUtil.subString(name, 1, 2), StrUtil.subString(name, 2, 3), StrUtil.subString(name, 3, 4), StrUtil.subString(name, 4, 5), StrUtil.subString(name, 5, 6));
            }
        } else {
            System.out.println("字数过长，推荐自定义图章");
            throw new RuntimeException("字数过长，推荐自定义图章");
        }
        // 边框
        bufferedImage = drawSquareOrder(bufferedImage);
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        try {
            ImageIO.write(bufferedImage, "png", outStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return outStream.toByteArray();
    }

    /**
     * 制作圆章(企业图章)
     *
     * @param name 名称
     * @return 字节
     */
    public static byte[] createCircleSeal(String name) {
        int pointCount = name.codePointCount(0, name.length());
        if (pointCount >= 24) {
            System.out.println("字数过长，推荐自定义图章");
            throw new RuntimeException("字数过长，推荐自定义图章");
        }
        // 画布 300X300
        BufferedImage bufferedImage = new BufferedImage(IMAGE_SIZE, IMAGE_SIZE, BufferedImage.TYPE_4BYTE_ABGR);
        // 画笔
        Graphics2D g2d = bufferedImage.createGraphics();
        // 抗锯齿
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        // 透明度
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC, 1));
        // 画笔颜色
        g2d.setPaint(COLOR);

        // 画圆
        int firstCircle = (int) (IMAGE_SIZE * 0.026);
        g2d.setStroke(new BasicStroke(firstCircle));
        // 因为圈占x像素，所以坐标位移x像素，整体宽高都减2 * x
        g2d.drawOval(firstCircle, firstCircle, IMAGE_SIZE - firstCircle * 2, IMAGE_SIZE - firstCircle * 2);

        // 中间五角星 大小比例 0.4
        int size = (int) (IMAGE_SIZE * 0.4F);
        g2d.setFont(new Font("宋体", Font.PLAIN, size));
        // 高度Y误差 0.16
        g2d.drawString("★", IMAGE_SIZE / 2F - size / 2F, (float) (IMAGE_SIZE / 2F - size / 2F + size - size * 0.16F));

        // 画字
        // 距离圆圈边界值
        int topFix = 15;
        double circleRadius = IMAGE_SIZE / 2F;
        // 字体 宋体 大小 0.088
        Font font = new Font("宋体", Font.PLAIN, (int) Math.ceil(IMAGE_SIZE * 0.088));
        FontRenderContext fontRenderContext = g2d.getFontRenderContext();
        Rectangle2D stringBounds = font.getStringBounds(name, fontRenderContext);
        double width = stringBounds.getWidth();
        double height = stringBounds.getHeight();
        double scaleSize = 2;
        topFix = topFix + Convert.toInt(height / scaleSize * 2.0);
        // 调节字体间距
        double interval;
        if (pointCount < 9) {
            interval = width / (pointCount - 1) * 1.45;
        } else if (pointCount < 12) {
            interval = width / (pointCount - 1) * 1.25;
        } else if (pointCount < 15) {
            interval = width / (pointCount - 1) * 0.95;
        } else if (pointCount < 18) {
            interval = width / (pointCount - 1) * 0.85;
        } else {
            interval = width / (pointCount - 1) * 0.75;
        }
        double radius = circleRadius + stringBounds.getY() - topFix;
        double radianInterval = 2 * Math.asin(interval / (2 * radius));
        double firstAngle;
        // 第一个字起始位置
        double fix = 0.18;
        if (pointCount % 2 == 1) {
            firstAngle = (pointCount - 1) * radianInterval / 2.0 + Math.PI / 2 + fix;
        } else {
            firstAngle = (pointCount / 2.0 - 1) * radianInterval + radianInterval / 2.0 + Math.PI / 2 + fix;
        }
        // 字体旋转倾斜度      数值越大顺时针方向旋转度越高
        double gradient = 0.2;
        // 字体间是否靠近度    数值越大越靠近
        double distance = 0.9;
        // 为每个字找到合适字体
        Map<String, Font> fitFont = getFitFont(name, (int) Math.ceil(IMAGE_SIZE * 0.088), Font.PLAIN);
        for (int i = 0; i < pointCount; i++) {
            double aa = firstAngle - i * radianInterval;
            double ax = radius * Math.sin(Math.PI / 2 - aa);
            double ay = radius * Math.cos(aa - Math.PI / 2);
            AffineTransform transform = AffineTransform.getRotateInstance(Math.PI / 2 - aa + gradient);
            AffineTransform scaleform = AffineTransform.getScaleInstance(distance, scaleSize);
            // 防止文本和外圈间距
            transform.concatenate(scaleform);

            String text = StrUtil.subString(name, i, i + 1);
            Font textFont = fitFont.get(text);
            Font font2 = textFont.deriveFont(transform);
            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);//设置抗锯齿
            g2d.setFont(font2);
            g2d.setPaint(COLOR);
            // 文字
            g2d.drawString(StrUtil.subString(name, i, i + 1), (float) (circleRadius + ax), (float) (circleRadius - ay));
        }

        g2d.dispose();
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        try {
            ImageIO.write(bufferedImage, "png", outStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return outStream.toByteArray();
    }

    /**
     * 遍历字体获取text最合适字体(利用itext的FontSelector)
     *
     * @param text     文字
     * @param fontSize 字体大小
     * @return 字体
     */
    public static Map<String, Font> getFitFont(String text, int fontSize, int fontStyle) {
        FontSelector fs = new FontSelector();
        for (Map.Entry<BaseFont, String> fontEntry : SPARE_FONT.entrySet()) {
            fs.addFont(new com.itextpdf.text.Font(fontEntry.getKey(), 12, 0, GrayColor.GRAYBLACK));
        }
        Phrase process = fs.process(text);
        List<Chunk> chunks = process.getChunks();
        Map<String, Font> stringFontMap = new HashMap<>();
        for (Chunk chunk : chunks) {
            String content = chunk.getContent();
            for (int i = 0; i < content.codePointCount(0, content.length()); i++) {
                String fontName = SPARE_FONT.get(chunk.getFont().getBaseFont());
                stringFontMap.put(StrUtil.subString(content, i, i + 1), new Font(fontName, fontStyle, fontSize));
            }
        }
        return stringFontMap;
    }


    /**
     * 垂直二字
     *
     * @param top    二字上，单字图像
     * @param bottom 二字下，单字图像
     * @return 图像
     */
    private static BufferedImage drawTwoVerticalString(BufferedImage top, BufferedImage bottom) {
        // 画布
        BufferedImage bufferedImage = new BufferedImage(IMAGE_SIZE / 2, IMAGE_SIZE, BufferedImage.TYPE_4BYTE_ABGR);
        // 画笔
        Graphics2D graphics = bufferedImage.createGraphics();
        // 抗锯齿设置
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        // 颜色
        graphics.setPaint(COLOR);
        graphics.drawImage(top, 0, (int) (0 + IMAGE_SIZE * 0.04), IMAGE_SIZE / 2, IMAGE_SIZE / 2, null);
        graphics.drawImage(bottom, 0, IMAGE_SIZE / 2 - (int) (IMAGE_SIZE * 0.04), IMAGE_SIZE / 2, IMAGE_SIZE / 2, null);
        graphics.dispose();
        return bufferedImage;
    }


    /**
     * 画平行二字
     *
     * @param left  左字
     * @param right 右字
     * @return 图像
     */
    public static BufferedImage drawTwoHorizonString(String left, String right) {
        // 画布
        BufferedImage bufferedImage = new BufferedImage(IMAGE_SIZE, IMAGE_SIZE, BufferedImage.TYPE_4BYTE_ABGR);
        // 画笔
        Graphics2D graphics = bufferedImage.createGraphics();
        // 抗锯齿设置
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        // 颜色
        graphics.setPaint(COLOR);
        graphics.drawImage(drawOneStringHeightImageSizeHalf(left), (int) (0 + IMAGE_SIZE * 0.033), 0, IMAGE_SIZE / 2, IMAGE_SIZE, null);
        graphics.drawImage(drawOneStringHeightImageSizeHalf(right), (int) (IMAGE_SIZE / 2 - IMAGE_SIZE * 0.033), 0, IMAGE_SIZE / 2, IMAGE_SIZE, null);
        graphics.dispose();
        return bufferedImage;
    }

    /**
     * 画垂直三字
     *
     * @param top    三字上，单字图像
     * @param middle 三字中，单字图像
     * @param bottom 三字下，单字图像
     * @return 图像
     */
    private static BufferedImage drawThreeVerticalString(BufferedImage top, BufferedImage middle, BufferedImage bottom) {
        // 画布
        BufferedImage bufferedImage = new BufferedImage(IMAGE_SIZE / 2, IMAGE_SIZE, BufferedImage.TYPE_4BYTE_ABGR);
        // 画笔
        Graphics2D graphics = bufferedImage.createGraphics();
        // 抗锯齿设置
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        // 颜色
        graphics.setPaint(COLOR);
        graphics.drawImage(top, 0, IMAGE_SIZE / 3 * 0, IMAGE_SIZE / 2, IMAGE_SIZE / 3, null);
        graphics.drawImage(middle, 0, IMAGE_SIZE / 3 * 1, IMAGE_SIZE / 2, IMAGE_SIZE / 3, null);
        graphics.drawImage(bottom, 0, IMAGE_SIZE / 3 * 2, IMAGE_SIZE / 2, IMAGE_SIZE / 3, null);
        graphics.dispose();
        return bufferedImage;
    }

    /**
     * 三字，二字在左
     *
     * @param leftTop    左上字
     * @param leftBottom 左下字
     * @param right      右字
     * @return 图像
     */
    public static BufferedImage drawThreeHorizonTwoLeftString(String leftTop, String leftBottom, String right) {
        // 画布
        BufferedImage bufferedImage = new BufferedImage(IMAGE_SIZE, IMAGE_SIZE, BufferedImage.TYPE_4BYTE_ABGR);
        // 画笔
        Graphics2D graphics = bufferedImage.createGraphics();
        // 抗锯齿设置
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        // 颜色
        graphics.setPaint(COLOR);
        graphics.drawImage(drawTwoVerticalString(drawOneStringHeightImageSizeHalf(leftTop), drawOneStringHeightImageSizeHalf(leftBottom)), (int) (0 + IMAGE_SIZE * 0.02), 0, IMAGE_SIZE / 2, IMAGE_SIZE, null);
        graphics.drawImage(drawOneStringHeight300(right), IMAGE_SIZE / 2 - (int) (IMAGE_SIZE * 0.02), (int) (0 - IMAGE_SIZE * 0.02), IMAGE_SIZE / 2, IMAGE_SIZE, null);
        graphics.dispose();
        return bufferedImage;
    }

    /**
     * 三字，二字在右
     *
     * @param rightTop    右上
     * @param rightBottom 右下
     * @param left        左
     * @return 图像
     */
    public static BufferedImage drawThreeHorizonTwoRightString(String rightTop, String rightBottom, String left) {
        // 画布
        BufferedImage bufferedImage = new BufferedImage(IMAGE_SIZE, IMAGE_SIZE, BufferedImage.TYPE_4BYTE_ABGR);
        // 画笔
        Graphics2D graphics = bufferedImage.createGraphics();
        // 抗锯齿设置
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        // 颜色
        graphics.setPaint(COLOR);
        graphics.drawImage(drawOneStringHeight300(left), (int) (0 + IMAGE_SIZE * 0.02), (int) (0 - IMAGE_SIZE * 0.02), IMAGE_SIZE / 2 + 20, IMAGE_SIZE + 10, null);
        graphics.drawImage(drawTwoVerticalString(drawOneStringHeightImageSizeHalf(rightTop), drawOneStringHeightImageSizeHalf(rightBottom)), IMAGE_SIZE / 2 - (int) (IMAGE_SIZE * 0.02), 0, IMAGE_SIZE / 2, IMAGE_SIZE, null);
        graphics.dispose();
        return bufferedImage;
    }

    /**
     * 画四字
     *
     * @param leftTop     左上
     * @param leftBottom  左下
     * @param rightTop    右上
     * @param rightBottom 右下
     * @return 图像
     */
    public static BufferedImage drawFourString(String leftTop, String leftBottom, String rightTop, String rightBottom) {
        // 画布
        BufferedImage bufferedImage = new BufferedImage(IMAGE_SIZE, IMAGE_SIZE, BufferedImage.TYPE_4BYTE_ABGR);
        // 画笔
        Graphics2D graphics = bufferedImage.createGraphics();
        // 抗锯齿设置
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        // 颜色
        graphics.setPaint(COLOR);
        graphics.drawImage(drawTwoVerticalString(drawOneStringHeightImageSizeHalf(leftTop), drawOneStringHeightImageSizeHalf(leftBottom)), (int) (0 + IMAGE_SIZE * 0.033), 0, IMAGE_SIZE / 2, IMAGE_SIZE, null);
        graphics.drawImage(drawTwoVerticalString(drawOneStringHeightImageSizeHalf(rightTop), drawOneStringHeightImageSizeHalf(rightBottom)), IMAGE_SIZE / 2 - (int) (0 + IMAGE_SIZE * 0.033), 0, IMAGE_SIZE / 2, IMAGE_SIZE, null);
        graphics.dispose();
        return bufferedImage;
    }

    /**
     * 五字，二字在左，等比例字体
     *
     * @param topThree    三字上
     * @param middleThree 三字中
     * @param bottomThree 三字下
     * @param topTwo      二字上
     * @param bottomTwo   二字下
     * @return 图像
     */
    public static BufferedImage drawFiveVerticalTwoLeftIsometricFontStringTest(String topThree, String middleThree, String bottomThree, String topTwo, String bottomTwo) {
        // 画布
        BufferedImage bufferedImage = new BufferedImage(IMAGE_SIZE, IMAGE_SIZE, BufferedImage.TYPE_4BYTE_ABGR);
        // 画笔
        Graphics2D graphics = bufferedImage.createGraphics();
        // 抗锯齿设置
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        // 颜色
        graphics.setPaint(COLOR);
        graphics.drawImage(drawThreeVerticalString(drawOneStringHeightImageSizeOneThirds(topThree), drawOneStringHeightImageSizeOneThirds(middleThree), drawOneStringHeightImageSizeOneThirds(bottomThree)), IMAGE_SIZE / 2, 0, IMAGE_SIZE / 2, IMAGE_SIZE, null);
        graphics.drawImage(drawTwoVerticalString(drawOneStringHeightImageSizeHalf(topTwo), drawOneStringHeightImageSizeHalf(bottomTwo)), 0, 0, IMAGE_SIZE / 2, IMAGE_SIZE, null);
        graphics.dispose();
        return bufferedImage;
    }

    /**
     * 五字，二字在左，非等比例字体
     *
     * @param topThree    三字上
     * @param middleThree 三字中
     * @param bottomThree 三字下
     * @param topTwo      二字上
     * @param bottomTwo   二字下
     * @return 图像
     */
    public static BufferedImage drawFiveVerticalTwoLeftNotIsometricFontStringTest(String topThree, String middleThree, String bottomThree, String topTwo, String bottomTwo) {
        // 画布
        BufferedImage bufferedImage = new BufferedImage(IMAGE_SIZE, IMAGE_SIZE, BufferedImage.TYPE_4BYTE_ABGR);
        // 画笔
        Graphics2D graphics = bufferedImage.createGraphics();
        // 抗锯齿设置
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        // 颜色
        graphics.setPaint(COLOR);
        graphics.drawImage(drawThreeVerticalString(imageStretch(drawOneStringHeightImageSizeHalf(topThree), IMAGE_SIZE / 2, IMAGE_SIZE / 3), imageStretch(drawOneStringHeightImageSizeHalf(middleThree), IMAGE_SIZE / 2, IMAGE_SIZE / 3), imageStretch(drawOneStringHeightImageSizeHalf(bottomThree), IMAGE_SIZE / 2, IMAGE_SIZE / 3)), IMAGE_SIZE / 2, 0, IMAGE_SIZE / 2, IMAGE_SIZE, null);
        graphics.drawImage(drawTwoVerticalString(drawOneStringHeightImageSizeHalf(topTwo), drawOneStringHeightImageSizeHalf(bottomTwo)), 0, 0, IMAGE_SIZE / 2, IMAGE_SIZE, null);
        graphics.dispose();
        return bufferedImage;
    }

    /**
     * 五字，二字在右，等比例字体
     *
     * @param topThree    三字上
     * @param middleThree 三字中
     * @param bottomThree 三字下
     * @param topTwo      二字上
     * @param bottomTwo   二字下
     * @return 图像
     */
    public static BufferedImage drawFiveVerticalTwoRightIsometricFontStringTest(String topThree, String middleThree, String bottomThree, String topTwo, String bottomTwo) {
        // 画布
        BufferedImage bufferedImage = new BufferedImage(IMAGE_SIZE, IMAGE_SIZE, BufferedImage.TYPE_4BYTE_ABGR);
        // 画笔
        Graphics2D graphics = bufferedImage.createGraphics();
        // 抗锯齿设置
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        // 颜色
        graphics.setPaint(COLOR);
        graphics.drawImage(drawThreeVerticalString(drawOneStringHeightImageSizeOneThirds(topThree), drawOneStringHeightImageSizeOneThirds(middleThree), drawOneStringHeightImageSizeOneThirds(bottomThree)), 0, 0, IMAGE_SIZE / 2, IMAGE_SIZE, null);
        graphics.drawImage(drawTwoVerticalString(drawOneStringHeightImageSizeHalf(topTwo), drawOneStringHeightImageSizeHalf(bottomTwo)), IMAGE_SIZE / 2, 0, IMAGE_SIZE / 2, IMAGE_SIZE, null);
        graphics.dispose();
        return bufferedImage;
    }

    /**
     * 五字，二字在右，非等比例字体
     *
     * @param topThree    三字上
     * @param middleThree 三字中
     * @param bottomThree 三字下
     * @param topTwo      二字上
     * @param bottomTwo   二字下
     * @return 图像
     */
    public static BufferedImage drawFiveVerticalTwoRightNotIsometricFontStringTest(String topThree, String middleThree, String bottomThree, String topTwo, String bottomTwo) {
        // 画布
        BufferedImage bufferedImage = new BufferedImage(IMAGE_SIZE, IMAGE_SIZE, BufferedImage.TYPE_4BYTE_ABGR);
        // 画笔
        Graphics2D graphics = bufferedImage.createGraphics();
        // 抗锯齿设置
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        // 颜色
        graphics.setPaint(COLOR);
        graphics.drawImage(drawThreeVerticalString(imageStretch(drawOneStringHeightImageSizeHalf(topThree), IMAGE_SIZE / 2, IMAGE_SIZE / 3), imageStretch(drawOneStringHeightImageSizeOneThirds(middleThree), IMAGE_SIZE / 2, IMAGE_SIZE / 3), imageStretch(drawOneStringHeightImageSizeOneThirds(bottomThree), IMAGE_SIZE / 2, IMAGE_SIZE / 3)), 0, 0, IMAGE_SIZE / 2, IMAGE_SIZE, null);
        graphics.drawImage(drawTwoVerticalString(drawOneStringHeightImageSizeHalf(topTwo), drawOneStringHeightImageSizeHalf(bottomTwo)), IMAGE_SIZE / 2, 0, IMAGE_SIZE / 2, IMAGE_SIZE, null);
        graphics.dispose();
        return bufferedImage;
    }

    /**
     * 六字，非等比例字体
     *
     * @param topLeft     三字左上
     * @param middleLeft  三字左中
     * @param bottomLeft  三字左下
     * @param topRight    三字右上
     * @param middleRight 三字右中
     * @param bottomRight 三字右下
     * @return 图像
     */
    public static BufferedImage drawSixVerticalTwoRightNotIsometricFontStringTest(String topLeft, String middleLeft, String bottomLeft, String topRight, String middleRight, String bottomRight) {
        // 画布
        BufferedImage bufferedImage = new BufferedImage(IMAGE_SIZE, IMAGE_SIZE, BufferedImage.TYPE_4BYTE_ABGR);
        // 画笔
        Graphics2D graphics = bufferedImage.createGraphics();
        // 抗锯齿设置
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        // 颜色
        graphics.setPaint(COLOR);
        graphics.drawImage(drawThreeVerticalString(imageStretch(drawOneStringHeightImageSizeHalf(topLeft), IMAGE_SIZE / 2, IMAGE_SIZE / 3), imageStretch(drawOneStringHeightImageSizeHalf(middleLeft), IMAGE_SIZE / 2, IMAGE_SIZE / 3), imageStretch(drawOneStringHeightImageSizeHalf(bottomLeft), IMAGE_SIZE / 2, IMAGE_SIZE / 3)), 0, 0, IMAGE_SIZE / 2, IMAGE_SIZE, null);
        graphics.drawImage(drawThreeVerticalString(imageStretch(drawOneStringHeightImageSizeHalf(topRight), IMAGE_SIZE / 2, IMAGE_SIZE / 3), imageStretch(drawOneStringHeightImageSizeHalf(middleRight), IMAGE_SIZE / 2, IMAGE_SIZE / 3), imageStretch(drawOneStringHeightImageSizeHalf(bottomRight), IMAGE_SIZE / 2, IMAGE_SIZE / 3)), IMAGE_SIZE / 2, 0, IMAGE_SIZE / 2, IMAGE_SIZE, null);
        graphics.dispose();
        return bufferedImage;
    }

    /**
     * 六字，等比例字体
     *
     * @param topLeft     三字左上
     * @param middleLeft  三字左中
     * @param bottomLeft  三字左下
     * @param topRight    三字右上
     * @param middleRight 三字右中
     * @param bottomRight 三字右下
     * @return 图像
     */
    public static BufferedImage drawSixVerticalTwoRightIsometricFontStringTest(String topLeft, String middleLeft, String bottomLeft, String topRight, String middleRight, String bottomRight) {
        // 画布
        BufferedImage bufferedImage = new BufferedImage(IMAGE_SIZE, IMAGE_SIZE, BufferedImage.TYPE_4BYTE_ABGR);
        // 画笔
        Graphics2D graphics = bufferedImage.createGraphics();
        // 抗锯齿设置
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        // 颜色
        graphics.setPaint(COLOR);
        graphics.drawImage(drawThreeVerticalString(drawOneStringHeightImageSizeOneThirds(topLeft), drawOneStringHeightImageSizeOneThirds(middleLeft), drawOneStringHeightImageSizeOneThirds(bottomLeft)), 0, 0, IMAGE_SIZE / 2, IMAGE_SIZE, null);
        graphics.drawImage(drawThreeVerticalString(drawOneStringHeightImageSizeOneThirds(topRight), drawOneStringHeightImageSizeOneThirds(middleRight), drawOneStringHeightImageSizeOneThirds(bottomRight)), IMAGE_SIZE / 2, 0, IMAGE_SIZE / 2, IMAGE_SIZE, null);
        graphics.dispose();
        return bufferedImage;
    }

    /**
     * 画单字，高度为IMAGE_SIZE，宽度为IMAGE_SIZE / 2
     *
     * @param str 字
     * @return 图像
     */
    public static BufferedImage drawOneStringHeight300(String str) {
        // 画布
        BufferedImage bufferedImage = new BufferedImage(IMAGE_SIZE / 2, IMAGE_SIZE, BufferedImage.TYPE_4BYTE_ABGR);
        // 画笔
        Graphics2D graphics = bufferedImage.createGraphics();
        // 抗锯齿设置
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        // 画字
        graphics.drawImage(drawOneStringHeightImageSizeHalf(str), 0, 0, IMAGE_SIZE / 2, IMAGE_SIZE, null);
        // 释放
        graphics.dispose();
        return bufferedImage;
    }

    /**
     * 画单字，高度为IMAGE_SIZE / 2，宽度为IMAGE_SIZE / 2
     *
     * @param str 字
     * @return 图像
     */
    public static BufferedImage drawOneStringHeightImageSizeHalf(String str) {
        // 画布
        BufferedImage bufferedImage = new BufferedImage(IMAGE_SIZE / 2, IMAGE_SIZE / 2, BufferedImage.TYPE_4BYTE_ABGR);
        // 画笔
        Graphics2D graphics = bufferedImage.createGraphics();
        // 抗锯齿设置
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        Map<String, Font> fitFont = getFitFont(str, (int) (IMAGE_SIZE * 0.4), Font.BOLD);
        // 字体
        graphics.setFont(fitFont.get(str));
        // 颜色
        graphics.setPaint(COLOR);
        // 画字
        graphics.drawString(str, (int) (IMAGE_SIZE * 0.05), (int) (IMAGE_SIZE * 0.4));
        // 释放
        graphics.dispose();
        return bufferedImage;
    }

    /**
     * 画单字，高度为IMAGE_SIZE / 3，宽度为IMAGE_SIZE / 2
     *
     * @param str 字
     * @return 图像
     */
    public static BufferedImage drawOneStringHeightImageSizeOneThirds(String str) {
        // 画布
        BufferedImage bufferedImage = new BufferedImage(IMAGE_SIZE / 2, IMAGE_SIZE / 3, BufferedImage.TYPE_4BYTE_ABGR);
        // 画笔
        Graphics2D graphics = bufferedImage.createGraphics();
        // 抗锯齿设置
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        Map<String, Font> fitFont = getFitFont(str, (int) (IMAGE_SIZE * 0.266), Font.BOLD);
        // 字体
        graphics.setFont(fitFont.get(str));
        // 颜色
        graphics.setPaint(COLOR);
        // 画字
        graphics.drawString(str, (int) (IMAGE_SIZE * 0.116), (int) (IMAGE_SIZE * 0.266));
        // 释放
        graphics.dispose();
        return bufferedImage;
    }

    /**
     * 拉伸图像
     *
     * @param bufferedImageOriginal 原始图像
     * @param width                 拉伸后宽
     * @param height                拉伸后高
     * @return 图像
     */
    public static BufferedImage imageStretch(BufferedImage bufferedImageOriginal, int width, int height) {
        // 画布
        BufferedImage bufferedImageAfter = new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR);
        // 画笔
        Graphics2D graphics = bufferedImageAfter.createGraphics();
        // 抗锯齿设置
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        // 画字
        graphics.drawImage(bufferedImageOriginal, 0, 0, width, height, null);
        // 释放
        graphics.dispose();
        return bufferedImageAfter;
    }

    /**
     * 画矩形外线（300X300）
     *
     * @param bufferedImage 原始图像
     * @return 图像
     */
    public static BufferedImage drawSquareOrder(BufferedImage bufferedImage) {
        BufferedImage bufferedImage1 = new BufferedImage(IMAGE_SIZE, IMAGE_SIZE, bufferedImage.getType());
        Graphics2D graphics1 = bufferedImage1.createGraphics();
        graphics1.setPaint(COLOR);
        graphics1.drawImage(bufferedImage, 0, 0, IMAGE_SIZE, IMAGE_SIZE, null);
        graphics1.setStroke(new BasicStroke((float) (IMAGE_SIZE * 0.053)));
        graphics1.drawRect(0, 0, IMAGE_SIZE, IMAGE_SIZE);
        graphics1.dispose();
        return bufferedImage1;
    }
}
