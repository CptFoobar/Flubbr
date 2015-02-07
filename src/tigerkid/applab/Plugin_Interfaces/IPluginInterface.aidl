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

interface IPluginInterface {
	void load(in PluginConfiguration pluginConfiguration);
    void run();
    void unload();
}