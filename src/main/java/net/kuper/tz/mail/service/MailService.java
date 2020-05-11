package net.kuper.tz.mail.service;


import net.kuper.tz.common.entity.MacroEntity;
import net.kuper.tz.mail.entity.MailCnfEntity;
import net.kuper.tz.mail.entity.MailContentEntity;

import java.util.List;
import java.util.Map;

public interface MailService {

    /**
     * 查询所有发件人
     *
     * @return
     */
    List<MacroEntity> queryFroms();

    /**
     * 获取配置
     *
     * @return
     */
    MailCnfEntity getConfig(String key);

    /**
     * 修改配置
     *
     * @param mailCnfEntity
     * @return
     */
    void updateConfig(MailCnfEntity mailCnfEntity);

    /**
     * 发送邮件
     *
     * @param mailContentEntity
     */
    void sendEmail(MailContentEntity mailContentEntity);


    /**
     * freemarker 模板方式发送
     *
     * @param fromKey      发件配置key
     * @param tos          收件人
     * @param ccs          抄送人
     * @param params       参数
     * @param templateFile 模板
     */
    void sendEmail(String fromKey, String[] tos, String[] ccs, Map<String, Object> params, String templateFile);
}
