package com.example.ls;

import android.content.ContentUris;
import android.content.Context;
import android.graphics.*;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Handler;
import android.provider.ContactsContract;
import android.widget.ImageView;

import java.io.InputStream;
import java.util.List;
import java.util.concurrent.*;

public class ImageLoaderManager extends ThreadPoolExecutor {

    private static final int MAX_ACTIVE_DOWNLOAD_TASKS = 4;
    private static final int CHECK_DOWNLOAD_QUEUE_PERIOD = 1;

    private final ConcurrentMap<Integer, ImageLoaderTask> mTasks = new ConcurrentHashMap<Integer, ImageLoaderTask>();
    final Handler mHandler;
    final Context mContext;

    public ImageLoaderManager(Handler handler, Context context) {
        super(MAX_ACTIVE_DOWNLOAD_TASKS / 2, MAX_ACTIVE_DOWNLOAD_TASKS, CHECK_DOWNLOAD_QUEUE_PERIOD, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
        mHandler = handler;
        mContext = context;
    }

    public void execute(ImageLoaderTask task) {
        if (isShutdown() || isTerminated() || isTerminating()) {
            return;
        }

        ImageLoaderTask runnable = mTasks.get(task.getHashCode());
        if (runnable != null) {
            super.remove(runnable);
        }
        mTasks.put(task.getHashCode(), task);
        super.execute(task);
    }


    @Override
    protected void beforeExecute(Thread t, Runnable r) {
        t.setPriority(Thread.MIN_PRIORITY);
        super.beforeExecute(t, r);
    }

    @Override
    protected void afterExecute(Runnable r, Throwable t) {
        super.afterExecute(r, t);

        ImageLoaderTask task = (ImageLoaderTask) r;
        mTasks.remove(task.getHashCode());
    }

    @Override
    public List<Runnable> shutdownNow() {
        mTasks.clear();
        return super.shutdownNow();
    }

    public void loadContactPhoto(ImageView img, SimpleIndexAdapter.Contact contact) {
        execute(new LoadContactPhotoTask(img, contact));
    }

    public class LoadContactPhotoTask implements ImageLoaderTask {
        final ImageView mImageView;
        final SimpleIndexAdapter.Contact mContact;

        public LoadContactPhotoTask(ImageView img, SimpleIndexAdapter.Contact contact) {
            mImageView = img;
            mContact = contact;
            mImageView.setTag(mContact.id);
            mImageView.setImageBitmap(null);
        }

        @Override
        public int getHashCode() {
            return mImageView.hashCode();
        }

        @Override
        public void run() {
            final Bitmap result = getPhoto(mContact.id);
            if (mImageView.getTag().equals(mContact.id)) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mImageView.setImageBitmap(result);
                        mImageView.setBackgroundResource(result != null ? android.R.color.transparent : R.drawable.empty);
                    }
                });
            }
        }
    }

    public Bitmap getPhoto(int contactId) {
        try {
            final Uri contactUri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, contactId);
            final InputStream input = ContactsContract.Contacts.openContactPhotoInputStream(mContext.getContentResolver(), contactUri);
            Bitmap photo = BitmapFactory.decodeStream(input);

            if (photo == null) {
                return null;
            }

            float density = mContext.getResources().getDisplayMetrics().density;

            int targetWidth = (int) (32 * density);
            int targetHeight = (int) (32 * density);

            Bitmap targetBitmap = Bitmap.createBitmap(targetWidth,
                    targetHeight, Bitmap.Config.ARGB_8888);


            Canvas canvas = new Canvas(targetBitmap);
            Path path = new Path();
            path.addCircle(
                    ((float) targetWidth - 1) / 2,
                    ((float) targetHeight - 1) / 2,
                    (Math.min(((float) targetWidth), ((float) targetHeight)) / 2),
                    Path.Direction.CCW);

            canvas.clipPath(path);
            canvas.drawBitmap(
                    photo,
                    new Rect(0, 0, photo.getWidth(), photo
                            .getHeight()), new Rect(0, 0, targetWidth,
                    targetHeight), null);
            return targetBitmap;

            //return photo;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public static interface ImageLoaderTask extends Runnable {
        int getHashCode();
    }
}
