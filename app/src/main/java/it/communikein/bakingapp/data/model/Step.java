package it.communikein.bakingapp.data.model;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.PrimaryKey;
import android.content.ContentValues;
import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

import static android.arch.persistence.room.ForeignKey.CASCADE;
import static it.communikein.bakingapp.data.contentprovider.StepContract.StepEntry.COLUMN_DESCRIPTION;
import static it.communikein.bakingapp.data.contentprovider.StepContract.StepEntry.COLUMN_ID;
import static it.communikein.bakingapp.data.contentprovider.StepContract.StepEntry.COLUMN_RECIPE_ID;
import static it.communikein.bakingapp.data.contentprovider.StepContract.StepEntry.COLUMN_SHORT_DESCRIPTION;
import static it.communikein.bakingapp.data.contentprovider.StepContract.StepEntry.COLUMN_STEP_NUM;
import static it.communikein.bakingapp.data.contentprovider.StepContract.StepEntry.COLUMN_THUMBNAIL_URL;
import static it.communikein.bakingapp.data.contentprovider.StepContract.StepEntry.COLUMN_VIDEO_URL;
import static it.communikein.bakingapp.data.contentprovider.StepContract.StepEntry.TABLE_NAME;

@Entity(tableName = TABLE_NAME,
        foreignKeys = @ForeignKey(entity = Recipe.class,
                parentColumns = "id",
                childColumns = COLUMN_RECIPE_ID,
                onDelete = CASCADE))
public class Step implements Parcelable {

    public static final String STEP_NUM = "id";
    public static final String RECIPE_ID = "recipe_id";
    public static final String SHORT_DESCRIPTION = "shortDescription";
    public static final String DESCRIPTION = "description";
    public static final String VIDEO_URL = "videoURL";
    public static final String THUMBNAIL_URL = "thumbnailURL";


    @SerializedName(COLUMN_ID)
    @PrimaryKey(autoGenerate = true) @ColumnInfo(index = true, name = COLUMN_ID)
    public int id;

    @SerializedName(STEP_NUM)
    @ColumnInfo(name = COLUMN_STEP_NUM)
    private int stepNum;

    @SerializedName(RECIPE_ID)
    @ColumnInfo(index = true, name = COLUMN_RECIPE_ID)
    private int recipeId;

    @SerializedName(SHORT_DESCRIPTION)
    @ColumnInfo(name = COLUMN_SHORT_DESCRIPTION)
    private String shortDescription;

    @SerializedName(DESCRIPTION)
    @ColumnInfo(name = COLUMN_DESCRIPTION)
    private String description;

    @SerializedName(VIDEO_URL)
    @ColumnInfo(name = COLUMN_VIDEO_URL)
    private String videoURL;

    @SerializedName(THUMBNAIL_URL)
    @ColumnInfo(name = COLUMN_THUMBNAIL_URL)
    private String thumbnailURL;


    public static Step fromParcel(Parcel origin) {
        final Step step = new Step();

        step.setId(origin.readInt());
        step.setStepNum(origin.readInt());
        step.setRecipeId(origin.readInt());
        step.setShortDescription(origin.readString());
        step.setDescription(origin.readString());
        step.setVideoURL(origin.readString());
        step.setThumbnailURL(origin.readString());

        return step;
    }

    public static Step fromContentValues(ContentValues origin) {
        if (origin == null) return null;

        final Step step = new Step();

        if (origin.containsKey(COLUMN_ID))
            step.setId(origin.getAsInteger(COLUMN_ID));
        if (origin.containsKey(COLUMN_STEP_NUM))
            step.setStepNum(origin.getAsInteger(COLUMN_STEP_NUM));
        if (origin.containsKey(COLUMN_RECIPE_ID))
            step.setRecipeId(origin.getAsInteger(COLUMN_RECIPE_ID));
        if (origin.containsKey(COLUMN_DESCRIPTION))
            step.setDescription(origin.getAsString(COLUMN_DESCRIPTION));
        if (origin.containsKey(COLUMN_SHORT_DESCRIPTION))
            step.setShortDescription(origin.getAsString(COLUMN_SHORT_DESCRIPTION));
        if (origin.containsKey(COLUMN_VIDEO_URL))
            step.setVideoURL(origin.getAsString(COLUMN_VIDEO_URL));
        if (origin.containsKey(COLUMN_THUMBNAIL_URL))
            step.setThumbnailURL(origin.getAsString(COLUMN_THUMBNAIL_URL));

        return step;
    }

    public static Step fromCursor(Cursor cursor) {
        int idIndex = cursor.getColumnIndex(COLUMN_ID);
        int stepNumIndex = cursor.getColumnIndex(COLUMN_STEP_NUM);
        int recipeIdIndex = cursor.getColumnIndex(COLUMN_RECIPE_ID);
        int shortDescriptionIndex = cursor.getColumnIndex(COLUMN_SHORT_DESCRIPTION);
        int descriptionIndex = cursor.getColumnIndex(COLUMN_DESCRIPTION);
        int videoUrlIndex = cursor.getColumnIndex(COLUMN_VIDEO_URL);
        int thumbnailUrlIndex = cursor.getColumnIndex(COLUMN_THUMBNAIL_URL);

        int stepId = cursor.getInt(idIndex);
        int stepNum = cursor.getInt(stepNumIndex);
        int stepRecipeId = cursor.getInt(recipeIdIndex);
        String stepShortDescription = cursor.getString(shortDescriptionIndex);
        String stepDescription = cursor.getString(descriptionIndex);
        String stepVideoUrl = cursor.getString(videoUrlIndex);
        String stepThumbnailUrl = cursor.getString(thumbnailUrlIndex);

        return new Step(stepId, stepNum, stepRecipeId, stepShortDescription, stepDescription,
                stepVideoUrl, stepThumbnailUrl);
    }


    private Step() {}

    public Step(int id, int stepNum, int recipeId,
                String shortDescription, String description, String videoURL, String thumbnailURL) {
        setId(id);
        setStepNum(stepNum);
        setRecipeId(recipeId);
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

    public int getStepNum() {
        return stepNum;
    }

    public void setStepNum(int stepNum) {
        this.stepNum = stepNum;
    }

    public int getRecipeId() {
        return recipeId;
    }

    public void setRecipeId(int recipeId) {
        this.recipeId = recipeId;
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
        return this.getStepNum() == other.getStepNum() &&
                this.getRecipeId() == other.getRecipeId();
    }

    public boolean displayEquals(Object obj) {
        return this.equals(obj);
    }

    @Override
    public String toString() {
        return getDescription();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(getId());
        dest.writeInt(getStepNum());
        dest.writeInt(getRecipeId());
        dest.writeString(getShortDescription());
        dest.writeString(getDescription());
        dest.writeString(getVideoURL());
        dest.writeString(getThumbnailURL());
    }

    public static final Parcelable.Creator<Step> CREATOR = new Parcelable.Creator<Step>() {

        public Step createFromParcel(Parcel in) {
            return Step.fromParcel(in);
        }

        public Step[] newArray(int size) {
            return new Step[size];
        }
    };

    public ContentValues writeToContentValues() {
        ContentValues contentValues = new ContentValues();

        contentValues.put(COLUMN_ID, getId());
        contentValues.put(COLUMN_STEP_NUM, getStepNum());
        contentValues.put(COLUMN_RECIPE_ID, getRecipeId());
        contentValues.put(COLUMN_SHORT_DESCRIPTION, getShortDescription());
        contentValues.put(COLUMN_DESCRIPTION, getDescription());
        contentValues.put(COLUMN_VIDEO_URL, getVideoURL());
        contentValues.put(COLUMN_THUMBNAIL_URL, getThumbnailURL());

        return contentValues;
    }
}

