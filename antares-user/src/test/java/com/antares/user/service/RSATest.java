package com.antares.user.service;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;

public class RSATest {
    public static void main(String[] args) {
        try {
            // 创建 KeyPairGenerator 对象，指定使用的算法（如 "RSA"）
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            
            // 初始化密钥对生成器，指定密钥的长度（如 2048 位）
            keyPairGenerator.initialize(512);
            
            // 生成密钥对
            KeyPair keyPair = keyPairGenerator.generateKeyPair();
            
            // 获取公钥和私钥
            PublicKey publicKey = keyPair.getPublic();
            PrivateKey privateKey = keyPair.getPrivate();
            
            // 打印生成的公钥和私钥（Base64 编码形式）
            System.out.println("Public Key: " + java.util.Base64.getEncoder().encodeToString(publicKey.getEncoded()));
            System.out.println("Private Key: " + java.util.Base64.getEncoder().encodeToString(privateKey.getEncoded()));
            
        } catch (NoSuchAlgorithmException e) {
            System.err.println("No such algorithm: " + e.getMessage());
        }
    }
}
