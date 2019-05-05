package com.example.processmaker;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.SystemClock;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Space;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import com.squareup.picasso.Picasso;

public class Dynaforms extends AppCompatActivity {

    private String pro_uid;
    private String tas_uid;
    private String token;
    private LinearLayout myLayout;
    private JSONObject bodyForm;
    private JSONObject body;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dynaforms);
        Intent intent = getIntent();
        pro_uid = intent.getStringExtra("pro_uid");
        tas_uid = intent.getStringExtra("tas_uid");
        token = intent.getStringExtra("token");
        myLayout = (LinearLayout) findViewById(R.id.myLayout);
        TreatDynaforms treat = new TreatDynaforms();
        treat.execute();

    }


    public class TreatDynaforms extends AsyncTask<Void, Void, Boolean> {

        private String formName;
        private JSONArray jItems2;


        TreatDynaforms (){

        }

        @Override
        protected Boolean doInBackground(Void... params) {

            OkHttpClient client = new OkHttpClient();

            Request request = new Request.Builder()
                    .url("http://process.isiforge.tn/api/1.0/isi/project/"+pro_uid+"/dynaforms")
                    .get()
                    .addHeader("Authorization", "Bearer "+token)
                    .build();

            try{
                Response response = client.newCall(request).execute();
                if (response.isSuccessful()) {
                    String resp = response.body().string();
                    JSONArray array = (JSONArray) new JSONTokener(resp).nextValue();
                    JSONObject jObject = (JSONObject) array.getJSONObject(0);
                    String content = (String) jObject.get("dyn_content");
                    JSONObject jContent = new JSONObject(content);
                    JSONObject jItems1 = (JSONObject) ((JSONArray) jContent.get("items")).getJSONObject(0);
                    formName = (String) jItems1.get("name");
                    jItems2 = (JSONArray) jItems1.get("items");
                    return true;
                }else
                    return false;

            }catch(Exception e){

            }

            return true;
        }
        @Override
        protected void onPostExecute(final Boolean success) {
            bodyForm = new JSONObject();
            try {
                for (int i = 0; i<jItems2.length();i++){
                    JSONArray array = (JSONArray) jItems2.getJSONArray(i);
                    int n = array.length();
                    for (int j=0;j<n;j++){
                        JSONObject jObject = (JSONObject) array.getJSONObject(j);
                        treatView(jObject);
                        Space space = new Space(Dynaforms.this);
                        space.setMinimumHeight(20);
                        myLayout.addView(space);
                    }
                }
            }catch(Exception e){

            }
            Button button = new Button(Dynaforms.this);
            button.setText("Envoyer");
            button.setBackgroundColor(Color.rgb(121,134,203));
            button.setTextColor(Color.WHITE);
            submit(button);
            myLayout.addView(button);

        }
        @Override
        protected void onCancelled() {

        }

        void submit(Button button){
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Iterator<String> temp = bodyForm.keys();
                    while (temp.hasNext()) {
                        String key = temp.next();
                        try {
                            TextInputEditText edit = (TextInputEditText) myLayout.findViewWithTag(key);
                            bodyForm.put(key, edit.getText().toString());
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    try{
                        JSONArray variables = new JSONArray();
                        variables.put(bodyForm);
                        body = new JSONObject();
                        body.put("pro_uid",pro_uid);
                        body.put("tas_uid",tas_uid);
                        body.put("variables",variables);
                    }catch(Exception e){

                    }
                    Submit s = new Submit(body.toString());
                    s.execute();


                }

            });
        }

        void treatView(JSONObject jObject){

            String type = jObject.optString("type");
            if (type.equals("image")){
                String url = jObject.optString("src");
                ImageView imageView = new ImageView(Dynaforms.this);
                Picasso.with(Dynaforms.this).load(url).into(imageView);
                myLayout.addView(imageView);
            }
            else if(type.equals("title")){
                TextView textView = new TextView(Dynaforms.this);
                textView.setText(jObject.optString("label"));
                textView.setTextSize(30);
                textView.setTextColor(Color.BLACK);
                textView.setGravity(Gravity.CENTER_HORIZONTAL);
                textView.setPadding(0, 0, 0, 60);
                myLayout.addView(textView);
            }
            else if (type.equals("text")){
                TextView textView = new TextView(Dynaforms.this);
                textView.setText(jObject.optString("label"));
                TextInputEditText editText = new TextInputEditText(Dynaforms.this);

                try {
                    editText.setTag(jObject.getString("variable"));
                    bodyForm.put(jObject.getString("variable"), "");
                } catch (JSONException e) {

                }
                myLayout.addView(textView);
                myLayout.addView(editText);
            }
            else if(type.equals("dropdown")){
                Spinner spinner = new Spinner(Dynaforms.this);
                ArrayList<String> list = new ArrayList<String>();
                TextView textView = new TextView(Dynaforms.this);
                textView.setText(jObject.optString("label"));
                try{
                    JSONArray options = (JSONArray) jObject.getJSONArray("options");
                    for (int i = 0; i<options.length();i++){
                        JSONObject  opt = (JSONObject) options.getJSONObject(i);
                        String label = opt.optString("label");
                        list.add(label);
                    }
                }catch(Exception e){

                }

                ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(Dynaforms.this, android.R.layout.simple_spinner_item, list);
                spinner.setAdapter(arrayAdapter);
                myLayout.addView(textView);
                myLayout.addView(spinner);


            }

            else if(type.equals("radio")){
                RadioGroup radioGroup =new RadioGroup(Dynaforms.this);
                TextView textView = new TextView(Dynaforms.this);
                textView.setText(jObject.optString("label"));
                try{
                    JSONArray options = (JSONArray) jObject.getJSONArray("options");
                    for (int i = 0; i<options.length();i++){
                        JSONObject  opt = (JSONObject) options.getJSONObject(i);
                        String label = opt.optString("label");
                        RadioButton radioButton = new RadioButton(Dynaforms.this);
                        radioButton.setText(label);
                        radioGroup.addView(radioButton);
                    }
                }catch(Exception e){

                }
                myLayout.addView(textView);
                myLayout.addView(radioGroup);


            }

            else if(type.equals("textarea")){
                TextView textView = new TextView(Dynaforms.this);
                textView.setText(jObject.optString("label"));
                TextInputEditText editText = new TextInputEditText(Dynaforms.this);
                editText.setMinLines(4);
                try {
                    editText.setTag(jObject.getString("variable"));
                    bodyForm.put(jObject.getString("variable"), "");
                } catch (JSONException e) {

                }
                myLayout.addView(textView);
                myLayout.addView(editText);
            }
            else if (type.equals("datetime")){
                TextView textView = new TextView(Dynaforms.this);
                DatePicker datePicker = new DatePicker(Dynaforms.this);
                datePicker.setCalendarViewShown(false);
                datePicker.setSpinnersShown(true);
                textView.setText(jObject.optString("label"));
                myLayout.addView(textView);
                myLayout.addView(datePicker);
            }
            else if (type.equals("subtitle")){
                TextView textView = new TextView(Dynaforms.this);
                textView.setText(jObject.optString("label"));
                textView.setTextSize(18);
                textView.setTextColor(Color.BLUE);
                textView.setPadding(0, 0, 0, 40);
                myLayout.addView(textView);
            }
            else if (type.equals("file")){

            }

        }

    }

    public class Submit extends AsyncTask<Void,Void,Boolean>{

        private String bodyString;

        public Submit(String bodyString) {
            this.bodyString= bodyString;

        }
        @Override
        protected Boolean doInBackground(Void... voids) {
            OkHttpClient client = new OkHttpClient();

            MediaType mediaType = MediaType.parse("application/json");
            RequestBody body = RequestBody.create(mediaType, bodyString);
            Request request = new Request.Builder()
                    .url("http://isiforge.tn/api/1.0/isi/cases")
                    .post(body)
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Authorization", "Bearer "+token)
                    .build();
            try {
                Response response = client.newCall(request).execute();
            }catch (Exception e){

            }

            return true;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            Toast.makeText(getApplicationContext(),"Submitted successfully",Toast.LENGTH_LONG).show();
            onBackPressed();


        }

        @Override
        protected void onCancelled(Boolean aBoolean) {
            super.onCancelled(aBoolean);

        }
    }

}
