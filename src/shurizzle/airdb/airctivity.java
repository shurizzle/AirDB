package shurizzle.airdb;

import android.app.Activity;
import android.os.Bundle;
import android.content.Context;
import android.view.View;
import shurizzle.airdb.views.Switch;
import java.lang.InterruptedException;
import java.util.Enumeration;
import java.net.NetworkInterface;
import java.net.InetAddress;
import java.net.SocketException;

import android.widget.Toast;
import android.widget.TextView;

public class airctivity extends Activity
{
  private static Context context;
  private Switch switchButton;
  private TextView textView;

  /** Called when the activity is first created. */
  @Override
  public void onCreate(Bundle savedInstanceState)
  {
    airctivity.context = getApplicationContext();

    this.setTheme(R.style.AppThemeDark);

    super.onCreate(savedInstanceState);
    setContentView(R.layout.main);

    switchButton = (Switch) findViewById(R.id.btn_on_off);
    textView = (TextView) findViewById(R.id.textView);

    if (RootCommand.isGranted()) {
      switchButton.setOnClickListener(new View.OnClickListener()
          {
            @Override
            public void onClick(View v) {
              if (switchButton.isChecked()) {
                setProperty("service.adb.tcp.port", "5555");
              } else {
                setProperty("service.adb.tcp.port", "-1");
              }
              try {
                RootCommand.vexec("stop", "adbd").waitFor();
                RootCommand.vexec("start", "adbd").waitFor();
              } catch (InterruptedException e) {}
              setButton();
            }
          });
    } else {
      switchButton.setOnClickListener(new View.OnClickListener()
          {
            @Override
            public void onClick(View v) {
              setButton();
            }
          });
      Toast.makeText(airctivity.context, "Cannot get root permissions", Toast.LENGTH_SHORT).show();
    }


    setButton();
  }

  public String getLocalIpAddress() {
    try {
      for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
        NetworkInterface intf = en.nextElement();
        for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
          InetAddress inetAddress = enumIpAddr.nextElement();
          if (!inetAddress.isLoopbackAddress()) {
            return inetAddress.getHostAddress().toString();
          }
        }
      }
    } catch (SocketException ex) {
    }

    return null;
  }

  private void setButton()
  {
    if (getProperty("service.adb.tcp.port", "-1").equals("5555")) {
      switchButton.setChecked(true);
      String ip = getLocalIpAddress();
      if (ip != null) {
        textView.setText(ip);
        textView.append(":5555");
      }
    } else {
      switchButton.setChecked(false);
      textView.setText("");
    }
  }

  public void setProperty(String key, String val)
  {
    SystemProperty.set(context, key, val);
  }

  public String getProperty(String key, String def)
  {
    return SystemProperty.get(context, key, def);
  }
}
