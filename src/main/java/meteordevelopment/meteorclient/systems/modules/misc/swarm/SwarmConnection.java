/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.systems.modules.misc.swarm;

import meteordevelopment.meteorclient.utils.player.ChatUtils;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class SwarmConnection extends Thread {
    public final Socket socket;
    public String messageToSend;

    public SwarmConnection(Socket socket) {
        this.socket = socket;
        start();
    }

    @Override
    public void run() {
        ChatUtils.info("Swarm", "在%s上连接了新的工作者.", getIp(socket.getInetAddress().getHostAddress()));

        try {
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());

            while (!isInterrupted()) {
                if (messageToSend != null) {
                    try {
                        out.writeUTF(messageToSend);
                        out.flush();
                    } catch (Exception e) {
                        ChatUtils.error("Swarm", "发送命令时遇到了错误.");
                        e.printStackTrace();
                    }

                    messageToSend = null;
                }
            }

            out.close();
        } catch (IOException e) {
            ChatUtils.info("Swarm", "在端口%s上创建与%s的连接时出错.", getIp(socket.getInetAddress().getHostAddress()), socket.getPort());
            e.printStackTrace();
        }
    }

    public void disconnect() {
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        ChatUtils.info("Swarm", "工作者在ip: %s 上断开连接", socket.getInetAddress().getHostAddress());

        interrupt();
    }

    public String getConnection() {
        return getIp(socket.getInetAddress().getHostAddress()) + ":" + socket.getPort();
    }

    private String getIp(String ip) {
        return ip.equals("127.0.0.1") ? "localhost" : ip;
    }
}
