package com.okeandra.demo.services.transport;

import com.okeandra.demo.exceptions.FtpTransportException;
import org.springframework.stereotype.Component;

@Component
public interface FtpTransporter extends Transporter{
    boolean copyFileToFtp(String ftpDestinationDirectory, String sourceFilePath, String newFilename) throws FtpTransportException;
    boolean copyFileFromFtp(String filename) throws FtpTransportException;
}
