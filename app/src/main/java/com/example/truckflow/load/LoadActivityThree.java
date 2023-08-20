package com.example.truckflow.load;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.truckflow.R;
import com.example.truckflow.databinding.ActivityLoadThreeBinding;
import com.example.truckflow.entities.Distance;
import com.example.truckflow.entities.DistanceMatrixResponse;
import com.example.truckflow.entities.Duration;
import com.example.truckflow.entities.Element;
import com.example.truckflow.entities.Row;
import com.example.truckflow.home.Home;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.AddressComponent;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.gson.Gson;


import java.io.IOException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

public class LoadActivityThree extends AppCompatActivity implements OnMapReadyCallback {


    private MapView mapView;
    private GoogleMap googleMap;
    private TextView unitTV, streetNameTV, cityTV, provinceTV, countryTV, zipcodeTV;
    private ActivityLoadThreeBinding binding;

    private RequestQueue mRequestQueue;
    private StringRequest mStringRequest;


    String streetNumber, streetName, city, state, country, postalCode, latitude, longitude, selectedDate, fullAddress;

    private String distanceH,durationH;
    private int distanceV,durationV;

    private boolean isEditTextEmpty(EditText editText) {
        return editText.getText().toString().trim().isEmpty();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoadThreeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Places.initialize(getApplicationContext(), "AIzaSyAtw3f2NBYcbNVz01pmZPfZnQlOwnoErNk");

        PlacesClient placesClient = Places.createClient(this);


        MapsInitializer.initialize(getApplicationContext());

        mapView = findViewById(R.id.drop_map);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        unitTV = binding.apartmentNumberEditText;
        streetNameTV = binding.streetNameEditText;
        cityTV = binding.cityNameEditText;
        provinceTV = binding.provinceNameEditText;
        countryTV = binding.countryNameEditText;
        zipcodeTV = binding.pinCodeEditText;


        Button openDatePickerButton = binding.dropDateTime;
        openDatePickerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDatePicker(openDatePickerButton);
            }
        });

        AutocompleteSupportFragment autocompleteFragment = (AutocompleteSupportFragment) getSupportFragmentManager().findFragmentById(R.id.deliveryLocationLayout);

        autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS, Place.Field.LAT_LNG, Place.Field.ADDRESS_COMPONENTS));
        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                // TODO: Get info about the selected place.
                place.toString();
                Log.i(TAG, "Place: " + place.toString() + ", " + place.getId() + "," + place.getAddress());


                List<AddressComponent> addressComponents = place.getAddressComponents().asList();
                System.out.println(place);
                // Variables to store the relevant address information
                // Loop through the address components and extract relevant information
                for (AddressComponent component : addressComponents) {
                    List<String> types = component.getTypes();
                    String value = component.getName();

                    // Check the type of the address component and store the relevant information
                    if (types.contains("street_number")) {
                        streetNumber = value;
                    } else if (types.contains("route")) {
                        streetName = value;
                    } else if (types.contains("locality")) {
                        city = value;
                    } else if (types.contains("administrative_area_level_1")) {
                        state = value;
                    } else if (types.contains("country")) {
                        country = value;
                    } else if (types.contains("postal_code")) {
                        postalCode = value;
                    }
                }

                fullAddress = streetNumber + " " + streetName + ", " + city + ", " + state + ", " + country + " " + postalCode;
                Log.i(TAG, "Full Address: " + fullAddress);

                unitTV.setText(streetNumber);
                streetNameTV.setText(streetName);
                cityTV.setText(city);
                provinceTV.setText(state);
                countryTV.setText(country);
                zipcodeTV.setText(postalCode);

                LatLng newLatLng = place.getLatLng();
                String placeName = place.getName();

                longitude = String.valueOf(newLatLng.longitude);
                latitude = String.valueOf(newLatLng.latitude);

                updateMapMarker(newLatLng, placeName);
            }

            @Override
            public void onError(Status status) {
                // TODO: Handle the error.
                Log.i(TAG, "An error occurred: " + status);
            }
        });


        Button postLoadButton = binding.dropLocButton;

        postLoadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isEditTextEmpty((EditText) unitTV) || isEditTextEmpty((EditText) streetNameTV) || isEditTextEmpty((EditText) cityTV) ||
                        isEditTextEmpty((EditText) provinceTV) || isEditTextEmpty((EditText) countryTV) || isEditTextEmpty((EditText) zipcodeTV) ||
                        selectedDate == null || fullAddress == null) {

                    // Show an error message indicating that all fields are required
                    Toast.makeText(LoadActivityThree.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                } else {
                    String origin = "";
                    Bundle extras = getIntent().getExtras();
                    if (extras != null) {
                        // ... (rest of the code)

                        origin = extras.getString("addressPU");
                        String destination = fullAddress;

                        // Proceed with the getDistance method call and other actions
                        getDistance(origin, destination);
                    }
                }
            }
        });
    }


    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        googleMap.getUiSettings().setZoomControlsEnabled(true);
        this.googleMap = googleMap;
        double latitude1 = 37.7749;
        double longitude1 = -122.4194;

        LatLng pickupLatLng = new LatLng(latitude1, longitude1);

        MarkerOptions pickupMarkerOptions = new MarkerOptions().position(pickupLatLng).title("Pickup");
        googleMap.addMarker(pickupMarkerOptions);

        CameraPosition cameraPosition = new CameraPosition.Builder().target(pickupLatLng).zoom(5).build();
        googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

    }


    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }


    private void updateMapMarker(LatLng latLng, String title) {
        googleMap.clear(); // Clear any existing markers

        MarkerOptions markerOptions = new MarkerOptions().position(latLng).title(title);
        googleMap.addMarker(markerOptions);

        CameraPosition cameraPosition = new CameraPosition.Builder().target(latLng).zoom(15) // Adjust the zoom level as needed
                .build();
        googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }

    private void showDatePicker(Button button) {
        // Get the current date
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);

        // Create a DatePickerDialog and set the initial date to the current date
        DatePickerDialog datePickerDialog = new DatePickerDialog(LoadActivityThree.this, (view, selectedYear, selectedMonth, selectedDayOfMonth) -> {
            selectedDate = selectedDayOfMonth + "/" + (selectedMonth + 1) + "/" + selectedYear;
            button.setText(selectedDate);
        }, year, month, dayOfMonth);

        datePickerDialog.show();
    }
    private void getDistance(String origin,String destination) {
        // RequestQueue initialized
        mRequestQueue = Volley.newRequestQueue(this);
        String url = "https://maps.googleapis.com/maps/api/distancematrix/json?origins=" + origin + "&destinations=" + destination + "&units=imperial&key=AIzaSyAtw3f2NBYcbNVz01pmZPfZnQlOwnoErNk";
        // String Request initialized
        mStringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.i("Response", "Response :" + response.toString());
                parseDistanceAndDuration(response.toString());
                Toast.makeText(getApplicationContext(), "Response :" + response.toString(), Toast.LENGTH_LONG).show();//display the response on screen
                Bundle extras = getIntent().getExtras();
                if (extras != null) {

                    String role = extras.getString("role");
                    String email = extras.getString("EMAIL_KEY");

                    if (role != null && role.equals("trucker")) {
                        // Start Home activity for trucker
                        Intent homeIntent = new Intent(LoadActivityThree.this, Home.class);
                        // Rest of the code...
                        startActivity(homeIntent);
                        finish(); // Close this activity
                    }

                    else {
                        String streetNumberPU = extras.getString("streetNumberPU");
                        String streetNamePU = extras.getString("streetNamePU");
                        String cityPU = extras.getString("cityPU");
                        String statePU = extras.getString("statePU");
                        String countryPU = extras.getString("countryPU");
                        String postalCodePU = extras.getString("postalCodePU");
                        String longitudePU = extras.getString("longitudePU");
                        String latitudePU = extras.getString("latitudePU");



                        String datePU = extras.getString("datePU");

                        String origin = extras.getString("addressPU");

                        String destination = fullAddress;
                        Intent currentIntent = new Intent(LoadActivityThree.this,LoadActivity.class);
                        currentIntent.putExtra("streetNumberPU",streetNumberPU);
                        currentIntent.putExtra("streetNamePU",streetNamePU);
                        currentIntent.putExtra("cityPU",cityPU);
                        currentIntent.putExtra("statePU",statePU);
                        currentIntent.putExtra("countryPU",countryPU);
                        currentIntent.putExtra("postalCodePU",postalCodePU);
                        currentIntent.putExtra("longitudePU",longitudePU);
                        currentIntent.putExtra("latitudePU",latitudePU);
                        currentIntent.putExtra("datePU",datePU);
                        currentIntent.putExtra("longitudeDel",longitude);
                        currentIntent.putExtra("latitudeDel", latitude);
                        //full address
                        String fullAddressOri = streetNumberPU + " " + streetNamePU + ", " + city + ", " + state + ", " + country + " " + postalCode;
                        currentIntent.putExtra("addressPU",fullAddressOri);

                        currentIntent.putExtra("streetNumberPU",streetNumber);
                        currentIntent.putExtra("streetNamePU",streetName);
                        currentIntent.putExtra("cityPU",city);
                        currentIntent.putExtra("statePU",state);
                        currentIntent.putExtra("countryPU",country);
                        currentIntent.putExtra("postalCodePU",postalCode);
                        currentIntent.putExtra("longitudePU",longitude);
                        currentIntent.putExtra("latitudePU",latitude);
                        currentIntent.putExtra("distance",distanceV);
                        currentIntent.putExtra("duration",durationV);

                        currentIntent.putExtra("dateDel",selectedDate);
                        //full address
                        String fullAddressDel = streetNumber + " " + streetName
                                + ", " + city + ", " + state + ", " + country + " " + postalCode;
                        currentIntent.putExtra("addressDel",fullAddressDel);
                        startActivity(currentIntent);
                    }
                    }
                    //getting the values from the old ones

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.i(TAG, "Error :" + error.toString());
            }
        });

        mRequestQueue.add(mStringRequest);
    }

    private void parseDistanceAndDuration(String result) {
        Gson gson = new Gson();
        Log.i("Response::::",result);

        DistanceMatrixResponse response = gson.fromJson(result, DistanceMatrixResponse.class);

        Log.i("Response::::",result);
        Log.i("Response class",response.toString());
        Log.i("Response",result);
        if ("OK".equals(response.status) && response.rows != null && !response.rows.isEmpty()) {
            Row row = response.rows.get(0);
            if (row.elements != null && !row.elements.isEmpty()) {
                Element element = row.elements.get(0);
                if ("OK".equals(element.status)) {
                    // Extract distance
                    Distance distance = element.distance;
                    String distanceText = distance.text;
                    int distanceValue = distance.value;

                    // Extract duration
                    Duration duration = element.duration;
                    String durationText = duration.text;
                    int durationValue = duration.value;

                    distanceV=distanceValue;
                    durationV=durationValue;

                    // Now you have the distance and duration as strings and integer values, which you can use as needed
                    Log.d("LoadActivityThree", "Distance: " + distanceText + " (" + distanceValue + " meters)");
                    Log.d("LoadActivityThree", "Duration: " + durationText + " (" + durationValue + " seconds)");
                } else {
                    // Handle API response with status other than "OK"
                    Log.e("LoadActivityThree", "Error: " + element.status);
                }
            }
        } else {
            Log.e("LoadActivityThree", "Error: " + response.status);
        }
    }


}