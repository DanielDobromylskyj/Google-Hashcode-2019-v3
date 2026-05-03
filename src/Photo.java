import  it.unimi.dsi.fastutil.ints.*;
import java.util.*;

public abstract class Photo {
    private final int tagCount;
    private final boolean isVertical;

    private final String[] stringTags;
    private final IntList tagIDs = new IntArrayList();
    private final int photoID;
    public boolean used = false;

    protected abstract Map<String,Integer> getTagMap();
    protected abstract List<String> getIdToTag();
    protected abstract List<Integer> getTagFrequency();

    public int getTagCount() {
        return this.tagCount;
    }

    public String[] getTags() {
        return this.stringTags;
    }

    public IntList getTagIDs() {
        return this.tagIDs;
    }

    public void ScoredTagsByFrequency(List<Float> tagPopulationMap) {
        // This function replaces:
        // this.tagIDs.sort( Comparator.comparingDouble(tagPopulationMap::get).reversed() );
        // Due to this function repeatedly calling tagPopulationMap::get, causing a slight slow-down
        // This clipping technique really didn't perform as well as I was hoping, and shouldn't be used

        int n = tagIDs.size();

        float[] scores = new float[n];
        for (int i = 0; i < n; i++) {
            scores[i] = tagPopulationMap.get(tagIDs.getInt(i));
        }

        Integer[] indices = new Integer[n];
        for (int i = 0; i < n; i++) indices[i] = i;

        Arrays.sort(indices, (a, b) -> Float.compare(scores[b], scores[a]));

        // reorder tagIDs based on sorted indices
        ArrayList<Integer> newOrder = new ArrayList<>(n);

        // Direction A
        int max = Math.min(indices.length, 15);
        for (int i = 0; i < max; i++) {
            newOrder.add(tagIDs.getInt(indices[i]));
        }


        tagIDs.clear();
        tagIDs.addAll(newOrder);
    }

    public int getTagMapSize() {
        return getTagMap().size();
    }

    public int getPhotoID() {
        return this.photoID;
    }

    public boolean isVertical() {
        return this.isVertical;
    }

    public double getPhotoTotalTagFrequency() {
        List<Integer> freq = getTagFrequency();
        double total = 0.0f;

        for (int tagId : this.tagIDs) {
            total += 1d / (double) freq.get(tagId);
        }

        return total;

    }

    public int convertTagToID(String tag) {
        Map<String, Integer> tagToId = getTagMap();
        List<String> idToTag = getIdToTag();
        List<Integer> tagFrequency = getTagFrequency();

        Integer id = tagToId.get(tag);

        if (id != null) { // Already exists
            tagFrequency.set(id, tagFrequency.get(id) + 1);
            return id;
        }

        // Create new
        int newId = idToTag.size();

        tagToId.put(tag, newId);
        idToTag.add(tag);
        tagFrequency.add(1);

        return newId;
    }

    public static boolean isVertical(String photoData) {
        String[] parts = photoData.split(" ");
        return parts[0].equals("V");
    }

    Photo (String photoData, int photoID) {
        this.photoID = photoID;
        String[] parts = photoData.split(" ");

        // Load / Parse Data
        this.isVertical = parts[0].equals("V");
        this.tagCount = Integer.parseInt(parts[1]);
        this.stringTags = Arrays.copyOfRange(parts, 2, parts.length);  // python_array[2:]

        // Convert Tags to IDs
        for (String tag : this.stringTags) {
            int tagID = this.convertTagToID(tag);
            tagIDs.add(tagID);
        }

        Collections.sort(tagIDs);
    }

    Photo(String[] tags, int photoID) {
        this.photoID = photoID;

        this.isVertical = false; // Must only be vertical (only to be used when combining)
        this.tagCount = tags.length;
        this.stringTags = Arrays.copyOf(tags, tags.length);

        // Convert Tags to IDs
        for (String tag : this.stringTags) {
            int tagID = this.convertTagToID(tag);
            tagIDs.add(tagID);
        }

        Collections.sort(tagIDs);
    }
}
