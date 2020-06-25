package com.symbol.multiuserstorage;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

public class FirstFragment extends Fragment {
    private static final String TAG = "MultiUserTestApp";
    private static final int WRITE_EXTERNAL_STORAGE_REQUEST_CODE = 54654;
    private MainActivity mActivity;
    boolean mExternalStorageAvailable = false;
    boolean mExternalStorageWriteable = false;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_first, container, false);
    }


    /** Method to check whether external media available and writable. This is adapted from
     http://developer.android.com/guide/topics/data/data-storage.html#filesExternal */

    private void checkExternalMedia(){

        String state = Environment.getExternalStorageState();

        if (Environment.MEDIA_MOUNTED.equals(state)) {
            // Can read and write the media
            mExternalStorageAvailable = mExternalStorageWriteable = true;
             if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
                 // Can only read the media
                 mExternalStorageAvailable = true;
                 mExternalStorageWriteable = false;
             }
             else{
                 mExternalStorageAvailable = true;
                 mExternalStorageWriteable = true;
             }
        } else {
            // Can't read or write
            mExternalStorageAvailable = mExternalStorageWriteable = false;
        }
        Log.v(TAG,"\n\nExternal Media: readable="
                +mExternalStorageAvailable+" writable="+mExternalStorageWriteable);
    }

    public File getPublicAlbumStorageDir(String albumName) {
        // Get the directory for the user's public pictures directory.
        File file = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), albumName);
        if (!file.mkdirs()) {
            Log.e(TAG, "Directory not created");
        }
        return file;
    }

    public File getPrivateAlbumStorageDir(String albumName) {
        // Get the directory for the user's public pictures directory.
        File file = new File(mActivity.getExternalFilesDirs(
                Environment.DIRECTORY_PICTURES)[1], albumName);
        if (!file.mkdirs()) {
            Log.e(TAG, "Directory not created");
        }
        return file;
    }

    /**
     * @return true if the device or profile is already owned
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static boolean isManaged(Context context) {
        DevicePolicyManager devicePolicyManager = (DevicePolicyManager) context.getSystemService(
                Context.DEVICE_POLICY_SERVICE);

        List<ComponentName> admins = devicePolicyManager.getActiveAdmins();
        if (admins == null) return false;
        for (ComponentName admin : admins) {
            String adminPackageName = admin.getPackageName();
            if (devicePolicyManager.isDeviceOwnerApp(adminPackageName)
                    || devicePolicyManager.isProfileOwnerApp(adminPackageName)) {
                return true;
            }
        }

        return false;
    }



    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        view.findViewById(R.id.button_first).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NavHostFragment.findNavController(FirstFragment.this)
                        .navigate(R.id.action_FirstFragment_to_SecondFragment);
            }
        });
        mActivity = (MainActivity)getActivity();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, WRITE_EXTERNAL_STORAGE_REQUEST_CODE);
            //return;
        }

        // check whether we are running under profile owner
                if (!(isManaged(mActivity.getApplicationContext()))) {
                    // creating file in external storage public directory pictures
                    checkExternalMedia();

                    if (mExternalStorageWriteable) {
                        File[] filearr = mActivity.getExternalFilesDirs(null);
                        filearr = getActivity().getExternalFilesDirs(null);
                        //filearr= mActivity.getExternalFilesDirs(null);
                        File[] Dirs = ContextCompat.getExternalFilesDirs(mActivity.getApplicationContext(), null);

                        if (Dirs[0]!=null)
                            Log.v(TAG, "\n\n Storage External Location 1 " + Dirs[0] +Dirs.length );

                        if (filearr[0]!=null)
                        Log.v(TAG, "\n\n Storage External Location 1 " + filearr[0] +filearr.length );
                        if (filearr[1]!=null) {

                            Log.v(TAG, "\n\n Storage External Location 2 " + filearr[1]);
                            File dir = getPrivateAlbumStorageDir("MyPublicAlbum");
                            File file = new File(dir, "myData.txt");
                            try {
                                FileOutputStream f = new FileOutputStream(file);
                                PrintWriter pw = new PrintWriter(f);
                                pw.println("Hi , How are you");
                                pw.println("Hello");
                                pw.flush();
                                pw.close();
                                f.close();
                                Toast.makeText(mActivity.getApplicationContext(), "File created in external SD Card", Toast.LENGTH_SHORT).show();
                            } catch (FileNotFoundException e) {
                                e.printStackTrace();
                                Log.i(TAG, "******* File not found. Did you" +
                                        " add a WRITE_EXTERNAL_STORAGE permission to the   manifest?");
                                Toast.makeText(mActivity.getApplicationContext(), "File Failed", Toast.LENGTH_SHORT).show();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    } else {
                        Toast.makeText(mActivity.getApplicationContext(), "No External SD Card", Toast.LENGTH_SHORT).show();
                    }
                } else { // running under managed profile
                    // creating file in external storage public directory pictures
                        checkExternalMedia();

                        if (mExternalStorageWriteable) {

                            File[] filearr = mActivity.getExternalFilesDirs(Environment.DIRECTORY_PICTURES);

                            if (filearr[0]!=null)
                                Log.v(TAG, "\n\n Storage External Location 1 " + filearr[0] + "Length " +filearr.length );

                            Toast.makeText(mActivity.getApplicationContext(), "Cant Access EXT in Managed Profile", Toast.LENGTH_SHORT).show();

                        } else {
                            Toast.makeText(mActivity.getApplicationContext(), "No External SD Card", Toast.LENGTH_SHORT).show();
                        }


                }

    }
}
