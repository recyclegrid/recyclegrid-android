package com.recyclegrid.app;

import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

public class Toast {
    private AppCompatActivity _context;
    private View _toastView;

    public Toast(AppCompatActivity context) {
        _context = context;

        LayoutInflater inflater =  LayoutInflater.from(_context);

        _toastView = inflater.inflate(R.layout.toast_view, (ViewGroup) _context.findViewById(R.id.error_toast_container));
    }

    public void showError(int resourceId){
        showError(_context.getString(resourceId));
    }

    public void showError(String text) {
        TextView errorText = _toastView.findViewById(R.id.text_message);
        ImageView icon = _toastView.findViewById(R.id.image_message_icon);

        icon.setBackgroundColor(Color.parseColor("#e80c4d"));
        icon.setImageResource(R.drawable.ic_error_white_24dp);

        errorText.setText(text);

        show(android.widget.Toast.LENGTH_SHORT);
    }

    public void showInformation(int resourceId){
        showInformation(_context.getString(resourceId));
    }

    public void showInformation(String text) {
        TextView errorText = _toastView.findViewById(R.id.text_message);
        ImageView icon = _toastView.findViewById(R.id.image_message_icon);

        icon.setBackgroundResource(R.drawable.ic_info_white_24dp);
        icon.setBackgroundColor(Color.parseColor("#00ACE0"));

        errorText.setText(text);

        show(android.widget.Toast.LENGTH_SHORT);
    }

    private void show(int duration) {
        android.widget.Toast toast = new android.widget.Toast(_context);

        toast.setDuration(duration);
        toast.setGravity(Gravity.BOTTOM, 0, 24);
        toast.setView(_toastView);

        toast.show();
    }
}
