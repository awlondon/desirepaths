package com.pdceng.www.desirepaths;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.text.InputType;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnSuccessListener;

import org.jibble.simpleftp.SimpleFTP;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Timestamp;

import static android.graphics.Color.LTGRAY;
import static android.widget.LinearLayout.VERTICAL;
import static com.pdceng.www.desirepaths.Universals.chooseLocation;

public class PublicInputAddActivity extends AppCompatActivity implements AfterGetAll {

    private Context mContext = this;
    private DatabaseHelper dh = new DatabaseHelper(this);
    private boolean mLocationPermissionGranted;
    private LatLng mCurrLatLng;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_public_input);

        RelativeLayout relativeLayout = new RelativeLayout(this);

        Bitmap imageBitmap = Universals.bitmapBeingProcessed;
        if (imageBitmap != null) {
            imageBitmap = Universals.sampleBitmap(imageBitmap);

            final ImageView imageView = new ImageView(this);
            ViewGroup.LayoutParams ivParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 700);
            imageView.setLayoutParams(ivParams);
            imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);

            if (imageView.getDrawable() == null) imageView.setImageBitmap(imageBitmap);

            relativeLayout.addView(imageView);
        }

        final RelativeLayout topView = (RelativeLayout) findViewById(R.id.topView);
        final CardView cardView = new CardView(mContext);

        CardView.LayoutParams params = new CardView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
//        params.setMargins(30, 30, 30, 200);
        cardView.setLayoutParams(params);

        cardView.setCardBackgroundColor(getColor(R.color.white));
        cardView.setCardElevation(20f);
        cardView.setClickable(true);

        topView.addView(cardView);

        relativeLayout.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        final LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(VERTICAL);
        linearLayout.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        final EditText titleEdit = new EditText(this);
        final EditText snippetEdit = new EditText(this);
        final Spinner spinner = new Spinner(this);
        Button button = new Button(this);
        button.setText(R.string.addButton);
        button.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        titleEdit.setHint("Add a title");
        titleEdit.setInputType(InputType.TYPE_TEXT_FLAG_IME_MULTI_LINE);
        snippetEdit.setHint("Your message");
        snippetEdit.setInputType(InputType.TYPE_TEXT_FLAG_MULTI_LINE);

        spinner.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, new String[]{"(Type)", "Positive", "Neutral", "Negative"}));

        //Close window 'button'
        ImageView closeWindow = createCloseWindowButton(cardView, topView);

        relativeLayout.addView(closeWindow);

        linearLayout.addView(relativeLayout);

        linearLayout.addView(titleEdit);
        linearLayout.addView(snippetEdit);
        linearLayout.addView(spinner);
        linearLayout.addView(button);

        cardView.addView(linearLayout);

        titleEdit.requestFocus();
        final InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.toggleSoftInputFromWindow(linearLayout.getApplicationWindowToken(), InputMethodManager.SHOW_IMPLICIT, 0);

        SendImageFTP sendImageFTP = null;
        if (imageBitmap != null) {
            sendImageFTP = new SendImageFTP(imageBitmap, this);
            sendImageFTP.execute();
        }

        final Bitmap finalImageBitmap = imageBitmap;
        final SendImageFTP finalSendImageFTP = sendImageFTP;

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //START: Check if all fields are filled
                for (int i = 0; i < linearLayout.getChildCount(); i++) {
                    if (linearLayout.getChildAt(i) instanceof EditText) {
                        EditText et = (EditText) linearLayout.getChildAt(i);
                        if (et.getText() == null || et.getText().toString().length() < 2) {
                            inputMethodManager.toggleSoftInputFromWindow(cardView.getApplicationWindowToken(), InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
                            Snackbar.make(findViewById(android.R.id.content),
                                    "Please complete all the fields",
                                    Snackbar.LENGTH_INDEFINITE).setAction("OKAY",
                                    new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            inputMethodManager.toggleSoftInputFromWindow(cardView.getApplicationWindowToken(), InputMethodManager.SHOW_IMPLICIT, 0);
                                        }
                                    }).show();
//                            Toast.makeText(MapActivity.this, "Please complete all the fields", Toast.LENGTH_SHORT).show();
                            return;
                        }
                    } else if (linearLayout.getChildAt(i) instanceof Spinner) {
                        Spinner spinner = (Spinner) linearLayout.getChildAt(i);
                        if (spinner.getSelectedItemPosition() == 0) {
                            inputMethodManager.toggleSoftInputFromWindow(cardView.getApplicationWindowToken(), InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
                            Snackbar.make(findViewById(android.R.id.content),
                                    "Please choose an option from the spinner",
                                    Snackbar.LENGTH_INDEFINITE).setAction("OKAY",
                                    new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                        }
                                    }).show();
//                            Toast.makeText(MapActivity.this, "Please choose an option from the spinner", Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }
                }
                //END: Check if all fields are filled

                //Remember field values
                final String sentiment = spinner.getSelectedItem().toString().toLowerCase();
                final String title = titleEdit.getText().toString();
                final String snippet = snippetEdit.getText().toString();

                System.out.println("Checking location...");
                //Check & requests location; handles outcome
                if (Universals.mapActivity.checkPermission(android.Manifest.permission.ACCESS_FINE_LOCATION, mLocationPermissionGranted) > 0) {
                    if (ActivityCompat.checkSelfPermission(mContext,
                            android.Manifest.permission.ACCESS_FINE_LOCATION) !=
                            PackageManager.PERMISSION_GRANTED &&
                            ActivityCompat.checkSelfPermission(mContext, android.Manifest.permission.ACCESS_COARSE_LOCATION)
                                    != PackageManager.PERMISSION_GRANTED) {
                        Toast.makeText(mContext, "Cannot access location!", Toast.LENGTH_SHORT).show();
                        System.out.println("Permission not granted!");
                        Universals.mapActivity.chooseLocationExec(false, title, snippet, sentiment, finalImageBitmap, finalSendImageFTP, (Activity) mContext);
                        finish();
                    } else {
                        System.out.println("Permission granted!");
                        Universals.mapActivity.mFusedLocationClient.getLastLocation().addOnSuccessListener((Activity) mContext, new OnSuccessListener<Location>() {
                            @Override
                            public void onSuccess(Location location) {
                                if (location == null) {
                                    System.out.println("location is not known...");
                                    System.out.println("asking to choose location...");
                                    Toast.makeText(mContext, "Location permission was granted, but service can't find location. Please turn location on from device settings.", Toast.LENGTH_SHORT).show();
                                    Universals.mapActivity.chooseLocationExec(false, title, snippet, sentiment, finalImageBitmap, finalSendImageFTP, (Activity) mContext);
                                } else {
                                    System.out.println("location is known...");
                                    mCurrLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                                    Universals.mapActivity.mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mCurrLatLng, 15));
                                    if (chooseLocation) {
                                        System.out.println("asking to choose location...");
                                        Universals.mapActivity.chooseLocationExec(true, title, snippet, sentiment, finalImageBitmap, finalSendImageFTP, (Activity) mContext);
                                    } else {
                                        System.out.println("don't need to know location...");
                                        Bundle bundle = new Bundle();
                                        bundle.putString(PIEntryTable.URL, finalImageBitmap != null ? finalSendImageFTP.getFilename() : "");
                                        bundle.putString(PIEntryTable.LATITUDE, String.valueOf(location.getLatitude()));
                                        bundle.putString(PIEntryTable.LONGITUDE, String.valueOf(location.getLongitude()));
                                        bundle.putString(PIEntryTable.SENTIMENT, sentiment);
                                        bundle.putString(PIEntryTable.TITLE, title);
                                        bundle.putString(PIEntryTable.SNIPPET, snippet);
                                        bundle.putString(PIEntryTable.USER, Universals.NAME);
                                        bundle.putString(PIEntryTable.TIMESTAMP, new Timestamp(System.currentTimeMillis()).toString());
                                        dh.insert(bundle, new PIEntryTable());
                                        Toast.makeText(mContext, "Content added!", Toast.LENGTH_SHORT).show();
                                        Universals.mapActivity.addItems(new String[]{null});
                                        finish();
                                    }
                                }
                            }
                        });
                    }
                }
            }
        });
    }

    private ImageView createCloseWindowButton(final View cardView, final RelativeLayout topView) {
        final ImageView closeWindow = new ImageView(mContext);
        closeWindow.setImageDrawable(getDrawable(R.drawable.ic_dialog_close_dark));
        closeWindow.setColorFilter(LTGRAY);
        RelativeLayout.LayoutParams fabParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        fabParams.setMargins(10, 10, 0, 0);
        fabParams.addRule(RelativeLayout.ALIGN_PARENT_END);
        closeWindow.setLayoutParams(fabParams);
        closeWindow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dh.getAllFromSQL((AfterGetAll) mContext);
                finish();
            }
        });
        return closeWindow;
    }

    @Override
    public void afterGetAll() {
        Universals.mapActivity.setUpCluster();
    }

    class SendImageFTP extends AsyncTask<Void, Integer, String> {
        Bitmap bitmap;
        Context context;
        String filename;
        ProgressDialog progressDialog;

        SendImageFTP(Bitmap bitmap, Context context) {
            this.bitmap = bitmap;
            this.context = context;
            this.filename = Universals.SOCIAL_MEDIA_ID + "_" + String.valueOf(System.currentTimeMillis() + ".jpg");
        }

        @Override
        protected String doInBackground(Void... params) {
            //converts bitmap to image file
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);
            File file = new File(Environment.getExternalStorageDirectory() + File.separator + filename);

            try {
                FileOutputStream fos = new FileOutputStream(file);
                fos.write(bos.toByteArray());
                fos.flush();
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            SimpleFTP ftp = new SimpleFTP();

            try {
                ftp.connect("153.92.6.4", 21, getString(R.string.ftp_username), getString(R.string.ftp_password));
                ftp.bin();
                if (ftp.stor(file)) {
                    // TODO: 8/16/2017 Close progress bar on main thread
                    progressDialog.dismiss();
                }
                ftp.disconnect();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = ProgressDialog.show(mContext, "Uploading image", null, true);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
        }

        String getFilename() {
            return filename;
        }
    }
}