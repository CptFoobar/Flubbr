/*
 * Copyright (c) 2015 Sidhant Sharma <tigerkid001@gmail.com>.
 * Distributed under the terms of the MIT license.
 */

package tigerkid.applab.Flubbr;

import android.app.ListActivity;
import android.content.*;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 * Flubbr: (ListActivity)
 * This is the entry-point activity of the app. It is a listActivity , each of its item being
 * an available plugin.
 */

public class Flubbr extends ListActivity {

    /**
     * Constants Section
     */
    public static final String ACTION_PICK_PLUGIN = "tigerkid.applab.intent.action.PICK_PLUGIN";

    static final String KEY_PKG = "pkg";
    static final String KEY_SERVICENAME = "servicename";
    static final String KEY_ACTIONS = "actions";
    static final String KEY_CATEGORIES = "categories";
    static final String BUNDLE_EXTRAS_CATEGORY = "category";

    static final String LOG_TAG = "Plugin";

    /**
     * Private variables section
     */

    private PackageBroadcastReceiver packageBroadcastReceiver;
    private IntentFilter packageFilter;
    private ArrayList<HashMap<String, String>> services;
    private ArrayList<String> categories;
    private SimpleAdapter itemAdapter;
    private PMServiceConnection pmServiceConnection;
    private PluginManager.PMIBinder PMBinder;


    /**
     * onCreate:
     * Sets list adapter for listView, initializes packageFilter
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        fillPluginList();
        itemAdapter =
                new SimpleAdapter(this,
                        services,
                        R.layout.services_row,
                        new String[]{KEY_PKG, KEY_SERVICENAME, KEY_ACTIONS, KEY_CATEGORIES},
                        new int[]{R.id.pkg, R.id.servicename, R.id.actions, R.id.categories}
                );
        setListAdapter(itemAdapter);

        packageBroadcastReceiver = new PackageBroadcastReceiver();
        packageFilter = new IntentFilter();
        packageFilter.addAction(Intent.ACTION_PACKAGE_ADDED);
        packageFilter.addAction(Intent.ACTION_PACKAGE_REPLACED);
        packageFilter.addAction(Intent.ACTION_PACKAGE_REMOVED);
        packageFilter.addCategory(Intent.CATEGORY_DEFAULT);
        packageFilter.addDataScheme("package");
        bindPluginManager();
    }

    protected void onStart() {
        super.onStart();
        Log.d(LOG_TAG, "onStart");
        registerReceiver(packageBroadcastReceiver, packageFilter);
    }

    protected void onStop() {
        super.onStop();
        Log.d(LOG_TAG, "onStop");
        unregisterReceiver(packageBroadcastReceiver);
        stopService(new Intent("tigerkid.applab.intent.action.PLUGIN_MGR"));
    }

    /**
     * bindPluginManager:
     * Binds to the PluginManager Service
     */
    //TODO: Stop service to prevent leaks.
    private void bindPluginManager() {
        pmServiceConnection = new PMServiceConnection();
        Intent intent;
        intent = new Intent("tigerkid.applab.intent.action.PLUGIN_MGR");
        intent.addCategory("tigerkid.applab.intent.category.PLUGIN_MGR");
        bindService(intent, pmServiceConnection, Context.BIND_AUTO_CREATE);
    }

    /**
     * onListItemClick:
     * Handles onClick event for the ListView. When a item in the list the clicked,
     * it retrieves its category information, bundles it up with an intent for InvokePlugin and
     * launches the activity.
     */
    protected void onListItemClick(ListView l, View v, int position, long id) {
        Log.d(LOG_TAG, "onListItemClick: " + position);
        String category = categories.get(position);
        PMBinder.bindPlugin(BUNDLE_EXTRAS_CATEGORY, category);
    }

    /**
     * fillPluginList:
     * Populates plugin list with all the available plugins. The plugins are scanned for
     * by PackageManager for a specific intent, defined by ACTION_PICK_PLUGIN.
     */
    private void fillPluginList() {
        services = new ArrayList<HashMap<String, String>>();
        categories = new ArrayList<String>();
        PackageManager packageManager = getPackageManager();
        Intent baseIntent = new Intent(ACTION_PICK_PLUGIN);
        baseIntent.setFlags(Intent.FLAG_DEBUG_LOG_RESOLUTION);
        List<ResolveInfo> list = packageManager.queryIntentServices(baseIntent,
                PackageManager.GET_RESOLVED_FILTER);
        Log.d(LOG_TAG, "fillPluginList: " + list);
        for (int i = 0; i < list.size(); ++i) {
            ResolveInfo info = list.get(i);
            ServiceInfo sinfo = info.serviceInfo;
            IntentFilter filter = info.filter;
            Log.d(LOG_TAG, "fillPluginList: i: " + i + "; sinfo: " + sinfo + ";filter: " + filter);
            if (sinfo != null) {
                HashMap<String, String> item = new HashMap<String, String>();
                item.put(KEY_PKG, sinfo.packageName);
                item.put(KEY_SERVICENAME, sinfo.name);
                String firstCategory = null;
                if (filter != null) {
                    StringBuilder actions = new StringBuilder();
                    for (Iterator<String> actionIterator = filter.actionsIterator(); actionIterator.hasNext(); ) {
                        String action = actionIterator.next();
                        if (actions.length() > 0)
                            actions.append(",");
                        actions.append(action);
                    }
                    StringBuilder categories = new StringBuilder();
                    for (Iterator<String> categoryIterator = filter.categoriesIterator();
                         categoryIterator.hasNext(); ) {
                        String category = categoryIterator.next();
                        if (firstCategory == null)
                            firstCategory = category;
                        if (categories.length() > 0)
                            categories.append(",");
                        categories.append(category);
                    }
                    item.put(KEY_ACTIONS, new String(actions));
                    item.put(KEY_CATEGORIES, new String(categories));
                } else {
                    item.put(KEY_ACTIONS, "<null>");
                    item.put(KEY_CATEGORIES, "<null>");
                }
                if (firstCategory == null)
                    firstCategory = "";
                categories.add(firstCategory);
                services.add(item);
            }
        }
        Log.d(LOG_TAG, "services: " + services);
        Log.d(LOG_TAG, "categories: " + categories);
    }

    /**
     * PackageBroadcastReceiver:
     * BroadcastReceiver for PackageManager.
     */
    //TODO: Register/unregister receiver at runtime to avoid battery drain.
    class PackageBroadcastReceiver extends BroadcastReceiver {
        private static final String LOG_TAG = "PackageBroadcastReceiver";

        public void onReceive(Context context, Intent intent) {
            Log.d(LOG_TAG, "onReceive: " + intent);
            services.clear();
            fillPluginList();
            itemAdapter.notifyDataSetChanged();
        }
    }

    /**
     * PMServiceConnection:
     * Class for handling PluginManager's ServiceConnection events.
     */
    class PMServiceConnection implements ServiceConnection {

        public void onServiceConnected(ComponentName className, IBinder boundService) {
            try {
                PMBinder = (PluginManager.PMIBinder) boundService;
                Log.d(LOG_TAG, "onServiceConnected");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public void onServiceDisconnected(ComponentName className) {
            PMBinder = null;
            Log.d(LOG_TAG, "PluginManager: onServiceDisconnected");
        }
    }
}
