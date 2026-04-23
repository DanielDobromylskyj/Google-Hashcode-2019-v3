import java.util.*;

public class VerticalPhoto extends Photo {

    private static final Map<String,Integer> tagToId = new HashMap<>();
    private static final List<String> idToTag = new ArrayList<>();
    private static final List<Integer> tagFrequency = new ArrayList<>();

    public int getTagMapSize() {
        return idToTag.size();
    }

    protected Map<String,Integer> getTagMap() {
        return tagToId;
    }

    protected List<Integer> getTagFrequency() {
        return tagFrequency;
    }

    protected List<String> getIdToTag() {
        return idToTag;
    }

    VerticalPhoto(String photoData, int photoID) {
        super(photoData, photoID);
    }
}
