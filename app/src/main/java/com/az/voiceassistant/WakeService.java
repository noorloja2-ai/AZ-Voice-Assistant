package com.az.voiceassistant;

import android.app.*;import android.content.*;import android.os.*;import android.speech.*;import androidx.core.app.NotificationCompat;import java.util.*;

public class WakeService extends Service implements RecognitionListener {
 private SpeechRecognizer sr; private Intent intent; private boolean running;
 @Override public void onCreate(){super.onCreate(); String id="az_wake"; NotificationManager nm=getSystemService(NotificationManager.class); nm.createNotificationChannel(new NotificationChannel(id,"Hey AZ",NotificationManager.IMPORTANCE_LOW)); startForeground(11,new NotificationCompat.Builder(this,id).setSmallIcon(android.R.drawable.ic_btn_speak_now).setContentTitle("AZ Voice Assistant").setContentText("Listening for Hey AZ").setOngoing(true).build());
  sr=SpeechRecognizer.createSpeechRecognizer(this);sr.setRecognitionListener(this);intent=new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE,"bn-BD");intent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS,true);start();}
 private void start(){if(running)return;running=true;try{sr.startListening(intent);}catch(Exception e){restart();}}
 private void restart(){running=false;new Handler(Looper.getMainLooper()).postDelayed(this::start,700);}
 private void check(Bundle b){ArrayList<String>x=b.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);if(x!=null)for(String s:x){String q=s.toLowerCase(Locale.ROOT);if(q.contains("hey az")||q.contains("হেই এ জেড")){Intent a=new Intent(this,MainActivity.class);a.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_SINGLE_TOP);startActivity(a);break;}}}
 @Override public void onResults(Bundle b){check(b);restart();}@Override public void onPartialResults(Bundle b){check(b);}@Override public void onError(int e){restart();}@Override public void onReadyForSpeech(Bundle b){}@Override public void onBeginningOfSpeech(){}@Override public void onRmsChanged(float r){}@Override public void onBufferReceived(byte[] b){}@Override public void onEndOfSpeech(){running=false;}@Override public void onEvent(int t,Bundle b){}
 @Override public android.os.IBinder onBind(Intent i){return null;}@Override public void onDestroy(){if(sr!=null)sr.destroy();super.onDestroy();}
}
