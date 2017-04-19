package co.aospa.mandy.view.recyclerview;

import android.support.v7.widget.AppCompatCheckBox;
import android.support.v7.widget.AppCompatTextView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;

import co.aospa.mandy.R;
import co.aospa.mandy.utils.server.MandyStatus;

/**
 * Created by willi on 16.04.17.
 */

public class RepoView extends Item {

    public interface RepoListener {
        void onConflictChecked(MandyStatus.AospaProject aospaProject, boolean conflicted);
    }

    private AppCompatTextView mRepoName;
    private AppCompatTextView mLatestTag;
    private AppCompatCheckBox mConflicted;

    private MandyStatus mMandyStatus;
    private MandyStatus.AospaProject mAospaProject;
    private RepoListener mRepoListener;

    @Override
    public void onBind(View view) {
        mRepoName = (AppCompatTextView) view.findViewById(R.id.repo_name);
        mLatestTag = (AppCompatTextView) view.findViewById(R.id.latest_tag);
        mConflicted = (AppCompatCheckBox) view.findViewById(R.id.conflicted_box);

        setup();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent) {
        return inflater.inflate(R.layout.view_repo, parent, false);
    }

    public void setStatus(MandyStatus mandyStatus, MandyStatus.AospaProject aospaProject,
                          RepoListener repoListener) {
        mMandyStatus = mandyStatus;
        mAospaProject = aospaProject;
        mRepoListener = repoListener;
        setup();
    }

    private void setup() {
        if (mRepoName == null || mLatestTag == null || mConflicted == null) return;
        if (mMandyStatus == null || mAospaProject == null || mRepoListener == null) return;

        mRepoName.setText(mAospaProject.mName);
        mLatestTag.setText(mAospaProject.mLatestTag);
        if (mMandyStatus.mMerged) {
            mConflicted.setVisibility(View.VISIBLE);
            mConflicted.setOnCheckedChangeListener(null);
            mConflicted.setChecked(mAospaProject.mConflicted);
            mConflicted.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    mRepoListener.onConflictChecked(mAospaProject, isChecked);
                }
            });
        } else {
            mConflicted.setVisibility(View.GONE);
        }
    }

}
