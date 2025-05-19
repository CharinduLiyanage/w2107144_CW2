package lk.ac.iit.ds.charindu.transaction;

public interface DistributedTxListener {

    void setTxnStarted(boolean txnStarted);

    void onGlobalCommit();

    void onGlobalAbort();
}
