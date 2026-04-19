// Interface for all operations that can be applied to a media file array.
// Each implementation handles one specific step of the video steps,
// Keeping each step behind this interface makes the pipeline easy to extend
// without touching the classes that already work.
public interface MediaOperation {

    // Executes the operation on the given array of media files.
    // param media: the sorted array of MediaFile objects to process
    // throws Exception if the operation fails at any point
    void execute(MediaFile[] media) throws Exception;
}
