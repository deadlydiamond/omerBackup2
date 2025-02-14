package com.example.seekm.studemts;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import static android.widget.Toast.LENGTH_SHORT;
import static com.example.seekm.studemts.NetworkChangeReceiver.IS_NETWORK_AVAILABLE;

public class NearbyTutorsMapActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleApiClient.OnConnectionFailedListener, GoogleMap.OnInfoWindowClickListener {


    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
    }
    @Override
    public void onMapReady(GoogleMap googleMap) {
        // Toast.makeText(this, "Map is Ready", Toast.LENGTH_SHORT).show();

        Log.d(TAG, "onMapReady: map is ready");
        mMap = googleMap;

        try {
            // Customise the styling of the base map using a JSON object defined
            // in a raw resource file.
            boolean success = googleMap.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(
                            this, R.raw.maps));

            if (!success) {
                Log.e(TAG, "Style parsing failed.");
            }

        } catch (Resources.NotFoundException e) {
            Log.e(TAG, "Can't find style. Error: ", e);
        }
        if (mLocationPermissionsGranted) {
            getDeviceLocation();

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(false);
            mMap.getUiSettings().setMapToolbarEnabled(false);
            mMap.setOnInfoWindowClickListener(this);
        }
    }




    private static final String TAG = "MapActivity";

    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COURSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1234;
    private static final float DEFAULT_ZOOM = 16.5f;
    private static final float DEFAULT_RADIUS = 250;
    private static final LatLngBounds LAT_LNG_BOUNDS = new LatLngBounds(
            new LatLng(-40, -168), new LatLng(71, 136));

    /*__________________________________________________WIDGETS_________________________________________________________*/

    private ImageButton mNext, mGps;

    /*__________________________________________________VARIABLES_________________________________________________________*/

    SharedPreferences Profile_preferences;
    FirebaseAuth mAuth;
    Marker marker;
    int count;
    boolean load = false;
    Circle circle;
    String FirstName, LastName, Qualification, Email, Board, Gender,Dob, myEmail,UID;
    private double latitude, longitude;
    private double latitudeDevice, longitudeDevice;
    private Boolean mLocationPermissionsGranted = false;
    private GoogleMap mMap;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private GoogleApiClient mGoogleApiClient;
    SeekBar seekbar;
    Float radius = DEFAULT_RADIUS, zoom = DEFAULT_ZOOM;

    @SuppressLint("ResourceType")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nearby_tutors_maps);

        statusCheck();

        if (isConnected()) {
            //Snackbar.make(findViewById(R.id.nearbyTutors),"Internet Connected" , Snackbar.LENGTH_INDEFINITE).show();
            //Toast.makeText(getApplicationContext(), "Internet Connected", Toast.LENGTH_SHORT).show();
        } else {
            Snackbar.make(findViewById(R.id.nearbyTutors),"No Internet Connection" , Snackbar.LENGTH_INDEFINITE).show();
            //Toast.makeText(getApplicationContext(), "No Internet Connection", Toast.LENGTH_SHORT).show();
        }

        IntentFilter intentFilter = new IntentFilter(NetworkChangeReceiver.NETWORK_AVAILABLE_ACTION);
        LocalBroadcastManager.getInstance(this).registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                boolean isNetworkAvailable = intent.getBooleanExtra(IS_NETWORK_AVAILABLE, false);
                String networkStatus = isNetworkAvailable ? "connected" : "disconnected";
                if (networkStatus=="disconnected"){
                    Snackbar.make(findViewById(R.id.nearbyTutors),"No Internet Connection " , Snackbar.LENGTH_INDEFINITE).show();
                }else{
                    Snackbar.make(findViewById(R.id.nearbyTutors),"Internet Connected" , Snackbar.LENGTH_LONG).show();
                }
            }
        }, intentFilter);



        //statusCheck();
        getDeviceLocation();

        mGps = (ImageButton) findViewById(R.id.ic_gps);
        mNext = (ImageButton) findViewById(R.id.next);
        seekbar = (SeekBar) findViewById(R.id.seekBar);
        getLocationPermission();
        Profile_preferences = getApplicationContext().getSharedPreferences("Profile_Preferecens", 0);
        mAuth = FirebaseAuth.getInstance();
    }

    public boolean isConnected() {
        boolean connected = false;
        try {
            ConnectivityManager cm = (ConnectivityManager)getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo nInfo = cm.getActiveNetworkInfo();
            connected = nInfo != null && nInfo.isAvailable() && nInfo.isConnected();
            return connected;
        } catch (Exception e) {
            Log.e("Connectivity Exception", e.getMessage());
        }
        return connected;
    }

    public class SeekBarHint extends SeekBar {
        public SeekBarHint (Context context) {
            super(context);
        }

        public SeekBarHint (Context context, AttributeSet attrs, int defStyle) {
            super(context, attrs, defStyle);
        }

        public SeekBarHint (Context context, AttributeSet attrs) {
            super(context, attrs);
        }
    }



    /*__________________________________________________CUSTOM LOCATION POPUP_________________________________________________________*/

//    public boolean statusCheck() {
//        final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
//        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
//            buildAlertMessageNoGps();
//            getDeviceLocation();
//            return true;
//        }
//        return false;
//    }
    public void statusCheck() {
        final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            buildAlertMessageNoGps();

        }
        else{
            retrieveData();
        }
    }

    private void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.activity_custom_location, null);
        builder.setView(dialogView);
        builder
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        load = true;
                        dialog.cancel();
                        dialog.dismiss();
                        startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                        getDeviceLocation();
                    }
                })
                .setNegativeButton("NO THANKS   ", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        //dialog.dismiss();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
        getDeviceLocation();
    }


    public void buildAlertMessageInfo() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.activity_custom_info, null);
        builder.setView(dialogView);
        builder
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        dialog.cancel();
                        dialog.dismiss();
                        Intent intent = new Intent(NearbyTutorsMapActivity.this,TutorsProfile.class)
                                .putExtra("uid", UID);
                        startActivity(intent);
                        //getDeviceLocation();
                    }
                })
                .setNegativeButton("NO THANKS   ", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        //dialog.dismiss();

                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }


    /*__________________________________________________INIT_________________________________________________________*/

    private void init() {

        Log.d(TAG, "init: initializing");
        mGoogleApiClient = new GoogleApiClient
                .Builder(this)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .enableAutoManage(this, this)
                .build();
        /*__________________________________________________NEXT BUTTON_________________________________________________________*/
        mNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(NearbyTutorsMapActivity.this, Drawer.class);
                startActivity(intent);
            }
        });

        /*__________________________________________________GPS BUTTON_________________________________________________________*/
        mGps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try{
                    circle.remove();
                }catch (NullPointerException e){
                    Log.d(TAG, "onClick: ");
                }
                statusCheck();
                radius = DEFAULT_RADIUS;
                zoom = DEFAULT_ZOOM;
                seekbar.setProgress(0);

                //if (statusCheck() == true) {
                    getDeviceLocation();
                    circle = mMap.addCircle(new CircleOptions()
                            .center(new LatLng(latitudeDevice, longitudeDevice))
                            .radius(DEFAULT_RADIUS)
                            .strokeWidth(5)
                            .strokeColor(Color.rgb(7, 160, 225))
                            .fillColor(0x22FFFFFF));
                //}
                Log.d(TAG, "onClick:  Clicked gps icon");
                if (mLocationPermissionsGranted) {
                    getDeviceLocation();
                } else {
                    try {
                        Log.d(TAG, "getLocationPermission: getting location permissions");
                        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION};

                        if (ContextCompat.checkSelfPermission(NearbyTutorsMapActivity.this,
                                FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                            if (ContextCompat.checkSelfPermission(NearbyTutorsMapActivity.this,
                                    COURSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                                mLocationPermissionsGranted = true;
                                initMap();
                            } else {
                                ActivityCompat.requestPermissions(NearbyTutorsMapActivity.this,
                                        permissions,
                                        LOCATION_PERMISSION_REQUEST_CODE);
                            }
                        } else {
                            ActivityCompat.requestPermissions(NearbyTutorsMapActivity.this,
                                    permissions,
                                    LOCATION_PERMISSION_REQUEST_CODE);
                        }

                    } catch (Error e) {
                        Log.d(TAG, "onClick: Error on Click" + e.getMessage());
                    }
                }
            }
        });

        /*__________________________________________________SEEKBAR_________________________________________________________*/

        seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (circle!=null){
                    circle.remove();
                }
                float factor = 0.15f;
                if (radius >= DEFAULT_RADIUS) {
                    updateCircle(radius * progress);
                    updateZoom((float) (zoom - (progress * factor)));
                    factor = factor * 0.002f;
                    //updateZoom(zoom * progress)
                    // ;
                } else {
                    updateCircle(radius / progress);
                    updateZoom((float) (zoom * 0.5));
                    //updateZoom(zoom * progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    /*__________________________________________________SEEKBAR ZOOM_________________________________________________________*/

    private void updateZoom(float zoom) {
        if (zoom > DEFAULT_ZOOM) {
            mMap.animateCamera(CameraUpdateFactory.zoomTo(zoom), 2000, null);
            moveCameraToUserLocation(new LatLng(latitudeDevice, longitudeDevice),
                    zoom);

        } else {
            mMap.animateCamera(CameraUpdateFactory.zoomTo(zoom), 2000, null);
            moveCameraToUserLocation(new LatLng(latitudeDevice, longitudeDevice),
                    zoom);
        }
    }

    /*__________________________________________________GET DEVICE LOCATION_________________________________________________________*/

    private void getDeviceLocation() {
        Log.d(TAG, "getDeviceLocation: getting the devices current location");
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        try {
            if (mLocationPermissionsGranted) {
                final Task location = mFusedLocationProviderClient.getLastLocation();
                location.addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        try {
                            if (task.isSuccessful()) {
                                Log.d(TAG, "onComplete: found location!");
                                Location currentLocation = (Location) task.getResult();
                                latitudeDevice = currentLocation.getLatitude();
                                longitudeDevice = currentLocation.getLongitude();
                                moveCameraToUserLocation(new LatLng(latitudeDevice, longitudeDevice),
                                        DEFAULT_ZOOM+2);

                            } else {
                                Log.d(TAG, "onComplete: current location is null");
                                Toast.makeText(NearbyTutorsMapActivity.this, "unable to get current location", LENGTH_SHORT).show();
                            }
                        } catch (NullPointerException e) {
                            Log.d(TAG, "onComplete: Nullpointer" + e.getMessage());
                        }
                    }
                });
            }

        } catch (SecurityException e) {
            Log.e(TAG, "getDeviceLocation: SecurityException: " + e.getMessage());
        }
    }

    /*__________________________________________________MOVING CAMERA TO TARGET LOCATIONS_____________________________________________________*/

    private void moveCameraToTargetLocation(LatLng latLng, float defaultZoom) {
        marker = mMap.addMarker(new MarkerOptions()
                .position(latLng)
                .title(UID)
                .snippet("Name: " + FirstName + " "+ LastName + "\n" + " Gender: " + Gender + "\n" + " Qualification: " + Qualification)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.iconhat)));
        mMap.setOnInfoWindowClickListener(this);
        mMap.setInfoWindowAdapter(new CustomInfoWindowAdapter(NearbyTutorsMapActivity.this));

    }

    /*__________________________________________________UPDATE BLUE CIRCLE_________________________________________________________*/

    private void updateCircle(float r) {
        if (r >= DEFAULT_RADIUS) {
            circle = mMap.addCircle(new CircleOptions()
                    .center(new LatLng(latitudeDevice, longitudeDevice))
                    .radius(r)
                    .strokeWidth(5)
                    .strokeColor(Color.rgb(7, 160, 225))
                    .fillColor(0x22FFFFFF));
            //Toast.makeText(NearbyTutorsMapActivity.this, "radius: " + r, Toast.LENGTH_LONG).show();
        }
    }

    /*__________________________________________________MOVE CAMERA TO USER's LOCATION_________________________________________________________*/

    private void moveCameraToUserLocation(LatLng latLng, float zoom) {
        Log.d(TAG, "moveCamera: moving the camera to: lat: " + latLng.latitude + ", lng: " + latLng.longitude);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
        if (circle == null) {
            circle = mMap.addCircle(new CircleOptions()
                    .center(new LatLng(latitude, longitude))
                    .radius(250)
                    .strokeWidth(5)
                    .strokeColor(Color.rgb(7, 160, 225))
                    .fillColor(0x220000FF));
        }


    }

    /*__________________________________________________INITIALIZING MAP_________________________________________________________*/

    private void initMap() {
        //reading data from FIREBASE

        getDeviceLocation();
        Log.d(TAG, "initMap: initializing map");
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(NearbyTutorsMapActivity.this);
    }

    /*__________________________________________________LOADING DATA FROM FIREBASE_________________________________________________________*/

    private void retrieveData() {
        final CShowProgress cShowProgress = CShowProgress.getInstance();
        cShowProgress.showProgress(NearbyTutorsMapActivity.this);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("Tutors")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d(TAG, document.getId() + " => " + document.get("Latitude"));
                                try {
                                    UID = document.getId();
                                    FirstName = document.get("FirstName").toString();
                                    LastName = document.get("LastName").toString();
                                    Qualification = document.get("LatestQualification").toString();
                                    Board = document.get("EducationBoard").toString();
                                    Email = document.get("EmailAddress").toString();
                                    Gender = document.get("Gender").toString();
                                    Dob = document.get("DateOfBirth").toString();

                                    latitude = Double.parseDouble(document.get("Latitude").toString());
                                    longitude = Double.parseDouble(document.get("Longitiude").toString());
                                    //toastME("Coordinates: " + longitude+latitude);
                                    moveCameraToTargetLocation(new LatLng(latitude, longitude), DEFAULT_ZOOM);
                                    cShowProgress.hideProgress();
                                } catch (NullPointerException e) {
                                    Log.d(TAG, "onComplete: Exception" + e.getMessage());
                                }
                            }
                        } else {
                            Log.w(TAG, "Error getting documents.", task.getException());
                        }
                    }
                });
    }

    /*__________________________________________________GET LOCATION PERMISSION_________________________________________________________*/

    private void getLocationPermission() {
        Log.d(TAG, "getLocationPermission: getting location permissions");
        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION};

        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                    COURSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                mLocationPermissionsGranted = true;
                initMap();
            } else {
                ActivityCompat.requestPermissions(this,
                        permissions,
                        LOCATION_PERMISSION_REQUEST_CODE);
            }
        } else {
            ActivityCompat.requestPermissions(this,
                    permissions,
                    LOCATION_PERMISSION_REQUEST_CODE);
        }
        initMap();
        init();
    }

    /*__________________________________________________ON REQUEST PERMISSION GRANTED_________________________________________________________*/

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.d(TAG, "onRequestPermissionsResult: called.");
        mLocationPermissionsGranted = false;

        switch (requestCode) {
            case LOCATION_PERMISSION_REQUEST_CODE: {
                if (grantResults.length > 0) {
                    for (int i = 0; i < grantResults.length; i++) {
                        if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                            mLocationPermissionsGranted = false;
                            Log.d(TAG, "onRequestPermissionsResult: permission failed");
                            return;
                        }
                    }
                    Log.d(TAG, "onRequestPermissionsResult: permission granted");
                    mLocationPermissionsGranted = true;
                    //initialize our map
                    initMap();
                }
            }
        }
    }

    /*__________________________________________________MARKER's INFO WINDOW_________________________________________________________*/

    @Override
    public void onInfoWindowClick(Marker marker)
    {
        Toast.makeText(NearbyTutorsMapActivity.this,marker.getTitle(),Toast.LENGTH_LONG).show();
        UID = marker.getTitle();
        buildAlertMessageInfo();

    }


    /*__________________________________________________HIDING SOFT KEYBOARD_________________________________________________________*/

    private void hideSoftKeyboard() {
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.
                SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    /*__________________________________________________TOAST_________________________________________________________*/

    private void toastME(String msg){
        Toast.makeText(NearbyTutorsMapActivity.this,msg,Toast.LENGTH_LONG).show();
    }
}

