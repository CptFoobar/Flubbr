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
    private OpServiceConnection opServiceConnection;    // Service to handle binding with plugins
    private IPluginInterface opService;                 // Interface object
    private String category;
    private PluginConfiguration pluginConfiguration;
    private Handler h = new Handler();
    private String text;

    /**
     * Service onStart
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //TODO do something useful
        return Service.START_NOT_STICKY;
    }

    /**
     * Return PMIBinder to Binding Activity
     */
    @Override
    public IBinder onBind(Intent intent) {
        return pmBinder;
    }

    private final IBinder pmBinder = new PMIBinder();

    public void onDestroy() {
        super.onDestroy();
        releaseOpService();
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
            if (!TAG.equals(Flubbr.BUNDLE_EXTRAS_CATEGORY))
                return;
            else {
                if (ctgry != null) {
                    category = ctgry;
                    bindOpService();
                }
            }
        }
    }

    /**
     * bindOpService:
     * Binds to the service of the plugin picked from the listView.
     */
    private void bindOpService() {
        if (category != null) {
            pluginConfiguration = new PluginConfiguration("Plugins rock!", 12);
            pluginConfiguration.describeContents();
            opServiceConnection = new OpServiceConnection();
            Intent i = new Intent(Flubbr.ACTION_PICK_PLUGIN);
            i.addCategory(category);
            bindService(i, opServiceConnection, Context.BIND_AUTO_CREATE);
        }
    }

    /**
     * releaseOpService:
     * Unload the plugin and release the plugin service.
     */
    private void releaseOpService() {
        try {
            opService.unload();
        } catch (RemoteException e) {
            Log.e("InvokePlugin:: releaseOpService: ", "Failed to unload plugin.");
            e.printStackTrace();
        }
        unbindService(opServiceConnection);
        opServiceConnection = null;
    }


    /**
     * OpServiceConnection:
     * Handles Plugins' ServiceConnection events.
     */
    class OpServiceConnection implements ServiceConnection {
        /**
         * onServiceConnected:
         * Once connected to plugin's service, load the plugin and run it.
         */
        public void onServiceConnected(ComponentName className, IBinder boundService) {
            try {
                opService = IPluginInterface.Stub.asInterface((IBinder) boundService);
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
