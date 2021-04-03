package ie.ul.fitbook.recording.services;

/**
 * This interface marks an object that is capable of receiving and processing RecordedLocation objects.
 * Using these objects, the UI can be updated from them. RecordedLocation objects are created by the RecordingService
 * class whenever it comes across a location update that is within a specified accuracy and speed.
 */
public interface RecordedLocationReceiver {
    /**
     * This method is used to "receive" a RecordedLocation from the RecordingService
     * @param recordedLocation the recorded location
     */
    void receive(RecordedLocation recordedLocation);
}
