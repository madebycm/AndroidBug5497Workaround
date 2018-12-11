import android.app.Activity;
import android.graphics.Rect;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.FrameLayout;

public class AndroidBug5497Workaround {
    private static final String TAG = "aBug5497";

    // For more information, see https://code.google.com/p/android/issues/detail?id=5497
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
                    
                    //frameLayoutParams.topMargin = getStatusBarHeight(activity);
                    //((CustomActivity)activity).setFullscreenWithStatusbarHeight(getStatusBarHeight(activity), true);//Make your own Method....
                    
                    activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);//could be handled by activity...
                    activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);//could be handled by activity...
                    
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
                        activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);//could be handled by activity...
                        activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);//could be handled by activity...

                    }

                    //frameLayoutParams.topMargin = 0;
                    //((CustomActivity)activity).setKeyboardViewWithStatusbarHeight(getStatusBarHeight(activity), false);//Make your own Method....

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


    public static int getStatusBarHeight(Activity activity){

        int result = 0;
        int resourceId = activity.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = activity.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    // https://stackoverflow.com/a/34092477/1815624
    public static int getNavigationBarHeight(Activity activity){
        boolean hasMenuKey = ViewConfiguration.get(activity).hasPermanentMenuKey();
        boolean hasBackKey = KeyCharacterMap.deviceHasKey(KeyEvent.KEYCODE_BACK);
        int result = 0;
        if(!hasMenuKey && !hasBackKey) {
            Resources resources = activity.getResources();

            int orientation = resources.getConfiguration().orientation;
            int resourceId;
            if (isTablet(activity)){
                resourceId = resources.getIdentifier(orientation == Configuration.ORIENTATION_PORTRAIT ? "navigation_bar_height" : "navigation_bar_height_landscape", "dimen", "android");
            } else {
                resourceId = resources.getIdentifier(orientation == Configuration.ORIENTATION_PORTRAIT ? "navigation_bar_height" : "navigation_bar_width", "dimen", "android");
            }
            if (resourceId > 0) {
                result = activity.getResources().getDimensionPixelSize(resourceId);
            }
        }
        return result;
    }

    //https://stackoverflow.com/a/29938139/1815624   
    private static boolean isTablet(Context c) {
        return (c.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK)
                >= Configuration.SCREENLAYOUT_SIZE_LARGE;
    }
}
