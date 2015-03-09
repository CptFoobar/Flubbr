/*
 * Copyright (c) 2015 Sidhant Sharma <tigerkid001@gmail.com>.
 * Distributed under the terms of the MIT license.
 */

package tigerkid.applab.Plugin_Interfaces;

import tigerkid.applab.Plugin_Interfaces.PluginResponse;

oneway interface IPluginServiceCallback {
	/**
	 * receivedCallBack:
	 * Make callbacks using receivedCallback.
	 */
	void receivedCallBack(inout PluginResponse pluginResponse);
}
