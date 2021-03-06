package com.vdong.rms.entity.visual;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @description: 员工用户-返回AUTHTOKEN
 * @author: Mr.WangGang
 * @create: 2018-10-19 上午 11:38
 **/
@ApiModel(value="AUTH_TOKEN信息",description="返回AUTH_TOKEN信息")
@Data
public class EmployeeAuthTokenVO implements Serializable {

    @ApiModelProperty(value="AUTH_TOKEN",name="authToken",example="E17D0212DE0DB4403549C7F575ECE9E6")
    private String authToken;
}
