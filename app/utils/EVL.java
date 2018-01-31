package utils;

import configuration.inject.ConfigurationValue;

import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidParameterSpecException;

import java.util.HashMap;
import java.util.Map;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import javax.inject.Inject;

public class EVL {
    private String taxURL;
    private String sornURL;
    private Encryption encryption;

    @Inject
    public EVL(@ConfigurationValue(key = "evl.taxURL") String taxURL,
               @ConfigurationValue(key = "evl.sornURL") String sornURL,
               @ConfigurationValue(key = "evl.preSharedKey") String preSharedKey) {
        this.taxURL = taxURL;
        this.sornURL = sornURL;
        this.encryption = new Encryption(preSharedKey);
    }

    public String getTaxURL() { return taxURL; }

    public String getSORNURL() { return sornURL; }

    public Map<String, String> encryptFields(Map<String, String> fields) throws NoSuchPaddingException, NoSuchAlgorithmException,
                InvalidKeyException, BadPaddingException, IllegalBlockSizeException, InvalidAlgorithmParameterException,
                InvalidParameterSpecException, UnsupportedEncodingException {
        Map<String, String> encrypted = new HashMap<>();
        for (Map.Entry<String, String> e : fields.entrySet()) {
            encrypted.put(encryption.encrypt(e.getKey()), encryption.encrypt(e.getValue()));
        }
        return encrypted;
    }
}
