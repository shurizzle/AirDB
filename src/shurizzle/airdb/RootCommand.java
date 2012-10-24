package shurizzle.airdb;

import java.util.StringTokenizer;
import java.lang.Process;
import java.lang.Runtime;
import java.lang.StringBuilder;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import android.util.Log;

public class RootCommand
{
  private static final String TAG = "SHELL";
  private RootCommand() {}

  public static String shellEscape(String[] args)
  {
    StringBuilder res = new StringBuilder();

    for (int i = 0; i < args.length; i++) {
      res.append(shellEscapeWord(args[i]));

      if (i != args.length - 1) {
        res.append(' ');
      }
    }

    return res.toString();
  }

  public static String shellEscapeWord(String word)
  {
    if (word.isEmpty())
      return "''";

    StringBuilder res = new StringBuilder();
    StringTokenizer toks = new StringTokenizer(word, "'", true);

    for (int i = 0; i < toks.countTokens(); i++) {
      String tok = toks.nextToken();

      if (tok.equals("'")) {
        res.append("\\'");
      } else {
        res.append('\'').append(tok).append('\'');
      }
    }

    return res.toString();
  }

  public static Process vexec(String... args)
  {
    return exec(args);
  }

  public static Process exec(String[] args)
  {
    return exec(shellEscape(args));
  }

  public static Process exec(String cmd)
  {
    try {
      Process proc = Runtime.getRuntime().exec("su");
      DataOutputStream writer = new DataOutputStream(proc.getOutputStream());
      writer.writeBytes(cmd);
      writer.writeBytes("\n");
      writer.flush();
      writer.close();
      return proc;
    } catch (IOException e) {
      Log.e(TAG, e.toString());
      return null;
    }
  }

  public static boolean isGranted()
  {
    DataOutputStream writer = null;
    BufferedReader reader = null;
    boolean granted = false;

    try {
      Process process = Runtime.getRuntime().exec("su");
      writer = new DataOutputStream(process.getOutputStream());
      reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

      writer.writeBytes("id\n");
      writer.flush();
      writer.writeBytes("exit\n");
      writer.flush();

      String line = null;

      while ((line = reader.readLine()) != null) {
        if (line.toLowerCase().contains("uid=0")) {
          granted = true;
          break;
        }
      }

      process.waitFor();
    } catch (Exception e) {
      Log.e(TAG, e.toString());
    } finally {
      try {
        if (writer != null)
          writer.close();

        if (reader != null)
          reader.close();
      } catch (Exception e) {}
    }

    return granted;
  }
}
