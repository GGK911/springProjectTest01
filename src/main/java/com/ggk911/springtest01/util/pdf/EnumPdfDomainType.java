package com.ggk911.springtest01.util.pdf;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 模板域类型枚举
 *
 * @author fu
 */
@Getter
@AllArgsConstructor
public enum EnumPdfDomainType {
    /**
     * 枚举
     * 获取PDF模板参数
     * ("0", "FIELD_TYPE_NONE"); 无
     * ("1", "FIELD_TYPE_PUSHBUTTON");按钮
     * ("2", "FIELD_TYPE_CHECKBOX");复选框
     * ("3", "FIELD_TYPE_RADIOBUTTON");单选按钮
     * ("4", "FIELD_TYPE_TEXT");text
     * ("5", "FIELD_TYPE_LIST");list
     * ("6", "FIELD_TYPE_COMBO");下拉列表
     * ("7", "FIELD_TYPE_SIGNATURE");签名
     */
    NONE_DOMAIN("0", "无", "0"),
    TEXT_DOMAIN("1", "文本域", "4"),
    SIGNATURE_DOMAIN("2", "签名域", "7"),
    CHECKBOX_DOMAIN("3", "复选框", "2"),
    RADIOBUTTON_DOMAIN("4", "单选按钮", "3"),
    BUTTON_DOMAIN("5", "按钮", "1"),
    LIST_DOMAIN("6", "表单", "5"),
    COMBOBOX("7", "下拉列表", "6"),
    ;
    private String code;
    private String msg;
    private String pdfCode;

    public static EnumPdfDomainType getEnumByCode(String code) {
        if (code == null) {
            return null;
        }
        for (EnumPdfDomainType type : values()) {
            if (type.getCode().equals(code.trim())) {
                return type;
            }
        }
        return null;
    }

    public static EnumPdfDomainType getEnumByPdfCode(String code) {
        if (code == null) {
            return null;
        }
        for (EnumPdfDomainType type : values()) {
            if (type.getPdfCode().equals(code)) {
                return type;
            }
        }
        return null;
    }
}
