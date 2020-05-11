package net.kuper.tz.mail.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

@Getter
@Setter
@ApiModel("邮箱配置")
public class MailCnfEntity {

    @NotBlank(message = "Key能为空")
    @ApiModelProperty("字典Key")
    private String key;
    // 邮件服务器SMTP地址
    @NotBlank(message = "主机地址不能为空")
    @ApiModelProperty("主机地址")
    private String host;
    // 邮件服务器SMTP端口
    @NotBlank(message = "消息不能为空")
    @ApiModelProperty("SMTP端口")
    private String port;

    @ApiModelProperty("是否SSL方式")
    private boolean ssl;

    @ApiModelProperty("协议类型")
    private String protocol;

    // 发件者用户名，默认为发件人邮箱前缀
    @ApiModelProperty("发件人别名（用户名或姓名或称谓）")
    private String user;

    @ApiModelProperty("发件人密码")
    @NotBlank(message = "密码不能为空")
    private String pass;

    //发收件人
    @ApiModelProperty("发件地址")
    @NotBlank(message = "发件地址不能为空")
    private String address;

}
