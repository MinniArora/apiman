/*
 * Copyright 2016 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.apiman.common.util;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;

/**
 * A simple AES encrypter.
 *
 * @author eric.wittmann@redhat.com
 * @author Rachel Yordán <ryordan@redhat.com>
 */
public class AesEncrypter {

    private static final String secretKey = "f2f0aa80-84bd8a6"; //$NON-NLS-1$
    private static final Map<String, SecretKeySpec> keySpecs = new HashMap<>();

    /**
     * Constructor.
     */
    private AesEncrypter() {
    }

    /**
     * Encrypt.
     * @param plainText the plain text
     * @return the string
     */
    public static String encrypt(String plainText) {
        return encrypt(secretKey, plainText);
    }
    
    
    /**
     * Encrypt.
     * @param plainText the plain text
     * @param secretKey the secret key
     * @return the string
     */
    public static String encrypt(String secretKey, String plainText) {
        if (plainText == null) {
            return null;
        } else if (plainText.startsWith("$CRYPT::")) {
             // Avoid encrypting already encrypted text - APIMAN-1201
             return plainText;
        }
        
        byte[] encrypted;
        Cipher cipher;
        try {
            SecretKeySpec skeySpec = keySpecFromSecretKey(secretKey);
            
            cipher = Cipher.getInstance("AES"); //$NON-NLS-1$
            cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException e) {
            throw new RuntimeException(e);
        }
        try {
            encrypted = cipher.doFinal(plainText.getBytes());
        } catch (IllegalBlockSizeException | BadPaddingException e) {
            throw new RuntimeException(e);
        }
        return "$CRYPT::" + new String(Base64.encodeBase64(encrypted)); //$NON-NLS-1$
    }

    /**
     * Returns a {@link SecretKeySpec} given a secret key.
     * @param secretKey
     */
    private static SecretKeySpec keySpecFromSecretKey(String secretKey) {
        if (!keySpecs.containsKey(secretKey)) {
            byte[] ivraw = secretKey.getBytes();
            SecretKeySpec skeySpec = new SecretKeySpec(ivraw, "AES"); //$NON-NLS-1$
            keySpecs.put(secretKey, skeySpec);
        }
        return keySpecs.get(secretKey);
    }
    

    /**
     * Decrypt.
     * @param encryptedText the encrypted text
     * @return the string
     */
    public static final String decrypt(String encryptedText) {
        return decrypt(secretKey, encryptedText);
    }
    
    
    /**
     * Decrypt.
     * @param encryptedText the encrypted text
     * @param secretKey the secret key
     * @return the string
     */
    public static final String decrypt(String secretKey, String encryptedText) {
        if (encryptedText == null) {
            return null;
        }
        if (encryptedText.startsWith("$CRYPT::")) { //$NON-NLS-1$
            byte[] decoded = Base64.decodeBase64(encryptedText.substring(8));
            Cipher cipher;
            try {
                SecretKeySpec skeySpec = keySpecFromSecretKey(secretKey);
                
                cipher = Cipher.getInstance("AES"); //$NON-NLS-1$
                cipher.init(Cipher.DECRYPT_MODE, skeySpec);
            } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException e) {
                throw new RuntimeException(e);
            }
            try {
                return new String(cipher.doFinal(decoded));
            } catch (IllegalBlockSizeException | BadPaddingException e) {
                throw new RuntimeException(e);
            }
        } else {
            return encryptedText;
        }
    }
    

    /**
     * Main entry point for the encrypter.  Allows encryption and decryption of text
     * from the command line.
     * @param args
     */
    public static final void main(String [] args) {
        if (args.length != 2) {
            printUsage();
            return;
        }
        String cmd = args[0];
        String input = args[1];
        if ("encrypt".equals(cmd)) { //$NON-NLS-1$
            System.out.println(AesEncrypter.encrypt(input).substring("$CRYPT::".length())); //$NON-NLS-1$
        } else if ("decrypt".equals(cmd)) { //$NON-NLS-1$
            System.out.println(AesEncrypter.decrypt("$CRYPT::" + input)); //$NON-NLS-1$
        } else {
            printUsage();
        }

    }

    /**
     * Usage.
     */
    @SuppressWarnings("nls")
    private static void printUsage() {
        System.out.println("Usage:");
        System.out.println("\tAesEncrypter encrypt|decrypt \"input\"\n------");
        System.out.println("Argument 1: the command, either 'encrypt' or 'decrypt'");
        System.out.println("Argument 2: the text to encrypt or decrypt (use quotes if the input contains spaces)");
        System.out.println("");
    }
}
