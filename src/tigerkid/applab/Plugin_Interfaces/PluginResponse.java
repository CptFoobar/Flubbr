/*
 * Copyright (c) 2015 Sidhant Sharma <tigerkid001@gmail.com>.
 * Distributed under the terms of the MIT license.
 */

package tigerkid.applab.Plugin_Interfaces;


import android.os.Parcel;
import android.os.Parcelable;
/**
 * PluginResponse:
 * Parcelable object for remote callbacks.
 * */
public class PluginResponse implements Parcelable {

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
     * PluginResponse:
     * A constructor that initializes the PluginResponse object
     **/
    public PluginResponse(String sName, int sAge){
        this.mString = sName;
        this.mInt = sAge;
    }
    /**
     * PluginResponse:
     * Empty constructor.
     **/
    public PluginResponse(){
        this.mString = "";
        this.mInt = -1;
    }

    private PluginResponse(Parcel in){
        this.mString = in.readString();
        this.mInt = in.readInt();
    }

    /**
     * getPluginResponse:
     * Returns PluginResponse with data identical to that of 'in'
     * */
    public PluginResponse getPluginResponse(PluginResponse in){
        return new PluginResponse(in.mString, in.mInt);
    }

    public static final Parcelable.Creator<PluginResponse> CREATOR = new Parcelable.Creator<PluginResponse>() {

        @Override
        public PluginResponse createFromParcel(Parcel source) {
            return new PluginResponse(source);
        }

        @Override
        public PluginResponse[] newArray(int size) {
            return new PluginResponse[size];
        }
    };
}