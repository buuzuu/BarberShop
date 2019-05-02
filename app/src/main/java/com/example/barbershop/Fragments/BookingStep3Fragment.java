package com.example.barbershop.Fragments;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.barbershop.Adapter.MyTimeSlotAdapter;
import com.example.barbershop.Common.Common;
import com.example.barbershop.Common.SpaceItemDecoration;
import com.example.barbershop.Interface.ITimeSlotLoadListner;
import com.example.barbershop.Model.TimeSlot;
import com.example.barbershop.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import devs.mulham.horizontalcalendar.HorizontalCalendar;
import devs.mulham.horizontalcalendar.HorizontalCalendarView;
import devs.mulham.horizontalcalendar.utils.HorizontalCalendarListener;
import dmax.dialog.SpotsDialog;

public class BookingStep3Fragment extends Fragment implements ITimeSlotLoadListner {

    static BookingStep3Fragment instance;
    AlertDialog  dialog;
    LocalBroadcastManager localBroadcastManager;
    Calendar selected_date;
    Unbinder unbinder;
    DocumentReference barberDoc;
    ITimeSlotLoadListner iTimeSlotLoadListner;

    @BindView(R.id.recycler_time_slot)
    RecyclerView recycler_time_slot;
    @BindView(R.id.calenderView)
    HorizontalCalendarView calendarView;
    SimpleDateFormat simpleDateFormat;
    BroadcastReceiver displayTimeSlot = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Calendar date = Calendar.getInstance();
            date.add(Calendar.DATE,0);
            loadAvailableTimeSlotOfBarber(Common.currentBarber.getBarberId(),simpleDateFormat.format(date.getTime()));
        }
    };

    private void loadAvailableTimeSlotOfBarber(String barberId, final String bookDate) {

        dialog.show();
    //    /AllSalon/Boston/Branch/ZHeaCwtrLPz79eycpmHz/Barbers/sWyRt36aPn9xTYTPPjxB
        barberDoc = FirebaseFirestore.getInstance().collection("AllSalon").document(Common.city)
                .collection("Branch").document(Common.currentSalon.getSalonId()).collection("Barbers")
                .document(Common.currentBarber.getBarberId());
        barberDoc.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()){
                    DocumentSnapshot documentSnapshot = task.getResult();
                    if (documentSnapshot.exists()){//if barber available
                        // GET INFORMATION OF BOOKING
                        // IF NOT CREATED RETURN EMPTY
                        CollectionReference date = FirebaseFirestore.getInstance().collection("AllSalon").document(Common.city)
                                .collection("Branch").document(Common.currentSalon.getSalonId()).collection("Barbers")
                                .document(Common.currentBarber.getBarberId()).collection(bookDate);
                        date.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.isSuccessful()){
                                    QuerySnapshot querySnapshot= task.getResult();
                                    if (querySnapshot.isEmpty()){//if don't have any appointment
                                        iTimeSlotLoadListner.onTimeSlotLoadEmpty();
                                    }else {
                                        // if have appointment
                                        List<TimeSlot> timeSlots = new ArrayList<>();
                                        for (QueryDocumentSnapshot document:task.getResult()){
                                            timeSlots.add(document.toObject(TimeSlot.class));
                                            iTimeSlotLoadListner.onTimeSlotLoadSuccess(timeSlots);
                                        }

                                    }
                                }
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {


                                iTimeSlotLoadListner.onTimeSlotLoadFailed(e.getMessage());
                            }
                        });
                    }
                }
            }
        });



    }

    public static BookingStep3Fragment getInstance() {
        if (instance == null)
            instance = new BookingStep3Fragment();

        return instance;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        iTimeSlotLoadListner = this;
        selected_date = Calendar.getInstance();
        localBroadcastManager = LocalBroadcastManager.getInstance(getContext());
        localBroadcastManager.registerReceiver(displayTimeSlot,new IntentFilter(Common.KEY_DISPLAY_TIME_OUT));
        simpleDateFormat = new SimpleDateFormat("dd_MM_yyyy");
        dialog = new SpotsDialog.Builder().setContext(getContext()).setCancelable(false).build();
        selected_date.add(Calendar.DATE,0);  // current date

    }

    @Override
    public void onDestroy() {
        localBroadcastManager.unregisterReceiver(displayTimeSlot);
        super.onDestroy();

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater,container,savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_booking_step_three, container, false);
        unbinder = ButterKnife.bind(this,view);
        init(view);
        return view;
    }

    private void init(View view) {

        recycler_time_slot.setHasFixedSize(true);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getActivity(),3);
        recycler_time_slot.setLayoutManager(gridLayoutManager);
        recycler_time_slot.addItemDecoration(new SpaceItemDecoration(8));

        //Calender
        Calendar startDate = Calendar.getInstance();
        startDate.add(Calendar.DATE,0);
        Calendar endDate = Calendar.getInstance();
        endDate.add(Calendar.DATE,2);   // 2 DAYS LEFT
        HorizontalCalendar horizontalCalendar = new HorizontalCalendar.Builder(view,R.id.calenderView)
                        .range(startDate,endDate)
                .datesNumberOnScreen(1)
                .mode(HorizontalCalendar.Mode.DAYS)
                .defaultSelectedDate(startDate)
                .build();

        horizontalCalendar.setCalendarListener(new HorizontalCalendarListener() {
            @Override
            public void onDateSelected(Calendar date, int position) {
                if (selected_date.getTimeInMillis() != date.getTimeInMillis()){
                    selected_date = date;
                    loadAvailableTimeSlotOfBarber(Common.currentBarber.getBarberId(),simpleDateFormat.format(date.getTime()));

                }
            }
        });

    }

    @Override
    public void onTimeSlotLoadSuccess(List<TimeSlot> timeSlotList) {

        MyTimeSlotAdapter adapter = new MyTimeSlotAdapter(getActivity(),timeSlotList);
        recycler_time_slot.setAdapter(adapter);
        dialog.dismiss();
    }

    @Override
    public void onTimeSlotLoadFailed(String message) {

        Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
        dialog.dismiss();
        
    }

    @Override
    public void onTimeSlotLoadEmpty() {
        MyTimeSlotAdapter adapter = new MyTimeSlotAdapter(getActivity());
        recycler_time_slot.setAdapter(adapter);
        dialog.dismiss();
    }
}