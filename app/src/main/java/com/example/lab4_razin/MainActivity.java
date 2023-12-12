package com.example.lab4_razin;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.Firebase;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kotlin.collections.ArrayDeque;

public class MainActivity extends AppCompatActivity {
    EditText editTextName;
    Button buttonAdd;

    DatabaseReference databaseArtists;

    List<Artist> artistList;
    ListView listViewArtists;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FirebaseApp.initializeApp(this);

        databaseArtists = FirebaseDatabase.getInstance().getReference("artists");

        editTextName = (EditText) findViewById(R.id.editTextName);
        buttonAdd = (Button) findViewById(R.id.buttonAddArtist);

        listViewArtists = (ListView) findViewById(R.id.ListViewArtist);
        artistList = new ArrayList<>();
        buttonAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addArtist();
            } });

        listViewArtists.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int i, long l) {
                Artist artist= artistList.get(i);

                showUpdateDialog(artist.getArtistId(), artist.getArtistName());

                return false;
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        //attaching value event listener
        databaseArtists.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                //clearing the previous artist list
                artistList.clear();

                //iterating through all the nodes
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    //getting artist
                    Artist artist = postSnapshot.getValue(Artist.class);
                    //adding artist to the list
                    artistList.add(artist);
                }

                //creating adapter
                ArtistList artistAdapter = new ArtistList(MainActivity.this, artistList);
                //attaching adapter to the listview
                listViewArtists.setAdapter(artistAdapter);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void showUpdateDialog(String artistId, String artistName) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);

        LayoutInflater inflater = getLayoutInflater();

        final View dialogView = inflater.inflate(R.layout.update_dialog, null);

        dialogBuilder.setView(dialogView);

        final EditText editTextName = (EditText)  dialogView.findViewById(R.id.editTextName);
        final Button buttonUpdate = (Button)  dialogView.findViewById(R.id.buttonUpdate);
        final Button buttonDelete = (Button)  dialogView.findViewById(R.id.buttonDelete);

        dialogBuilder.setTitle("Updating Artist "+artistName);

        final AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.show();

        editTextName.setText(artistName);

        buttonUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name = editTextName.getText().toString().trim();

                if(TextUtils.isEmpty(name)){
                    editTextName.setError("Name required");
                    return;
                }

                updateArtist(artistId, name);

                alertDialog.dismiss();
            }
        });

        buttonDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteArtist(artistId);
            }
        });
    }

    private void deleteArtist(String artistId) {
        DatabaseReference drArtist = FirebaseDatabase.getInstance().getReference("artists").child(artistId);

        drArtist.removeValue();

        Toast.makeText(this, "Artists deleted", Toast.LENGTH_LONG).show();
    }

    private boolean updateArtist(String id, String name) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("artists").child(id);

        // Create a map to update only the 'name' field
        Map<String, Object> updateMap = new HashMap<>();
        updateMap.put("artistName", name);

        databaseReference.updateChildren(updateMap);

        Toast.makeText(this, "Artist updated successfully!", Toast.LENGTH_LONG).show();

        return true;
    }

    private void addArtist() {
        //get artistname and convert to string from editextname
        String name = editTextName.getText().toString().trim();

        //check if the name is not empty
        if (!TextUtils.isEmpty(name)) {
            //if exist push data to firebase database
            //store inside id in database
            //every time data stored the id will be unique
            String id = databaseArtists.push().getKey();
            //store
            Artist artist = new Artist(id, name);
            //store artist inside unique id
            databaseArtists.child(id).setValue(artist);
            Toast.makeText(this, "Artist added", Toast.LENGTH_LONG).show();

            } else {
                //if the name is empty
                //if the value is not given displaying a toast
                Toast.makeText(this, "Please enter a name", Toast.LENGTH_LONG).show();
            }
    }
}