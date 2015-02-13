/*
 * Copyright (c) 2015 Sidhant Sharma <tigerkid001@gmail.com>.
 * Distributed under the terms of the MIT license.
 */

package tigerkid.applab.Flubbr;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.*;
import android.util.Log;
import android.widget.Toast;
import tigerkid.applab.Plugin_Interfaces.*;

import java.util.HashMap;
import java.util.Map;


/**
 * PluginManager: (Service)
 * This is the heart of the framework. PluginManager is the service that will bind to
 * the services of the plugins. All IPC communications will be handled here.
 */
public class PluginManager extends Service {

    /**
     * Constants section
     */
    private static final String LOG_TAG = "PluginManager";

    /**
     * Private variables section
     */
    private PluginServiceConnection pluginServiceConnection;    // Service to handle binding with plugins
    private IPluginInterface opService;                         // Interface object
    private String category;
    private PluginConfiguration pluginConfiguration;
    private Handler h = new Handler();
    private String text;

    /**
     * Public variables
     */
    // Hashmap to store plugins info
    public Map<String, PluginServiceConnection> connectionMap;        // Key: Plugin's intent.category, Value: Plugin's ServiceConnection
    public Map<PluginServiceConnection, IPluginInterface> serviceMap;           // Key: Plugin's ServiceConnection, Value: Plugin's IBinder

    /**
     * Service onStart
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return Service.START_NOT_STICKY;
    }

    /**
     * Return PMIBinder to Binding Activity
     */
    @Override
    public IBinder onBind(Intent intent) {
        connectionMap = new HashMap<String, PluginServiceConnection>();
        serviceMap = new HashMap<PluginServiceConnection, IPluginInterface>();
        return pmBinder;
    }

    private final IBinder pmBinder = new PMIBinder();   //Plugin Manager's binder. Returned to the binding activity.

    @Override
    public boolean onUnbind(Intent intent) {
        releaseAllServiceConnections();
        return false;
    }

    public void onDestroy() {
        super.onDestroy();
    }

    /**
     * PMIBinder:
     * IBinder to bind with PluginManager Service. An object of PMIBinder is returned
     * to main activity (Flubbr).
     */
    public class PMIBinder extends Binder {
        PluginManager getService() {
            // Return this instance of LocalService so clients can call public methods
            return PluginManager.this;
        }

        /**
         * bindPlugin:
         * Bind to plugin whose category is as specified
         */
        public void bindPlugin(String TAG, String ctgry) {
            //Get category
            if (TAG.equals(Flubbr.BUNDLE_EXTRAS_CATEGORY)) {
                if (ctgry != null) {
                    category = ctgry;
                    PluginServiceConnection psc = bindServiceConnection();
                    if (psc != null) {
                        connectionMap.put(ctgry, psc);
                        System.out.println("Added to connectionMap: " + ctgry);
                        System.out.println("connectionMap is " + connectionMap.toString());
                    }
                }
            }
        }
    }

    /**
     * bindOpService:
     * Binds to the service of the plugin picked from the listView.
     */
    private PluginServiceConnection bindServiceConnection() {
        if (category != null) {
            pluginConfiguration = new PluginConfiguration("Plugins rock!", 12);
            pluginConfiguration.describeContents();
            pluginServiceConnection = new PluginServiceConnection();
            Intent i = new Intent(Flubbr.ACTION_PICK_PLUGIN);
            i.addCategory(category);
            bindService(i, pluginServiceConnection, Context.BIND_AUTO_CREATE);
            return pluginServiceConnection;
        }
        return null;
    }

    /**
     * releaseOpService:
     * Unload the plugin and release the plugin service.
     */
    private void releaseAllServiceConnections() {
      /*  if (pluginServiceConnection != null)
            unbindService(pluginServiceConnection);*/
        //Unbind all service connections in connectionMap
        for (String i : connectionMap.keySet()) {
            if (connectionMap.get(i) != null)
                System.out.println("Unplugging " + i);
            unbindService(connectionMap.get(i));
        }
        connectionMap.clear();
        //clear serviceMap
        serviceMap.clear();
    }

    /**
     * OpServiceConnection:
     * Handles Plugins' ServiceConnection events.
     */
    class PluginServiceConnection implements ServiceConnection {
        /**
         * onServiceConnected:
         * Once connected to plugin's service, load the plugin and run it.
         */
        public void onServiceConnected(ComponentName className, IBinder boundService) {
            try {
                opService = IPluginInterface.Stub.asInterface(boundService);
                serviceMap.put(connectionMap.get(category), opService);
                System.out.println("Added to serviceMap: " + category + "'s ServiceConnection.");
                System.out.println("serviceMap is " + serviceMap.toString());
                Log.d(LOG_TAG, "onServiceConnected");
                try {
                    opService.load(pluginConfiguration);
                    opService.registerCallback(pluginCallback);
                    text = "Plugin loaded";
                    h.post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(), text, Toast.LENGTH_LONG).show();
                        }
                    });
                } catch (RemoteException e) {
                    e.printStackTrace();
                    Log.e("onServiceConnected: ", "Failed to load plugin.");
                    text = "Oops! It looks like the plugin didn't load correctly. Try restarting the app.";
                    h.post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(), text, Toast.LENGTH_LONG).show();
                        }
                    });
                    return;
                }
                try {
                    opService.run();
                    text = "Plugin running";
                    h.post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(), text, Toast.LENGTH_LONG).show();
                        }
                    });
                } catch (RemoteException e) {
                    Log.d("InvokePlugin::onServiceConnected: ", "Failed to run plugin.");
                    text = "Oops! It looks like the plugin failed to run. Try restarting the app.";
                    h.post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(), text, Toast.LENGTH_LONG).show();
                        }
                    });
                    e.printStackTrace();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        /**
         * onServiceDisconnected:
         * Unload plugin and make opService null
         */
        public void onServiceDisconnected(ComponentName className) {
            Log.d(LOG_TAG, "PluginManager: onServiceDisconnected");
            try {
                opService.unregisterCallback(pluginCallback);
                opService.unload();
            } catch (RemoteException e) {
                Log.e("PluginManager: onServiceConnected: ", "Failed to unload plugin");
                e.printStackTrace();
            }
            opService = null;
        }
    }

    /**
     * mCallBack:
     * Implementation used to receive callbacks from the remote service.
     */
    private IPluginServiceCallback pluginCallback = new IPluginServiceCallback.Stub() {
        @Override
        public void receivedCallBack(PluginResponse pluginResponse) throws RemoteException {
            if (pluginResponse != null)
                pluginResponse.describeContents();
        }

    };
}
