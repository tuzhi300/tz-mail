package net.kuper.tz.mail.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;

@Getter
@Setter
@ApiModel("邮件")
public class MailContentEntity {

    @NotEmpty(message = "发件人不能为空")
    @ApiModelProperty("发件配置Key")
    private String fromKey;
    // 收件人，支持多个收件人，用逗号分隔
    @NotEmpty(message = "收件人不能为空")
    @ApiModelProperty("收件人")
    private String[] tos;
    // 抄送人
    @ApiModelProperty("抄送人")
    private String[] ccs;

    @NotBlank(message = "主题不能为空")
    @ApiModelProperty("主题")
    private String subject;

    @ApiModelProperty("是否网页内容")
    private boolean html;

    @ApiModelProperty("内容")
    @NotBlank(message = "邮件内容不能为空")
    private String content;

}
