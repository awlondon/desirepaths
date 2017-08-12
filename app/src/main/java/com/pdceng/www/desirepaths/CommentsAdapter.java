package com.pdceng.www.desirepaths;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Timestamp;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by alondon on 8/3/2017.
 */

public class CommentsAdapter extends BaseAdapter {
    TextView tvRating;
    Bundle bundle;

    List<String> mData;
    DatabaseHelper dh;

    Context mContext;
    LayoutInflater inflater=null;
    String id;
    ImageView ivProfile;


//    public CommentsAdapter(@NonNull Context context, @LayoutRes int resource, @NonNull List<Bundle> objects) {
//        super(context, resource, objects);
//        layoutResource = resource;
//    }

    public CommentsAdapter(Context context, List<String> mData){
        super();
        this.mData = mData;
        this.mContext = context;
        inflater = LayoutInflater.from(mContext);
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public List<String> getData(){
        return mData;
    }

    @NonNull
    @Override
    public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        dh = new DatabaseHelper(mContext);
        if(convertView==null)
            convertView = inflater.inflate(R.layout.comment,null);

        id = mData.get(position);
        bundle = dh.getRow(new CommentsTable(),CommentsTable.ID, id);

        Bundle userBundle = dh.getRow(new UserTable(), UserTable.FACEBOOK_ID, bundle.getString(CommentsTable.USER));

        tvRating = (TextView) convertView.findViewById(R.id.rating);
        TextView tvUser = (TextView) convertView.findViewById(R.id.user);
        TextView tvComment = (TextView) convertView.findViewById(R.id.comment);
        TextView tvDate = (TextView) convertView.findViewById(R.id.date);

        ImageButton ibUp = (ImageButton) convertView.findViewById(R.id.upArrow);
        ImageButton ibDown = (ImageButton) convertView.findViewById(R.id.downArrow);

        ivProfile = (ImageView) convertView.findViewById(R.id.ivProfile);

        tvRating.setText(bundle.getString(CommentsTable.RATING));
        tvComment.setText(bundle.getString(CommentsTable.COMMENT));
        tvUser.setText(userBundle.getString(UserTable.NAME));
        tvDate.setText(getDuration(bundle.getString(CommentsTable.TIMESTAMP)));

        new DownloadImageTask(ivProfile).execute(getFacebookProfileURL(userBundle.getString(UserTable.FACEBOOK_ID)));

        int ratingGiven = dh.checkRatingGiven(mData.get(position));
        switch (ratingGiven){
            case DatabaseHelper.NO_RATING_GIVEN:
                ibDown.setAlpha(.5f);
                ibUp.setAlpha(.5f);
                break;
            case DatabaseHelper.NEG_RATING_GIVEN:
                ibDown.setAlpha(1f);
                ibUp.setAlpha(.5f);
                break;
            case DatabaseHelper.POS_RATING_GIVEN:
                ibUp.setAlpha(1f);
                ibDown.setAlpha(.5f);
            }

        ibUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rateChangerClicked(true,mData.get(position));
            }
        });

        ibDown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rateChangerClicked(false,mData.get(position));
            }
        });

        return convertView;
    }

    private void rateChangerClicked(boolean positive, String commentId){
        dh.adjustRating(positive,commentId);
        ((MapsActivity)mContext).setCommentsAdapter(bundle.getString(CommentsTable.PIEntry_ID));
    }

    private String getDuration(String timestamp) {
        //The variable constants below describe how many milliseconds (a millisecond = 1/1000 of a second) are in the given
        //unit of time.
        final long SECOND = 1_000;
        final long MINUTE = 60_000;
        final long HOUR = 3_600_000;
        final long DAY = 86_400_000;
        //The following variable constants describe how many days are in the given unit of time.
        final int WEEK = 7;
        final int MONTH = 30;
        final int YEAR = 365;

        //A timestamp is a java class that stores detailed time information.
        //'postedTime' is a Timestamp value of the date argument to be compared.
        //'now' is a Timstamp create from the system's current time.

        Timestamp firstTime = new Timestamp(Long.valueOf(timestamp));
        Timestamp lastTime = new Timestamp(System.currentTimeMillis());
        //The following variables contain the millisecond and day difference between these two TimeStamps.
        long compareToMillis = TimeUnit.MILLISECONDS.toMillis(lastTime.getTime() - firstTime.getTime());
        long compareToDays = TimeUnit.MILLISECONDS.toDays(lastTime.getTime() - firstTime.getTime());
        String result = null;

        //This if/else routine finds the most appropriate description of the time between the date argument and now.
        int value = 0;
        if (compareToMillis < MINUTE) {
            value = (int) (compareToMillis / SECOND);
            if (value==1) result = value + " second";
            else result = value + " seconds";
        } else if (compareToMillis > MINUTE && compareToMillis < HOUR) {
            value = (int) (compareToMillis / MINUTE);
            if (value==1) result = value + " minute";
            else result = value + " minutes";
        } else if (compareToMillis > HOUR && compareToMillis < DAY) {
            value = (int) (compareToMillis / HOUR);
            if (value==1) result = value + " hour";
            else result = value + " hours";
        } else if (compareToMillis > DAY && compareToDays < WEEK) {
            value = (int) (compareToMillis / DAY);
            if (value==1) result = value + " day";
            else result = value + " days";
        } else if (compareToDays > WEEK && compareToDays < MONTH) {
            value = (int) (compareToMillis / WEEK);
            if (value==1) result = value + " week";
            else result = value + " weeks";
        } else if (compareToDays > MONTH && compareToDays < YEAR) {
            value = (int) (compareToMillis / MONTH);
            if (value==1) result = value + " month";
            else result = value + " months";
        } else if (compareToDays > YEAR) {
            value = (int) (compareToMillis / YEAR);
            if (value==1) result = value + " year";
            else result = value + " years";
        }
        return result + " ago";
    }

    private String getFacebookProfileURL(String userID){
        return "https://graph.facebook.com/" + userID + "/picture?type=large";
    }

    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;

        public DownloadImageTask(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            ivProfile.setImageBitmap(null);
        }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            System.out.println(urldisplay);
            Bitmap mIcon11 = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {
            bmImage.setImageBitmap(result);
        }
    }
}