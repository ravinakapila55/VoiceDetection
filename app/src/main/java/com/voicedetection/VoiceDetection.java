package com.voicedetection;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.Voice;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.ml.common.modeldownload.FirebaseModelDownloadConditions;
import com.google.firebase.ml.naturallanguage.FirebaseNaturalLanguage;
import com.google.firebase.ml.naturallanguage.languageid.FirebaseLanguageIdentification;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslateLanguage;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslator;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslatorOptions;

import java.util.ArrayList;
import java.util.Locale;

public class VoiceDetection  extends AppCompatActivity {

    ConstraintLayout ccLanguage;
    TextView tvListening,tvEnglish,tvArabic,tvGerman,tvMicLabel;
    ImageView ivMic;
    private SpeechRecognizer speechRecognizer;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.voice_detection);

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED)
        {
            checkPermission();
        }

        findIds();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1 && grantResults.length > 0 ){
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED)
                Toast.makeText(this,"Permission Granted",Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        speechRecognizer.destroy();
    }

    private void checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.RECORD_AUDIO},1);
        }
    }

    public void startSpeechRecognition()
    {
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        final Intent speechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());

        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle bundle) {
                Log.e("onReadyForSpeech ",bundle.toString());
                Log.e("Method ","onReadyForSpeech");

            }

            @Override
            public void onBeginningOfSpeech() {
                Log.e("onBeginningOfSpeech ","bundle.toString()");
                Log.e("Method ","onBeginningOfSpeech");

                tvListening.setVisibility(View.VISIBLE);
                tvMicLabel.setVisibility(View.GONE);
                ccLanguage.setVisibility(View.GONE);
            }

            public void onRmsChanged(float v) {
                Log.e("onRmsChanged ","onRmsChanged");
                Log.e("Method ","onRmsChanged");


            }

            @Override
            public void onBufferReceived(byte[] bytes)
            {
                Log.e("onBufferReceived ","onBufferReceived");
                Log.e("Method ","onBufferReceived");
            }

            @Override
            public void onEndOfSpeech()
            {
                Log.e("onEndOfSpeech ","onEndOfSpeech");
                Log.e("Method ","onEndOfSpeech");
            }

            @Override
            public void onError(int i)
            {
                Log.e("onError ",i+"");
                Log.e("Method ","onError");

            }

            @Override
            public void onResults(Bundle bundle) {
                Log.e("Method ","onResults");

                tvListening.setVisibility(View.GONE);
                ivMic.setImageResource(R.drawable.mic);
                ArrayList<String> data = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                Log.e("onResultsData ",data.toString());
                ccLanguage.setVisibility(View.VISIBLE);
                tvMicLabel.setVisibility(View.VISIBLE);
                tvEnglish.setText(getResources().getString(R.string.english)+" " +data.get(0));
                translateTextToGerman(data.get(0).toString());
                translateTextToArabic(data.get(0).toString());
            }

            @Override
            public void onPartialResults(Bundle bundle)
            {
                Log.e("onPartialResults ",bundle+"");
                Log.e("Method ","onPartialResults");
            }

            @Override
            public void onEvent(int i, Bundle bundle)
            {
                Log.e("onEvent ",bundle+" index "+i);
                Log.e("Method ","onEvent");

            }
        });

        ivMic.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_UP){
                    speechRecognizer.stopListening();
                }
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN){
                    ivMic.setImageResource(R.drawable.mic_enable);
                    speechRecognizer.startListening(speechRecognizerIntent);
                }
                return false;
            }
        });

    }

    public void findIds()
    {
        ccLanguage=(ConstraintLayout) findViewById(R.id.ccLanguage);
        tvListening=(TextView) findViewById(R.id.tvListening);
        tvEnglish=(TextView) findViewById(R.id.tvEnglish);
        tvArabic=(TextView) findViewById(R.id.tvArabic);
        tvGerman=(TextView) findViewById(R.id.tvGerman);
        tvMicLabel=(TextView) findViewById(R.id.tvMicLabel);
        ivMic=(ImageView) findViewById(R.id.ivMic);

        startSpeechRecognition();
    }

    public void translateText(FirebaseTranslator langTranslator, String lang,String text) {
        //translate source text to english
        langTranslator.translate(text.toString())
                .addOnSuccessListener(
                        new OnSuccessListener<String>() {
                            @Override
                            public void onSuccess(@NonNull String translatedText) {

                                if (lang.equalsIgnoreCase("ar"))
                                {
                                    tvArabic.setText(getResources().getString(R.string.arabic)+" " +translatedText);
                                }
                                else  if (lang.equalsIgnoreCase("gr")) {
                                    tvGerman.setText(getResources().getString(R.string.german)+" "+translatedText);
                                }


                            }
                        })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(VoiceDetection.this,
                                        "Problem in translating the text entered",
                                        Toast.LENGTH_LONG).show();
                            }
                        });

    }

    public void downloadTranslatorAndTranslate(String lang,String text) {

        FirebaseTranslatorOptions options =null;
        if (lang.equalsIgnoreCase("ar"))
        {
            options =
                    new FirebaseTranslatorOptions.Builder()
                            .setSourceLanguage(FirebaseTranslateLanguage.EN)
                            .setTargetLanguage(FirebaseTranslateLanguage.AR)
                            .build();
        }

        else
        {
            options =
                    new FirebaseTranslatorOptions.Builder()
                            .setSourceLanguage(FirebaseTranslateLanguage.EN)
                            .setTargetLanguage(FirebaseTranslateLanguage.DE)
                            .build();
        }


        final FirebaseTranslator langTranslator =
                FirebaseNaturalLanguage.getInstance().getTranslator(options);

        //download language models if needed
        FirebaseModelDownloadConditions conditions = new FirebaseModelDownloadConditions.Builder()
                .requireWifi()
                .build();
        langTranslator.downloadModelIfNeeded(conditions)
                .addOnSuccessListener(
                        new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void v) {
                                Log.e("translator", "downloaded lang  model");
                                //after making sure language models are available
                                //perform translation
                                translateText(langTranslator,lang,text);
                            }
                        })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(VoiceDetection.this,
                                        "Problem in translating the text entered",
                                        Toast.LENGTH_LONG).show();
                            }
                        });
    }

    public void translateTextToGerman(String text) {
        //First identify the language of the entered text
        FirebaseLanguageIdentification languageIdentifier =
                FirebaseNaturalLanguage.getInstance().getLanguageIdentification();
        languageIdentifier.identifyLanguage(text)
                .addOnSuccessListener(
                        new OnSuccessListener<String>() {
                            @Override
                            public void onSuccess(@Nullable String languageCode) {
                                Log.e("onSuccesstranslator", "lang "+languageCode);
                                if (languageCode != "und") {
                                    Log.e("translator", "lang "+languageCode);
                                    //download translator for the identified language
                                    // and translate the entered text into english
                                    downloadTranslatorAndTranslate("gr",text);
                                } else {
                                    Toast.makeText(VoiceDetection.this,
                                            "Could not identify language of the text entered",
                                            Toast.LENGTH_LONG).show();
                                }
                            }
                        })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(VoiceDetection.this,
                                        "Problem in identifying language of the text entered",
                                        Toast.LENGTH_LONG).show();
                            }
                        });
    }

    public void translateTextToArabic(String text) {
        //First identify the language of the entered text
        FirebaseLanguageIdentification languageIdentifier =
                FirebaseNaturalLanguage.getInstance().getLanguageIdentification();
        languageIdentifier.identifyLanguage(text)
                .addOnSuccessListener(
                        new OnSuccessListener<String>() {
                            @Override
                            public void onSuccess(@Nullable String languageCode) {
                                Log.e("onSuccesstranslator", "lang "+languageCode);
                                if (languageCode != "und") {
                                    Log.e("translator", "lang "+languageCode);
                                    //download translator for the identified language
                                    // and translate the entered text into english
                                    downloadTranslatorAndTranslate("ar",text);
                                } else {
                                    Toast.makeText(VoiceDetection.this,
                                            "Could not identify language of the text entered",
                                            Toast.LENGTH_LONG).show();
                                }
                            }
                        })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(VoiceDetection.this,
                                        "Problem in identifying language of the text entered",
                                        Toast.LENGTH_LONG).show();
                            }
                        });
    }
}
