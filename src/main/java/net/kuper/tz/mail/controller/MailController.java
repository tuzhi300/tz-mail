package net.kuper.tz.mail.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import net.kuper.tz.common.entity.MacroEntity;
import net.kuper.tz.core.controller.Res;
import net.kuper.tz.core.validator.ValidatorUtils;
import net.kuper.tz.mail.entity.MailCnfEntity;
import net.kuper.tz.mail.entity.MailContentEntity;
import net.kuper.tz.mail.service.MailService;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(value = "邮箱", description = "邮箱")
@RestController
@RequestMapping("/mail")
public class MailController {

    @Autowired
    private MailService mailService;


    /**
     * 获取所有发件人配置
     *
     * @return
     */
    @ApiOperation("获取所有发件人配置")
    @RequiresPermissions("mail:cnf:list")
    @ResponseBody
    @GetMapping("/cnf")
    public Res<List<MacroEntity>> queryFroms() {
        return Res.ok(mailService.queryFroms());
    }

    /**
     * 获取配置
     *
     * @return
     */
    @ApiOperation("邮箱配置详情查询")
    @RequiresPermissions("mail:cnf:detail")
    @ResponseBody
    @GetMapping("/cnf/{key}")
    public Res<MailCnfEntity> getConfig(@PathVariable("key") String key) {
        return Res.ok(mailService.getConfig(key));
    }

    /**
     * 修改配置
     *
     * @param mailCnfEntity
     * @return
     */
    @ApiOperation("邮箱配置修改")
    @RequiresPermissions("mail:cnf:update")
    @ResponseBody
    @PutMapping("/cnf")
    public Res updateConfig(@RequestBody MailCnfEntity mailCnfEntity) {
        ValidatorUtils.validateEntity(mailCnfEntity);
        mailService.updateConfig(mailCnfEntity);
        return Res.ok();
    }

    /**
     * 发送邮件
     *
     * @param mailContentEntity
     */
    @ApiOperation("发送邮件")
    @RequiresPermissions("mail:email:send")
    @ResponseBody
    @PostMapping("/send")
    public Res sendEmail(@RequestBody MailContentEntity mailContentEntity) {
        ValidatorUtils.validateEntity(mailContentEntity);
        mailService.sendEmail(mailContentEntity);
        return Res.ok();
    }
}
