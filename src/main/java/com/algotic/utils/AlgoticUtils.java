package com.algotic.utils;

import static java.lang.System.*;

import com.algotic.constants.AdminEndPoints;
import com.algotic.exception.AlgoticException;
import com.algotic.exception.CommonErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.UUID;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

@Slf4j
public class AlgoticUtils {

    private static final String DEFAULT_IP = "127.0.0.1";

    private static final String DEFAULT_MAC = "00:11:22:AA:BB:CC";

    public static String generateUUID() {
        UUID uuid = UUID.randomUUID();
        return uuid.toString();
    }

    public static String objectToJsonString(Object data) {
        try {
            ObjectMapper obj = new ObjectMapper();
            return obj.writeValueAsString(data);
        } catch (Exception ex) {
            throw new AlgoticException(CommonErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    // sha-256 implementation
    public static String getSHA(String input) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        return toHexString(md.digest(input.getBytes(StandardCharsets.UTF_8)));
    }

    private static String toHexString(byte[] hash) {
        // convert byte array into signum
        BigInteger number = new BigInteger(1, hash);

        // convert message digest into hex value
        StringBuilder hexString = new StringBuilder(number.toString(16));

        while (hexString.length() < 64) {
            hexString.insert(0, '0');
        }
        return hexString.toString();
    }

    public static String generateHash(String password, String salt)
            throws NoSuchAlgorithmException, InvalidKeySpecException {

        KeySpec spec = new PBEKeySpec(password.toCharArray(), salt.getBytes(), 65536, 128);
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        byte[] hash = factory.generateSecret(spec).getEncoded();
        Base64.Encoder enc = Base64.getEncoder();
        return enc.encodeToString(hash);
    }

    public static String generateSalt() {
        byte[] salt = new byte[16];
        SecureRandom random = new SecureRandom();
        random.nextBytes(salt);
        return random.toString();
    }

    public static String UTCtoIST(Instant date) {

        // Convert UTC time to IST
        ZonedDateTime utcDateTime = date.atZone(ZoneOffset.UTC);
        ZonedDateTime istDateTime = utcDateTime.withZoneSameInstant(ZoneId.of("Asia/Kolkata"));

        // Format the IST time
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        return istDateTime.format(formatter);
    }

    public static String encrypt(String strToEncript, String secretKey, String saltKey) {
        try {
            byte[] iv = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
            IvParameterSpec ivspec = new IvParameterSpec(iv);
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            KeySpec spec = new PBEKeySpec(secretKey.toCharArray(), saltKey.getBytes(), 65536, 256);
            SecretKey tmp = factory.generateSecret(spec);
            SecretKeySpec secretKeySpec = new SecretKeySpec(tmp.getEncoded(), "AES");
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivspec);
            return Base64.getEncoder().encodeToString(cipher.doFinal(strToEncript.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            out.println(e.toString());
        }
        return null;
    }

    public static String decrypt(String strToDecrypt, String secretKey, String saltKey) {
        try {
            byte[] iv = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
            IvParameterSpec ivspec = new IvParameterSpec(iv);
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            KeySpec spec = new PBEKeySpec(secretKey.toCharArray(), saltKey.getBytes(), 65536, 256);
            SecretKey tmp = factory.generateSecret(spec);
            SecretKeySpec secretKeySpec = new SecretKeySpec(tmp.getEncoded(), "AES");
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, ivspec);
            return new String(cipher.doFinal(Base64.getDecoder().decode(strToDecrypt)));
        } catch (Exception e) {
            out.println(e.toString());
        }
        return null;
    }

    public static String convertToPascalCase(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }

        StringBuilder converted = new StringBuilder();

        boolean convertNext = true;
        for (char ch : text.toCharArray()) {
            if (Character.isSpaceChar(ch)) {
                convertNext = true;
            } else if (convertNext) {
                ch = Character.toTitleCase(ch);
                convertNext = false;
            } else {
                ch = Character.toLowerCase(ch);
            }
            converted.append(ch);
        }

        return converted.toString();
    }

    public static boolean isValidAdminUrl(String url) {
        for (AdminEndPoints adminEndpoint : AdminEndPoints.values()) {
            if (adminEndpoint.getEndPoint().equalsIgnoreCase(url)) {
                return true;
            }
        }
        return false;
    }

    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = BigDecimal.valueOf(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    // replace comma
    public static String commaFilter(String data) {
        return data.replaceAll(",", "");
    }

    public static void main(String[] args) {
        encrypt("3V7peAJbzCRUc//o1H7Fwg==", "rm1XurB9lRsOktyr+0FTjA==", "qHE6tOOli1ROnMC7bxS+zg==");
    }

    /**
     * @return macAddress of the system
     */
    public static String getMacAddress() {
        try {
            byte[] mac = NetworkInterface.getByInetAddress(InetAddress.getLocalHost())
                    .getHardwareAddress();
            StringBuilder macAddress = new StringBuilder();
            for (int i = 0; i < mac.length; i++) {
                macAddress.append(String.format("%02X%s", mac[i], (i < mac.length - 1) ? "-" : ""));
            }
            return StringUtils.defaultIfEmpty(macAddress.toString(), DEFAULT_MAC);
        } catch (Exception e) {
            log.error("Error occure while fetching mac-address. Return default mac-address -> {}", DEFAULT_MAC);
            return DEFAULT_MAC;
        }
    }

    /**
     * @return LocalIp
     */
    public static String getLocalIp() {
        try {
            String[] ipAddress = InetAddress.getLocalHost().toString().split("/");
            return StringUtils.defaultIfEmpty(ipAddress[1], DEFAULT_IP);

        } catch (Exception e) {
            log.error("Error occure while fetching ip-address. Return default ip-address -> {}", DEFAULT_IP);
            return DEFAULT_IP;
        }
    }

    /**
     * @return public ip of the system
     */
    public static String getPublicIp() {
        try {
            return StringUtils.defaultIfEmpty(InetAddress.getLocalHost().getHostAddress(), DEFAULT_IP);
        } catch (Exception e) {
            log.error(
                    "Error occure while fetching publicIp-address. Return default publicIp-address -> {}", DEFAULT_IP);
            return DEFAULT_IP;
        }
    }

    public static String getPriceData(BigDecimal priceOpt, BigDecimal triggerOpt, BigDecimal averagePriceOpt) {
        String priceData;

        if (triggerOpt != null && triggerOpt.compareTo(BigDecimal.ZERO) > 0) {

            if (priceOpt != null && priceOpt.compareTo(BigDecimal.ZERO) > 0) {
                priceData = StringUtils.join(priceOpt, "/", triggerOpt, " trg.");
            } else {
                priceData = StringUtils.join(averagePriceOpt, "/", triggerOpt, " trg.");
            }

        } else {

            if (priceOpt != null && priceOpt.compareTo(BigDecimal.ZERO) > 0) {
                priceData = priceOpt.toString();
            } else {
                priceData = StringUtils.join(averagePriceOpt != null ? averagePriceOpt : 0.0);
            }
        }
        return priceData;
    }
}
