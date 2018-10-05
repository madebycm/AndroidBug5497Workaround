import android.app.Activity;
import android.graphics.Rect;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.FrameLayout;

public class AndroidBug5497Workaround {

    // For more information, see https://issuetracker.google.com/issues/36911528
    // To use this class, simply invoke assistActivity() on an Activity that already has its content view set.

    private View mChildOfContent;
    private int usableHeightPrevious;
    private FrameLayout.LayoutParams frameLayoutParams;
    Activity activity;

    private AndroidBug5497Workaround(Activity activity) {
        this.activity = activity;
        FrameLayout content = activity.findViewById(android.R.id.content);
        mChildOfContent = content.getChildAt(0);
        mChildOfContent.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            public void onGlobalLayout() {
                possiblyResizeChildOfContent();
            }
        });
        frameLayoutParams = (FrameLayout.LayoutParams) mChildOfContent.getLayoutParams();
    }

    public static void assistActivity(Activity activity) {
        new AndroidBug5497Workaround(activity);
    }

    private void possiblyResizeChildOfContent() {
        int usableHeightNow = computeUsableHeight();
        if (usableHeightNow != usableHeightPrevious) {
            int usableHeightSansKeyboard = mChildOfContent.getRootView().getHeight();
            int heightDifference = usableHeightSansKeyboard - usableHeightNow;
            if (heightDifference > (usableHeightSansKeyboard / 4)) {

                // keyboard probably just became visible
                frameLayoutParams.height = usableHeightSansKeyboard - heightDifference;

                frameLayoutParams.topMargin = getStatusBarHeight();
                activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
                activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);

                if(BuildConfig.DEBUG){
                    Log.v("aBug5497", "keyboard probably just became visible");
                }
            } else {

                // keyboard probably just became hidden
                if(usableHeightPrevious != 0) {
                    frameLayoutParams.height = usableHeightSansKeyboard;
                    activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
                    activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

                }

                frameLayoutParams.topMargin = 0;

                if(BuildConfig.DEBUG){
                    Log.v("aBug5497", "keyboard probably just became hidden");
                }
            }

            mChildOfContent.requestLayout();
            usableHeightPrevious = usableHeightNow;

            if(BuildConfig.DEBUG){
                Log.v("aBug5497", "frameLayoutParams.height="+frameLayoutParams.height);
                Log.v("aBug5497", "mChildOfContent.getTop()="+mChildOfContent.getTop());
                Log.v("aBug5497", "mChildOfContent.getBottom()="+mChildOfContent.getBottom());
                Log.v("aBug5497", "frameLayoutParams.topMargin="+frameLayoutParams.topMargin);
                Log.v("aBug5497", "frameLayoutParams.bottomMargin="+frameLayoutParams.bottomMargin);
            }
        }
    }

    private int computeUsableHeight() {
        Rect r = new Rect();
        mChildOfContent.getWindowVisibleDisplayFrame(r);
        return (r.bottom - r.top);
    }

    public int getStatusBarHeight() {
        Rect r = new Rect();
        mChildOfContent.getWindowVisibleDisplayFrame(r);
        return r.top;
    }
}
