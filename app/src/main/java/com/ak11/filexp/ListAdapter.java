package com.ak11.filexp;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.VideoView;

import com.ak11.filexp.databinding.MyListImageItemBinding;
import com.ak11.filexp.databinding.MyListItemBinding;
import com.ak11.filexp.databinding.MyListVideoItemBinding;
import com.shashank.sony.fancytoastlib.FancyToast;

import java.io.File;
import java.lang.reflect.Array;
import java.net.URLConnection;
import java.util.Arrays;

public class ListAdapter extends BaseAdapter  {

    Context context;
    LayoutInflater layoutInflater;

    File currentPath;
    String[] files;
    MyListItemBinding itemBinding;
    MyListImageItemBinding imageItemBinding;
    MyListVideoItemBinding videoItemBinding;


    public ListAdapter(Context context, File currentPath) {
        this.context = context;
        layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        this.currentPath = currentPath;
        files = currentPath.list();
        Arrays.sort(files);


    }

    @Override
    public int getCount() {
        return files.length;
    }

    @Override
    public Object getItem(int position) {
        return files[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {

        if(isImage(files[position])) {
            view = layoutInflater.inflate(R.layout.my_list_image_item, null);
            imageItemBinding = MyListImageItemBinding.bind(view);

            imageItemBinding.txtItem.setText(files[position]);
            updateImage(imageItemBinding.imgThumbnail, position);

        }
        else if(isVideoFile(files[position])){

            view = layoutInflater.inflate(R.layout.my_list_video_item, null);
            videoItemBinding = MyListVideoItemBinding.bind(view);

            videoItemBinding.txtItem.setText(files[position]);
            updateVideoView(videoItemBinding.videoView,position);



        }
        else {
            view = layoutInflater.inflate(R.layout.my_list_item, null);
            itemBinding = MyListItemBinding.bind(view);
            itemBinding.txtItem.setText(files[position]);
        }

//        view.setOnCreateContextMenuListener(new View.OnCreateContextMenuListener() {
//            @Override
//            public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
//                menu.add(Menu.NONE,position,0," Delete");
//                menu.add(Menu.NONE,position,1,"Edit");
//            }
//        });



        return view;
    }

    private void updateVideoView(VideoView videoView, int position) {

        MediaController mediaController = new MediaController(context);
        videoView.setVideoPath(getFile(position).getAbsolutePath());
        videoView.setMediaController(mediaController);
        mediaController.setAnchorView(videoView);
        videoView.seekTo(1);



    }

    public File getFile(int position){
        File newFile = new File(currentPath,files[position]);
        return newFile;

    }

    private void updateImage(ImageView imageView, int position){
        File imageFile = new File(currentPath,files[position]);
        int reqHeight = imageView.getMaxHeight();
        int reqWidth = imageView.getMaxWidth();
        Bitmap image = decodeSampledBitmapFromPath(imageFile.getAbsolutePath(),reqWidth,reqHeight);
        imageView.setImageBitmap(image);
    }

    public static Bitmap decodeSampledBitmapFromPath(String picPath, int reqWidth, int reqHeight) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(picPath, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(picPath, options);
    }

    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    public boolean isImage(String path){
//        String [] extensions = new String[] {
//                "jpg",
//                "png",
//                "gif",
//                "jpeg"
//        };
//
//        for (String extension: extensions) {
//            if (picName.toLowerCase().endsWith(extension)) {
//                return true;
//            }
//        }
//        return false;

        String mimeType = URLConnection.guessContentTypeFromName(path);
        return mimeType != null && mimeType.startsWith("image");
    }

    public boolean isVideoFile(String path) {
        String mimeType = URLConnection.guessContentTypeFromName(path);
        return mimeType != null && mimeType.startsWith("video");
    }

    public boolean isTextFile(String path) {
        String mimeType = URLConnection.guessContentTypeFromName(path);
        return mimeType != null && mimeType.startsWith("text");
    }



}
