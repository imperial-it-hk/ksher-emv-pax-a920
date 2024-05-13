package com.evp.payment.ksher.view.progressbar;

class ProgressBarParameter {

    private boolean hideProgress;

    private String primaryContent;

    private String subContent;

    private long timeout = -1;

    private boolean approved = false;
    private boolean declined = false;

    public boolean isHideProgress() {
        return hideProgress;
    }

    public void setHideProgress(boolean hideProgress) {
        this.hideProgress = hideProgress;
    }

    String getPrimaryContent() {
        return primaryContent;
    }

    void setPrimaryContent(String primaryContent) {
        this.primaryContent = primaryContent;
    }

    public boolean isApproved() {
        return approved;
    }

    public void setApproved(boolean approved) {
        this.approved = approved;
    }

    public boolean isDeclined() {
        return declined;
    }

    public void setDeclined(boolean declined) {
        this.declined = declined;
    }

    String getSubContent() {
        return subContent;
    }

    void setSubContent(String subContent) {
        this.subContent = subContent;
    }

    long getTimeout() {
        return timeout;
    }

    void setTimeout(long timeout) {
        this.timeout = timeout;
    }
}
