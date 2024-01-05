package com.ggk911.springtest01.util.word;

import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.CharUtil;
import cn.hutool.core.util.HexUtil;
import cn.hutool.core.util.StrUtil;
import lombok.SneakyThrows;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.hwpf.usermodel.Range;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 解析WORD模板工具类
 *
 * @author TangHaoKai
 * @version V1.0 2023-11-23 11:42
 **/
public class WordTemplateUtil {
    public static final String START = "${";

    public static final String END = "}";

    public static final String BLANK = "                                                                                                                ";

    public static final String WORD_MAGIC = "D0CF11E0A1B11AE10000";

    /**
     * 获取word文档的所有变量
     *
     * @param wordBytes          word文件
     * @param varList            要添入的List
     * @param detailTableVarList 表格
     */
    @SneakyThrows
    public static void getWordAllVars(byte[] wordBytes, List<String> varList, List<Map<String, List<String>>> detailTableVarList) {
        // 版本
        byte[] fileByteBefore28 = new byte[28];
        System.arraycopy(wordBytes, 0, fileByteBefore28, 0, fileByteBefore28.length);
        String hexStr = HexUtil.encodeHexStr(fileByteBefore28, false);
        if (hexStr.startsWith(WORD_MAGIC)) {
            // (老版本word：doc)
            getWordAllVars(new HWPFDocument(new POIFSFileSystem(IoUtil.toStream(wordBytes))), varList);
        } else {
            // (新版本word：docx)
            getWordAllVars(new XWPFDocument(IoUtil.toStream(wordBytes)), varList, detailTableVarList);
        }
    }

    /**
     * 填充Word文件
     *
     * @param wordBytes       word文件
     * @param paramMap        key与word文件form域中{}的参数名一致，value为替换值
     * @param detailTableData word模板中普通表格的参数
     * @return 填充后的文件
     */
    @SneakyThrows
    public static byte[] setDocxParam(byte[] wordBytes, Map<String, Object> paramMap, Map<String, List<Map<String, String>>> detailTableData) {
        // 版本
        byte[] fileByteBefore28 = new byte[28];
        System.arraycopy(wordBytes, 0, fileByteBefore28, 0, fileByteBefore28.length);
        String hexStr = HexUtil.encodeHexStr(fileByteBefore28, false);
        if (hexStr.startsWith(WORD_MAGIC)) {
            // (老版本word：doc)
            byte[] toDocx = WordUtil.docToDocx(wordBytes);
            if (null == toDocx) {
                return setDocxParam(new HWPFDocument(new POIFSFileSystem(IoUtil.toStream(wordBytes))), paramMap);
            } else {
                // 这里已经转成docx了
                return setDocxParam(new XWPFDocument(IoUtil.toStream(toDocx)), paramMap, detailTableData);
            }
        } else {
            // (新版本word：docx)
            return setDocxParam(new XWPFDocument(IoUtil.toStream(wordBytes)), paramMap, detailTableData);
        }
    }

    /**
     * 填充Word文件(新版本word：docx)
     *
     * @param doc             word
     * @param paramMap        key与word文件form域中{}的参数名一致，value为替换值
     * @param detailTableData word模板中普通表格的参数（不带{}的）
     * @return 填充后文件
     */
    @SneakyThrows
    public static byte[] setDocxParam(XWPFDocument doc, Map<String, Object> paramMap, Map<String, List<Map<String, String>>> detailTableData) {
        // 替换段落里面的变量
        replaceVarInStage(doc, paramMap);
        // 替换表格里面的变量
        replaceVarInTable(doc, paramMap, detailTableData);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        doc.write(bos);
        doc.close();
        return bos.toByteArray();
    }

    /**
     * 填充Word文件(老版本word：doc)
     *
     * @param doc      word
     * @param paramMap 填充参数
     * @return 填充后文件
     */
    @SneakyThrows
    public static byte[] setDocxParam(HWPFDocument doc, Map<String, Object> paramMap) {
        Range range = doc.getRange();
        // 替换内容
        for (Map.Entry<String, Object> entry : paramMap.entrySet()) {
            range.replaceText(START + entry.getKey() + END, entry.getValue().toString());
        }
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        doc.write(byteArrayOutputStream);
        doc.close();
        return byteArrayOutputStream.toByteArray();
    }

    /**
     * 获取word文档的【所有变量】(新版本word：docx)
     */
    @SneakyThrows
    public static void getWordAllVars(XWPFDocument doc, List<String> varList, List<Map<String, List<String>>> detailTableVarList) {
        // 获取word整个文档里段落的变量
        getWordStageVars(doc, varList);
        // 获取表格里面的变量 (包括普通表格和明细列表)
        getTableVars(doc, varList, detailTableVarList);
        doc.close();
    }

    /**
     * 获取word文档的【所有变量】(老版本word：doc)
     *
     * @param doc     word
     * @param varList 获取的变量列表
     */
    @SneakyThrows
    public static void getWordAllVars(HWPFDocument doc, List<String> varList) {
        // 读取2003版本word中的文本
        WordExtractor we = new WordExtractor(doc);
        String text = we.getText();
        String tempParam;
        // 匹配是否存在变量
        while (matcher(text).find()) {
            // 获取第一个参数
            tempParam = getFirstParm(text);
            // 删除第一个参数
            text = text.substring(text.indexOf(tempParam) + tempParam.length());
            // 添加参数到参数列表中
            addParam(varList, tempParam);
        }
        doc.close();
    }

    /**
     * 获取word整个文档里【段落的变量】
     */
    public static void getWordStageVars(XWPFDocument doc, List<String> varList) {
        Iterator<XWPFParagraph> iterator = doc.getParagraphsIterator();
        XWPFParagraph row;
        // 获取一行文本当中的变量
        while (iterator.hasNext()) {
            row = iterator.next();
            getRowVars(row, varList);
        }
    }

    /**
     * 获取一行文本当中的【行变量】
     *
     * @param row     一行文本
     * @param varList 获取的变量列表
     */
    private static void getRowVars(XWPFParagraph row, List<String> varList) {
        // 一行 转换为纯文本
        String rowStr = row.getParagraphText();
        String tempParam;
        // 匹配是否存在变量
        while (matcher(rowStr).find()) {
            // 获取第一个参数
            tempParam = getFirstParm(rowStr);
            // 删除第一个参数
            rowStr = rowStr.substring(rowStr.indexOf(tempParam) + tempParam.length());
            // 添加参数到参数列表中
            addParam(varList, tempParam);
        }
    }

    /**
     * 获取表格里面的变量 (包括普通表格和明细列表)
     *
     * @param doc                要查找的文档
     * @param varList            普通变量列表
     * @param detailTableVarList 明细列表变量列表
     */
    private static void getTableVars(XWPFDocument doc, List<String> varList,
                                     List<Map<String, List<String>>> detailTableVarList) {
        Iterator<XWPFTable> iterator = doc.getTablesIterator();
        XWPFTable table;
        while (iterator.hasNext()) {
            table = iterator.next();
            // 表格第一行第一个单元格的值
            String temp = table.getRows().get(0).getTableCells().get(0).getText();
            // 判断是否是明细表格
            if (temp.contains(START) && temp.contains(END)) {
                // 获取明细列表的变量
                getDetailTableVar(table, detailTableVarList);
            } else {
                // 获取普通表格的变量
                getCommTableVar(table, varList);
            }
        }
    }

    /**
     * 【获取】【明细列表】【变量】
     */
    private static void getDetailTableVar(XWPFTable table, List<Map<String, List<String>>> detailTableVarList) {
        List<XWPFTableRow> rows = table.getRows();
        List<XWPFTableCell> cells;
        // 明细列表列字段
        List<String> varList = new ArrayList<>();
        // 一个明细列表的变量
        Map<String, List<String>> map = new HashMap<>();
        // 第一行取明细表名称 作为主键对应业务对象
        String key = rows.get(0).getCell(0).getText().replace(START, "").replace(END, "");

        // 第二行取单元格内容作为列变更对就业务对象
        cells = rows.get(1).getTableCells();
        for (XWPFTableCell cell : cells) {
            addParam(varList, cell.getText());
        }
        // 组装明细列表变更
        map.put(key, varList);
        // 返回变量
        detailTableVarList.add(map);
    }

    /**
     * 【获取】【普通表格】【变量】
     */
    private static void getCommTableVar(XWPFTable table, List<String> varList) {
        List<XWPFTableRow> rows = table.getRows();
        List<XWPFTableCell> cells;
        String temp;
        String tempParam;
        for (XWPFTableRow row : rows) {
            cells = row.getTableCells();
            for (XWPFTableCell cell : cells) {
                temp = cell.getText();
                // 匹配是否存在变量
                while (matcher(temp).find()) {
                    // 获取第一个参数
                    tempParam = getFirstParm(temp);
                    // 删除第一个参数
                    temp = temp.replace(tempParam, "");
                    // 添加参数到参数列表中
                    addParam(varList, tempParam);
                }
            }
        }
    }

    /**
     * 【替换】【普通文本】【变量】 替换段落里面的变量
     *
     * @param doc    要替换的文档
     * @param params 参数
     */
    public static void replaceVarInStage(XWPFDocument doc, Map<String, Object> params) {
        Iterator<XWPFParagraph> iterator = doc.getParagraphsIterator();
        // 按行替换变量
        while (iterator.hasNext()) {
            replaceVarInRow(iterator.next(), params, true);
        }
    }

    /**
     * 替换 行的变量
     *
     * @param rowText 要替换的段落
     * @param params  参数 //
     *                直接调用XWPFRun的setText()方法设置文本时，在底层会重新创建一个XWPFRun，把文本附加在当前文本后面，
     *                // 所以我们不能直接设值，需要先删除当前run,然后再自己手动插入一个新的run。
     */
    private static void replaceVarInRow(XWPFParagraph rowText, Map<String, Object> params, boolean isUnderLine) {
        List<XWPFRun> runs;
        Matcher matcher;
        if (matcher(rowText.getParagraphText()).find()) {
            // 提前邓处理,把多个run和成一个 ,因为有时一个变量被word拆分到多个run中了
            runs = combineRuns(rowText).getRuns();
            // 下面开始处理run,把变量替换成 传入的参数
            for (int i = 0; i < runs.size(); i++) {
                XWPFRun run = runs.get(i);
                String runText = run.toString();
                matcher = matcher(runText);
                if (matcher.find()) {
                    Object value = null;
                    int varLength = 0;
                    int dataLength = 0;
                    while ((matcher = matcher(runText)).find()) {
                        varLength = matcher.group(1).getBytes().length + 3;
                        value = params.get(matcher.group(1).trim());
                        // runText =
                        // matcher.replaceFirst(replaceNum(String.valueOf(value
                        // == null ? " /" : value)));
                        runText = matcher.replaceFirst(replaceNum(String.valueOf(value == null ? "/" : String.valueOf(value).contains("\\") ? String.valueOf(value).replace("\\", "\\\\") : value)));
                        dataLength = runText.getBytes().length;
                    }
                    // 变量与数据参数长度不一致时的处理,保证word文件的格式不变
                    if (varLength >= dataLength) {
                        if (null == value || isEmpty(value.toString())) {// 传入值为空时，也需要显示,长度为参数长度
                            runText = runText + BLANK.substring(0, varLength);
                        }
                    } else {
                        // 数据长度大于变量长度时,先去掉前空格,不够再去掉后空格
                        handleDataLength(rowText, i, dataLength - varLength);
                    }
                    updateRuns(rowText, i, runText, isUnderLine);
                } else {
                    if (runText.contains(START) || runText.contains(END)) {
                        String temp = runText.replace(START, "").replace(END, "");
                        updateRuns(rowText, i, temp, isUnderLine);
                    }
                }
            }
        }
    }

    /**
     * 获取字符串显示的长度（汉字占2，字母数字占1）
     *
     * @param str 字符
     * @return 显示的长度
     */
    public static int getStringViewLength(String str) {
        if (StrUtil.isEmpty(str)) {
            return 0;
        }
        int length = 0;
        for (char letter : str.toCharArray()) {
            if (letter == '/') {
                length += 1;
                continue;
            }
            if (isLetter(letter)) {
                length += 1;
            } else {
                length += 2;
            }
        }
        return length;
    }

    public static boolean isLetter(char c) {
        Character.UnicodeBlock ub = Character.UnicodeBlock.of(c);
        if (ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS // 汉字
                || ub == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS // 汉字（兼容字）
                || ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A // 汉字（扩展A）
                || ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_B // 汉字（扩展B）
                || ub == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION // 中日韩符号和标点
                || ub == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS // 半角和全角字符
        ) {
            return false;
        } else {
            return CharUtil.isLetterOrNumber(c); // 英文字母
        }
    }

    /**
     * runs 中有变量被拆分,现在做合并处理
     */
    private static XWPFParagraph combineRuns(XWPFParagraph rowText) {
        List<XWPFRun> runs = rowText.getRuns();
        XWPFRun run;
        String runText;
        int cnt;

        for (int i = 0; i < runs.size(); i++) {
            cnt = 0;
            run = runs.get(i);
            runText = run.toString();
            while (runText.contains(START) && isNotHaveEnd(runText, START)) {
                cnt++;
                XWPFRun tempRun = runs.get(i + cnt);
                runText = runText + tempRun.toString();// 合并后的数据
            }
            // 如果标签以${开头 需要合并$,{分开情况
            while (runText.contains("$") && isNotHaveEnd(runText, "$")) {
                cnt++;
                XWPFRun tempRun = runs.get(i + cnt);
                runText = runText + tempRun.toString();// 合并后的数据
            }
            while (runText.contains("{") && isNotHaveEnd(runText, "{")) {
                cnt++;
                XWPFRun tempRun = runs.get(i + cnt);
                runText = runText + tempRun.toString();// 合并后的数据
            }

            // 删除原有的run,合并成新的run
            if (cnt > 0) {
                updateRuns(rowText, i, runText, true);
                for (int j = 1; j <= cnt; j++) {
                    updateRuns(rowText, i + j, "", true);
                }
            }
        }
        runs = rowText.getRuns();
        return rowText;
    }

    private static boolean isNotHaveEnd(String runText, String START) {
        runText = runText.substring(runText.lastIndexOf(START));
        return !runText.contains(END);
    }

    private static String replaceNum(String item) {
        return item;
    }

    /**
     * 替换表格里面的变量
     *
     * @param doc             要替换的文档
     * @param params          参数（包含明细表格中带有{}的变量）
     * @param detailTableData 普通表格的变量
     */
    public static void replaceVarInTable(XWPFDocument doc, Map<String, Object> params, Map<String, List<Map<String, String>>> detailTableData) {
        Iterator<XWPFTable> iterator = doc.getTablesIterator();
        XWPFTable table;
        while (iterator.hasNext()) {
            table = iterator.next();
            // 表格第一行第一个单元格的值
            String temp = table.getRows().get(0).getTableCells().get(0).getText();
            // 判断是否是明细表格
            if (temp.contains(START) && temp.contains(END)) {
                // 处理明细列表的变量
                handleDetailTableVar(table, detailTableData);
            } else {
                // 处理普通表格的变量
                handleCommTableVar(table, params);
            }
        }
    }

    /**
     * 处理普通表格变更
     */
    private static void handleCommTableVar(XWPFTable table, Map<String, Object> params) {
        List<XWPFTableRow> rows = table.getRows();
        List<XWPFTableCell> cells;
        List<XWPFParagraph> paras;
        for (XWPFTableRow row : rows) {
            cells = row.getTableCells();
            for (XWPFTableCell cell : cells) {
                paras = cell.getParagraphs();
                for (XWPFParagraph para : paras) {
                    replaceVarInRow(para, params, false);
                }
            }
        }
    }

    /**
     * 处理明细表格变量
     */
    private static void handleDetailTableVar(XWPFTable table, Map<String, List<Map<String, String>>> detailTableData) {
        // 表格第一行第一个单元格的值
        String temp = table.getRows().get(0).getTableCells().get(0).getText().replace(START, "").replace(END, "").trim();
        // 替换明细表内容
        replaceTableDetails(table, detailTableData.get(temp));
    }

    /**
     * 替换》》表格明细数据
     */
    private static void replaceTableDetails(XWPFTable table, List<Map<String, String>> dataList) {
        List<XWPFTableRow> rows;
        List<XWPFTableCell> cells;
        XWPFTableRow row = null;
        List<String> titleList = new ArrayList<>();
        rows = table.getRows();
        // 修改第一行的标题
        replaceTableTitle(rows.get(0).getCell(0));
        if (null == dataList || dataList.size() < 1) {
            return;
        }

        // 获取表格的标题栏顺序
        for (XWPFTableCell cell : rows.get(1).getTableCells()) {
            titleList.add(cell.getText());
        }
        // 新增》》表格数据行
        for (int n = 1; n < dataList.size(); n++) {
            createBankRow(table, titleList.size());
        }
        rows = table.getRows();
        // 修改数据行内容
        Object value = null;
        for (int i = 2; i - 2 < dataList.size(); i++) {
            row = rows.get(i);
            // 复制行属性
            row.getCtRow().setTrPr(rows.get(2).getCtRow().getTrPr());
            cells = row.getTableCells();
            for (int j = 0; j < cells.size(); j++) {
                // 复制列属性
                cells.get(j).getCTTc().setTcPr(rows.get(2).getCell(j).getCTTc().getTcPr());
                // 复制段落属性
                cells.get(j).getParagraphs().get(0).getCTP()
                        .setPPr(rows.get(2).getCell(j).getParagraphs().get(0).getCTP().getPPr());
                value = dataList.get(i - 2).get(titleList.get(j));
                cells.get(j).setText(value == null ? "" : String.valueOf(value));
            }
        }
    }

    /**
     * 替换标题行内容
     */
    private static void replaceTableTitle(XWPFTableCell cell) {
        List<XWPFParagraph> paras = cell.getParagraphs();
        List<XWPFRun> runs;
        for (XWPFParagraph para : paras) {
            runs = para.getRuns();
            for (int i = 0; i < runs.size(); i++) {
                XWPFRun run = runs.get(i);
                String runText = run.toString();
                para.removeRun(i);
                para.insertNewRun(i).setText(runText.replace(START, "").replace(END, ""));
            }
        }
    }

    /**
     * 明细表 创建空白行
     */
    private static XWPFTableRow createBankRow(XWPFTable table, int cellSize) {
        XWPFTableRow Row = table.createRow();
        for (int i = 1; i < cellSize; i++) {
            Row.addNewTableCell();
        }
        return Row;
    }

    /**
     * 正则匹配字符串
     */
    private static Matcher matcher(String str) {
        Pattern pattern;
        pattern = Pattern.compile("\\$\\{(.+?)\\}", Pattern.CASE_INSENSITIVE);
        return pattern.matcher(str);
    }

    /**
     * 关闭输入流
     */
    public static void close(InputStream is) throws IOException {
        if (is != null) {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
                throw e;
            }
        }
    }

    /**
     * 关闭输出流
     */
    public static void close(OutputStream os) throws IOException {
        if (os != null) {
            try {
                os.close();
            } catch (IOException e) {
                e.printStackTrace();
                throw e;
            }
        }
    }

    /**
     * 文本为空
     */
    private static boolean isEmpty(String str) {
        if (null == str) {
            return true;
        }
        return str.trim().length() < 1;
    }

    /**
     * 获取文本中的第一个变量
     */
    private static String getFirstParm(String rowStr) {
        rowStr = rowStr.substring(rowStr.indexOf(START), rowStr.indexOf(END, rowStr.indexOf(START)) + 1);
        while (rowStr.indexOf(START, 2) > 0) {
            rowStr = rowStr.substring(rowStr.indexOf(START, 2), rowStr.indexOf(END) + 1);
        }
        return rowStr;
    }

    /**
     * 添加变量进列表
     */
    private static void addParam(List<String> list, String param) {
        if (isEmpty(param)) {
            return;
        }
        param = param.replace(START, "").replace(END, "").trim();
        if (isEmpty(param)) {
            return;
        }
        // 判断变量是否存在列表当中
        if (list.contains(param)) {
            return;
        }
        list.add(param);
    }

    private static void handleDataLength(XWPFParagraph rowText, int index, int length) {
        List<XWPFRun> runs = rowText.getRuns();
        // 先去变量前面的空格
        String temp = "";
        for (int i = index - 1; i >= 0; i--) {
            temp = runs.get(i).toString();
            if (!temp.endsWith("  ")) {
                break;
            }
            while (temp.endsWith("  ")) {
                temp = temp.substring(0, temp.length() - 1);
                length--;
                if (length == 0) {
                    break;
                }
            }
            updateRuns(rowText, i, temp, true);
            if (length == 0) {
                break;
            }
        }
        // 先去变量后面的空格
        for (int i = index + 1; i < runs.size(); i++) {
            temp = runs.get(i).toString();
            if (!temp.startsWith(" ")) {
                break;
            }
            while (temp.startsWith(" ")) {
                temp = temp.substring(0, temp.length() - 1);
                length--;
                if (length == 0) {
                    break;
                }
            }
            updateRuns(rowText, i, temp, true);
            if (length == 0) {
                break;
            }
        }
    }

    /**
     * 一行中 替换runs片段,有下划线的要加下划线
     */
    private static void updateRuns(XWPFParagraph rowText, int index, String str, boolean isUnderLine) {
        XWPFRun newrun = rowText.getRuns().get(index);// 当前节点
        // newrun.setFontFamily("Calibri");//设置字体
        // newrun.setFontSize(18);
        newrun.setText(str, 0);
        newrun.setBold(false);
    }

}
