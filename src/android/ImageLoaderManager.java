package org.apache.cordova.contactlist;

import android.content.ContentUris;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
            return photo;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    public static interface ImageLoaderTask extends Runnable {
        int getHashCode();
    }
}
