package com.example.processmaker;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.example.processmaker.model.Workflow;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.util.ArrayList;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity implements
        NavigationView.OnNavigationItemSelectedListener,
        WorkflowFragment.OnListFragmentInteractionListener,
        DemandeFragment.OnListFragmentInteractionListener{

    private String token;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Process Maker");
        //Récupération du token
        Intent intent = getIntent();
        token = intent.getStringExtra("token");
        String email =intent.getStringExtra("email");

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        getSupportActionBar().setTitle("Mes services");
        TreatRequest treat = new TreatRequest(1,"http://isiforge.tn/api/1.0/isi/case/start-cases");
        treat.execute();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            //super.onBackPressed();
        }
        //getSupportActionBar().setTitle("Process Maker");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.services) {

            getSupportActionBar().setTitle("Mes services");
            TreatRequest treat = new TreatRequest(1,"http://isiforge.tn/api/1.0/isi/case/start-cases");
            treat.execute();


        } else if (id == R.id.historique) {
            getSupportActionBar().setTitle("Historique de demandes");
            TreatRequest treat = new TreatRequest(2,"http://isiforge.tn/api/1.0/isi/cases/participated");
            treat.execute();

        } else if (id == R.id.logout) {
            token ="";
            Intent intent = new Intent(MainActivity.this, LoginActivity.class) ;
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            getApplicationContext().startActivity(intent);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    @Override
    public void onListFragmentInteraction(Workflow workflow) {
        //Toast.makeText(getApplicationContext(),workflow.getId(),Toast.LENGTH_LONG).show();
        //intent
        Intent intent = new Intent(MainActivity.this, Dynaforms.class) ;
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("pro_uid", workflow.getPro_uid());
        intent.putExtra("tas_uid", workflow.getTas_uid());
        intent.putExtra("token", token);
        getApplicationContext().startActivity(intent);
    }
    @Override
    public void onListFragmentInteraction(String item){

    }

    public class TreatRequest extends AsyncTask<Void, Void, Boolean> {

        private int cas ;
        private String url;
        private JSONArray array;

        TreatRequest(int cas , String url){
            this.cas = cas;
            this.url =url;
            this.array=null;
        }

        @Override
        protected Boolean doInBackground(Void... params) {

            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(url)
                    .get()
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Authorization", "Bearer "+token)
                    .build();
            try {
                Response response = client.newCall(request).execute();
                if (response.isSuccessful()) {
                    String resp = response.body().string();
                    array = (JSONArray) new JSONTokener(resp).nextValue();
                    return true;
                }else
                    return false;


            } catch (Exception e) {
                e.printStackTrace();
            }
            return false;

        }


        @Override
        protected void onPostExecute(final Boolean success) {

            switch (cas){
                case 1:
                    ArrayList<Workflow> wkfList = new ArrayList<Workflow>();
                    Workflow wkf;
                    try{
                        for(int i=0 ; i< array.length();i++){
                            JSONObject jObject = array.getJSONObject(i);
                            wkf = new Workflow(jObject.getString("pro_uid"),jObject.getString("tas_uid"), jObject.getString("pro_title"),i+1);
                            wkfList.add(wkf);
                        }
                    }catch(Exception e){

                    }
                    FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                    android.support.v4.app.Fragment pl = new WorkflowFragment(wkfList) ;
                    transaction.replace(R.id.fragment_holder, pl);
                    transaction.addToBackStack(null);
                    transaction.commit();

                    break;

                case 2:
                    ArrayList<String> listDemandes = new ArrayList<String>();
                    try{
                        for(int i=0 ; i< array.length();i++){
                            JSONObject jObject = array.getJSONObject(i);
                            listDemandes.add(jObject.getString("app_tas_title"));
                        }
                    }catch(Exception e) {

                    }
                    FragmentTransaction transaction1 = getSupportFragmentManager().beginTransaction();
                    android.support.v4.app.Fragment pl1 = new DemandeFragment(listDemandes) ;
                    transaction1.replace(R.id.fragment_holder, pl1);
                    transaction1.addToBackStack(null);
                    transaction1.commit();

                    break;
                default:
                    break;
            }


        }

        @Override
        protected void onCancelled() {}

    }



}
