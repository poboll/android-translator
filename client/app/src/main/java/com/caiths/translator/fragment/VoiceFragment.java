package com.caiths.translator.fragment;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.baidu.translate.asr.data.Language;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.json.JSONException;
import org.json.JSONObject;

import com.caiths.translator.R;
import com.caiths.translator.Translator;
import com.caiths.translator.database.DatabaseHelper;
import com.caiths.translator.adapter.VoiceLangAdapter;
import com.caiths.translator.util.NetUtil;
import com.caiths.translator.util.NewWordUtil;
import com.caiths.translator.util.HistoryUtil;
import com.caiths.translator.util.SPUtil;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.StringEntity;

public class VoiceFragment extends Fragment {
    // 常量
    private static final String HISTORY_RECORDE_TYPE = "voice";
    private static final String TAG = "VoiceFragment";
    private static final int PERMISSION_REQUEST_CODE = 2021;

    // Audio Recording Constants
    private static final int AUDIO_SOURCE = MediaRecorder.AudioSource.MIC;
    private static final int SAMPLE_RATE_IN_HZ = 16000;
    private static final int CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO;
    private static final int AUDIO_FORMAT_ENCODING = AudioFormat.ENCODING_PCM_16BIT;
    private int bufferSize = 0;

    // View和Context
    private View view;
    private Context context;

    // 数据库
    private SQLiteDatabase sqLiteWritableDB;

    // 控件
    private Spinner fromLangSpinner;
    private Spinner toLangSpinner;
    private TextView exchangeLangTextView;
    private LinearLayout historySetLinearLayout;
    private Button translateButton;

    // 语言
    private VoiceLangAdapter fromLangAdapter;
    private VoiceLangAdapter toLangAdapter;
    private Language fromLang;
    private Language toLang;
    private String newWordEnglish;
    private String newWordChinese;

    // 权限
    private boolean hasPermission;

    // Audio Recording
    private AudioRecord audioRecord;
    private boolean isRecording = false;
    private Thread recordingThread;
    private ByteArrayOutputStream audioDataStream;
    private MediaPlayer recognitionStartPlayer;

    // HTTP Client
    private static final AsyncHttpClient httpClient = new AsyncHttpClient();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_voice, container, false);
        context = view.getContext();

        // 初始化
        initDatabase();
        initView();
        initLang();
        initListener();
        initAudio();

        checkPermission();
        return view;
    }

    // 初始化Database
    private void initDatabase() {
        DatabaseHelper databaseHelper = new DatabaseHelper(context);
        sqLiteWritableDB = databaseHelper.getWritableDatabase();
    }

    // 初始化View
    private void initView() {
        fromLangSpinner = view.findViewById(R.id.voice_from_lang_spinner);
        toLangSpinner = view.findViewById(R.id.voice_to_lang_spinner);
        exchangeLangTextView = view.findViewById(R.id.voice_exchange_lang_text_view);
        historySetLinearLayout = view.findViewById(R.id.voice_history_set_linear_layout);
        translateButton = view.findViewById(R.id.voice_trans_button);

        // 显示历史记录
        HistoryUtil.showHistoricalRecords(context, HISTORY_RECORDE_TYPE, historySetLinearLayout, sqLiteWritableDB);
    }

    // 初始化语言
    private void initLang() {
        // 实例化Adapter
        fromLangAdapter = new VoiceLangAdapter(context, Language.getAsrAvailableLanguages());
        toLangAdapter = new VoiceLangAdapter(context, Language.values());

        // 设置Spinner控件的Adapter
        fromLangSpinner.setAdapter(fromLangAdapter);
        toLangSpinner.setAdapter(toLangAdapter);

        // 设置默认的语言
        fromLang = Language.Chinese;
        toLang = Language.English;

        // 设置默认的Item
        fromLangSpinner.setSelection(0);
        toLangSpinner.setSelection(1);
    }

    // 初始化Listener
    @SuppressLint("ClickableViewAccessibility")
    private void initListener() {
        fromLangSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                fromLang = fromLangAdapter.getItem(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        toLangSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                toLang = toLangAdapter.getItem(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        exchangeLangTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Check if the selected "to" language is one of the ASR-supported "from"
                // languages
                // This logic might need update based on actual API capabilities for language
                // swapping
                boolean toLangIsAsrSupported = false;
                Language selectedToLang = (Language) toLangSpinner.getSelectedItem();
                for (Language asrLang : Language.getAsrAvailableLanguages()) {
                    if (asrLang == selectedToLang) {
                        toLangIsAsrSupported = true;
                        break;
                    }
                }

                if (!toLangIsAsrSupported) {
                    Toast.makeText(context, "目标语言不支持作为源语言进行交换", Toast.LENGTH_SHORT).show();
                } else {
                    int fromLangSelectedItemPosition = fromLangSpinner.getSelectedItemPosition();
                    int toLangSelectedItemPosition = toLangSpinner.getSelectedItemPosition();
                    fromLangSpinner.setSelection(toLangSelectedItemPosition);
                    toLangSpinner.setSelection(fromLangSelectedItemPosition);
                }
            }
        });

        translateButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getActionMasked()) {
                    case MotionEvent.ACTION_DOWN:
                        if (NetUtil.isNetworkAvailable(context)) {
                            v.setPressed(true);
                            if (hasPermission) {
                                startAudioRecording();
                            } else {
                                Log.e(TAG, "未授予应用权限");
                                Toast.makeText(context, "请授予应用权限", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Log.e(TAG, "网络连接错误");
                            Toast.makeText(context, "网络似乎走丢了", Toast.LENGTH_SHORT).show();
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                        v.setPressed(false);
                        if (hasPermission && isRecording) {
                            stopAudioRecordingAndTranslate();
                        }
                        break;
                    default:
                        break;
                }
                return true;
            }
        });
    }

    private void initAudio() {
        bufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE_IN_HZ, CHANNEL_CONFIG, AUDIO_FORMAT_ENCODING);
        if (context.getResources().getIdentifier("speech_recognition_start_audio", "raw",
                context.getPackageName()) != 0) {
            recognitionStartPlayer = MediaPlayer.create(context, R.raw.speech_recognition_start_audio);
        } else {
            Log.w(TAG, "Raw resource speech_recognition_start_audio not found.");
            recognitionStartPlayer = null;
        }
    }

    // 开始语音识别 -> Start Audio Recording
    @SuppressLint("MissingPermission")
    private void startAudioRecording() {
        if (isRecording) {
            Log.w(TAG, "Already recording.");
            return;
        }
        Log.d(TAG, "开始录音");
        try {
            if (recognitionStartPlayer != null && recognitionStartPlayer.isPlaying()) {
                recognitionStartPlayer.stop(); // Stop if playing
                recognitionStartPlayer.prepare(); // Prepare again or re-create
            }
            if (recognitionStartPlayer != null) {
                recognitionStartPlayer.start();
            }
        } catch (IllegalStateException | IOException e) {
            Log.e(TAG, "MediaPlayer error: " + e.getMessage());
        }

        audioRecord = new AudioRecord(AUDIO_SOURCE, SAMPLE_RATE_IN_HZ, CHANNEL_CONFIG, AUDIO_FORMAT_ENCODING,
                bufferSize);
        if (audioRecord.getState() != AudioRecord.STATE_INITIALIZED) {
            Log.e(TAG, "AudioRecord initialization failed.");
            Toast.makeText(context, "录音设备初始化失败", Toast.LENGTH_SHORT).show();
            return;
        }

        audioDataStream = new ByteArrayOutputStream();
        isRecording = true;
        audioRecord.startRecording();

        recordingThread = new Thread(() -> {
            byte[] data = new byte[bufferSize];
            while (isRecording && audioRecord != null
                    && audioRecord.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING) {
                int read = audioRecord.read(data, 0, bufferSize);
                if (AudioRecord.ERROR_INVALID_OPERATION != read && AudioRecord.ERROR_BAD_VALUE != read && read > 0) {
                    audioDataStream.write(data, 0, read);
                }
            }
        }, "AudioRecorder Thread");
        recordingThread.start();
        Toast.makeText(context, "正在录音...", Toast.LENGTH_SHORT).show();
    }

    // 取消语音识别 -> Stop Audio Recording and Translate
    private void stopAudioRecordingAndTranslate() {
        if (!isRecording || audioRecord == null) {
            Log.w(TAG, "Not recording or audioRecord is null.");
            return;
        }
        Log.d(TAG, "录音结束，准备翻译");
        isRecording = false; // Signal the recording thread to stop

        // It's important to stop and release the AudioRecord before joining the thread
        // or accessing the stream, to ensure all data is flushed.
        if (audioRecord.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING) {
            audioRecord.stop();
        }
        audioRecord.release();
        audioRecord = null;

        if (recordingThread != null) {
            try {
                recordingThread.join(500); // Wait for the recording thread to finish
            } catch (InterruptedException e) {
                Log.e(TAG, "Recording thread interruption", e);
                Thread.currentThread().interrupt(); // Preserve interrupt status
            }
            recordingThread = null;
        }

        byte[] audioBytes = audioDataStream.toByteArray();
        try {
            audioDataStream.close();
        } catch (IOException e) {
            Log.e(TAG, "Failed to close audioDataStream", e);
        }

        if (audioBytes.length == 0) {
            Log.w(TAG, "录音数据为空");
            Toast.makeText(context, "未录制到音频", Toast.LENGTH_SHORT).show();
            return;
        }
        // Max 4MB before base64 encoding as per Baidu docs (roughly 4 * 1024 * 1024
        // bytes)
        // Audio duration check (e.g., 16000 samples/sec * 2 bytes/sample * 60 sec =
        // 1,920,000 bytes for PCM 16-bit mono)
        // 4MB is 4,194,304 bytes. This is a generous limit for 60s of PCM.
        if (audioBytes.length > 4 * 1024 * 1024) {
            Log.e(TAG, "录音文件过大: " + audioBytes.length + " bytes");
            Toast.makeText(context, "录音文件过大，请保持在60秒内", Toast.LENGTH_LONG).show();
            return;
        }

        Log.d(TAG, "录音数据大小: " + audioBytes.length + " bytes");
        Toast.makeText(context, "正在翻译...", Toast.LENGTH_SHORT).show();
        translateAudioWithApi(audioBytes);
    }

    private void translateAudioWithApi(byte[] audioBytes) {
        String audioBase64 = Base64.encodeToString(audioBytes, Base64.NO_WRAP);
        String timestamp = String.valueOf(System.currentTimeMillis() / 1000);
        String fromLangAbbr = fromLang.getAbbreviation();
        String toLangAbbr = toLang.getAbbreviation();

        // Ensure abbreviations are valid for the API (e.g., "zh", "en")
        // This might need adjustment if Language.getAbbreviation() isn't directly
        // compatible.

        String stringToSign = Translator.BAIDU_TRANS_APP_ID + timestamp + audioBase64;
        String sign = Translator.signHmacSha256(Translator.BAIDU_TRANS_SECRET_KEY, stringToSign);

        if (sign == null) {
            Log.e(TAG, "签名生成失败");
            Toast.makeText(context, "翻译请求准备失败 (签名)", Toast.LENGTH_SHORT).show();
            return;
        }

        JSONObject requestJson = new JSONObject();
        try {
            requestJson.put("from", fromLangAbbr);
            requestJson.put("to", toLangAbbr);
            requestJson.put("format", "pcm"); // AudioRecord provides PCM data
            requestJson.put("voice", audioBase64);
        } catch (JSONException e) {
            Log.e(TAG, "构建JSON请求体失败", e);
            Toast.makeText(context, "翻译请求准备失败 (JSON)", Toast.LENGTH_SHORT).show();
            return;
        }

        StringEntity entity;
        try {
            entity = new StringEntity(requestJson.toString(), StandardCharsets.UTF_8);
        } catch (Exception e) { // More general catch for StringEntity issues
            Log.e(TAG, "创建StringEntity失败", e);
            Toast.makeText(context, "翻译请求准备失败 (Entity)", Toast.LENGTH_SHORT).show();
            return;
        }

        // Clear previous headers if any, or use a new HttpClient instance for each
        // request
        // For simplicity, clearing headers if AsyncHttpClient is reused. Best to set
        // headers per request.
        httpClient.removeAllHeaders(); // Important if httpClient is a long-lived instance
        httpClient.addHeader("Content-Type", "application/json");
        httpClient.addHeader("X-Appid", Translator.BAIDU_TRANS_APP_ID);
        httpClient.addHeader("X-Timestamp", timestamp);
        httpClient.addHeader("X-Sign", sign);

        Log.d(TAG, "Sending API request to: " + Translator.BAIDU_VOICE_TRANS_API_URL);
        Log.d(TAG, "Request headers: X-Appid=" + Translator.BAIDU_TRANS_APP_ID + ", X-Timestamp=" + timestamp
                + ", X-Sign=" + sign);
        // Log.d(TAG, "Request body: " + requestJson.toString()); // Be cautious logging
        // full base64 audio

        httpClient.post(context, Translator.BAIDU_VOICE_TRANS_API_URL, entity, "application/json",
                new AsyncHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                        String responseString = new String(responseBody, StandardCharsets.UTF_8);
                        Log.d(TAG, "API 响应: " + responseString);
                        try {
                            JSONObject responseJson = new JSONObject(responseString);
                            int code = responseJson.optInt("code", -1); // Default to -1 if "code" not found
                            String msg = responseJson.optString("msg", "Unknown error");

                            if (code == 0) {
                                JSONObject data = responseJson.getJSONObject("data");
                                String sourceText = data.getString("source");
                                String targetText = data.getString("target");
                                // String targetTtsBase64 = data.optString("target_tts"); // Optional TTS data

                                Log.d(TAG, "识别结果 (原文): " + sourceText);
                                Log.d(TAG, "翻译结果 (译文): " + targetText);

                                HistoryUtil.storeHistoricalRecord(sourceText, targetText, HISTORY_RECORDE_TYPE,
                                        sqLiteWritableDB);
                                historySetLinearLayout.removeAllViews();
                                HistoryUtil.showHistoricalRecords(context, HISTORY_RECORDE_TYPE, historySetLinearLayout,
                                        sqLiteWritableDB);
                                Toast.makeText(context, "翻译成功", Toast.LENGTH_SHORT).show();

                                if (NewWordUtil.isMeetNewWordCondition(fromLangAbbr, toLangAbbr, context)) {
                                    newWordEnglish = toLangAbbr.equals("en") ? targetText : sourceText;
                                    newWordChinese = toLangAbbr.equals("en") ? sourceText : targetText;
                                    NewWordUtil.judgeLegalWord(newWordEnglish, context, wordJudgeRepHandler);
                                }
                                // Optionally play TTS if feature is enabled and targetTtsBase64 is present
                                boolean isAutoTts = SPUtil.getBoolean(Translator.CONFIG_NAME,
                                        Translator.VOICE_AUTO_TTS_CONFIG_NAME, true, context);
                                if (isAutoTts && data.has("target_tts")) {
                                    String targetTtsBase64 = data.getString("target_tts");
                                    if (targetTtsBase64 != null && !targetTtsBase64.isEmpty()) {
                                        playMp3Base64(targetTtsBase64);
                                    }
                                }

                            } else {
                                Log.e(TAG, "API 错误: code=" + code + ", msg=" + msg);
                                Toast.makeText(context, "翻译失败: " + msg + " (代码: " + code + ")", Toast.LENGTH_LONG)
                                        .show();
                            }
                        } catch (JSONException e) {
                            Log.e(TAG, "解析API响应JSON失败", e);
                            Toast.makeText(context, "翻译结果解析失败", Toast.LENGTH_SHORT).show();
                        } catch (Exception e) { // Catch other potential exceptions during processing
                            Log.e(TAG, "处理API响应时发生意外错误", e);
                            Toast.makeText(context, "处理翻译结果时出错", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                        String responseString = responseBody != null ? new String(responseBody, StandardCharsets.UTF_8)
                                : "(no response body)";
                        Log.e(TAG, "API 请求失败: statusCode=" + statusCode + ", response=" + responseString, error);
                        Toast.makeText(context, "翻译请求失败 (" + statusCode + ")", Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void playMp3Base64(String mp3Base64) {
        try {
            byte[] mp3SoundByteArray = Base64.decode(mp3Base64, Base64.DEFAULT);
            // Create a temporary file to play the MP3 data
            java.io.File tempMp3 = java.io.File.createTempFile("temp_tts", "mp3", context.getCacheDir());
            tempMp3.deleteOnExit();
            java.io.FileOutputStream fos = new java.io.FileOutputStream(tempMp3);
            fos.write(mp3SoundByteArray);
            fos.close();

            MediaPlayer mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(tempMp3.getAbsolutePath());
            mediaPlayer.setOnCompletionListener(mp -> {
                mp.release();
                if (tempMp3.exists()) {
                    tempMp3.delete(); // Clean up the temp file
                }
            });
            mediaPlayer.setOnErrorListener((mp, what, extra) -> {
                Log.e(TAG, "MediaPlayer error during playback: what=" + what + ", extra=" + extra);
                mp.release();
                if (tempMp3.exists()) {
                    tempMp3.delete(); // Clean up the temp file
                }
                return true; // Error was handled
            });
            mediaPlayer.prepareAsync(); // Use prepareAsync for non-blocking preparation
            mediaPlayer.setOnPreparedListener(MediaPlayer::start);

        } catch (IOException | IllegalArgumentException | SecurityException | IllegalStateException e) {
            Log.e(TAG, "播放TTS音频失败", e);
            Toast.makeText(context, "播放译文音频失败", Toast.LENGTH_SHORT).show();
        }
    }

    // 检查权限
    private void checkPermission() {
        String[] permissions = new String[] { Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE };
        this.requestPermissions(permissions, PERMISSION_REQUEST_CODE);
    }

    // 检查权限的回调
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
            @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            boolean allowed = true;
            for (int grantResult : grantResults) {
                if (grantResult != PackageManager.PERMISSION_GRANTED) {
                    allowed = false;
                    break;
                }
            }
            if (allowed) {
                hasPermission = true;
                // No need to initTransAsrClient() anymore
                // AudioRecord will be initialized when needed
                Log.i(TAG, "所有权限已授予");
            } else {
                hasPermission = false;
                Toast.makeText(context, "未授予应用相关权限，语音翻译功能可能无法使用", Toast.LENGTH_LONG).show();
            }
        }
    }

    AsyncHttpResponseHandler wordJudgeRepHandler = new AsyncHttpResponseHandler() {
        @Override
        public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
            try {
                JSONObject judgeRepJsonObject = new JSONObject(new String(responseBody));
                if (judgeRepJsonObject.getInt("error_code") == 0) {
                    boolean isLegalWord = judgeRepJsonObject.getBoolean("legal");

                    if (isLegalWord) {
                        NewWordUtil.storeNewWord(newWordEnglish, newWordChinese, sqLiteWritableDB);
                    } else {
                        Log.d(TAG, "[" + newWordEnglish + "]不是合法单词");
                    }
                } else {
                    Log.e(TAG, "生词判断失败，响应数据为：" + judgeRepJsonObject.toString());
                    Toast.makeText(context, "生词判断失败", Toast.LENGTH_SHORT).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }

        @Override
        public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
            Log.e(TAG, "生词判断请求失败");
            error.printStackTrace();
            Toast.makeText(context, "生词判断失败", Toast.LENGTH_SHORT).show();
        }
    };

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (isRecording && audioRecord != null) {
            isRecording = false;
            try {
                if (audioRecord.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING) {
                    audioRecord.stop();
                }
            } catch (IllegalStateException e) {
                Log.e(TAG, "Error stopping audio record on destroy: " + e.getMessage());
            }
            audioRecord.release();
            audioRecord = null;
        }
        if (recordingThread != null) {
            try {
                recordingThread.interrupt(); // Interrupt if it's somehow stuck
                recordingThread.join(100); // Give it a moment to die
            } catch (InterruptedException e) {
                Log.w(TAG, "Interrupted while waiting for recording thread to finish on destroy");
                Thread.currentThread().interrupt(); // Preserve interrupt status
            }
            recordingThread = null;
        }
        if (audioDataStream != null) {
            try {
                audioDataStream.close();
            } catch (IOException e) {
                Log.e(TAG, "Failed to close audioDataStream on destroy", e);
            }
            audioDataStream = null;
        }
        if (recognitionStartPlayer != null) {
            if (recognitionStartPlayer.isPlaying()) {
                recognitionStartPlayer.stop();
            }
            recognitionStartPlayer.release();
            recognitionStartPlayer = null;
        }
        // Cancel any ongoing HTTP requests if httpClient allows and if it's appropriate
        // for your app lifecycle.
        // httpClient.cancelRequests(context, true); // Or
        // httpClient.cancelAllRequests(true);
        // Be cautious with cancelling requests in onDestroyView if they might be
        // important for background state saving.
    }
}