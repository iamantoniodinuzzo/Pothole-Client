package com.indisparte.pothole.view.adapter;

import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;

import androidx.appcompat.widget.AppCompatToggleButton;
import androidx.appcompat.widget.Toolbar;

import com.indisparte.pothole.util.DotProgressView;
import com.indisparte.pothole.util.Mode;

/**
 * @author Antonio Di Nuzzo (Indisparte)
 */
public class BindingAdapter {

    @androidx.databinding.BindingAdapter(value = {"permission"})
    public static void shouldShowProgressBar(ProgressBar progressBar,Boolean permission) {
        if (permission){
            progressBar.setVisibility(View.GONE);
        }else {
            progressBar.setVisibility(View.VISIBLE);
        }
    }

    @androidx.databinding.BindingAdapter(value = {"permission"})
    public static void shouldShowTrackingButton(AppCompatToggleButton button, Boolean permission) {
        if (permission){
            button.setVisibility(View.VISIBLE);
        }else {
            button.setVisibility(View.GONE);
        }
    }

    @androidx.databinding.BindingAdapter(value = {"permission","mode"})
    public static void shouldShowLocateMeButton(ImageButton button, Boolean permission, Mode mode) {
        if (permission){
           switch (mode){
               case LOCATION:
                   button.setVisibility(View.VISIBLE);
                   break;
               case TRACKING:
                   button.setVisibility(View.GONE);
                   break;
           }
        }else {
            button.setVisibility(View.GONE);
        }
    }

    @androidx.databinding.BindingAdapter(value = {"mode"})
    public static void shouldShowAnimatedView(DotProgressView progressView, Mode mode) {
            switch (mode){
                case LOCATION:
                    progressView.setVisibility(View.GONE);
                    break;
                case TRACKING:
                    progressView.setVisibility(View.VISIBLE);
                    break;
            }

    }

    @androidx.databinding.BindingAdapter(value = {"mode"})
    public static void customizeToolbar(Toolbar toolbar, Mode mode) {
        switch (mode){
            case LOCATION:
                toolbar.setTitle("Map");
                break;
            case TRACKING:
                toolbar.setTitle("Tracking");
                break;
        }

    }
}
