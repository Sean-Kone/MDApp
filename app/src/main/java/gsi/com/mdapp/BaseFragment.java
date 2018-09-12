package gsi.com.mdapp;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.support.v4.app.Fragment;
import android.util.TypedValue;

public abstract class BaseFragment<T> extends Fragment {

    protected T mListener;

    protected abstract void init(Context context);

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        init(context);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            init(activity);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    protected float dpToPx(int px) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, px, getResources().getDisplayMetrics());
    }
}