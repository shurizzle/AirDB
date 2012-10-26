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
import java.util.regex.Pattern;
import android.view.Window;
import android.view.Menu;
import android.content.Intent;
import android.net.Uri;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;

import android.widget.Toast;
import android.widget.TextView;

public class airctivity extends Activity
{
  Intent i = new Intent(Intent.ACTION_VIEW,
      Uri.parse("https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=FD9PLW4LYJXQA"));
  private static Pattern ipv4Pattern = Pattern.compile("^(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){0,3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$");
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
    requestWindowFeature(Window.FEATURE_NO_TITLE);
    setContentView(R.layout.main);

    switchButton = (Switch) findViewById(R.id.btn_on_off);
    textView = (TextView) findViewById(R.id.textView);

    if (RootCommand.isGranted()) {
      switchButton.setOnClickListener(new View.OnClickListener()
          {
            @Override
            public void onClick(View v) {
              if (switchButton.isChecked()) {
                SystemProperty.set("service.adb.tcp.port", "5555");
              } else {
                SystemProperty.set("service.adb.tcp.port", "-1");
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

  @Override
  public boolean onCreateOptionsMenu(Menu menu)
  {
    menu.add("Donate").
      setIcon(android.R.drawable.btn_star).
      setOnMenuItemClickListener(new OnMenuItemClickListener() {
      @Override
      public boolean onMenuItemClick(MenuItem item) {
        try {
          startActivity(i);
        } catch (Exception e) {
          Toast.makeText(airctivity.this, "Browser not found.", Toast.LENGTH_SHORT);
        }
        return true;
      }
    });
    return true;
  }

  public static boolean isIpv4(InetAddress addr)
  {
    return ipv4Pattern.matcher(addr.getHostAddress().toString()).matches();
  }

  public String getLocalIpAddress() {
    try {
      InetAddress res = null;
      for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
        NetworkInterface intf = en.nextElement();
        for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
          InetAddress inetAddress = enumIpAddr.nextElement();
          if (!inetAddress.isLoopbackAddress()) {
            if (isIpv4(inetAddress))
              return inetAddress.getHostAddress().toString();
            else
              res = inetAddress;
          }
        }
        if (res != null)
          return res.getHostAddress().toString();
      }
    } catch (SocketException ex) {
    }

    return null;
  }

  private void setButton()
  {
    if (SystemProperty.get("service.adb.tcp.port", "-1").equals("5555")) {
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
}
