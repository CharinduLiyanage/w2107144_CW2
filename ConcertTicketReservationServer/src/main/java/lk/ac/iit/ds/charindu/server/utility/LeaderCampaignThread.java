package lk.ac.iit.ds.charindu.server.utility;

import lk.ac.iit.ds.charindu.server.TicketReservationServer;
import lk.ac.iit.ds.charindu.synchronization.DistributedMasterLock;

public class LeaderCampaignThread implements Runnable {
    private final DistributedMasterLock leaderLock;
    private final TicketReservationServer server;
    private byte[] currentLeaderData = null;

    public LeaderCampaignThread(DistributedMasterLock leaderLock, TicketReservationServer server) {
        this.leaderLock = leaderLock;
        this.server = server;
    }

    @Override
    public void run() {
        boolean isLeader = false;
        byte[] leaderData = null;


        System.out.println("Start competing the Master Campaign.. ");
        try {
             isLeader = leaderLock.tryAcquireMasterLock();
            while (!isLeader) {
                leaderData = leaderLock.getMasterData();
                if (currentLeaderData != leaderData) {
                    currentLeaderData = leaderData;
                    server.setCurrentLeaderData(currentLeaderData);
                }
                Thread.sleep(1000);
                isLeader = leaderLock.tryAcquireMasterLock();
            }
            server.beTheLeader();
            currentLeaderData = null;
        } catch (Exception e) {
            System.out.println("Error in LeaderCampaignThread: " + e.getMessage());
        }
    }
}
