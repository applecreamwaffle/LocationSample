package kr.acw.demo.locationsample;

import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.Api;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;


public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    //private FusedLocationProviderClient mFusedLocationClient;
    private static final String TAG = "MainActivity";

    private Location location;
    private TextView locationTv;
    private GoogleApiClient googleApiClient;
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private LocationRequest locationRequest;
    private static final long UPDATE_INTERVAL = 5000;
    private static final long FASTEST_INTERVAL = 5000;
    private ArrayList<String> permissionsToRequest;
    private ArrayList<String> permissionsToRejected = new ArrayList<>();
    private ArrayList<String> permissions = new ArrayList<>();
    private static final int ALL_PERMISSIONS_RESULT = 1011;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        locationTv = findViewById(R.id.location);

        permissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
        permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION);

        permissionsToRequest = permissionsToRequests(permissions);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (permissionsToRequest.size() > 0) {
                requestPermissions(permissionsToRequest.toArray(
                        new String[permissionsToRequest.size()]), ALL_PERMISSIONS_RESULT);
            }
        }

        googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        /*
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mFusedLocationClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                // Got last known location. In some rare situations this can be null.
                if (location != null) {
                    // Logic to handle location object
                    Log.d("LocationTest", "Location: " + location.getLongitude() + ", " + location.getLatitude());
                }
            }
        });
        */

    }

    private ArrayList<String> permissionsToRequests(ArrayList<String> wantedPermissions) {
        ArrayList<String> result = new ArrayList<>();

        for (String perm : wantedPermissions) {
            if (!hasPermission(perm)) {
                result.add(perm);
            }
        }

        return  result;
    }

    private boolean hasPermission(String permission) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED;
        }
        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (googleApiClient != null) {
            googleApiClient.connect();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        Log.d(TAG, "onResume() checkPlayServices() ? " + checkPlayServices());
        if (!checkPlayServices()) {
            locationTv.setText("You need to install Google Play Services to use the App properly");
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        // stop location updates
        if (googleApiClient != null && googleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);
            googleApiClient.disconnect();
        }
    }

    private boolean checkPlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);

        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(this, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST);
            } else {
                finish();
            }

            return false;
        }
        return true;
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d(TAG, "onLocationChanged() " + location);
        if (location != null) {
            locationTv.setText("Latitude: " + location.getLatitude() + "\nLongitude: " + location.getLongitude());
        }
    }
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.d(TAG, "onConnected() ");
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
        ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        // Permissions ok, we get last location
        location = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
        Log.d(TAG, "onConnected() " + location);
        if (location != null) {
            locationTv.setText("Latitude: " + location.getLatitude() + "\nLongitude: " + location.getLongitude());
        }

        startLocationUpdates();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    private void startLocationUpdates() {
        locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(UPDATE_INTERVAL);
        locationRequest.setFastestInterval(FASTEST_INTERVAL);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "You need to enable permission to display location!", Toast.LENGTH_LONG).show();
        }

        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);

    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case ALL_PERMISSIONS_RESULT:
                for (String perm : permissionsToRequest) {
                    if (!hasPermission(perm)) {
                        permissionsToRejected.add(perm);
                    }
                }

                if (permissionsToRejected.size() > 0) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (shouldShowRequestPermissionRationale(permissionsToRejected.get(0))) {
                            new AlertDialog.Builder(MainActivity.this)
                                    .setMessage("These permissions are mandatory to get your location. You need to allow them.")
                                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                                requestPermissions(permissionsToRejected.toArray(new String[permissionsToRejected.size()]), ALL_PERMISSIONS_RESULT);
                                            }
                                        }
                                    })
                                    .setNegativeButton("Cancel", null)
                                    .create()
                                    .show();
                            return;
                        }
                    }
                } else {
                    if (googleApiClient != null) {
                        googleApiClient.connect();
                    }
                }
                break;
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}
