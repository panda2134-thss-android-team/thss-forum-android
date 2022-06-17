package site.panda2134.thssforum.ui.post;

import android.content.Context;
import android.util.AttributeSet;

import com.arges.sepan.argmusicplayer.PlayerViews.ArgPlayerSmallView;

/**
 * Use this in {databinding} instead of {ArgPlayerSmallView} to avoid no such symbol errors
 */
public class MyArgPlayerSmallView extends ArgPlayerSmallView {
    public MyArgPlayerSmallView(Context context) {
        super(context);
    }

    public MyArgPlayerSmallView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MyArgPlayerSmallView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }
}
