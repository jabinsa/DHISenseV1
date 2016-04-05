package shah.com.dhisensev1;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Set;

public class Downlaod extends AppCompatActivity {

    TextView datatxt;
    EditText tx;
    Button b;
    static final int READ_BLOCK_SIZE = 100;
    File docsFolder;
    String filename;
     TextView display;
    private UsbService usbService;

    private MyHandler mHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_downlaod);

        tx=(EditText)findViewById(R.id.editText);
        b=(Button)findViewById(R.id.btnsave);
        display = (TextView) findViewById(R.id.dataview);

        mHandler = new MyHandler(this);

                                                                                        //set File name

        docsFolder = new File(Environment.getExternalStorageDirectory() + "/DHISense Data");
        boolean isPresent = true;
        if (!docsFolder.exists()) {
            isPresent = docsFolder.mkdirs();
            Toast.makeText(getBaseContext(), docsFolder.toString(),Toast.LENGTH_SHORT).show();
        }

        Date date = new Date() ;
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss") ;

        filename = docsFolder +"/"+dateFormat.format(date) + ".txt";
        Toast.makeText(getBaseContext(), filename.toString(),Toast.LENGTH_SHORT).show();

        tx.setText(filename);



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
        private final WeakReference<Downlaod> mActivity;

        public MyHandler(Downlaod activity)
        {
            mActivity = new WeakReference<Downlaod>(activity);
        }

        @Override
        public void handleMessage(Message msg)
        {
            switch(msg.what)
            {
                case UsbService.MESSAGE_FROM_SERIAL_PORT:
                    String data = (String) msg.obj;
                    mActivity.get().display.append(data);
                    break;
            }
        }
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



    public void write(View v) {
        // add-write text into file

      /*   docsFolder = new File(Environment.getExternalStorageDirectory() + "/DHISense Data");
        boolean isPresent = true;
        if (!docsFolder.exists()) {
            isPresent = docsFolder.mkdirs();
            Toast.makeText(getBaseContext(), docsFolder.toString(),Toast.LENGTH_SHORT).show();
        }
*/
        try {
            String storageState = Environment.getExternalStorageState();
            if (storageState.equals(Environment.MEDIA_MOUNTED)) {


            /*    Date date = new Date() ;
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss") ;

                 filename = docsFolder +"/"+dateFormat.format(date) + ".txt";
                Toast.makeText(getBaseContext(), filename.toString(),Toast.LENGTH_SHORT).show();
*/
                String newfilename=tx.getText().toString();

                File file = new File(newfilename);
                BufferedOutputStream out=new BufferedOutputStream(new FileOutputStream(file));
              //  FileOutputStream fos2 = new FileOutputStream(file);
                out.write(display.getText().toString().getBytes());
                out.close();
            }
          /*  else
            {
                Toast.makeText(getBaseContext(), "no file created",Toast.LENGTH_SHORT).show();
            }*/
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void ondownload(View view) {


        String data = "2";
        if (usbService != null) // if UsbService was correctly binded, Send data
        {
            usbService.write(data.getBytes());
        }
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
}
