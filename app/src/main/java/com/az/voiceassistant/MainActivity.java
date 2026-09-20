package com.az.voiceassistant;

import android.Manifest;
import android.app.*;
import android.content.*;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import java.util.*;

public class MainActivity extends AppCompatActivity {
  private static final int SPEECH=77;
  private TextView status; private TextToSpeech tts;
  private final SharedPreferences.OnSharedPreferenceChangeListener unused=null;
  @Override public void onCreate(Bundle b){ super.onCreate(b); setContentView(R.layout.activity_main);
    status=findViewById(R.id.status); EditText server=findViewById(R.id.server);
    server.setText(getPreferences(0).getString("server",""));
    tts=new TextToSpeech(this, ok->{ if(ok==TextToSpeech.SUCCESS){ int r=tts.setLanguage(new Locale("bn","BD")); if(r<0) tts.setLanguage(Locale.UK); tts.setPitch(1.08f); tts.setSpeechRate(.92f); }});
    findViewById(R.id.listen).setOnClickListener(v->listen());
    findViewById(R.id.wake).setOnClickListener(v->startWake());
    findViewById(R.id.save).setOnClickListener(v->{ String u=server.getText().toString().trim(); if(!u.isEmpty()&&!u.startsWith("https://")){toast("HTTPS server required");return;} getPreferences(0).edit().putString("server",u).apply(); toast("Saved securely"); });
    findViewById(R.id.test).setOnClickListener(v->{ String u=server.getText().toString().trim(); if(!u.startsWith("https://")){toast("Add your HTTPS server URL first");return;} web(u); });
    findViewById(R.id.remove).setOnClickListener(v->{ server.setText(""); getPreferences(0).edit().remove("server").apply(); toast("AI connection removed"); });
    requestPermissions();
  }
  private void requestPermissions(){ArrayList<String> p=new ArrayList<>(); if(checkSelfPermission(Manifest.permission.RECORD_AUDIO)!=PackageManager.PERMISSION_GRANTED)p.add(Manifest.permission.RECORD_AUDIO); if(android.os.Build.VERSION.SDK_INT>=33&&checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)!=PackageManager.PERMISSION_GRANTED)p.add(Manifest.permission.POST_NOTIFICATIONS); if(!p.isEmpty()) ActivityCompat.requestPermissions(this,p.toArray(new String[0]),5);}
  private void listen(){ Intent i=new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH); i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM); i.putExtra(RecognizerIntent.EXTRA_LANGUAGE,"bn-BD"); i.putExtra(RecognizerIntent.EXTRA_PROMPT,"বলুন…"); startActivityForResult(i,SPEECH); }
  private void startWake(){ if(checkSelfPermission(Manifest.permission.RECORD_AUDIO)!=PackageManager.PERMISSION_GRANTED){requestPermissions();return;} startForegroundService(new Intent(this,WakeService.class)); status.setText("Hey AZ চালু আছে • Always confirm enabled"); speak("হেই এ জেড চালু হয়েছে"); }
  @Override protected void onActivityResult(int r,int c,Intent d){super.onActivityResult(r,c,d); if(r==SPEECH&&c==RESULT_OK&&d!=null){ArrayList<String>x=d.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS); if(x!=null&&!x.isEmpty()) route(x.get(0));}}
  private void route(String raw){String q=raw.trim(); status.setText("আপনি বলেছেন: "+q); String l=q.toLowerCase(Locale.ROOT);
    if(l.contains("whatsapp")||l.contains("হোয়াটসঅ্যাপ")||l.contains("হোয়াটসঅ্যাপ")){confirm("WhatsApp খুলব?",()->share("com.whatsapp",""));}
    else if(l.contains("email")||l.contains("gmail")||l.contains("ইমেইল")){confirm("ইমেইল খুলব?",()->startActivity(new Intent(Intent.ACTION_SENDTO,Uri.parse("mailto:"))));}
    else if(l.contains("call")||l.contains("ফোন")||l.contains("কল")){confirm("ফোন ডায়ালার খুলব?",()->startActivity(new Intent(Intent.ACTION_DIAL)));}
    else if(l.contains("flight")||l.contains("air ticket")||l.contains("বিমান")){confirm("ফ্লাইট খুঁজব?",()->web("https://www.google.com/travel/flights"));}
    else if(l.contains("hotel")||l.contains("হোটেল")){confirm("হোটেল খুঁজব?",()->web("https://www.google.com/travel/hotels"));}
    else if(l.contains("bus")||l.contains("বাস")){confirm("বাসের টিকিট খুঁজব?",()->web("https://www.google.com/search?q="+Uri.encode(q)));}
    else {speak("আমি শুনেছি। এই প্রশ্নের জন্য নিরাপদ এ আই সার্ভার সংযোগ করুন।");}
  }
  private void confirm(String msg,Runnable yes){new AlertDialog.Builder(this).setTitle("AZ confirmation").setMessage(msg+"\n\nAZ will never confirm payment automatically.").setPositiveButton("হ্যাঁ / YES",(d,w)->yes.run()).setNegativeButton("না / NO",null).show(); speak(msg);}
  private void share(String pkg,String text){Intent i=new Intent(Intent.ACTION_SEND);i.setType("text/plain");i.putExtra(Intent.EXTRA_TEXT,text);i.setPackage(pkg);try{startActivity(i);}catch(Exception e){toast("App not installed");}}
  private void web(String u){startActivity(new Intent(Intent.ACTION_VIEW,Uri.parse(u)));}
  private void speak(String s){if(tts!=null)tts.speak(s,TextToSpeech.QUEUE_FLUSH,null,"az");}
  private void toast(String s){Toast.makeText(this,s,Toast.LENGTH_SHORT).show();}
  @Override protected void onDestroy(){if(tts!=null){tts.stop();tts.shutdown();}super.onDestroy();}
}
