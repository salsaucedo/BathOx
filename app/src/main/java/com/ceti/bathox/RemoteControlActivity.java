package com.ceti.bathox;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

public class RemoteControlActivity extends AppCompatActivity {

    private static final int TIME_INTERVAL_BETWEEN_BACKS = 2000;
    private long mBackPressed = 0L;

    private SeekBar sbarSpeed;
    private Button btnChooseDevice, btnDisconnect;
    private View whiskerBack, whiskerFront;
    //whiskerFront.setBackgroundColor(getResources().getColor(R.color.notOkRed, null));
    public ListView lvDevices;
    private TextView txtDevice;
    private ProgressDialog progress;
    private RadioGroup toggleTM, toggleTMDir, toggleWP, toggleAM, toggleB;
    private RadioButton rbTMOn, rbTMOff, rbTMFWD, rbTMBCK, rbWPOn, rbWPOff, rbAMOn, rbAMOff, rbBOn, rbBOff;
    private ProgressBar pgBarTankLevel;

    private BluetoothAdapter myBluetooth = null;
    private Set s_pairedDevices;
    BluetoothSocket btSocket = null;
    private volatile boolean isBtConnected = false;
    static final UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    String address = null;
    ConnectedThread connThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_remote_control);
        sbarSpeed = (SeekBar) findViewById(R.id.sbarSpeed);
        btnChooseDevice = (Button) findViewById(R.id.btnConnect);
        btnDisconnect = (Button) findViewById(R.id.btnDisconnect);
        whiskerBack = findViewById(R.id.whiskerBack);
        whiskerFront = findViewById(R.id.whiskerFront);
        txtDevice = (TextView) findViewById(R.id.lblSelectedDevice);
        pgBarTankLevel = (ProgressBar) findViewById(R.id.pgBarTankLevel);
        toggleTM = (RadioGroup) findViewById(R.id.toggleTM);
        toggleTMDir = (RadioGroup) findViewById(R.id.toggleTMDir);
        toggleWP = (RadioGroup) findViewById(R.id.toggleWP);
        toggleAM = (RadioGroup) findViewById(R.id.toggleAM);
        toggleB = (RadioGroup) findViewById(R.id.toggleB);
        rbTMOn = (RadioButton) findViewById(R.id.rbTMOn);
        rbTMOn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                connThread.write("T1:".getBytes());
            }
        });
        rbTMOff = (RadioButton) findViewById(R.id.rbTMOff);
        rbTMOff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                connThread.write("T0:".getBytes());
            }
        });
        rbTMFWD = (RadioButton) findViewById(R.id.rbTMFWD);
        rbTMFWD.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                connThread.write("D0:".getBytes());
            }
        });
        rbTMBCK = (RadioButton) findViewById(R.id.rbTMBCK);
        rbTMBCK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                connThread.write("D1:".getBytes());
            }
        });
        rbAMOn = (RadioButton) findViewById(R.id.rbAMOn);
        rbAMOn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                connThread.write("A1:".getBytes());
                updateUI(3);
            }
        });
        rbAMOff = (RadioButton) findViewById(R.id.rbAMOff);
        rbAMOff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                connThread.write("A0:".getBytes());
                updateUI(2);
            }
        });
        rbWPOn = (RadioButton) findViewById(R.id.rbWPOn);
        rbWPOn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                connThread.write("W1:".getBytes());
            }
        });
        rbWPOff = (RadioButton) findViewById(R.id.rbWPOff);
        rbWPOff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                connThread.write("W0:".getBytes());
            }
        });
        rbBOn = (RadioButton) findViewById(R.id.rbBOn);
        rbBOn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                connThread.write("B1:".getBytes());
            }
        });
        rbBOff = (RadioButton) findViewById(R.id.rbBOff);
        rbBOff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                connThread.write("B0:".getBytes());
            }
        });
        bluetoothInit();
        btnChooseDevice.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                final Dialog dgDevices = new Dialog(RemoteControlActivity.this);
                dgDevices.setContentView(R.layout.device_selector_layout);
                dgDevices.setTitle("Choose a device");
                s_pairedDevices = myBluetooth.getBondedDevices();
                ArrayList al_pairedDevices = new ArrayList();
                if(s_pairedDevices.size() > 0) {
                    for(Object btd : s_pairedDevices) {
                        BluetoothDevice aux = (BluetoothDevice) btd;
                        al_pairedDevices.add(aux.getName() + "\n" + aux.getAddress());
                    }
                }
                else {
                    Toast.makeText(getApplicationContext(), "No paired devices found", Toast.LENGTH_SHORT).show();
                }
                lvDevices = (ListView) dgDevices.findViewById(R.id.lvDevices);
                ArrayAdapter adapter = new ArrayAdapter(RemoteControlActivity.this, android.R.layout.simple_list_item_1, al_pairedDevices);
                lvDevices.setAdapter(adapter);
                lvDevices.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        String info = ((TextView) view).getText().toString();
                        address = info.substring(info.length() - 17);
                        dgDevices.dismiss();
                        txtDevice.setText(address);
                        ConnectBT cbt = new ConnectBT();
                        cbt.execute();
                    }
                });
                dgDevices.show();
            }
        });
        btnDisconnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    btSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                isBtConnected = false;
                updateUI(1);
            }
        });
        sbarSpeed.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                String sbarVal = "P" + sbarSpeed.getProgress() + ":";
                connThread.write(sbarVal.getBytes());
            }
        });
        updateUI(1);
    }

    @Override
    public void onBackPressed() {
        if(mBackPressed + TIME_INTERVAL_BETWEEN_BACKS > System.currentTimeMillis()) {
            moveTaskToBack(true);
            return;
        }
        else {
            Toast.makeText(getApplicationContext(), getResources().getString(R.string.press_back_again_to_exit), Toast.LENGTH_SHORT).show();
        }
        mBackPressed = System.currentTimeMillis();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
    }

    private void bluetoothInit() {
        myBluetooth = BluetoothAdapter.getDefaultAdapter();
        if(myBluetooth == null) {
            //El dispositivo no tiene bluetooth :v
            Toast.makeText(getApplicationContext(), getResources().getString(R.string.no_bluetooth), Toast.LENGTH_LONG).show();
        }
        else {
            if(!myBluetooth.isEnabled()) {
                Intent intTurnBTOn = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(intTurnBTOn, 1);
            }
        }
    }

    private void updateUI(int state) {
        //states 1 = disconnected, 2 = connected, 3 = automatic
        switch (state) {
            case 1:
                txtDevice.setText(getResources().getString(R.string.no_device_selected));
                rbBOn.setEnabled(false);
                rbBOff.setEnabled(false);
                rbTMOn.setEnabled(false);
                rbTMOff.setEnabled(false);
                rbTMFWD.setEnabled(false);
                rbTMBCK.setEnabled(false);
                rbWPOn.setEnabled(false);
                rbWPOff.setEnabled(false);
                rbAMOn.setEnabled(false);
                rbAMOff.setEnabled(false);
                sbarSpeed.setEnabled(false);
                btnChooseDevice.setEnabled(true);
                btnDisconnect.setEnabled(false);
                break;
            case 2:
                rbBOn.setEnabled(true);
                rbBOff.setEnabled(true);
                rbTMOn.setEnabled(true);
                rbTMOff.setEnabled(true);
                rbTMFWD.setEnabled(true);
                rbTMBCK.setEnabled(true);
                rbWPOn.setEnabled(true);
                rbWPOff.setEnabled(true);
                rbAMOn.setEnabled(true);
                rbAMOff.setEnabled(true);
                sbarSpeed.setEnabled(true);
                btnChooseDevice.setEnabled(false);
                btnDisconnect.setEnabled(true);
                /*rbBOff.setChecked(true);
                rbTMOff.setChecked(true);
                rbTMFWD.setChecked(true);
                rbWPOff.setChecked(true);
                sbarSpeed.setProgress(0);*/
                break;
            case 3:
                rbBOn.setEnabled(false);
                rbBOff.setEnabled(false);
                rbTMOn.setEnabled(false);
                rbTMOff.setEnabled(false);
                rbTMFWD.setEnabled(false);
                rbTMBCK.setEnabled(false);
                rbWPOn.setEnabled(false);
                rbWPOff.setEnabled(false);
                break;
            default:
                return;
        }
    }

    public class ConnectBT extends AsyncTask<Void, Void, Void> {

        private boolean connectionSuccess = true;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progress = ProgressDialog.show(RemoteControlActivity.this, getResources().getString(R.string.connecting), getResources().getString(R.string.please_wait));
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try
            {
                if (btSocket == null || !isBtConnected) {
                    myBluetooth = BluetoothAdapter.getDefaultAdapter();
                    BluetoothDevice device = myBluetooth.getRemoteDevice(address);//connects to the device's address and checks if it's available
                    btSocket = device.createInsecureRfcommSocketToServiceRecord(myUUID);//create a RFCOMM (SPP) connection
                    myBluetooth.cancelDiscovery();
                    btSocket.connect();//start connection
                }
            }
            catch (IOException e) {
                connectionSuccess = false;//if the try failed, you can check the exception here
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if(connectionSuccess) {
                Toast.makeText(getApplicationContext(), getResources().getString(R.string.succ_connect), Toast.LENGTH_LONG).show();
                isBtConnected = true;
                updateUI(2);
                connThread = new ConnectedThread(btSocket);
                connThread.execute();
            }
            else {
                Toast.makeText(getApplicationContext(), getResources().getString(R.string.fail_connect), Toast.LENGTH_LONG).show();
                txtDevice.setText(getResources().getString(R.string.no_device_selected));
            }
            progress.dismiss();
        }
    }

    private class ConnectedThread extends AsyncTask<Void, String, Void> {

        private final BluetoothSocket mmSocket;
        private InputStream mmInStream;
        private OutputStream mmOutStream;
        private String aux;

        public ConnectedThread(BluetoothSocket socket) {
            mmSocket = socket;
        }

        /* Call this from the main activity to send data to the remote device */
        public void write(byte[] bytes) {
            try {
                mmOutStream.write(bytes);
            } catch (IOException e) { }
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the input and output streams, using temp objects because
            // member streams are final
            try {
                tmpIn = mmSocket.getInputStream();
                tmpOut = mmSocket.getOutputStream();
            } catch (IOException e) {
                Log.d("BathOx Error", e.getMessage());
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;

            new Thread(new Runnable() {
                @Override
                public void run() {
                    while (isBtConnected) {
                        try {
                            Thread.sleep(150);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        connThread.write("GD:".getBytes());
                    }
                }
            }).start();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            byte[] buffer = new byte[1024];  // buffer store for the stream
            int bytes; // bytes returned from read()
            aux = "";
            // Keep listening to the InputStream until an exception occurs
            while (true) {
                try {
                    // Read from the InputStream
                    bytes = mmInStream.read(buffer);
                    if(bytes != -1) {
                        for (int i = 0; i < bytes; i++) {
                            if (buffer[i] == "#".getBytes()[0]) {
                                publishProgress(aux);
                                aux = "";
                            } else {
                                aux += (char) buffer[i];
                            }
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.d("desconectado", e.getMessage());
                    break;
                }
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
            if (values[0] != null && values[0].length() > 0) {
                try {
                    JSONObject readValues = new JSONObject(values[0]);
                    //readValues.getBoolean("whisk1") ? R.color.notOkRed : R.color.okGreen;
                    whiskerFront.setBackgroundColor(getResources().getColor(readValues.getInt("whisk1") == 1 ? R.color.notOkRed : R.color.okGreen, null));
                    whiskerBack.setBackgroundColor(getResources().getColor(readValues.getInt("whisk2") == 1 ? R.color.notOkRed : R.color.okGreen, null));
                    double sensorVal = readValues.getDouble("sensor") - 400;
                    if (sensorVal > 250) {
                        pgBarTankLevel.setProgress(100);
                    }
                    else if (sensorVal > 0) {
                        pgBarTankLevel.setProgress((int) (sensorVal * (100.0 / 250.0)));
                    }
                    else {
                        pgBarTankLevel.setProgress(0);
                    }
                }
                catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            updateUI(1);
            isBtConnected = false;
            try {
                btSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            Toast.makeText(RemoteControlActivity.this, getResources().getString(R.string.lost_conn), Toast.LENGTH_SHORT).show();
        }
    }

}
