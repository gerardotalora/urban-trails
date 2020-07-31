package edu.neu.madcourse.urban_trails;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import static android.content.Context.LOCATION_SERVICE;

public class MapsFragment extends Fragment implements OnMapReadyCallback, LocationListener {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private static final String STATE_KEY_MAP_CAMERA = "STATE_KEY_MAP_CAMERA";
    private static final String STATE_KEY_TRAIL = "STATE_KEY_TRAIL";
    private static final boolean STATIONARY_TESTING = false; // TODO This flag controls whether you need to walk or not

    GoogleMap map;
    private LocationManager locationManager;
    CameraPosition previousCameraPosition;
    private LatLng myLocation;

    private ParcelableArrayList<Stop> trail;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_maps, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        SupportMapFragment mapFragment =
                (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
        this.locationManager = (LocationManager) getActivity().getSystemService(LOCATION_SERVICE);
        this.trail = new ParcelableArrayList<>();
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera.
     * In this case, we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to
     * install it inside the SupportMapFragment. This method will only be triggered once the
     * user has installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.map = googleMap;
        this.enableMyLocation();
        this.displayTrailOnMap();
    }

    /**
     * Gets called when the user clicks "Add Stop" in the interface.
     * Should add a stop to the trail at my location.
     */
    public void addStop() {
        if (myLocation != null) {
            LatLng latLng;

            // FOR TESTING
            if (STATIONARY_TESTING) {
                if (this.trail.size() > 0) {
                    latLng = shiftLatLngByFeet(this.trail.get(this.trail.size() - 1).getLatLng(), 100, 9);
                } else {
                    latLng = shiftLatLngByFeet(myLocation, 101, 0);
                }
            } else {
                if (this.trail.size() > 0 && myLocation == this.trail.get(this.trail.size() - 1).getLatLng()) {
                    Toast.makeText(getActivity(), "You already added this stop! Try walking a little farther.", Toast.LENGTH_LONG).show();
                }
                latLng = myLocation;
            }



            Stop stop = new Stop("Stop " + this.trail.size(), latLng);
            this.trail.add(stop);
        } else {
            Toast.makeText(getActivity(), "myLocation is null", Toast.LENGTH_LONG).show();
        }
        this.displayTrailOnMap();
    }

    public ArrayList<Stop> getTrail() {
        return this.trail;
    }

    private LatLng shiftLatLngByFeet(LatLng in, double latShiftFeet, double longShiftFeet) {
        double latShiftDegrees = latShiftFeet / (70 * 5280);
        double longShiftDegrees = longShiftFeet / (70 * 5280);
        return new LatLng(in.latitude + latShiftDegrees, in.longitude + longShiftDegrees);
    }

    private void enableMyLocation() {
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
            return;
        }
        this.map.setMyLocationEnabled(true);
        subscribeToLocation();
    }

    private void zoomToLocation(double latitude, double longitude) {
        LatLng latLng = new LatLng(latitude, longitude);
        map.animateCamera(CameraUpdateFactory.newCameraPosition(CameraPosition.fromLatLngZoom(latLng, 16.0f)));
    }

    /**
     * This method gets called whenever Android gives us an update on the user's location
     *
     * @param location
     */
    @Override
    public void onLocationChanged(Location location) {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm:ss");
        Toast.makeText(getActivity(), "Location updated at " + dtf.format(LocalDateTime.now()), Toast.LENGTH_SHORT).show();
        this.myLocation = new LatLng(location.getLatitude(), location.getLongitude());
        if (this.trail.size() == 0) {
            Stop stop = new Stop("Starting Location", this.myLocation);
            this.trail.add(stop);
            this.displayTrailOnMap();
        }
        zoomToLocation(location.getLatitude(), location.getLongitude());
    }

    private void displayTrailOnMap() {
        this.map.clear();
        for (Stop stop : this.trail) {
            this.map.addMarker(new MarkerOptions().position(stop.getLatLng()).title(stop.getTitle()));
        }
    }


    private void subscribeToLocation() {
        Log.v("DEBUG", "subscribeToLocation");
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_COARSE);

        String enabledProvider = locationManager.getBestProvider(criteria, false);

        if (enabledProvider == null) {
            Log.v("DEBUG", "Best provider is null");
            return;
        }

        Log.v("DEBUG", "locationManager.requestSingleUpdate");
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        locationManager.requestLocationUpdates(enabledProvider, 3000, 3, this, null);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode,
                permissions,
                grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                // Showing the toast message
                Toast.makeText(getActivity(),
                        "Fine Location Permission Granted",
                        Toast.LENGTH_SHORT)
                        .show();

                enableMyLocation();
            } else {
                Toast.makeText(getActivity(),
                        "Fine Location Permission Denied",
                        Toast.LENGTH_SHORT)
                        .show();
            }
        }
    }

    /**
     * This method must restore the fragment state (e.g. after switching between portrait and landscape mode)
     *
     * @param savedInstanceState
     */
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {
            this.previousCameraPosition = (CameraPosition) savedInstanceState.get(STATE_KEY_MAP_CAMERA);
            this.trail = (ParcelableArrayList<Stop>) savedInstanceState.get(STATE_KEY_TRAIL);
        }
    }

    /**
     * This method must save the fragment state before switching between portrait and landscape mode
     *
     * @param outState
     */
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(STATE_KEY_MAP_CAMERA, this.map.getCameraPosition());
        outState.putParcelable(STATE_KEY_TRAIL, this.trail);
    }


    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

}

class Stop {
    private final String title;
    private final LatLng latLng;

    public Stop(String title, LatLng latLng) {
        this.title = title;
        this.latLng = latLng;
    }

    public String getTitle() {
        return title;
    }

    public LatLng getLatLng() {
        return latLng;
    }
}

class ParcelableArrayList<T> extends ArrayList<T> implements Parcelable, List<T> {

    private ArrayList<T> arrayList;

    protected ParcelableArrayList(Parcel in) {
        this.arrayList = in.readArrayList(null);
    }

    public static final Creator<ParcelableArrayList> CREATOR = new Creator<ParcelableArrayList>() {
        @Override
        public ParcelableArrayList createFromParcel(Parcel in) {
            return new ParcelableArrayList(in);
        }

        @Override
        public ParcelableArrayList[] newArray(int size) {
            return new ParcelableArrayList[size];
        }
    };

    public ParcelableArrayList() {
        this.arrayList = new ArrayList<>();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeList(this.arrayList);
    }

    @Override
    public int size() {
        return this.arrayList.size();
    }

    @Override
    public boolean isEmpty() {
        return this.arrayList.isEmpty();
    }

    @Override
    public boolean contains(@Nullable Object o) {
        return this.arrayList.contains(o);
    }

    @NonNull
    @Override
    public Iterator<T> iterator() {
        return this.arrayList.iterator();
    }

    @NonNull
    @Override
    public Object[] toArray() {
        return this.arrayList.toArray();
    }

    @NonNull
    @Override
    public <T1> T1[] toArray(@NonNull T1[] a) {
        return this.arrayList.toArray(a);
    }

    @Override
    public boolean add(T t) {
        return this.arrayList.add(t);
    }

    @Override
    public boolean remove(@Nullable Object o) {
        return this.arrayList.remove(o);
    }

    @Override
    public boolean containsAll(@NonNull Collection<?> c) {
        return this.arrayList.containsAll(c);
    }

    @Override
    public boolean addAll(@NonNull Collection<? extends T> c) {
        return this.arrayList.addAll(c);
    }

    @Override
    public boolean addAll(int index, @NonNull Collection<? extends T> c) {
        return this.arrayList.addAll(index, c);
    }

    @Override
    public boolean removeAll(@NonNull Collection<?> c) {
        return this.arrayList.removeAll(c);
    }

    @Override
    public boolean retainAll(@NonNull Collection<?> c) {
        return this.arrayList.retainAll(c);
    }

    @Override
    public void clear() {
        this.arrayList.clear();
    }

    @Override
    public T get(int index) {
        return this.arrayList.get(index);
    }

    @Override
    public T set(int index, T element) {
        return this.arrayList.set(index, element);
    }

    @Override
    public void add(int index, T element) {
        this.arrayList.add(index, element);
    }

    @Override
    public T remove(int index) {
        return this.arrayList.remove(index);
    }

    @Override
    public int indexOf(@Nullable Object o) {
        return this.arrayList.indexOf(o);
    }

    @Override
    public int lastIndexOf(@Nullable Object o) {
        return this.arrayList.lastIndexOf(o);
    }

    @NonNull
    @Override
    public ListIterator<T> listIterator() {
        return this.arrayList.listIterator();
    }

    @NonNull
    @Override
    public ListIterator<T> listIterator(int index) {
        return this.arrayList.listIterator(index);
    }

    @NonNull
    @Override
    public List<T> subList(int fromIndex, int toIndex) {
        return this.arrayList.subList(fromIndex, toIndex);
    }
}