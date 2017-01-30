package com.ecocarunicamp.ecoapp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.concurrent.TimeUnit;


public class MainActivity extends Activity implements ConnectionCallbacks, OnConnectionFailedListener, LocationListener {

    // Some definitions
    private static final String TAG = MainActivity.class.getSimpleName();
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 1000;
    private static double MASS = 100.0;
    private static double MAX_VEL = 80.0;
    private static double DANGER_VEL = 50.0;

    private Location mLastLocation;

    // Google client to interact with Google API
    private GoogleApiClient mGoogleApiClient;

    // boolean flag to toggle periodic location updates
    private boolean mRequestingLocationUpdates = false;
    private LocationRequest mLocationRequest;

    // Location updates intervals in sec
    private static int UPDATE_INTERVAL = 250;
    private static int FATEST_INTERVAL = 100;
    //private static int DISPLACEMENT = 1;

    // UI elements
    private FrameLayout mainScreen;
    private TextView latitudeView;
    private TextView longitudeView;
    private TextView speedView;
    private TextView averageSpeedView;
    private TextView timeView;
    private TextView energyView;
    private TextView powerView;
    private Button startButton;
    private ImageView speedometerImage;
    //private Button tableButton;
    private TextView runView;

    //Angle sensor
    private CarAngleSensor carAngleSensor;


    //Control variables
    private long previousTime = 0;
    private double previousLatitude = 0;
    private double previousLongitude = 0;
    private double currentLatitude = 0;
    private double currentLongitude = 0;
    private long currentTime=0;
    private double previousSpeed = 0;
    private double currentSpeed = 0;
    private long startingTime;
    private double previousPower = 0;
    private double totalEnergy = 0;
    private double power = 0;
    private Information[] informationArray;
    private int run = 0;
    SimpleDateFormat dateFormat;

    private MovingAveregeBuffer xFilter;
    private MovingAveregeBuffer yFilter;
    private MovingAveregeBuffer speedFilter;

    private double averageSpeed = 0;
    private int numSamples = 0;
    private boolean isEnable = false;
    private boolean firstTime = true;

    private String[] mPlanetTitles;
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //cant sleep
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        //Brightness 100%
//        WindowManager.LayoutParams layout = getWindow().getAttributes();
//        layout.screenBrightness = 1F;
//        getWindow().setAttributes(layout);



        latitudeView = (TextView) findViewById(R.id.txtLatitude);
        longitudeView = (TextView) findViewById(R.id.txtLongitude);
        speedView = (TextView) findViewById(R.id.txtVelocidade);
        averageSpeedView = (TextView) findViewById(R.id.txtVelocidadeGps);
        timeView = (TextView) findViewById(R.id.txtTempo);
        energyView = (TextView) findViewById(R.id.txtEnergia);
        powerView = (TextView) findViewById(R.id.txtPotencia);
        mainScreen = (FrameLayout) findViewById(R.id.mainScreen);
        startButton = (Button) findViewById(R.id.buttonStart);
        speedometerImage = (ImageView) findViewById(R.id.speedometerImage);
        //tableButton = (Button) findViewById(R.id.buttonTable);
        runView = (TextView) findViewById(R.id.txtRunNumber);



        //invert screen
        //mainScreen.setScaleX(-1);


        //startingTime = System.currentTimeMillis();
        dateFormat = new SimpleDateFormat("mm:ss");

        if (checkPlayServices()) {

            buildGoogleApiClient();

            createLocationRequest();
        }

        startButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                if (!isEnable){

                    isEnable = true;
                    startButton.setBackgroundResource(R.drawable.startflagyellow);
                    Toast toast = Toast.makeText(getApplicationContext(), "Captura ON!", Toast.LENGTH_SHORT);
                    toast.show();
                }
                else {
                    isEnable = false;
                    startButton.setBackgroundResource(R.drawable.startflaggreen);
                    Toast toast = Toast.makeText(getApplicationContext(), "Captura OFF!", Toast.LENGTH_SHORT);
                    toast.show();
                    firstTime = true;
                }
            }
        });

//        tableButton.setOnClickListener(new View.OnClickListener() {
//            public void onClick(View v) {
//                Intent intentMain = new Intent(MainActivity.this ,
//                        TableActivity.class);
//                MainActivity.this.startActivity(intentMain);
//
//            }
//        });




        this.carAngleSensor = new CarAngleSensor((SensorManager) getSystemService(Context.SENSOR_SERVICE),(TextView)findViewById(R.id.txtAngle), 10);
        xFilter = new MovingAveregeBuffer(15);
        yFilter = new MovingAveregeBuffer(15);
        speedFilter = new MovingAveregeBuffer(5);


        SendMySql pegaRun = new SendMySql(){
            @Override
            public void naResposta(ResultSet result) throws SQLException{
                result.first();
                run = result.getInt("run");
                //incrementa run
                run++;
                runView.setText(String.format("Run %d", run));

            }


        };


        pegaRun.execute("SELECT * FROM `dados` WHERE `run` = (SELECT MAX(`run`) FROM `dados`)");


    }



    //creating google play client
    private void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).build();
    }

    //Verify if google play services is available
    private boolean checkPlayServices() {

        GoogleApiAvailability googleAPI = GoogleApiAvailability.getInstance();
        int resultCode = googleAPI.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (googleAPI.isUserResolvableError(resultCode)) {
                googleAPI.getErrorDialog(this, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Toast.makeText(getApplicationContext(),
                        "This device is not supported.", Toast.LENGTH_LONG)
                        .show();
                finish();
            }
            return false;
        }
        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }

        this.carAngleSensor.start();
    }

    @Override
    protected void onResume() {
        super.onResume();

        checkPlayServices();

        if (mGoogleApiClient.isConnected()) {
            startLocationUpdates();
        }

        this.carAngleSensor.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopLocationUpdates();

        this.carAngleSensor.pause();
    }



    @Override
    public void onConnected(Bundle bundle) {
        // Once connected with google api, get the location
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }

        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (mLastLocation != null) {

            displayLocation(mLastLocation.getLatitude(), mLastLocation.getLongitude());
            //set initial time
            previousTime = System.currentTimeMillis();

            previousLatitude = mLastLocation.getLatitude();
            previousLongitude = mLastLocation.getLongitude();
        }
        else {
            latitudeView.setText("Location not available");
            longitudeView.setText("Location not available");
        }

        startLocationUpdates();
    }

    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this);
        Log.d(TAG, "Periodic location updates started!");
    }

    protected void stopLocationUpdates() {

        LocationServices.FusedLocationApi.removeLocationUpdates(
                mGoogleApiClient, this);
    }

    private void displayLocation(double latitude, double longitude) {

        latitudeView.setText(String.valueOf(latitude));
        longitudeView.setText(String.valueOf(longitude));

    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = "
                + result.getErrorCode());
    }


//    /**
//     * Gets distance in meters, coordinates in RADIAN
//     */
//    private static double getDistance(double lat1, double lon1, double lat2, double lon2) {
//        double R = 6371000; // for haversine use R = 6372.8 km instead of 6371 km
//        double dLat = lat2 - lat1;
//        double dLon = lon2 - lon1;
//        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
//                Math.cos(lat1) * Math.cos(lat2) *
//                        Math.sin(dLon / 2) * Math.sin(dLon / 2);
//        //double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
//        return 2 * R * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
//        // simplify haversine:
//        //return 2 * R * 1000 * Math.asin(Math.sqrt(a));
//    }



    @Override
    public void onLocationChanged(Location location) {

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if(mLastLocation!=null){
            float latitude = xFilter.add((float)mLastLocation.getLatitude());
            float longitude = yFilter.add((float)mLastLocation.getLongitude());
            //double distancia = getDistance(latitude,longitude ,previousLatitude,previousLongitude);
            //long time = System.currentTimeMillis();
//            long intervalo_tempo =  time - previousTime;
//            currentSpeed = (3600*distancia)/(intervalo_tempo);
//            if(currentSpeed>99){
//                currentSpeed=0;
//            }

            currentSpeed = mLastLocation.getSpeed();
            currentSpeed = speedFilter.add((float)currentSpeed);
            //speedView.setText(Double.toString(currentSpeed));

            speedView.setText(String.format("%d",(int)(3.6*(currentSpeed))));
            //calculatedSpeedView.setText(Double.toString(distancia));


            if(currentSpeed < DANGER_VEL/3.6){
                speedometerImage.setBackgroundResource(R.drawable.speedgreen);
            }
            else if(currentSpeed < MAX_VEL/3.6){
                speedometerImage.setBackgroundResource(R.drawable.speedyellow);

            }
            else{
                speedometerImage.setBackgroundResource(R.drawable.speedred);

            }


            previousLatitude = latitude;
            previousLongitude = longitude;
            displayLocation(previousLatitude,previousLongitude);

//            timeView.setText(String.format("%02d:%02d", (((time-startingTime)/60000)),TimeUnit.MILLISECONDS.toSeconds(time-startingTime)%60));
//            previousTime = time;


            if (isEnable){

                if(firstTime){
                    startingTime = System.currentTimeMillis();
                    previousPower = MASS*currentSpeed*carAngleSensor.getAceleration();
                    long time = System.currentTimeMillis();
                    previousTime = time;

                    firstTime = false;
                }
                else{
                    long time = System.currentTimeMillis();

                    timeView.setText(String.format("%02d:%02d", (((time-startingTime)/60000)),TimeUnit.MILLISECONDS.toSeconds(time-startingTime)%60));



                    numSamples++;
                    averageSpeed += currentSpeed/numSamples;
                    averageSpeedView.setText(String.format("%.2f km/h",(averageSpeed)));

                    //power = energy/(1000*(time-startingTime));
                    power = MASS*currentSpeed*carAngleSensor.getAceleration();
                    powerView.setText(String.format("%.2f W", power));

                    double energy = (time - previousTime)*((previousPower + power)/2);
                    energyView.setText(String.format("%.2f J",energy));
                    totalEnergy+=energy;

                    previousPower = power;
                    previousTime = time;


                    //enviando dados para o servidor
                    sendDataSQL(run,(time-startingTime), currentSpeed, carAngleSensor.getInclination(), energy, totalEnergy,
                            power, latitude, longitude);

                }
            }
        }
    }

    //create location Request
    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FATEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        //mLocationRequest.setSmallestDisplacement(DISPLACEMENT);
    }

//    public double calculateDistanceFromCoordinates (double lat1, double lon1, double lat2, double lon2) {
//
//        // Convert degrees to radians
//        lat1 = lat1 * Math.PI / 180.0;
//        lon1 = lon1 * Math.PI / 180.0;
//
//        lat2 = lat2 * Math.PI / 180.0;
//        lon2 = lon2 * Math.PI / 180.0;
//
//        // radius of earth in metres
//        double r = 6378100;
//
//        // P
//        double rho1 = r * Math.cos(lat1);
//        double z1 = r * Math.sin(lat1);
//        double x1 = rho1 * Math.cos(lon1);
//        double y1 = rho1 * Math.sin(lon1);
//
//        // Q
//        double rho2 = r * Math.cos(lat2);
//        double z2 = r * Math.sin(lat2);
//        double x2 = rho2 * Math.cos(lon2);
//        double y2 = rho2 * Math.sin(lon2);
//
//        // Dot product
//        double dot = (x1 * x2 + y1 * y2 + z1 * z2);
//        double cos_theta = dot / (r * r);
//
//        double theta = Math.acos(cos_theta);
//
//        // Distnce in Metres
//        return r * theta;
//    }

    public void sendDataSQL(int run, long time, double speed, double angle, double currentEnergy, double totalEnergy,
                            double power, double latitude, double longitude){
        String sqlCommand = "INSERT INTO dados (`id`,`run`,`tempoms`,`velocidade`,`inclinacao`, `energia`, `energiaTotal`, `potencia`, `gpsx`, `gpsy` ) VALUES ('" +
                String.valueOf(0)+
                "','" +
                String.valueOf(run)+
                "','" +
                String.valueOf(time)+
                "','" +
                String.valueOf(speed)+
                "','" +
                String.valueOf(angle)+
                "','" +
                String.valueOf(currentEnergy)+
                "','" +
                String.valueOf(totalEnergy)+
                "','" +
                String.valueOf(power)+
                "','" +
                String.valueOf(latitude)+
                "','" +
                String.valueOf(longitude)+
                "')";

        //Sending to database
        UpdateMySql job = new UpdateMySql() {
            public void naResposta(Integer result) throws SQLException {

                if (result > 0) {
                    System.out.println("Enviado com sucesso!");
//                    Toast.makeText(getApplicationContext(), "Enviado com sucesso!",
//                            Toast.LENGTH_SHORT).show();

                } else {
                    System.out.println("Problema no envio!");
                    Toast.makeText(getApplicationContext(), "Problema no envio!",
                            Toast.LENGTH_SHORT).show();
                }
            }
        };

        job.execute(sqlCommand);
        //System.out.println(sqlCommand);
        System.out.println("Enviando SQL ao servidor");

//        Toast.makeText(getApplicationContext(), "Enviando SQL ao servidor!",
//                Toast.LENGTH_SHORT).show();


    }
}

