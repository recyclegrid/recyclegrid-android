package com.recyclegrid.core;

import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.CloudBlobClient;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import com.microsoft.azure.storage.blob.CloudBlockBlob;

import java.io.File;
import java.io.FileInputStream;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;

public class AzureStorageFileUploader {
    private CloudStorageAccount _storageAccount;
    private CloudBlobClient _blobClient;

    public AzureStorageFileUploader(String connectionString) {
        try {
            _storageAccount = CloudStorageAccount.parse(connectionString);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        }

        _blobClient = _storageAccount.createCloudBlobClient();
    }

    public void uploadFile(String filePath, String containerName, String blobName) {
        try {
            CloudBlobContainer container = _blobClient.getContainerReference(containerName);
            CloudBlockBlob cloudBlob = container.getBlockBlobReference(blobName);
            container.createIfNotExists();
            File file = new File(filePath);
            cloudBlob.upload(new FileInputStream(file), file.length());
        } catch (URISyntaxException e) {
            e.printStackTrace();
        } catch (StorageException e) {
            e.printStackTrace();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
