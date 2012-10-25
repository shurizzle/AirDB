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

  private static Class SYSPROP = null;
  private static Method GET_METHOD = null;

  static {
    try {
      ClassLoader cl = SystemProperty.class.getClassLoader();
      @SuppressWarnings("rawtypes")
      Class SYSPROP = cl.loadClass("android.os.SystemProperties");

      @SuppressWarnings("rawtypes")
      Class[] paramTypes = new Class[2];
      paramTypes[0] = String.class;
      paramTypes[1] = String.class;

      GET_METHOD = SYSPROP.getMethod("get", paramTypes);
    } catch (Exception e) {}
  }

  public static String get(String key, String def)
  {
    String ret = def;

    try {
      Object[] params = new Object[2];
      params[0] = new String(key);
      params[1] = new String(def);

      ret = (String) GET_METHOD.invoke(SYSPROP, params);
    } catch (Exception e) {
      ret = def;
    }

    return ret;
  }

  public static boolean set(String key, String val)
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
