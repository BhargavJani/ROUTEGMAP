package com.project.routeapidemo;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Build;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class Permission_Class {

    boolean returnvalue;


        boolean getPermission(Context context, String PERMISSION_CONSTANT, int STORAGE_PERMISSION_CODE, String Message) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

                //First checking if the app is already having the permission
                if (check_Permission_Granted(context, PERMISSION_CONSTANT, STORAGE_PERMISSION_CODE, Message)) {
                    //If permission is already having then showing the toast
                    Toast.makeText(context, "You already have the permission", Toast.LENGTH_LONG).show();
                    //Existing the method with return
                    return true;
                }
                else {
                    if (requestForPermission((Activity) context,PERMISSION_CONSTANT,STORAGE_PERMISSION_CODE,Message))
                    {
                        return true;
                    }

                    return false;
                }
            }
            else {
                return true;
            }
        }










    boolean check_Permission_Granted(Context context, String PERMISSION_CONSTANT, int STORAGE_PERMISSION_CODE, String Message){
        int result= ContextCompat.checkSelfPermission(context, PERMISSION_CONSTANT);
        if (result == PackageManager.PERMISSION_GRANTED)
            return true;
        return false;
        //If permission is not granted returning false

    }

     boolean requestForPermission(final Activity activity, final String PERMISSION_CONSTANT, final int  STORAGE_PERMISSION_CODE, String Message)
     {

        if (ActivityCompat.shouldShowRequestPermissionRationale(activity,PERMISSION_CONSTANT)){
            //If the user has denied the permission previously your code will come to this block
            //Here you can explain why you need this permission
            //Explain here why you need this permission
            new AlertDialog.Builder(activity).setTitle("Permission Dialog").setMessage(Message)
                    .setPositiveButton("Allow", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    ActivityCompat.requestPermissions(activity,new String[]{PERMISSION_CONSTANT},STORAGE_PERMISSION_CODE);
                    returnvalue=true;
                }
            }).setNegativeButton("Cancle", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Toast.makeText(activity, "App not work properly", Toast.LENGTH_SHORT).show();
                    returnvalue=false;

                }
            }).show();
            return returnvalue;

        }



        //And finally ask for the permission
        ActivityCompat.requestPermissions(activity,new String[]{PERMISSION_CONSTANT},STORAGE_PERMISSION_CODE);
         return false;
     }
}
