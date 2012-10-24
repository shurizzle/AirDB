package shurizzle.airdb;

import android.content.Context;
import java.lang.ClassLoader;
import java.lang.Class;
import java.lang.reflect.Method;
import dalvik.system.DexFile;
import java.io.File;
import java.lang.Process;
import java.lang.InterruptedException;

public class SystemProperty
{
  private SystemProperty() {
  }

  public static String get(Context context, String key, String def)
  {
    String ret = def;

    try {
      ClassLoader cl = context.getClassLoader();
      @SuppressWarnings("rawtypes")
      Class SysProp = cl.loadClass("android.os.SystemProperties");

      @SuppressWarnings("rawtypes")
      Class[] paramTypes = new Class[2];
      paramTypes[0] = String.class;
      paramTypes[1] = String.class;

      Method get = SysProp.getMethod("get", paramTypes);

      Object[] params = new Object[2];
      params[0] = new String(key);
      params[1] = new String(def);

      ret = (String) get.invoke(SysProp, params);
    } catch (Exception e) {
      ret = def;
    }

    return ret;
  }

  @SuppressWarnings("unused")
  public static boolean set(Context context, String key, String val)
  {
    Process proc = RootCommand.vexec("setprop", key, val);

    if (proc == null)
      return false;

    try {
      proc.waitFor();
      return proc.exitValue() == 0;
    } catch(InterruptedException e) {
      return false;
    }
  }
}
