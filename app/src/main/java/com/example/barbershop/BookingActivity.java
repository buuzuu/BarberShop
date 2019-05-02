package com.example.barbershop;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.Toast;

import com.example.barbershop.Adapter.MyViewPagerAdapter;
import com.example.barbershop.Common.Common;
import com.example.barbershop.Common.NonSwipeViewPager;
import com.example.barbershop.Model.Barber;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.shuhart.stepview.StepView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import dmax.dialog.SpotsDialog;

public class BookingActivity extends AppCompatActivity {

    @BindView(R.id.step_view)
    StepView stepView;
    @BindView(R.id.view_pager)
    NonSwipeViewPager viewPager;
    @BindView(R.id.btn_previous_step)
    Button btn_previous_step;
    @BindView(R.id.btn_next_step)
    Button btn_next_step;
    LocalBroadcastManager localBroadcastManager;
    AlertDialog dialog;
    CollectionReference barberRef;

    @OnClick(R.id.btn_previous_step)
    void previousStep() {
        if (Common.step == 3 || Common.step > 0) {
            Common.step--;
            viewPager.setCurrentItem(Common.step);
        }
    }


    @OnClick(R.id.btn_next_step)
    void nextClick() {
        if (Common.step < 3 || Common.step == 0) {
            Common.step++;
            if (Common.step == 1) {// After choose salon
                if (Common.currentSalon != null)
                    loadBarberBySalon(Common.currentSalon.getSalonId());
            } else if (Common.step == 2) // pick time slot
            {
                if (Common.currentBarber != null) {
                    loadTimeSlotBarber(Common.currentBarber.getBarberId());
                }
            }

            viewPager.setCurrentItem(Common.step);
        }
    }

    private void loadTimeSlotBarber(String barberId) {

        //Send local broadcast to fragment 3
        Intent intent = new Intent(Common.KEY_DISPLAY_TIME_OUT);
        localBroadcastManager.sendBroadcast(intent);
    }

    private void loadBarberBySalon(String salonId) {

        dialog.show();
        //now , select all barber of salon

        if (!TextUtils.isEmpty(Common.city)) {
            barberRef = FirebaseFirestore.getInstance().collection("AllSalon")
                    .document(Common.city).collection("Branch")
                    .document(salonId).collection("Barbers");
            barberRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {

                    ArrayList<Barber> barbers = new ArrayList<>();
                    for (QueryDocumentSnapshot barberSnapshot : task.getResult()) {

                        Barber barber = barberSnapshot.toObject(Barber.class);
                        barber.setPassword("");
                        barber.setBarberId(barberSnapshot.getId());
                        barbers.add(barber);

                    }

                    //send broadcast to step2fragment to load recyclerview
                    Intent intent = new Intent(Common.KEY_BARBER_LOAD_DONE);
                    intent.putParcelableArrayListExtra(Common.KEY_BARBER_LOAD_DONE, barbers);
                    localBroadcastManager.sendBroadcast(intent);
                    dialog.dismiss();

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    dialog.dismiss();
                }
            });
        }


    }


    private BroadcastReceiver buttonNextReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            int step = intent.getIntExtra(Common.KEY_STEP, 0);
            if (step == 1)
                Common.currentSalon = intent.getParcelableExtra(Common.KEY_SALON_STORE);
            else if (step == 2)
                Common.currentBarber = intent.getParcelableExtra(Common.KEY_BARBER_SELECTED);


            btn_next_step.setEnabled(true);
            setColorButtons();

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking);
        ButterKnife.bind(BookingActivity.this);
        setupStepView();
        setColorButtons();
        dialog = new SpotsDialog.Builder().setCancelable(false).setContext(this).build();
        localBroadcastManager = LocalBroadcastManager.getInstance(this);
        localBroadcastManager.registerReceiver(buttonNextReceiver, new IntentFilter(Common.KEY_ENABLE_BUTTON_NEXT));
        //View pager

        viewPager.setAdapter(new MyViewPagerAdapter(getSupportFragmentManager()));
        viewPager.setOffscreenPageLimit(4);// if we don't do it we will loose state
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i1) {

            }

            @Override
            public void onPageSelected(int i) {

                stepView.go(i, true);

                if (i == 0) {
                    btn_previous_step.setEnabled(false);
                } else {
                    btn_previous_step.setEnabled(true);
                }

                // set disable next button here
                btn_next_step.setEnabled(false);

                setColorButtons();
            }

            @Override
            public void onPageScrollStateChanged(int i) {

            }
        });

    }

    private void setColorButtons() {

        if (btn_next_step.isEnabled()) {
            btn_next_step.setBackgroundResource(R.color.colorButton);
        } else {
            btn_next_step.setBackgroundResource(android.R.color.darker_gray);
        }

        if (btn_previous_step.isEnabled()) {

            btn_previous_step.setBackgroundResource(R.color.colorButton);
        } else {
            btn_previous_step.setBackgroundResource(android.R.color.darker_gray);
        }

    }

    private void setupStepView() {
        List<String> stepList = new ArrayList<>();
        stepList.add("Salon");
        stepList.add("Barber");
        stepList.add("Time");
        stepList.add("Confirm");
        stepView.setSteps(stepList);
    }

    @Override
    protected void onDestroy() {
        localBroadcastManager.unregisterReceiver(buttonNextReceiver);
        super.onDestroy();

    }
}
