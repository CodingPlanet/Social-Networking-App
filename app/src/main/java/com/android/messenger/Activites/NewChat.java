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
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.android.messenger.Adapter.DialogLoadingAdapter;
import com.android.messenger.Adapter.UserSearchAdapter;
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

import de.hdodenhof.circleimageview.CircleImageView;

public class NewChat extends AppCompatActivity {

    // decalre all the controls
    DialogLoadingAdapter dialogLoadingAdapter;
    Context context;
    List<User> userList = new ArrayList<User>();
    RecyclerView recyclerViewAllUsers;
    LinearLayout layout_noResult;
    EditText edtSearch;
    CircleImageView profileimageview;
    TextView txtusername;
    Button btnCancel, btnCreateGroup;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_chat);
        // cast all the controls
        init();

        // set the username of current user
        txtusername.setText(CurrentUser.userInfo.username);

        // set the profile pic of the current user using the library glide
        if (!CurrentUser.userInfo.profileImageUrl.equals("default")) {
            Glide.with(context).load(CurrentUser.userInfo.profileImageUrl).placeholder(R.drawable.default_user_img).into(profileimageview);
        }


        // show the loading adapter
        dialogLoadingAdapter.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialogLoadingAdapter.setCancelable(false);
        dialogLoadingAdapter.setCanceledOnTouchOutside(false);
        dialogLoadingAdapter.show();

        // set the layout manager for the recyclerview
        recyclerViewAllUsers.setLayoutManager(new LinearLayoutManager(context));

        // load all the registered users for the app
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
                        // at this point all the users hasbeen loaded into the database no sort them in ascending order
                        Collections.sort(userList, new Comparator<User>() {
                            @Override
                            public int compare(User lhs, User rhs) {
                                return lhs.username.compareTo(rhs.username);
                            }
                        });

                        // if no users found then display the no user found layout
                        if (userList.size() < 1) {
                            layout_noResult.setVisibility(View.VISIBLE);
                        } else {
                            layout_noResult.setVisibility(View.GONE);
                        }

                        // add all the users to recyclerview using the adapter
                        UserSearchAdapter userSearchAdapter = new UserSearchAdapter(context, userList);
                        recyclerViewAllUsers.setAdapter(userSearchAdapter);
                        userSearchAdapter.notifyDataSetChanged();

                        // dismiss the dialoge
                        dialogLoadingAdapter.dismiss();

                    }

                    counter++;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        // track the text changes in the textfield
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

        // go back to the dashboard
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // open the newgroup activity
        btnCreateGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent_newgroup = new Intent(context,NewGroup.class);
                startActivity(intent_newgroup);
                finish();
            }
        });


    }

    // cast all the controls
    private void init() {
        context = this;
        dialogLoadingAdapter = new DialogLoadingAdapter(context);
        recyclerViewAllUsers = (RecyclerView) findViewById(R.id.recyclerViewAllUsers);
        layout_noResult = (LinearLayout) findViewById(R.id.layout_noResult);
        edtSearch = (EditText) findViewById(R.id.edtSearch);
        profileimageview = (CircleImageView) findViewById(R.id.profileimageview);
        txtusername = (TextView) findViewById(R.id.txtusername);
        btnCancel = (Button) findViewById(R.id.btnCancel);
        btnCreateGroup = (Button) findViewById(R.id.btnCreateGroup);
    }


    // display the filterd users based on search
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

        UserSearchAdapter userSearchFilteredAdapter = new UserSearchAdapter(context, filteredList);
        recyclerViewAllUsers.setAdapter(userSearchFilteredAdapter);


    }


}
