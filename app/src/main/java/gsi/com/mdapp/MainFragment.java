package gsi.com.mdapp;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

public class MainFragment extends BaseFragment<MainFragment.OnMainFragmentListener> {

    private static final long BUTTON_ANIMATION_DURATION = 1000; // milliseconds

    @ColorInt
    private int mBtnBgColor;
    private Button mBtnPerformAction;

    public MainFragment() {}

    public static MainFragment newInstance() {
        MainFragment fragment = new MainFragment();
        return fragment;
    }

    @Override
    protected void init(Context context) {
        if (context instanceof OnMainFragmentListener) {
            mListener = (OnMainFragmentListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnMainFragmentListener");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_main, container, false);

        mBtnPerformAction = (Button) root.findViewById(R.id.main_frag_btn_action);
        mBtnPerformAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mListener != null) {
                    mListener.onPerformAction();
                }
            }
        });

        mBtnBgColor = ContextCompat.getColor(getActivity(), R.color.colorPrimary);
        setViewBackgroundColor(mBtnPerformAction, mBtnBgColor);
        return root;
    }

    private void setViewBackgroundColor(View view, @ColorInt int color) {
        float radius = dpToPx(3);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            view.setForeground(ColorAdapter.getAdaptiveRippleDrawable(color, radius));
        }
        view.setBackground(ColorAdapter.getSelectorDrawable(color, radius));
        int padding = (int) dpToPx(12);
        view.setPadding(padding, padding, padding, padding);
    }

    public void updateButton(ButtonConfiguration cfg) {
        mBtnBgColor = cfg.getColor();
        mBtnPerformAction.setText(cfg.getTitle());
        setViewBackgroundColor(mBtnPerformAction, cfg.getColor());
    }


    public void animateButton() {
        ObjectAnimator animator1 = ObjectAnimator.ofFloat(mBtnPerformAction,"rotation", 0f, 720f);
        animator1.setDuration(BUTTON_ANIMATION_DURATION);
        @ColorInt int fromColor = mBtnBgColor;
        @ColorInt int toColor = ContextCompat.getColor(getActivity(), R.color.colorPrimary);

        ObjectAnimator animator2 = ObjectAnimator.ofInt(mBtnPerformAction ,"backgroundColor", fromColor, toColor, fromColor);
        animator2.setDuration(BUTTON_ANIMATION_DURATION);
        animator2.setEvaluator(new ArgbEvaluator());

        AnimatorSet set = new AnimatorSet();
        set.playTogether(animator1, animator2);
        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                setViewBackgroundColor(mBtnPerformAction, mBtnBgColor);
            }
        });
        set.start();
    }

    public interface OnMainFragmentListener {
        void onPerformAction();
    }
}
