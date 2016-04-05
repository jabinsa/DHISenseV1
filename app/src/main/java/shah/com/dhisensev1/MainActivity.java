package shah.com.dhisensev1;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.felhr.usbserial.UsbSerialDevice;
import com.felhr.usbserial.UsbSerialInterface;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;

public class MainActivity extends AppCompatActivity {


    private UsbService usbService;

    private TextView display,statxt,devtxt,firtxt,battxt,sertxt;
    private EditText editText;
    private Button sendButton;
    int x;
    String s,appendS;
    StringBuilder stringb;
    ArrayList aList;
    private MyHandler mHandler;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_main);

        mHandler = new MyHandler(this);

        display = (TextView) findViewById(R.id.dataTxt);
        editText = (EditText) findViewById(R.id.editText);
        sendButton = (Button) findViewById(R.id.btncnt);

        statxt = (TextView) findViewById(R.id.status_txt);
        devtxt = (TextView) findViewById(R.id.device_txt);
        sertxt = (TextView) findViewById(R.id.serial_txt);
        firtxt = (TextView) findViewById(R.id.firmv_txt);
        battxt = (TextView) findViewById(R.id.batt_txt);


    }


    public void onStartService(View view) {


        Intent myIntent = new Intent("shah.com.dhisensev1.Download");
        startActivity(myIntent);

    }

    public void onClickSetting(View view) {


        Intent myIntent2 = new Intent("shah.com.dhisensev1.SettingActivity");
        startActivity(myIntent2);

    }

    public void onClickConnect(View view) {


            String data = "1";
            if(usbService != null) // if UsbService was correctly binded, Send data
                usbService.write(data.getBytes());

    }

    public void onStopService(View view) {




    }

    @Override
    public void onResume()
    {
        super.onResume();
        setFilters();  // Start listening notifications from UsbService
        startService(UsbService.class, usbConnection, null); // Start UsbService(if it was not started before) and Bind it
    }

    @Override
    public void onPause()
    {
        super.onPause();
        unregisterReceiver(mUsbReceiver);
        unbindService(usbConnection);
    }

    private void setFilters()
    {
        IntentFilter filter = new IntentFilter();
        filter.addAction(UsbService.ACTION_USB_PERMISSION_GRANTED);
        filter.addAction(UsbService.ACTION_NO_USB);
        filter.addAction(UsbService.ACTION_USB_DISCONNECTED);
        filter.addAction(UsbService.ACTION_USB_NOT_SUPPORTED);
        filter.addAction(UsbService.ACTION_USB_PERMISSION_NOT_GRANTED);
        registerReceiver(mUsbReceiver, filter);
    }

    private void startService(Class<?> service, ServiceConnection serviceConnection, Bundle extras)
    {
        if(UsbService.SERVICE_CONNECTED == false)
        {
            Intent startService = new Intent(this, service);
            if(extras != null && !extras.isEmpty())
            {
                Set<String> keys = extras.keySet();
                for(String key: keys)
                {
                    String extra = extras.getString(key);
                    startService.putExtra(key, extra);
                }
            }
            startService(startService);
        }
        Intent bindingIntent = new Intent(this, service);
        bindService(bindingIntent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    private static class MyHandler extends Handler
    {
        private final WeakReference<MainActivity> mActivity;

        public MyHandler(MainActivity activity)
        {
            mActivity = new WeakReference<MainActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg)
        {
             int x;

            switch(msg.what)
            {
                case UsbService.MESSAGE_FROM_SERIAL_PORT:
                    String data = (String) msg.obj;
                    mActivity.get().display.append(data);


                    x = data.indexOf("V");
                    if(x>0) {
                        mActivity.get().splitAppend();
                        //splitAppend();

                    }

                    break;
            }
        }
    }

    private void splitAppend() {
      //  final TextView ftv = tv;
//        final String ftext = text;


          runOnUiThread(new Runnable() {
            @Override
            public void run() {

                statxt.setText("Connected");

               s = display.getText().toString();
               aList = new ArrayList(Arrays.asList(s.split(",")));
               devtxt.setText(aList.get(1).toString());
               sertxt.setText(aList.get(2).toString());
                firtxt.setText(aList.get(3).toString());
                battxt.setText(aList.get(4).toString());


            }
        });
    }

    private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context arg0, Intent arg1)
        {
            if(arg1.getAction().equals(UsbService.ACTION_USB_PERMISSION_GRANTED)) // USB PERMISSION GRANTED
            {
                Toast.makeText(arg0, "USB Ready", Toast.LENGTH_SHORT).show();
            }else if(arg1.getAction().equals(UsbService.ACTION_USB_PERMISSION_NOT_GRANTED)) // USB PERMISSION NOT GRANTED
            {
                Toast.makeText(arg0, "USB Permission not granted", Toast.LENGTH_SHORT).show();
            }else if(arg1.getAction().equals(UsbService.ACTION_NO_USB)) // NO USB CONNECTED
            {
                Toast.makeText(arg0, "No USB connected", Toast.LENGTH_SHORT).show();
            }else if(arg1.getAction().equals(UsbService.ACTION_USB_DISCONNECTED)) // USB DISCONNECTED
            {
                Toast.makeText(arg0, "USB disconnected", Toast.LENGTH_SHORT).show();
            }else if(arg1.getAction().equals(UsbService.ACTION_USB_NOT_SUPPORTED)) // USB NOT SUPPORTED
            {
                Toast.makeText(arg0, "USB device not supported", Toast.LENGTH_SHORT).show();
            }
        }
    };

    private final ServiceConnection usbConnection = new ServiceConnection()
    {
        @Override
        public void onServiceConnected(ComponentName arg0, IBinder arg1)
        {
            usbService = ((UsbService.UsbBinder) arg1).getService();
            usbService.setHandler(mHandler);
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0)
        {
            usbService = null;
        }
    };
}
