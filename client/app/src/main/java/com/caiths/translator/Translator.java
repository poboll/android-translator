package com.caiths.translator;

import android.app.Application;
import android.util.Log;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;
import java.nio.charset.StandardCharsets;

public class Translator extends Application {
    private static final String TAG = "TranslatorApp";

    // 必选修改配置项
    public static final String TEXT_TRANS_API_URL = "https://translate.caiths.com/api/translate/common/"; // 通用翻译API地址
    public static final String TEXT_TRANS_AUTH_KEY = "KRYbv1W9HCT1QXRxFRaT"; // 通用翻译key
    public static final String BAIDU_TRANS_APP_ID = "20230713001742514"; // 百度翻译平台APP ID
    public static final String BAIDU_TRANS_SECRET_KEY = "KRYbv1W9HCT1QXRxFRaT"; // 百度翻译平台密钥 (用于新语音API签名)
    public static final String BAIDU_VOICE_TRANS_API_URL = "https://fanyi-api.baidu.com/api/trans/v2/voicetrans"; // 百度语音翻译API地址
    public static final String IMAGE_TRANS_API_URL = "https://translate.caiths.com/api/translate/image/"; // 图片翻译API地址
    public static final String IMAGE_TRANS_API_AUTH = "KRYbv1W9HCT1QXRxFRaT"; // 图片翻译Key
    public static final String LEGAL_WORD_API_URL = "https://translate.caiths.com/api/legal-word/en/"; // 生词API地址
    public static final String LEGAL_WORD_AUTH_KEY = "KRYbv1W9HCT1QXRxFRaT"; // 生词Key
    public static final String VERSION_CHECK_API_URL = "https://translate.caiths.com/api/update/"; // 版本检查API地址
    public static final String VERSION_CHECK_AUTH_KEY = "KRYbv1W9HCT1QXRxFRaT"; // 版本检查Key

    // 可选修改配置项
    public static final String CONFIG_NAME = "Config";
    public static final String VOICE_AUTO_TTS_CONFIG_NAME = "IsVoiceAutoTts";
    public static final String ENG_AUTO_TTS_TYPE_CONFIG_NAME = "EngAutoTtsType";
    public static final String AUTO_ADD_NEW_WORD_CONFIG_NAME = "IsAutoAddNewWord";

    @Override
    public void onCreate() {
        super.onCreate();

        try {
            // 预加载百度语音SDK相关库，确保它们被正确加载 (旧版SDK，新API不需要这些)
            // System.loadLibrary("bdEASRAndroid");
            // System.loadLibrary("BaiduSpeechSDK");
            // Log.i(TAG, "旧版百度语音相关SDK库尝试加载（如bdEASRAndroid, BaiduSpeechSDK），新API不再需要。");

            // 尝试加载之前缺失的库 (旧版SDK)
            // try {
            // System.loadLibrary("bdSpilWakeup");
            // Log.i(TAG, "成功加载bdSpilWakeup库 (旧版SDK)");
            // } catch (UnsatisfiedLinkError e) {
            // Log.e(TAG, "bdSpilWakeup库加载失败 (旧版SDK): " + e.getMessage());
            // }

            Log.i(TAG, "翻译君应用初始化完成。");
        } catch (UnsatisfiedLinkError e) {
            // This might catch if other non-Baidu-voice libs fail, so keep general catch
            Log.e(TAG, "加载本地库失败: " + e.getMessage());
        }

        Log.i(TAG, "API配置: APP_ID=" + BAIDU_TRANS_APP_ID);
    }

    /**
     * 计算HMAC-SHA256签名并进行Base64编码
     * 
     * @param secret  密钥
     * @param message 要签名的消息
     * @return Base64编码的签名, 或在出错时返回null
     */
    public static String signHmacSha256(String secret, String message) {
        try {
            Mac sha256Hmac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            sha256Hmac.init(secretKey);
            byte[] hash = sha256Hmac.doFinal(message.getBytes(StandardCharsets.UTF_8));
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                return Base64.getEncoder().encodeToString(hash);
            } else {
                return android.util.Base64.encodeToString(hash, android.util.Base64.NO_WRAP);
            }
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            Log.e(TAG, "HMAC-SHA256签名失败: " + e.getMessage(), e);
            return null;
        }
    }
}
