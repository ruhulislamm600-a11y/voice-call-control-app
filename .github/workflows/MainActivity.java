package com.example.honorcallcontrol;

import android.app.Activity;
import android.app.role.RoleManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends Activity {

    private TextView statusText;
    private SpeechRecognizer speechRecognizer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        statusText = findViewById(R.id.statusText);

        Button receiveButton = findViewById(R.id.receiveButton);
        Button endButton = findViewById(R.id.endButton);
        Button voiceButton = findViewById(R.id.voiceButton);

        receiveButton.setOnClickListener(v -> {
            statusText.setText("কল রিসিভ করার কমান্ড দেওয়া হয়েছে");
            answerCall();
        });

        endButton.setOnClickListener(v -> {
            statusText.setText("কল কাটার কমান্ড দেওয়া হয়েছে");
            endCall();
        });

        voiceButton.setOnClickListener(v -> startVoiceRecognition());

        requestDefaultPhoneRole();
    }

    private void requestDefaultPhoneRole() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            RoleManager roleManager =
                    (RoleManager) getSystemService(Context.ROLE_SERVICE);

            if (roleManager != null &&
                    roleManager.isRoleAvailable(RoleManager.ROLE_DIALER) &&
                    !roleManager.isRoleHeld(RoleManager.ROLE_DIALER)) {

                Intent intent =
                        roleManager.createRequestRoleIntent(RoleManager.ROLE_DIALER);

                startActivityForResult(intent, 100);
            }
        }
    }

    private void startVoiceRecognition() {

        if (!SpeechRecognizer.isRecognitionAvailable(this)) {
            statusText.setText("এই ফোনে Voice Recognition পাওয়া যায় না");
            return;
        }

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);

        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
        );
        intent.putExtra(
                RecognizerIntent.EXTRA_LANGUAGE,
                "bn-BD"
        );

        speechRecognizer.setRecognitionListener(
                new android.speech.RecognitionListener() {

                    @Override
                    public void onResults(Bundle results) {

                        ArrayList<String> matches =
                                results.getStringArrayList(
                                        SpeechRecognizer.RESULTS_RECOGNITION
                                );

                        if (matches != null && !matches.isEmpty()) {

                            String text = matches.get(0).toLowerCase(
                                    Locale.getDefault()
                            );

                            statusText.setText("আপনি বলেছেন: " + text);

                            if (text.contains("কল রিসিভ")
                                    || text.contains("কল ধর")
                                    || text.contains("কল ধরো")) {

                                answerCall();

                            } else if (text.contains("কল কেটে")
                                    || text.contains("কল কাট")
                                    || text.contains("কল শেষ")) {

                                endCall();

                            } else {
                                statusText.setText(
                                        "কমান্ড বোঝা যায়নি"
                                );
                            }
                        }
                    }

                    @Override public void onReadyForSpeech(Bundle params) {}
                    @Override public void onBeginningOfSpeech() {}
                    @Override public void onRmsChanged(float rmsdB) {}
                    @Override public void onBufferReceived(byte[] buffer) {}
                    @Override public void onEndOfSpeech() {}
                    @Override public void onError(int error) {}
                    @Override public void onPartialResults(Bundle partialResults) {}
                    @Override public void onEvent(int eventType, Bundle params) {}
                }
        );

        speechRecognizer.startListening(intent);
        statusText.setText("শুনছি...");
    }

    private void answerCall() {
        statusText.setText("কল রিসিভ করার চেষ্টা করা হচ্ছে...");

        // কল রিসিভ করার জন্য ফোন অ্যাপের ডিফল্ট
        // কল-ম্যানেজমেন্ট সুবিধা প্রয়োজন।
    }

    private void endCall() {
        statusText.setText("কল কাটার চেষ্টা করা হচ্ছে...");

        // কল কাটার জন্য অ্যাপকে Default Phone/Dialer
        // হিসেবে সেট করতে হবে।
    }

    @Override
    protected void onDestroy() {

        if (speechRecognizer != null) {
            speechRecognizer.destroy();
        }

        super.onDestroy();
    }
}
