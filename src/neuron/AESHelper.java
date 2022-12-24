package neuron;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

/*
* AES加密
*/
public class AESHelper {
    public static String m_key = "1234567890123456"; //密钥

    /*
     * 解密
    * 密码
    * 要解密的字符
    */
    public static String decrypt(String key, String toDecrypt) {
        try {
            if (key == null || key.length() == 0) {
                key = m_key;
            }
            byte[] raw = key.getBytes("utf-8");
            SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS7Padding");
            cipher.init(Cipher.DECRYPT_MODE, skeySpec);
            byte[] encrypted1 = Base64.getDecoder().decode(toDecrypt);
            try {
                byte[] original = cipher.doFinal(encrypted1);
                String originalString = new String(original,"utf-8");
                return originalString;
            } catch (Exception e) {
                System.out.println(e.toString());
                return null;
            }
        } catch (Exception ex) {
            System.out.println(ex.toString());
            return null;
        }
    }

    /*
    * 加密
    * 密码
    * 要解密的字符
    */
    public static String encrypt(String key, String toEncrypt) {
        try {
            if (key == null || key.length() == 0) {
                key = m_key;
            }
            byte[] raw = key.getBytes("utf-8");
            SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS7Padding");//"算法/模式/补码方式"
            cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
            byte[] encrypted = cipher.doFinal(toEncrypt.getBytes("utf-8"));
            
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception ex) {
            System.out.println(ex.toString());
            return null;
        }
    }
}
