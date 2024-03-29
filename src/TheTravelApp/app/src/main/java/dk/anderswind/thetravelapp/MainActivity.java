package dk.anderswind.thetravelapp;

import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.database.Cursor;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private final String LAST_START = IntentKeys.PACKAGE_NAME + "LastStart";
    private final String LAST_END = IntentKeys.PACKAGE_NAME + "LastEnd";
    private final String IS_CHECKING_IN = IntentKeys.PACKAGE_NAME + "isCheckingIn";

    private TextView checkInStation;
    private TextView checkOutStation;
    private Button checkInButton;
    private Button checkOutButton;
    private Button selectCheckInButton;
    private Button selectCheckOutButton;

    private String startDestination;
    private Location startLocation;
    private String endDestination;
    private Location destinationLocation;
    private boolean isCheckingIn = true;

    private TravelDAO dbAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        dbAdapter = new TravelDAO(this);

        checkInButton = (Button) findViewById(R.id.checkInButton);
        checkOutButton = (Button) findViewById(R.id.checkOutButton);
        selectCheckInButton = (Button) findViewById(R.id.selectCheckInButton);
        selectCheckOutButton = (Button) findViewById(R.id.selectCheckOutButton);
        ImageView logoImage = (ImageView) findViewById(R.id.imageView);
        checkInStation = (TextView) findViewById(R.id.checkInStation);
        checkOutStation = (TextView) findViewById(R.id.checkOutStation);

        logoImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, WebInfoActivity.class);
                startActivity(intent);
            }
        });

        checkInButton.setOnClickListener(new CheckInButton(this));
        checkOutButton.setOnClickListener(new CheckOutButton(this));
        selectCheckInButton.setOnClickListener(new SelectStationButton(0));
        selectCheckOutButton.setOnClickListener(new SelectStationButton(1));
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        startDestination = savedInstanceState.getString(LAST_START);
        endDestination = savedInstanceState.getString(LAST_END);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(LAST_START, startDestination);
        outState.putString(LAST_END, endDestination);
        outState.putBoolean(IS_CHECKING_IN, isCheckingIn);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuItem history = menu.add(Menu.NONE, Menu.FIRST, Menu.NONE, "History");
        MenuItem settings = menu.add(Menu.NONE, 2, Menu.NONE, "Settings");
        MenuItem invite = menu.add(Menu.NONE, 3, Menu.NONE, "Invite friends");
        MenuItem notify = menu.add(Menu.NONE, 4, Menu.NONE, "Notify a friend");
        history.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        settings.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        invite.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        notify.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == Menu.FIRST)
        {
            Intent intent = new Intent(MainActivity.this, TravelsListView.class);
            startActivity(intent);
        }
        else if (item.getItemId() == 2)
        {
            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(intent);
        }
        else if (item.getItemId() == 3)
        {
            Intent intent = new Intent(MainActivity.this, InviteActivity.class);
            startActivity(intent);
        }
        else if (item.getItemId() == 4)
        {
            if (PermissionRequester.maySendSms(this))
            {
                Uri contentUri = Uri.parse("content://sms/inbox");
                CursorLoader sms = new CursorLoader(this, contentUri, null, "body = \"" + getResources().getString(R.string.invite_sms_text) + "\"", null, null);
                Cursor cursor = sms.loadInBackground();
                if(cursor.getCount() > 0)
                {
                    cursor.moveToFirst();
                    String address = cursor.getString(cursor.getColumnIndex("address"));
                    SmsManager smsManager = SmsManager.getDefault();
                    try {
                        smsManager.sendTextMessage(address, null, getResources().getString(R.string.invitation_notation_sms_text), null, null);
                        Toast.makeText(this, "Yay now the inviter knows he invited you", Toast.LENGTH_SHORT).show();
                    } catch (Exception e)
                    {
                        Toast.makeText(this, "Something went wrong with notifying the inviter", Toast.LENGTH_SHORT).show();
                    }

                }
                else
                    Toast.makeText(this, "You have not been invited", Toast.LENGTH_SHORT).show();

            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK)
        {
            String result = data.getStringExtra(IntentKeys.SELECTED_STATION_NAME);
            if(requestCode == 0)
            {
                checkInStation.setText(result);
            }
            else if (requestCode == 1)
            {
                checkOutStation.setText(result);
            }
        }
    }

    // This is here to allow the innerclass to make toasts.
    private void informUser(String message){
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void changeToCheckIn(boolean yes)
    {
        isCheckingIn = yes;
        checkInButton.setEnabled(yes);
        checkInStation.setEnabled(yes);
        findViewById(R.id.selectCheckInButton).setEnabled(yes);
        checkOutButton.setEnabled(!yes);
        checkOutStation.setEnabled(!yes);
        findViewById(R.id.selectCheckOutButton).setEnabled(!yes);
    }


    class SelectStationButton implements View.OnClickListener
    {
        int id;
        public SelectStationButton(int id)
        {
            this.id = id;
        }

        @Override
        public void onClick(View view) {
            Intent intent = new Intent(MainActivity.this, StationsListView.class);
            startActivityForResult(intent, id);
        }
    }
    class CheckInButton implements View.OnClickListener
    {
        private Context context;

        public CheckInButton(Context context) {
            this.context = context;
        }

        @Override
        public void onClick(View view) {
            String station = checkInStation.getText().toString();
            if(Geocoder.isPresent())
            {
                Geocoder geocoder = new Geocoder(context);
                try{
                    List<Address> addressList = geocoder.getFromLocationName(station, 1);
                    if(addressList.size()>0)
                    {
                        Address a = addressList.get(0);
                        startLocation = new Location(a.getAddressLine(0));
                    }
                } catch(IOException e)
                {

                }
            }
            if(!TextUtils.isEmpty(station))
            {
                changeToCheckIn(false);
                dbAdapter.open();
                dbAdapter.saveStation(station);
                dbAdapter.close();
            }else
            {
                checkInStation.setError("Please enter a station");
            }
        }
    }

    class CheckOutButton implements View.OnClickListener
    {
        private Context context;

        public CheckOutButton(Context context) {
            this.context = context;
        }

        @Override
        public void onClick(View view) {
            String station = checkOutStation.getText().toString();
            if(Geocoder.isPresent())
            {
                Geocoder geocoder = new Geocoder(context);
                try{
                    List<Address> addressList = geocoder.getFromLocationName(station, 1);
                    if(addressList.size()>0)
                    {
                        Address a = addressList.get(0);
                        destinationLocation = new Location(a.getAddressLine(0));
                    }
                } catch(IOException e)
                {

                }
            }
            if(!TextUtils.isEmpty(station))
            {
                float distance = 0;
                if(destinationLocation != null && startLocation != null) {
                    distance = destinationLocation.distanceTo(startLocation) / 1000;
                }
                changeToCheckIn(true);

                startDestination = checkInStation.getText().toString();
                endDestination = station;

                checkInStation.setText("");
                checkOutStation.setText("");
                startLocation = null;
                destinationLocation = null;

                informUser("Travel finished");

                dbAdapter.open();
                dbAdapter.saveStation(station);
                dbAdapter.saveTravels(startDestination, endDestination, distance);
                dbAdapter.close();
            }else
            {
                checkOutStation.setError("Please enter a station");
            }
        }
    }


}



