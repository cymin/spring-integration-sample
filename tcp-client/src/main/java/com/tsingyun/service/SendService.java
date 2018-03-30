package com.tsingyun.service;

import com.alibaba.fastjson.JSON;
import com.tsingyun.entity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.integration.ip.tcp.connection.AbstractClientConnectionFactory;
import org.springframework.integration.ip.tcp.connection.TcpConnection;
import org.springframework.integration.ip.tcp.connection.TcpConnectionSupport;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.stereotype.Component;

import java.util.Random;
import java.util.concurrent.TimeUnit;


/**
 * Created by chen on 17/9/19.
 */
@Component
public class SendService implements CommandLineRunner {
    Logger logger = LoggerFactory.getLogger(SendService.class);

    @Autowired
    AbstractClientConnectionFactory clientNio;

    /**
     * 发送消息
     */
    public void doSend() {
        clientNio.start();
        new Thread(() -> {
            int times = 3;
            Random random = new Random();
            TcpConnection connection = null;
            try {
                connection = clientNio.getConnection();
            } catch (Exception e) {
                connection = retryConnect(times, e);
            }
            while (true) {
                try {
                    if (connection.isOpen()) {
                        User user = new User(random.nextInt(1000), "cc");
                        String s = JSON.toJSONString(user);
                        connection.send(MessageBuilder.withPayload(s).build());
                      /*  for (int i = 0; i < 100; i++) {
                            logger.debug("{}-send data: {}", (i + 1), s);
                        }
                        logger.debug("---------------------------\n");
                        */
                        logger.debug("send data: {}", s);
                        TimeUnit.SECONDS.sleep(1);

                    } else {
                        connection = retryConnect(times, null);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    connection = retryConnect(times, e);
                }
            }
        }).start();
    }

    /**
     * 重试连接服务器
     * @param times
     * @param ex
     * @return
     */
    private TcpConnection retryConnect(int times, Exception ex) {
        if (times > 0) {
            try {
                TcpConnectionSupport connection = clientNio.getConnection();
                logger.info("重新连接服务器成功,connectId={}", connection.getConnectionId());
                return connection;
            } catch (Exception e) {
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
                retryConnect(times -1, e);
            }
        } else {
            logger.error("尝试重新连接服务器失败");
            if (ex != null) {
                ex.printStackTrace();
            }
        }
        return null;
    }

    @Override
    public void run(String... args) throws Exception {
        doSend();
    }
}
