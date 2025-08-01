package com.ach.component;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.Channel;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

public class SftpUploader {

    public static void upload(String localFilePath, String remoteDir, String sftpHost, int sftpPort,
                              String username, String password) {

        Session session = null;
        Channel channel = null;
        ChannelSftp channelSftp = null;

        try {
            JSch jsch = new JSch();
            session = jsch.getSession(username, sftpHost, sftpPort);
            session.setPassword(password);

            Properties config = new Properties();
            config.put("StrictHostKeyChecking", "no");  // skip key check
            session.setConfig(config);

            session.connect();
            channel = session.openChannel("sftp");
            channel.connect();

            channelSftp = (ChannelSftp) channel;
            channelSftp.cd(remoteDir);

            File file = new File(localFilePath);
            channelSftp.put(new FileInputStream(file), file.getName());

            System.out.println("✅ File uploaded to SFTP: " + remoteDir + "/" + file.getName());
            
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            if (channelSftp != null) channelSftp.exit();
            if (channel != null) channel.disconnect();
            if (session != null) session.disconnect();
        }
    }
}
