package com.antares.member.listener;

import com.rabbitmq.client.Channel;
import com.antares.common.exception.BusinessException;
import com.antares.common.model.enums.AppHttpCodeEnum;
import com.antares.member.utils.MailUtil;
import com.antares.member.utils.SmsUtil;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;

@RabbitListener(queues = {"antares.member.code"})
@Service
public class CodeListener {
    @Resource
    private SmsUtil smsUtil;
    @Resource
    private MailUtil mailUtil;

    @RabbitHandler
    public void handleCodeMessage(Message message, String[] sendCodeRequest, Channel channel) {
        String type = sendCodeRequest[0];
        String dest = sendCodeRequest[1];
        String codeNum = sendCodeRequest[2];

        if(type.equals("phone")){
            smsUtil.sendCode(dest, codeNum);
        } else if(type.equals("mail")){
            mailUtil.sendMail(dest, codeNum);
        }

        //手动ack（false代表不批量接收）
        try {
            long deliveryTag = message.getMessageProperties().getDeliveryTag();
            channel.basicAck(deliveryTag, false);
        } catch (IOException e) {
            throw new BusinessException(AppHttpCodeEnum.INTERNAL_SERVER_ERROR, "RabbitMQ连接接异常！");
        }
    }
}
