package com.example.truckflow.load;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.truckflow.R;
import com.example.truckflow.databinding.ActivityLoadTwoBinding;
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

import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

public class LoadActivityTwo extends AppCompatActivity implements OnMapReadyCallback {

    private MapView mapView;
    private GoogleMap googleMap;
    private TextView unitTV, streetNameTV, cityTV, provinceTV, countryTV, zipcodeTV;
    private  ActivityLoadTwoBinding binding;

    String streetNumber,streetName,city,state,country,postalCode,latitude,longitude,selectedDate;

    private boolean isEditTextEmpty(EditText editText) {
        return editText.getText().toString().trim().isEmpty();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoadTwoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Places.initialize(getApplicationContext(), "AIzaSyAtw3f2NBYcbNVz01pmZPfZnQlOwnoErNk");
        PlacesClient placesClient = Places.createClient(this);

        //Google Maps Code
        try {
            MapsInitializer.initialize(getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }

        mapView = findViewById(R.id.pickup_map);
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

        AutocompleteSupportFragment autocompleteFragment = (AutocompleteSupportFragment) getSupportFragmentManager().findFragmentById(R.id.pickupLocationLayout);
        autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS, Place.Field.LAT_LNG, Place.Field.ADDRESS_COMPONENTS));
        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                // TODO: Get info about the selected place.
                place.toString();
                Log.i(TAG, "Place: " + place.toString() + ", " + place.getId() + "," + place.getAddress());


                List<AddressComponent> addressComponents = place.getAddressComponents().asList();
                for (AddressComponent component : addressComponents) {
                    List<String> types = component.getTypes();
                    String value = component.getName();

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

                String fullAddress = streetNumber + " " + streetName + ", " + city + ", " + state + ", " + country + " " + postalCode;
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
                Log.i(TAG, "An error occurred: " + status);
            }
        });


        //button to go to next activity
        Button next = binding.pickUpButton;
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (isEditTextEmpty((EditText) unitTV) || isEditTextEmpty((EditText) streetNameTV) || isEditTextEmpty((EditText) cityTV) ||
                        isEditTextEmpty((EditText) provinceTV) || isEditTextEmpty((EditText) countryTV) || isEditTextEmpty((EditText) zipcodeTV) ||
                        selectedDate == null) {

                    // Show an error message indicating that all fields are required
                    Toast.makeText(LoadActivityTwo.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                }

                else {

                    Intent currentIntent = new Intent(LoadActivityTwo.this, LoadActivityThree.class);

                    Bundle extras = getIntent().getExtras();
                    String role = "";
                    String email = "";

                    if (extras != null) {
                        role = extras.getString("role");
                        email = extras.getString("EMAIL_KEY");
                    }

                    currentIntent.putExtra("streetNumberPU",streetNumber);
                    currentIntent.putExtra("streetNamePU",streetName);
                    currentIntent.putExtra("cityPU",city);
                    currentIntent.putExtra("statePU",state);
                    currentIntent.putExtra("countryPU",country);
                    currentIntent.putExtra("postalCodePU",postalCode);
                    currentIntent.putExtra("longitudePU",longitude);
                    currentIntent.putExtra("latitudePU",latitude);

                    currentIntent.putExtra("datePU",selectedDate);
                    currentIntent.putExtra("role", role);
                    currentIntent.putExtra("EMAIL_KEY", email);

                    Log.d("got email and role", role + email);

                    //full address
                    String fullAddress = streetNumber + " " + streetName + ", " + city + ", " + state + ", " + country + " " + postalCode;
                    currentIntent.putExtra("addressPU",fullAddress);
                    startActivity(currentIntent);



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

        DatePickerDialog datePickerDialog = new DatePickerDialog(LoadActivityTwo.this, (view, selectedYear, selectedMonth, selectedDayOfMonth) -> {
            selectedDate = selectedDayOfMonth + "/" + (selectedMonth + 1) + "/" + selectedYear;
            button.setText(selectedDate);
        }, year, month, dayOfMonth);

        datePickerDialog.show();
    }





}