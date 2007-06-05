/*
 * CDDL HEADER START
 *
 * The contents of this file are subject to the terms of the
 * Common Development and Distribution License, Version 1.0 only
 * (the "License").  You may not use this file except in compliance
 * with the License.
 *
 * You can obtain a copy of the license at
 * trunk/opends/resource/legal-notices/OpenDS.LICENSE
 * or https://OpenDS.dev.java.net/OpenDS.LICENSE.
 * See the License for the specific language governing permissions
 * and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL HEADER in each
 * file and include the License file at
 * trunk/opends/resource/legal-notices/OpenDS.LICENSE.  If applicable,
 * add the following below this CDDL HEADER, with the fields enclosed
 * by brackets "[]" replaced with your own identifying information:
 *      Portions Copyright [yyyy] [name of copyright owner]
 *
 * CDDL HEADER END
 *
 *
 *      Portions Copyright 2006-2007 Sun Microsystems, Inc.
 */
package org.opends.quicksetup.util;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.Window;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.net.ConnectException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.naming.CommunicationException;
import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.directory.SearchControls;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapName;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.opends.quicksetup.*;
import org.opends.quicksetup.webstart.JnlpProperties;
import org.opends.quicksetup.i18n.ResourceProvider;
import org.opends.server.util.SetupUtils;


/**
 * This class provides some static convenience methods of different nature.
 *
 */
public class Utils
{
  private static final Logger LOG =
          Logger.getLogger(Utils.class.getName());

  private static final int DEFAULT_LDAP_CONNECT_TIMEOUT = 3000;

  private static final int BUFFER_SIZE = 1024;

  private static final int MAX_LINE_WIDTH = 80;

  private Utils()
  {
  }

  /**
   * Center the component location based on its preferred size. The code
   * considers the particular case of 2 screens and puts the component on the
   * center of the left screen
   *
   * @param comp the component to be centered.
   */
  public static void centerOnScreen(Component comp)
  {
    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

    int width = (int) comp.getPreferredSize().getWidth();
    int height = (int) comp.getPreferredSize().getHeight();

    boolean multipleScreen = screenSize.width / screenSize.height >= 2;

    if (multipleScreen)
    {
      comp.setLocation((screenSize.width / 4) - (width / 2),
          (screenSize.height - height) / 2);
    } else
    {
      comp.setLocation((screenSize.width - width) / 2,
          (screenSize.height - height) / 2);
    }
  }

  /**
   * Center the component location of the ref component.
   *
   * @param comp the component to be centered.
   * @param ref the component to be used as reference.
   *
   */
  public static void centerOnComponent(Window comp, Component ref)
  {
    comp.setLocationRelativeTo(ref);
  }

  /**
   * Returns <CODE>true</CODE> if the provided port is free and we can use it,
   * <CODE>false</CODE> otherwise.
   * @param port the port we are analyzing.
   * @return <CODE>true</CODE> if the provided port is free and we can use it,
   * <CODE>false</CODE> otherwise.
   */
  public static boolean canUseAsPort(int port)
  {
    return SetupUtils.canUseAsPort(port);
  }

  /**
   * Returns <CODE>true</CODE> if the provided port is a priviledged port,
   * <CODE>false</CODE> otherwise.
   * @param port the port we are analyzing.
   * @return <CODE>true</CODE> if the provided port is a priviledged port,
   * <CODE>false</CODE> otherwise.
   */
  public static boolean isPriviledgedPort(int port)
  {
    return SetupUtils.isPriviledgedPort(port);
  }

  /**
   * Creates a new file attempting to create the parent directories
   * if necessary.
   * @param f File to create
   * @return boolean indicating whether the file was created; false otherwise
   * @throws IOException if something goes wrong
   */
  public static boolean createFile(File f) throws IOException {
    boolean success = false;
    if (f != null) {
      File parent = f.getParentFile();
      if (!parent.exists()) {
        parent.mkdirs();
      }
      success = f.createNewFile();
    }
    return success;
  }

  /**
   * Returns the absolute path for the given parentPath and relativePath.
   * @param parentPath the parent path.
   * @param relativePath the relative path.
   * @return the absolute path for the given parentPath and relativePath.
   */
  public static String getPath(String parentPath, String relativePath)
  {
    return getPath(new File(new File(parentPath), relativePath));
  }

  /**
   * Returns the absolute path for the given parentPath and relativePath.
   * @param f File to get the path
   * @return the absolute path for the given parentPath and relativePath.
   */
  public static String getPath(File f)
  {
    try
    {
      /*
       * Do a best effort to avoid having a relative representation (for
       * instance to avoid having ../../../).
       */
      File canonical = f.getCanonicalFile();
      f = canonical;
    }
    catch (IOException ioe)
    {
      /* This is a best effort to get the best possible representation of the
       * file: reporting the error is not necessary.
       */
    }
    return f.toString();
  }

  /**
   * Returns <CODE>true</CODE> if the first provided path is under the second
   * path in the file system.
   * @param descendant the descendant candidate path.
   * @param path the path.
   * @return <CODE>true</CODE> if the first provided path is under the second
   * path in the file system.
   */
  public static boolean isDescendant(String descendant, String path)
  {
    boolean isDescendant = false;
    File f1;
    File f2;

    try
    {
      f1 = (new File(path)).getCanonicalFile();
    }
    catch (IOException ioe)
    {
      f1 = new File(path);
    }

    try
    {
      f2 = (new File(descendant)).getCanonicalFile();
    }
    catch (IOException ioe)
    {
      f2 = new File(descendant);
    }

    f2 = f2.getParentFile();

    while ((f2 != null) && !isDescendant)
    {
      isDescendant = f1.equals(f2);

      if (!isDescendant)
      {
        f2 = f2.getParentFile();
      }
    }
    return isDescendant;
  }

  /**
   * Returns <CODE>true</CODE> if we are running under windows and
   * <CODE>false</CODE> otherwise.
   * @return <CODE>true</CODE> if we are running under windows and
   * <CODE>false</CODE> otherwise.
   */
  public static boolean isWindows()
  {
    return SetupUtils.isWindows();
  }

  /**
   * Returns <CODE>true</CODE> if we are running under Mac OS and
   * <CODE>false</CODE> otherwise.
   * @return <CODE>true</CODE> if we are running under Mac OS and
   * <CODE>false</CODE> otherwise.
   */
  public static boolean isMacOS()
  {
    return SetupUtils.isMacOS();
  }

  /**
   * Returns <CODE>true</CODE> if we are running under Unix and
   * <CODE>false</CODE> otherwise.
   * @return <CODE>true</CODE> if we are running under Unix and
   * <CODE>false</CODE> otherwise.
   */
  public static boolean isUnix()
  {
    return SetupUtils.isUnix();
  }

  /**
   * Returns a String representation of the OS we are running.
   * @return a String representation of the OS we are running.
   */
  public static String getOSString()
  {
    return SetupUtils.getOSString();
  }

  /**
   * Returns <CODE>true</CODE> if the parent directory for the provided path
   * exists and <CODE>false</CODE> otherwise.
   * @param path the path that we are analyzing.
   * @return <CODE>true</CODE> if the parent directory for the provided path
   * exists and <CODE>false</CODE> otherwise.
   */
  public static boolean parentDirectoryExists(String path)
  {
    boolean parentExists = false;
    File f = new File(path);
    if (f != null)
    {
      File parentFile = f.getParentFile();
      if (parentFile != null)
      {
        parentExists = parentFile.isDirectory();
      }
    }
    return parentExists;
  }

  /**
   * Returns <CODE>true</CODE> if the the provided path is a file and exists and
   * <CODE>false</CODE> otherwise.
   * @param path the path that we are analyzing.
   * @return <CODE>true</CODE> if the the provided path is a file and exists and
   * <CODE>false</CODE> otherwise.
   */
  public static boolean fileExists(String path)
  {
    boolean isFile = false;
    File f = new File(path);
    if (f != null)
    {
      isFile = f.isFile();
    }
    return isFile;
  }

  /**
   * Returns <CODE>true</CODE> if the the provided path is a directory, exists
   * and is not empty <CODE>false</CODE> otherwise.
   * @param path the path that we are analyzing.
   * @return <CODE>true</CODE> if the the provided path is a directory, exists
   * and is not empty <CODE>false</CODE> otherwise.
   */
  public static boolean directoryExistsAndIsNotEmpty(String path)
  {
    boolean directoryExistsAndIsNotEmpty = false;
    boolean isDirectory = false;

    File f = new File(path);
    if (f != null)
    {
      isDirectory = f.isDirectory();
    }
    if (isDirectory)
    {
      String[] ch = f.list();

      directoryExistsAndIsNotEmpty = (ch != null) && (ch.length > 0);
    }

    return directoryExistsAndIsNotEmpty;
  }

  /**
   * Returns <CODE>true</CODE> if the the provided string is a DN and
   * <CODE>false</CODE> otherwise.
   * @param dn the String we are analyzing.
   * @return <CODE>true</CODE> if the the provided string is a DN and
   * <CODE>false</CODE> otherwise.
   */
  public static boolean isDn(String dn)
  {
    boolean isDn = true;
    try
    {
      new LdapName(dn);
    } catch (Exception ex)
    {
      isDn = false;
    }
    return isDn;
  }

  /**
   * Returns <CODE>true</CODE> if the the provided string is a configuration DN
   * and <CODE>false</CODE> otherwise.
   * @param dn the String we are analyzing.
   * @return <CODE>true</CODE> if the the provided string is a configuration DN
   * and <CODE>false</CODE> otherwise.
   */
  public static boolean isConfigurationDn(String dn)
  {
    boolean isConfigurationDn = false;
    String[] configDns =
      { "cn=config", "cn=schema" };
    for (int i = 0; i < configDns.length && !isConfigurationDn; i++)
    {
      isConfigurationDn = areDnsEqual(dn, configDns[i]);
    }
    return isConfigurationDn;
  }

  /**
   * Returns <CODE>true</CODE> if the the provided strings represent the same
   * DN and <CODE>false</CODE> otherwise.
   * @param dn1 the first dn to compare.
   * @param dn2 the second dn to compare.
   * @return <CODE>true</CODE> if the the provided strings represent the same
   * DN and <CODE>false</CODE> otherwise.
   */
  public static boolean areDnsEqual(String dn1, String dn2)
  {
    boolean areDnsEqual = false;
    try
    {
      LdapName name1 = new LdapName(dn1);
      LdapName name2 = new LdapName(dn2);
      areDnsEqual = name1.equals(name2);
    } catch (Exception ex)
    {
    }

    return areDnsEqual;
  }

  /**
   * Creates the parent path for the provided path.
   * @param path the path.
   * @return <CODE>true</CODE> if the parent path was created or already existed
   * and <CODE>false</CODE> otherwise.
   */
  public static boolean createParentPath(String path)
  {
    boolean parentPathExists = true;
    if (!parentDirectoryExists(path))
    {
      File f = new File(path);
      if (f != null)
      {
        File parentFile = f.getParentFile();
        parentPathExists = parentFile.mkdirs();
      }
    }
    return parentPathExists;
  }

  /**
   * Creates the parent directory if it does not already exist.
   * @param f File for which parentage will be insured
   * @return boolean indicating whether or not the input <code>f</code>
   * has a parent after this method is invoked.
   */
  static public boolean insureParentsExist(File f) {
    File parent = f.getParentFile();
    boolean b = parent.exists();
    if (!b) {
      b = parent.mkdirs();
    }
    return b;
  }

  /**
   * Returns <CODE>true</CODE> if we can write on the provided path and
   * <CODE>false</CODE> otherwise.
   * @param path the path.
   * @return <CODE>true</CODE> if we can write on the provided path and
   * <CODE>false</CODE> otherwise.
   */
  public static boolean canWrite(String path)
  {
    boolean canWrite;
    File file = new File(path);
    if (file.exists())
    {
      canWrite = file.canWrite();
    } else
    {
      File parentFile = file.getParentFile();
      if (parentFile != null)
      {
        canWrite = parentFile.canWrite();
      } else
      {
        canWrite = false;
      }
    }
    return canWrite;
  }

  /**
   * Creates the a directory in the provided path.
   * @param path the path.
   * @return <CODE>true</CODE> if the path was created or already existed (and
   * was a directory) and <CODE>false</CODE> otherwise.
   * @throws IOException if something goes wrong.
   */
  public static boolean createDirectory(String path) throws IOException {
    return createDirectory(new File(path));
  }

  /**
   * Creates the a directory in the provided path.
   * @param f the path.
   * @return <CODE>true</CODE> if the path was created or already existed (and
   * was a directory) and <CODE>false</CODE> otherwise.
   * @throws IOException if something goes wrong.
   */
  public static boolean createDirectory(File f) throws IOException
  {
    boolean directoryCreated;
    if (!f.exists())
    {
      directoryCreated = f.mkdirs();
    } else
    {
      directoryCreated = f.isDirectory();
    }
    return directoryCreated;
  }

  /**
   * Creates a file on the specified path with the contents of the provided
   * stream.
   * @param path the path where the file will be created.
   * @param is the InputStream with the contents of the file.
   * @throws IOException if something goes wrong.
   */
  public static void createFile(String path, InputStream is)
          throws IOException
  {
    createFile(new File(path), is);
  }

  /**
   * Creates a file on the specified path with the contents of the provided
   * stream.
   * @param path the path where the file will be created.
   * @param is the InputStream with the contents of the file.
   * @throws IOException if something goes wrong.
   */
  public static void createFile(File path, InputStream is) throws IOException
  {
    FileOutputStream out;
    BufferedOutputStream dest;
    byte[] data = new byte[BUFFER_SIZE];
    int count;

    out = new FileOutputStream(path);

    dest = new BufferedOutputStream(out);

    while ((count = is.read(data, 0, BUFFER_SIZE)) != -1)
    {
      dest.write(data, 0, count);
    }
    dest.flush();
    dest.close();
  }

  /**
   * Creates a file on the specified path with the contents of the provided
   * String.
   * @param path the path where the file will be created.
   * @param content the String with the contents of the file.
   * @throws IOException if something goes wrong.
   */
  public static void createFile(String path, String content) throws IOException
  {
    FileWriter file = new FileWriter(path);
    PrintWriter out = new PrintWriter(file);

    out.println(content);

    out.flush();
    out.close();
  }

  /**
   * This is a helper method that gets a String representation of the elements
   * in the Collection. The String will display the different elements separated
   * by the separator String.
   *
   * @param col
   *          the collection containing the String.
   * @param separator
   *          the separator String to be used.
   * @return the String representation for the collection.
   */
  public static String getStringFromCollection(Collection<String> col,
      String separator)
  {
    String msg = null;
    for (String m : col)
    {
      if (msg == null)
      {
        msg = m;
      } else
      {
        msg += separator + m;
      }
    }
    return msg;
  }

  /**
   * Returns the default server location that will be proposed to the user
   * in the installation.
   * @return the default server location that will be proposed to the user
   * in the installation.
   */
  public static String getDefaultServerLocation()
  {
    String userDir = System.getProperty("user.home");
    String firstLocation =
        userDir + File.separator
            + org.opends.server.util.DynamicConstants.SHORT_NAME;
    String serverLocation = firstLocation;
    int i = 1;
    while (fileExists(serverLocation)
        || directoryExistsAndIsNotEmpty(serverLocation))
    {
      serverLocation = firstLocation + "-" + i;
      i++;
    }
    return serverLocation;
  }

  /**
   * Returns <CODE>true</CODE> if there is more disk space in the provided path
   * than what is specified with the bytes parameter.
   * @param directoryPath the path.
   * @param bytes the disk space.
   * @return <CODE>true</CODE> if there is more disk space in the provided path
   * than what is specified with the bytes parameter.
   */
  public static synchronized boolean hasEnoughSpace(String directoryPath,
      long bytes)
  {
    // TODO This does not work with quotas etc. but at least it seems that
    // we do not write all data on disk if it fails.
    boolean hasEnoughSpace = false;
    File file = null;
    RandomAccessFile raf = null;
    File directory = new File(directoryPath);
    boolean deleteDirectory = false;
    if (!directory.exists())
    {
      deleteDirectory = directory.mkdir();
    }
    try
    {
      file = File.createTempFile("temp" + System.nanoTime(), ".tmp", directory);
      raf = new RandomAccessFile(file, "rw");
      raf.setLength(bytes);
      hasEnoughSpace = true;
    } catch (IOException ex)
    {
    } finally
    {
      if (raf != null)
      {
        try
        {
          raf.close();
        } catch (IOException ex2)
        {
        }
      }
      if (file != null)
      {
        file.delete();
      }
    }
    if (deleteDirectory)
    {
      directory.delete();
    }
    return hasEnoughSpace;
  }

  /**
   * Returns a localized message for a given properties key an throwable.
   * @param key the key of the message in the properties file.
   * @param i18n the ResourceProvider to be used.
   * @param args the arguments of the message in the properties file.
   * @param t the throwable for which we want to get a message.
   *
   * @return a localized message for a given properties key and throwable.
   */
  public static String getThrowableMsg(ResourceProvider i18n, String key,
      String[] args, Throwable t)
  {
    String msg;
    if (args != null)
    {
      msg = i18n.getMsg(key, args);
    } else
    {
      msg = i18n.getMsg(key);
    }

    String tag;
    if (isOutOfMemory(t))
    {
      tag = "exception-out-of-memory-details";
    }
    else
    {
      tag = "exception-details";
    }
    String detail = t.toString();
    if (detail != null)
    {
      String[] arg =
      { detail };
      msg = msg + "  " + i18n.getMsg(tag, arg);
    }
    return msg;
  }

  /**
   * Sets the permissions of the provided paths with the provided permission
   * String.
   * @param paths the paths to set permissions on.
   * @param permissions the UNIX-mode file system permission representation
   * (for example "644" or "755")
   * @return the return code of the chmod command.
   * @throws IOException if something goes wrong.
   * @throws InterruptedException if the Runtime.exec method is interrupted.
   */
  public static int setPermissionsUnix(ArrayList<String> paths,
      String permissions) throws IOException, InterruptedException
  {
    String[] args = new String[paths.size() + 2];
    args[0] = "chmod";
    args[1] = permissions;
    for (int i = 2; i < args.length; i++)
    {
      args[i] = paths.get(i - 2);
    }
    Process p = Runtime.getRuntime().exec(args);
    return p.waitFor();
  }

  /**
   * Sets the permissions of the provided paths with the provided permission
   * String.
   * @param path to set permissions on.
   * @param permissions the UNIX-mode file system permission representation
   * (for example "644" or "755")
   * @return the return code of the chmod command.
   * @throws IOException if something goes wrong.
   * @throws InterruptedException if the Runtime.exec method is interrupted.
   */
  public static int setPermissionsUnix(String path,
      String permissions) throws IOException, InterruptedException
  {
    String[] args = new String[3];
    args[0] = "chmod";
    args[1] = permissions;
    args[2] = path;
    Process p = Runtime.getRuntime().exec(args);
    return p.waitFor();
  }

  // Very limited for the moment: apply only permissions to the current user and
  // does not work in non-English environments... to work in non English we
  // should use xcalcs but it does not come in the windows default install...
  // :-(
  // This method is not called for the moment, but the code works, so that is
  // why
  // is kept.
  private static int changePermissionsWindows(String path, String unixPerm)
      throws IOException, InterruptedException
  {
    String windowsPerm;
    int i = Integer.parseInt(unixPerm.substring(0, 1));
    if (Integer.lowestOneBit(i) == 1)
    {
      // Executable: give full permissions
      windowsPerm = "F";
    } else if (Integer.highestOneBit(i) == 4)
    {
      // Writable
      windowsPerm = "W";
    } else if (Integer.highestOneBit(i) == 2)
    {
      // Readable
      windowsPerm = "R";
    } else
    {
      // No permissions
      windowsPerm = "N";
    }

    String user = System.getProperty("user.name");
    String[] args =
      { "cacls", path, "/P", user + ":" + windowsPerm };
    Process p = Runtime.getRuntime().exec(args);

    // TODO: This only works in ENGLISH systems!!!!!!
    p.getOutputStream().write("Y\n".getBytes());
    p.getOutputStream().flush();
    return p.waitFor();
  }

  /**
   * Indicates whether we are in a web start installation or not.
   *
   * @return <CODE>true</CODE> if we are in a web start installation and
   *         <CODE>false</CODE> if not.
   */
  public static boolean isWebStart()
  {
    return "true".equals(System.getProperty(JnlpProperties.IS_WEBSTART));
  }

  /**
   * Returns <CODE>true</CODE> if this is executed from command line and
   * <CODE>false</CODE> otherwise.
   * @return <CODE>true</CODE> if this is executed from command line and
   * <CODE>false</CODE> otherwise.
   */
  public static boolean isCli()
  {
    return "true".equals(System.getProperty("org.opends.quicksetup.cli"));
  }

  /**
   * Creates a clear LDAP connection and returns the corresponding LdapContext.
   * This methods uses the specified parameters to create a JNDI environment
   * hashtable and creates an InitialLdapContext instance.
   *
   * @param ldapURL
   *          the target LDAP URL
   * @param dn
   *          passed as Context.SECURITY_PRINCIPAL if not null
   * @param pwd
   *          passed as Context.SECURITY_CREDENTIALS if not null
   * @param timeout
   *          passed as com.sun.jndi.ldap.connect.timeout if > 0
   * @param env
   *          null or additional environment properties
   *
   * @throws NamingException
   *           the exception thrown when instantiating InitialLdapContext
   *
   * @return the created InitialLdapContext.
   * @see javax.naming.Context
   * @see javax.naming.ldap.InitialLdapContext
   */
  public static InitialLdapContext createLdapContext(String ldapURL, String dn,
      String pwd, int timeout, Hashtable<String, String> env)
      throws NamingException
  {
    if (env != null)
    { // We clone 'env' so that we can modify it freely
      env = new Hashtable<String, String>(env);
    } else
    {
      env = new Hashtable<String, String>();
    }
    env
        .put(Context.INITIAL_CONTEXT_FACTORY,
            "com.sun.jndi.ldap.LdapCtxFactory");
    env.put(Context.PROVIDER_URL, ldapURL);
    if (timeout >= 1)
    {
      env.put("com.sun.jndi.ldap.connect.timeout", String.valueOf(timeout));
    }
    if (dn != null)
    {
      env.put(Context.SECURITY_PRINCIPAL, dn);
    }
    if (pwd != null)
    {
      env.put(Context.SECURITY_CREDENTIALS, pwd);
    }

    /* Contains the DirContext and the Exception if any */
    final Object[] pair = new Object[]
      { null, null };
    final Hashtable fEnv = env;
    Thread t = new Thread(new Runnable()
    {
      public void run()
      {
        try
        {
          pair[0] = new InitialLdapContext(fEnv, null);

        } catch (NamingException ne)
        {
          pair[1] = ne;

        } catch (Throwable t)
        {
          pair[1] = t;
        }
      }
    });
    return getInitialLdapContext(t, pair, timeout);
  }

  /**
   * Method used to know if we can connect as administrator in a server with a
   * given password and dn.
   * @param ldapUrl the ldap URL of the server.
   * @param dn the dn to be used.
   * @param pwd the password to be used.
   * @return <CODE>true</CODE> if we can connect and read the configuration and
   * <CODE>false</CODE> otherwise.
   */
  public static boolean canConnectAsAdministrativeUser(String ldapUrl,
      String dn, String pwd)
  {
    boolean canConnectAsAdministrativeUser = false;
    try
    {
      InitialLdapContext ctx =
        Utils.createLdapContext(ldapUrl, dn, pwd,
            Utils.getDefaultLDAPTimeout(), null);

      /*
       * Search for the config to check that it is the directory manager.
       */
      SearchControls searchControls = new SearchControls();
      searchControls.setCountLimit(1);
      searchControls.setSearchScope(
      SearchControls. OBJECT_SCOPE);
      searchControls.setReturningAttributes(
      new String[] {"dn"});
      ctx.search("cn=config", "objectclass=*", searchControls);

      canConnectAsAdministrativeUser = true;
    } catch (NamingException ne)
    {
      // Nothing to do.
    } catch (Throwable t)
    {
      throw new IllegalStateException("Unexpected throwable.", t);
    }
    return canConnectAsAdministrativeUser;
  }

  /**
   * Returns the path of the installation of the directory server.  Note that
   * this method assumes that this code is being run locally.
   * @return the path of the installation of the directory server.
   */
  public static String getInstallPathFromClasspath()
  {
    String installPath = null;

    /* Get the install path from the Class Path */
    String sep = System.getProperty("path.separator");
    String[] classPaths = System.getProperty("java.class.path").split(sep);
    String path = null;
    for (int i = 0; i < classPaths.length && (path == null); i++)
    {
      for (int j = 0; j < Installation.OPEN_DS_JAR_RELATIVE_PATHS.length &&
      (path == null); j++)
      {
        String normPath = classPaths[i].replace(File.separatorChar, '/');
        if (normPath.endsWith(Installation.OPEN_DS_JAR_RELATIVE_PATHS[j]))
        {
          path = classPaths[i];
        }
      }
    }
    if (path != null) {
      File f = new File(path).getAbsoluteFile();
      File librariesDir = f.getParentFile();

      /*
       * Do a best effort to avoid having a relative representation (for
       * instance to avoid having ../../../).
       */
      try
      {
        installPath = librariesDir.getParentFile().getCanonicalPath();
      }
      catch (IOException ioe)
      {
        // Best effort
        installPath = librariesDir.getParent();
      }
    }
    return installPath;
  }

  /**
   * Displays a confirmation message dialog.
  *
  * @param parent
   *          the parent frame of the confirmation dialog.
   * @param msg
  *          the confirmation message.
  * @param title
  *          the title of the dialog.
  * @return <CODE>true</CODE> if the user confirms the message, or
  * <CODE>false</CODE> if not.
  */
 public static boolean displayConfirmation(JFrame parent, String msg,
     String title)
 {
   return JOptionPane.YES_OPTION == JOptionPane.showOptionDialog(
       parent, msg, title, JOptionPane.YES_NO_OPTION,
       JOptionPane.QUESTION_MESSAGE, null, // don't use a custom
       // Icon
       null, // the titles of buttons
       null); // default button title
 }

  /**
   * Displays an error message dialog.
   *
   * @param parent
   *          the parent component of the error dialog.
   * @param msg
   *          the error message.
   * @param title
   *          the title for the dialog.
   */
  public static void displayError(Component parent, String msg, String title)
  {
    JOptionPane.showMessageDialog(parent, msg, title,
        JOptionPane.ERROR_MESSAGE);
  }

  /**
   * Displays an information message dialog.
   *
   * @param parent
   *          the parent frame of the information dialog.
   * @param msg
   *          the error message.
   * @param title
   *          the title for the dialog.
   */
  public static void displayInformationMessage(JFrame parent, String msg,
      String title)
  {
    JOptionPane.showMessageDialog(parent, msg, title,
        JOptionPane.INFORMATION_MESSAGE);
  }

  /**
   * Returns a Set of relative paths containing the db paths outside the
   * installation.
   * @param installStatus the Current Install Status object.
   * @return a Set of relative paths containing the db paths outside the
   * installation.
   */
  public static Set<String> getOutsideDbs(CurrentInstallStatus installStatus)
  {
    String installPath = getInstallPathFromClasspath();
    Set<String> dbs = installStatus.getDatabasePaths();
    Set<String> outsideDbs = new HashSet<String>();
    for (String relativePath : dbs)
    {
      /* The db paths are relative */
      String fullDbPath = getPath(installPath, relativePath);
      if (!isDescendant(fullDbPath, installPath))
      {
        outsideDbs.add(fullDbPath);
      }
    }
    return outsideDbs;
  }

  /**
   * Returns a Set of relative paths containing the log paths outside the
   * installation.
   * @param installStatus the Current Install Status object.
   * @return a Set of relative paths containing the log paths outside the
   * installation.
   */
  public static Set<String> getOutsideLogs(CurrentInstallStatus installStatus)
  {
    String installPath = getInstallPathFromClasspath();
    Set<String> logs = installStatus.getLogPaths();
    Set<String> outsideLogs = new HashSet<String>();
    for (String relativePath : logs)
    {
      /* The db paths are relative */
      String fullDbPath = getPath(installPath, relativePath);
      if (!isDescendant(fullDbPath, installPath))
      {
        outsideLogs.add(fullDbPath);
      }
    }
    return outsideLogs;
  }

  /**
   * This is just a commodity method used to try to get an InitialLdapContext.
   * @param t the Thread to be used to create the InitialLdapContext.
   * @param pair an Object[] array that contains the InitialLdapContext and the
   * Throwable if any occurred.
   * @param timeout the timeout.  If we do not get to create the connection
   * before the timeout a CommunicationException will be thrown.
   * @return the created InitialLdapContext
   * @throws NamingException if something goes wrong during the creation.
   */
  private static InitialLdapContext getInitialLdapContext(Thread t,
      Object[] pair, int timeout) throws NamingException
  {
    try
    {
      if (timeout > 0)
      {
        t.start();
        t.join(timeout);
      } else
      {
        t.run();
      }

    } catch (InterruptedException x)
    {
      // This might happen for problems in sockets
      // so it does not necessarily imply a bug
    }

    boolean throwException = false;

    if ((timeout > 0) && t.isAlive())
    {
      t.interrupt();
      try
      {
        t.join(2000);
      } catch (InterruptedException x)
      {
        // This might happen for problems in sockets
        // so it does not necessarily imply a bug
      }
      throwException = true;
    }

    if ((pair[0] == null) && (pair[1] == null))
    {
      throwException = true;
    }

    if (throwException)
    {
      NamingException xx;
      ConnectException x = new ConnectException("Connection timed out");
      xx = new CommunicationException("Connection timed out");
      xx.initCause(x);
      throw xx;
    }

    if (pair[1] != null)
    {
      if (pair[1] instanceof NamingException)
      {
        throw (NamingException) pair[1];

      } else if (pair[1] instanceof RuntimeException)
      {
        throw (RuntimeException) pair[1];

      } else if (pair[1] instanceof Throwable)
      {
        throw new IllegalStateException("Unexpected throwable occurred",
            (Throwable) pair[1]);
      }
    }
    return (InitialLdapContext) pair[0];
  }

  /**
   * Returns the default LDAP timeout in milliseconds when we try to connect to
   * a server.
   * @return the default LDAP timeout in milliseconds when we try to connect to
   * a server.
   */
  public static int getDefaultLDAPTimeout()
  {
    return DEFAULT_LDAP_CONNECT_TIMEOUT;
  }

  /**

   * Returns the max size in character of a line to be displayed in the command
   * line.
   * @return the max size in character of a line to be displayed in the command
   * line.
   */
  public static int getCommandLineMaxLineWidth()
  {
    return MAX_LINE_WIDTH;
  }

  /**
   * Puts Swing menus in the Mac OS menu bar, if using the Aqua look and feel,
   * and sets the application name that is displayed in the application menu
   * and in the dock.
   * @param appName
   *          application name to display in the menu bar and the dock.
   */
  public static void setMacOSXMenuBar(String appName)
  {
    System.setProperty("apple.laf.useScreenMenuBar", "true");
    System.setProperty("com.apple.mrj.application.apple.menu.about.name",
                       appName);
  }

  /**
    * Tells whether this throwable has been generated for an out of memory
    * error or not.
    * @param t the throwable to analyze.
    * @return <CODE>true</CODE> if the throwable was generated by an out of
    * memory error and false otherwise.
    */
  private static boolean isOutOfMemory(Throwable t)
  {
    boolean isOutOfMemory = false;
    while (!isOutOfMemory && (t != null))
    {
      if (t instanceof OutOfMemoryError)
      {
        isOutOfMemory = true;
      }
      else if (t instanceof IOException)
      {
        String msg = t.toString();
        if (msg != null)
        {
          isOutOfMemory = msg.indexOf("Not enough space") != -1;
        }
      }
      t = t.getCause();
    }
    return isOutOfMemory;
  }

  /**
   * Returns the number of entries contained in the zip file.  This is used to
   * update properly the progress bar ratio.
   * @return the number of entries contained in the zip file.
   */
  static public int getNumberZipEntries()
  {
    // TODO  we should get this dynamically during build
    return 83;
  }

  /**
   * Determines whether one file is the parent of another.
   * @param ancestor possible parent of <code>descendant</code>
   * @param descendant possible child 0f <code>ancestor</code>
   * @return return true if ancestor is a parent of descendant
   */
  static public boolean isParentOf(File ancestor, File descendant) {
    if (ancestor != null) {
      if (ancestor.equals(descendant)) {
        return false;
      }
      while ((descendant != null) && !ancestor.equals(descendant)) {
        descendant = descendant.getParentFile();
      }
    }
    return (ancestor != null) && (descendant != null);
  }

  /**
   * Creates a string consisting of the string representation of the
   * elements in the <code>list</code> separated by <code>separator</code>.
   * @param list the list to print
   * @param separator to use in separating elements
   * @return String representing the list
   */
  static public String listToString(List<?> list, String separator) {
    return listToString(list, separator, null, null);
  }

  /**
   * Creates a string consisting of the string representation of the
   * elements in the <code>list</code> separated by <code>separator</code>.
   * @param list the list to print
   * @param separator to use in separating elements
   * @param prefix prepended to each individual element in the list before
   *        adding to the returned string.
   * @param suffix appended to each individual element in the list before
   *        adding to the returned string.
   * @return String representing the list
   */
  static public String listToString(List<?> list, String separator,
                                    String prefix, String suffix) {
    StringBuffer sb = new StringBuffer();
    for (int i = 0; i < list.size(); i++) {
      if (prefix != null) {
        sb.append(prefix);
      }
      sb.append(list.get(i));
      if (suffix != null) {
        sb.append(suffix);
      }
      if (i < list.size() - 1) {
        sb.append(separator);
      }
    }
    return sb.toString();
  }

  /**
   * Creates a string consisting of the string representation of the
   * elements in the <code>list</code> separated by <code>separator</code>.
   * @param array the list to print
   * @param separator to use in separating elements
   * @return String representing the list
   */
  static public String stringArrayToString(String[] array, String separator) {
    StringBuffer sb = new StringBuffer();
    for (int i = 0; i < array.length; i++) {
      sb.append(array[i]);
      if (i < array.length - 1) {
        sb.append(separator);
      }
    }
    return sb.toString();
  }

  /**
   * Returns the file system permissions for a file.
   * @param path the file for which we want the file permissions.
   * @return the file system permissions for the file.
   */
  static public String getFileSystemPermissions(String path)
  {
    return getFileSystemPermissions(new File(path));
  }

  /**
   * Returns the file system permissions for a file.
   * @param file the file for which we want the file permissions.
   * @return the file system permissions for the file.
   */
  static public String getFileSystemPermissions(File file)
    {
    String perm;
    String name = file.getName();
    if (file.getParent().endsWith(
        File.separator + Installation.WINDOWS_BINARIES_PATH_RELATIVE) ||
        file.getParent().endsWith(
        File.separator + Installation.UNIX_BINARIES_PATH_RELATIVE))
    {
      if (name.endsWith(".bat"))
      {
        perm = "644";
      }
      else
      {
        perm = "755";
      }
    }
    else if (name.endsWith(".sh"))
    {
      perm = "755";
    } else if (name.endsWith(Installation.UNIX_SETUP_FILE_NAME) ||
            name.endsWith(Installation.UNIX_UNINSTALL_FILE_NAME) ||
            name.endsWith(Installation.UNIX_UPGRADE_FILE_NAME))
    {
      perm = "755";
    } else
    {
      perm = "644";
    }
    return perm;
  }

  /**
   * Returns a string representing the installation's current build information
   * useful for presenting to the user.  If the build string could not be
   * determined for any reason a localized String 'unknown' is returned.
   * @param installation whose build information is sought
   * @return String representing the application's build.
   */
  static public String getBuildString(Installation installation) {
    String b = null;
    try {
      BuildInformation bi = installation.getBuildInformation();
      if (bi != null) {
        b = bi.toString();
      }
    } catch (ApplicationException e) {
      LOG.log(Level.INFO, "error trying to determine current build string", e);
    }
    if (b == null) {
      b = ResourceProvider.getInstance().
              getMsg("upgrade-build-id-unknown");
    }
    return b;
  }

  /**
   * Inserts HTML break tags into <code>d</code> breaking it up
   * so that no line is longer than <code>maxll</code>.
   * @param d String to break
   * @param maxll int maximum line length
   * @return String representing <code>d</code> with HTML break
   *         tags inserted
   */
  static public String breakHtmlString(String d, int maxll) {
    // Primitive line wrapping
    int len = d.length();
    if (len <= 0)
      return d;
    if (len > maxll) {
      int p = d.lastIndexOf(' ', maxll);
      if (p <= 0)
        p = d.indexOf(' ', maxll);
      if (p > 0 && p < len) {
        return d.substring(0, p) +
                "<br>" +
               breakHtmlString(d.substring(p + 1), maxll);
      } else {
        return d;
      }
    } else {
      return d;
    }
  }

  /**
   * Tests a text string to see if it contains HTML.
   * @param text String to test
   * @return true if the string contains HTML
   */
  static public boolean containsHtml(String text) {
    return (text != null &&
            text.indexOf('<') != -1 &&
            text.indexOf('>') != -1);
  }

}
