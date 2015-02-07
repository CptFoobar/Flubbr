/*
 * Copyright (c) 2015 Sidhant Sharma <tigerkid001@gmail.com>.
 * Distributed under the terms of the MIT license.
 */

package tigerkid.applab.Plugin_Interfaces;

import tigerkid.applab.Plugin_Interfaces.PluginResponse;
/**
 * IPluginServiceCallback:
 * Oneway interface for remote callbacks.
 */
oneway interface IPluginServiceCallback {
	void receivedCallBack(out PluginResponse pluginResponse);
}
