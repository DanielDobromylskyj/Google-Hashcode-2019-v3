import java.util.*;

public class HorizontalPhoto extends Photo {
    private static final Map<String,Integer> tagToId = new HashMap<>();
    private static final List<String> idToTag = new ArrayList<>();
    private static final List<Integer> tagFrequency = new ArrayList<>();

    protected Map<String,Integer> getTagMap() {
        return tagToId;
    }

    protected List<Integer> getTagFrequency() {
        return tagFrequency;
    }

    protected List<String> getIdToTag() {
        return idToTag;
    }

    private static String[] unionTagIDs(String[] tagIDs1, String[] tagIDs2) {
        // Combine and Convert to set to remove duplicates
        Set<String> union = new HashSet<>(List.of(tagIDs1));
        union.addAll(List.of(tagIDs2));

        // Convert back to List
        return union.toArray(new String[0]);
    }

    HorizontalPhoto(String photoData, int photoID) {
        super(photoData, photoID);
    }

    HorizontalPhoto(VerticalPhoto photo1, VerticalPhoto photo2, int photoID) {
        String[] newTags = unionTagIDs(photo1.getTags(), photo2.getTags());
        super(newTags, photoID);
    }
}
