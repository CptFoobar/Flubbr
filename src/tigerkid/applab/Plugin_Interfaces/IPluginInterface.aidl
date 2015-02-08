/*
 * Copyright (c) 2015 Sidhant Sharma <tigerkid001@gmail.com>.
 * Distributed under the terms of the MIT license.
 */

package tigerkid.applab.Plugin_Interfaces;
/**
 * IPluginInterface:
 * Interface for connection and execution of plugins.
 */
import tigerkid.applab.Plugin_Interfaces.PluginConfiguration;
import tigerkid.applab.Plugin_Interfaces.IPluginServiceCallback;

interface IPluginInterface {

	/**
	* registerCallback:
	* Register callbacks for binding services.
	*/
    void registerCallback(IPluginServiceCallback ipsc);
	/**
	 * load:
	 * Initialize the plugin with the inbound data from host and register callbacks.
	 */
	void load(in PluginConfiguration pluginConfiguration);
	/**
     * run:
     * Let the plugin do the dance.
     */
    void run();
	/**
     * unload:
     * Deinitalize plugin, unregister callbacks, cleanup.
     */
    void unload();
    /**
     * unegisterCallback:
     * Unregister callbacks for binding services.
     */
    void unregisterCallback(IPluginServiceCallback ipsc);
}