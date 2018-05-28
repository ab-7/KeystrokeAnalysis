package com.example.adrija.keystrokeanalysis;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by Adrija on 27-05-2018.
 */

public class FTPUtil {

    static  boolean success=true;


        public static boolean uploadDirectory(FTPClient ftpClient,
                String remoteDirPath, String localParentDir, String remoteParentDir)
            throws IOException {

            System.out.println("LISTING directory: " + localParentDir);

            File localDir = new File(localParentDir);
            File[] subFiles = localDir.listFiles();
             if (subFiles != null && subFiles.length > 0)
            {
                for (File item : subFiles) {
                    String remoteFilePath = remoteDirPath + "/" + remoteParentDir
                            + "/" + item.getName();
                    if (remoteParentDir.equals("")) {
                        remoteFilePath = remoteDirPath + "/" + item.getName();
                        System.out.println("File path\n");
                    }


                    if (item.isFile()) {
                        // upload the file
                        String localFilePath = item.getAbsolutePath();
                        System.out.println("About to upload the file: " + localFilePath);
                        boolean uploaded = uploadSingleFile(ftpClient, localFilePath, remoteFilePath);
                        if (uploaded) {
                            System.out.println("UPLOADED a file to: "
                                    + remoteFilePath);
                        } else {
                            System.out.println("COULD NOT upload the file: "
                                    + localFilePath);
                            success=false;
                        }
                    } else {
                        // create directory on the server
                        boolean created = ftpClient.makeDirectory(remoteFilePath);
                        if (created) {
                            System.out.println("CREATED the directory: "
                                    + remoteFilePath);
                        } else {
                            System.out.println("COULD NOT create the directory: "
                                    + remoteFilePath);
                            success=false;
                        }

                        // upload the sub directory
                        String parent = remoteParentDir + "/" + item.getName();
                        if (remoteParentDir.equals("")) {
                            parent = item.getName();
                        }

                        localParentDir = item.getAbsolutePath();
                        success=uploadDirectory(ftpClient, remoteDirPath, localParentDir,
                                parent);
                    }
                }

        }
        return success;

    }

    public static boolean uploadSingleFile(FTPClient ftpClient,
                                           String localFilePath, String remoteFilePath) throws IOException {
        File localFile = new File(localFilePath);

        InputStream inputStream = new FileInputStream(localFile);
        try {
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
            return ftpClient.storeFile(remoteFilePath, inputStream);
        } finally {
            inputStream.close();
        }
    }
}
