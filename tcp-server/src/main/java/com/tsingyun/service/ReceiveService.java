package com.tsingyun.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.integration.channel.QueueChannel;
import org.springframework.integration.ip.tcp.connection.AbstractServerConnectionFactory;
import org.springframework.integration.ip.tcp.connection.TcpConnection;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.ServerSocket;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by chen on 17/9/19.
 */
@Component
public class ReceiveService implements CommandLineRunner {
    Logger logger = LoggerFactory.getLogger(ReceiveService.class);

    @Autowired
    AbstractServerConnectionFactory server;

    @Autowired
    QueueChannel serverSideChannel;

    @Override
    public void run(String... args) throws Exception {
        server.start();
        try {
            ExecutorService executorService = Executors.newSingleThreadExecutor();
            executorService.execute(() -> {
                Thread.currentThread().setName("receiveTask-");

                while (this.server.isListening()) {
                    Message<?> message = this.serverSideChannel.receive(10000);
                    if (message != null) {
                        logger.debug("---> Message received: " + new String((byte[]) message.getPayload()));
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
