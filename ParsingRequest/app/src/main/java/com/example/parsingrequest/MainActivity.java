package com.example.parsingrequest;

import android.content.Intent;
import android.content.IntentFilter;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.phone.SmsRetriever;
import com.google.android.gms.auth.api.phone.SmsRetrieverClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

public class MainActivity extends AppCompatActivity implements MySMSBroadcastReceiver.REQReceiveListener {

    private TextView t_type;
    private TextView t_scale;
    private TextView t_latitude;
    private TextView t_longitude;
    private TextView t_vehicleid;
    private TextView t_hospitalid;
    private TextView t_requester;

    private String email = "sdb@gmail.com";
    private String Pass = "12345678";

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;


    public static final String TAG = MainActivity.class.getSimpleName();
    private MySMSBroadcastReceiver smsbroadcast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        FirebaseApp.initializeApp(this);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        Signin();


        t_type = findViewById(R.id.type);
        t_scale = findViewById(R.id.scale);
        t_latitude = findViewById(R.id.latitude);
        t_longitude = findViewById(R.id.longitude);
        t_vehicleid = findViewById(R.id.vehicleid);
        t_hospitalid = findViewById(R.id.hospitalid);
        t_requester = findViewById(R.id.user);
        //AppSignatureHashHelper appSignatureHashHelper = new AppSignatureHashHelper(this);

        // This code requires one time to get Hash keys do comment and share key
        //Log.d(TAG, "Apps Hash Key: " + appSignatureHashHelper.getAppSignatures().get(0));

        startsmslistner();


    }

    public void startsmslistner()
    {
        try {
            smsbroadcast = new MySMSBroadcastReceiver();
            smsbroadcast.setREQListener(this);

            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(SmsRetriever.SMS_RETRIEVED_ACTION);
            this.registerReceiver(smsbroadcast, intentFilter);

            SmsRetrieverClient client = SmsRetriever.getClient(this);

            Task<Void> task = client.startSmsRetriever();
            task.addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    // API successfully started
                    showToast("Listening");
                }
            });

            task.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    // Fail to start API
                    showToast("Not Listening");
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void Parserequest(String req)
    {
        String data[] = req.split("\\n");



        String type = data[1];
        String scale = data[2];
        String latitude = data[3];
        String longitude = data[4];
        String vehicleid = data[5];
        String hospitalid = data[6];
        String requester = data[7];

        t_type.setText(type);
        t_scale.setText(scale);
        t_latitude.setText(latitude);
        t_longitude.setText(longitude);
        t_vehicleid.setText(vehicleid);
        t_hospitalid.setText(hospitalid);
        t_requester.setText(requester);

        CollectionReference dbreq = db.collection("Requests");

        GeoPoint location = new GeoPoint(Double.parseDouble(latitude), Double.parseDouble(longitude));
        //  Toast.makeText(RequestDetails.this, "reference to database",Toast.LENGTH_SHORT).show();
        Request request = new Request(
                requester,
                type,
                Integer.parseInt(scale),
                location,
                vehicleid,
                hospitalid
        );

        //Toast.makeText(RequestDetails.this, "",Toast.LENGTH_SHORT).show();

        dbreq.add(request)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Toast.makeText(getApplicationContext(), "Request Stored",Toast.LENGTH_LONG).show();
                        // hideDialog();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getApplicationContext(), e.getMessage(),Toast.LENGTH_LONG).show();
                //hideDialog();
            }
        });



    }

    private void Signin() {

       // showDialog();
        mAuth.signInWithEmailAndPassword(email, Pass)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            //hideDialog();
                            Toast.makeText(getApplicationContext(), "Signed in as " + email, Toast.LENGTH_LONG).show();

                        } else {
                            Toast.makeText(getApplicationContext(), "Authentication Failed!", Toast.LENGTH_LONG).show();
                            //hideDialog();
                        }

                    }
                });
    }


    @Override
    public void onREQReceived(String req) {
        showToast("recieved");

        Parserequest(req);
        //t_type.setText(req);

        if (smsbroadcast != null) {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(smsbroadcast);
        }
    }

    @Override
    public void onREQTimeOut() {
        showToast("REQ Time out");

    }

    @Override
    public void onREQReceivedError(String error) {
        showToast(error);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (smsbroadcast != null) {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(smsbroadcast);
        }
    }

    @Override
    protected void onResume(){
        super.onResume();
        db.collection("Requests").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()){
                    for(QueryDocumentSnapshot document : task.getResult()) {
                        showToast(document.getData().toString());
                    }
                } else {
                    showToast("Error getting documents");
                }
            }
        });
    }


    private void showToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

}
