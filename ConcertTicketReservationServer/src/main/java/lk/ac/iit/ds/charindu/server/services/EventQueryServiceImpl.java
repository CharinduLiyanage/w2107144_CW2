package lk.ac.iit.ds.charindu.server.services;

import io.grpc.stub.StreamObserver;
import lk.ac.iit.ds.charindu.grpc.generated.*;
import lk.ac.iit.ds.charindu.server.TicketReservationServer;

public class EventQueryServiceImpl extends EventQueryServiceGrpc.EventQueryServiceImplBase {

    TicketReservationServer server;

    public EventQueryServiceImpl(TicketReservationServer server) {
        this.server = server;
    }

    @Override
    public void getEvent(GetEventRequest request, StreamObserver<GetEventResponse> responseObserver) {
        String eventId = request.getEventId();
        System.out.println("[QUERY] Received GetEvent request: " + eventId);
        GetEventResponse response;
        if (server.isServerReady()) {
            Event event = server.getEvent(eventId);
            if (event == null) {
                event = Event.newBuilder().build();
            }
             response = GetEventResponse
                    .newBuilder()
                    .setEvent(event)
                    .build();
        } else {
            response = GetEventResponse
                    .newBuilder()
                    .build();
        }
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void getEvents(GetEventsRequest request, StreamObserver<GetEventsResponse> responseObserver) {
        System.out.println("[QUERY] Received GetEvents request");
        GetEventsResponse response;
        if (server.isServerReady()) {
             response = GetEventsResponse
                    .newBuilder()
                    .addAllEvents(server.getEvents())
                    .build();
        } else {
            response = GetEventsResponse.newBuilder().build();
        }
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
