/*
 * Copyright (c) 2015 Sidhant Sharma <tigerkid001@gmail.com>.
 * Distributed under the terms of the MIT license.
 */

package tigerkid.applab.Plugin_Interfaces;

import android.os.Parcel;
import android.os.Parcelable;

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

    /**
     * Storing the PluginResponse data to Parcel object
     **/
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mString);
        dest.writeInt(mInt);
    }

    /**
     * A constructor that initializes the PluginResponse object
     **/
    public PluginConfiguration(String sName, int sAge){
        this.mString = sName;
        this.mInt = sAge;
    }

    /**
     * Retrieving PluginResponse data from Parcel object
     * This constructor is invoked by the method createFromParcel(Parcel source) of
     * the object CREATOR
     **/
    private PluginConfiguration(Parcel in){
        this.mString = in.readString();
        this.mInt = in.readInt();
    }

    public String getmString(){
        return mString;
    }

    public int getmInt(){
        return mInt;
    }

    public PluginConfiguration getPluginConfiguration(){
        return new PluginConfiguration(this.mString, this.mInt);
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