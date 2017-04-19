package co.aospa.mandy.view;

import android.content.Context;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.LinearLayoutCompat;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;

import co.aospa.mandy.R;
import co.aospa.mandy.utils.server.MandyStatus;

/**
 * Created by willi on 16.04.17.
 */

public class MandyStatusView extends LinearLayoutCompat {

    public interface MandyStatusViewListener {
        void onMergePressed();

        void onSubmittingPressed();

        void onRevertingPressed();
    }

    private AppCompatTextView mManifestTag;
    private AppCompatTextView mLatestTag;
    private AppCompatTextView mProgressText;
    private ProgressBar mProgress;
    private AppCompatButton mButton1;
    private AppCompatButton mButton2;
    private AppCompatTextView mError;

    private MandyStatus mMandyStatus;

    private MandyStatusViewListener mMandyStatusViewListener;

    public MandyStatusView(Context context) {
        this(context, null);
    }

    public MandyStatusView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MandyStatusView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        LayoutInflater.from(context).inflate(R.layout.view_mandystatus, this);

        mManifestTag = (AppCompatTextView) findViewById(R.id.manifest_tag);
        mLatestTag = (AppCompatTextView) findViewById(R.id.latest_tag);
        mProgressText = (AppCompatTextView) findViewById(R.id.progress_text);
        mProgress = (ProgressBar) findViewById(R.id.progress);
        mButton1 = (AppCompatButton) findViewById(R.id.btn1);
        mButton2 = (AppCompatButton) findViewById(R.id.btn2);
        mError = (AppCompatTextView) findViewById(R.id.error);

        setup();
    }

    public void setStatus(MandyStatus status, MandyStatusViewListener mandyStatusViewListener) {
        mMandyStatus = status;
        mMandyStatusViewListener = mandyStatusViewListener;

        setup();
    }

    private void setup() {
        if (mMandyStatus == null || mMandyStatusViewListener == null) return;

        mManifestTag.setText(mMandyStatus.mManifestTag);
        mLatestTag.setText(mMandyStatus.mLatestTag == null ? "-" : mMandyStatus.mLatestTag);

        mProgressText.setVisibility(GONE);
        mProgress.setVisibility(GONE);
        mProgress.setIndeterminate(true);
        mError.setVisibility(GONE);
        mButton1.setVisibility(GONE);
        mButton2.setVisibility(GONE);
        mButton1.setEnabled(true);
        mButton2.setEnabled(true);
        mButton1.setOnClickListener(null);
        mButton2.setOnClickListener(null);

        if (mMandyStatus.mMerging || mMandyStatus.mSubmitting || mMandyStatus.mReverting) {
            String progressText;
            if (mMandyStatus.mMerging) {
                progressText = getContext().getString(R.string.merging) + "\n" +
                        getContext().getString(R.string.executed_by, mMandyStatus.mMerger.mName);
            } else if (mMandyStatus.mSubmitting) {
                progressText = getContext().getString(R.string.submitting) + "\n" +
                        getContext().getString(R.string.executed_by, mMandyStatus.mSubmitter.mName);
            } else {
                progressText = getContext().getString(R.string.reverting) + "\n" +
                        getContext().getString(R.string.executed_by, mMandyStatus.mReverter.mName);
            }
            mProgressText.setText(progressText);
            mProgressText.setVisibility(VISIBLE);
            mProgress.setVisibility(VISIBLE);
        } else if (mMandyStatus.mMerged) {
            mButton1.setText(getContext().getString(R.string.revert));
            mButton2.setText(getContext().getString(R.string.submit));

            mButton1.setVisibility(VISIBLE);
            mButton2.setVisibility(VISIBLE);
            if (mMandyStatus.mSubmittable) {
                for (MandyStatus.AospaProject aospaProject : mMandyStatus.mAospaProjects) {
                    if (aospaProject.mConflicted) {
                        mError.setVisibility(VISIBLE);
                        mError.setText(getContext().getString(R.string.repos_conflicted));
                        mButton2.setEnabled(false);
                        break;
                    }
                }
            } else {
                mError.setVisibility(VISIBLE);
                mError.setText(getContext().getString(R.string.repos_conflicted));
                mButton2.setEnabled(false);
            }

            mButton1.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    mMandyStatusViewListener.onRevertingPressed();
                }
            });
            mButton2.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    mMandyStatusViewListener.onSubmittingPressed();
                }
            });
        } else if (mMandyStatus.mSubmitted) {
            mError.setVisibility(VISIBLE);
            mError.setText(getContext().getString(R.string.latest_tag_submitted));
        } else {
            boolean mergeable = true;
            if (mMandyStatus.mMergeable) {
                for (MandyStatus.AospaProject project : mMandyStatus.mAospaProjects) {
                    if (!project.mLatestTag.equals(mMandyStatus.mLatestTag)) {
                        mergeable = false;
                        break;
                    }
                }
            }

            if (!mMandyStatus.mManifestTag.equals(mMandyStatus.mLatestTag)) {
                if (mergeable && mMandyStatus.mMergeable) {
                    mButton1.setText(getContext().getString(R.string.merge));
                    mButton1.setVisibility(VISIBLE);

                    mButton1.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mMandyStatusViewListener.onMergePressed();
                        }
                    });
                } else {
                    mError.setVisibility(VISIBLE);
                    mError.setText(getContext().getString(R.string.repos_wrong_tag));
                }
            }
        }
    }

}
