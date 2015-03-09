package tigerkid.applab.Plugin_Interfaces;


import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.util.Log;

public abstract class FlubbrPlugin extends Service {
    private static final String LOG_TAG = "FlubbrPlugin";
    static final String PLUGIN_ACTION = "tigerkid.applab.intent.category.EG_PLUGIN";
    protected PluginConfiguration rec;        //TODO: Remove this
    private static final RemoteCallbackList<IPluginServiceCallback> remoteCallbacks
            = new RemoteCallbackList<IPluginServiceCallback>();

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return Service.START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public final IBinder onBind(Intent intent) {
        return addBinder;
    }

    private final IPluginInterface.Stub addBinder =
            new IPluginInterface.Stub() {

                public void registerCallback(IPluginServiceCallback ipsc) {
                    if (ipsc != null) {
                        remoteCallbacks.register(ipsc);
                        Log.d(LOG_TAG, "registerCallback: Registered callback");
                    }
                }

                public void load(PluginConfiguration pluginConfiguration) {
                    Log.d(LOG_TAG, "load: Plugin loading.");
                    try {
                        if (pluginConfiguration != null) {
                            Log.d(LOG_TAG, "load: Plugin configuration received.");
                            pluginConfiguration.describeContents();
                            rec = pluginConfiguration.getPluginConfiguration();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    FlubbrPlugin.this.onLoad();
                }

                public void run() {
                    Log.d(LOG_TAG, "run: Plugin running");
                    rec.describeContents();
                    FlubbrPlugin.this.run();
                    Log.d(LOG_TAG,"run: *//**************************************************//*");
                    Log.d(LOG_TAG,"run: Making callBacks");
                    Log.d(LOG_TAG,"run: *//**************************************************//*");
                    makeCallback(new PluginResponse("Sample callback", 2));
                }

                public void update(PluginConfiguration pluginConfiguration) {
                    Log.d(LOG_TAG, "update: Update Received");
                   pluginConfiguration.describeContents();
                    FlubbrPlugin.this.onUpdate(pluginConfiguration);
                }

                public void unload() {
                    Log.d(LOG_TAG, "unload: Plugin unloaded");
                    rec = null;
                    FlubbrPlugin.this.onUnload();
                }

                public void unregisterCallback(IPluginServiceCallback ipsc) {
                    if (ipsc != null) {
                        remoteCallbacks.unregister(ipsc);
                        Log.d(LOG_TAG,"unregisterCallback: Unregistered callback");
                    }
                }
            };

    // To make callbacks
    private static void makeCallback(final PluginResponse info) {
        final int n = remoteCallbacks.beginBroadcast();
        Log.d(LOG_TAG,"makeCallback: Callback is sending: ");
        info.describeContents();
        for (int i = 0; i < n; i++) {
            final IPluginServiceCallback callback = remoteCallbacks.getBroadcastItem(i);
            try {
                callback.receivedCallBack(info);
            } catch (RemoteException e) {
                Log.e(LOG_TAG, "makeCallback: Broadcast error", e);
            }
        }
        remoteCallbacks.finishBroadcast();
    }

    protected abstract void onLoad();

    protected abstract void run();

    protected abstract void onUpdate(final PluginConfiguration pluginConfiguration);

    protected abstract void onUnload();

    protected static void requestCallBack(String s, int i) {
        makeCallback(new PluginResponse(s,i));
    }
}
