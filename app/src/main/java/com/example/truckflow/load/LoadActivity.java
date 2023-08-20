package com.example.truckflow.load;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.example.truckflow.databinding.ActivityLoadBinding;
import com.example.truckflow.R;
import com.example.truckflow.entities.Load;
import com.example.truckflow.entities.Trucker;
import com.example.truckflow.home.Home;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.libraries.places.api.Places;

import com.google.android.material.slider.LabelFormatter;
import com.google.android.material.slider.Slider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.UUID;

public class LoadActivity extends AppCompatActivity {


    private ActivityLoadBinding binding;

    float loadWeight = 0;
    float loadLength = 0;

    String loadName, loadDescription;

    DatabaseReference databaseRef;
    FirebaseFirestore db;


    private String streetNumberPU, streetNamePU, cityPU, statePU, countryPU, postalCodePU, longitudePU, latitudePU, datePU, addressPU;
    private String streetNumberDel, streetNameDel, cityDel, stateDel, countryDel, postalCodeDel, longitudeDel, latitudeDel, dateDel, addressDel;
    private int distance;
    private Double distanceInKM;
    double expectedPrice;
    private int duration;

    private Load load;
    private Double durationInHours;

    private boolean isEditTextEmpty(EditText editText) {
        return editText.getText().toString().trim().isEmpty();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
     //   setContentView(R.layout.activity_load);

        SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);

        load = new Load();
        binding = ActivityLoadBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        loadName = binding.loadName.getText().toString();
        Log.i("Load Name::Load Act::",loadName);

        loadDescription = binding.loadDescription.getEditableText().toString();
        Log.i("Load Name::Load Act::",loadName);

        databaseRef = FirebaseDatabase.getInstance().getReference();
        db = FirebaseFirestore.getInstance();


        Places.initialize(getApplicationContext(), "AIzaSyAtw3f2NBYcbNVz01pmZPfZnQlOwnoErNk");

        Bundle bundle = getIntent().getExtras();

        if (bundle != null) {
            // Retrieve the data from the bundle
            streetNumberPU = bundle.getString("streetNumberPU");
            streetNamePU = bundle.getString("streetNamePU");
            cityPU = bundle.getString("cityPU");
            statePU = bundle.getString("statePU");
            countryPU = bundle.getString("countryPU");
            postalCodePU = bundle.getString("postalCodePU");
            longitudePU = bundle.getString("longitudePU");
            latitudePU = bundle.getString("latitudePU");
            datePU = bundle.getString("datePU");
            addressPU = bundle.getString("addressPU");

            streetNumberDel = bundle.getString("streetNumberDel");
            streetNameDel = bundle.getString("streetNameDel");
            cityDel = bundle.getString("cityDel");
            stateDel = bundle.getString("stateDel");
            countryDel = bundle.getString("countryDel");
            postalCodeDel = bundle.getString("postalCodeDel");
            longitudeDel = bundle.getString("longitudeDel");
            latitudeDel = bundle.getString("latitudeDel");
            int distance = bundle.getInt("distance");
            distanceInKM = Double.valueOf(Double.valueOf(distance) / 1000);
            duration = bundle.getInt("duration");
            durationInHours = Double.valueOf(duration / 60 * 60);
            dateDel = bundle.getString("dateDel");
            addressDel = bundle.getString("addressDel");
        }
        double baseRatePerKm = 2.5;
        double additionalRatePerKmPerTonne = 0.1;
        double distance = distanceInKM; // Length of the freight route in kilometers.
        double pricePerKm = baseRatePerKm + (additionalRatePerKmPerTonne * loadWeight);
        expectedPrice = pricePerKm * distance;


        load.setLoadDescription(loadDescription);
        load.setLoadLength(loadLength+"");
        load.setLoadName(loadName);
        load.setLoadWeight(loadWeight+"");


        load.setPickUpDate(datePU);
        load.setPickupAddress(addressPU);

        load.setDeliveryDate(dateDel);
        load.setDeliveryAddress(addressDel);


        //setting long and lat
        load.setLongitudePU(longitudePU);
        load.setLatitudePU(latitudePU);

        load.setLongitudeDel(longitudeDel);
        load.setLatitudeDel(latitudeDel);

        load.setTotalDistance(distance+"");
        load.setExpectedPrice(expectedPrice+"");
        //duration setting
        load.setDurationInHours(durationInHours+"");

        binding.pickupAddress.setText("PickUp Address:"+addressPU);
        binding.deliveryAddress.setText("Delivery Address:"+addressDel);
        binding.deliveryDate.setText("Delivery Date:"+dateDel);
        binding.pickupDate.setText("Pick up date:"+datePU);
        binding.distance.setText("Total distance:"+distanceInKM.toString()+" KM");
        String formattedValue = String.format("%.2f", expectedPrice);
        binding.price.setText("Expected Price:"+formattedValue+" $");
        binding.sliderLoad.addOnChangeListener(new Slider.OnChangeListener() {
            @Override
            public void onValueChange(Slider slider, float value, boolean fromUser) {
                loadWeight = value;

            }

        });

        binding.sliderLoad.addOnSliderTouchListener(new Slider.OnSliderTouchListener() {

            @Override
            public void onStartTrackingTouch(@NonNull Slider slider) {

            }

            @Override
            public void onStopTrackingTouch(@NonNull Slider slider) {
                double baseRatePerKm = 2.5;
                double additionalRatePerKmPerTonne = 0.1;
                double distance = distanceInKM; // Length of the freight route in kilometers.
                double pricePerKm = baseRatePerKm + (additionalRatePerKmPerTonne * loadWeight/2000);
                double expectedPrice = pricePerKm * distance;


                binding.loadWeightLabel.setText("Load Weight : " + loadWeight + " lbs");
                String formattedValue = String.format("%.2f", expectedPrice);
                load.setExpectedPrice(formattedValue);
                load.setLoadWeight(loadWeight+"");
                binding.price.setText("Expected Price:"+formattedValue+" $");

            }
        });

        binding.sliderLength.setLabelFormatter(new LabelFormatter() {
            @NonNull
            @Override
            public String getFormattedValue(float value) {
                return String.valueOf(value) + " ft";
            }
        });


        binding.sliderLength.addOnChangeListener(new Slider.OnChangeListener() {
            @Override
            public void onValueChange(Slider slider, float value, boolean fromUser) {
                loadLength = value;
                load.setLoadLength(loadLength+"");

            }

        });

        binding.sliderLength.addOnSliderTouchListener(new Slider.OnSliderTouchListener() {

            @Override
            public void onStartTrackingTouch(@NonNull Slider slider) {
            }

            @Override
            public void onStopTrackingTouch(@NonNull Slider slider) {


                binding.loadDimLabel.setText("Load Length : " + loadLength + " ft");
            }
        });

        binding.sliderLoad.setLabelFormatter(new LabelFormatter() {
            @NonNull
            @Override
            public String getFormattedValue(float value) {
                return value + " ft";
            }
        });


        binding.button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                if (isEditTextEmpty(binding.loadName) || isEditTextEmpty(binding.loadDescription)) {
                    // Show an error message indicating that all fields are required
                    Toast.makeText(LoadActivity.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                } else {

                    Intent i = new Intent(LoadActivity.this, Home.class);
                    //post intent here

                    UUID uuid = UUID.randomUUID();
                    loadName = binding.loadName.getText().toString();
                    Log.i("Load Name::Load Act::", loadName);

                    loadDescription = binding.loadDescription.getEditableText().toString();
                    Log.i("Load Name::Load Act::", loadName);
                    load.setLoadName(loadName);
                    load.setLoadDescription(loadDescription);

                    String username = sharedPreferences.getString("email", null);
                    load.setContactInformation(username);
                    Log.i("Username", username);
                    databaseRef.child("Load").child(String.valueOf(uuid)).setValue(load)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    Log.d(TAG, "Load data saved to Firebase Realtime Database");
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.e(TAG, "Error saving Load data to Firebase Realtime Database", e);
                                }
                            });

                    db.collection("load")
                            .add(load)
                            .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                @Override
                                public void onSuccess(DocumentReference documentReference) {
                                    Log.d(TAG, "Trucker data saved to Firestore with ID: " + documentReference.getId());
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.e(TAG, "Error saving trucker data to Firestore", e);
                                }
                            });
                    startActivity(i);

                }
            }
            });

}}
