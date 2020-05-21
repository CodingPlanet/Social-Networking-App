package com.android.messenger.Activites;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.android.messenger.Adapter.DialogGroupName;
import com.android.messenger.Adapter.DialogLoadingAdapter;
import com.android.messenger.Adapter.GroupAllUserAdapter;
import com.android.messenger.Adapter.GroupUserAdapter;
import com.android.messenger.Helpers.CurrentUser;
import com.android.messenger.Helpers.DatabaseRef;
import com.android.messenger.Model.User;
import com.android.messenger.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class NewGroup extends AppCompatActivity {

    // declare all the controls
    Context context;
    Button btnBack,btnNext;
    public static RecyclerView recyclerview_groupuserlist,recyclerview_alluserlist;
    public static List<User> userList = new ArrayList<User>();
    public static List<User> selectedUserList = new ArrayList<User>();
    DialogLoadingAdapter dialogLoadingAdapter;
    public static LinearLayout layout_noResult;
    public static EditText edtSearch;
    public static GroupUserAdapter groupUserAdapter;
    public static GroupAllUserAdapter groupAllUserAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_group);
        init();

        // display the loading adapter
        dialogLoadingAdapter = new DialogLoadingAdapter(context);
        dialogLoadingAdapter.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialogLoadingAdapter.setCancelable(false);
        dialogLoadingAdapter.setCanceledOnTouchOutside(false);
        dialogLoadingAdapter.show();

        // set the layout manager for the recyclerviews vertically and horizontolly
        recyclerview_alluserlist.setLayoutManager(new LinearLayoutManager(context));

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
        linearLayoutManager.setOrientation(RecyclerView.HORIZONTAL);
        recyclerview_groupuserlist.setLayoutManager(linearLayoutManager);


        groupUserAdapter = new GroupUserAdapter(context,selectedUserList);
        recyclerview_groupuserlist.setAdapter(groupUserAdapter);

        groupAllUserAdapter = new GroupAllUserAdapter(context, userList);


        // load all the registered users
        DatabaseRef.dbref_users.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userList.clear();
                long countAllUsers = dataSnapshot.getChildrenCount();
                int counter = 1;

                for (DataSnapshot dataSnapshot_individualUser : dataSnapshot.getChildren()) {
                    User user = dataSnapshot_individualUser.getValue(User.class);
                    if (!user.uid.equals(CurrentUser.userInfo.uid)) {
                        userList.add(user);
                    }
                    if (counter == countAllUsers) {
                        // all the users has been loaded no sort them
                        Collections.sort(userList, new Comparator<User>() {
                            @Override
                            public int compare(User lhs, User rhs) {
                                return lhs.username.compareTo(rhs.username);
                            }
                        });

                        // if no users found then display the no user layout
                        if (userList.size() < 1) {
                            layout_noResult.setVisibility(View.VISIBLE);
                        } else {
                            layout_noResult.setVisibility(View.GONE);
                        }


                        recyclerview_alluserlist.setAdapter(groupAllUserAdapter);
                        groupAllUserAdapter.notifyDataSetChanged();

                        // dismiss the loading adapter
                        dialogLoadingAdapter.dismiss();

                    }

                    counter++;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });




        // track the text changes event to filter the users based on the typed text
        edtSearch.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {

                filterUsers(String.valueOf(s));

            }
        });




        // display the group name dialoge
        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // make sure that atleast 1 user is selected for the group
                if(selectedUserList.size()<1){
                    Toast toast = Toast.makeText(context,"Select group members",Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER,0,0);
                    toast.show();

                }else{

                    DialogGroupName dialogGroupName = new DialogGroupName(context);
                    dialogGroupName.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                    dialogGroupName.setCancelable(false);
                    dialogGroupName.setCanceledOnTouchOutside(false);
                    dialogGroupName.show();


                }


            }
        });


        // go back to the dashboard
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent_dashboard = new Intent(context,Dashboard.class);
                startActivity(intent_dashboard);
                finish();
            }
        });

    }

    // cast all the controls
    private void init(){
        context = this;
        btnBack = (Button)findViewById(R.id.btnBack);
        btnNext = (Button)findViewById(R.id.btnNext);
        recyclerview_groupuserlist = (RecyclerView)findViewById(R.id.recyclerview_groupuserlist);
        recyclerview_alluserlist = (RecyclerView)findViewById(R.id.recyclerview_alluserlist);
        layout_noResult = (LinearLayout)findViewById(R.id.layout_noResult);
        edtSearch = (EditText) findViewById(R.id.edtSearch);
    }



    // filter the users based on the search
    private void filterUsers(String text) {
        ArrayList<User> filteredList = new ArrayList<>();

        for (User users : userList) {
            if (users.username.contains(text.toLowerCase())) {
                filteredList.add(users);
            }
        }
        if (filteredList.size() <= 0) {

            layout_noResult.setVisibility(View.VISIBLE);
        } else {
            layout_noResult.setVisibility(View.GONE);
        }

        GroupAllUserAdapter groupAllUserAdapter = new GroupAllUserAdapter(context, filteredList);
        recyclerview_alluserlist.setAdapter(groupAllUserAdapter);


    }



    // go back to the dashboard
    @Override
    public void onBackPressed() {
        super.onBackPressed();

        Intent intent_dashboard = new Intent(context,Dashboard.class);
        startActivity(intent_dashboard);
        finish();
    }
}
