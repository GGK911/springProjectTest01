package com.ggk911.springtest01.util.word;

import cn.hutool.core.io.IoUtil;
import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.core.util.HexUtil;
import com.aspose.words.Document;
import com.aspose.words.DocumentBuilder;
import com.aspose.words.HeaderFooterType;
import com.aspose.words.License;
import com.aspose.words.SaveFormat;
import lombok.SneakyThrows;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

/**
 * WORD文件工具类
 *
 * @author TangHaoKai
 * @version V1.0 2023-11-23 11:38
 **/
public class WordUtil {

    private static final String WORD_MAGIC = "D0CF11E0A1B11AE10000";

    /**
     * 老版本doc转docx
     *
     * @param doc 老版本word
     * @return 新版本word
     */
    @SneakyThrows
    public static byte[] docToDocx(byte[] doc) {
        // 版本
        byte[] fileByteBefore28 = new byte[28];
        System.arraycopy(doc, 0, fileByteBefore28, 0, fileByteBefore28.length);
        String hexStr = HexUtil.encodeHexStr(fileByteBefore28, false);
        // 根据魔数判断版本
        if (hexStr.startsWith(WORD_MAGIC)) {
            // 验证License 若不验证则转化出的pdf文档会有水印产生
            if (isNotAuthEdition()) {
                return null;
            }
            // (老版本word：doc)
            Document document = new Document(IoUtil.toStream(doc));
            try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
                document.save(outputStream, SaveFormat.DOCX);
                return outputStream.toByteArray();
            } catch (Exception e) {
                return null;
            }
        } else {
            // (新版本word：docx)
            return doc;
        }
    }

    /**
     * 不是正版（去除水印）
     *
     * @return 不是正版
     */
    public static boolean isNotAuthEdition() {
        boolean result = false;
        try {
            // license.xml应放在..\WebRoot\WEB-INF\classes路径下
            InputStream is = ResourceUtil.getResourceObj("file/license/Aspose.Words.Java.lic").getStream();
            License aposeLic = new License();
            aposeLic.setLicense(is);
            result = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return !result;
    }

    /**
     * Word 转 PDF
     *
     * @param docBytes word文件
     * @return pdf文件
     */
    public static byte[] docToPdf(byte[] docBytes) throws Exception {
        // 去除水印
        // 验证License 若不验证则转化出的pdf文档会有水印产生
        if (isNotAuthEdition()) {
            return null;
        }
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            Document doc = new Document(new ByteArrayInputStream(docBytes));
            DocumentBuilder builder = new DocumentBuilder(doc);
            // 文档主体内容设置段后和行距
            builder.moveToDocumentStart();
            // 单倍行距 = 12 ， 1.5 倍 =
            builder.getParagraphFormat().setLineSpacing(12);
            // 18
            // 段后
            builder.getParagraphFormat().setSpaceAfter(0);
            // 页眉设置段后和行距
            builder.moveToHeaderFooter(HeaderFooterType.HEADER_PRIMARY);
            builder.getParagraphFormat().setLineSpacing(12);
            builder.getParagraphFormat().setSpaceAfter(0);
            // 页脚设置段后和行距
            builder.moveToHeaderFooter(HeaderFooterType.FOOTER_PRIMARY);
            builder.getParagraphFormat().setLineSpacing(12);
            builder.getParagraphFormat().setSpaceAfter(0);
            // 表格设置段后和行距
            // builder.moveToCell(0,0,0,0);
            // builder.getParagraphFormat().setLineSpacing(12);
            // builder.getParagraphFormat().setSpaceAfter(0);
            // 全面支持DOC, DOCX, OOXML, RTF HTML, OpenDocument, PDF
            doc.save(bos, SaveFormat.PDF);
            return bos.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
}
