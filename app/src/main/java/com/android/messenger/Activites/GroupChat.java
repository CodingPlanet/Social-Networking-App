package com.android.messenger.Activites;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.android.messenger.Adapter.AllMsgAdapter;
import com.android.messenger.Adapter.DialogImageUploadAdapter;
import com.android.messenger.Adapter.DialogLoadingAdapter;
import com.android.messenger.Helpers.CurrentUser;
import com.android.messenger.Helpers.DatabaseRef;
import com.android.messenger.Model.ChatInfo;
import com.android.messenger.Model.Message;
import com.android.messenger.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

import de.hdodenhof.circleimageview.CircleImageView;

public class GroupChat extends AppCompatActivity {

    // decalre all the controls to cast
    ChatInfo chatInfo;
    Context context;
    TextView txtgroupname;
    CircleImageView profileimageview;
    Button btnCancel, btnSelectImage;
    EditText edt_msg;
    ImageView imgview_msgsend;
    RecyclerView recyclerView_chat;
    List<String> userUsernameList = new ArrayList<String>();

    long timestamp;
    Calendar calendar;
    List<Message> messageList = new ArrayList<Message>();
    List<String> membersUidList = new ArrayList<String>();
    List<String> adminsUidList = new ArrayList<String>();


    private final int PICK_IMAGE_REQUEST = 71;

    StorageReference ref;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat);

        // cast all the controls
        init();

        // get the group info from the intent
        chatInfo = (ChatInfo) getIntent().getSerializableExtra("group");

        // set the layout manager for the recyclerview
        recyclerView_chat.setLayoutManager(new LinearLayoutManager(context));

        // set the storage reference that will point to the receiver profile image
        StorageReference storageReference = FirebaseStorage.getInstance().getReference(chatInfo.chatId);

        // set the group name
        txtgroupname.setText(chatInfo.receiverUid);
        // set the profile image for the group
        Glide.with(context).load(storageReference).placeholder(R.drawable.default_group_profile).into(profileimageview);

        // show the loading adapter
        DialogLoadingAdapter dialogLoadingAdapter = new DialogLoadingAdapter(context);
        dialogLoadingAdapter.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialogLoadingAdapter.setCancelable(false);
        dialogLoadingAdapter.setCanceledOnTouchOutside(false);
        dialogLoadingAdapter.show();

        // get all the members for this group
        DatabaseRef.dbref_groupinfo.child(chatInfo.chatId).child("members").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                membersUidList.clear();
                for (DataSnapshot dataSnapshot_individualUser : dataSnapshot.getChildren()) {
                    membersUidList.add(dataSnapshot_individualUser.getKey());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        // get all the admins for this group
        DatabaseRef.dbref_groupinfo.child(chatInfo.chatId).child("info").child("admins").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                adminsUidList.clear();
                for (DataSnapshot dataSnapshot_individualUser : dataSnapshot.getChildren()) {
                    adminsUidList.add(dataSnapshot_individualUser.getKey());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        // dismiss the loading dialoge
        dialogLoadingAdapter.dismiss();


        // loadi all the messege for the group
        DatabaseRef.dbref_messages.child(chatInfo.chatId).addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                    Message message = dataSnapshot.getValue(Message.class);
                    messageList.add(message);

                    AllMsgAdapter allMsgAdapter = new AllMsgAdapter(context, messageList);
                    recyclerView_chat.setAdapter(allMsgAdapter);
                    recyclerView_chat.scrollToPosition(messageList.size() - 1);

                }

                @Override
                public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                }

                @Override
                public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

                }

                @Override
                public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });


        // send the messege
        imgview_msgsend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // get the messge to send
                String msg = edt_msg.getText().toString().trim();

                // check if the messge is empty then don't do anything
                if (!msg.isEmpty()) {

                    //get the current itmestamp from th system in UTC
                    calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
                    timestamp = calendar.getTimeInMillis();

                    // get the unique id for each message
                    String messageId = DatabaseRef.dbref_messages.push().getKey();

                    // make object of chatInfo to display information in inbox
                    ChatInfo chatInfo1 = new ChatInfo(chatInfo.chatId, msg, "text", CurrentUser.userInfo.uid, chatInfo.receiverUid, timestamp, true);

                    // create the messge class object to save all the information realted to this message on databse
                    Message message = new Message(messageId, msg, "grouptext", CurrentUser.userInfo.uid, CurrentUser.userInfo.username,"", chatInfo.chatId, timestamp);

                    // save messge to databse into the group
                    DatabaseRef.dbref_messages.child(chatInfo.chatId).child(messageId).setValue(message);

                    // update information for the inbox
                    DatabaseRef.dbref_chatinfo.child(chatInfo.chatId).setValue(chatInfo1);

                    // update information of the current user inbox who is sending the message
                    DatabaseRef.dbref_inbox.child(CurrentUser.userInfo.uid).child(chatInfo.chatId).setValue(timestamp);

                    // update information of all the group members
                    for (String uid : membersUidList) {
                        DatabaseRef.dbref_inbox.child(uid).child(chatInfo.chatId).setValue(timestamp);
                    }

                    // reset the edit text for new messege
                    edt_msg.setText("");


                }
            }
        });



        // select the image to send
        btnSelectImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                // check if the permission granted to read the external storage or not
                int checkVal = context.checkCallingOrSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE);
                if (checkVal == PackageManager.PERMISSION_GRANTED) {
                    // if permission is granted the choose the image
                    chooseImage();
                } else {
                    // else ask for the permission
                    requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                            123);
                }



            }
        });




        // go back to the dashboard
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent_dashboard = new Intent(context, Dashboard.class);
                startActivity(intent_dashboard);
                finish();
            }
        });


    }


    // cast all the controls
    private void init() {
        context = this;


        txtgroupname = (TextView) findViewById(R.id.txtgroupname);
        profileimageview = (CircleImageView) findViewById(R.id.profileimageview);
        btnCancel = (Button) findViewById(R.id.btnCancel);
        btnSelectImage = (Button) findViewById(R.id.btnSelectImage);
        edt_msg = (EditText) findViewById(R.id.edt_msg);
        imgview_msgsend = (ImageView) findViewById(R.id.imgview_msgsend);
        recyclerView_chat = (RecyclerView) findViewById(R.id.recyclerView_chat);


    }

    // choose the image from gallary
    private void chooseImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    // this will run when image has been selected from the storage and it will hold the selected image
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null) {
            Uri filePath = data.getData();
            uploadImage(filePath);

        }


    }


    // upload image to the database
    private void uploadImage(Uri filePath) {

        if (filePath != null) {

            // show the image uploading dialoge
            final DialogImageUploadAdapter dialogImageUploadAdapter = new DialogImageUploadAdapter(context);
            dialogImageUploadAdapter.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialogImageUploadAdapter.setCancelable(false);
            dialogImageUploadAdapter.setCanceledOnTouchOutside(false);
            dialogImageUploadAdapter.show();



            // get the current timestamp in UTC
            calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
            timestamp = calendar.getTimeInMillis();

            // create the unique id for this image message
            final String messageId = DatabaseRef.dbref_messages.push().getKey();


            //upload image to the database

            ref = DatabaseRef.storageReference.child("images/" + messageId);
            ref.putFile(filePath)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {


                            ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    Uri downloadUrl = uri;


                                    final ChatInfo chatInfo1 = new ChatInfo(chatInfo.chatId, "Image", "groupimage", CurrentUser.userInfo.uid, chatInfo.receiverUid, timestamp, true);
                                    final Message message = new Message(messageId, downloadUrl.toString(), "groupimage", CurrentUser.userInfo.uid, CurrentUser.userInfo.username,"", chatInfo.chatId, timestamp);

                                    DatabaseRef.dbref_messages.child(chatInfo.chatId).child(messageId).setValue(message);
                                    DatabaseRef.dbref_chatinfo.child(chatInfo.chatId).setValue(chatInfo1);
                                    DatabaseRef.dbref_inbox.child(CurrentUser.userInfo.uid).child(chatInfo.chatId).setValue(timestamp);

                                    for (String uid : membersUidList) {
                                        DatabaseRef.dbref_inbox.child(uid).child(chatInfo.chatId).setValue(timestamp);
                                    }

                                    dialogImageUploadAdapter.dismiss();
                                }
                            });






                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            dialogImageUploadAdapter.dismiss();
                            Toast.makeText(context, "Failed " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot
                                    .getTotalByteCount());
                            dialogImageUploadAdapter.txtProgress.setText((int) progress + "%");
                        }
                    });
        }
    }


}
