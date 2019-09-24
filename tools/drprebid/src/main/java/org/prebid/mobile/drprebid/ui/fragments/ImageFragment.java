package org.prebid.mobile.drprebid.ui.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import org.prebid.mobile.drprebid.R;

public class ImageFragment extends Fragment {
    private static final String IMAGE_RESOURCE_ARG = "image_res";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_image, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getArguments() != null && getArguments().containsKey(IMAGE_RESOURCE_ARG)) {
            ImageView imageView = view.findViewById(R.id.view_image);
            imageView.setImageResource(getArguments().getInt(IMAGE_RESOURCE_ARG, -1));
        }
    }

    public static ImageFragment newInstance(int imageResource) {

        Bundle args = new Bundle();
        args.putInt(IMAGE_RESOURCE_ARG, imageResource);

        ImageFragment fragment = new ImageFragment();
        fragment.setArguments(args);
        return fragment;
    }
}
