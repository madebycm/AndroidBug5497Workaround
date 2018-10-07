import android.app.Activity;
import android.graphics.Rect;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.FrameLayout;

public class AndroidBug5497Workaround {
    private static final String TAG = "aBug5497";

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
        
        
        InputMethodManager imm = (InputMethodManager) activity
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        
        int usableHeightNow = computeUsableHeight();
        if (usableHeightNow != usableHeightPrevious) {
            int usableHeightSansKeyboard = mChildOfContent.getRootView().getHeight();
            int heightDifference = usableHeightSansKeyboard - usableHeightNow;
            if (heightDifference > (usableHeightSansKeyboard / 4)) {

                if(BuildConfig.DEBUG){
                    // keyboard probably just became visible
                    Log.v(TAG, "keyboard probably just became visible");
                }
                
                if (imm.isAcceptingText()) { 
                    
                    // keyboard is shown
                    if(BuildConfig.DEBUG){
                        Log.d(TAG,"Software keyboard is open");
                    }
                    frameLayoutParams.height = usableHeightSansKeyboard - heightDifference;
                    frameLayoutParams.topMargin = getStatusBarHeight();
                    
                    activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
                    activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
                    
                } else {
                    // keyboard is hidden
                    if(BuildConfig.DEBUG) {
                        Log.d(TAG, "Software Keyboard is closed");
                    }

                }
            } else {


                // keyboard probably just became hidden
                if (BuildConfig.DEBUG) {
                    Log.v(TAG, "keyboard probably just became hidden");
                }

                if (imm.isAcceptingText()) {
                    // keyboard is shown
                    if (BuildConfig.DEBUG) {
                        Log.d(TAG, "Software Keyboard is open");
                    }
                } else {
                    // keyboard is hidden
                    if(usableHeightPrevious != 0) {
                        frameLayoutParams.height = usableHeightSansKeyboard;
                        activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
                        activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

                    }

                    frameLayoutParams.topMargin = 0;

                    if(BuildConfig.DEBUG) {
                        Log.d(TAG, "Software Keyboard is closed");
                    }
                }
            }

            mChildOfContent.requestLayout();
            usableHeightPrevious = usableHeightNow;

            if(BuildConfig.DEBUG){
                Log.v(TAG, "frameLayoutParams.height="+frameLayoutParams.height);
                Log.v(TAG, "mChildOfContent.getTop()="+mChildOfContent.getTop());
                Log.v(TAG, "mChildOfContent.getBottom()="+mChildOfContent.getBottom());
                Log.v(TAG, "frameLayoutParams.topMargin="+frameLayoutParams.topMargin);
                Log.v(TAG, "frameLayoutParams.bottomMargin="+frameLayoutParams.bottomMargin);
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
