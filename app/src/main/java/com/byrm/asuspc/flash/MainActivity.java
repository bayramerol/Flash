package com.byrm.asuspc.flash;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.PowerManager;
import android.util.Log;
import android.view.View;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;

public class MainActivity extends Activity {

    private InterstitialAd gecisReklam;//geçiş reklam referansı
    private AdView bannerReklam, bannerReklam2;//bannerReklam reklam referansı
    private AdRequest adRequest;//adRequest referansı


    protected PowerManager.WakeLock mWakeLock;
    Camera camera;   // Kamera nesnesi oluşturuyoruz.
    Parameters parameters;  // kamera parametreleri için gerekli olan parametre değişkenini ve nesnesini bildiriyoruz. (declare etmek)
    Boolean VarFlash;   //  VarFlash değişkenimizi bildiriyoruz.





    private TextView contentTxt;
    private BroadcastReceiver mBatInfoReceiver = new BroadcastReceiver(){
        @Override
        public void onReceive(Context arg0, Intent intent) {
            // TODO Auto-generated method stub
            int level = intent.getIntExtra("level", 0);
            contentTxt.setText(getText(R.string.battery_situaiton_settext) + " "+ String.valueOf(level) );
        }};






    @SuppressWarnings("deprecation")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_main);   // uygulamamızın arayüzü dosyasını layout klasörü altındaki activity_main dosyası olacak şekilde ayarlıyoruz.




        contentTxt = (TextView) this.findViewById(R.id.monospaceTxt);
        this.registerReceiver(this.mBatInfoReceiver,
                new IntentFilter(Intent.ACTION_BATTERY_CHANGED));




//        screen.setOnClickListener(new OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//
//                WindowManager.LayoutParams params = getWindow().getAttributes();
//                params.screenBrightness = 1.0f;
//                getWindow().setAttributes(params);
//
//
//            }
//        });



        adRequest = new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                .addTestDevice("3894F32FDF323E46E7F0199F4A87215B")
                .build();



        //Burda bannerReklam objesini oluşturuyoruz ve activity_anasayfa.xml de oluşturduğumuz adView e bağlıyoruz
        bannerReklam = (AdView) this.findViewById(R.id.adView);
       // adRequest = new AdRequest.Builder().build();
        bannerReklam.loadAd(adRequest); //bannerReklam ı yüklüyoruz.



        //Burda bannerReklam objesini oluşturuyoruz ve activity_anasayfa.xml de oluşturduğumuz adView e bağlıyoruz
        bannerReklam2 = (AdView) this.findViewById(R.id.adView2);
        // adRequest = new AdRequest.Builder().build();
        bannerReklam2.loadAd(adRequest); //bannerReklam ı yüklüyoruz.



        //***********Geçiş reklam işlemleri*********
        gecisReklam = new InterstitialAd(this);
        gecisReklam.setAdUnitId("ca-app-pub-2928097859559744/6954526913");//Reklam İd miz.Admob da oluşturduğumuz geçiş reklam id si
        gecisReklam.setAdListener(new AdListener() { //Geçiş reklama listener ekliyoruz
            @Override
            public void onAdLoaded() { //Geçiş reklam Yüklendiğinde çalışır
                //Toast.makeText(getApplicationContext(), "Reklam Yüklendi.", Toast.LENGTH_LONG).show();

            }

            @Override
            public void onAdFailedToLoad(int errorCode) { //Geçiş Reklam Yüklenemediğinde Çalışır
                //Toast.makeText(getApplicationContext(), "Reklam Yüklenirken Hata Oluştu.", Toast.LENGTH_LONG).show();
            }

            public void onAdClosed(){ //Geçiş Reklam Kapatıldığında çalışır
               // Toast.makeText(getApplicationContext(), "Reklam Kapatıldı.", Toast.LENGTH_LONG).show();

            //Geçiş reklam kapatıldığı zamanda yeni reklam yükleme işlemimizi başlatabiliriz.
                //Böylelikle geçiş reklamımız gösterilmek iöçin hazırda bekler.
                loadGecisReklam();
            }
        });

        loadGecisReklam();//Geçiş reklamı yüklüyoruz



        final ImageView opbuton = (ImageView) findViewById(R.id.opbutton);  // kullandığımız üç butona java dosyasından buton nesneleri yaratıp ulaşıyoruz.
        final ImageView clbutton = (ImageView) findViewById(R.id.clbutton);


        /* This code together with the one in onDestroy()
         * will make the screen be always on until this Activity gets destroyed. */
        final PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        this.mWakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "My Tag");
        this.mWakeLock.acquire();


        camera = Camera.open();  // burada kameramızı açıyoruz yani kameraya bağlanıyoruz.
        parameters = camera.getParameters(); // kamera parametrelerini alıyoruz.
        camera.startPreview(); // kamera nın parametrelere göre iş yapmasını burada sağlıyoruz.


	/*
	 * First check if device is supporting flashlight or not
	 */
        VarFlash = getApplicationContext().getPackageManager()
                .hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);  // Kameramız flash a sahip mi? Onun bilgisini VarFlash değişkenine atıyoruz.

        if (!VarFlash) {
            // cihazın flashı yok!
            // bir alarm mesajı gösterip uygulamayı kapatıyoruz.
            AlertDialog alert = new AlertDialog.Builder(MainActivity.this)
                    .create();
            alert.setTitle(MainActivity.this.getString(R.string.error_alert));
            alert.setMessage(MainActivity.this.getString(R.string.alert_message));  //alert dialogda stringden alma
            alert.setButton(MainActivity.this.getString(R.string.alert_button), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    // uygulamayı kapatıyoruz
                    finish();
                }
            });
            alert.show();   // alarm mesajını gösteriyoruz
            return;
        }

        opbuton.setOnClickListener(new OnClickListener() {

            public void onClick(View arg0) {
                // opbuton a dokunulduğunda kameranın parametrelerini flashı açacak şekilde ayarlayıp kameraya yolluyoruz.


                parameters.setFlashMode(Parameters.FLASH_MODE_TORCH);
                camera.setParameters(parameters);
                opbuton.setVisibility(View.INVISIBLE);
                clbutton.setVisibility(View.VISIBLE);
                showGecisReklam();
            }
        });

        clbutton.setOnClickListener(new OnClickListener() {

            public void onClick(View arg0) {

                // opbuton a dokunulduğunda kameranın parametrelerini flashı kapatacak şekilde ayarlayıp kameraya yolluyoruz.
                showGecisReklam();
                parameters.setFlashMode(Parameters.FLASH_MODE_OFF);
                camera.setParameters(parameters);
                opbuton.setVisibility(View.VISIBLE);
                clbutton.setVisibility(View.INVISIBLE);
            }

        });



    }



    @Override
    protected void onDestroy() {
        this.mWakeLock.release();
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        // TODO Auto-generated method stub

        showGecisReklam();
        super.onBackPressed();

        parameters = camera.getParameters();
        parameters.setFlashMode(Parameters.FLASH_MODE_OFF);
        camera.setParameters(parameters);
        camera.stopPreview();
        VarFlash = false;

        if (camera != null) {
            camera.release();
            camera = null;
        }
        Log.d("Camera", "Back Pressed");
    }

    /*@Override
    protected void onRestart() {
        super.onRestart();
    }
*/
    @Override
    protected void onStop() {
        super.onStop();

      /*  // durma durumunda(mesela kullanıcı telefonunda geri butonuna basarsa) kamerayı serbest bırakıyoruz. Başka bir uygulama kullanmak isterse problem çıkmaması için.
        if (camera != null) {
            camera.release();
            camera = null;
        }*/
    }

    public void loadGecisReklam() {//Geçiþ reklamý Yüklemek için





        //Device id mizi yazıyoruz ki reklamımızı test ederken istedimiz kadar tıklayalım
        //Google bu device id den tıklanan reklamlara ücret ödemeyecek bunun test için kullanıldığını bilecek
        //Eğer bunu yazmazsak Google haksız kazanç elde edeceğimizi düşünüp hesabımızı banlayabilir.

        //Device id yi bulmak için uygulamanızı çalıştırdıktan sorna LogCat i açıyoruz
        //Filtreleme Kısmına AdRequest veya device yazıyoruz.
        //Filtreleme sonucu olarak   "Use AdRequest.Builder.addTestDevice("C521B8BE91B4860C229030D8E3CEA254") to get test ads on this device."
        //yukardaki gibi bir sonuç çıkacaktır. Yukarda C521... ile başlayan kısım device id nizdir
        //Bunu yapmayı kesinlikle unutmayın yoksa banlanırsınız.

        adRequest = new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                .addTestDevice("3894F32FDF323E46E7F0199F4A87215B")
                .build();







        //Reklam Yükleniyor
        gecisReklam.loadAd(adRequest);
    }
    public void showGecisReklam() {//Geçiþ reklamý Göstermek için

        if (gecisReklam.isLoaded()) {//Eðer reklam yüklenmiþse kontrol ediliyor
            gecisReklam.show(); //Reklam yüklenmiþsse gösterilecek
        } else {//reklam yüklenmemiþse
            //Toast.makeText(getApplicationContext(), "Reklam Gösterim Ýçin Hazýr Deðil.", Toast.LENGTH_LONG).show();
        }
    }
}