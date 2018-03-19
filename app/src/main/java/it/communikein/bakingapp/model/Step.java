package it.communikein.bakingapp.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

public class Step implements Parcelable {

    public static final String ID = "id";
    public static final String SHORT_DESCRIPTION = "shortDescription";
    public static final String DESCRIPTION = "description";
    public static final String VIDEO_URL = "videoURL";
    public static final String THUMBNAIL_URL = "thumbnailURL";

    @SerializedName(ID)
    private int id;

    @SerializedName(SHORT_DESCRIPTION)
    private String shortDescription;

    @SerializedName(DESCRIPTION)
    private String description;

    @SerializedName(VIDEO_URL)
    private String videoURL;

    @SerializedName(THUMBNAIL_URL)
    private String thumbnailURL;


    public Step(Parcel in) {
        setId(in.readInt());
        setShortDescription(in.readString());
        setDescription(in.readString());
        setVideoURL(in.readString());
        setThumbnailURL(in.readString());
    }

    public Step(int id, String shortDescription, String description, String videoURL, String thumbnailURL) {
        setId(id);
        setShortDescription(shortDescription);
        setDescription(description);
        setVideoURL(videoURL);
        setThumbnailURL(thumbnailURL);
    }


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getShortDescription() {
        return shortDescription;
    }

    public void setShortDescription(String shortDescription) {
        this.shortDescription = shortDescription;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getVideoURL() {
        return videoURL;
    }

    public void setVideoURL(String videoURL) {
        this.videoURL = videoURL;
    }

    public String getThumbnailURL() {
        return thumbnailURL;
    }

    public void setThumbnailURL(String thumbnailURL) {
        this.thumbnailURL = thumbnailURL;
    }

    @Override
    public boolean equals(Object obj) {
        if (! (obj instanceof Step)) return false;

        Step other = (Step) obj;
        return this.getId() == other.getId();
    }

    public boolean displayEquals(Object obj) {
        return this.equals(obj);
    }



    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(getId());
        dest.writeString(getShortDescription());
        dest.writeString(getDescription());
        dest.writeString(getVideoURL());
        dest.writeString(getThumbnailURL());
    }

    public static final Parcelable.Creator<Step> CREATOR = new Parcelable.Creator<Step>() {

        public Step createFromParcel(Parcel in) {
            return new Step(in);
        }

        public Step[] newArray(int size) {
            return new Step[size];
        }
    };
}

