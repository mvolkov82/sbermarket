package com.okeandra.demo.services.transport.impl;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;

import com.okeandra.demo.exceptions.FtpTransportException;
import com.okeandra.demo.services.transport.FtpTransporter;
import org.apache.commons.net.PrintCommandListener;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class FtpTransporterImpl implements FtpTransporter {

    private FTPClient ftp = null;

    @Value("${ftp.server}")
    private String server;

    @Value("${ftp.port}")
    private int port;

    @Value("${ftp.user}")
    private String user;

    @Value("${ftp.password}")
    private String password;

    @Value("${ftp.xls.source.directory}")
    private String sourceFtpDirectory;

//    @Value("${ftp.xls.source.file}")
//    private String destinationFileName;


    @Override
    public boolean downloadFileFromFtp(String ftpFolder, String fileName) {
        boolean isDownloaded = false;
        try {
            ftp = connect(ftpFolder);
            isDownloaded = downloadFile(ftp, fileName);
            return isDownloaded;
        } catch (IOException e) {
            String message = String.format("Ошибка при получении файла с FTP: %s", e.getMessage());
            System.out.println(message);
            throw new FtpTransportException(e.getMessage(), message);
        }
         finally {
            if (ftp != null) {
                close(ftp);
            }
        }
    }

    @Override
    public boolean uploadFileToFtp(String ftpDestinationDirectory, String sourceFilePath, String newFilename) throws FtpTransportException {
        try {
            ftp = connect(ftpDestinationDirectory);
            ftp.storeFile(newFilename, new FileInputStream(sourceFilePath));
        } catch (IOException e) {
            throw new FtpTransportException(e.getMessage(), "Exception on uploading file from FTP-server");
        } finally {
            if (ftp != null) {
                close(ftp);
            }
        }
        return true;
    }

    private FTPClient connect(String subFolder) throws IOException {
        if (ftp == null || !ftp.isConnected()) {
                ftp = new FTPClient();
                ftp.addProtocolCommandListener(new PrintCommandListener(new PrintWriter(System.out)));
                ftp.connect(server, port);
                int reply = ftp.getReplyCode();

                if (!FTPReply.isPositiveCompletion(reply)) {
                    ftp.disconnect();
                    throw new IOException("Exception in connecting to FTP Server");
                }
                ftp.login(user, password);
                ftp.enterLocalPassiveMode();
                ftp.setFileType(FTP.BINARY_FILE_TYPE);
                ftp.changeWorkingDirectory(subFolder);

                return ftp;

        } else {
            return ftp;
        }
    }

    private boolean downloadFile(FTPClient ftp, String sourceFileName) {
        boolean isSuccess = false;
        try (FileOutputStream out = new FileOutputStream(sourceFileName);) {
            isSuccess = ftp.retrieveFile(sourceFileName, out);
        } catch (IOException e) {
            throw new FtpTransportException(e.getMessage(), "Exception on downloading file from FTP-server");
        }

        return isSuccess;
    }

//    private void uploadFile(FTPClient ftp, String filePath) throws FtpGettingFileException {
//        try {
//            ftp.storeFile("result_finished.xml", new FileInputStream(filePath));
//        } catch (IOException e) {
//            throw new FtpGettingFileException(e.getMessage(), "Exception on uploading file from FTP-server");
//        }
//    }

    private void close(FTPClient ftp) {
        try {
            ftp.disconnect();
        } catch (IOException e) {
            System.out.println("Exception at closing FTP connection");
        }
    }
}
