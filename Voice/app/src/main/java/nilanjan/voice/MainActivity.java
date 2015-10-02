/**
 * Author: Nilanjan Daw
 * Dated: January 2015
 * Notes:If you are a normal person stay away from my code.
 *       If you are a coder...then keep your hands off my code
 */
package nilanjan.voice;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.support.v4.widget.DrawerLayout;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.OutputStream;
import java.util.List;
import java.util.Locale;


public class MainActivity extends ActionBarActivity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks, View.OnClickListener {

    private NavigationDrawerFragment mNavigationDrawerFragment;
    public static FragmentManager fragmentManager;

    private CharSequence mTitle;
    private int RESULT=0;
    private Button click;
    private TextView txt;
    private Button connect;
    private Button disconnect;
    private String[] logArray = null;
    private BluetoothComm bt = null;
    private String TAG="MainActivity";
    private TextView logview;
    public OutputStream optstrm;
    private int setBT=0;
    private final int REQUEST_ENABLE_BT=10;
    private TextView lp;
    private int speed=100;
    private int direction=0;
    private TextToSpeech Speak;
    final Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            String data = msg.getData().getString("receivedData");
            addToLog(data);
        }
    };

    //this handler is dedicated to the status of the bluetooth connection
    final Handler handlerStatus = new Handler() {
        public void handleMessage(Message msg) {
            int status = msg.arg1;
            if(status == BluetoothComm.CONNECTED) {
                setBT=1;
                addToLog("Connected");
            } else if(status == BluetoothComm.DISCONNECTED) {
                setBT=0;
                addToLog("Disconnected");
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        fragmentManager=getSupportFragmentManager();
        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();
        Speak =new TextToSpeech(getApplicationContext(),new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status!=TextToSpeech.ERROR) {
                    Speak.setLanguage(Locale.US);
                    Speak.setSpeechRate((float) 0.8);
                    Speak.setPitch((float)0.2);
                }
            }
        });
        click=(Button)findViewById(R.id.button);
        click.setOnClickListener(this);
        txt=(TextView)findViewById(R.id.textView1);
        connect=(Button)this.findViewById(R.id.connect);
        connect.setOnClickListener(this);
        disconnect=(Button)this.findViewById(R.id.disconnect);
        disconnect.setOnClickListener(this);
        logArray=new String[3];
        logview=(TextView)findViewById(R.id.textView);
        lp=(TextView)findViewById((R.id.editText));
        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));
    }

    private void addToLog(String message) {
        System.arraycopy(logArray, 1, logArray, 0, logArray.length - 1);
        logArray[logArray.length - 1] = message;

        logview.setText("");
        for (String aLogArray : logArray) {
            if (aLogArray != null) {
                logview.append(aLogArray + "\n");
            }
        }
        if(setBT==1)
            logview.setBackgroundColor(Color.parseColor("green"));
        else
            logview.setBackgroundColor(Color.parseColor("red"));
    }

    public void onClick(View v){
        if(v==click) {
            /*tv.setVisibility(View.VISIBLE);
            Log.d("Clicked?", "" + tv.requestFocus());
            tv.setVisibility(View.INVISIBLE);*/
            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                    RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            startActivityForResult(intent, RESULT);
        }
        if (v == connect) {
            addToLog("Trying to connect");
            EditText txt=(EditText)findViewById(R.id.btaddress);
            optstrm=bt.connect(txt.getText().toString());
            //setBT=1;
        } else if (v == disconnect) {
            addToLog("closing connection");
            bt.close();
            // setBT=0;
        }
    }
    @Override
    public void onResume(){
        super.onResume();
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            // Device does not support Bluetooth
            Log.d(TAG, "Device does not support Bluetooth");
        }
        else{
            //Device supports BT
            if (!mBluetoothAdapter.isEnabled()){
                //if Bluetooth not activated, then request it
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }
            else{
                //BT activated, then initiate the BtInterface object to handle all BT communication
                bt = new BluetoothComm(handlerStatus, handler);
            }
        }
    }

    private void parseText(String x){
        String feedback="";
        byte sendBuffer[]=new byte[3];
        sendBuffer[0] = (byte) 0;

        if (x.contains("hi") ||x.contains("hey") || x.contains("hello"))
            feedback="Hey! Whats up!";
        if (x.contains("name")  && (x.contains("what") || x.contains("what's")|| x.contains("tell")))
            feedback="My name is Scooby.What's yours?";
        if(x.contains("left")) {
            if(x.contains("hard")) {
                feedback="Banking left. Hold ON!";
                sendBuffer[0] = 3;
            }
            else {
                feedback="Going left, because women, are always right";
                sendBuffer[0] = 4;
                direction = (int) sendBuffer[0];
            }
        }
        if(x.contains("right")) {

            if(x.contains("hard")) {
                feedback="Banking right. Hold ON!";
                sendBuffer[0] = 5;
            }
            else {
                feedback="Going right Broom! broom!";
                sendBuffer[0] = 6;
                direction = (int) sendBuffer[0];
            }
        }
        if(x.contains("forward") || (x.contains("up") && (!x.contains("hurry") && !x.contains("speed"))) || x.contains("ahead")) {
            feedback="Moving Forward.Left! Right! Left!.left! right! left!";
            sendBuffer[0] = (byte) 1;
            direction=1;
        }
        if(x.contains("backward") || x.contains("back")) {
            feedback="Move move move....Coming Back!!!";
            sendBuffer[0] = (byte) 2;
            direction=2;
        }
        if(x.contains("hurry")|| (x.contains("hurry") && x.contains("up"))) {
            if(speed<255) {
                feedback="Speeding Up! Fasten your seat belts!";
                speed += 50;
                if(speed>255)
                    speed=255;
                sendBuffer[0]=(byte)direction;
            }
            else
                feedback="Speed thrills but kills";
        }
        if(x.contains("slow")) {
            if (speed > 100) {
                feedback="Slowing down...";
                speed -= 50;
                if(speed<50)
                    speed=50;
                sendBuffer[0]=(byte)direction;
            }
            else
                feedback="Dude, I am moving like a snail!";
        }
        if (x.contains("stop") || x.contains("halt") || x.contains("hold"))
        {
            feedback="Breaking bad!!!sorry for the pun, couldn't help it.";
            direction=0;
            sendBuffer[0]=(byte)0;
        }
        speakText(feedback);
        lp.setText(feedback+" direction "+sendBuffer[0]+" speed "+speed);
        sendBuffer[1]=(byte)speed;
        sendBuffer[2]=(byte)'#';
        if(setBT==1 && bt!=null)
        {
            Log.d("MainActivity","Sending");
            if(optstrm!=null) {
                Log.d("MainActivity", optstrm.toString());
                bt.sendData(sendBuffer,optstrm);
            }
            else
                Log.d(TAG,"MainActivity stream null");

        }

    }

    private void speakText(String x){
        Speak.speak(x,TextToSpeech.QUEUE_FLUSH,null);
    }

    protected void onActivityResult(int requestCode, int resultCode,Intent data) {
        if (requestCode == RESULT && resultCode == RESULT_OK) {
            List<String> results = data.getStringArrayListExtra(
                    RecognizerIntent.EXTRA_RESULTS);
            String spokenText = results.get(0);
            txt.setText(spokenText);
            parseText(spokenText);
        }
        if (requestCode == REQUEST_ENABLE_BT){
            if (resultCode == Activity.RESULT_OK){
                //BT activated, then initiate the BtInterface object to handle all BT communication
                bt = new BluetoothComm(handlerStatus, handler);
            }
            else if (resultCode == Activity.RESULT_CANCELED)
                Log.d(TAG, "BT not activated");
            else
                Log.d(TAG, "result code not known");
        }
        else{
            Log.d(TAG, "request code not known");
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this).setTitle("Exit").setMessage("Are you sure you want to exit?")
                .setNegativeButton("No", null).setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.addCategory(Intent.CATEGORY_HOME);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        }).create().show();
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        // update the main content by replacing fragments
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.container, PlaceholderFragment.newInstance(position + 1))
                .commit();
    }

    public void onSectionAttached(int number) {
        switch (number) {
            case 1:
                mTitle = getString(R.string.title_section1);
                break;
            case 2:
                mTitle = getString(R.string.title_section2);
                break;
            case 3:
                mTitle = getString(R.string.title_section3);
                break;
        }
    }

    public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            getMenuInflater().inflate(R.menu.main, menu);
            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            return inflater.inflate(R.layout.fragment_main, container, false);
        }

        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);
            ((MainActivity) activity).onSectionAttached(
                    getArguments().getInt(ARG_SECTION_NUMBER));
        }
    }

}
