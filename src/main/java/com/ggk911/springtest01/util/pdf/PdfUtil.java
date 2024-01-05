package com.ggk911.springtest01.util.pdf;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.ggk911.springtest01.util.ImageUtil;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Image;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.AcroFields;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.FontSelector;
import com.itextpdf.text.pdf.GrayColor;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfDictionary;
import com.itextpdf.text.pdf.PdfName;
import com.itextpdf.text.pdf.PdfObject;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;
import com.itextpdf.text.pdf.PdfString;
import com.itextpdf.text.pdf.TextField;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.SneakyThrows;
import org.bouncycastle.util.encoders.Base64;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * pdf操作工具类
 *
 * @author TangHaoKai
 * @version V1.0 2023-10-19 11:19
 **/
public class PdfUtil {

    /**
     * 基础字体
     */
    private static final BaseFont BASE_FONT;
    /**
     * 备用字体-金标宋
     */
    private static final BaseFont SPARE_FONT;
    /**
     * 备用字体2-宋-extB
     */
    private static final BaseFont SPARE_FONT2;

    static {
        try {
            BASE_FONT = BaseFont.createFont("STSong-Light", "UniGB-UCS2-H", BaseFont.NOT_EMBEDDED);
            Font font1 = FontFactory.getFont("src/main/resources/font/JinbiaoSong.TTF", BaseFont.IDENTITY_H, BaseFont.NOT_EMBEDDED, 12f, Font.NORMAL, BaseColor.BLACK);
            Font font2 = FontFactory.getFont("src/main/resources/font/simsunb.ttf", BaseFont.IDENTITY_H, BaseFont.NOT_EMBEDDED, 12f, Font.NORMAL, BaseColor.BLACK);
            SPARE_FONT = font1.getBaseFont();
            SPARE_FONT2 = font2.getBaseFont();
        } catch (Exception e) {
            throw new RuntimeException("加载字体异常");
        }
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class FillImageParam {
        /**
         * KEY(域名)
         * 暂无用，一般在外部有KEY
         */
        String key;
        /**
         * 图片base64
         */
        String base64;
        /**
         * 旋转角度
         */
        String rotate;
        /**
         * 是否铺满域
         */
        String isTile;
        /**
         * 是否居中域
         */
        String isCenter;
    }

    /**
     * 文本域、复选框填充
     * 遍历要填充的key，非法key抛出异常
     *
     * @param pdfByte          pdf
     * @param textDomainParams 填充数据JSON<key,value>格式
     * @return 待签署文件
     */
    public static byte[] pdfFill(byte[] pdfByte, String textDomainParams) {
        JSONObject jsonObject = JSON.parseObject(textDomainParams);
        Map<String, String> toStringMap = jsonObject.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, entry -> String.valueOf(entry.getValue())));
        return pdfFill(pdfByte, toStringMap);
    }

    /**
     * 文本域、复选框填充
     * 根据key填充pdf模板中的文本域，非法key抛出异常
     *
     * @param pdfByte  pdf
     * @param fillData <key, value> 填充数据
     * @return 填充后的pdf
     */
    public static byte[] pdfFill(byte[] pdfByte, Map<String, String> fillData) {
        Map<String, Object> stringObjectMap = fillData.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        return pdfFill(pdfByte, stringObjectMap, null);
    }

    /**
     * 文本域、复选框、图片填充
     * 单行文本域如文本宽度超过设置文本域宽度，字段缩小fontsize，直至为6报异常超限
     * 多行文本域如行数超限，如上
     * 生僻字支持
     * 复选框支持
     * 图片支持
     *
     * @param pdfByte  pdf
     * @param fillData 填充MAP value可以是普通字符串，也可以是图片填充对象FillImageParam
     * @param picByte  填充图片 填充图片默认使用FillImageParam中的Base64，如为空则用此
     * @return 填充后的pdf
     */
    public static byte[] pdfFill(byte[] pdfByte, Map<String, Object> fillData, byte[] picByte) {
        PdfReader reader;
        try {
            reader = new PdfReader(new PdfReader(pdfByte));
        } catch (IOException e) {
            throw new RuntimeException("读取文件数据异常");
        }
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        PdfStamper stamper;
        try {
            stamper = new PdfStamper(reader, bos);
        } catch (DocumentException | IOException e) {
            throw new RuntimeException("pdf读取异常");
        }
        ArrayList<BaseFont> fontList = new ArrayList<>();
        fontList.add(BASE_FONT);
        fontList.add(SPARE_FONT);
        fontList.add(SPARE_FONT2);
        AcroFields form = stamper.getAcroFields();
        // if (form.getFields().size() == 0) {
        //     throw new RuntimeException("无域信息");
        // }
        form.setSubstitutionFonts(fontList);
        // if (MapUtil.isEmpty(fillData)) {
        //     throw new RuntimeException("填充参数为空");
        // }
        try {
            for (Map.Entry<String, Object> next : fillData.entrySet()) {
                String key = next.getKey();
                if (form.getFieldItem(key) == null) {
                    throw new RuntimeException("未知参数" + key);
                }
                Object value = next.getValue();
                // 文本类型
                if (value instanceof String) {
                    fillText(form, key, String.valueOf(value));
                }
                // 填充的是图片类型
                if (value instanceof FillImageParam) {
                    FillImageParam fillImage = (FillImageParam) value;
                    if (StrUtil.isEmpty(fillImage.getBase64())) {
                        if (picByte == null) {
                            throw new RuntimeException("此域无图片数据KEY：" + key);
                        }
                        fillImage(stamper, picByte, form, key, Boolean.parseBoolean(fillImage.getIsTile()), Boolean.parseBoolean(fillImage.getIsCenter()), StrUtil.isEmpty(fillImage.getRotate()) ? 0D : Double.parseDouble(fillImage.getRotate()));
                    } else {
                        byte[] picBase64Decode;
                        try {
                            picBase64Decode = Base64.decode(fillImage.getBase64());
                        } catch (Exception e) {
                            throw new RuntimeException("图片Base64转换失败");
                        }
                        fillImage(stamper, picBase64Decode, form, key, Boolean.parseBoolean(fillImage.getIsTile()), Boolean.parseBoolean(fillImage.getIsCenter()), StrUtil.isEmpty(fillImage.getRotate()) ? 0D : Double.parseDouble(fillImage.getRotate()));
                    }
                }
            }
        } catch (IOException | DocumentException e) {
            throw new RuntimeException("设置文本域内容异常");
        }
        // 如果为false那么生成的PDF文件还能编辑
        stamper.setFormFlattening(true);
        try {
            stamper.close();
        } catch (DocumentException | IOException e) {
            throw new RuntimeException("关闭pdf解析工具异常");
        }
        byte[] returnBytes = bos.toByteArray();
        try {
            bos.close();
        } catch (IOException e) {
            throw new RuntimeException("解析读取PDF复制PDF异常PdfCopy");
        }
        reader.close();
        return returnBytes;
    }

    /**
     * 获取PDF参数
     *
     * @param bytes                 模板
     * @param templateParameterList 参数list
     * @param templateId            模板ID
     */
    public static void getPdfDomain(byte[] bytes, List<PdfParameterEntity> templateParameterList, String templateId) {
        PdfReader reader;
        PdfStamper stamper;
        try {
            reader = new PdfReader(new PdfReader(bytes));
        } catch (Exception e) {
            throw new RuntimeException("参数上传模板-解析文本域-读取文件数据异常");
        }
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            stamper = new PdfStamper(reader, bos);
        } catch (Exception e) {
            throw new RuntimeException("参数上传模板-解析文本域-pdf读取异常");
        }
        AcroFields form = stamper.getAcroFields();
        if (ObjectUtil.isNull(form)) {
            throw new RuntimeException("参数上传模板-解析文本域-请根据模板要求上传数据");
        }
        try {
            for (String name : form.getFields().keySet()) {
                //获取key值
                //获取域的X轴，Y轴
                List<AcroFields.FieldPosition> list = form.getFieldPositions(name);
                AcroFields.FieldPosition fieldPosition = list.get(0);
                //所在页数
                int page = fieldPosition.page;
                //域名类型
                int fieldType = reader.getAcroFields().getFieldType(name);
                EnumPdfDomainType pdfType = EnumPdfDomainType.getEnumByPdfCode(String.valueOf(fieldType));
                TextField textField = null;
                //字体大小
                float size = 0;
                //这里是筛选表单文本域的
                if (EnumPdfDomainType.TEXT_DOMAIN.getCode().equals(pdfType.getCode())) {
                    PdfDictionary merged = form.getFieldItem(name).getMerged(0);
                    textField = new TextField(null, null, name);
                    form.decodeGenericDictionary(merged, textField);
                    size = textField.getFontSize();
                }
                // 模板参数构造
                PdfParameterEntity templateParameter = PdfParameterEntity.builder()
                        .templateId(templateId)
                        .pageNo(page + "")
                        .positionXl(fieldPosition.position.getLeft() + "")
                        .positionXr(fieldPosition.position.getRight() + "")
                        .positionYl(fieldPosition.position.getBottom() + "")
                        .positionYr(fieldPosition.position.getTop() + "")
                        .keyword(name)
                        .type(pdfType.getCode())
                        .textFont(textField == null ? null : textField.getFont().getFullFontName()[0][3])
                        .textSize(String.valueOf(size))
                        .textLength(String.valueOf(fieldPosition.position.getRight() - fieldPosition.position.getLeft()))
                        .createTime(DateUtil.date())
                        .build();
                templateParameterList.add(templateParameter);
            }
        } catch (Exception e) {
            throw new RuntimeException("参数上传模板-解析文本域-设置文本域内容异常");
        }
        try {
            stamper.close();
            bos.close();
            reader.close();
        } catch (Exception e) {
            throw new RuntimeException("参数上传模板-解析文本域-关闭pdf解析工具异常");
        }
    }

    /**
     * PDF填充图片通过坐标
     *
     * @param stamper pdf文件
     * @param picByte 图片
     * @param page    页数
     * @param x       图片左下坐标X
     * @param y       图片左下坐标Y
     */
    private static void fillImage(PdfStamper stamper, byte[] picByte, int page, float x, float y) {
        fillImageByCoordinate(stamper, picByte, page, x, y, null, null);
    }

    /**
     * PDF填充图片通过坐标
     *
     * @param stamper pdf文件
     * @param picByte 图片
     * @param page    页数
     * @param x       图片左下坐标X
     * @param y       图片左下坐标Y
     * @param rotate  旋转角度 正数：顺时针 负数：逆时针
     */
    private static void fillImage(PdfStamper stamper, byte[] picByte, int page, float x, float y, Double rotate) {
        if (rotate != null && rotate != 0) {
            picByte = rotate(picByte, rotate);
        }
        fillImageByCoordinate(stamper, picByte, page, x, y, null, null);
    }

    /**
     * PDF填充图片通过域
     *
     * @param stamper  pdf文件
     * @param picByte  图片
     * @param form     pdf所有文本域
     * @param key      要填充的文本域
     * @param isCenter 是否居中文本域
     */
    private static void fillImage(PdfStamper stamper, byte[] picByte, AcroFields form, String key, boolean isCenter) {
        fillImageByField(stamper, picByte, form, key, false, isCenter);
    }

    /**
     * PDF填充图片通过域
     *
     * @param stamper  pdf文件
     * @param picByte  图片
     * @param form     pdf所有文本域
     * @param key      要填充的文本域
     * @param isScale  是否铺满整个文本域
     * @param isCenter 是否居中文本域
     * @param rotate   旋转角度 正数：顺时针 负数：逆时针
     */
    private static void fillImage(PdfStamper stamper, byte[] picByte, AcroFields form, String key, boolean isScale, boolean isCenter, Double rotate) {
        // 旋转
        if (rotate != null && rotate != 0) {
            picByte = rotate(picByte, rotate);
        }
        fillImageByField(stamper, picByte, form, key, isScale, isCenter);
    }

    /**
     * PDF填充图片通过域
     *
     * @param stamper  pdf文件
     * @param picByte  图片
     * @param form     pdf所有文本域
     * @param key      要填充的文本域
     * @param isScale  是否铺满整个文本域
     * @param isCenter 是否居中文本域
     */
    @SneakyThrows
    private static void fillImageByField(PdfStamper stamper, byte[] picByte, AcroFields form, String key, boolean isScale, boolean isCenter) {
        // 通过域名获取所在页和坐标，左下角为起点
        int pageNo = form.getFieldPositions(key).get(0).page;
        Rectangle signRect = form.getFieldPositions(key).get(0).position;
        // 图片位置默认与左下角对齐
        float x = signRect.getLeft();
        float y = signRect.getBottom();
        // 缩放
        if (isScale) {
            picByte = scale(picByte, signRect.getWidth(), signRect.getHeight());
        }
        // 计算居中坐标
        if (isCenter) {
            ByteArrayInputStream inputStream = new ByteArrayInputStream(picByte);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ImageUtil.scaleBySizeKeepRatio(inputStream, outputStream, (int) signRect.getWidth(), (int) signRect.getHeight());
            Image image = Image.getInstance(outputStream.toByteArray());
            x = signRect.getLeft() + (signRect.getWidth() / 2 - image.getWidth() / 2);
            y = signRect.getBottom() + (signRect.getHeight() / 2 - image.getHeight() / 2);
        }
        // 有了坐标
        fillImageByCoordinate(stamper, picByte, pageNo, x, y, signRect.getWidth(), signRect.getHeight());
    }

    /**
     * PDF填充图片通过坐标
     *
     * @param stamper pdf文件
     * @param picByte 文件
     * @param page    页数
     * @param x       图片左下坐标X
     * @param y       图片左下坐标Y
     * @param width   宽
     * @param height  高
     */
    @SneakyThrows
    private static void fillImageByCoordinate(PdfStamper stamper, byte[] picByte, int page, float x, float y, Float width, Float height) {
        // 获取操作的页面
        PdfContentByte under = stamper.getOverContent(page);
        Image image = Image.getInstance(picByte);
        // 根据高宽缩放图片
        if (width != null && height != null) {
            image.scaleToFit(width, height);
        } else {
            image.scaleToFit(image.getWidth(), image.getHeight());
        }
        image.setAbsolutePosition(x, y);
        under.addImage(image);
    }

    /**
     * 旋转图片
     *
     * @param picByte 图片
     * @param rotate  旋转角度 正数：顺时针 负数：逆时针
     * @return 旋转后图片
     */
    @SneakyThrows
    private static byte[] rotate(byte[] picByte, Double rotate) {
        ByteArrayOutputStream imageByte = new ByteArrayOutputStream();
        Image instance = Image.getInstance(picByte);
        ImageUtil.rotateByAngle(new ByteArrayInputStream(picByte), imageByte, (int) instance.getWidth(), (int) instance.getHeight(), rotate);
        return imageByte.toByteArray();
    }

    /**
     * 调整图片宽高
     *
     * @param picByte 图片
     * @param width   调整后宽度
     * @param height  调整后高度
     * @return 调整宽高后图片
     */
    @SneakyThrows
    private static byte[] scale(byte[] picByte, float width, float height) {
        ByteArrayOutputStream imageByte = new ByteArrayOutputStream();
        ImageUtil.scaleBySize(new ByteArrayInputStream(picByte), imageByte, (int) width, (int) height);
        return imageByte.toByteArray();
    }

    /**
     * 文本填充
     *
     * @param form  所有pdf域
     * @param key   KEY
     * @param value VALUE
     */
    private static void fillText(AcroFields form, String key, String value) throws IOException, DocumentException {
        // 获取设置字体大小，默认12
        float fontSize = getFontSize(form, key);
        // 字体选择器
        FontSelector fs = new FontSelector();
        ArrayList<BaseFont> substitutionFonts = form.getSubstitutionFonts();
        if (substitutionFonts != null) {
            for (BaseFont substitutionFont : substitutionFonts) {
                fs.addFont(new Font(substitutionFont, fontSize, 0, GrayColor.GRAYBLACK));
            }
        }
        Phrase phrase = fs.process(value);
        fontSize = judgeFontSize(fontSize, form, key, phrase);
        form.setFieldProperty(key, "textsize", fontSize, null);
        // 域类型
        int fieldType = form.getFieldType(key);
        EnumPdfDomainType pdfType = EnumPdfDomainType.getEnumByPdfCode(String.valueOf(fieldType));
        // 复选框
        if (pdfType == EnumPdfDomainType.CHECKBOX_DOMAIN) {
            if (Boolean.TRUE.toString().equalsIgnoreCase(value) || "是".equals(value) || "1".equals(value)) {
                form.setField(key, "true", true);
                // thk's todo 在低版本itext中的 叉 会有bug,没有边框
            } else if (Boolean.FALSE.toString().equalsIgnoreCase(value) || "否".equals(value) || "0".equals(value)) {
                form.setField(key, "true", false);
            } else {
                System.out.println("此复选框：" + key + "，值错误");
                // thk's todo 是否抛出
                // throw new RuntimeException(EnumException.TEMP_FILL_ERROR).setMessage("此复选框：" + key + "，值错误，");
            }
        } else {
            form.setField(key, value);
        }
    }

    /**
     * 获取带字体的Phrase的宽度
     *
     * @param phrase   要填充的文本(由多个chunk组成的文本)
     * @param fontSize 字体大小
     * @return 总长度
     */
    private static float getPhraseAllChunkWidth(Phrase phrase, float fontSize) {
        float widthPoint = 0;
        List<Chunk> chunkList = phrase.getChunks();
        for (Chunk chunk : chunkList) {
            BaseFont baseFont = chunk.getFont().getBaseFont();
            widthPoint += baseFont.getWidthPoint(chunk.getContent(), fontSize);
        }
        return widthPoint;
    }

    /**
     * 获取设置字体大小
     *
     * @param form 所有文本域
     * @param key  域
     * @return 字体大小
     */
    private static float getFontSize(AcroFields form, String key) {
        PdfString da = form.getFieldItem(key).getMerged(0).getAsString(PdfName.DA);
        if (da != null) {
            Object[] dab = AcroFields.splitDAelements(da.toUnicodeString());
            if (dab[AcroFields.DA_SIZE] != null) {
                return (float) dab[AcroFields.DA_SIZE];
            }
        }
        // 默认字体大小
        return 12;
    }

    /**
     * 判断字体适合大小（针对文本框）
     *
     * @param fontSize 字体大小
     * @param form     文本域
     * @param key      KEY
     * @param phrase   VALUE
     * @return 适合大小
     */
    private static float judgeFontSize(float fontSize, AcroFields form, String key, Phrase phrase) {
        // 渲染后文字宽度
        float textWidth = getPhraseAllChunkWidth(phrase, fontSize);
        // 文本单行高度(只算上升ASCENT，不算下降)
        float ascent = BASE_FONT.getFontDescriptor(BaseFont.ASCENT, fontSize);
        // float deascent = BASE_FONT.getFontDescriptor(BaseFont.DESCENT, fontSize);
        // 文本框宽度
        Rectangle position = form.getFieldPositions(key).get(0).position;
        float textBoxWidth = position.getWidth();
        // 文本框高度
        float textBoxHeight = position.getHeight();
        // 理想多行文本框行数
        double line = Math.ceil(textWidth / textBoxWidth);
        // 总的文字宽度(因为实际渲染出来每行始终有一个字的误差)
        float totalTextWidth = (float) (textWidth + line * fontSize);
        // 实际行数
        double realLine = Math.ceil(totalTextWidth / textBoxWidth);
        // 文本框最多显示全行数
        double boxMaxLine = Math.floor(textBoxHeight / (ascent * 1.25));
        PdfObject directObject = form.getFieldItem(key).getMerged(0).getDirectObject(PdfName.FF);
        // 文本框高度只够写一行，并且文字宽度大于文本宽度，则缩小字体
        // 判断单行文本
        if (directObject == null) {
            // 单行文本框
            while (textWidth > textBoxWidth) {
                fontSize--;
                if (fontSize < 6) {
                    System.out.println("字体长度超限");
                    throw new RuntimeException("文本域：" + key + "文本长度超限，请调整");
                }
                textWidth = getPhraseAllChunkWidth(phrase, fontSize);
            }
        } else {
            // 多行文本框
            while (boxMaxLine <= realLine) {
                fontSize--;
                if (fontSize < 6) {
                    System.out.println("字体长度超限");
                    throw new RuntimeException("文本域：" + key + "文本长度超限，请调整");
                }
                ascent = BASE_FONT.getFontDescriptor(BaseFont.ASCENT, fontSize);
                // 字体减小后的理想总长度
                textWidth = getPhraseAllChunkWidth(phrase, fontSize);
                // 字体减小后的文本框的最大行数
                boxMaxLine = Math.floor(textBoxHeight / (ascent * 1.25));
                // 字体减小后的理想行数
                line = Math.ceil(textWidth / textBoxWidth);
                // 字体减小后实际总长度
                totalTextWidth = (float) (textWidth + line * fontSize);
                // 实际行数
                realLine = Math.ceil(totalTextWidth / textBoxWidth);
            }
        }
        return fontSize;
    }
}
