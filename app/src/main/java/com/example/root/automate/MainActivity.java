package com.example.root.automate;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import org.json.JSONObject;

import java.net.SocketTimeoutException;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;



public class MainActivity extends AppCompatActivity {
    final OkHttpClient client = new OkHttpClient();
    CompoundButton.OnCheckedChangeListener sListener;
    Button.OnClickListener bListener;
    final Switch[] s=new Switch[6];
    TextView t;
    Button M_on,M_off;
    ConnectivityManager conman;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        t= (TextView) findViewById(R.id.textView2);

        M_on= (Button) findViewById(R.id.button1);
        M_off= (Button) findViewById(R.id.button2);

        s[0]= (Switch) findViewById(R.id.switch1);
        s[1]= (Switch) findViewById(R.id.switch2);
        s[2]= (Switch) findViewById(R.id.switch3);
        s[3]= (Switch) findViewById(R.id.switch4);
        s[4]= (Switch) findViewById(R.id.switch5);
        s[5]= (Switch) findViewById(R.id.switch6);

        sListener=new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                switch(buttonView.getId())
                {
                    case R.id.switch1:
                        new send_req(s).execute(1);
                        break;
                    case R.id.switch2:
                        new send_req(s).execute(2);
                        break;
                    case R.id.switch3:
                        new send_req(s).execute(3);
                        break;
                    case R.id.switch4:
                        new send_req(s).execute(4);
                        break;
                    case R.id.switch5:
                        new send_req(s).execute(5);
                        break;
                    case R.id.switch6:
                        new send_req(s).execute(6);
                        break;
                }

            }

        };

        bListener= new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                switch (v.getId()){
                    case R.id.button1:
                        new send_req(s).execute(-2);
                        break;
                    case R.id.button2:
                        new send_req(s).execute(-1);
                        break;
                }
            }
        };

        //s[0].setOnCheckedChangeListener(sListener);
        s[1].setOnCheckedChangeListener(sListener);
        s[2].setOnCheckedChangeListener(sListener);
        s[3].setOnCheckedChangeListener(sListener);
        s[4].setOnCheckedChangeListener(sListener);
        s[5].setOnCheckedChangeListener(sListener);
        s[5].setOnCheckedChangeListener(sListener);
        M_off.setOnClickListener(bListener);
        M_on.setOnClickListener(bListener);
    }

    public void getNetworkState(int status){
        AlertDialog.Builder b= new AlertDialog.Builder(this);
        b.setTitle("Error");
        if (status==1){
            b.setMessage("Please connect to Wi-Fi and Restart the app");
        }
        else if (status==2){
            b.setMessage("Please connect to the correct Wi-Fi and Restart the app");
        }
        b.setNegativeButton("Close", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });

        AlertDialog alertDialog= b.create();
        alertDialog.show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        conman = (ConnectivityManager) getSystemService(getBaseContext().CONNECTIVITY_SERVICE);
        NetworkInfo mwifi = conman.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

        if (mwifi.isConnected() == false) {
            getNetworkState(1);
            Log.e("Network"," not connected");
        }
        else
            new onresume_req(s).execute(0);
    }

    class send_req extends AsyncTask<Integer,Void,Integer[]> {
        Switch[] s;
        int i=0;
        Integer[] k=new Integer[7];
        String sss;
        ProgressDialog progressDialog=new ProgressDialog(MainActivity.this);
        public send_req(Switch[] s) {
            this.s = s;
        }

        @Override
        protected Integer[] doInBackground(Integer... params) {
            RequestBody formBody = new FormBody.Builder()
                    .add("load", params[0]+"")
                    .build();
            Request request = new Request.Builder()
                    .url("http://192.168.0.120:8081/automation")
                    .post(formBody)
                    .build();
            Log.e("Request built",1+"");
            try {
                Log.e("Sending request!",1+"");
                Response response = client.newCall(request).execute();
                Log.e("Request sent",1+"");
                String responseString = response.body().string();
                response.body().close();
                sss=responseString+"";
                Log.e("Response Recieved",1+"");
                JSONObject j=new JSONObject(responseString);
                for (i=0;i<=5;i++){
                    k[i]=j.getInt("state"+(i+1)+"");
                }

                k[i]=j.getInt("temper");
                Log.e("Getting out of bg",1+"");
            } catch (Exception e) {
                e.printStackTrace();
                return k=null;
            }
            return k;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog.setMessage("Loading Status...");
            progressDialog.show();
        }

        @Override
        protected void onPostExecute(Integer[] k) {
            super.onPostExecute(k);
            Log.e("Recieved Response:",sss);
           if (k==null){
               getNetworkState(2);
           }
           else{

               for(i=0;i<=5;i++){
                   if (k[i]==1){
                       s[i].setOnCheckedChangeListener(null);
                       s[i].setChecked(true);
                       s[i].setOnCheckedChangeListener(sListener);
                   }
                   else if(k[i]==0){
                       s[i].setOnCheckedChangeListener(null);
                       s[i].setChecked(false);
                       s[i].setOnCheckedChangeListener(sListener);
                   }
               }
               Log.e("switch set",i+"");
               t.setText(""+k[i]+" *C");
               Log.e("Temperature got",k[i]+"");
               progressDialog.dismiss();
           }

        }


    }

    class onresume_req extends AsyncTask<Integer,Void,Integer[]>{

        Switch[] s;
        int i=0;
        Integer[] k=new Integer[7];
        String sss;
        public onresume_req(Switch[] s) {
            this.s = s;
        }

        @Override
        protected Integer[] doInBackground(Integer... params) {
            RequestBody formBody = new FormBody.Builder()
                    .add("load", params[0]+"")
                    .build();
            Request request = new Request.Builder()
                    .url("http://192.168.0.120:8081/automation")
                    .post(formBody)
                    .build();
            try {
                Response response = client.newCall(request).execute();
                String responseString = response.body().string();
                response.body().close();

                sss=responseString+"";
                JSONObject j=new JSONObject(responseString);

                for (i=0;i<=5;i++){
                    k[i]=j.getInt("state"+(i+1)+"");
                }

                k[i]=j.getInt("temper");
            } catch (Exception e) {
                e.printStackTrace();
                return k=null;
            }
            return k;
        }

        @Override
        protected void onPostExecute(Integer[] k) {
            super.onPostExecute(k);
            Log.e("-----------","1");
            if (k==null)
            {
                getNetworkState(2);
            }
            else
            {
                for(i=0;i<=5;i++){
                    if (k[i]==1){
                        s[i].setOnCheckedChangeListener(null);
                        s[i].setChecked(true);
                        s[i].setOnCheckedChangeListener(sListener);
                    }
                    else if(k[i]==0){
                        s[i].setOnCheckedChangeListener(null);
                        s[i].setChecked(false);
                        s[i].setOnCheckedChangeListener(sListener);
                    }
                }
                t.setText(""+k[i]+" *C");
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        new onresume_req(s).execute(0);
                    }
                }, 2000);

            }
        }


    }

}