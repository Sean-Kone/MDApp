package gsi.com.mdapp;

public class ButtonAction {

    public static final float ACTION_PRIORITY_WEIGHT_DISABLED = 0.5f;

    @MDA.ActionType
    private String mType;
    private boolean mIsEnabled;
    private int mPriority;

    public ButtonAction(boolean isEnabled, int priority, String type) {
        this.mIsEnabled = isEnabled;
        this.mPriority = priority;
        this.mType = type;
    }

    public boolean isIsEnabled() {
        return mIsEnabled;
    }

    public void setIsEnabled(boolean isEnabled) {
        this.mIsEnabled = isEnabled;
    }

    public int getPriority() {
        return mPriority;
    }

    public void setPriority(int priority) {
        this.mPriority = priority;
    }

    @MDA.ActionType
    public String getType() {
        return mType;
    }

    public void setType(@MDA.ActionType String type) {
        this.mType = type;
    }



    public static class ActionInfo {

        private int mOccurances;
        private float mPrioritySum;

        public ActionInfo(int occurances, float prioritySum) {
            this.mOccurances = occurances;
            this.mPrioritySum = prioritySum;
        }

        public int getOccurances() {
            return mOccurances;
        }

        public void setOccurances(int occurances) {
            this.mOccurances = occurances;
        }

        public float getPrioritySum() {
            return mPrioritySum;
        }

        public void setPrioritySum(float prioritySum) {
            this.mPrioritySum = prioritySum;
        }

        public float getAveragePriority() {
            return (float)mPrioritySum/(float)mOccurances;
        }
    }
}
