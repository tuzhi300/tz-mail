package net.kuper.tz.mail.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import freemarker.core.ParseException;
import freemarker.template.MalformedTemplateNameException;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateNotFoundException;
import lombok.extern.slf4j.Slf4j;
import net.kuper.tz.common.dao.MacroDao;
import net.kuper.tz.common.entity.MacroEntity;
import net.kuper.tz.common.entity.MacroQueryEntity;
import net.kuper.tz.common.entity.MacroUpdateEntity;
import net.kuper.tz.core.controller.exception.ApiException;
import net.kuper.tz.core.utils.StringUtils;
import net.kuper.tz.mail.entity.MailCnfEntity;
import net.kuper.tz.mail.entity.MailContentEntity;
import net.kuper.tz.mail.service.MailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;

import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;
import java.util.Properties;

@Slf4j
@Component("mailServiceImpl")
public class MailServiceImpl implements MailService {

//    private final String dicKey = "MAIL_CONFIG";

    @Autowired
    private MacroDao dicDao;
    @Autowired
    private ObjectMapper objectMapper;
    //发送邮件的模板引擎
    @Autowired
    private FreeMarkerConfigurer configurer;


    @Override
    public List<MacroEntity> queryFroms() {
        MacroQueryEntity queryEntity = new MacroQueryEntity();
        queryEntity.setStatus(1);
        queryEntity.setParentKey("mail");
        return dicDao.queryList(queryEntity);
    }

    @Override
    public MailCnfEntity getConfig(String key) {
        MailCnfEntity mailCnfEntity = null;
        try {
            MacroEntity dicEntity = dicDao.queryObjectByKey(key);
            if (dicEntity != null && !StringUtils.isEmpty(dicEntity.getValue())) {
                String src = dicEntity.getValue();
                mailCnfEntity = objectMapper.readValue(src, MailCnfEntity.class);
            }
        } catch (IOException e) {
            throw new ApiException(e, "配置数据格式有误");
        }
        return mailCnfEntity;
    }

    @Override
    public void updateConfig(MailCnfEntity mailCnfEntity) {
        try {
            MacroEntity dicEntity = dicDao.queryObjectByKey(mailCnfEntity.getKey());
            if (dicEntity == null) {
                throw new ApiException("该邮箱配置不存在");
            }
            dicEntity.setValue(objectMapper.writeValueAsString(new MailCnfEntity()));
            MacroUpdateEntity dicUpdate = new MacroUpdateEntity();
            dicUpdate.setId(dicEntity.getId());
            dicUpdate.setDisplayName(dicEntity.getDisplayName());
            dicUpdate.setKey(dicEntity.getKey());
            dicUpdate.setValue(objectMapper.writeValueAsString(mailCnfEntity));
            dicUpdate.setType(dicEntity.getType());
            dicUpdate.setStatus(dicEntity.getStatus());
            dicUpdate.setEdit(dicEntity.getEdit());
            dicDao.update(dicUpdate);
        } catch (JsonProcessingException e) {
            throw new ApiException(e, "修改配置失败");
        }


    }


    private JavaMailSenderImpl createMailSender(MailCnfEntity mailCnfEntity) {
        JavaMailSenderImpl sender = new JavaMailSenderImpl();
        sender.setHost(mailCnfEntity.getHost());
        sender.setPort(Integer.valueOf(mailCnfEntity.getPort()));
        sender.setPassword(mailCnfEntity.getPass());
        sender.setUsername(mailCnfEntity.getAddress());
        sender.setDefaultEncoding("UTF-8");
        Properties p = new Properties();
        p.setProperty("mail.smtp.timeout", "20000");
        p.setProperty("mail.smtp.auth", "true");
        if (mailCnfEntity.isSsl()) {
            p.setProperty("mail.smtp.ssl.enable", "true");
            p.setProperty("mail.smtp.ssl.socketFactory.fallback", "false");
            p.setProperty("mail.smtp.ssl.socketFactory.class", "net.kuper.tz.mail.cnf.MailSSLSocketFactory");
        }
        sender.setJavaMailProperties(p);
        return sender;
    }


    @Override
    public void sendEmail(MailContentEntity mailContentEntity) {
        try {
            MailCnfEntity mailCnfEntity = getConfig(mailContentEntity.getFromKey());
            if (mailCnfEntity == null) {
                throw new ApiException("请先配置邮件，才可以发送");
            }
            //设置邮件服务主机
            JavaMailSenderImpl javaMailSender = createMailSender(mailCnfEntity);
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper messageHelper = new MimeMessageHelper(message);
            if (!StringUtils.isEmpty(mailCnfEntity.getUser())) {
                try {
                    String alias = javax.mail.internet.MimeUtility.encodeText(mailCnfEntity.getUser());
                    messageHelper.setFrom(new InternetAddress(alias + "<" + mailCnfEntity.getAddress() + ">"));
                } catch (UnsupportedEncodingException e) {
                    log.error(e.getMessage(), e);
                }
            }
            messageHelper.setTo(mailContentEntity.getTos());
            if (mailContentEntity.getCcs() != null) {
                messageHelper.setCc(mailContentEntity.getCcs());
            }
            messageHelper.setSubject(mailContentEntity.getSubject());
            messageHelper.setText(mailContentEntity.getContent(), mailContentEntity.isHtml());

            javaMailSender.send(message);
        } catch (MessagingException e) {
            throw new ApiException(e, "发送邮件失败");
        }
    }

    @Override
    public void sendEmail(String fromKey, String[] tos, String[] ccs, Map<String, Object> params, String templateFile) {
        try {
            Template template = configurer.getConfiguration().getTemplate(templateFile);
            String text = FreeMarkerTemplateUtils.processTemplateIntoString(template, params);
            MailContentEntity mailContentEntity = new MailContentEntity();
            mailContentEntity.setFromKey(fromKey);
            mailContentEntity.setTos(tos);
            mailContentEntity.setCcs(ccs);
            mailContentEntity.setContent(text);
            this.sendEmail(mailContentEntity);
        } catch (TemplateException e) {
            e.printStackTrace();
            throw new ApiException(e, "发送邮件失败,模板异常");
        } catch (ParseException e) {
            throw new ApiException(e, "发送邮件失败,模板解析异常");
        } catch (MalformedTemplateNameException e) {
            throw new ApiException(e, "发送邮件失败,模板异常");
        } catch (TemplateNotFoundException e) {
            throw new ApiException(e, "发送邮件失败,未找到模板");
        } catch (IOException e) {
            throw new ApiException(e, "发送邮件失败");
        }
    }
}
