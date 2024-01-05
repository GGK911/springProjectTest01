package com.ggk911.springtest01.util.pdf;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

/**
 * 模板参数表
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PdfParameterEntity {
    /**
     * Column -- 模板参数主键ID --length:32
     */
    private String id;

    /**
     * Column -- 模板表主键ID --length:32
     */
    private String templateId;

    /**
     * Column -- 页数 --length:3
     */
    private String pageNo;

    /**
     * Column -- 位置X1 --length:32
     */
    private String positionXl;

    /**
     * Column -- 位置Y1 --length:32
     */
    private String positionYl;

    /**
     * Column -- 位置X2 --length:32
     */
    private String positionXr;

    /**
     * Column -- 位置Y2 --length:32
     */
    private String positionYr;

    /**
     * Column -- 模板域key值 --length:64
     */
    private String keyword;

    /**
     * Column -- 类型：1:表单文本域，2：签名域 --length:2
     */
    private String type;

    /**
     * Column -- 文本域字体 --length:32
     */
    private String textFont;

    /**
     * Column -- 文本域字体大小 --length:32
     */
    private String textSize;

    /**
     * Column -- 文本域长度 --length:32
     */
    private String textLength;

    /**
     * Column -- 商户ID --length:32
     */
    private String deptId;

    /**
     * Column -- 商户名称 --length:128
     */
    private String deptName;

    /**
     * Column -- 应用ID --length:32
     */
    private String appId;

    /**
     * Column -- 应用名称 --length:128
     */
    private String appName;

    /**
     * Column -- 创建人ID --length:32
     */
    private String createUserId;

    /**
     * Column -- 创建时间 --length:19
     */
    private Date createTime;

    /**
     * Column -- 创建人名称 --length:64
     */
    private String createName;

    /**
     * Column -- 修改人ID --length:32
     */
    private String updateUserId;

    /**
     * Column -- 修改人名称 --length:64
     */
    private String updateName;

    /**
     * Column -- 修改时间 --length:19
     */
    private Date updateTime;

    /**
     * Column -- 状态，0无效，1有效 --length:2
     */
    private String status;

    /**
     * Column -- 备注(业务编号) --length:256
     */
    private String remark;
}
