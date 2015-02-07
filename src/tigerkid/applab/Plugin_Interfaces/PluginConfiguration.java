/*
 * Copyright (c) 2015 Sidhant Sharma <tigerkid001@gmail.com>.
 * Distributed under the terms of the MIT license.
 */

package tigerkid.applab.Plugin_Interfaces;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * PluginConfiguration:
 * Parcelable object for sending initial values to plugin.
 * */
public class PluginConfiguration implements Parcelable {

    String mString;
    int mInt;

    @Override
    public int describeContents() {
        // TODO Auto-generated method stub
        System.out.println("mString: " + mString);
        System.out.println("mInt: " + Integer.toString(mInt));
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mString);
        dest.writeInt(mInt);
    }

    /**
     * PluginConfiguration:
     * A constructor that initializes the PluginResponse object
     **/
    public PluginConfiguration(String sName, int sAge){
        this.mString = sName;
        this.mInt = sAge;
    }

    private PluginConfiguration(Parcel in){
        this.mString = in.readString();
        this.mInt = in.readInt();
    }
    /**
     * getPluginConfiguration:
     * Returns PluginConfiguration with data identical to that of 'in'
     * */
    public PluginConfiguration getPluginConfiguration(PluginConfiguration in){
        return new PluginConfiguration(in.mString, in.mInt);
    }

    public static final Parcelable.Creator<PluginConfiguration> CREATOR = new Parcelable.Creator<PluginConfiguration>() {

        @Override
        public PluginConfiguration createFromParcel(Parcel source) {
            return new PluginConfiguration(source);
        }

        @Override
        public PluginConfiguration[] newArray(int size) {
            return new PluginConfiguration[size];
        }
    };
}